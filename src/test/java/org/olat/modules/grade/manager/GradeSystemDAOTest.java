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

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.GradeSystemSearchParams;
import org.olat.modules.grade.GradeSystemType;
import org.olat.modules.grade.NumericResolution;
import org.olat.modules.grade.Rounding;
import org.olat.modules.grade.model.GradeSystemImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Feb 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GradeSystemDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private GradeSystemDAO sut;

	@Test
	public void shouldCreateGradeSystem() {
		String identifier = random();
		GradeSystemType type = GradeSystemType.text;
		
		GradeSystem gradeSystem = sut.create(identifier, type);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(gradeSystem.getKey()).isNotNull();
		softly.assertThat(((GradeSystemImpl)gradeSystem).getCreationDate()).isNotNull();
		softly.assertThat(((GradeSystemImpl)gradeSystem).getLastModified()).isNotNull();
		softly.assertThat(gradeSystem.getIdentifier()).isEqualTo(identifier);
		softly.assertThat(gradeSystem.isPredefined()).isFalse();
		softly.assertThat(gradeSystem.getType()).isEqualTo(type);
		softly.assertThat(gradeSystem.isEnabled()).isTrue();
		softly.assertThat(gradeSystem.getResolution()).isNull();
		softly.assertThat(gradeSystem.getRounding()).isNull();
		softly.assertThat(gradeSystem.getBestGrade()).isNull();
		softly.assertThat(gradeSystem.getLowestGrade()).isNull();
		softly.assertThat(gradeSystem.getCutValue()).isNull();
		softly.assertAll();
	}
	
	@Test
	public void shouldSave() {
		GradeSystem gradeSystem = sut.create(random(), GradeSystemType.numeric);
		dbInstance.commitAndCloseSession();
		
		gradeSystem.setEnabled(false);
		NumericResolution resolution = NumericResolution.tenth;
		gradeSystem.setResolution(resolution);
		Rounding rounding = Rounding.down;
		gradeSystem.setRounding(rounding);
		Integer bestGrade = Integer.valueOf(6);
		gradeSystem.setBestGrade(bestGrade);
		Integer lowestGrade = Integer.valueOf(1);
		gradeSystem.setLowestGrade(lowestGrade);
		BigDecimal cutValue = BigDecimal.valueOf(2.3d);
		gradeSystem.setCutValue(cutValue);
		
		gradeSystem = sut.save(gradeSystem);
		dbInstance.commitAndCloseSession();
		
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		searchParams.setGradeSystem(gradeSystem);
		gradeSystem = sut.load(searchParams).get(0);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(gradeSystem.isEnabled()).isFalse();
		softly.assertThat(gradeSystem.getResolution()).isEqualTo(resolution);
		softly.assertThat(gradeSystem.getRounding()).isEqualTo(rounding);
		softly.assertThat(gradeSystem.getBestGrade()).isEqualTo(bestGrade);
		softly.assertThat(gradeSystem.getLowestGrade()).isEqualTo(lowestGrade);
		softly.assertThat(gradeSystem.getCutValue()).isEqualByComparingTo(cutValue);
		softly.assertAll();
	}

	@Test
	public void shouldFilterByKeys() {
		GradeSystem gradeSystem1 = sut.create(random(), GradeSystemType.numeric);
		GradeSystem gradeSystem2 = sut.create(random(), GradeSystemType.numeric);
		GradeSystem gradeSystem3 = sut.create(random(), GradeSystemType.numeric);
		dbInstance.commitAndCloseSession();
		
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		searchParams.setGradeSystems(List.of(gradeSystem1, gradeSystem2));
		List<GradeSystem> filtered = sut.load(searchParams);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(gradeSystem1, gradeSystem2)
				.doesNotContain(gradeSystem3);
	}

	@Test
	public void shouldFilterByIdentifier() {
		GradeSystem gradeSystem1 = sut.create(random(), GradeSystemType.numeric);
		GradeSystem gradeSystem2 = sut.create(random(), GradeSystemType.numeric);
		GradeSystem gradeSystem3 = sut.create(random(), GradeSystemType.numeric);
		dbInstance.commitAndCloseSession();
		
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		searchParams.setIdentifier(gradeSystem1.getIdentifier());
		List<GradeSystem> filtered = sut.load(searchParams);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(gradeSystem1)
				.doesNotContain(gradeSystem2, gradeSystem3);
	}

	@Test
	public void shouldFilterByEnabled() {
		GradeSystem gradeSystem1 = sut.create(random(), GradeSystemType.numeric);
		gradeSystem1.setEnabled(true);
		sut.save(gradeSystem1);
		GradeSystem gradeSystem2 = sut.create(random(), GradeSystemType.numeric);
		gradeSystem2.setEnabled(true);
		sut.save(gradeSystem2);
		GradeSystem gradeSystem3 = sut.create(random(), GradeSystemType.numeric);
		gradeSystem3.setEnabled(false);
		sut.save(gradeSystem3);
		dbInstance.commitAndCloseSession();
		
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		searchParams.setGradeSystems(List.of(gradeSystem1, gradeSystem2, gradeSystem3));
		searchParams.setEnabledOnly(true);
		List<GradeSystem> filtered = sut.load(searchParams);
		
		assertThat(filtered)
				.containsExactlyInAnyOrder(gradeSystem1, gradeSystem2)
				.doesNotContain(gradeSystem3);
	}

	@Test
	public void shouldDelete() {
		GradeSystem gradeSystem = sut.create(random(), GradeSystemType.numeric);
		dbInstance.commitAndCloseSession();
		
		sut.delete(gradeSystem);
		dbInstance.commitAndCloseSession();
		
		GradeSystemSearchParams searchParams = new GradeSystemSearchParams();
		searchParams.setGradeSystem(gradeSystem);
		List<GradeSystem> filtered = sut.load(searchParams);
		
		assertThat(filtered).isEmpty();
	}

}
