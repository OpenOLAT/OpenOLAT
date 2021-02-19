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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityShort;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.RubricRating;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.forms.model.xml.Slider;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.analysis.AnalysisPresentationRef;
import org.olat.modules.quality.analysis.AnalysisPresentationSearchParameter;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AnlaysisFigures;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.EvaluationFormView;
import org.olat.modules.quality.analysis.EvaluationFormViewSearchParams;
import org.olat.modules.quality.analysis.GroupedStatistic;
import org.olat.modules.quality.analysis.GroupedStatistics;
import org.olat.modules.quality.analysis.HeatMapStatistic;
import org.olat.modules.quality.analysis.MultiGroupBy;
import org.olat.modules.quality.analysis.MultiKey;
import org.olat.modules.quality.analysis.MultiTrendSeries;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.analysis.RawGroupedStatistic;
import org.olat.modules.quality.analysis.TemporalGroupBy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
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
	private EvaluationFormDAO evaluationFromDao;
	@Autowired
	private AnalysisPresentationDAO presentationDAO;
	@Autowired
	private AnalysisFilterDAO filterDao;
	@Autowired
	private StatisticsCalculator statisticsCalculator;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private BaseSecurity securityManager;

	@Override
	public List<EvaluationFormView> loadEvaluationForms(EvaluationFormViewSearchParams searchParams) {
		return evaluationFromDao.load(searchParams);
	}

	@Override
	public AnalysisPresentation createPresentation(RepositoryEntry formEntry,
			List<? extends OrganisationRef> dataCollectionOrganisationRefs) {
		return presentationDAO.create(formEntry, dataCollectionOrganisationRefs);
	}

	@Override
	public AnalysisPresentation clonePresentation(AnalysisPresentation presentation) {
		AnalysisPresentation clone = presentationDAO.create(presentation.getFormEntry(),
				presentation.getSearchParams().getDataCollectionOrganisationRefs());
		clone.setAnalysisSegment(presentation.getAnalysisSegment());
		clone.setHeatMapGrouping(presentation.getHeatMapGrouping());
		clone.setHeatMapInsufficientOnly(presentation.getHeatMapInsufficientOnly());
		clone.setName(presentation.getName());
		clone.setSearchParams(presentation.getSearchParams().clone());
		return clone;
	}

	@Override
	public AnalysisPresentation savePresentation(AnalysisPresentation presentation) {
		return presentationDAO.save(presentation);
	}

	@Override
	public List<AnalysisPresentation> loadPresentations(AnalysisPresentationSearchParameter searchParams) {
		return presentationDAO.load(searchParams);
	}

	@Override
	public AnalysisPresentation loadPresentationByKey(AnalysisPresentationRef presentationRef) {
		return presentationDAO.loadByKey(presentationRef);
	}

	@Override
	public void deletePresentation(AnalysisPresentationRef presentationRef) {
		presentationDAO.delete(presentationRef);
	}

	@Override
	public AvailableAttributes getAvailableAttributes(AnalysisSearchParameter searchParams) {
		return filterDao.getAvailableAttributes(searchParams);
	}

	@Override
	public AnlaysisFigures loadFigures(AnalysisSearchParameter searchParams) {
		return filterDao.loadAnalyticFigures(searchParams);
	}

	@Override
	public List<Organisation> loadTopicOrganisations(AnalysisSearchParameter searchParams, boolean withParents) {
		List<Organisation> organisations = organisationService.getOrganisations();
		List<String> pathes = filterDao.loadTopicOrganisationPaths(searchParams);
		if (withParents) {
			organisations.removeIf(e -> isUnusedChild(e.getMaterializedPathKeys(), pathes));
		} else {
			organisations.removeIf(e -> isUnused(e.getMaterializedPathKeys(), pathes));
		}
		return organisations;
	}

	@Override
	public List<Curriculum> loadTopicCurriculums(AnalysisSearchParameter searchParams) {
		List<Long> keys = filterDao.loadTopicCurriculumKeys(searchParams);
		List<CurriculumRef> refs = keys.stream().map(CurriculumRefImpl::new).collect(Collectors.toList());
		return curriculumService.getCurriculums(refs);
	}

	@Override
	public List<CurriculumElement> loadTopicCurriculumElements(AnalysisSearchParameter searchParams) {
		List<Long> keys = filterDao.loadTopicCurriculumElementKeys(searchParams);
		List<CurriculumElementRef> refs = keys.stream().map(CurriculumElementRefImpl::new).collect(Collectors.toList());
		return curriculumService.getCurriculumElements(refs);
	}

	@Override
	public List<IdentityShort> loadTopicIdentity(AnalysisSearchParameter searchParams) {
		List<Long> keys = filterDao.loadTopicIdentityKeys(searchParams);
		return securityManager.loadIdentityShortByKeys(keys);
	}

	@Override
	public List<RepositoryEntry> loadTopicRepositoryEntries(AnalysisSearchParameter searchParams) {
		List<Long> keys = filterDao.loadTopicRepositoryKeys(searchParams);
		return repositoryManager.lookupRepositoryEntries(keys);
	}

	@Override
	public List<String> loadContextLocations(AnalysisSearchParameter searchParams) {
		return filterDao.loadContextLocations(searchParams);
	}

	@Override
	public List<Organisation> loadContextExecutorOrganisations(AnalysisSearchParameter searchParams, boolean withParents) {
		List<Organisation> organisations = organisationService.getOrganisations();
		List<String> pathes = filterDao.loadContextOrganisationPathes(searchParams);
		if (withParents) {
			organisations.removeIf(e -> isUnusedChild(e.getMaterializedPathKeys(), pathes));
		} else {
			organisations.removeIf(e -> isUnused(e.getMaterializedPathKeys(), pathes));
		}	
		return organisations;
	}

	@Override
	public List<Curriculum> loadContextCurriculums(AnalysisSearchParameter searchParams) {
		return filterDao.loadContextCurriculums(searchParams);
	}

	@Override
	public List<CurriculumElement> loadContextCurriculumElements(AnalysisSearchParameter searchParams, boolean withParents) {
		List<CurriculumRefImpl> curriculumRefs = filterDao.loadContextCurriculumElementsCurriculumKey(searchParams)
				.stream().map(CurriculumRefImpl::new).collect(toList());
		// Reload the curriculum elements to fetch the parents
		List<CurriculumElement> elementsOfCurriculums = curriculumService
				.getCurriculumElementsByCurriculums(curriculumRefs);
		List<String> pathes = filterDao.loadContextCurriculumElementPathes(searchParams);

		if (withParents) {
			elementsOfCurriculums.removeIf(e -> isUnusedChild(e.getMaterializedPathKeys(), pathes));
		} else {
			elementsOfCurriculums.removeIf(e -> isUnused(e.getMaterializedPathKeys(), pathes));
		}
		return elementsOfCurriculums;
	}
	
	@Override
	public List<CurriculumElementType> loadContextCurriculumElementTypes(AnalysisSearchParameter searchParams) {
		return filterDao.loadContextCurriculumElementsTypes(searchParams);
	}

	@Override
	public List<Organisation> loadContextCurriculumOrganisations(AnalysisSearchParameter searchParams, boolean withParents) {
		List<Organisation> organisations = organisationService.getOrganisations();
		List<String> pathes = filterDao.loadContextCurriculumOrganisationPathes(searchParams);
		if (withParents) {
			organisations.removeIf(e -> isUnusedChild(e.getMaterializedPathKeys(), pathes));
		} else {
			organisations.removeIf(e -> isUnused(e.getMaterializedPathKeys(), pathes));
		}	
		return organisations;
	}

	@Override
	public List<TaxonomyLevel> loadContextTaxonomyLevels(AnalysisSearchParameter searchParams, boolean withParents) {
		List<TaxonomyLevel> levels = taxonomyService.getTaxonomyLevelsByRefs(null);
		List<String> pathes = filterDao.loadContextTaxonomyLevelPathes(searchParams);
		if (withParents) {
			levels.removeIf(e -> isUnusedChild(e.getMaterializedPathKeys(), pathes));
		} else {
			levels.removeIf(e -> isUnused(e.getMaterializedPathKeys(), pathes));
		}	
		return levels;
	}
	
	@Override
	public List<QualityContextRole> loadContextRoles(AnalysisSearchParameter searchParams) {
		return filterDao.loadContextRoles(searchParams);
	}

	@Override
	public List<QualityDataCollection> loadDataCollections(AnalysisSearchParameter searchParams) {
		return filterDao.loadDataCollection(searchParams);
	}

	@Override
	public Integer loadMaxSeriesIndex(AnalysisSearchParameter searchParams) {
		return filterDao.loadMaxSeriesIndex(searchParams);
	}

	private boolean isUnusedChild(String pathToCheck, List<String> pathes) {
		for (String path : pathes) {
			if (path.contains(pathToCheck)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isUnused(String pathToCheck, List<String> pathes) {
		for (String path : pathes) {
			if (path.equals(pathToCheck)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public SessionFilter createSessionFilter(AnalysisSearchParameter searchParams) {
		return new AnalysisSessionFilter(searchParams);
	}

	@Override
	public GroupedStatistics<GroupedStatistic> calculateStatistics(AnalysisSearchParameter searchParams,
			Collection<String> responseIdentifiers, Collection<Rubric> rubrics, MultiGroupBy multiGroupBy) {
		List<RawGroupedStatistic> statisticsList = filterDao.loadGroupedStatistic(searchParams,
				responseIdentifiers, true, multiGroupBy, null);
		GroupedStatistics<RawGroupedStatistic> rawStatistics = new GroupedStatistics<>(statisticsList);
		GroupedStatistics<GroupedStatistic> statistics = statisticsCalculator.getGroupedStatistics(rawStatistics, rubrics);
		return statistics;
	}

	@Override
	public MultiTrendSeries<String> calculateIdentifierTrends(AnalysisSearchParameter searchParams,
			Collection<String> responseIdentifiers, Collection<Rubric> rubrics, TemporalGroupBy temporalGroupBy) {
		if (temporalGroupBy == null) return new MultiTrendSeries<>();
		
		List<RawGroupedStatistic> statisticsList = filterDao.loadGroupedStatistic(searchParams,
				responseIdentifiers, true, MultiGroupBy.noGroupBy(), temporalGroupBy);
		GroupedStatistics<RawGroupedStatistic> rawStatistics = new GroupedStatistics<>(statisticsList);
		GroupedStatistics<GroupedStatistic> statistics = statisticsCalculator.getGroupedStatistics(rawStatistics, rubrics);
		return statisticsCalculator.getTrendsByIdentifiers(statistics, temporalGroupBy);
	}

	@Override
	public MultiTrendSeries<MultiKey> calculateTrends(AnalysisSearchParameter searchParams,
			Set<Rubric> rubrics, MultiGroupBy groupBy, TemporalGroupBy temporalGroupBy) {
		if (groupBy == null || temporalGroupBy == null || rubrics == null || rubrics.size() == 0) return new MultiTrendSeries<>();
		
		List<String> identifiers = rubrics.stream().map(Rubric::getSliders).flatMap(s -> s.stream()).map(Slider::getId).collect(toList());
		List<RawGroupedStatistic> statisticsList;
		if (hasWeights(rubrics)) {
			statisticsList = filterDao.loadGroupedStatistic(searchParams, identifiers, true, groupBy, temporalGroupBy);
			statisticsList = statisticsCalculator.reduceIdentifier(statisticsList, rubrics);
		} else {
			statisticsList = filterDao.loadGroupedStatistic(searchParams, identifiers, false, groupBy, temporalGroupBy);
		}
		GroupedStatistics<RawGroupedStatistic> rawStatistics = new GroupedStatistics<>(statisticsList);
		GroupedStatistics<GroupedStatistic> statistics = statisticsCalculator.getGroupedStatistics(rawStatistics, rubrics);
		return statisticsCalculator.getTrendsByMultiKey(statistics, temporalGroupBy);
	}

	private boolean hasWeights(Set<Rubric> rubrics) {
		return rubrics.stream()
				.map(Rubric::getSliders)
				.flatMap(s -> s.stream())
				.filter(s -> s.getWeight().intValue() != 1)
				.findAny()
				.isPresent();
	}

	@Override
	public HeatMapStatistic calculateRubricsTotal(List<? extends GroupedStatistic> statistics,
			Collection<Rubric> rubric) {
		return statisticsCalculator.calculateRubricsTotal(statistics, rubric);
	}

	@Override
	public HeatMapStatistic calculateSliderTotal(List<? extends HeatMapStatistic> statistics, Rubric rubrics) {
		return statisticsCalculator.calculateSliderTotal(statistics, rubrics);
	}

	@Override
	public boolean isInsufficient(Rubric rubric, Double avg) {
		RubricRating rating = evaluationFormManager.getRubricRating(rubric, avg);
		if (RubricRating.INSUFFICIENT.equals(rating)) {
			return true;
		}
		return false;
	}

}
