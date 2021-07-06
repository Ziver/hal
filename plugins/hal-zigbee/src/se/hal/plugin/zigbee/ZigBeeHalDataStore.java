/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Ziver Koc
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

package se.hal.plugin.zigbee;

import com.zsmartsystems.zigbee.IeeeAddress;
import com.zsmartsystems.zigbee.database.ZigBeeNetworkDataStore;
import com.zsmartsystems.zigbee.database.ZigBeeNodeDao;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.db.handler.ListSQLResult;
import zutil.io.StringInputStream;
import zutil.log.LogUtil;
import zutil.parser.json.JSONObjectInputStream;
import zutil.parser.json.JSONObjectOutputStream;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


public class ZigBeeHalDataStore implements ZigBeeNetworkDataStore {
    private static final Logger logger = LogUtil.getLogger();

    private DBConnection db;


    public ZigBeeHalDataStore(DBConnection db) {
        this.db = db;
    }


    @Override
    public Set<IeeeAddress> readNetworkNodes() {
        Set<IeeeAddress> ieeeAddresses = new HashSet<>();

        try {
            PreparedStatement stmt = db.getPreparedStatement( "SELECT address FROM zigbee_node" );
            List<String> strAddresses = DBConnection.exec(stmt, new ListSQLResult<String>());

            for (String address : strAddresses) {
                ieeeAddresses.add(new IeeeAddress(address));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ieeeAddresses;
    }

    @Override
    public ZigBeeNodeDao readNode(IeeeAddress address) {
        try {
            ZigbeeNodeDSO dso = ZigbeeNodeDSO.load(db, address.toString());
            if (dso != null)
                return dso.getConfig();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void writeNode(ZigBeeNodeDao node) {
        try {
            logger.fine("[Node: " + node.getIeeeAddress() + "]: Storing Zigbee Node in DB: " +
                    "NetAddr: " + node.getNetworkAddress() + ", " +
                    "binding: " + node.getBindingTable() + ", " +
                    "description: " + node.getNodeDescriptor() + ", " +
                    "endpoints: " + node.getEndpoints() + ", " +
                    "Power: " + node.getPowerDescriptor()
            );

            ZigbeeNodeDSO dso = ZigbeeNodeDSO.load(db, node.getIeeeAddress().toString());
            if (dso == null) {
                dso = new ZigbeeNodeDSO();
                dso.address = node.getIeeeAddress().toString();
            }

            dso.setConfig(node);
            dso.save(db);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeNode(IeeeAddress address) {
        try {
            logger.fine("[Node: " + address + "]: Removing Node from DB.");

            ZigbeeNodeDSO dso = ZigbeeNodeDSO.load(db, address.toString());
            if (dso != null)
                dso.delete(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * A private data storage object connected to the DB schema.
     */
    private static class ZigbeeNodeDSO extends DBBean {
        protected String address;
        protected String config;


        public static ZigbeeNodeDSO load(DBConnection db, String address) throws SQLException{
            PreparedStatement stmt = db.getPreparedStatement( "SELECT * FROM zigbee_node WHERE ? == zigbee_node.address" );
            stmt.setString(1, address);
            return DBConnection.exec(stmt, DBBeanSQLResultHandler.create(ZigbeeNodeDSO.class, db));
        }


        protected void setConfig(ZigBeeNodeDao node) {
            config = JSONObjectOutputStream.toString(node);
        }

        protected ZigBeeNodeDao getConfig() {
            JSONObjectInputStream in = new JSONObjectInputStream(new StringInputStream(config));
            in.registerRootClass(ZigBeeNodeDao.class);
            return (ZigBeeNodeDao) in.readObject();
        }
    }
}
