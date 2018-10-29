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

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class CurriculumElementQualityContextBuilder extends ForwardingQualityContextBuilder {

	@Autowired
	private QualityContextDAO qualityContextDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	
	static final CurriculumElementQualityContextBuilder builder(QualityDataCollection dataCollection,
			EvaluationFormParticipation evaluationFormParticipation, CurriculumElement curriculumElement,
			CurriculumRoles role) {
		return new CurriculumElementQualityContextBuilder(dataCollection, evaluationFormParticipation, curriculumElement, role);
	}

	private CurriculumElementQualityContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation evaluationFormParticipation, CurriculumElement curriculumElement,
			CurriculumRoles role) {
		super(dataCollection, evaluationFormParticipation);
		CoreSpringFactory.autowireObject(this);
		initBuilder(evaluationFormParticipation, curriculumElement, role);
	}

	private void initBuilder(EvaluationFormParticipation evaluationFormParticipation, CurriculumElement curriculumElement,
			CurriculumRoles role) {
		curriculumElement = curriculumService.getCurriculumElement(curriculumElement);
		
		QualityContextRole contextRole = QualityContextRole.valueOf(role.name());
		builder.withRole(contextRole);
		builder.withAudiencCurriculumElement(curriculumElement);	

		List<QualityContext> contextToDelete = qualityContextDao
				.loadByAudienceCurriculumElement(evaluationFormParticipation, curriculumElement, contextRole);
		contextToDelete.forEach(c -> builder.addToDelete(c));
		builder.addCurriculumElement(curriculumElement);
		Curriculum curriculum = curriculumElement.getCurriculum();
		builder.addCurriculum(curriculum);
		
		Organisation curriculumOrganisation = curriculum.getOrganisation();
		List<Organisation> organisations = organisationService
				.getOrganisations(evaluationFormParticipation.getExecutor(), OrganisationRoles.user);
		if (organisations.contains(curriculumOrganisation)) {
			builder.addExecutorOrganisation(curriculumOrganisation);
		} else {
			for (Organisation organisation : organisations) {
				builder.addExecutorOrganisation(organisation);
			}
		}
		
		List<TaxonomyLevel> taxonomyLevels = curriculumService.getTaxonomy(curriculumElement);
		for (TaxonomyLevel taxonomyLevel: taxonomyLevels) {
			builder.addTaxonomyLevel(taxonomyLevel);
		}
	}
	
}
