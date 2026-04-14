/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.ui;

import java.text.Collator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiUsageLog;
import org.olat.core.commons.services.ai.AiUsageLogSearchParams;
import org.olat.core.commons.services.ai.AiUsageLogSearchParams.OrderBy;
import org.olat.core.commons.services.ai.AiUsageLogStatus;
import org.olat.core.commons.services.ai.manager.AiUsageLogDAO;
import org.olat.core.commons.services.ai.model.AiUsageLogStats;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.translator.Translator;

/**
 * Initial date: 07.04.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AiUsageLogDataSource implements FlexiTableDataSourceDelegate<AiUsageLog> {

	static final String FILTER_AI_FEATURE = "aiFeature";
	static final String FILTER_STATUS = "status";

	private final AiUsageLogDAO usageLogDAO;
	private final AiUsageLogSearchParams searchParams;
	private final Translator translator;
	private Integer count;

	public AiUsageLogDataSource(AiUsageLogDAO usageLogDAO, AiUsageLogSearchParams searchParams, Translator translator) {
		this.usageLogDAO = usageLogDAO;
		this.searchParams = searchParams;
		this.translator = translator;
	}

	public void reset() {
		count = null;
	}

	public void applyFilters(List<FlexiTableFilter> filters) {
		if (filters != null) {
			for (FlexiTableFilter filter : filters) {
				if (FILTER_AI_FEATURE.equals(filter.getFilter()) && filter instanceof FlexiTableMultiSelectionFilter multiFilter) {
					List<String> values = multiFilter.getValues();
					searchParams.setAiFeatures(values != null && !values.isEmpty() ? values : null);
				} else if (FILTER_STATUS.equals(filter.getFilter()) && filter instanceof FlexiTableMultiSelectionFilter multiFilter) {
					List<String> values = multiFilter.getValues();
					if (values != null && !values.isEmpty()) {
						searchParams.setStatuses(values.stream().map(AiUsageLogStatus::valueOf).toList());
					} else {
						searchParams.setStatuses(null);
					}
				}
			}
		}
	}

	public AiUsageLogStats loadStats() {
		return usageLogDAO.getStats(searchParams);
	}

	@Override
	public int getRowCount() {
		if (count == null) {
			count = usageLogDAO.getCount(searchParams);
		}
		return count;
	}

	@Override
	public List<AiUsageLog> reload(List<AiUsageLog> rows) {
		return rows;
	}

	@Override
	public ResultInfos<AiUsageLog> getRows(String query, List<FlexiTableFilter> filters,
			int firstResult, int maxResults, SortKey... orderBy) {
		if (orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			searchParams.setOrder(OrderBy.secureValueOf(orderBy[0].getKey()));
			searchParams.setOrderAsc(orderBy[0].isAsc());
			if (OrderBy.aiFeature == searchParams.getOrder()) {
				Collator collator = Collator.getInstance(translator.getLocale());
				Map<String, AiFeature> translationToFeature = AiFeature.VALUES.stream()
					.collect(Collectors.toMap(v -> translator.translate(v.getI18nKey()), Function.identity()));
				List<String> orderedValues = translationToFeature.keySet().stream()
						.sorted((s1, s2) -> collator.compare(s1,  s2))
						.map(translation -> translationToFeature.get(translation).getType())
						.toList();
				searchParams.setOrderedValues(orderedValues);
			} else if (OrderBy.status == searchParams.getOrder()) {
				Collator collator = Collator.getInstance(translator.getLocale());
				Map<String, AiUsageLogStatus> translationToFeature = AiUsageLogStatus.VALUES.stream()
					.collect(Collectors.toMap(v -> translator.translate(v.getI18nKey()), Function.identity()));
				List<String> orderedValues = translationToFeature.keySet().stream()
						.sorted((s1, s2) -> collator.compare(s1,  s2))
						.map(translation -> translationToFeature.get(translation).name())
						.toList();
				searchParams.setOrderedValues(orderedValues);
			}
		} else {
			searchParams.setOrder(null);
		}

		applyFilters(filters);

		List<AiUsageLog> rows = usageLogDAO.getUsageLogs(searchParams, firstResult, maxResults);
		return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
	}
	
}
