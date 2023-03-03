/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.project.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectUserInfo;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.model.ProjProjectUserInfoImpl;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjProjectUserInfoDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ProjProjectUserInfoDAO sut;
	
	@Test
	public void shouldCreateProjectUserInfo() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(identity);
		dbInstance.commitAndCloseSession();
		
		ProjProjectUserInfo projectUserInfo = sut.create(project, identity);
		dbInstance.commitAndCloseSession();
		
		assertThat(projectUserInfo).isNotNull();
		assertThat(((ProjProjectUserInfoImpl)projectUserInfo).getCreationDate()).isNotNull();
		assertThat(((ProjProjectUserInfoImpl)projectUserInfo).getLastModified()).isNotNull();
		assertThat(projectUserInfo.getProject()).isEqualTo(project);
		assertThat(projectUserInfo.getIdentity()).isEqualTo(identity);
	}
	
	@Test
	public void shouldSaveProjectUserInfo() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(identity);
		ProjProjectUserInfo projectUserInfo = sut.create(project, identity);
		dbInstance.commitAndCloseSession();
		
		Date lastVisitDate = new GregorianCalendar(2022, 01, 04, 10, 3, 8).getTime();
		projectUserInfo.setLastVisitDate(lastVisitDate);
		sut.save(projectUserInfo);
		dbInstance.commitAndCloseSession();
		
		projectUserInfo = sut.loadProjectUserInfos(project, List.of(identity)).get(0);
		assertThat(projectUserInfo.getLastVisitDate()).isCloseTo(lastVisitDate, 2000);
	}
	
	@Test
	public void shouldDeleteProjectUserInfo() {
		Identity identity = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(identity);
		ProjProjectUserInfo projectUserInfo = sut.create(project, identity);
		dbInstance.commitAndCloseSession();
		
		sut.delete(projectUserInfo);
		dbInstance.commitAndCloseSession();
		
		assertThat(sut.loadProjectUserInfos(project, List.of(identity))).isEmpty();
	}

}
