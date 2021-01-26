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

package se.hal.plugin.nvr.page;

import se.hal.HalContext;
import se.hal.intf.HalWebPage;
import se.hal.plugin.nvr.device.Camera;
import se.hal.struct.Event;
import se.hal.util.DeviceNameComparator;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.Templator;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CameraOverviewWebPage extends HalWebPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final int HISTORY_LIMIT = 200;
    private static final String OVERVIEW_TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/camera_overview.tmpl";
    private static final String DETAIL_TEMPLATE   = HalContext.RESOURCE_WEB_ROOT + "/camera_detail.tmpl";


    public CameraOverviewWebPage(){
        super("camera_overview");
        super.getRootNav().createSubNav("Surveillance").createSubNav(this.getId(), "Overview");
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        DBConnection db = HalContext.getDB();

        int id = (ObjectUtil.isEmpty(request.get("id")) ? -1 : Integer.parseInt(request.get("id")));

        // Save new input
        if (id >= 0) {
            Camera camera = Camera.getCamera(db, id);

            Templator tmpl = new Templator(FileUtil.find(DETAIL_TEMPLATE));
            tmpl.set("camera", camera);
            return tmpl;
        }
        else {
            List<Event> events = Event.getLocalEvents(db);
            Collections.sort(events, DeviceNameComparator.getInstance());

            Templator tmpl = new Templator(FileUtil.find(OVERVIEW_TEMPLATE));
            tmpl.set("events", events);
            return tmpl;
        }
    }
}
