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
package org.olat.course.assessment.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.course.assessment.ScoreAccountingTrigger;
import org.olat.course.assessment.ScoreAccountingTriggerData;
import org.olat.course.assessment.ScoreAccountingTriggerSearchParams;
import org.olat.course.assessment.model.ScoreAccountingTriggerImpl;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ScoreAccountingTriggerDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	
	@Autowired
	private ScoreAccountingTriggerDAO sut;
	
	@Test
	public void shouldCreate() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		String subIdent = random();
		ScoreAccountingTriggerData data = new ScoreAccountingTriggerData();
		String identifier = random();
		data.setIdentifier(identifier);
		BusinessGroupRef businessGroupRef = () -> Long.valueOf(33);
		data.setBusinessGroupRef(businessGroupRef);
		OrganisationRef organisationRef = () -> Long.valueOf(44);
		data.setOrganisationRef(organisationRef);
		CurriculumElementRef curriculumElementRef = () -> Long.valueOf(55);
		data.setCurriculumElementRef(curriculumElementRef);
		String userPropertyName = random();
		data.setUserPropertyName(userPropertyName);
		String userPropertyValue = random();
		data.setUserPropertyValue(userPropertyValue);
		
		ScoreAccountingTrigger scoreAccountingTrigger = sut.create(entry, subIdent, data);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(((ScoreAccountingTriggerImpl)scoreAccountingTrigger).getKey()).isNotNull();
		softly.assertThat(((ScoreAccountingTriggerImpl)scoreAccountingTrigger).getCreationDate()).isNotNull();
		softly.assertThat(scoreAccountingTrigger.getRepositoryEntry().getKey()).isEqualTo(entry.getKey());
		softly.assertThat(scoreAccountingTrigger.getSubIdent()).isEqualTo(subIdent);
		softly.assertThat(scoreAccountingTrigger.getIdentifier()).isEqualTo(identifier);
		softly.assertThat(scoreAccountingTrigger.getBusinessGroupRef().getKey()).isEqualTo(businessGroupRef.getKey());
		softly.assertThat(scoreAccountingTrigger.getOrganisationRef().getKey()).isEqualTo(organisationRef.getKey());
		softly.assertThat(scoreAccountingTrigger.getCurriculumElementRef().getKey()).isEqualTo(curriculumElementRef.getKey());
		softly.assertThat(scoreAccountingTrigger.getUserPropertyName()).isEqualTo(userPropertyName);
		softly.assertThat(scoreAccountingTrigger.getUserPropertyValue()).isEqualTo(userPropertyValue);
		softly.assertAll();
	}

	@Test
	public void shouldLoadByRepositoryEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entryOther = JunitTestHelper.deployBasicCourse(author);
		ScoreAccountingTriggerData data1 = new ScoreAccountingTriggerData();
		data1.setIdentifier(random());
		ScoreAccountingTrigger triggerRefs1 = sut.create(entry, random(), data1);
		ScoreAccountingTriggerData data2 = new ScoreAccountingTriggerData();
		data2.setIdentifier(random());
		ScoreAccountingTrigger triggerRefs2 = sut.create(entry, random(), data2);
		ScoreAccountingTriggerData dateOther = new ScoreAccountingTriggerData();
		dateOther.setIdentifier(random());
		ScoreAccountingTrigger triggerRefsOther = sut.create(entryOther, random(), dateOther);
		dbInstance.commitAndCloseSession();
		
		List<ScoreAccountingTrigger> scoreAccountingTrigger = sut.load(entry);
		
		assertThat(scoreAccountingTrigger)
			.containsExactlyInAnyOrder(triggerRefs1, triggerRefs2)
			.doesNotContain(triggerRefsOther);
	}
	
	@Test
	public void shouldLoadEntriesByBusinessGroup() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entryOther = JunitTestHelper.deployBasicCourse(author);
		BusinessGroupRef businessGroupRef = () -> Long.valueOf(50);
		BusinessGroupRef businessGroupRefOther = () -> Long.valueOf(51);
		ScoreAccountingTriggerData data1 = new ScoreAccountingTriggerData();
		data1.setIdentifier(random());
		data1.setBusinessGroupRef(businessGroupRef);
		sut.create(entry1, random(), data1);
		ScoreAccountingTriggerData data2 = new ScoreAccountingTriggerData();
		data2.setIdentifier(random());
		data2.setBusinessGroupRef(businessGroupRef);
		sut.create(entry2, random(), data2);
		ScoreAccountingTriggerData dataOther = new ScoreAccountingTriggerData();
		dataOther.setIdentifier(random());
		dataOther.setBusinessGroupRef(businessGroupRefOther);
		sut.create(entryOther, random(), dataOther);
		dbInstance.commitAndCloseSession();
		
		ScoreAccountingTriggerSearchParams searchParams = new ScoreAccountingTriggerSearchParams();
		searchParams.setBusinessGroupRef(businessGroupRef);
		List<RepositoryEntry> entries = sut.load(searchParams);
		
		assertThat(entries)
				.contains(entry1, entry2)
				.doesNotContain(entryOther);
	}

	@Test
	public void shouldLoadEntriesByOrganisation() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entryOther = JunitTestHelper.deployBasicCourse(author);
		OrganisationRef organisationRef = () -> Long.valueOf(44);
		OrganisationRef organisationRefOther = () -> Long.valueOf(45);
		ScoreAccountingTriggerData data1 = new ScoreAccountingTriggerData();
		data1.setIdentifier(random());
		data1.setOrganisationRef(organisationRef);
		sut.create(entry1, random(), data1);
		ScoreAccountingTriggerData data2 = new ScoreAccountingTriggerData();
		data2.setIdentifier(random());
		data2.setOrganisationRef(organisationRef);
		sut.create(entry2, random(), data2);
		ScoreAccountingTriggerData dataOther = new ScoreAccountingTriggerData();
		dataOther.setIdentifier(random());
		dataOther.setOrganisationRef(organisationRefOther);
		sut.create(entryOther, random(), dataOther);
		dbInstance.commitAndCloseSession();
		
		ScoreAccountingTriggerSearchParams searchParams = new ScoreAccountingTriggerSearchParams();
		searchParams.setOrganisationRef(organisationRef);
		List<RepositoryEntry> entries = sut.load(searchParams);
		
		assertThat(entries)
				.contains(entry1, entry2)
				.doesNotContain(entryOther);
	}

	@Test
	public void shouldLoadEntriesByCurriculumElement() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entryOther = JunitTestHelper.deployBasicCourse(author);
		CurriculumElementRef curriculumElementRef = () -> Long.valueOf(60);
		CurriculumElementRef curriculumElementRefOther = () -> Long.valueOf(61);
		ScoreAccountingTriggerData data1 = new ScoreAccountingTriggerData();
		data1.setIdentifier(random());
		data1.setCurriculumElementRef(curriculumElementRef);
		sut.create(entry1, random(), data1);
		ScoreAccountingTriggerData data2 = new ScoreAccountingTriggerData();
		data2.setIdentifier(random());
		data2.setCurriculumElementRef(curriculumElementRef);
		sut.create(entry2, random(), data2);
		ScoreAccountingTriggerData dataOther = new ScoreAccountingTriggerData();
		dataOther.setIdentifier(random());
		dataOther.setCurriculumElementRef(curriculumElementRefOther);
		sut.create(entryOther, random(), dataOther);
		dbInstance.commitAndCloseSession();
		
		ScoreAccountingTriggerSearchParams searchParams = new ScoreAccountingTriggerSearchParams();
		searchParams.setCurriculumElementRef(curriculumElementRef);
		List<RepositoryEntry> entries = sut.load(searchParams);
		
		assertThat(entries)
				.contains(entry1, entry2)
				.doesNotContain(entryOther);
	}
	
	@Test
	public void shouldLoadEntriesByUserPropertyName() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entryOther = JunitTestHelper.deployBasicCourse(author);
		String userPropertyName = random();
		String userPropertyNameOther = random();
		ScoreAccountingTriggerData data1 = new ScoreAccountingTriggerData();
		data1.setIdentifier(random());
		data1.setUserPropertyName(userPropertyName);
		sut.create(entry1, random(), data1);
		ScoreAccountingTriggerData data2 = new ScoreAccountingTriggerData();
		data2.setIdentifier(random());
		data2.setUserPropertyName(userPropertyName);
		sut.create(entry2, random(), data2);
		ScoreAccountingTriggerData dataOther = new ScoreAccountingTriggerData();
		dataOther.setIdentifier(random());
		dataOther.setUserPropertyName(userPropertyNameOther);
		sut.create(entryOther, random(), dataOther);
		dbInstance.commitAndCloseSession();
		
		ScoreAccountingTriggerSearchParams searchParams = new ScoreAccountingTriggerSearchParams();
		searchParams.setUserPropertyName(userPropertyName);
		List<RepositoryEntry> entries = sut.load(searchParams);
		
		assertThat(entries)
				.contains(entry1, entry2)
				.doesNotContain(entryOther);
	}
	
	@Test
	public void shouldLoadEntriesByUserPropertyValue() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry1 = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entry2 = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entryOther = JunitTestHelper.deployBasicCourse(author);
		String userPropertyValue = random();
		String userPropertyValueOther = random();
		ScoreAccountingTriggerData data1 = new ScoreAccountingTriggerData();
		data1.setIdentifier(random());
		data1.setUserPropertyValue(userPropertyValue);
		sut.create(entry1, random(), data1);
		ScoreAccountingTriggerData data2 = new ScoreAccountingTriggerData();
		data2.setIdentifier(random());
		data2.setUserPropertyValue(userPropertyValue);
		sut.create(entry2, random(), data2);
		ScoreAccountingTriggerData dataOther = new ScoreAccountingTriggerData();
		dataOther.setIdentifier(random());
		dataOther.setUserPropertyValue(userPropertyValueOther);
		sut.create(entryOther, random(), dataOther);
		dbInstance.commitAndCloseSession();
		
		ScoreAccountingTriggerSearchParams searchParams = new ScoreAccountingTriggerSearchParams();
		searchParams.setUserPropertyValue(userPropertyValue);
		List<RepositoryEntry> entries = sut.load(searchParams);
		
		assertThat(entries)
				.contains(entry1, entry2)
				.doesNotContain(entryOther);
	}

	@Test
	public void shouldDelete() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		ScoreAccountingTriggerData data1 = new ScoreAccountingTriggerData();
		data1.setIdentifier(random());
		ScoreAccountingTrigger triggerRefs1 = sut.create(entry, random(), data1);
		ScoreAccountingTriggerData data2 = new ScoreAccountingTriggerData();
		data2.setIdentifier(random());
		ScoreAccountingTrigger triggerRefs2 = sut.create(entry, random(), data2);
		ScoreAccountingTriggerData data3 = new ScoreAccountingTriggerData();
		data3.setIdentifier(random());
		ScoreAccountingTrigger triggerRefs3 = sut.create(entry, random(), data3);
		dbInstance.commitAndCloseSession();
		
		sut.delete(List.of(triggerRefs1, triggerRefs2));
		dbInstance.commitAndCloseSession();
		
		List<ScoreAccountingTrigger> scoreAccountingTrigger = sut.load(entry);
		assertThat(scoreAccountingTrigger)
			.containsExactlyInAnyOrder(triggerRefs3)
			.doesNotContain(triggerRefs1, triggerRefs2);
	}
	
	@Test
	public void shouldDeleteByRepositoryEntry() {
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndAdmin(random());
		RepositoryEntry entry = JunitTestHelper.deployBasicCourse(author);
		RepositoryEntry entryOther = JunitTestHelper.deployBasicCourse(author);
		ScoreAccountingTriggerData data1 = new ScoreAccountingTriggerData();
		data1.setIdentifier(random());
		sut.create(entry, random(), data1);
		ScoreAccountingTriggerData data2 = new ScoreAccountingTriggerData();
		data2.setIdentifier(random());
		sut.create(entry, random(), data2);
		ScoreAccountingTriggerData dataOther = new ScoreAccountingTriggerData();
		dataOther.setIdentifier(random());
		sut.create(entryOther, random(), dataOther);
		dbInstance.commitAndCloseSession();
		
		sut.delete(entry);
		dbInstance.commitAndCloseSession();
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.load(entry)).isEmpty();
		softly.assertThat(sut.load(entryOther)).hasSize(1);
		softly.assertAll();
	}

}
