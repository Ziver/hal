package se.hal.util;

import se.hal.intf.HalDeviceData;
import zutil.ClassUtil;
import zutil.db.SQLResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by Ziver on 2016-08-22.
 */
public class DeviceDataSqlResult implements SQLResultHandler<HalDeviceData> {

    private Class<? extends HalDeviceData> clazz;


    public DeviceDataSqlResult(Class<? extends HalDeviceData> clazz){
        this.clazz = clazz;
    }


    @Override
    public HalDeviceData handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
        try {
            HalDeviceData dataObj = clazz.newInstance();
            dataObj.setData(result.getDouble("data"));
            dataObj.setTimestamp(result.getLong("timestamp"));
            return dataObj;
        } catch (SQLException e){
            throw e;
        } catch (Exception e){
            throw new SQLException(e);
        }
    }
}
