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
package org.olat.repository.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureRollCallStatus;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.ExecutionType;
import org.olat.repository.ui.author.copy.wizard.CopyCourseOverviewRow;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 30.08.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyServiceImplTest extends OlatTestCase {
	
	private Identity author;
	private Identity owner1;
	private Identity owner2;
	private Identity coach1;
	private Identity coach2;
	private RepositoryEntry source;
	private RepositoryEntry target;
	private CopyCourseContext context;
		
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CopyServiceImpl copyService;
	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDAO;
	
	@Before
	public void createCourse() {
		author = JunitTestHelper.createAndPersistIdentityAsRndUser("Copy-author");
		owner1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Copy-owner1");
		owner2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Copy-owner2");
		coach1 = JunitTestHelper.createAndPersistIdentityAsRndUser("Copy-coach1");
		coach2 = JunitTestHelper.createAndPersistIdentityAsRndUser("Copy-coach2");
		
		source = JunitTestHelper.deployCopyWizardCourse(author);
		
		repositoryEntryRelationDao.addRole(owner1, source, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(owner2, source, GroupRoles.owner.name());
		repositoryEntryRelationDao.addRole(coach1, source, GroupRoles.coach.name());
		repositoryEntryRelationDao.addRole(coach2, source, GroupRoles.coach.name());
		
		OLATResourceable sourceOres = source.getOlatResource();
		ICourse sourceCourse = CourseFactory.openCourseEditSession(sourceOres.getResourceableId());
		
		context = new CopyCourseContext();
		context.setExecutingIdentity(author);
		context.setSourceRepositoryEntry(source);
		context.setDisplayName(source.getDisplayname());
		
		loadCopyConfig(context);
		
		TreeNode rootNode = sourceCourse.getEditorTreeModel().getRootNode();
		List<CopyCourseOverviewRow> courseNodes = new ArrayList<>();
		forgeRows(context, courseNodes, rootNode, 0, null);
		
		context.setCourseNodes(courseNodes);
		
		CourseFactory.closeCourseEditSession(sourceOres.getResourceableId(), true);
	}
	
	private void loadCopyConfig(CopyCourseContext context) {
		// Set everything to ignore
		context.setGroupCopyType(CopyType.ignore);
		context.setOwnersCopyType(CopyType.ignore);
		context.setCoachesCopyType(CopyType.ignore);
		context.setDisclaimerCopyType(CopyType.ignore);
		context.setCatalogCopyType(CopyType.ignore);
		
		context.setTaskCopyType(CopyType.ignore);
		context.setBlogCopyType(CopyType.ignore);
		context.setWikiCopyType(CopyType.ignore);
		context.setFolderCopyType(CopyType.ignore);
		
		context.setReminderCopyType(CopyType.ignore);
		context.setAssessmentModeCopyType(CopyType.ignore);
		context.setLectureBlockCopyType(CopyType.ignore);
		
		context.setExecutionType(ExecutionType.none);
	}
	
	private void forgeRows(CopyCourseContext context, List<CopyCourseOverviewRow> rows, INode node, int recursionLevel, CopyCourseOverviewRow parent) {
		if (node instanceof CourseEditorTreeNode) {
			CourseEditorTreeNode editorNode = (CourseEditorTreeNode)node;
			CopyCourseOverviewRow row = forgeRow(context, editorNode, recursionLevel, parent);
			rows.add(row);
			
			int childCount = editorNode.getChildCount();
			for (int i = 0; i < childCount; i++) {
				INode child = editorNode.getChildAt(i);
				forgeRows(context, rows, child, ++recursionLevel, row);
			}
		}
	}
	
	private CopyCourseOverviewRow forgeRow(CopyCourseContext context, CourseEditorTreeNode editorNode, int recursionLevel, CopyCourseOverviewRow parent) {
		CourseNode courseNode = editorNode.getCourseNode();
		CopyCourseOverviewRow row = new CopyCourseOverviewRow(editorNode, recursionLevel);
		row.setParent(parent);
		if (context.isLearningPath()) {
			LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(courseNode);
			row.setDuration(learningPathConfigs.getDuration());
			row.setStart(learningPathConfigs.getStartDateConfig());
			row.setEnd(learningPathConfigs.getEndDateConfig());
			row.setLearningPathConfigs(learningPathConfigs);
		}
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(source, courseNode);
		row.setAssessmentConfig(assessmentConfig);
		return row;
	}
	
	private void prepareCatalog() {
		try {
			catalogManager.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void referenceGroups() {
		context.setGroupCopyType(CopyType.reference);
		
		target = copyService.copyLearningPathCourse(context);	
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setIdentity(author);
		
		List<BusinessGroup> sourceGroups = businessGroupService.findBusinessGroups(params, source, 0, -1);
		List<BusinessGroup> targetGroups = businessGroupService.findBusinessGroups(params, target, 0, -1);
		
		boolean groupsAreTheSame = true;
		
		for (BusinessGroup sourceGroup : sourceGroups) {
			if (!targetGroups.contains(sourceGroup)) {
				groupsAreTheSame = false;
				break;
			}
		}
		
		Assert.assertTrue(sourceGroups.size() > 0);
		Assert.assertTrue(targetGroups.size() > 0);
		Assert.assertTrue(groupsAreTheSame);
	}
	
	@Test
	public void copyGroups() {
		context.setGroupCopyType(CopyType.copy);
		
		target = copyService.copyLearningPathCourse(context);
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setIdentity(author);
		
		List<BusinessGroup> sourceGroups = businessGroupService.findBusinessGroups(params, source, 0, -1);
		List<BusinessGroup> targetGroups = businessGroupService.findBusinessGroups(params, target, 0, -1);
		
		boolean groupsAreTheSame = false;
		
		for (BusinessGroup sourceGroup : sourceGroups) {
			if (targetGroups.contains(sourceGroup)) {
				groupsAreTheSame = true;
				break;
			}
		}
		
		Assert.assertTrue(sourceGroups.size() > 0);
		Assert.assertTrue(targetGroups.size() > 0);
		Assert.assertFalse(groupsAreTheSame);
	}
	
	@Test
	public void ignoreGroups() {
		context.setGroupCopyType(CopyType.ignore);
		
		target = copyService.copyLearningPathCourse(context);
		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setIdentity(author);
		
		List<BusinessGroup> targetGroups = businessGroupService.findBusinessGroups(params, target, 0, -1);
		
		Assert.assertTrue(targetGroups.isEmpty());
	}
	
	@Test
	public void copyCoaches() {
		context.setCoachesCopyType(CopyType.copy);
		
		target = copyService.copyLearningPathCourse(context);
		
		List<Identity> sourceCoaches = repositoryService.getMembers(source, RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
		List<Identity> targetCoaches = repositoryService.getMembers(target, RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
		
		Assert.assertTrue(sourceCoaches.size() > 0);
		Assert.assertTrue(targetCoaches.size() > 0);
		Assert.assertEquals(sourceCoaches, targetCoaches);
	}
	
	@Test 
	public void ignoreCoaches() {
		context.setCoachesCopyType(CopyType.ignore);
		
		target = copyService.copyLearningPathCourse(context);
		
		List<Identity> targetCoaches = repositoryService.getMembers(target, RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name());
		
		Assert.assertEquals(0, targetCoaches.size());
	}
	
	@Test
	public void copyOwners() {
		context.setOwnersCopyType(CopyType.copy);
		
		target = copyService.copyLearningPathCourse(context);
		
		List<Identity> sourceOwners = repositoryService.getMembers(source, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		List<Identity> targetOwners = repositoryService.getMembers(target, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		
		Assert.assertTrue(sourceOwners.size() > 0);
		Assert.assertTrue(targetOwners.size() > 0);
		Assert.assertEquals(sourceOwners, targetOwners);
	}
	
	@Test 
	public void ignoreOwners() {
		context.setOwnersCopyType(CopyType.ignore);
		
		target = copyService.copyLearningPathCourse(context);
		
		List<Identity> targetOwners = repositoryService.getMembers(target, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		
		Assert.assertEquals(1, targetOwners.size());
	}	
	
	@Test
	public void copyCatalogEntry() {
		// Publish in catalog
		prepareCatalog();
		
		CatalogEntry rootEntry = catalogManager.getRootCatalogEntries().get(0);
		CatalogEntry sourceEntry = catalogManager.createCatalogEntry();
		sourceEntry.setRepositoryEntry(source);
		sourceEntry.setName("Source");

		catalogManager.addCatalogEntry(rootEntry, sourceEntry);
		
		context.setCatalogCopyType(CopyType.copy);
		
		target = copyService.copyLearningPathCourse(context);
		
		List<CatalogEntry> sourceEntries = catalogManager.getCatalogEntriesReferencing(source);
		List<CatalogEntry> targetEntries = catalogManager.getCatalogEntriesReferencing(target);
		
		Assert.assertTrue(sourceEntries.size() > 0);
		Assert.assertTrue(targetEntries.size() > 0);
		Assert.assertEquals(sourceEntries.size(), targetEntries.size());
	}
	
	@Test 
	public void ignoreCatalogEntry() {
		// Publish in catalog
		prepareCatalog();
		
		CatalogEntry rootEntry = catalogManager.getRootCatalogEntries().get(0);
		CatalogEntry sourceEntry = catalogManager.createCatalogEntry();
		sourceEntry.setRepositoryEntry(source);
		sourceEntry.setName("Source");

		catalogManager.addCatalogEntry(rootEntry, sourceEntry);
		
		context.setCatalogCopyType(CopyType.ignore);
		
		target = copyService.copyLearningPathCourse(context);
		
		List<CatalogEntry> targetEntries = catalogManager.getCatalogEntriesReferencing(target);
		
		Assert.assertEquals(0, targetEntries.size());
	}
	
	@Test
	public void copyDisclaimer() {
		context.setDisclaimerCopyType(CopyType.copy);
		
		target = copyService.copyLearningPathCourse(context);
		
		OLATResourceable courseOres = target.getOlatResource();
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
		
		Assert.assertEquals(true, courseConfig.isDisclaimerEnabled());
		
		CourseFactory.closeCourseEditSession(courseOres.getResourceableId(), true);
	}
	
	@Test 
	public void ignoreDisclaimer() {
		context.setDisclaimerCopyType(CopyType.ignore);
		
		target = copyService.copyLearningPathCourse(context);
		
		OLATResourceable courseOres = target.getOlatResource();
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
		
		Assert.assertEquals(false, courseConfig.isDisclaimerEnabled());
		
		CourseFactory.closeCourseEditSession(courseOres.getResourceableId(), true);
	}
	
	@Test
	public void moveDates() {
		String sourceSoftKey = "lf_" + source.getSoftkey();
		RepositoryEntryLifecycle sourceCycle = lifecycleDAO.create(source.getDisplayname(), sourceSoftKey, true, new Date(), new Date());
		source.setLifecycle(sourceCycle);
		long dateDifference = 1000*60*60*24*30;		// Move by 1 month
		
		context.setExecutionType(ExecutionType.beginAndEnd);
		context.setDateDifference(dateDifference);
		context.setBeginDate(new Date(sourceCycle.getValidFrom().getTime() + dateDifference));
		context.setEndDate(new Date(sourceCycle.getValidTo().getTime() + dateDifference));
		
		target = copyService.copyLearningPathCourse(context);
		
		
		RepositoryEntryLifecycle targetCycle = target.getLifecycle();
		
		Assert.assertEquals(dateDifference, targetCycle.getValidFrom().getTime() - sourceCycle.getValidFrom().getTime());
		Assert.assertEquals(dateDifference, targetCycle.getValidTo().getTime() - sourceCycle.getValidTo().getTime());
	}
	
	@Test
	public void copyLectureBlocks() {
		createLectureBlocks();
		context.setLectureBlockCopyType(CopyType.copy);
		
		target = copyService.copyLearningPathCourse(context);
		
		List<LectureBlock> sourceBlocks = lectureService.getLectureBlocks(source);
		List<LectureBlock> targetBlocks = lectureService.getLectureBlocks(target);
		
		Assert.assertTrue(sourceBlocks.size() > 0);
		Assert.assertTrue(targetBlocks.size() > 0);
		Assert.assertEquals(sourceBlocks.size(), targetBlocks.size());		
	}
	
	@Test
	public void ignoreLectureBlocks() {
		createLectureBlocks();
		context.setLectureBlockCopyType(CopyType.ignore);
		
		target = copyService.copyLearningPathCourse(context);
		
		List<LectureBlock> targetBlocks = lectureService.getLectureBlocks(target);
		
		Assert.assertEquals(0, targetBlocks.size());
	}
	
	private void createLectureBlocks() {
		LectureBlock block1 = lectureService.createLectureBlock(source);
		block1.setStartDate(new Date());
		block1.setEndDate(new Date());
		block1.setRollCallStatus(LectureRollCallStatus.open);
		block1.setTitle("Block 1");
		
		lectureService.save(block1, null);
		
		LectureBlock block2 = lectureService.createLectureBlock(source);
		block2.setStartDate(new Date());
		block2.setEndDate(new Date());
		block2.setRollCallStatus(LectureRollCallStatus.open);
		block2.setTitle("Block 2");
		
		lectureService.save(block2, null);
	}
	
	@Test
	public void copyAssessmentModes() {
		createAssessmentModes();
		context.setAssessmentModeCopyType(CopyType.copy);
		
		target = copyService.copyLearningPathCourse(context);
		
		List<AssessmentMode> sourceModes = assessmentModeManager.getAssessmentModeFor(source);
		List<AssessmentMode> targetModes = assessmentModeManager.getAssessmentModeFor(target);
		
		Assert.assertTrue(sourceModes.size() > 0);
		Assert.assertTrue(targetModes.size() > 0);
		Assert.assertEquals(sourceModes.size(), targetModes.size());
	}
	
	@Test
	public void ignoreAssessmentModes() {
		createAssessmentModes();
		context.setAssessmentModeCopyType(CopyType.ignore);
		
		target = copyService.copyLearningPathCourse(context);
		
		List<AssessmentMode> targetModes = assessmentModeManager.getAssessmentModeFor(target);
		
		Assert.assertEquals(0, targetModes.size());
	}
	
	private void createAssessmentModes() {
		AssessmentMode mode1 = assessmentModeManager.createAssessmentMode(source);
		mode1.setName("Mode 1");
		mode1.setBegin(new Date());
		mode1.setEnd(new Date());
		
		assessmentModeManager.persist(mode1);
		
		AssessmentMode mode2 = assessmentModeManager.createAssessmentMode(source);
		mode2.setName("Mode 2");
		mode2.setBegin(new Date());
		mode2.setEnd(new Date());
		
		assessmentModeManager.persist(mode2);
	}
}
