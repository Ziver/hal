package se.koc.hal.deamon;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import se.koc.hal.HalContext;
import se.koc.hal.deamon.DataSynchronizationDaemon.SensorDataDTO;
import se.koc.hal.deamon.DataSynchronizationDaemon.SensorDataListDTO;
import se.koc.hal.struct.Sensor;
import se.koc.hal.struct.User;
import zutil.db.DBConnection;
import zutil.log.LogUtil;

public class DataSynchronizationClient extends TimerTask implements HalDaemon{
	private static final Logger logger = LogUtil.getLogger();
	private static final long SYNC_INTERVALL = 5 * 60 * 1000; // 5 min


	@Override
	public void initiate(Timer timer) {
		timer.schedule(this, 10000, SYNC_INTERVALL);
	}

	@Override
	public void run() {
		try {
			DBConnection db = HalContext.db;
			List<User> users = User.getExternalUsers(db);
			for(User user : users){
				if(user.getHostname() == null){
					logger.fine("Hostname not defined for user: "+ user.getName());
					continue;
				}
				logger.fine("Synchronizing user: "+ user.getName() +" ("+user.getHostname()+":"+user.getPort()+")");
				try (Socket s = new Socket(user.getHostname(), user.getPort());){
					ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
					ObjectInputStream in = new ObjectInputStream(s.getInputStream());
					
					List<Sensor> sensors = Sensor.getSensors(db, user);
					for(Sensor sensor : sensors){
						PeerDataReqDTO req = new PeerDataReqDTO();
						req.sensorId = sensor.getExternalId();	
						req.offsetSequenceId = Sensor.getHighestSequenceId(sensor.getId());
						out.writeObject(req);
						
						SensorDataListDTO dataList = (SensorDataListDTO) in.readObject();
						for(SensorDataDTO data : dataList){
							int deletions = db.exec("DELETE FROM sensor_data_aggr WHERE sensor_id == "+ sensor.getId() +" AND "+ data.timestampStart +" <= timestamp_start AND timestamp_end <= "+ data.timestampEnd);
							if(deletions > 0)
								logger.finer("Aggregate data replaced "+ deletions +" entries");
							db.exec(String.format(Locale.US, "INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(%d, %d, %d, %d, %d, %f)",
									sensor.getId(),
									data.sequenceId,
									data.timestampStart,
									data.timestampEnd,
									data.data,
									data.confidence));
						}
						logger.fine("Stored " + dataList.size() + " entries for sensor " + sensor.getId() + " from " + user.getName());
					}
					out.writeObject(null);
					out.close();
					in.close();
					s.close();
					
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}



	///////////////  DTO ///////////////////////
	protected static class PeerDataReqDTO implements Serializable{
		private static final long serialVersionUID = -9066734025245139989L;
		
		public long sensorId;
		public long offsetSequenceId; // highest known sequence id
	}
}
