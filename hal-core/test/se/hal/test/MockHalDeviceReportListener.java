package se.hal.test;

import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalDeviceReportListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Class will store all reports to be used later in a test to verify the behaviours
 */
public class MockHalDeviceReportListener implements HalDeviceReportListener {

    private List<DeviceTestReport> reports = new ArrayList<>();


    public int getNumberOfReports() {
        return reports.size();
    }

    public DeviceTestReport getReport(int index) {
        return reports.get(index);
    }

    public void reset() {
        reports = new ArrayList<>();
    }

    @Override
    public void reportReceived(HalDeviceConfig deviceConfig, HalDeviceData deviceData) {
        reports.add(new DeviceTestReport(deviceConfig, deviceData));
    }


    public static class DeviceTestReport {
        public HalDeviceConfig config;
        public HalDeviceData data;

        private DeviceTestReport(HalDeviceConfig config, HalDeviceData data) {
            this.config = config;
            this.data = data;
        }
    }
}
