/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Ziver Koc
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

import se.hal.HalContext;
import se.hal.intf.HalWebPage;
import se.hal.struct.Room;
import se.hal.struct.User;
import se.hal.util.ClassConfigurationFacade;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.Templator;

import java.util.Map;
import java.util.logging.Logger;

import static zutil.ui.UserMessageManager.*;

public class RoomConfigWebPage extends HalWebPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/room_config.tmpl";

    private ClassConfigurationFacade roomConfiguration = new ClassConfigurationFacade(Room.class);


    public RoomConfigWebPage() {
        super("room_config");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "Room Settings").setWeight(200);
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception {

        DBConnection db = HalContext.getDB();

        // Save new input
        if (request.containsKey("action")){
            int id = (ObjectUtil.isEmpty(request.get("id")) ? -1 : Integer.parseInt(request.get("id")));

            Room room = null;

            if (id >= 0) {
                // Read in requested id
                room = Room.getRoom(db, id);

                if (room == null) {
                    logger.warning("Unknown room id: " + id);
                    HalContext.getUserMessageManager().add(new UserMessage(
                            MessageLevel.ERROR, "Unknown room id: " + id, MessageTTL.ONE_VIEW));
                }
            }

            switch(request.get("action")) {
                case "create_room":
                    logger.info("Creating new room: " + request.get("name"));
                    room = new Room();
                    /* FALLTHROUGH */

                case "modify_room":
                    if (room != null) {
                        logger.info("Modifying room(id: " + room.getId() + "): " + room.getName());
                        room.setName(request.get("name"));
                        room.save(db);

                        HalContext.getUserMessageManager().add(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully updated room: " + room.getName(), MessageTTL.ONE_VIEW));
                    }
                    break;

                case "remove_room":
                    if (room != null) {
                        logger.info("Removing room(id: " + room.getId() + "): " + room.getName());
                        room.delete(db);

                        HalContext.getUserMessageManager().add(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully removed room: " + room.getName(), MessageTTL.ONE_VIEW));
                    }
                    break;
            }
        }

        // Output
        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("rooms", Room.getRooms(db));
        tmpl.set("roomConfiguration", roomConfiguration);

        return tmpl;

    }
}
