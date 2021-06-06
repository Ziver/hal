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
import zutil.log.CompactLogFormatter;
import zutil.log.LogUtil;

import java.io.IOException;
import java.util.logging.Level;


public class HalZigbeeControllerTest {

    public static void main(String[] args) throws IOException {
        LogUtil.readConfiguration("logging.properties");
        LogUtil.setGlobalFormatter(new CompactLogFormatter());
        LogUtil.setGlobalLevel(Level.ALL);

        HalZigbeeController controller = new HalZigbeeController();
        controller.initialize("COM3", HalZigbeeController.ZIGBEE_DONGLE_CC2531);

        handleConsoleInput('h', controller.networkManager);

        while (true) {
            char input = waitForInout();
            handleConsoleInput(input, controller.networkManager);

            if (input == 'q') break;
        }

        controller.close();
    }


    private static void handleConsoleInput(char input, ZigBeeNetworkManager networkManager) {
        switch (input) {
            case 'i':
                System.out.println("PAN ID          = " + networkManager.getZigBeePanId());
                System.out.println("Extended PAN ID = " + networkManager.getZigBeeExtendedPanId());
                System.out.println("Channel         = " + networkManager.getZigBeeChannel());
                break;

            case 'l':
                for (ZigBeeNode node : networkManager.getNodes()) {
                    System.out.println(node + " (" + node.getNodeState() + ")");

                    for (ZigBeeEndpoint endpoint : node.getEndpoints()) {
                        System.out.println("  - " + endpoint);
                    }

                    System.out.println("  Number of Endpoints: " + node.getEndpoints().size());
                }
                System.out.println("Number of ZigBee Nodes: " + networkManager.getNodes().size());
                break;

            case 'p':
                System.out.println("Enabling pairing.");
                networkManager.permitJoin(200);
                break;

            case 'q':
                System.out.println("Shutting down.");
                break;

            case 'h':
            default:
                System.out.println("Available commands:");
                System.out.println("  i: List network info");
                System.out.println("  l: List available ZigBee Nodes");
                System.out.println("  p: Enable pairing of ZigBee devices");
                System.out.println("  q: Quit");
                System.out.println("  h: Help text");
                break;
        }
    }

    private static char waitForInout() throws IOException {
        System.out.print("");
        System.out.print("Input command and finish with ENTER: ");

        while (true) {
            char input=(char)System.in.read();
            if (input != '\n')
                return input;
        }

    }
}