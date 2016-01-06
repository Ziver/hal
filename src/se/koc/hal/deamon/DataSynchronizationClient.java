package se.koc.hal.deamon;

import se.koc.hal.HalContext;
import se.koc.hal.deamon.DataSynchronizationDaemon.*;
import se.koc.hal.intf.HalDaemon;
import se.koc.hal.struct.Sensor;
import se.koc.hal.struct.User;
import zutil.db.DBConnection;
import zutil.log.LogUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataSynchronizationClient implements HalDaemon {
	private static final Logger logger = LogUtil.getLogger();
	private static final long SYNC_INTERVAL = 5 * 60 * 1000; // 5 min


	@Override
	public void initiate(ScheduledExecutorService executor){
		executor.scheduleAtFixedRate(this, 10000, SYNC_INTERVAL, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		try {
			DBConnection db = HalContext.getDB();
			List<User> users = User.getExternalUsers(db);
			for(User user : users){
				if(user.getHostname() == null){
					logger.fine("Hostname not defined for user: "+ user.getUserName());
					continue;
				}
				logger.fine("Synchronizing user: "+ user.getUserName() +" ("+user.getHostname()+":"+user.getPort()+")");
				try (Socket s = new Socket(user.getHostname(), user.getPort());){
					ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(s.getInputStream());

                    // Request peer data
                    out.writeObject(new PeerDataReqDTO());
                    PeerDataRspDTO peerData = (PeerDataRspDTO) in.readObject();
                    user.setUserName(peerData.username);
                    user.setAddress(peerData.address);
                    user.save(db);

                    for(SensorDTO sensorDTO : peerData.sensors){
                        Sensor sensor = Sensor.getExternalSensor(db, sensorDTO.sensorId);
                        if(sensor != null) { // new sensor
                            sensor = new Sensor();
                            logger.fine("Created new external sensor with external_id: "+ sensorDTO.sensorId);
                        }
                        else
                            logger.fine("Updating external sensor with external_id: "+ sensorDTO.sensorId);
                        sensor.setExternalId(sensorDTO.sensorId);
                        sensor.setName(sensorDTO.name);
                        sensor.setType(sensorDTO.type);
                        sensor.setConfig(sensorDTO.config);
                        sensor.save(db);
                    }

                    // Request sensor data
					List<Sensor> sensors = Sensor.getSensors(db, user);
					for(Sensor sensor : sensors){
						if(sensor.isSynced()) {
							SensorDataReqDTO req = new SensorDataReqDTO();
							req.sensorId = sensor.getExternalId();
							req.offsetSequenceId = Sensor.getHighestSequenceId(sensor.getId());
							out.writeObject(req);

							SensorDataListDTO dataList = (SensorDataListDTO) in.readObject();
							for (SensorDataDTO data : dataList) {
								PreparedStatement stmt = db.getPreparedStatement("INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(?, ?, ?, ?, ?, ?)");
								stmt.setLong(1, sensor.getId());
								stmt.setLong(2, data.sequenceId);
								stmt.setLong(3, data.timestampStart);
								stmt.setLong(4, data.timestampEnd);
								stmt.setInt(5, data.data);
								stmt.setFloat(6, data.confidence);
								DBConnection.exec(stmt);
							}
							logger.fine("Stored " + dataList.size() + " entries for sensor " + sensor.getId() + " from " + user.getUserName());
						}
                        else
                            logger.fine("Skipped sensor " + sensor.getId());
					}
					out.writeObject(null); // Tell server we are disconnecting
					out.close();
					in.close();
					s.close();
					
				} catch (UnknownHostException|ConnectException e) {
					logger.warning("Unable to connect to: "+ user.getHostname()+":"+user.getPort() +" "+ e.getMessage());
				} catch (ClassNotFoundException|IOException e) {
                    logger.log(Level.SEVERE, null, e);
				}

			}
		} catch (SQLException e) {
            logger.log(Level.SEVERE, null, e);
		}
	}



	///////////////  DTO ///////////////////////

    /**
     * Request Peer information and available sensors
     */
    protected static class PeerDataReqDTO implements Serializable{}

    /**
     * Request aggregate data for a specific sensor and offset
     */
    protected static class SensorDataReqDTO implements Serializable{
		private static final long serialVersionUID = -9066734025245139989L;
		
		public long sensorId;
		public long offsetSequenceId; // highest known sequence id
	}
}
