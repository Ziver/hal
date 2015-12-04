/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Ziver Koc
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

package zutil.osal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * User: Ziver
 */
public abstract class OSAbstractionLayer {
    public static enum OSType{
        Windows, Linux, MacOS, Unix
    }

    // Variables
    private static OSAbstractionLayer instance;

    public static OSAbstractionLayer getInstance(){
        if(instance == null)
            instance = getAbstractionLayer();
        return instance;
    }

    private static OSAbstractionLayer getAbstractionLayer(){
        String os = System.getProperty("os.name");
        if     (os.contains("Linux"))   return new OsalLinuxImpl();
        else if(os.contains("Windows")) return new OsalWindowsImpl();
        else                            return null;
    }

    /**
     * Executes a command and returns the first line of the result
     *
     * @param   cmd             the command to run
     * @return first line of the command
     */
    protected static String getFirstLineFromCommand(String cmd) {
        String[] tmp = runCommand(cmd);
        if(tmp.length > 1)
            return tmp[0];
        return null;
    }

    /**
     * Executes a command and returns the result
     *
     * @param   cmd             the command to run
     * @return a String list of the output of the command
     */
    public static String[] runCommand(String cmd) {
        ArrayList<String> ret = new ArrayList<String>();
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec(cmd);
            proc.waitFor();
            BufferedReader output = new BufferedReader(new InputStreamReader(proc.getInputStream()));


            String line;
            while ((line = output.readLine()) != null) {
                ret.add(line);
            }
            output.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret.toArray(new String[1]);
    }

    /**
     * @return the generic type of the OS
     */
    public abstract OSType getOSType();

    /**
     * @return a more specific OS or distribution name e.g "ubuntu", "suse", "windows"
     */
    public abstract String getOSName();

    /**
     * @return the OS version e.g windows: "vista", "7"; ubuntu: "10.4", "12.10"
     */
    public abstract String getOSVersion();

    public abstract String getKernelVersion();

    /**
     * @return the name of the current user
     */
    public abstract String getUsername();

    /**
     * @return the path to the root folder for the current users configuration files e.g Linux: "/home/$USER"
     */
    public abstract File getUserConfigPath();

    /**
     * @return the path to the global configuration folder e.g Linux: "/etc
     */
    public abstract File getGlobalConfigPath();

    public abstract HardwareAbstractionLayer getHAL();
}