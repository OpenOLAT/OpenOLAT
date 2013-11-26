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
* <p>
*/ 

package org.olat.course.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.test.OlatTestCase;

/**
 * Description: <br>
 * TODO: patrick Class Description for CourseConfigManagerImplTest
 * <P>
 * Initial Date: Jun 3, 2005 <br>
 * 
 * @author patrick
 */
public class CourseConfigManagerImplTest extends OlatTestCase {
	private static OLog log = Tracing.createLoggerFor(CourseConfigManagerImplTest.class);

	private static ICourse course1;

	private static boolean isSetup = false;

	
	/**
	 * SetUp is called before each test.
	 */
	@Before
	public void setUp() {
		if (isSetup) return;
		try {
			// create course and persist as OLATResourceImpl
			OLATResourceable resourceable = OresHelper.createOLATResourceableInstance("junitConfigCourse", new Long(System.currentTimeMillis()));
			course1 = CourseFactory.createEmptyCourse(resourceable, "JUnitCourseConfig", "JUnitCourseConfig Long Title",
					"objective 1 objective 2 objective 3");
			DBFactory.getInstance().closeSession();
			isSetup = true;
		} catch (Exception e) {
			log.error("Exception in setUp(): " + e);
			e.printStackTrace();
		}
	}

	@Test
	public void testConfigFileCRUD() {
		/*
		 * a new created course gets its configuration upon the first load with
		 * default values
		 */
		CourseConfigManager ccm = CourseConfigManagerImpl.getInstance();
		CourseConfig cc1 = ccm.loadConfigFor(course1);
		assertNotNull("CourseConfiguration is not null", cc1);
		/*
		 * update values
		 */
		ccm.saveConfigTo(course1, cc1);
		cc1 = null;
		// check the saved values
		cc1 = ccm.loadConfigFor(course1);
		assertNotNull("CourseConfiguration is not null", cc1);
		/*
		 * delete configuration
		 */
		ccm.deleteConfigOf(course1);
		VFSItem cc1File = CourseConfigManagerImpl.getConfigFile(course1);
		assertFalse("CourseConfig file no longer exists.", cc1File != null);
	}
}