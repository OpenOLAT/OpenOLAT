/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.course.nodes.sp;

import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DownloadComponent;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<br>
 * This is the implementatino of the peekview for the sp course node. Files of
 * type html, htm or xhtml are displayed in the peekview with a 75% scaling. For
 * other types only a download link is displayed.
 * 
 * <P>
 * Initial Date: 09.12.2009 <br>
 * 
 * @author gnaegi
 */
public class SPPeekviewController extends BasicController {

	/**
	 * Constructor for the sp peek view
	 * @param ureq
	 * @param wControl
	 * @param userCourseEnv
	 * @param config
	 * @param ores
	 */
	public SPPeekviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, ModuleConfiguration config, OLATResourceable ores) {
		super(ureq, wControl);
		// just display the page
		String file = config.getStringValue(SPEditController.CONFIG_KEY_FILE);
		DeliveryOptions deliveryOptions = (DeliveryOptions)config.get(SPEditController.CONFIG_KEY_DELIVERYOPTIONS);
		Component resPanel = new Panel("empty"); // empty panel to use if no file could be found
		if (file != null) {
			String fileLC = file.toLowerCase();
			if (fileLC.endsWith(".html") || fileLC.endsWith(".htm") || fileLC.endsWith(".xhtml")) {
				// Render normal view but scaled down to 75%
				boolean allowRelativeLinks = config.getBooleanSafe(SPEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS);
				// in preview, randomize the mapper of the html page
				SinglePageController spController =  new SinglePageController(ureq, wControl,
						userCourseEnv.getCourseEnvironment().getCourseFolderContainer(), 
						file, allowRelativeLinks, null, ores, deliveryOptions,
						userCourseEnv.getCourseEnvironment().isPreview());		
				// but add scaling to fit preview into minimized space
				spController.setScaleFactorAndHeight(0.75f, 400, true);
				listenTo(spController);
				resPanel = spController.getInitialComponent();
			} else {
				// Render a download link for file
				VFSContainer courseFolder = userCourseEnv.getCourseEnvironment().getCourseFolderContainer();
				VFSItem downloadItem = courseFolder.resolve(file);
				if (file != null && downloadItem instanceof VFSLeaf) {
					DownloadComponent downloadComp = new DownloadComponent("downloadComp",  (VFSLeaf) downloadItem);
					VelocityContainer peekviewVC = createVelocityContainer("peekview");
					peekviewVC.put("downloadComp", downloadComp);
					resPanel = peekviewVC;
				} 
			}
		}
		putInitialPanel(resPanel);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// no events to catch
	}

}
