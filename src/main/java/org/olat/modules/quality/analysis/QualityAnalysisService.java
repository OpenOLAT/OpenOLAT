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
package org.olat.modules.quality.analysis;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.IdentityShort;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Rubric;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 03.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface QualityAnalysisService {

	public List<EvaluationFormView> loadEvaluationForms(EvaluationFormViewSearchParams searchParams);
	
	public AnalysisPresentation createPresentation(RepositoryEntry formEntry);
	
	public AnalysisPresentation clonePresentation(AnalysisPresentation presentation);

	public AnalysisPresentation savePresentation(AnalysisPresentation presentation);

	public List<AnalysisPresentation> loadPresentations(AnalysisPresentationSearchParameter searchParams);

	public AnalysisPresentation loadPresentationByKey(AnalysisPresentationRef presentationRef);

	public void deletePresentation(AnalysisPresentationRef presentationRef);

	public AvailableAttributes getAvailableAttributes(AnalysisSearchParameter searchParams);

	public AnlaysisFigures loadFigures(AnalysisSearchParameter searchParams);
	
	public List<Organisation> loadTopicOrganisations(AnalysisSearchParameter searchParams, boolean withParents);

	public List<Curriculum> loadTopicCurriculums(AnalysisSearchParameter searchParams);

	public List<CurriculumElement> loadTopicCurriculumElements(AnalysisSearchParameter searchParams);

	public List<IdentityShort> loadTopicIdentity(AnalysisSearchParameter searchParams);
	
	public List<RepositoryEntry> loadTopicRepositoryEntries(AnalysisSearchParameter searchParams);

	public List<String> loadContextLocations(AnalysisSearchParameter searchParams);

	public List<Organisation> loadContextExecutorOrganisations(AnalysisSearchParameter searchParams, boolean withParents);

	public List<Curriculum> loadContextCurriculums(AnalysisSearchParameter searchParams);

	public List<CurriculumElement> loadContextCurriculumElements(AnalysisSearchParameter searchParams, boolean withParents);
	
	public List<CurriculumElementType> loadContextCurriculumElementTypes(AnalysisSearchParameter searchParams);
	
	public List<Organisation> loadContextCurriculumOrganisations(AnalysisSearchParameter searchParams, boolean withParents);

	public List<TaxonomyLevel> loadContextTaxonomyLevels(AnalysisSearchParameter searchParams, boolean withParents);

	public List<QualityContextRole> loadContextRoles(AnalysisSearchParameter clonedSearchParams);

	public List<QualityDataCollection> loadDataCollections(AnalysisSearchParameter searchParams);
	
	public Integer loadMaxSeriesIndex(AnalysisSearchParameter searchParams);

	public SessionFilter createSessionFilter(AnalysisSearchParameter searchParams);

	public GroupedStatistics<GroupedStatistic> calculateStatistics(AnalysisSearchParameter searchParams,
			Collection<String> responseIdentifiers, Collection<Rubric> rubrics, MultiGroupBy multiGroupBy);
	
	public MultiTrendSeries<String> calculateIdentifierTrends(AnalysisSearchParameter searchParams,
			Collection<String> responseIdentifiers, Collection<Rubric> rubrics, TemporalGroupBy temporalGroupBy);

	/**
	 * Calculate trend of multiple rubrics. Before using this method, make sure,
	 * that all rubrics are identically configured to get accurate results (same
	 * scale, number of steps, good end, ...)!
	 *
	 * @param searchParams
	 * @param rubrics
	 * @param groupBy
	 * @param temporalGroupBy
	 * @return
	 */
	public MultiTrendSeries<MultiKey> calculateTrends(AnalysisSearchParameter searchParams,
			Set<Rubric> rubrics, MultiGroupBy groupBy, TemporalGroupBy temporalGroupBy);

	public boolean isInsufficient(Rubric rubric, Double avg);
}
