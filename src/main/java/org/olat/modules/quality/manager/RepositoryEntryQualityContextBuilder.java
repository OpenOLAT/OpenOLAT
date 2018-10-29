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

import static java.util.Collections.singletonList;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextBuilder;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryQualityContextBuilder extends ForwardingQualityContextBuilder implements QualityContextBuilder {
	
	@Autowired
	private QualityContextDAO qualityContextDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	
	static final RepositoryEntryQualityContextBuilder builder(QualityDataCollection dataCollection,
			EvaluationFormParticipation evaluationFormParticipation, RepositoryEntry repositoryEntry,
			GroupRoles role) {
		return new RepositoryEntryQualityContextBuilder(dataCollection, evaluationFormParticipation, repositoryEntry, role);
	}

	private RepositoryEntryQualityContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation evaluationFormParticipation, RepositoryEntry repositoryEntry,
			GroupRoles role) {
		super(dataCollection, evaluationFormParticipation);
		CoreSpringFactory.autowireObject(this);
		initBuilder(evaluationFormParticipation, repositoryEntry, role);
	}

	private void initBuilder(EvaluationFormParticipation evaluationFormParticipation, RepositoryEntry repositoryEntry,
			GroupRoles role) {
		QualityContextRole contextRole = QualityContextRole.valueOf(role.name());
		builder.withRole(contextRole);
		builder.withLocation(repositoryEntry.getLocation());
		builder.withAudienceRepositoryEntry(repositoryEntry);

		List<QualityContext> contextToDelete = qualityContextDao
				.loadByAudienceRepositoryEntry(evaluationFormParticipation, repositoryEntry, contextRole);
		contextToDelete.forEach(c -> builder.addToDelete(c));
		
		Identity executor = evaluationFormParticipation.getExecutor();
		initExecutorOrganisation(executor, repositoryEntry, role);
			
		if (executor != null) {
			initCurriculumContext(executor, repositoryEntry, role);
			if (GroupRoles.coach.equals(role)) {
				initCurriculumContextForCoach(repositoryEntry);
			}
		}
		List<TaxonomyLevel> taxonomyLevels = repositoryService.getTaxonomy(repositoryEntry);
		for (TaxonomyLevel taxonomyLevel: taxonomyLevels) {
			builder.addTaxonomyLevel(taxonomyLevel);
		}
	}

	private void initExecutorOrganisation(Identity executor, RepositoryEntry repositoryEntry, GroupRoles role) {
		List<Organisation> userOrganisations = organisationService.getOrganisations(executor, OrganisationRoles.user);
		boolean organisationDone = initOrganisationFromCurriculum(executor, repositoryEntry, role, userOrganisations);
		if (!organisationDone) {
			organisationDone = initOrganisationFromRepositoreyEntry(repositoryEntry, userOrganisations);
		}
		if (!organisationDone) {
			initOrganisationFromUser(userOrganisations);
		}
	}

	private boolean initOrganisationFromCurriculum(Identity executor, RepositoryEntry repositoryEntry, GroupRoles role,
			List<Organisation> userOrganisations) {
		boolean organisationDone = false;
		CurriculumRoles curriculumRole = CurriculumRoles.valueOf(role.name());
		List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(repositoryEntry, executor,
				singletonList(curriculumRole));
		for (CurriculumElement curriculumElement : curriculumElements) {
			Organisation curriculumOrganisation = curriculumElement.getCurriculum().getOrganisation();
			if (userOrganisations.contains(curriculumOrganisation)) {
				builder.addExecutorOrganisation(curriculumOrganisation);
				organisationDone = true;
			}
		}
		return organisationDone;
	}

	private boolean initOrganisationFromRepositoreyEntry(RepositoryEntry repositoryEntry,
			List<Organisation> userOrganisations) {
		boolean organisationDone = false;
		List<OrganisationRef> repositoryOrganisations = repositoryService.getOrganisationReferences(repositoryEntry);
		for (OrganisationRef repositoryOrganisation : repositoryOrganisations) {
			for (Organisation userOrganisation: userOrganisations) {
				if (userOrganisation.getKey().equals(repositoryOrganisation.getKey())) {
					builder.addExecutorOrganisation(userOrganisation);
					organisationDone = true;
				}
			}
		}
		return organisationDone;
	}

	private void initOrganisationFromUser(List<Organisation> userOrganisations) {
		for (Organisation organisation : userOrganisations) {
			builder.addExecutorOrganisation(organisation);
		}
	}

	private void initCurriculumContext(Identity identity, RepositoryEntry repositoryEntry, GroupRoles role) {
		CurriculumRoles curriculumRole = CurriculumRoles.valueOf(role.name());
		List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(repositoryEntry, identity,
				singletonList(curriculumRole));
		for (CurriculumElement curriculumElement : curriculumElements) {
			builder.addCurriculumElement(curriculumElement);
			Curriculum curriculum = curriculumElement.getCurriculum();
			builder.addCurriculum(curriculum);
		}
	}

	/**
	 * A coach gains not only the the context informations of his curriculum
	 * elements, but also the distinct informations of all participants of the
	 * course. This is because the membership of a coach in curriculum element is
	 * not explicit stored but implicit derived form the participant memberships.
	 *
	 */
	private void initCurriculumContextForCoach(RepositoryEntry repositoryEntry) {
		List<Identity> participants = repositoryService.getMembers(repositoryEntry,
				RepositoryEntryRelationType.curriculums, GroupRoles.participant.name());
		for (Identity participant : participants) {
			initCurriculumContext(participant, repositoryEntry, GroupRoles.participant);
		}
	}

}
