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
package org.olat.modules.quality.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextToOrganisation;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.10.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AudiencelessQualityContextBuilderTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityTestHelper qualityTestHelper;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private OrganisationService organisationService;

	@Before
	public void cleanUp() {
		qualityTestHelper.deleteAll();
	}

	@Test
	public void shouldInitWithAllRelations() {
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		dbInstance.commitAndCloseSession();

		QualityContext context = AudiencelessQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation).build();

		assertThat(context.getDataCollection()).isEqualTo(dataCollection);
		assertThat(context.getEvaluationFormParticipation()).isEqualTo(evaluationFormParticipation);
		assertThat(context.getAudienceCurriculumElement()).isNull();
		assertThat(context.getAudienceRepositoryEntry()).isNull();
	}
	
	@Test
	public void shouldAddAllUserOrganisations() {
		Identity executor = JunitTestHelper.createAndPersistIdentityAsRndAuthor("");
		QualityDataCollection dataCollection = qualityTestHelper.createDataCollection();
		List<EvaluationFormParticipation> participations = qualityService.addParticipations(dataCollection,
				Arrays.asList(executor));
		EvaluationFormParticipation evaluationFormParticipation = participations.get(0);
		
		Organisation executorDefaultOrganisation = organisationService.getDefaultOrganisation();
		Organisation executorOrganisation1 = qualityTestHelper.createOrganisation();
		organisationService.addMember(executorOrganisation1, executor, OrganisationRoles.user);
		Organisation executorOrganisation2 = qualityTestHelper.createOrganisation();
		organisationService.addMember(executorOrganisation2, executor, OrganisationRoles.user);
		Organisation executorOrganisationManager = qualityTestHelper.createOrganisation();
		organisationService.addMember(executorOrganisationManager, executor, OrganisationRoles.administrator);
		dbInstance.commitAndCloseSession();
		
		QualityContext context = AudiencelessQualityContextBuilder
				.builder(dataCollection, evaluationFormParticipation).build();
		
		List<Organisation> organisations = context.getContextToOrganisation().stream()
				.map(QualityContextToOrganisation::getOrganisation).collect(Collectors.toList());
		assertThat(organisations)
				.containsExactlyInAnyOrder(executorDefaultOrganisation, executorOrganisation1, executorOrganisation2)
				.doesNotContain(executorOrganisationManager);
	}

}
