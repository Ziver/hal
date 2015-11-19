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

package se.koc.hal.test;

import zutil.io.DynamicByteArrayStream;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: ezivkoc
 * Date: 2013-12-17
 * Time: 11:24
 */
public class GoogleTTSTest {
    public static void main(String[] args){
        try {
/*            URL url = new URL("http://translate.google.com/translate_tts?q=I+love+techcrunch");
            InputStream in = url.openStream();
            byte[] data = getContent(in);
            in.close();
*/
            Clip push = AudioSystem.getClip();

            URL url = new URL("http://translate.google.com/translate_tts?ie=UTF-8&q=Hello%20World&tl=en-us");
            URLConnection con = url.openConnection();
            con.setRequestProperty("Cookie", "JSESSIONID=UX61oaN8vRhLE5pYXAjTWg; _ga=GA1.3.53937642.1385651778; HSID=AINSMauAFJWBs84WQ; APISID=RgytV3HJnm0dWjVr/AtidsIB_LJQzDmBMc; NID=67=R0kMLqIXXiOkrU8jlk4vgqLUiWUYUZRvxjf1Un0DQbQxGKt9pXXzDv-v0zSCSqLi_YNzcZujTDDr9r_KGsiPhEMfk-oKQSKvHe-DVVuwHZb2UZraJKCBAb6mPJO6AxBExoXHzU2pHd-DI1yIMxuLyVJA9RxhM_2kB4h7U0w9WiWqRNN7sU5DPVeLpF_ScW1VH9_igIR2ACK0WHvmoZXBjXDDrnUiVJt9DjkbMpHxU1o_1PnuUXi5FmfJLjrQspI; SID=DQAAANgAAADcaXZk9dA01UfdydUwIH32OGbA0k6mhbV2GSsiqcGYTUhNqLn_Z9TAlUsizBVoG-3g-ghXzpev46P--fqcR4UACZ2iVawFbfUB44B2hBmQQsFbyjGop1smPLu3cJORBLUKQ4PiZQb23GtXYg28prWlK3IFj8Wc3AHY5yoIpnssRY24k9DybwSSVt2Ww7c4ySzfw4uXxwtbSDTy0q8lmdAorjT4R6DCJwhaCGV4ysexY-vJaQE2kiRe3fPY2z9jQ6Mi9z_XjGRamLTI_AvJj-_XdQIfv0ZSo7JiEEjTUMb10Q; PREF=ID=8bf4d3a7414e8137:U=4ebb392cf34740cf:LD=sv:CR=2:TM=1368543367:LM=1387278509:GM=1:SG=1:S=fpteokIgX46FW8tp");
            con.connect();


            AudioInputStream audioPush = AudioSystem.getAudioInputStream(con.getInputStream());

            push.open(audioPush);
            push.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getContent(InputStream stream) throws IOException{
        BufferedInputStream in = new BufferedInputStream(stream);
        byte[] tmp = new byte[256];
        int len;
        DynamicByteArrayStream buff = new DynamicByteArrayStream();

        while((len=in.read(tmp)) != -1){
            buff.append(tmp, 0, len);
        }
        return buff.getBytes();
    }
}
