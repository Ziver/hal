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

package se.hal.plugin.powerchallenge.page;

import se.hal.HalContext;
import se.hal.intf.HalWebPage;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.Map;

public class PCHeatMapWebPage extends HalWebPage {
    private static final String TEMPLATE = HalContext.RESOURCE_ROOT + "/web/pc_heatmap.tmpl";


    public PCHeatMapWebPage() {
        super("pc_heatmap");
        super.getRootNav().createSubNav("Sensors").createSubNav(this.getId(), "Heatmap").setWeight(60);
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        return tmpl;
    }

}
