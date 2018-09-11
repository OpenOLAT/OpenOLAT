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
package org.olat.modules.quality.analysis.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.basesecurity.OrganisationService;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.EvaluationFormView;
import org.olat.modules.quality.analysis.EvaluationFormViewSearchParams;
import org.olat.modules.quality.analysis.GroupBy;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 03.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QualityAnalysisServiceImpl implements QualityAnalysisService {

	@Autowired
	private AnalysisFilterDAO filterDao;
	@Autowired
	private StatisticsCalculator statisticsCalculator;
	@Autowired
	private EvaluationFormDAO evaluationFromDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;

	@Override
	public List<EvaluationFormView> loadEvaluationForms(EvaluationFormViewSearchParams searchParams) {
		return evaluationFromDao.load(searchParams);
	}

	@Override
	public List<Organisation> loadFilterOrganisations(AnalysisSearchParameter searchParams) {
		if (searchParams == null) {
			return new ArrayList<>(0);
		}
		
		List<Organisation> organisations = organisationService.getOrganisations();
		List<String> pathes = filterDao.loadOrganisationPathes(searchParams);
		organisations.removeIf(e -> isUnused(e.getMaterializedPathKeys(), pathes));	
		return organisations;
	}

	@Override
	public List<Curriculum> loadFilterCurriculums(AnalysisSearchParameter searchParams) {
		return filterDao.loadCurriculums(searchParams);
	}

	@Override
	public List<CurriculumElement> loadFilterCurriculumElements(AnalysisSearchParameter searchParams) {
		if (searchParams == null || searchParams.getCurriculumRefs() == null
				|| searchParams.getCurriculumRefs().isEmpty()) {
			return new ArrayList<>(0);
		}

		List<CurriculumElement> elementsOfCurriculums = curriculumService
				.getCurriculumElementsByCurriculums(searchParams.getCurriculumRefs());
		List<String> pathes = filterDao.loadCurriculumElementPathes(searchParams);
		elementsOfCurriculums.removeIf(e -> isUnusedChild(e.getMaterializedPathKeys(), pathes));
		return elementsOfCurriculums;
	}

	private boolean isUnusedChild(String pathToCheck, List<String> pathsOfContexts) {
		for (String path : pathsOfContexts) {
			if (path.contains(pathToCheck)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isUnused(String pathToCheck, List<String> pathsOfContexts) {
		for (String path : pathsOfContexts) {
			if (path.equals(pathToCheck)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Long loadFilterDataCollectionCount(AnalysisSearchParameter searchParams) {
		return filterDao.loadFilterDataCollectionCount(searchParams);
	}

	@Override
	public SessionFilter createSessionFilter(AnalysisSearchParameter searchParams) {
		return new AnalysisSessionFilter(searchParams);
	}

	@Override
	public GroupedStatistics calculateStatistics(AnalysisSearchParameter searchParams,
			Collection<String> responseIdentifiers, Collection<Rubric> rubrics, GroupBy groupBy) {
		List<GroupedStatistic> statisticsList = filterDao.getAvgByResponseIdentifiers(searchParams, responseIdentifiers,
				groupBy);
		GroupedStatistics statistics = new GroupedStatistics(statisticsList);
		statistics = statisticsCalculator.getScaledStatistics(statistics, rubrics);
		return statistics;
	}
}
