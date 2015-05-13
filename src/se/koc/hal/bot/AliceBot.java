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

package se.koc.hal.bot;

import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.MagicBooleans;
import se.koc.hal.intf.HalBot;
import zutil.io.file.FileUtil;

import java.io.File;

/**
 * Created by Ziver on 2015-05-07.
 */
public class AliceBot implements HalBot{
    private Chat chatSession;

    @Override
    public void initialize() {
        MagicBooleans.trace_mode = false;

        File path = FileUtil.find("resource");
        if(path == null || !path.exists()){
            System.err.println("Bot folder does not exist");
            System.exit(1);
        }
        Bot bot = new Bot(
                "super",
                path.getAbsolutePath());
        chatSession = new Chat(bot);
    }

    @Override
    public String respond(String question) {
        return chatSession.multisentenceRespond(question);
    }
}
