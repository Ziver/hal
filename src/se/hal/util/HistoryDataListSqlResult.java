package se.hal.util;

import zutil.db.SQLResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HistoryDataListSqlResult implements SQLResultHandler<List<HistoryDataListSqlResult.HistoryData>> {
        public static class HistoryData{
            public long timestamp;
            public double data;
        }
        
        @Override
        public List<HistoryData> handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
            ArrayList<HistoryData> list = new ArrayList<HistoryData>();
            while(result.next()){
                HistoryData data = new HistoryData();
                data.timestamp = result.getLong("timestamp");
                data.data = result.getLong("data");
                list.add(data);
            }
            return list;
        }
    }