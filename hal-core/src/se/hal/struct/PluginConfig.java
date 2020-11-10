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

package se.hal.struct;

import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;


@DBBean.DBTable(value="plugin")
public class PluginConfig extends DBBean {
    private String name;
    private boolean enabled;


    /**
     * @return a PluginConfig bean for the specific plugin name.
     */
    public static PluginConfig getPluginConfig(DBConnection db, String name) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement( "SELECT plugin.* FROM plugin WHERE name == ?" );
        stmt.setString(1, name);
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.create(PluginConfig.class, db) );
    }


    public PluginConfig() {}
    public PluginConfig(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
