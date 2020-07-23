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
import se.hal.plugin.powerchallenge.daemon.PCDataSynchronizationClient.PeerDataReqDTO;
import se.hal.plugin.powerchallenge.daemon.PCDataSynchronizationClient.SensorDataReqDTO;
import se.hal.intf.HalDaemon;
import se.hal.struct.Sensor;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
import zutil.log.LogUtil;
import zutil.net.threaded.ThreadedTCPNetworkServer;
import zutil.net.threaded.ThreadedTCPNetworkServerThread;
import zutil.parser.json.JSONWriter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PCDataSynchronizationDaemon extends ThreadedTCPNetworkServer implements HalDaemon {
    private static final Logger logger = LogUtil.getLogger();

    public static final String PROPERTY_SYNC_PORT = "powerchallenge.sync_port";
    public static final int PROTOCOL_VERSION = 5; // Increment for protocol changes


    public PCDataSynchronizationDaemon() {
        super(HalContext.getIntegerProperty(PROPERTY_SYNC_PORT));
    }

    @Override
    public void initiate(ScheduledExecutorService executor){
        this.start();
    }



    @Override
    protected ThreadedTCPNetworkServerThread getThreadInstance(Socket s) {
        try {
            return new DataSynchronizationDaemonThread(s);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to create DataSynchronizationDaemonThread", e);
        }
        return null;
    }


    private class DataSynchronizationDaemonThread implements ThreadedTCPNetworkServerThread{
        private Socket s;
        private ObjectOutputStream out;
        private ObjectInputStream in;


        public DataSynchronizationDaemonThread(Socket s) throws IOException{
            this.s = s;
            this.out = new ObjectOutputStream(s.getOutputStream());
            this.in = new ObjectInputStream(s.getInputStream());
        }


        public void run(){
            logger.fine("User connected: "+ s.getInetAddress().getHostName());
            DBConnection db = HalContext.getDB();

            try {
                Object obj = null;
                out.writeInt(PROTOCOL_VERSION); // send our protocol version to client
                out.flush();
                while((obj = in.readObject()) != null){
                    if(obj instanceof PeerDataReqDTO){
                        logger.fine("Client requesting peer data");
                        PeerDataRspDTO rsp = new PeerDataRspDTO();
                        User localUser = User.getLocalUser(db);
                        rsp.username = localUser.getUsername();
                        rsp.email = localUser.getEmail();
                        rsp.address = localUser.getAddress();

                        rsp.sensors = new ArrayList<>();
                        for(Sensor sensor : Sensor.getLocalSensors(db)){
                            if(sensor.isSynced()) {
                                SensorDTO dto = new SensorDTO();
                                dto.sensorId = sensor.getId();
                                dto.name = sensor.getName();
                                dto.type = sensor.getType();
                                dto.config = JSONWriter.toString(sensor.getDeviceConfigurator().getValuesAsNode());
                                rsp.sensors.add(dto);
                            }
                        }
                        out.writeObject(rsp);
                    }
                    if(obj instanceof SensorDataReqDTO){
                        SensorDataReqDTO req = (SensorDataReqDTO) obj;
                        Sensor sensor = Sensor.getSensor(db, req.sensorId);
                        if(sensor.isSynced()) {
                            PreparedStatement stmt = db.getPreparedStatement("SELECT * FROM sensor_data_aggr WHERE sensor_id == ? AND sequence_id > ?");
                            stmt.setLong(1, sensor.getId());
                            logger.fine("Client requesting sensor data: sensorId: " + req.sensorId + ", offset: " + req.offsetSequenceId + ", " + req.aggregationVersion);
                            if(req.aggregationVersion != sensor.getAggregationVersion()){
                                logger.fine("The requested aggregation version does not match the local version: " + sensor.getAggregationVersion() + ". Will re-send all aggregated data.");
                                stmt.setLong(2, 0);	//0 since we want to re-send all data to the peer
                            }else{
                                stmt.setLong(2, req.offsetSequenceId);
                            }

                            SensorDataListDTO rsp = DBConnection.exec(stmt, new SQLResultHandler<SensorDataListDTO>() {
                                @Override
                                public SensorDataListDTO handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
                                    SensorDataListDTO list = new SensorDataListDTO();
                                    while (result.next()) {
                                        SensorDataDTO data = new SensorDataDTO();
                                        data.sequenceId = result.getLong("sequence_id");
                                        data.timestampStart = result.getLong("timestamp_start");
                                        data.timestampEnd = result.getLong("timestamp_end");
                                        data.data = result.getInt("data");
                                        data.confidence = result.getFloat("confidence");
                                        list.add(data);
                                    }
                                    return list;
                                }
                            });
                            rsp.aggregationVersion = sensor.getAggregationVersion();
                            logger.fine("Sending " + rsp.size() + " sensor data items to client");
                            out.writeObject(rsp);
                        }
                        else{
                            logger.warning("Client requesting non synced sensor data: sensorId: " + req.sensorId + ", offset: " + req.offsetSequenceId);
                            SensorDataListDTO rsp = new SensorDataListDTO();
                            out.writeObject(rsp);
                        }
                    }
                }
                out.close();
                in.close();
                s.close();

            } catch (Exception e) {
                logger.log(Level.SEVERE, null, e);
            }
            logger.fine("User disconnected: "+ s.getInetAddress().getHostName());
        }
    }

    ///////////////  DTO ///////////////////////
    protected static class PeerDataRspDTO implements Serializable{
        public String username;
        public String email;
        public String address;

        public ArrayList<SensorDTO> sensors;
    }
    protected static class SensorDTO implements Serializable{
        public long sensorId;
        public String name;
        public String type;
        public String config;
    }


    protected static class SensorDataListDTO extends ArrayList<SensorDataDTO> implements Serializable{
        private static final long serialVersionUID = -5701618637734020691L;

        public long aggregationVersion = 0;
    }
    protected static class SensorDataDTO implements Serializable{
        private static final long serialVersionUID = 8494331502087736809L;

        public long sequenceId;
        public long timestampStart;
        public long timestampEnd;
        public int data;
        public float confidence;
    }
}
