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
package org.olat.core.commons.services.export.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.ExportMetadataToCurriculum;
import org.olat.core.commons.services.export.ExportMetadataToCurriculumElement;
import org.olat.core.commons.services.export.ExportMetadataToOrganisation;
import org.olat.core.commons.services.export.ExportTask;
import org.olat.core.commons.services.export.model.CurriculumReportBlocParameters;
import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ExportManager exportManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private OrganisationService organisationService;
	
	@Test
	public void getContainer() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("export-area-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(initialAuthor);
		ICourse course = CourseFactory.loadCourse(entry);
		CourseNode courseNode = course.getRunStructure().getRootNode();
		
		VFSContainer container = exportManager.getExportContainer(entry, courseNode.getIdent());
		Assert.assertNotNull(container);
		Assert.assertTrue(container.exists());
	}
	
	/**
	 * Mostly a dummy test to check the query syntax and such things.
	 */
	@Test
	public void getResultsExport() {
		Identity initialAuthor = JunitTestHelper.createAndPersistIdentityAsRndUser("export-area-1");
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(initialAuthor);
		ICourse course = CourseFactory.loadCourse(entry);
		CourseNode courseNode = course.getRunStructure().getRootNode();
		
		SearchExportMetadataParameters params = new SearchExportMetadataParameters(List.of(ArchiveType.COMPLETE, ArchiveType.PARTIAL));
		List<ExportInfos> infos = exportManager.getResultsExport(entry, courseNode.getIdent(), params);
		Assert.assertNotNull(infos);
		Assert.assertTrue(infos.isEmpty());
	}
	
	@Test
	public void startExportWithRepositoryEntry() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("task-1");
		LittleTask task = new LittleTask();
		String subIdent = "task-two";
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		ExportMetadata metadata = exportManager.startExport(task, "New archive", "Brand new one", null, ArchiveType.COMPLETE,
				null, false, entry, subIdent, id);
		dbInstance.commit();
		
		Assert.assertNotNull(metadata);
		Assert.assertNotNull(metadata.getKey());
		Assert.assertNotNull(metadata.getTask());
		Assert.assertEquals(entry, metadata.getEntry());
		Assert.assertEquals(subIdent, metadata.getSubIdent());
		Assert.assertNotNull(metadata.getTask());
		Assert.assertEquals(entry.getOlatResource(), metadata.getTask().getResource());
		Assert.assertEquals(subIdent, metadata.getTask().getResSubPath());
	}
	
	@Test
	public void getExportMetadataByTask() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("task-1");
		LittleTask task = new LittleTask();
		String subIdent = "task-two";
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		ExportMetadata metadata = exportManager.startExport(task, "New archive", "Brand new one", null, ArchiveType.COMPLETE,
				null, false, entry, subIdent, id);
		dbInstance.commit();

		Assert.assertNotNull(metadata);
		Assert.assertNotNull(metadata.getTask());
		ExportMetadata reloadedMetadata = exportManager.getExportMetadataByTask(metadata.getTask());
		Assert.assertEquals(metadata, reloadedMetadata);
	}
	
	@Test
	public void addMetadataOrganisations() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("task-1");
		LittleTask task = new LittleTask();
		String subIdent = "task-two";
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		ExportMetadata metadata = exportManager.startExport(task, "New archive", "Brand new one", null, ArchiveType.COMPLETE,
				null, false, entry, subIdent, id);
		Organisation organisation = organisationService.createOrganisation("Export -1", "EXP-1", null, null, null, id);
		dbInstance.commit();
		
		// Add an organisation
		List<Organisation> organisations = List.of(organisation);
		ExportMetadata updatedMetadata = exportManager.addMetadataOrganisations(metadata, organisations);
		Assert.assertEquals(metadata, updatedMetadata);
		dbInstance.commitAndCloseSession();
		
		ExportMetadata reloadedMetadata = exportManager.getExportMetadataByKey(updatedMetadata.getKey());
		Assertions.assertThat(reloadedMetadata.getOrganisations())
			.hasSize(1)
			.map(ExportMetadataToOrganisation::getOrganisation)
			.containsExactly(organisation);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void addMetadataCurriculums() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("task-1");
		LittleTask task = new LittleTask();
		String subIdent = "task-cur-two";
		Curriculum curriculum = curriculumService.createCurriculum("Task", "TASK-1", null, false, null);
		ExportMetadata metadata = exportManager.startExport(task, "New archive", "Brand new one", null, ArchiveType.COMPLETE,
				null, false, (OLATResource)null, subIdent, id);
		dbInstance.commit();
		
		// Add a curriculum
		List<Curriculum> curriculums = List.of(curriculum);
		ExportMetadata updatedMetadata = exportManager.addMetadataCurriculums(metadata, curriculums);
		Assert.assertEquals(metadata, updatedMetadata);
		dbInstance.commitAndCloseSession();
		
		ExportMetadata reloadedMetadata = exportManager.getExportMetadataByKey(updatedMetadata.getKey());
		Assertions.assertThat(reloadedMetadata.getCurriculums())
			.hasSize(1)
			.map(ExportMetadataToCurriculum::getCurriculum)
			.containsExactly(curriculum);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void addMetadataCurriculumElements() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("task-2");
		LittleTask task = new LittleTask();
		String subIdent = "task-cur-el-two";
		Curriculum curriculum = curriculumService.createCurriculum("Task", "TASK-2", null, false, null);
		CurriculumElement element1 = curriculumService.createCurriculumElement("Task 2.1", "Task 2.1",
				CurriculumElementStatus.active, null, null, null, null, null, null, null, curriculum);
		CurriculumElement element2 = curriculumService.createCurriculumElement("Task 2.2", "Task 2.2",
				CurriculumElementStatus.active, null, null, null, null, null, null, null, curriculum);
	
		ExportMetadata metadata = exportManager.startExport(task, "New archive", "Brand new one", null, ArchiveType.COMPLETE,
				null, false, (OLATResource)null, subIdent, id);
		dbInstance.commit();
		
		// Add 2 curriculum elements
		List<CurriculumElement> elements = List.of(element1, element2);
		ExportMetadata updatedMetadata = exportManager.addMetadataCurriculumElements(metadata, elements);
		Assert.assertEquals(metadata, updatedMetadata);
		dbInstance.commitAndCloseSession();
		
		ExportMetadata reloadedMetadata = exportManager.getExportMetadataByKey(updatedMetadata.getKey());
		Assertions.assertThat(reloadedMetadata.getCurriculumElements())
			.hasSize(2)
			.map(ExportMetadataToCurriculumElement::getCurriculumElement)
			.containsExactlyInAnyOrder(element1, element2);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void searchMetadataExclusiveCurriculums() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("exporter-1");
		Curriculum curriculum1 = curriculumService.createCurriculum("Search metadata 1", "Search metadata 1", null, false, null);
		Curriculum curriculum2 = curriculumService.createCurriculum("Search metadata 2", "Search metadata 2", null, false, null);
		curriculumService.addMember(curriculum1, id, CurriculumRoles.owner);
		
		String subIdent = "task-search-curl-one";
		LittleTask task = new LittleTask();
		ExportMetadata metadata = exportManager.startExport(task, "New archive", "Brand new one", null, ArchiveType.CURRICULUM,
				null, false, (OLATResource)null, subIdent, id);
		metadata = exportManager.addMetadataCurriculums(metadata, List.of(curriculum1));
		dbInstance.commit();

		SearchExportMetadataParameters params = new SearchExportMetadataParameters(null, null, List.of(ArchiveType.CURRICULUM));
		params.setReportSubParameters(new CurriculumReportBlocParameters(null, List.of(curriculum1), null));
		List<ExportMetadata> metadataList = exportManager.searchMetadata(params);
		assertThat(metadataList)
			.hasSize(1)
			.contains(metadata);
		
		SearchExportMetadataParameters notParams = new SearchExportMetadataParameters(null, null, List.of(ArchiveType.CURRICULUM));
		notParams.setReportSubParameters(new CurriculumReportBlocParameters(null, List.of(curriculum2), null));
		List<ExportMetadata> metadataEmptyList = exportManager.searchMetadata(notParams);
		assertThat(metadataEmptyList)
			.isEmpty();
	}
	
	@Test
	public void searchMetadataExclusiveCurriculumsAlt() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("exporter-1");
		Curriculum curriculum1 = curriculumService.createCurriculum("Search metadata 3", "Search metadata 3", null, false, null);
		Curriculum curriculum2 = curriculumService.createCurriculum("Search metadata 4", "Search metadata 4", null, false, null);
		Curriculum curriculum3 = curriculumService.createCurriculum("Search metadata 5", "Search metadata 5", null, false, null);
		curriculumService.addMember(curriculum1, id, CurriculumRoles.owner);
		
		String subIdent = "task-search-curl-one";
		LittleTask task = new LittleTask();
		ExportMetadata metadata = exportManager.startExport(task, "New archive", "Brand new one", null, ArchiveType.CURRICULUM,
				null, false, (OLATResource)null, subIdent, id);
		metadata = exportManager.addMetadataCurriculums(metadata, List.of(curriculum1, curriculum2));
		dbInstance.commit();

		SearchExportMetadataParameters params = new SearchExportMetadataParameters(null, null, List.of(ArchiveType.CURRICULUM));
		params.setReportSubParameters(new CurriculumReportBlocParameters(null, List.of(curriculum1, curriculum2), null));
		List<ExportMetadata> metadataList = exportManager.searchMetadata(params);
		assertThat(metadataList)
			.hasSize(1)
			.contains(metadata);

		SearchExportMetadataParameters notParams = new SearchExportMetadataParameters(null, null, List.of(ArchiveType.CURRICULUM));
		notParams.setReportSubParameters(new CurriculumReportBlocParameters(null, List.of(curriculum2, curriculum3), null));
		List<ExportMetadata> metadataEmptyList = exportManager.searchMetadata(notParams);
		assertThat(metadataEmptyList)
			.isEmpty();
		
		SearchExportMetadataParameters notParamsAlt = new SearchExportMetadataParameters(null, null, List.of(ArchiveType.CURRICULUM));
		notParamsAlt.setReportSubParameters(new CurriculumReportBlocParameters(null, List.of(curriculum1, curriculum3), null));
		List<ExportMetadata> metadataEmptyListAlt = exportManager.searchMetadata(notParamsAlt);
		assertThat(metadataEmptyListAlt)
			.isEmpty();
	}
	
	public static class LittleTask implements ExportTask {

		private static final long serialVersionUID = 5706709932092654234L;

		@Override
		public void setTask(Task task) {
			//
		}

		@Override
		public void run() {
			//
		}

		@Override
		public String getTitle() {
			return null;
		}

		@Override
		public VFSLeaf getExportZip() {
			return null;
		}
	
		
	}

}
