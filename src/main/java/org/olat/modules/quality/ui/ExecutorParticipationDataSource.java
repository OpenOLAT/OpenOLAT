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
package org.olat.modules.quality.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationSearchParams;
import org.olat.modules.quality.QualityService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class ExecutorParticipationDataSource implements FlexiTableDataSourceDelegate<ExecutorParticipationRow> {
	
	private final Translator translator;
	private final QualityExecutorParticipationSearchParams searchParams;
	
	@Autowired
	private QualityService qualityService;
	
	public ExecutorParticipationDataSource(Translator translator, QualityExecutorParticipationSearchParams searchParams) {
		this.translator = translator;
		this.searchParams = searchParams;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public int getRowCount() {
		return Math.toIntExact(qualityService.getExecutorParticipationCount(searchParams));
	}

	@Override
	public List<ExecutorParticipationRow> reload(List<ExecutorParticipationRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<ExecutorParticipationRow> getRows(String query, List<FlexiTableFilter> filters,
			int firstResult, int maxResults, SortKey... orderBy) {

		List<QualityExecutorParticipation> participations = qualityService.loadExecutorParticipations(translator,
				searchParams, firstResult, maxResults, orderBy);
		List<ExecutorParticipationRow> rows = new ArrayList<>();
		for (QualityExecutorParticipation participation : participations) {
			rows.add(new ExecutorParticipationRow(participation));
		}

		return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
	}
	
}
