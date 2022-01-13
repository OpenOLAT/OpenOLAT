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
package org.olat.modules.quality.analysis.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.modules.quality.ui.QualityUIFactory.getCurriculumElementName;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.components.form.flexible.elements.AutoCompletionMultiSelection.AutoCompletionSource;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.QualityAnalysisService;

/**
 * 
 * Initial date: 9 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ContextCurriculumElementSource implements AutoCompletionSource {

	private final QualityAnalysisService analysisService;
	private AnalysisSearchParameter searchParams;
	
	public ContextCurriculumElementSource(QualityAnalysisService analysisService) {
		this.analysisService = analysisService;
	}

	public void setSearchParams(AnalysisSearchParameter searchParams) {
		this.searchParams = searchParams;
	}

	@Override
	public SelectionValues getSelectionValues(Collection<String> keys) {
		SelectionValues selectionValues = new SelectionValues();
		
		analysisService.loadContextCurriculumElements(searchParams, true).stream()
				.filter(re -> keys.contains(re.getKey().toString()))
				.forEach(element -> selectionValues.add(entry(element.getKey().toString(), getCurriculumElementName(element))));
		
		return selectionValues;
	}

	@Override
	public SearchResult getSearchResult(String searchText) {
		List<CurriculumElement> filtered = analysisService.loadContextCurriculumElements(searchParams, true).stream()
				.filter(element -> filter(element, searchText))
				.sorted((element1, element2) -> getCurriculumElementName(element1).compareToIgnoreCase(getCurriculumElementName(element2)))
				.collect(Collectors.toList());
		
		List<CurriculumElement> result = filtered.size() > 100? filtered.subList(0, 99): filtered;
		
		SelectionValues selectionValues = new SelectionValues();
		result.forEach(element -> selectionValues.add(entry(element.getKey().toString(), getCurriculumElementName(element))));
		
		return new SearchResult(filtered.size(), result.size(), selectionValues);
	}

	private boolean filter(CurriculumElement element, String searchText) {
		return (StringHelper.containsNonWhitespace(element.getDisplayName()) && element.getDisplayName().toLowerCase().contains(searchText))
				|| (StringHelper.containsNonWhitespace(element.getIdentifier()) && element.getIdentifier().toLowerCase().contains(searchText));
	}

}
