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

import java.util.HashSet;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.quality.QualityContext;
import org.olat.modules.quality.QualityContextBuilder;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityService;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class DefaultQualityContextBuilder implements QualityContextBuilder {

	private QualityDataCollection dataCollection;
	private EvaluationFormParticipation evaluationFormParticipation;
	private QualityContextRole role;
	private String location;
	private RepositoryEntry audienceRepositoryEntry;
	private CurriculumElement audienceCurriculumElement;
	private final Set<QualityContext> contextsToDelete = new HashSet<>();
	private final Set<Curriculum> curriculums = new HashSet<>();
	private final Set<CurriculumElement> curriculumElements = new HashSet<>();
	private final Set<Organisation> organisations = new HashSet<>();
	private final Set<TaxonomyLevel> taxonomyLevels = new HashSet<>();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private QualityContextDAO contextDao;
	@Autowired
	private QualityContextToCurriculumDAO contextToCurriculumDao;
	@Autowired
	private QualityContextToCurriculumElementDAO contextToCurriculumElementDao;
	@Autowired
	private QualityContextToOrganisationDAO contextToOrganisationDao;
	@Autowired
	private QualityContextToTaxonomyLevelDAO contextToTaxonomyLevelDao;
	
	private DefaultQualityContextBuilder(QualityDataCollection dataCollection,
			EvaluationFormParticipation evaluationFormParticipation) {
		this.dataCollection = dataCollection;
		this.evaluationFormParticipation = evaluationFormParticipation;
		CoreSpringFactory.autowireObject(this);
	}
	
	static final DefaultQualityContextBuilder builder(QualityDataCollection dataCollection,
			EvaluationFormParticipation evaluationFormParticipation) {
		return new DefaultQualityContextBuilder(dataCollection, evaluationFormParticipation);
	}
	
	DefaultQualityContextBuilder withAudienceRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.audienceRepositoryEntry = repositoryEntry;
		return this;
	}
	
	DefaultQualityContextBuilder withAudiencCurriculumElement(CurriculumElement curriculumElement) {
		this.audienceCurriculumElement = curriculumElement;
		return this;
	}

	@Override
	public QualityContextBuilder withRole(QualityContextRole role) {
		this.role = role;
		return this;
	}

	@Override
	public QualityContextBuilder withLocation(String location) {
		this.location = location;
		return this;
	}
	@Override
	public QualityContextBuilder addToDelete(QualityContext context) {
		if (context != null) {
			contextsToDelete.add(context);
		}
		return this;
	}
	
	public DefaultQualityContextBuilder addCurriculum(Curriculum curriculum) {
		if (curriculum != null) {
			curriculums.add(curriculum);
		}
		return this;
	}
	
	@Override
	public DefaultQualityContextBuilder addCurriculumElement(CurriculumElement curriculumElement) {
		if (curriculumElement != null) {
			curriculumElements.add(curriculumElement);
		}
		return this;
	}
	
	@Override
	public DefaultQualityContextBuilder addOrganisation(Organisation organisation) {
		if (organisation != null) {
			organisations.add(organisation);
		}
		return this;
	}
	
	@Override
	public DefaultQualityContextBuilder addTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		if (taxonomyLevel != null) {
			taxonomyLevels.add(taxonomyLevel);
		}
		return this;
	}
	
	@Override
	public QualityContext build() {
		for (QualityContext contextToDelete: contextsToDelete) {
			qualityService.deleteContext(contextToDelete);
		}
		QualityContext context = contextDao.createContext(dataCollection, evaluationFormParticipation, role, location,
				audienceRepositoryEntry, audienceCurriculumElement);
		for (Curriculum curriculum: curriculums) {
			contextToCurriculumDao.createRelation(context, curriculum);
		}
		for (CurriculumElement curriculumElement: curriculumElements) {
			contextToCurriculumElementDao.createRelation(context, curriculumElement);
		}
		for (Organisation organisation: organisations) {
			contextToOrganisationDao.createRelation(context, organisation);
		}
		for (TaxonomyLevel taxonomyLevel: taxonomyLevels) {
			contextToTaxonomyLevelDao.createRelation(context, taxonomyLevel);
		}
		dbInstance.getCurrentEntityManager().refresh(context);
		return context;
	}

}
