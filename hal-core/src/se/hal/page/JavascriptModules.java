package se.hal.page;

import se.hal.intf.HalJavascriptModule;

public class JavascriptModules implements HalJavascriptModule {

    @Override
    public HalJsModule[] getJavascriptModules() {
        return new HalJsModule[] {
                new HalJsModule("AlertStore",         "./js/vue/stores/AlertStore.js"),
                new HalJsModule("EventStore",         "./js/vue/stores/EventStore.js"),
                new HalJsModule("SensorStore",        "./js/vue/stores/SensorStore.js"),

                new HalJsModule("AlertComponent",     "./js/vue/components/AlertComponent.js"),
                new HalJsModule("AlertListComponent", "./js/vue/components/AlertListComponent.js"),

                new HalJsModule("EventActionComponent",         "./js/vue/components/EventActionComponent.js"),
                new HalJsModulePage("EventDetailPageComponent",     "./js/vue/components/EventDetailPageComponent.js", "/event/:id"),
                new HalJsModulePage("EventOverviewPageComponent",   "./js/vue/components/EventOverviewPageComponent.js", "/event_overview"),
                new HalJsModule("EventTableComponent",          "./js/vue/components/EventTableComponent.js"),
                new HalJsModule("EventTableRowComponent",       "./js/vue/components/EventTableRowComponent.js"),
        };
    }
}
