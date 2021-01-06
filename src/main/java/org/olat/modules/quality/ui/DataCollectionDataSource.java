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
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionDataSource implements FlexiTableDataSourceDelegate<DataCollectionRow> {
	
	private final Translator translator;
	private final QualityDataCollectionViewSearchParams defaultSearchParams;
	private QualityDataCollectionViewSearchParams searchParams;
	private Integer count;
	
	@Autowired
	private QualityService qualityService;

	public DataCollectionDataSource(Translator translator, QualityDataCollectionViewSearchParams defaultSearchParams) {
		this.translator = translator;
		this.defaultSearchParams = defaultSearchParams;
		
		CoreSpringFactory.autowireObject(this);
	}

	public void setSearchParams(QualityDataCollectionViewSearchParams searchParams) {
		this.searchParams = searchParams;
		this.searchParams.setOrgansationRefs(defaultSearchParams.getOrgansationRefs());
		this.searchParams.setReportAccessIdentity(defaultSearchParams.getReportAccessIdentity());
		this.searchParams.setLearnResourceManagerOrganisationRefs(defaultSearchParams.getLearnResourceManagerOrganisationRefs());
		this.searchParams.setIgnoreReportAccessRelationRole(defaultSearchParams.isIgnoreReportAccessRelationRole());
		count = null;
	}
	
	public void resetCount() {
		count = null;
	}

	@Override
	public int getRowCount() {
		if (count == null) {
			count = qualityService.getDataCollectionCount(searchParams);
		}
		return count.intValue();
	}

	@Override
	public List<DataCollectionRow> reload(List<DataCollectionRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<DataCollectionRow> getRows(String query, List<FlexiTableFilter> filters,
			List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
		searchParams.setSearchString(query);

		List<QualityDataCollectionView> dataCollections = qualityService.loadDataCollections(translator, searchParams,
				firstResult, maxResults, orderBy);
		List<DataCollectionRow> rows = new ArrayList<>();
		for (QualityDataCollectionView dataCollection : dataCollections) {
			rows.add(new DataCollectionRow(dataCollection));
		}

		return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
	}
}
