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

package se.hal.plugin.nvr.struct;

import se.hal.intf.HalAbstractController;
import se.hal.plugin.nvr.intf.HalCameraConfig;
import se.hal.plugin.nvr.intf.HalCameraData;
import se.hal.intf.HalAbstractDevice;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;

import java.sql.SQLException;
import java.util.List;


public class Camera extends HalAbstractDevice<Camera, HalCameraConfig, HalCameraData> {

    public static List<Camera> getCameras(DBConnection db) throws SQLException{
        return DBBean.load(db, Camera.class);
    }

    public static Camera getCamera(DBConnection db, long id) throws SQLException {
        return DBBean.load(db, Camera.class, id);
    }


    @Override
    public Class<? extends HalAbstractController> getController() {
        return getDeviceConfig().getDeviceControllerClass();
    }

    @Override
    protected HalCameraData getLatestDeviceData(DBConnection db) {
        return null;
    }
}