/*
 * Copyright (c) 2015 Ziver
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

package se.koc.hal.plugin.tellstick;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import se.koc.hal.plugin.tellstick.protocols.NexaSelfLearning;

import java.io.*;

/**
 * This version of the TwoWaySerialComm example makes use of the
 * SerialPortEventListener to avoid polling.
 */
public class TellstickSerialComm extends Thread{
    public static TellstickSerialComm instance;

    private BufferedReader in;
    private OutputStream out;
    private TellstickParser parser = new TellstickParser();
    private TellstickChangeListener listener;

    public void connect(String portName) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);

            if (commPort instanceof SerialPort) {
                SerialPort serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

                in  = new BufferedReader(new InputStreamReader (serialPort.getInputStream()));
                out = new BufferedOutputStream(serialPort.getOutputStream());

                serialPort.disableReceiveTimeout();
                serialPort.enableReceiveThreshold(1);
                this.start();

            } else {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }
    }

    public void run() {
        try {
            String data;
            while ((data = in.readLine()) != null) {
                System.out.println("> " + data);
                TellstickProtocol protocol = parser.decode(data);
                if (protocol == null && (data.startsWith("+S") || data.startsWith("+T"))) {
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
                else if(listener != null){
                    listener.stateChange(protocol);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void write(TellstickProtocol prot) {
        write(prot.encode());
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void write(String data) {
        try {
            System.out.println("< "+data);
            for(int i=0; i<data.length();i++)
                out.write(0xFF & data.charAt(i));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setListener(TellstickChangeListener listener){
        this.listener = listener;
    }


    public static TellstickSerialComm getInstance(){
        if(instance == null){
            try {
                instance = new TellstickSerialComm();
                instance.connect("COM6");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }
}