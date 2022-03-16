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
package org.olat.modules.grade.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScaleSearchParams;
import org.olat.modules.grade.GradeScaleStats;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.model.GradeScaleImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeScaleDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GradeSystemDAO gradeSystemDAO;
	
	@Autowired
	private GradeScaleDAO sut;

	@Test
	public void shouldCreateGradeScale() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		dbInstance.commitAndCloseSession();
		
		GradeScale gradeScale = sut.create(repositoryEntry, subIdent);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(((GradeScaleImpl)gradeScale).getKey()).isNotNull();
		softly.assertThat(((GradeScaleImpl)gradeScale).getCreationDate()).isNotNull();
		softly.assertThat(((GradeScaleImpl)gradeScale).getLastModified()).isNotNull();
		softly.assertThat(gradeScale.getRepositoryEntry()).isEqualTo(repositoryEntry);
		softly.assertThat(gradeScale.getSubIdent()).isEqualTo(subIdent);
		softly.assertAll();
	}
	
	@Test
	public void shouldSave() {
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		GradeScale gradeScale = sut.create(repositoryEntry, subIdent);
		dbInstance.commitAndCloseSession();
		
		BigDecimal minScore = BigDecimal.valueOf(1.1);
		gradeScale.setMinScore(minScore);
		BigDecimal maxScore = BigDecimal.valueOf(9.9);
		gradeScale.setMaxScore(maxScore);
		GradeSystem gradeSystem2 = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		gradeScale.setGradeSystem(gradeSystem2);
		gradeScale = sut.save(gradeScale);
		dbInstance.commitAndCloseSession();
		
		GradeScaleSearchParams searchParams = new GradeScaleSearchParams();
		searchParams.setRepositoryEntry(repositoryEntry);
		searchParams.setSubIdent(subIdent);
		gradeScale = sut.load(searchParams).get(0);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(gradeScale.getMinScore()).isEqualByComparingTo(minScore);
		softly.assertThat(gradeScale.getMaxScore()).isEqualByComparingTo(maxScore);
		softly.assertThat(gradeScale.getGradeSystem()).isEqualTo(gradeSystem2);
		softly.assertAll();
	}

	@Test
	public void shouldFilterByRepositoryEntry() {
		GradeSystem gradeSystem = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry repositoryEntry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		GradeScale gradeScale1 = sut.create(repositoryEntry, subIdent);
		gradeScale1.setGradeSystem(gradeSystem);
		sut.save(gradeScale1);
		GradeScale gradeScale2 = sut.create(repositoryEntry, random());
		gradeScale2.setGradeSystem(gradeSystem);
		sut.save(gradeScale2);
		GradeScale gradeScale3 = sut.create(repositoryEntry2, subIdent);
		gradeScale3.setGradeSystem(gradeSystem);
		sut.save(gradeScale3);
		dbInstance.commitAndCloseSession();
		
		GradeScaleSearchParams searchParams = new GradeScaleSearchParams();
		searchParams.setRepositoryEntry(repositoryEntry);
		searchParams.setSubIdent(subIdent);
		List<GradeScale> filtered = sut.load(searchParams);

		assertThat(filtered)
				.containsExactlyInAnyOrder(gradeScale1)
				.doesNotContain(gradeScale2, gradeScale3);
	}

	@Test
	public void shouldDelete() {
		GradeSystem gradeSystem = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		RepositoryEntry repositoryEntry2 = JunitTestHelper.createAndPersistRepositoryEntry();
		String subIdent = random();
		GradeScale gradeScale1 = sut.create(repositoryEntry, subIdent);
		gradeScale1.setGradeSystem(gradeSystem);
		sut.save(gradeScale1);
		GradeScale gradeScale2 = sut.create(repositoryEntry, random());
		gradeScale2.setGradeSystem(gradeSystem);
		sut.save(gradeScale2);
		GradeScale gradeScale3 = sut.create(repositoryEntry2, subIdent);
		gradeScale3.setGradeSystem(gradeSystem);
		sut.save(gradeScale3);
		dbInstance.commitAndCloseSession();
		
		sut.delete(repositoryEntry, subIdent);
		dbInstance.commitAndCloseSession();
		
		GradeScaleSearchParams searchParams = new GradeScaleSearchParams();
		searchParams.setRepositoryEntry(repositoryEntry);
		searchParams.setSubIdent(subIdent);
		List<GradeScale> filtered = sut.load(searchParams);
		assertThat(filtered).isEmpty();
		
		searchParams = new GradeScaleSearchParams();
		searchParams.setRepositoryEntry(repositoryEntry2);
		searchParams.setSubIdent(subIdent);
		filtered = sut.load(searchParams);
		assertThat(filtered).containsExactlyInAnyOrder(gradeScale3)
				.doesNotContain(gradeScale1, gradeScale2);
	}
	
	@Test
	public void shouldLoadStats() {
		GradeSystem gradeSystem1 = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		GradeSystem gradeSystem2 = gradeSystemDAO.create(random(), GradeSystemType.numeric);
		RepositoryEntry repositoryEntry = JunitTestHelper.createAndPersistRepositoryEntry();
		GradeScale gradeScale1 = sut.create(repositoryEntry, random());
		gradeScale1.setGradeSystem(gradeSystem1);
		sut.save(gradeScale1);
		GradeScale gradeScale2 = sut.create(repositoryEntry, random());
		gradeScale2.setGradeSystem(gradeSystem1);
		sut.save(gradeScale2);
		GradeScale gradeScale3 = sut.create(repositoryEntry, random());
		gradeScale3.setGradeSystem(gradeSystem1);
		sut.save(gradeScale3);
		GradeScale gradeScale4 = sut.create(repositoryEntry, random());
		gradeScale4.setGradeSystem(gradeSystem2);
		sut.save(gradeScale4);
		dbInstance.commitAndCloseSession();
		
		Map<Long, Long> gradesystemKeyToCount = sut.loadStats(null).stream()
				.collect(Collectors.toMap(GradeScaleStats::getGradeSystemKey, GradeScaleStats::getCount));
		assertThat(gradesystemKeyToCount.get(gradeSystem1.getKey())).isEqualTo(3);
		assertThat(gradesystemKeyToCount.get(gradeSystem2.getKey())).isEqualTo(1);
		
		assertThat(sut.loadStats(gradeSystem1).get(0).getCount()).isEqualTo(3);
	}


}
