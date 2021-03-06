package se.hal.util;

import se.hal.intf.HalDeviceData;
import zutil.db.SQLResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DeviceDataSqlResult implements SQLResultHandler<HalDeviceData> {

    private Class<? extends HalDeviceData> clazz;


    public DeviceDataSqlResult(Class<? extends HalDeviceData> clazz) {
        this.clazz = clazz;
    }


    @Override
    public HalDeviceData handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
        try {
            if (result.next()) {
                HalDeviceData dataObj = clazz.newInstance();
                dataObj.setData(result.getDouble("data"));
                dataObj.setTimestamp(result.getLong("timestamp"));
                return dataObj;
            }
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException(e);
        }
        return null;
    }
}
