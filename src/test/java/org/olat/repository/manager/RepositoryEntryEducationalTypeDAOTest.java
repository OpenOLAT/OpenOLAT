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

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.createAndPersistRepositoryEntry;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryEducationalTypeImpl;
import org.olat.repository.model.RepositoryEntryEducationalTypeStat;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryEducationalTypeDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryManager repositoryManager;
	
	@Autowired
	private RepositoryEntryEducationalTypeDAO sut;
	
	@Test
	public void shouldCreateEducationalType() {
		String identifier = random();
		
		RepositoryEntryEducationalType educationalType = sut.create(identifier);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(educationalType.getKey()).isNotNull();
		softly.assertThat(((RepositoryEntryEducationalTypeImpl)educationalType).getCreationDate()).isNotNull();
		softly.assertThat(((RepositoryEntryEducationalTypeImpl)educationalType).getLastModified()).isNotNull();
		softly.assertThat(educationalType.getIdentifier()).isEqualTo(identifier);
		softly.assertThat(educationalType.isPredefined()).isFalse();
		softly.assertAll();
	}

	@Test
	public void shouldCreatePredefinedEducationalType() {
		String identifier = random();
		String cssClass = random();
		RepositoryEntryEducationalType educationalType = sut.createPredefined(identifier, cssClass);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(educationalType.getKey()).isNotNull();
		softly.assertThat(((RepositoryEntryEducationalTypeImpl)educationalType).getCreationDate()).isNotNull();
		softly.assertThat(((RepositoryEntryEducationalTypeImpl)educationalType).getLastModified()).isNotNull();
		softly.assertThat(educationalType.getIdentifier()).isEqualTo(identifier);
		softly.assertThat(educationalType.isPredefined()).isTrue();
		softly.assertThat(educationalType.getCssClass()).isEqualTo(cssClass);
		softly.assertAll();
	}
	
	@Test
	public void shouldSave() {
		RepositoryEntryEducationalType educationalType = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		String cssClass = random();
		educationalType.setCssClass(cssClass);
		educationalType = sut.save(educationalType);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(educationalType.getCssClass()).isEqualTo(cssClass);
		softly.assertAll();
	}

	@Test
	public void shouldLoadByKey() {
		RepositoryEntryEducationalType educationalType = sut.create(random());
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryEducationalType reloaded = sut.loadByKey(educationalType.getKey());
		
		assertThat(reloaded).isEqualTo(educationalType);
	}

	@Test
	public void shouldLoadByIdentifier() {
		String identifier = random();
		RepositoryEntryEducationalType educationalType = sut.create(identifier);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryEducationalType reloaded = sut.loadByIdentifier(identifier);
		
		assertThat(reloaded).isEqualTo(educationalType);
	}
	
	@Test
	public void shouldLoadAll() {
		String identifier = random();
		sut.create(identifier);
		dbInstance.commitAndCloseSession();
		
		List<RepositoryEntryEducationalType> all = sut.loadAll();
		
		assertThat(all).hasSizeGreaterThan(0);
	}
	
	@Test
	public void shouldDelete() {
		String identifier = random();
		RepositoryEntryEducationalType educationalType = sut.create(identifier);
		dbInstance.commitAndCloseSession();
		
		sut.delete(educationalType);
		dbInstance.commitAndCloseSession();
		
		RepositoryEntryEducationalType reloaded = sut.loadByIdentifier(identifier);
		assertThat(reloaded).isNull();
	}
	
	@Test
	public void shouldGetStats() {
		RepositoryEntryEducationalType educationalType1 = sut.create(random());
		repositoryManager.setDescriptionAndName(createAndPersistRepositoryEntry(), random(), null, random(), random(),
				null, null, null, null, null, null, null, null, null, null, educationalType1);
		RepositoryEntryEducationalType educationalType2 = sut.create(random());
		repositoryManager.setDescriptionAndName(createAndPersistRepositoryEntry(), random(), null, random(), random(),
				null, null, null, null, null, null, null, null, null, null, educationalType2);
		repositoryManager.setDescriptionAndName(createAndPersistRepositoryEntry(), random(), null, random(), random(),
				null, null, null, null, null, null, null, null, null, null, educationalType2);
		RepositoryEntryEducationalType educationalType3 = sut.create(random());

		List<RepositoryEntryEducationalTypeStat> stats = sut.loadStats();
		
		Map<Long, Long> keysToNoEntries = stats.stream()
				.collect(Collectors.toMap(
						RepositoryEntryEducationalTypeStat::getEducationalTypeKey,
						RepositoryEntryEducationalTypeStat::getNumberOfRepositoryEntries));
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(keysToNoEntries.get(educationalType1.getKey())).isEqualTo(1);
		softly.assertThat(keysToNoEntries.get(educationalType2.getKey())).isEqualTo(2);
		softly.assertThat(keysToNoEntries.get(educationalType3.getKey())).isNull();
		softly.assertAll();
	}

}
