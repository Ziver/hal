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

import java.io.*;

import com.fazecast.jSerialComm.SerialPort;

/**
 * This version of the TwoWaySerialComm example makes use of the
 * SerialPortEventListener to avoid polling.
 */
public class TellstickSerialComm extends Thread{
    public static TellstickSerialComm instance;

    private com.fazecast.jSerialComm.SerialPort serial;
    private BufferedReader in;
    private Writer out;

    private TellstickParser parser = new TellstickParser();
    private TellstickChangeListener listener;

    public void connect(String portName) throws Exception {
        serial = SerialPort.getCommPort(portName);
        serial.setBaudRate(115200);
        if(!serial.openPort())
            throw new IOException("Could not open port: "+portName);
        serial.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                5000, 0);

        in  = new BufferedReader(new InputStreamReader(serial.getInputStream(),  "UTF-8"));
        out = new BufferedWriter(new OutputStreamWriter(serial.getOutputStream(), "UTF-8"));
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