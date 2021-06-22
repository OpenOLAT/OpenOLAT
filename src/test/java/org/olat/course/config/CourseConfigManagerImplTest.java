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

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.config.manager.CourseConfigManagerImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: Jun 3, 2005 <br>
 * 
 * @author patrick
 */
public class CourseConfigManagerImplTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OLATResourceManager resourceManager;
	@Autowired
	private CourseConfigManager courseConfigManager;
	@Autowired
	private OrganisationService organisationService;

	@Test
	public void testConfigFileCRUD() {
		// create course and persist as OLATResourceImpl
		OLATResource resource = resourceManager.createOLATResourceInstance(CourseModule.class);

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry addedEntry = repositoryService.create(null, "Ayanami", "-", "JUnit course configuration course", "A JUnit course",
				resource, RepositoryEntryStatusEnum.trash, defOrganisation);
		ICourse course = CourseFactory.createCourse(addedEntry, "JUnitCourseConfig", "JUnitCourseConfig Long Title",
				"objective 1 objective 2 objective 3");
		dbInstance.commitAndCloseSession();
		
		/*
		 * a new created course gets its configuration upon the first load with
		 * default values
		 */
		CourseConfig cc1 = courseConfigManager.loadConfigFor(course);
		assertNotNull("CourseConfiguration is not null", cc1);
		/*
		 * update values
		 */
		courseConfigManager.saveConfigTo(course, cc1);
		cc1 = null;
		// check the saved values
		cc1 = courseConfigManager.loadConfigFor(course);
		assertNotNull("CourseConfiguration is not null", cc1);
		/*
		 * delete configuration
		 */
		courseConfigManager.deleteConfigOf(course);
		VFSItem cc1File = CourseConfigManagerImpl.getConfigFile(course);
		Assert.assertNull("CourseConfig file no longer exists.", cc1File);
	}
}