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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.TagService;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjTag;
import org.olat.modules.project.ProjTagSearchParams;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Mar 2023<br>>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjTagDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private TagService tagService;
	
	@Autowired
	private ProjTagDAO sut;
	
	@Test
	public void shouldCreateTag() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator, creator);
		ProjArtefact artefact = projectService.createNote(creator, project).getArtefact();
		Tag tag = tagService.getOrCreateTag(random());
		dbInstance.commitAndCloseSession();
		
		ProjTag projTag = sut.create(project, artefact, tag);
		dbInstance.commitAndCloseSession();
		
		assertThat(projTag).isNotNull();
		assertThat(projTag.getCreationDate()).isNotNull();
		assertThat(projTag.getProject()).isEqualTo(project);
		assertThat(projTag.getArtefact()).isEqualTo(artefact);
		assertThat(projTag.getTag()).isEqualTo(tag);
	}
	
	@Test
	public void shouldDelete() {
		ProjTag projTag = createRandomTag();
		
		sut.delete(projTag);
		dbInstance.commitAndCloseSession();
		
		ProjTagSearchParams params = new ProjTagSearchParams();
		params.setProject(projTag.getProject());
		List<ProjTag> artefacts = sut.loadTags(params);
		
		assertThat(artefacts).isEmpty();
	}
	
	@Test
	public void shouldDeleteByArtefact() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator, creator);
		ProjArtefact artefact1 = projectService.createNote(creator, project).getArtefact();
		ProjArtefact artefact2 = projectService.createNote(creator, project).getArtefact();
		Tag tag1 = tagService.getOrCreateTag(random());
		Tag tag2 = tagService.getOrCreateTag(random());
		
		sut.create(project, artefact1, tag1);
		sut.create(project, artefact1, tag2);
		ProjTag projTag21 = sut.create(project, artefact2, tag1);
		ProjTag projTag22 = sut.create(project, artefact2, tag2);
		dbInstance.commitAndCloseSession();
		
		sut.delete(artefact1);
		dbInstance.commitAndCloseSession();
		
		ProjTagSearchParams params = new ProjTagSearchParams();
		params.setProject(project);
		List<ProjTag> artefacts = sut.loadTags(params);
		
		assertThat(artefacts).containsExactlyInAnyOrder(projTag21, projTag22);
	}
	
	@Test
	public void shouldLoadProjectTags() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator, creator);
		ProjArtefact artefact1 = projectService.createNote(creator, project).getArtefact();
		ProjArtefact artefact2 = projectService.createNote(creator, project).getArtefact();
		ProjArtefact artefact3 = projectService.createNote(creator, project).getArtefact();
		Tag tag1 = tagService.getOrCreateTag(random());
		Tag tag2 = tagService.getOrCreateTag(random());
		Tag tag3 = tagService.getOrCreateTag(random());
		Tag tag4 = tagService.getOrCreateTag(random());
		
		projectService.updateTags(creator, artefact1, List.of(tag1.getDisplayName(), tag2.getDisplayName(), tag3.getDisplayName()));
		projectService.updateTags(creator, artefact2, List.of(tag1.getDisplayName(), tag2.getDisplayName()));
		projectService.updateTags(creator, artefact3, List.of(tag1.getDisplayName(), tag4.getDisplayName()));
		projectService.updateTags(creator, artefact3, List.of(tag1.getDisplayName()));
		dbInstance.commitAndCloseSession();
		
		// Tags with artefact selection
		Map<Long, TagInfo> keyToTag = sut.loadProjectTagInfos(project, artefact2).stream()
				.collect(Collectors.toMap(TagInfo::getKey, Function.identity()));
		assertThat(keyToTag).hasSize(3);
		assertThat(keyToTag.get(tag1.getKey()).getCount()).isEqualTo(3);
		assertThat(keyToTag.get(tag2.getKey()).getCount()).isEqualTo(2);
		assertThat(keyToTag.get(tag3.getKey()).getCount()).isEqualTo(1);
		assertThat(keyToTag.get(tag1.getKey()).isSelected()).isTrue();
		assertThat(keyToTag.get(tag2.getKey()).isSelected()).isTrue();
		assertThat(keyToTag.get(tag3.getKey()).isSelected()).isFalse();
		
		// Tags without artefact selection
		keyToTag = sut.loadProjectTagInfos(project, null).stream()
					.collect(Collectors.toMap(TagInfo::getKey, Function.identity()));
		assertThat(keyToTag.get(tag1.getKey()).getCount()).isEqualTo(3);
		assertThat(keyToTag.get(tag2.getKey()).getCount()).isEqualTo(2);
		assertThat(keyToTag.get(tag3.getKey()).getCount()).isEqualTo(1);
		assertThat(keyToTag.get(tag1.getKey()).isSelected()).isFalse();
		assertThat(keyToTag.get(tag2.getKey()).isSelected()).isFalse();
		assertThat(keyToTag.get(tag3.getKey()).isSelected()).isFalse();
	}
	
	@Test
	public void shouldLoad_filter_project() {
		ProjTag projTag = createRandomTag();
		createRandomTag();
		
		ProjTagSearchParams params = new ProjTagSearchParams();
		params.setProject(projTag.getProject());
		List<ProjTag> projTags = sut.loadTags(params);
		
		assertThat(projTags).containsExactlyInAnyOrder(projTag);
	}
	
	@Test
	public void shouldLoad_filter_artefacts() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator, creator);
		ProjArtefact artefact1 = projectService.createNote(creator, project).getArtefact();
		ProjArtefact artefact2 = projectService.createNote(creator, project).getArtefact();
		ProjArtefact artefact3 = projectService.createNote(creator, project).getArtefact();
		Tag tag1 = tagService.getOrCreateTag(random());
		Tag tag2 = tagService.getOrCreateTag(random());
		ProjTag projTag11 = sut.create(project, artefact1, tag1);
		ProjTag projTag12 = sut.create(project, artefact1, tag2);
		sut.create(project, artefact2, tag1);
		sut.create(project, artefact2, tag2);
		ProjTag projTag31 = sut.create(project, artefact3, tag1);
		ProjTag projTag32 = sut.create(project, artefact3, tag2);
		dbInstance.commitAndCloseSession();
		
		ProjTagSearchParams params = new ProjTagSearchParams();
		params.setArtefacts(List.of(artefact1, artefact3));
		List<ProjTag> projTags = sut.loadTags(params);
		
		assertThat(projTags).containsExactlyInAnyOrder(projTag11, projTag12, projTag31, projTag32);
	}
	
	@Test
	public void shouldLoad_filter_artefactType() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator, creator);
		ProjArtefact artefact1 = projectService.createNote(creator, project).getArtefact();
		ProjArtefact artefact2 =  projectService.createToDo(creator, project).getArtefact();
		ProjArtefact artefact3 = projectService.createNote(creator, project).getArtefact();
		Tag tag1 = tagService.getOrCreateTag(random());
		Tag tag2 = tagService.getOrCreateTag(random());
		ProjTag projTag11 = sut.create(project, artefact1, tag1);
		ProjTag projTag12 = sut.create(project, artefact1, tag2);
		sut.create(project, artefact2, tag1);
		sut.create(project, artefact2, tag2);
		ProjTag projTag31 = sut.create(project, artefact3, tag1);
		ProjTag projTag32 = sut.create(project, artefact3, tag2);
		dbInstance.commitAndCloseSession();
		
		ProjTagSearchParams params = new ProjTagSearchParams();
		params.setProject(project);
		params.setArtefactTypes(List.of(ProjNote.TYPE));
		List<ProjTag> projTags = sut.loadTags(params);
		
		assertThat(projTags).containsExactlyInAnyOrder(projTag11, projTag12, projTag31, projTag32);
	}
	
	@Test
	public void shouldLoad_filter_artefactStatus() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator, creator);
		ProjArtefact artefact1 = projectService.createNote(creator, project).getArtefact();
		ProjNote note2 = projectService.createNote(creator, project);
		ProjArtefact artefact2 = note2.getArtefact();
		ProjArtefact artefact3 = projectService.createNote(creator, project).getArtefact();
		Tag tag1 = tagService.getOrCreateTag(random());
		Tag tag2 = tagService.getOrCreateTag(random());
		ProjTag projTag11 = sut.create(project, artefact1, tag1);
		ProjTag projTag12 = sut.create(project, artefact1, tag2);
		sut.create(project, artefact2, tag1);
		sut.create(project, artefact2, tag2);
		ProjTag projTag31 = sut.create(project, artefact3, tag1);
		ProjTag projTag32 = sut.create(project, artefact3, tag2);
		projectService.deleteNoteSoftly(creator, note2);
		dbInstance.commitAndCloseSession();
		
		ProjTagSearchParams params = new ProjTagSearchParams();
		params.setProject(project);
		params.setArtefactStatus(List.of(ProjectStatus.active));
		List<ProjTag> projTags = sut.loadTags(params);
		
		assertThat(projTags).containsExactlyInAnyOrder(projTag11, projTag12, projTag31, projTag32);
	}
	
	private ProjTag createRandomTag() {
		Identity creator = JunitTestHelper.createAndPersistIdentityAsRndUser(random());
		ProjProject project = projectService.createProject(creator, creator);
		ProjNote note = projectService.createNote(creator, project);
		Tag tag = tagService.getOrCreateTag(random());
		ProjTag projTag = sut.create(project, note.getArtefact(), tag);
		dbInstance.commitAndCloseSession();
		return projTag;
	}

}
