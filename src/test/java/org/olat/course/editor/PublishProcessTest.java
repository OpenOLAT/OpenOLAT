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
package org.olat.course.editor;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * These unit tests are related to OO-249. the main goal is to make the
 * publish process robust against the different AssertException and to
 * handle to possible corruption without triggering a red screen which
 * make the course corrupt, uneditable, lost.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PublishProcessTest extends OlatTestCase {
	
	private static final Locale locale = Locale.GERMAN;
	
	@Autowired
	private OLATResourceManager olatResourceManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	
	/**
	 * Publish process without error
	 * @throws URISyntaxException
	 */
	@Test
	public void testPublishProcess() throws URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin("publisher-");
		RepositoryEntry re = deployTestCourse("simple_course.zip");

		//change node 1
		ICourse course = CourseFactory.openCourseEditSession(re.getOlatResource().getResourceableId());
		CourseEditorTreeModel cetm = course.getEditorTreeModel();
		CourseEditorTreeNode node1 = (CourseEditorTreeNode)cetm.getRootNode().getChildAt(0);
		node1.getCourseNode().setShortTitle("Node 1 prime");
		cetm.nodeConfigChanged(node1);				
		CourseFactory.saveCourseEditorTreeModel(course.getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);	
		
		//publish the course
		List<String> nodeIds = Collections.singletonList(node1.getIdent());
		publishCourse(nodeIds, re, author);
		
		//check the change
		ICourse reloadedCourse = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		Assert.assertEquals(3, reloadedCourse.getRunStructure().getRootNode().getChildCount());
		INode runNode1 = reloadedCourse.getRunStructure().getRootNode().getChildAt(0);
		Assert.assertNotNull(runNode1);
		CourseNode runNode1Impl = (CourseNode)runNode1;
		Assert.assertEquals("Node 1 prime", runNode1Impl.getShortTitle());
	}
	
	/**
	 * Publish an unchanged course. We try to publish a node which
	 * was not changed nor deleted.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testPublishANotPublishedNode()
	throws URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin("publisher-");
		RepositoryEntry re = deployTestCourse("simple_course.zip");
		
		//change node 1
		ICourse course = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		CourseEditorTreeModel cetm = course.getEditorTreeModel();
		CourseEditorTreeNode node1 = (CourseEditorTreeNode)cetm.getRootNode().getChildAt(0);

		//publish the course and must survive this without exception
		//as the course has no changes but we try to publish it
		List<String> nodeIds = Collections.singletonList(node1.getIdent());
		publishCourse(nodeIds, re, author);
	}
	
	/**
	 * Publish a course with a node marked as new but the node
	 * exists already in the run structure.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testPublishANotReallyNewNode()
	throws URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin("publisher-");
		RepositoryEntry re = deployTestCourse("simple_course_err1_new.zip");
		
		//change node 1
		ICourse course = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		CourseEditorTreeModel cetm = course.getEditorTreeModel();
		CourseEditorTreeNode node1 = (CourseEditorTreeNode)cetm.getRootNode().getChildAt(0);

		//publish the course and must survive this without exception
		//as the course has no changes but we try to publish it
		List<String> nodeIds = Collections.singletonList(node1.getIdent());
		publishCourse(nodeIds, re, author);

		//check the change
		ICourse reloadedCourse = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		Assert.assertEquals(3, reloadedCourse.getRunStructure().getRootNode().getChildCount());
		INode runNode1 = reloadedCourse.getRunStructure().getRootNode().getChildAt(0);
		Assert.assertNotNull(runNode1);
		CourseNode runNode1Impl = (CourseNode)runNode1;
		Assert.assertEquals("Node 1 not really new", runNode1Impl.getShortTitle());
	}
	
	/**
	 * Publish a course with a node marked as new and deleted but the
	 * node exists already in the run structure.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testPublishANotReallyNewNodeButDeleted()
	throws URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin("publisher-");
		RepositoryEntry re = deployTestCourse("simple_course_err2_new_deleted.zip");
		
		//change node 1
		ICourse course = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		CourseEditorTreeModel cetm = course.getEditorTreeModel();
		CourseEditorTreeNode node1 = (CourseEditorTreeNode)cetm.getRootNode().getChildAt(0);

		//publish the course and must survive this without exception
		//as the course has no changes but we try to publish it
		List<String> nodeIds = Collections.singletonList(node1.getIdent());
		publishCourse(nodeIds, re, author);

		//check the change
		ICourse reloadedCourse = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		Assert.assertEquals(2, reloadedCourse.getRunStructure().getRootNode().getChildCount());
		INode runNode2 = reloadedCourse.getRunStructure().getRootNode().getChildAt(0);
		Assert.assertNotNull(runNode2);
		CourseNode runNode2Impl = (CourseNode)runNode2;
		Assert.assertEquals("Node 2", runNode2Impl.getShortTitle());
	}
	
	/**
	 * Publish a course with a node marked as not new but the
	 * node dosn't exist in the run structure.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testPublishNewNodeButNotMarkedAsSuch()
	throws URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin("publisher-");
		RepositoryEntry re = deployTestCourse("simple_course_err3_not_new.zip");
		
		//change node 1
		ICourse course = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		CourseEditorTreeModel cetm = course.getEditorTreeModel();
		CourseEditorTreeNode node1 = (CourseEditorTreeNode)cetm.getRootNode().getChildAt(0);

		//publish the course and must survive this without exception
		//as the course has no changes but we try to publish it
		List<String> nodeIds = Collections.singletonList(node1.getIdent());
		publishCourse(nodeIds, re, author);

		//check the change
		ICourse reloadedCourse = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		Assert.assertEquals(3, reloadedCourse.getRunStructure().getRootNode().getChildCount());
		INode runNode1 = reloadedCourse.getRunStructure().getRootNode().getChildAt(0);
		Assert.assertNotNull(runNode1);
		CourseNode runNode1Impl = (CourseNode)runNode1;
		Assert.assertEquals("Node 1 from hell", runNode1Impl.getShortTitle());
	}
	
	/**
	 * Publish a course with a node marked as not new and deleted but
	 * the node doesn't exist in the run structure.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testPublishNewNodeButNotMarkedAsSuchAndDeleted()
	throws URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin("publisher-");
		RepositoryEntry re = deployTestCourse("simple_course_err4_not_new_deleted.zip");
		
		//change node 1
		ICourse course = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		CourseEditorTreeModel cetm = course.getEditorTreeModel();
		CourseEditorTreeNode node1 = (CourseEditorTreeNode)cetm.getRootNode().getChildAt(0);

		//publish the course and must survive this without exception
		//as the course has no changes but we try to publish it
		List<String> nodeIds = Collections.singletonList(node1.getIdent());
		publishCourse(nodeIds, re, author);

		//check the change
		ICourse reloadedCourse = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		Assert.assertEquals(2, reloadedCourse.getRunStructure().getRootNode().getChildCount());
		INode runNode1 = reloadedCourse.getRunStructure().getRootNode().getChildAt(0);
		Assert.assertNotNull(runNode1);
		CourseNode runNode1Impl = (CourseNode)runNode1;
		Assert.assertEquals("Node 2", runNode1Impl.getShortTitle());
	}
	
	/**
	 * Publish a course with a node marked as not new but
	 * the node doesn't exist in the run structure. The node
	 * itself is not published.
	 * 
	 * @throws URISyntaxException
	 */
	@Test
	public void testPublishNewNodeNotMarkedAsSuchAndNotPublished()
	throws URISyntaxException {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin("publisher-");
		RepositoryEntry re = deployTestCourse("simple_course_err5_not_new_or_published.zip");
		
		//change node 1
		ICourse course = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		CourseEditorTreeModel cetm = course.getEditorTreeModel();

		//publish the course and must survive this without exception
		//as the course has no changes but we try to publish it
		List<String> nodeIds = Collections.singletonList(cetm.getRootNode().getIdent());
		publishCourse(nodeIds, re, author);

		//check the change
		ICourse reloadedCourse = CourseFactory.loadCourse(re.getOlatResource().getResourceableId());
		Assert.assertEquals(2, reloadedCourse.getRunStructure().getRootNode().getChildCount());
		INode runNode1 = reloadedCourse.getRunStructure().getRootNode().getChildAt(0);
		Assert.assertNotNull(runNode1);
		CourseNode runNode1Impl = (CourseNode)runNode1;
		Assert.assertEquals("Node 2", runNode1Impl.getShortTitle());
	}
	
	private void publishCourse(List<String> nodeIds, RepositoryEntry re, Identity author) {
		ICourse course = CourseFactory.openCourseEditSession(re.getOlatResource().getResourceableId());
		CourseEditorTreeModel cetm = course.getEditorTreeModel();
		PublishProcess pp = PublishProcess.getInstance(course, cetm, locale);
		// create publish node list
		pp.createPublishSetFor(nodeIds);
		PublishSetInformations set = pp.testPublishSet(locale);
		StatusDescription[] sds = set.getWarnings();
		Assert.assertNotNull(sds);
		Assert.assertEquals(0, sds.length);
		pp.applyPublishSet(author, locale, true);
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
	}
	
	private RepositoryEntry deployTestCourse(String filename)
	throws URISyntaxException {
		URL courseUrl = PublishProcessTest.class.getResource(filename);
		Assert.assertNotNull(courseUrl);
		File courseFile = new File(courseUrl.toURI());
		
		//deploy a course
		String softKey = UUID.randomUUID().toString().replace("-", "").substring(0, 30);
		RepositoryEntry re = deployRawCourseFromZIP(courseFile, softKey, 1);
		Assert.assertNotNull(re);
		return re;
	}
	
	/**
	 * This method copy the edtiro tree model and run structure as is during the import.
	 * @param exportedCourseZIPFile
	 * @param softKey
	 * @param access
	 * @return
	 */
	private RepositoryEntry deployRawCourseFromZIP(File exportedCourseZIPFile, String softKey, int access) {
		// create the course instance
		OLATResource newCourseResource = olatResourceManager.createOLATResourceInstance(CourseModule.class);
		ICourse course = CourseFactory.importCourseFromZip(newCourseResource, exportedCourseZIPFile);
		// course is now also in course cache!
		if (course == null) {
			return null;
		}
		
		File courseExportData = course.getCourseExportDataDir().getBasefile();
		// get the export data directory
		// create the repository entry
		RepositoryEntryImportExport importExport = new RepositoryEntryImportExport(courseExportData);
		if(!StringHelper.containsNonWhitespace(softKey)) {
			softKey = importExport.getSoftkey();
		}

		Organisation defOrganisation = organisationService.getDefaultOrganisation();
		RepositoryEntry re = repositoryService.create(null, importExport.getInitialAuthor(), importExport.getResourceName(),
				importExport.getDisplayName(), importExport.getDescription(), newCourseResource, 0, defOrganisation);
		// ok, continue import
		re.setSoftkey(softKey);
		// set access configuration
		re.setAccess(access);
		// save the repository entry
		re = repositoryService.update(re);

		//import groups
		course = CourseFactory.openCourseEditSession(course.getResourceableId());
		// create group management
		CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
		// import groups
		cgm.importCourseBusinessGroups(courseExportData);

		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		// cleanup export data
		FileUtils.deleteDirsAndFiles(courseExportData, true, true);
		return re;
	}
}
