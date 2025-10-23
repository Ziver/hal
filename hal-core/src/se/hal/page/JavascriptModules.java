/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Ziver Koc
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

package se.hal.page;

import se.hal.intf.HalJavascriptModule;
import se.hal.intf.HalWebPage;

public class JavascriptModules implements HalJavascriptModule {

    @Override
    public HalJsModule[] getJavascriptModules() {
        HalWebPage.getRootNav().createSubNav("Events").createSubNav("events", "Overview");

        return new HalJsModule[] {
                new HalJsModule("AlertStore",         "./js/vue/stores/AlertStore.js"),
                new HalJsModule("EventStore",         "./js/vue/stores/EventStore.js"),
                new HalJsModule("SensorStore",        "./js/vue/stores/SensorStore.js"),

                new HalJsModule("AlertComponent",     "./js/vue/components/AlertComponent.js"),
                new HalJsModule("AlertListComponent", "./js/vue/components/AlertListComponent.js"),

                new HalJsModule("EventActionComponent",         "./js/vue/components/EventActionComponent.js"),
                new HalJsModulePage("EventDetailPageComponent",     "./js/vue/components/EventDetailPageComponent.js", "/event/:id"),
                new HalJsModulePage("EventOverviewPageComponent",   "./js/vue/components/EventOverviewPageComponent.js", "/events"),
                new HalJsModule("EventTableComponent",          "./js/vue/components/EventTableComponent.js"),
                new HalJsModule("EventTableRowComponent",       "./js/vue/components/EventTableRowComponent.js"),
        };
    }
}
