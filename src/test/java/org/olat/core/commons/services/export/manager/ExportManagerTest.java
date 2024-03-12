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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.ExportTask;
import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
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
