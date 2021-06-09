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

package se.hal.plugin.nvr.page;

import se.hal.HalContext;
import se.hal.intf.HalWebPage;
import se.hal.page.HalAlertManager;
import se.hal.plugin.nvr.CameraControllerManager;
import se.hal.plugin.nvr.struct.Camera;
import se.hal.util.ClassConfigurationFacade;
import se.hal.struct.User;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.Templator;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import static zutil.ui.UserMessageManager.*;

public class CameraConfigWebPage extends HalWebPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/camera_config.tmpl";

    private ArrayList<ClassConfigurationFacade> cameraConfigurations;


    public CameraConfigWebPage() {
        super("camera_config");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "Camera Settings").setWeight(200);

        cameraConfigurations = new ArrayList<>();
        for (Class c : CameraControllerManager.getInstance().getAvailableDeviceConfigs())
            cameraConfigurations.add(new ClassConfigurationFacade(c));
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        DBConnection db = HalContext.getDB();
        User localUser = User.getLocalUser(db);

        // Save new input
        if (request.containsKey("action")){
            int id = (ObjectUtil.isEmpty(request.get("id")) ? -1 : Integer.parseInt(request.get("id")));
            Camera camera;

            switch(request.get("action")) {
                case "create_camera":
                    logger.info("Creating new camera: " + request.get("name"));
                    camera = new Camera();
                    camera.setName(request.get("name"));
                    camera.setType(request.get("type"));
                    camera.setUser(localUser);
                    camera.getDeviceConfigurator().setValues(request).applyConfiguration();
                    camera.save(db);
                    //ControllerManager.getInstance().register(camera);

                    HalAlertManager.getInstance().addAlert(new UserMessage(
                            MessageLevel.SUCCESS, "Successfully created new camera: " + camera.getName(), MessageTTL.ONE_VIEW));
                    break;

                case "modify_camera":
                    camera = Camera.getCamera(db, id);
                    if (camera != null) {
                        logger.info("Modifying camera: " + camera.getName());
                        camera.setName(request.get("name"));
                        camera.setType(request.get("type"));
                        camera.setUser(localUser);
                        camera.getDeviceConfigurator().setValues(request).applyConfiguration();
                        camera.save(db);

                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully saved camera: "+camera.getName(), MessageTTL.ONE_VIEW));
                    } else {
                        logger.warning("Unknown camera id: " + id);
                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.ERROR, "Unknown camera id: " + id, MessageTTL.ONE_VIEW));
                    }
                    break;

                case "remove_camera":
                    camera = Camera.getCamera(db, id);
                    if (camera != null) {
                        logger.info("Removing camera: " + camera.getName());
                        //ControllerManager.getInstance().deregister(camera);
                        camera.delete(db);

                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully removed camera: "+camera.getName(), MessageTTL.ONE_VIEW));
                    } else {
                        logger.warning("Unknown camera id: " + id);
                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.ERROR, "Unknown camera id: "+id, MessageTTL.ONE_VIEW));
                    }
                    break;
            }
        }

        // Output
        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("cameras", Camera.getCameras(db));

        return tmpl;

    }
}
