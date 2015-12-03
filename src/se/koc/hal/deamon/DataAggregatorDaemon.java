package se.koc.hal.deamon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.ibm.icu.util.Calendar;

import se.koc.hal.PowerChallenge;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
import zutil.db.handler.SimpleSQLHandler;
import zutil.log.LogUtil;

/**
 * Created by Ziver on 2015-12-03.
 */
public class DataAggregatorDaemon extends TimerTask implements HalDaemon {
	private static final Logger logger = LogUtil.getLogger();
    private static final int FIVE_MINUTES_IN_MS = 5 * 60 * 1000;


    public void initiate(Timer timer){
        timer.schedule(this, FIVE_MINUTES_IN_MS);
        run();
    }


    @Override
    public void run() {
    	DBConnection db = PowerChallenge.db;
    	try {
    		Long maxTimestampEnd = db.exec("SELECT MAX(timestamp_end) FROM sensor_data_aggr", new SimpleSQLHandler<Long>());
    		if(maxTimestampEnd == null)
    			maxTimestampEnd = 0l;
    		logger.fine("Calculating 5 min periods...");
    		long intervallTimestamp = getTimestampPeriodStart(5, System.currentTimeMillis());
    		db.exec("SELECT * FROM sensor_data_raw WHERE timestamp > " + maxTimestampEnd + " AND timestamp < " + intervallTimestamp + " ORDER BY timestamp ASC", new FiveMinuteAgrrigator());
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    
    private static long getTimestampPeriodStart(int min, long timestamp){
    	Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(System.currentTimeMillis());
		int currentMinute = cal.get(Calendar.MINUTE);
		cal.set(Calendar.MINUTE, (currentMinute/min) * min);
		return cal.getTimeInMillis();
    }
    
    private class FiveMinuteAgrrigator implements SQLResultHandler<Object>{

		@Override
		public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			long currentPeriodTimestamp = 0;
			int sum = 0;
			int count = 0;
			while(result.next()){
				long timestamp = result.getLong("timestamp");
				long periodTimestamp = getTimestampPeriodStart(5, timestamp);
				if(currentPeriodTimestamp != 0 && periodTimestamp != currentPeriodTimestamp){
					float confidence = count / 5f;
					logger.finer("Calculated period: "+ currentPeriodTimestamp +" sum: "+ sum +" confidence: "+ confidence);
					PowerChallenge.db.exec(String.format("INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(%d, %d, %d, %d, %d, %f)",
							result.getInt("sensor_id"),
							42,
							currentPeriodTimestamp,
							currentPeriodTimestamp + FIVE_MINUTES_IN_MS -1,
							sum,
							confidence));
					
					// Reset variables
					periodTimestamp = currentPeriodTimestamp;
					sum = count = 0;
				}
				if(currentPeriodTimestamp == 0) currentPeriodTimestamp = periodTimestamp;
				sum += result.getInt("data");
				++count;
			}
			return null;
		}
    	
    }
    
}
