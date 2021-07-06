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

package se.hal.plugin.zigbee;

import com.zsmartsystems.zigbee.ZigBeeEndpoint;
import com.zsmartsystems.zigbee.ZigBeeNetworkManager;
import com.zsmartsystems.zigbee.ZigBeeNode;
import com.zsmartsystems.zigbee.zcl.ZclAttribute;
import com.zsmartsystems.zigbee.zcl.ZclCluster;
import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalDeviceReportListener;
import zutil.log.CompactLogFormatter;
import zutil.log.LogUtil;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;


public class HalZigbeeControllerTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        LogUtil.readConfiguration("logging.properties");
        LogUtil.setGlobalFormatter(new CompactLogFormatter());
        LogUtil.setGlobalLevel(Level.ALL);

        ZigbeeController controller = new ZigbeeController();
        controller.initialize("COM5", ZigbeeController.ZIGBEE_DONGLE_CC2531);
        controller.addListener(new HalDeviceReportListener() {
            @Override
            public void reportReceived(HalDeviceConfig deviceConfig, HalDeviceData deviceData) {
                System.out.println("Device reported: " + deviceConfig + " , " + deviceData);
            }
        });

        Scanner in = new Scanner(System.in);
        handleConsoleInput("h", in, controller.networkManager);

        while (true) {
            System.out.print("");
            System.out.print("Input command and finish with ENTER: ");

            while (!in.hasNext()) { Thread.sleep(200); }

            String command = in.next().trim();
            handleConsoleInput(command, in, controller.networkManager);
            in.nextLine(); // read in the rest of the input line

            if (command.equals("q")) break;
        }

        controller.close();
        System.exit(0);
    }


    private static void handleConsoleInput(String command, Scanner in, ZigBeeNetworkManager networkManager) {
        switch (command) {
            case "i":
                System.out.println("PAN ID          = " + networkManager.getZigBeePanId());
                System.out.println("Extended PAN ID = " + networkManager.getZigBeeExtendedPanId());
                System.out.println("Channel         = " + networkManager.getZigBeeChannel());
                break;

            case "l":
                System.out.println("-----------------------------------------------------------------------");
                for (ZigBeeNode node : networkManager.getNodes()) {
                    System.out.println("[node id: " + node.getNetworkAddress() + "] " + node + " (" + node.getNodeState() + ")");

                    for (ZigBeeEndpoint endpoint : node.getEndpoints()) {
                        System.out.println("  - [endpoint id: " + endpoint.getDeviceId() + "] " + endpoint);
                        System.out.println("    - Input Clusters:");

                        for (int inputClusterId : endpoint.getInputClusterIds()) {
                            ZclCluster cluster = endpoint.getInputCluster(inputClusterId);
                            System.out.println("      - [cluster id: " + inputClusterId + "] " + cluster);

                            if (cluster != null) {
                                for (ZclAttribute attr : cluster.getAttributes()) {
                                    System.out.println("        - [attr id: " + attr.getId() + "] " + attr);
                                }
                            }
                        }

                        System.out.println("    - Output Clusters:");

                        for (int outputClusterId : endpoint.getOutputClusterIds()) {
                            ZclCluster cluster = endpoint.getInputCluster(outputClusterId);
                            System.out.println("      - [cluster id: " + outputClusterId + "] " + endpoint);

                            if (cluster != null) {
                                for (ZclAttribute attr : cluster.getAttributes()) {
                                    System.out.println("        - [attr id: " + attr.getId() + "] " + attr);
                                }
                            }
                        }
                    }

                    System.out.println("  Number of Endpoints: " + node.getEndpoints().size());
                    System.out.println();
                }
                System.out.println("--------------------------");
                System.out.println("Number of ZigBee Nodes: " + networkManager.getNodes().size());
                System.out.println("-----------------------------------------------------------------------");
                break;

            case "a":
                ZigBeeNode node = networkManager.getNode(in.nextInt());
                ZigBeeEndpoint endpoint = node.getEndpoint(in.nextInt());
                System.out.println("  - [id: " + endpoint.getDeviceId() + "]" + endpoint);
                break;

            case "p":
                System.out.println("Enabling pairing.");
                networkManager.permitJoin(200);
                break;

            case "q":
                System.out.println("Shutting down.");
                break;

            case "h":
            default:
                System.out.println("Available commands:");
                System.out.println("  i: List network info");
                System.out.println("  l: List available ZigBee Nodes");
                System.out.println("  a <node id> <endpoint id> <attribute id>: read attribute");
                System.out.println("  p: Enable pairing of ZigBee devices");
                System.out.println("  q: Quit");
                System.out.println("  h: Help text");
                break;
        }
    }
}