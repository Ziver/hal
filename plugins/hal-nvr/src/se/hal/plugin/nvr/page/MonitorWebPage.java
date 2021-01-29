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
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.Map;

public class MonitorWebPage extends HalWebPage {
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/camera_monitor.tmpl";

    public MonitorWebPage() {
        super("camera_monitor");
        super.getRootNav().createSubNav("Surveillance").createSubNav(this.getId(), "Monitoring").setWeight(-100);
    }

    @Override
    public Templator httpRespond(Map<String, Object> session, Map<String, String> cookie, Map<String, String> request) throws Exception {
        DBConnection db = HalContext.getDB();

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("stream1", "https://multiplatform-f.akamaihd.net/i/multi/will/bunny/big_buck_bunny_,640x360_400,640x360_700,640x360_1000,950x540_1500,.f4v.csmil/master.m3u8");
        tmpl.set("stream2", "http://vjs.zencdn.net/v/oceans.mp4");
        tmpl.set("stream3", "http://vjs.zencdn.net/v/oceans.mp4");
        tmpl.set("stream4", "http://vjs.zencdn.net/v/oceans.mp4");

        return tmpl;
    }
}
