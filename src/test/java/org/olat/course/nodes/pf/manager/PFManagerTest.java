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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jcodec.common.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.i18n.I18nManager;
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

	private Identity identity;
	private ICourse course;
	private RepositoryEntry repositoryEntry;
	private PFCourseNode pfNode;
	private CourseEnvironment courseEnv;
	private UserCourseEnvironment userCourseEnv;

	@Autowired
	private PFManager pfManager;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;

	@Before
	public void setUp() {
		// prepare
		identity = JunitTestHelper.createAndPersistIdentityAsRndUser("check-1");
		IdentityEnvironment ienv = new IdentityEnvironment();
		pfNode = new PFCourseNode();
		pfNode.getModuleConfiguration().setBooleanEntry(PFCourseNode.CONFIG_KEY_COACHBOX, true);
		pfNode.getModuleConfiguration().setBooleanEntry(PFCourseNode.CONFIG_KEY_PARTICIPANTBOX, true);
	
		ienv.setIdentity(identity);
		// import "Demo course" into the bcroot_junittest
		repositoryEntry = JunitTestHelper.deployDemoCourse(identity);
		Long resourceableId = repositoryEntry.getOlatResource().getResourceableId();

		course = CourseFactory.loadCourse(resourceableId);
		userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
	}


	@Test
	public void provideParticipantView_test () {
		Identity check3 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-3");
		repositoryEntryRelationDao.addRole(check3, repositoryEntry, GroupRoles.participant.name());
		VFSContainer vfsContainer = pfManager.provideCoachOrParticipantContainer(pfNode, userCourseEnv, check3);
		Assert.assertNotNull(vfsContainer);
	}

	@Test
	public void provideCoachView_test () {
		Identity check4 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-4");
		repositoryEntryRelationDao.addRole(check4, repositoryEntry, GroupRoles.coach.name());
		VFSContainer vfsContainer = pfManager.provideCoachOrParticipantContainer(pfNode, userCourseEnv, check4);
		Assert.assertNotNull(vfsContainer);
	}
	
	@Test 
	public void uploadFileToDropBox_test () {
		//create files
		boolean fileCreated = pfManager.uploadFileToDropBox(new File("text1.txt"), "textfile1",
				1, courseEnv, pfNode, identity);
		boolean fileNotCreated = pfManager.uploadFileToDropBox(new File("text2.txt"), "textfile2",
				0, courseEnv, pfNode, identity);
		
		Path relPath = Paths.get(PFManager.FILENAME_PARTICIPANTFOLDER, pfNode.getIdent(),
				pfManager.getIdFolderName(identity), PFManager.FILENAME_DROPBOX); 
		OlatRootFolderImpl baseContainer = courseEnv.getCourseBaseContainer();
		VFSContainer dropboxContainer = VFSManager.resolveOrCreateContainerFromPath(baseContainer, relPath.toString());
		
		//check
		Assert.assertTrue(fileCreated);
		Assert.assertTrue(!fileNotCreated);
		Assert.assertTrue("textfile1".equals(dropboxContainer.getItems().get(0).getName())); 
		
	}
	
	@Test 
	public void uploadFileToAllReturnBoxes_test () {
		// prepare 
		List<Identity> identities = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			identities.add(JunitTestHelper.createAndPersistIdentityAsRndUser("pf-user-" + i));
		}
		pfManager.uploadFileToAllReturnBoxes(new File("text3.txt"), "textfile3", courseEnv, pfNode, identities);
		//check
		for (Identity identity : identities) {
			Path relPath = Paths.get(PFManager.FILENAME_PARTICIPANTFOLDER, pfNode.getIdent(),
					pfManager.getIdFolderName(identity), PFManager.FILENAME_RETURNBOX); 
			OlatRootFolderImpl baseContainer = courseEnv.getCourseBaseContainer();
			VFSContainer returnboxContainer = VFSManager.resolveOrCreateContainerFromPath(baseContainer, relPath.toString());
			Assert.assertTrue("textfile3".equals(returnboxContainer.getItems().get(0).getName())); 
		}
	}
	
	@Test
	public void exportMediaResource_test () {
		//prepare
		List<Identity> identities = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			identities.add(JunitTestHelper.createAndPersistIdentityAsRndUser("pf-user-" + (i + 6)));
		}
		Identity check2 = JunitTestHelper.createAndPersistIdentityAsRndUser("check-2");
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(check2.getUser().getPreferences().getLanguage());
		MediaResource resource = pfManager.exportMediaResource(new SyntheticUserRequest(check2, locale), identities, pfNode, courseEnv);
		//check
		Assert.assertNotNull(resource);
	}
	
	@Test 
	public void getParticipants_test () {
		//prepare
		repositoryEntryRelationDao.addRole(identity, repositoryEntry, GroupRoles.coach.name());
		for (int i = 0; i < 5; i++) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("pf-user-" + (i+12));
			repositoryEntryRelationDao.addRole(id, repositoryEntry, GroupRoles.participant.name());
		}
		List<Identity> ids = pfManager.getParticipants(identity, courseEnv);
		//check
		Assert.assertEquals(ids.size(), 5);
	}


}
