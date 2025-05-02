/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.quality;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.miniRandom;
import static org.olat.test.JunitTestHelper.random;

import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.forms.EvaluationFormEmailExecutor;
import org.olat.modules.quality.manager.QualityTestHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Apr 29, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class QualityServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private OrganisationService organisationService;
	
	@Autowired
	private QualityService sut;

	@Test
	public void shouldAddParticipationsEmail_onceOnly() {
		RepositoryEntry formEntry = qualityTestHelper.createFormEntry();
		QualityDataCollection dataCollection = sut.createDataCollection(List.of(organisationService.getDefaultOrganisation()), formEntry);
		dbInstance.commitAndCloseSession();
		
		String emailAddress1 = random();
		String emailAddress2 = random();
		Collection<EvaluationFormEmailExecutor> emailExecutors = List.of(
				new EvaluationFormEmailExecutor(emailAddress1, random(), random()),
				new EvaluationFormEmailExecutor(emailAddress1, random(), random()),
				new EvaluationFormEmailExecutor(emailAddress2, random(), random())
				);
		sut.addParticipationsEmail(dataCollection, emailExecutors);
		dbInstance.commitAndCloseSession();
		
		List<QualityParticipation> participations = sut.loadParticipations(dataCollection);
		assertThat(participations)
				.hasSize(2)
				.extracting(QualityParticipation::getEmail)
				.containsExactlyInAnyOrder(emailAddress1, emailAddress2);
	}
	
	@Test
	public void shouldAddParticipationsEmail_updateName() {
		RepositoryEntry formEntry = qualityTestHelper.createFormEntry();
		QualityDataCollection dataCollection = sut.createDataCollection(List.of(organisationService.getDefaultOrganisation()), formEntry);
		dbInstance.commitAndCloseSession();
		
		String emailAddress = random();
		EvaluationFormEmailExecutor emailExecutor = new EvaluationFormEmailExecutor(emailAddress, random(), random());
		sut.addParticipationsEmail(dataCollection, List.of(emailExecutor));
		dbInstance.commitAndCloseSession();
		
		List<QualityParticipation> participations = sut.loadParticipations(dataCollection);
		assertThat(participations).hasSize(1);
		QualityParticipation participation = participations.get(0);
		assertThat(participation.getFirstname()).isEqualTo(emailExecutor.firstName());
		assertThat(participation.getLastname()).isEqualTo(emailExecutor.lastName());
		
		emailExecutor = new EvaluationFormEmailExecutor(emailAddress, miniRandom(), miniRandom());
		sut.addParticipationsEmail(dataCollection, List.of(emailExecutor));
		dbInstance.commitAndCloseSession();
		
		participations = sut.loadParticipations(dataCollection);
		assertThat(participations).hasSize(1);
		participation = participations.get(0);
		assertThat(participation.getFirstname()).isEqualTo(emailExecutor.firstName());
		assertThat(participation.getLastname()).isEqualTo(emailExecutor.lastName());
	}
	
	@Test
	public void shouldAddParticipationsEmail_setRole() {
		RepositoryEntry formEntry = qualityTestHelper.createFormEntry();
		QualityDataCollection dataCollection = sut.createDataCollection(List.of(organisationService.getDefaultOrganisation()), formEntry);
		dbInstance.commitAndCloseSession();
		
		EvaluationFormEmailExecutor emailExecutor = new EvaluationFormEmailExecutor(random(), random(), random());
		sut.addParticipationsEmail(dataCollection, List.of(emailExecutor));
		dbInstance.commitAndCloseSession();
		
		List<QualityParticipation> participations = sut.loadParticipations(dataCollection);
		assertThat(participations).hasSize(1);
		QualityParticipation participation = participations.get(0);
		assertThat(participation.getRole()).isEqualTo(QualityContextRole.email);
	}

}
