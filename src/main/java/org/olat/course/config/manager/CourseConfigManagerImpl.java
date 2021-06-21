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

package org.olat.course.config.manager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigManager;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * <P>
 * Initial Date: Jun 3, 2005 <br>
 * @author patrick
 */
@Service
public class CourseConfigManagerImpl implements CourseConfigManager {

	private static final Logger log = Tracing.createLoggerFor(CourseConfigManagerImpl.class);
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] {
				CourseConfig.class, Hashtable.class, HashMap.class
		};
		xstream.addPermission(new ExplicitTypePermission(types));
	}

	@Override
	public CourseConfig copyConfigOf(ICourse course) {
		return course.getCourseEnvironment().getCourseConfig().clone();
	}

	@Override
	public boolean deleteConfigOf(ICourse course) {
		VFSLeaf configFile = getConfigFile(course);
		if (configFile != null) {
			return configFile.delete() == VFSConstants.YES;
		}
		return false;
	}

	@Override
	public CourseConfig loadConfigFor(ICourse course) {
		CourseConfig retVal = null;
		VFSLeaf configFile = getConfigFile(course);
		if (configFile == null) {
			//config file does not exist! create one, init the defaults, save it.
			retVal = new CourseConfig();
			retVal.initDefaults();
			saveConfigTo(course, retVal);
		} else {
			//file exists, load it with XStream, resolve version
			Object tmp = XStreamHelper.readObject(xstream, configFile);
			if (tmp instanceof CourseConfig) {
				retVal = (CourseConfig) tmp;
				if (retVal.resolveVersionIssues()) {
					saveConfigTo(course, retVal);
				}
			}
		}
		return retVal;
	}

	@Override
	public void saveConfigTo(ICourse course, CourseConfig courseConfig) {
		VFSLeaf configFile = getConfigFile(course);
		if (configFile == null) {
			// create new config file
			configFile = course.getCourseBaseContainer().createChildLeaf(COURSECONFIG_XML);
		} else if(configFile.exists() && configFile.canVersion() == VFSConstants.YES) {
			try(InputStream in = configFile.getInputStream()) {
				CoreSpringFactory.getImpl(VFSRepositoryService.class).addVersion(configFile, null, false, "", in);
			} catch (Exception e) {
				log.error("Cannot versioned CourseConfig.xml", e);
			}
		}
		XStreamHelper.writeObject(xstream, configFile, courseConfig);
	}

	/**
	 * the configuration is saved in folder called <code>Configuration</code>
	 * residing in the course folder
	 * <p>
	 * package wide visibility for the CourseConfigManagerImplTest
	 * 
	 * @param course
	 * @return the configuration file or null if file does not exist
	 */
	public static VFSLeaf getConfigFile(ICourse course) {
		VFSItem item = course.getCourseBaseContainer().resolve(COURSECONFIG_XML);
		return item instanceof VFSLeaf ? (VFSLeaf)item : null;
	}
}