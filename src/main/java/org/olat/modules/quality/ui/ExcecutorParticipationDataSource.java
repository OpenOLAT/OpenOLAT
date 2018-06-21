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
import org.olat.core.id.Identity;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class ExcecutorParticipationDataSource implements FlexiTableDataSourceDelegate<ExcecutorParticipationRow> {
	
	private Identity executor;
	
	@Autowired
	private QualityManager qualityManager;
	
	public ExcecutorParticipationDataSource(Identity executor) {
		this.executor = executor;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public int getRowCount() {
		return qualityManager.getExecutorParticipationCount(executor);
	}

	@Override
	public List<ExcecutorParticipationRow> reload(List<ExcecutorParticipationRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<ExcecutorParticipationRow> getRows(String query, List<FlexiTableFilter> filters,
			List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {

		List<QualityExecutorParticipation> participations = qualityManager.loadExecutorParticipations(executor,
				firstResult, maxResults, orderBy);
		List<ExcecutorParticipationRow> rows = new ArrayList<>();
		for (QualityExecutorParticipation participation : participations) {
			rows.add(new ExcecutorParticipationRow(participation));
		}

		return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
	}
	
}
