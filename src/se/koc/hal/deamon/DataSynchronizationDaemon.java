package se.koc.hal.deamon;

import se.koc.hal.HalContext;
import se.koc.hal.deamon.DataSynchronizationClient.PeerDataReqDTO;
import se.koc.hal.intf.HalDaemon;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
import zutil.log.LogUtil;
import zutil.net.threaded.ThreadedTCPNetworkServer;
import zutil.net.threaded.ThreadedTCPNetworkServerThread;

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
import java.util.logging.Logger;

public class DataSynchronizationDaemon extends ThreadedTCPNetworkServer implements HalDaemon {
	private static final Logger logger = LogUtil.getLogger();


	public DataSynchronizationDaemon() {
		super(HalContext.getIntegerProperty("sync_port"));
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
			e.printStackTrace();
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

			try {
				Object obj = null;
				while((obj = in.readObject()) != null){
					if(obj instanceof PeerDataReqDTO){
						PeerDataReqDTO req = (PeerDataReqDTO) obj;
						PreparedStatement stmt = HalContext.getDB().getPreparedStatement("SELECT * FROM sensor_data_aggr WHERE sensor_id == ? AND sequence_id > ?");
						stmt.setLong(1, req.sensorId);
						stmt.setLong(2, req.offsetSequenceId);
						logger.fine("Client requesting: sensorId: "+req.sensorId+", offset: "+req.offsetSequenceId);
						SensorDataListDTO list = DBConnection.exec(stmt, new SQLResultHandler<SensorDataListDTO>() {
									@Override
									public SensorDataListDTO handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
										SensorDataListDTO list = new SensorDataListDTO();
										while(result.next()){
											SensorDataDTO data = new SensorDataDTO();
											data.sequenceId = result.getLong("sensor_id");
											data.timestampStart = result.getLong("timestamp_start");
											data.timestampEnd = result.getLong("timestamp_end");
											data.data = result.getInt("data");
											data.confidence = result.getFloat("confidence");
											list.add(data);
										}
										return list;
									}
						});
						logger.fine("Sending "+ list.size() +" sensor data items to client");
						out.writeObject(list);
					}
				}
                out.close();
                in.close();
				s.close();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
            logger.fine("User disconnected: "+ s.getInetAddress().getHostName());
		}
	}

	///////////////  DTO ///////////////////////
	protected static class SensorDataListDTO extends ArrayList<SensorDataDTO> implements Serializable{
		private static final long serialVersionUID = -5701618637734020691L;	
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
