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
package org.olat.course.nodes.pf.manager;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class PFManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PFManager pfManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;


	@Test
	public void provideParticipantContainer() {
		// prepare 
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("check-15");
		IdentityEnvironment ienv = new IdentityEnvironment();
		ienv.setIdentity(initialAuthor);
		PFCourseNode pfNode = new PFCourseNode();
		pfNode.getModuleConfiguration().setBooleanEntry(PFCourseNode.CONFIG_KEY_COACHBOX, true);
		pfNode.getModuleConfiguration().setBooleanEntry(PFCourseNode.CONFIG_KEY_PARTICIPANTBOX, true);
	
		// import "Demo course" into the bcroot_junittest
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(initialAuthor);
		Long resourceableId = entry.getOlatResource().getResourceableId();

		ICourse course = CourseFactory.loadCourse(resourceableId);
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());

		Identity check3 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-3");
		repositoryEntryRelationDao.addRole(check3, entry, GroupRoles.participant.name());
		VFSContainer vfsContainer = pfManager.provideCoachOrParticipantContainer(pfNode, userCourseEnv, check3, false);
		Assert.assertNotNull(vfsContainer);
		Assert.assertTrue(vfsContainer.exists());
	}

	@Test
	public void provideCoachContainer() {
		// prepare 
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("check-16");
		IdentityEnvironment ienv = new IdentityEnvironment();
		ienv.setIdentity(initialAuthor);
		PFCourseNode pfNode = new PFCourseNode();
		pfNode.getModuleConfiguration().setBooleanEntry(PFCourseNode.CONFIG_KEY_COACHBOX, true);
		pfNode.getModuleConfiguration().setBooleanEntry(PFCourseNode.CONFIG_KEY_PARTICIPANTBOX, true);
	
		// import "Demo course" into the bcroot_junittest
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(initialAuthor);
		Long resourceableId = entry.getOlatResource().getResourceableId();

		ICourse course = CourseFactory.loadCourse(resourceableId);
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
		
		Identity check4 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-4");
		repositoryEntryRelationDao.addRole(check4, entry, GroupRoles.coach.name());
		VFSContainer vfsContainer = pfManager.provideCoachOrParticipantContainer(pfNode, userCourseEnv, check4, false);
		Assert.assertNotNull(vfsContainer);
	}
	
	@Test
	public void uploadFileToDropBox() throws URISyntaxException{
		// prepare 
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("check-17");
		IdentityEnvironment ienv = new IdentityEnvironment();
		ienv.setIdentity(initialAuthor);
		PFCourseNode pfNode = new PFCourseNode();
		pfNode.getModuleConfiguration().setBooleanEntry(PFCourseNode.CONFIG_KEY_COACHBOX, true);
		pfNode.getModuleConfiguration().setBooleanEntry(PFCourseNode.CONFIG_KEY_PARTICIPANTBOX, true);
	
		// import "Demo course" into the bcroot_junittest
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(initialAuthor);
		Long resourceableId = entry.getOlatResource().getResourceableId();

		ICourse course = CourseFactory.loadCourse(resourceableId);
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		//create files
		URL portraitUrl = JunitTestHelper.class.getResource("file_resources/IMG_1482.JPG");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());

		boolean fileCreated = pfManager.uploadFileToDropBox(portrait, "textfile1",
				1, courseEnv, pfNode, initialAuthor);
		boolean fileNotCreated = pfManager.uploadFileToDropBox(portrait, "textfile2",
				0, courseEnv, pfNode, initialAuthor);
		
		Path relPath = Paths.get(PFManager.FILENAME_PARTICIPANTFOLDER, pfNode.getIdent(),
				pfManager.getIdFolderName(initialAuthor), PFManager.FILENAME_DROPBOX); 
		VFSContainer baseContainer = courseEnv.getCourseBaseContainer();
		VFSContainer dropboxContainer = VFSManager.resolveOrCreateContainerFromPath(baseContainer, relPath.toString());
		
		//check
		Assert.assertTrue(fileCreated);
		Assert.assertTrue(!fileNotCreated);
		Assert.assertTrue("textfile1".equals(dropboxContainer.getItems().get(0).getName())); 
	}
	
	@Test
	public void uploadFileToAllReturnBoxes() throws URISyntaxException {
		// prepare 
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("check-18");
		IdentityEnvironment ienv = new IdentityEnvironment();
		ienv.setIdentity(initialAuthor);
		PFCourseNode pfNode = new PFCourseNode();
		pfNode.getModuleConfiguration().setBooleanEntry(PFCourseNode.CONFIG_KEY_COACHBOX, true);
		pfNode.getModuleConfiguration().setBooleanEntry(PFCourseNode.CONFIG_KEY_PARTICIPANTBOX, true);
	
		// import "Demo course" into the bcroot_junittest
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(initialAuthor);
		Long resourceableId = entry.getOlatResource().getResourceableId();

		ICourse course = CourseFactory.loadCourse(resourceableId);
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		
		List<Identity> identities = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			identities.add(JunitTestHelper.createAndPersistIdentityAsRndUser("pf-user-" + i));
		}
		URL portraitUrl = JunitTestHelper.class.getResource("file_resources/IMG_1482.JPG");
		assertNotNull(portraitUrl);
		File portrait = new File(portraitUrl.toURI());
		
		pfManager.uploadFileToAllReturnBoxes(portrait, "textfile3", courseEnv, pfNode, identities);
		//check
		for (Identity identity : identities) {
			Path relPath = Paths.get(PFManager.FILENAME_PARTICIPANTFOLDER, pfNode.getIdent(),
					pfManager.getIdFolderName(identity), PFManager.FILENAME_RETURNBOX); 
			VFSContainer baseContainer = courseEnv.getCourseBaseContainer();
			VFSContainer returnboxContainer = VFSManager.resolveOrCreateContainerFromPath(baseContainer, relPath.toString());
			Assert.assertTrue("textfile3".equals(returnboxContainer.getItems().get(0).getName())); 
		}
	}

	@Test
	public void isValidModuleConfigSyncModuleConfigWithVFSContainer() {
		// prepare
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("check-19");
		IdentityEnvironment ienv = new IdentityEnvironment();
		List<String> folders = new ArrayList<>();
		String config = "drop.box/SubBoxD1,drop.box/SubBoxD1/ ,.return.box/SubBoxR1,/return.box/SubBoxR1/SubBoxR11,return.box/SubBoxR1/SubBoxR11/SubBoxR111,return.box/SubBoxR2";
		ienv.setIdentity(initialAuthor);
		PFCourseNode pfNode = new PFCourseNode();
		pfNode.getModuleConfiguration().setStringValue(PFCourseNode.CONFIG_KEY_TEMPLATE, config);

		Object moduleConfiguration = pfNode.getModuleConfiguration().get(PFCourseNode.CONFIG_KEY_TEMPLATE);

		if (!moduleConfiguration.toString().equals("")) {
			folders = new ArrayList<>(Arrays.asList(moduleConfiguration.toString().split(",")));
		}

		// false, because config contains illegal starting chars like . or /
		Assert.assertFalse(folders.stream().noneMatch(f -> f.startsWith(".") || f.startsWith("/")));
		folders.removeIf(f -> f.startsWith(".") || f.startsWith("/"));
		// true, because config was sanitized, paths were removed it they contained starting . or /
		Assert.assertTrue(folders.stream().noneMatch(f -> (f.startsWith(".") || f.startsWith("/"))));

		// false, because config contains fileNames which are invalid
		Assert.assertFalse(folders.stream().anyMatch(f -> (f.startsWith(".") || f.startsWith("/")) && FileUtils.validateFilename(f.replaceAll(".+?/", ""))));

		folders.removeIf(f -> !FileUtils.validateFilename(f.replaceAll(".+?/", "")));
		// true, because config was sanitized, foldernames with invalid Filenames were removed
		Assert.assertTrue(folders.stream().anyMatch(f -> (!f.startsWith(".") || !f.startsWith("/")) && FileUtils.validateFilename(f.replaceAll(".+?/", ""))));
	}
	
	@Test  
	public void getParticipants() {
		//prepare
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("check-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(initialAuthor);
		// import "Demo course" into the bcroot_junittest
		Long resourceableId = entry.getOlatResource().getResourceableId();

		ICourse course = CourseFactory.loadCourse(resourceableId);
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		
		repositoryEntryRelationDao.addRole(initialAuthor, entry, GroupRoles.coach.name());
		for (int i = 0; i < 5; i++) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-user-" + (i+12));
			repositoryEntryRelationDao.addRole(id, entry, GroupRoles.participant.name());
		}
		dbInstance.commitAndCloseSession();
		
		
		List<Identity> ids = pfManager.getParticipants(initialAuthor, courseEnv, true);
		//check
		Assert.assertEquals(5, ids.size());
		Assert.assertFalse(ids.contains(initialAuthor));
	}
}