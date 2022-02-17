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
package org.olat.modules.scorm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;


/**
 * Initial Date:  08.10.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
@Service
public class ScormMainManager {
	
	public static final String PACKAGE_CONFIG_FILE_NAME = "ScormPackageConfig.xml";
	
	private static final Logger log = Tracing.createLoggerFor(ScormMainManager.class);
	private static XStream configXstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				ScormPackageConfig.class, DeliveryOptions.class
			};
		configXstream.addPermission(new ExplicitTypePermission(types));
		configXstream.alias("packageConfig", ScormPackageConfig.class);
		configXstream.alias("deliveryOptions", DeliveryOptions.class);
	}
	
	public ScormPackageConfig getScormPackageConfig(File cpRoot) {
		File configXml = new File(cpRoot.getParentFile(), PACKAGE_CONFIG_FILE_NAME);
		if(configXml.exists()) {
			return (ScormPackageConfig)configXstream.fromXML(configXml);
		}
		return null;
	}
	
	public ScormPackageConfig getScormPackageConfig(OLATResourceable ores) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File reFolder = frm.getFileResourceRoot(ores);
		File configXml = new File(reFolder, PACKAGE_CONFIG_FILE_NAME);
		if(configXml.exists()) {
			return (ScormPackageConfig)configXstream.fromXML(configXml);
		}
		return null;
	}
	
	public void setScormPackageConfig(OLATResourceable ores, ScormPackageConfig config) {
		FileResourceManager frm = FileResourceManager.getInstance();
		File reFolder = frm.getFileResourceRoot(ores);
		File configXml = new File(reFolder, PACKAGE_CONFIG_FILE_NAME);
		if(config == null) {
			FileUtils.deleteFile(configXml);
		} else {
			try(OutputStream out = new FileOutputStream(configXml)) {
				configXstream.toXML(config, out);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}
	
	/**
	 * @param ureq
	 * @param wControl
	 * @param showMenu if true, the ims cp menu is shown
	 * @param apiCallback the callback to where lmssetvalue data is mirrored, or null if no callback is desired
	 * @param cpRoot
	 * @param resourceId
	 * @param lessonMode add null for the default value or "normal", "browse" or
	 *          "review"
	 * @param creditMode add null for the default value or "credit", "no-credit"
	 */
	public ScormAPIandDisplayController createScormAPIandDisplayController(UserRequest ureq, WindowControl wControl,
			boolean showMenu, File cpRoot, Long scormResourceId, String courseId,
			String lessonMode, String creditMode, String assessableType, boolean activate,
			ScormDisplayEnum fullWindow, boolean attemptsIncremented, boolean randomizeDelivery, DeliveryOptions deliveryOptions) {
		
		ScormAPIandDisplayController ctrl= new ScormAPIandDisplayController(ureq, wControl, showMenu, cpRoot,
				scormResourceId, courseId, lessonMode, creditMode, assessableType, activate, fullWindow,
				attemptsIncremented, randomizeDelivery, deliveryOptions);
		
		DeliveryOptions config = ctrl.getDeliveryOptions();
		boolean configAllowRawContent = (config == null || config.rawContent());
		ctrl.setRawContent(configAllowRawContent);
		return ctrl;
	}
	
}
