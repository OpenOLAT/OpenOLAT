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
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextBuilder;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryToTaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryQualityContextBuilder implements QualityContextBuilder {
	
	private final DefaultQualityContextBuilder builder;
	
	@Autowired
	private QualityContextDAO qualityContextDao;
	@Autowired
	private CurriculumService curriculumService;
	
	static final RepositoryQualityContextBuilder builder(QualityDataCollection dataCollection,
			EvaluationFormParticipation evaluationFormParticipation, RepositoryEntry repositoryEntry,
			List<GroupRoles> roles) {
		return new RepositoryQualityContextBuilder(dataCollection, evaluationFormParticipation, repositoryEntry, roles);
	}

	private RepositoryQualityContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation evaluationFormParticipation, RepositoryEntry repositoryEntry,
			List<GroupRoles> roles) {
		this.builder = DefaultQualityContextBuilder.builder(dataCollection, evaluationFormParticipation);
		CoreSpringFactory.autowireObject(this);
		initBuilder(evaluationFormParticipation, repositoryEntry, roles);
	}

	private void initBuilder(EvaluationFormParticipation evaluationFormParticipation, RepositoryEntry repositoryEntry,
			List<GroupRoles> roles) {
		builder.withRepositoryEntry(repositoryEntry);

		List<QualityContext> contextToDelete = qualityContextDao
				.loadByParticipationAndRepositoryEntry(evaluationFormParticipation, repositoryEntry);
		contextToDelete.forEach(c -> builder.addToDelete(c));
		
		Identity executor = evaluationFormParticipation.getExecutor();
		if (executor != null) {
			List<CurriculumRoles> curriculumRoles = roles.stream().map(GroupRoles::name).map(CurriculumRoles::valueOf)
					.collect(Collectors.toList());
			List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElements(repositoryEntry, executor, curriculumRoles);
			for (CurriculumElement curriculumElement: curriculumElements) {
				builder.addCurriculumElement(curriculumElement);
				Curriculum curriculum = curriculumElement.getCurriculum();
				builder.addCurriculum(curriculum);
				Organisation organisation = curriculum.getOrganisation();
				builder.addOrganisation(organisation);
			}
		}
		Set<RepositoryEntryToTaxonomyLevel> taxonomyLevels = repositoryEntry.getTaxonomyLevels();
		for (RepositoryEntryToTaxonomyLevel entryToLevel: taxonomyLevels) {
			builder.addTaxonomyLevel(entryToLevel.getTaxonomyLevel());
		}
	}

	@Override
	public QualityContextBuilder addToDelete(QualityContext context) {
		builder.addToDelete(context);
		return this;
	}

	@Override
	public QualityContextBuilder addCurriculum(Curriculum curriculum) {
		builder.addCurriculum(curriculum);
		return this;
	}

	@Override
	public QualityContextBuilder addCurriculumElement(CurriculumElement curriculumElement) {
		builder.addCurriculumElement(curriculumElement);
		return this;
	}

	@Override
	public QualityContextBuilder addOrganisation(Organisation organisation) {
		builder.addOrganisation(organisation);
		return this;
	}

	@Override
	public QualityContextBuilder addTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		builder.addTaxonomyLevel(taxonomyLevel);
		return this;
	}

	@Override
	public QualityContext build() {
		return builder.build();
	}

}
