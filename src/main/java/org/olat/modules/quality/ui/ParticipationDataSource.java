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
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityParticipation;
import org.olat.modules.quality.QualityService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class ParticipationDataSource implements FlexiTableDataSourceDelegate<ParticipationRow> {
	
	private QualityDataCollectionLight dataCollection;
	
	@Autowired
	private QualityService qualityService;
	
	public ParticipationDataSource(QualityDataCollectionLight dataCollection) {
		this.dataCollection = dataCollection;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public int getRowCount() {
		return qualityService.getParticipationCount(dataCollection);
	}

	@Override
	public List<ParticipationRow> reload(List<ParticipationRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<ParticipationRow> getRows(String query, List<FlexiTableFilter> filters,
			int firstResult, int maxResults, SortKey... orderBy) {

		List<QualityParticipation> participations = qualityService.loadParticipations(dataCollection,
				firstResult, maxResults, orderBy);
		List<ParticipationRow> rows = new ArrayList<>();
		for (QualityParticipation participation : participations) {
			rows.add(new ParticipationRow(participation));
		}

		return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
	}
	
}
