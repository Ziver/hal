/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Ziver Koc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.hal.plugin.powerchallenge.daemon;

import se.hal.HalContext;
import se.hal.intf.HalDaemon;
import se.hal.page.HalAlertManager;
import se.hal.plugin.powerchallenge.daemon.PCDataSynchronizationDaemon.PeerDataRspDTO;
import se.hal.plugin.powerchallenge.daemon.PCDataSynchronizationDaemon.SensorDTO;
import se.hal.plugin.powerchallenge.daemon.PCDataSynchronizationDaemon.SensorDataDTO;
import se.hal.plugin.powerchallenge.daemon.PCDataSynchronizationDaemon.SensorDataListDTO;
import se.hal.struct.Sensor;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.parser.json.JSONParser;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static zutil.ui.UserMessageManager.*;

public class PCDataSynchronizationClient implements HalDaemon, Runnable {
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
            for (User user : users){
                if (user.getHostname() == null){
                    logger.fine("Hostname not defined for user: "+ user.getUsername());
                    continue;
                }

                logger.fine("Synchronizing user: "+ user.getUsername() +" ("+user.getHostname()+":"+user.getPort()+")");
                try (Socket s = new Socket(user.getHostname(), user.getPort());){
                    ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(s.getInputStream());

                    // Check server protocol version
                    int version = in.readInt();
                    if (version != PCDataSynchronizationDaemon.PROTOCOL_VERSION){
                        logger.warning("Protocol version do not match, skipping user. " +
                                "(local v"+PCDataSynchronizationDaemon.PROTOCOL_VERSION+" != remote v"+version+")");
                        out.writeObject(null); // Tell server we are disconnecting
                        out.flush();
                        continue;
                    }

                    // Request peer data
                    out.writeObject(new PeerDataReqDTO());
                    PeerDataRspDTO peerData = (PeerDataRspDTO) in.readObject();
                    user.setUsername(peerData.username);
                    user.setEmail(peerData.email);
                    user.setAddress(peerData.address);
                    user.save(db);

                    for (SensorDTO sensorDTO : peerData.sensors){
                        try { // We might not have the sensor plugin installed
                            Sensor sensor = Sensor.getExternalSensor(db, user, sensorDTO.sensorId);
                            if (sensor == null) { // new sensor
                                sensor = new Sensor();
                                logger.fine("Created new external sensor with external_id: "+ sensorDTO.sensorId);
                            }
                            else
                                logger.fine("Updating external sensor with id: "+ sensor.getId() +" and external_id: "+ sensor.getExternalId());
                            sensor.setExternalId(sensorDTO.sensorId);
                            sensor.setName(sensorDTO.name);
                            sensor.setType(sensorDTO.type);
                            sensor.setUser(user);

                            sensor.getDeviceConfigurator().setValues(JSONParser.read(sensorDTO.config)).applyConfiguration();
                            sensor.save(db);
                        } catch (Exception e){
                            logger.warning("Unable to register external sensor: " +
                                    "name="+sensorDTO.name+", type="+ sensorDTO.type);
                        }
                    }

                    // Request sensor data
                    List<Sensor> sensors = Sensor.getSensors(db, user);
                    for (Sensor sensor : sensors){
                        if (sensor.isSynced()) {
                            SensorDataReqDTO req = new SensorDataReqDTO();
                            req.sensorId = sensor.getExternalId();
                            req.offsetSequenceId = Sensor.getHighestSequenceId(sensor.getId());
                            req.aggregationVersion = sensor.getAggregationVersion();
                            out.writeObject(req);

                            SensorDataListDTO dataList = (SensorDataListDTO) in.readObject();
                            if (dataList.aggregationVersion != sensor.getAggregationVersion()){
                                logger.fine("The peer has modified its aggregated data, clearing aggregate data. oldAggregationVersion:"+sensor.getAggregationVersion()+" , newAggregationVersion:"+dataList.aggregationVersion);

                                //clear old aggregated data for sensor
                                sensor.clearAggregatedData(db);

                                //save new aggregationVersion
                                sensor.setAggregationVersion(dataList.aggregationVersion);
                                sensor.save(db);
                            }
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
                            logger.fine("Stored " + dataList.size() + " entries for sensor " + sensor.getId() + " with offset "+ req.offsetSequenceId +" from " + user.getUsername());
                        }
                        else
                            logger.fine("Sensor not marked for syncing, skipping sensor id: " + sensor.getId());
                    }
                    out.writeObject(null); // Tell server we are disconnecting

                } catch (NoRouteToHostException|UnknownHostException|ConnectException|SocketTimeoutException e) {
                    logger.warning("Unable to connect to "+ user.getHostname()+":"+user.getPort() +", "+ e.getMessage());
                    HalAlertManager.getInstance().addAlert(new UserMessage(MessageLevel.WARNING,
                            "Unable to connect to user with host: "+user.getHostname(), MessageTTL.DISMISSED));
                } catch (Exception e) {
                    logger.log(Level.SEVERE, null, e);
                }

            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Thread has crashed", e);
        }
    }


    // ----------------------------------------------------
    //                     DTO
    // ----------------------------------------------------

    /**
     * Request Peer information and isAvailable sensors
     */
    protected static class PeerDataReqDTO implements Serializable{}

    /**
     * Request aggregate data for a specific sensor and offset
     */
    protected static class SensorDataReqDTO implements Serializable{
        private static final long serialVersionUID = -9066734025245139989L;

        public long sensorId;
        public long offsetSequenceId; // highest known sequence id
        public long aggregationVersion = 0;
    }
}
