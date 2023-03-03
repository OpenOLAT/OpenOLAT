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
import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Dez 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjFileDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private ProjArtefactDAO artefactDao;
	@Autowired
	private VFSMetadataDAO vfsMetadataDAO;
	
	@Autowired
	private ProjFileDAO sut;
	
	@Test
	public void shouldCreateFile() {
		ProjArtefact artefact = createRandomArtefact();
		VFSMetadata metadata = createRandomMetadata();
		dbInstance.commitAndCloseSession();
		
		ProjFile file = sut.create(artefact, metadata);
		dbInstance.commitAndCloseSession();
		
		assertThat(file).isNotNull();
		assertThat(file.getCreationDate()).isNotNull();
		assertThat(file.getLastModified()).isNotNull();
		assertThat(file.getArtefact()).isEqualTo(artefact);
		assertThat(file.getVfsMetadata()).isEqualTo(metadata);
	}
	
	@Test
	public void shouldSaveFile() {
		
		ProjArtefact artefact = createRandomArtefact();
		VFSMetadata metadata = createRandomMetadata();
		ProjFile file = sut.create(artefact, metadata);
		dbInstance.commitAndCloseSession();
		
		sut.save(file);
		dbInstance.commitAndCloseSession();
		
		// No exception
	}
	
	@Test
	public void shouldCount() {
		ProjFile file1 = createRandomFile();
		ProjFile file2 = createRandomFile();
		createRandomFile();
		
		ProjFileSearchParams params = new ProjFileSearchParams();
		params.setFiles(List.of(file1, file2));
		long count = sut.loadFilesCount(params);
		
		assertThat(count).isEqualTo(2);
	}
	
	@Test
	public void shouldLoad_filter_project() {
		ProjFile file = createRandomFile();
		createRandomFile();
		
		ProjFileSearchParams params = new ProjFileSearchParams();
		params.setProject(file.getArtefact().getProject());
		List<ProjFile> files = sut.loadFiles(params);
		
		assertThat(files).containsExactlyInAnyOrder(file);
	}
	
	@Test
	public void shouldLoad_filter_fileKeys() {
		ProjFile file1 = createRandomFile();
		ProjFile file2 = createRandomFile();
		createRandomFile();
		
		ProjFileSearchParams params = new ProjFileSearchParams();
		params.setFiles(List.of(file1, file2));
		List<ProjFile> files = sut.loadFiles(params);
		
		assertThat(files).containsExactlyInAnyOrder(file1, file2);
	}
	
	@Test
	public void shouldLoad_filter_artefacts() {
		ProjFile file1 = createRandomFile();
		ProjFile file2 = createRandomFile();
		createRandomFile();
		
		ProjFileSearchParams params = new ProjFileSearchParams();
		params.setArtefacts(List.of(file1.getArtefact(), file2.getArtefact()));
		List<ProjFile> files = sut.loadFiles(params);
		
		assertThat(files).containsExactlyInAnyOrder(file1, file2);
	}
	
	@Test
	public void shouldLoad_filter_status() {
		ProjFile file1 = createRandomFile();
		projectService.deleteFileSoftly(file1.getArtefact().getCreator(), file1);
		ProjFile file2 = createRandomFile();
		projectService.deleteFileSoftly(file2.getArtefact().getCreator(), file2);
		ProjFile file3 = createRandomFile();
		
		ProjFileSearchParams params = new ProjFileSearchParams();
		params.setFiles(List.of(file1, file2, file3));
		params.setStatus(List.of(ProjectStatus.deleted));
		List<ProjFile> files = sut.loadFiles(params);
		
		assertThat(files).containsExactlyInAnyOrder(file1, file2);
	}
	
	@Test
	public void shouldLoad_filter_creators() {
		Identity identity1 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		Identity identity2 = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjFile file1 = createRandomFile();
		file1.getVfsMetadata().setFileInitializedBy(identity1);
		vfsMetadataDAO.updateMetadata(file1.getVfsMetadata());
		ProjFile file2 = createRandomFile();
		file2.getVfsMetadata().setFileInitializedBy(identity1);
		vfsMetadataDAO.updateMetadata(file2.getVfsMetadata());
		ProjFile file3 = createRandomFile();
		file3.getVfsMetadata().setFileInitializedBy(identity2);
		vfsMetadataDAO.updateMetadata(file3.getVfsMetadata());
		dbInstance.commitAndCloseSession();
		
		ProjFileSearchParams params = new ProjFileSearchParams();
		params.setCreators(List.of(identity1));
		List<ProjFile> files = sut.loadFiles(params);
		
		assertThat(files).containsExactlyInAnyOrder(file1, file2);
	}
	
	@Test
	public void shouldLoad_filter_suffix() {
		ProjFile file1 = createRandomFile("test.txt");
		ProjFile file2 = createRandomFile("test.txt2");
		ProjFile file3 = createRandomFile("test.abc");
		ProjFile file4 = createRandomFile("test.xyz");
		
		ProjFileSearchParams params = new ProjFileSearchParams();
		params.setFiles(List.of(file1, file2, file3, file4));
		params.setSuffixes(List.of(".txt", ".abc"));
		List<ProjFile> files = sut.loadFiles(params);
		
		assertThat(files).containsExactlyInAnyOrder(file1, file3);
	}
	
	@Test
	public void shouldLoad_filter_createdAfter() {
		ProjFile file1 = createRandomFile();
		
		ProjFileSearchParams params = new ProjFileSearchParams();
		params.setFiles(List.of(file1));
		params.setCreatedAfter(new Date());
		sut.loadFiles(params);
		
		// Just syntax check because created date can't be modified.
	}
	
	@Test
	public void shouldLoad_filter_lastModified() {
		ProjFile file1 = createRandomFile();
		ProjFile file2 = createRandomFile();
		ProjFile file3 = createRandomFile();
		
		ProjFileSearchParams params = new ProjFileSearchParams();
		params.setFiles(List.of(file1, file2, file3));
		params.setNumLastModified(2);
		List<ProjFile> files = sut.loadFiles(params);
		
		assertThat(files).containsExactlyInAnyOrder(file2, file3);
	}
	
	private ProjFile createRandomFile() {
		ProjArtefact artefact = createRandomArtefact();
		VFSMetadata metadata = createRandomMetadata();
		ProjFile file = sut.create(artefact, metadata);
		dbInstance.commitAndCloseSession();
	
		return file;
	}

	private ProjFile createRandomFile(String filename) {
		ProjFile file = createRandomFile();
		VFSMetadata vfsMetadata = file.getVfsMetadata();
		((VFSMetadataImpl)vfsMetadata).setFilename(filename);
		vfsMetadataDAO.updateMetadata(vfsMetadata);
		dbInstance.commitAndCloseSession();
		
		return file;
	}

	private ProjArtefact createRandomArtefact() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator);
		ProjArtefact artefact = artefactDao.create(ProjFile.TYPE, project, creator);
		return artefact;
	}

	private VFSMetadata createRandomMetadata() {
		return vfsMetadataDAO.createMetadata(random(), random(), random(), new Date(), 1000l, false, "file://" + random(), "file", null);
	}

}
