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

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityShort;
import org.olat.core.gui.components.form.flexible.elements.AutoCompletionMultiSelection.AutoCompletionSource;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.id.Identity;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.QualityAnalysisService;

/**
 * 
 * Initial date: 10 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TopicIdentitySource implements AutoCompletionSource {
	
	private final Comparator<IdentityShort> lastNameComparator = Comparator.comparing(identity -> identity.getLastName().toLowerCase());
	private final Comparator<IdentityShort> firstNameComparator = Comparator.comparing(identity -> identity.getFirstName().toLowerCase());
	private final Comparator<IdentityShort> identityComparator = lastNameComparator.thenComparing(firstNameComparator);
	
	private final QualityAnalysisService analysisService;
	private AnalysisSearchParameter searchParams;

	public TopicIdentitySource(QualityAnalysisService analysisService) {
		this.analysisService = analysisService;
	}

	public void setSearchParams(AnalysisSearchParameter searchParams) {
		this.searchParams = searchParams;
	}

	@Override
	public SelectionValues getSelectionValues(Collection<String> keys) {
		SelectionValues selectionValues = new SelectionValues();
		
		analysisService.loadTopicIdentity(searchParams).stream()
				.filter(identity -> identity.getStatus() < Identity.STATUS_DELETED)
				.filter(identity -> keys.contains(identity.getKey().toString()))
				.forEach(identity -> selectionValues.add(entry(identity.getKey().toString(), getDisplayName(identity))));
		
		return selectionValues;
	}

	@Override
	public SearchResult getSearchResult(String searchText) {	
		List<IdentityShort> filtered = analysisService.loadTopicIdentity(searchParams).stream()
				.filter(identity -> identity.getStatus() < Identity.STATUS_DELETED)
				.filter(identity -> identity.getLastName().toLowerCase().contains(searchText) || identity.getFirstName().toLowerCase().contains(searchText))
				.sorted(identityComparator)
				.collect(Collectors.toList());
		
		List<IdentityShort> result = filtered.size() > 100? filtered.subList(0, 99): filtered;
		
		SelectionValues selectionValues = new SelectionValues();
		result.forEach(identity -> selectionValues.add(entry(identity.getKey().toString(), getDisplayName(identity))));
		
		return new SearchResult(filtered.size(), result.size(), selectionValues);
	}

	private String getDisplayName(IdentityShort identity) {
		return identity.getLastName() + " " + identity.getFirstName();
	}

}
