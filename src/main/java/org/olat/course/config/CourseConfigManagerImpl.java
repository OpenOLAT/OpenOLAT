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

package org.olat.course.config;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.version.Versionable;
import org.olat.core.util.vfs.version.VersionsFileManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseXStreamAliases;
import org.olat.course.ICourse;

import com.thoughtworks.xstream.XStream;

/**
 * Description: <br>
 * TODO: patrick Class Description for CourseConfigManagerImpl
 * <P>
 * Initial Date: Jun 3, 2005 <br>
 * @author patrick
 */
public class CourseConfigManagerImpl extends BasicManager implements CourseConfigManager {

	private static final OLog log = Tracing.createLoggerFor(CourseConfigManagerImpl.class);
	private static final CourseConfigManagerImpl INSTANCE = new CourseConfigManagerImpl();

	
	private CourseConfigManagerImpl() {
			// private for singleton
	}
	
	
	/**
	 * Singleton pattern
	 * 
	 * @return instance
	 */
	public static CourseConfigManager getInstance() {
		return INSTANCE;
	}

	/**
	 * @see org.olat.course.config.CourseConfigManager#copyConfigOf(org.olat.course.ICourse)
	 */
	public CourseConfig copyConfigOf(ICourse course) {
		CourseConfig tmp = course.getCourseEnvironment().getCourseConfig();

		return tmp;
	}

	/**
	 * @see org.olat.course.config.CourseConfigManager#deleteConfigOf(org.olat.course.ICourse)
	 */
	public boolean deleteConfigOf(ICourse course) {
		VFSLeaf configFile = getConfigFile(course);
		if (configFile != null) { return configFile.delete() == VFSConstants.YES; }
		return false;
	}

	/**
	 * @see org.olat.course.config.CourseConfigManager#loadConfigFor(org.olat.course.ICourse)
	 */
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
			XStream xstream = CourseXStreamAliases.getReadCourseXStream();
			Object tmp = XStreamHelper.readObject(xstream, configFile.getInputStream());
			if (tmp instanceof CourseConfig) {
				retVal = (CourseConfig) tmp;
				if (retVal.resolveVersionIssues()){
					configFile = null;
					saveConfigTo(course, retVal);
				}
			}
		}
		return retVal;
	}

	/**
	 * @see org.olat.course.config.CourseConfigManager#saveConfigTo(org.olat.course.ICourse,
	 *      org.olat.course.config.CourseConfig)
	 */
	public void saveConfigTo(ICourse course, CourseConfig courseConfig) {
		VFSLeaf configFile = getConfigFile(course);
		if (configFile == null) {
			// create new config file
			configFile = course.getCourseBaseContainer().createChildLeaf(COURSECONFIG_XML);
		} else if(configFile.exists() && configFile instanceof Versionable) {
			try {
				VersionsFileManager.getInstance().addToRevisions((Versionable)configFile, null, "");
			} catch (Exception e) {
				log.error("Cannot versioned CourseConfig.xml", e);
			}
		}
		XStreamHelper.writeObject(configFile, courseConfig);
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
	static VFSLeaf getConfigFile(ICourse course) {
		VFSItem item = course.getCourseBaseContainer().resolve(COURSECONFIG_XML);
		if (item == null || !(item instanceof VFSLeaf)) return null;
		return (VFSLeaf)item;
	}

}