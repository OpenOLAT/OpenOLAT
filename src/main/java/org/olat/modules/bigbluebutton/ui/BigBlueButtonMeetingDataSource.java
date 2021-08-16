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
package org.olat.modules.bigbluebutton.ui;

import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingAdminInfos;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingsSearchParameters;
import org.olat.modules.bigbluebutton.model.BigBlueButtonMeetingsSearchParameters.OrderBy;

/**
 * 
 * Initial date: 2 déc. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingDataSource implements FlexiTableDataSourceDelegate<BigBlueButtonMeetingAdminInfos> {
	
	private Integer count;
	private final BigBlueButtonManager bigBlueButtonManager;
	private BigBlueButtonMeetingsSearchParameters searchParams = new BigBlueButtonMeetingsSearchParameters();
	
	public BigBlueButtonMeetingDataSource() {
		bigBlueButtonManager = CoreSpringFactory.getImpl(BigBlueButtonManager.class);
	}
	
	public void resetCount() {
		count = null;
	}

	@Override
	public int getRowCount() {
		if(count == null) {
			count = bigBlueButtonManager.countMeetings(searchParams);
		}
		return count;
	}

	@Override
	public List<BigBlueButtonMeetingAdminInfos> reload(List<BigBlueButtonMeetingAdminInfos> rows) {
		return Collections.emptyList();
	}

	@Override
	public ResultInfos<BigBlueButtonMeetingAdminInfos> getRows(String query, List<FlexiTableFilter> filters,
			int firstResult, int maxResults, SortKey... orderBy) {
		if(StringHelper.containsNonWhitespace(query)) {
			searchParams.setSearchString(query);
		} else {
			searchParams.setSearchString(null);
		}
		
		Boolean recordings = null;
		if(filters != null && !filters.isEmpty()) {
			String filter = filters.get(0).getFilter();
			if("with-recordings".equals(filter)) {
				recordings = Boolean.TRUE;
			} else if("no-recordings".equals(filter)) {
				recordings = Boolean.FALSE;
			}
		}
		searchParams.setRecordings(recordings);
		
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			searchParams.setOrder(OrderBy.secureValueOf(orderBy[0].getKey()));
			searchParams.setOrderAsc(orderBy[0].isAsc());
		} else {
			searchParams.setOrder(null);
		}
		
		List<BigBlueButtonMeetingAdminInfos> viewResults = bigBlueButtonManager.searchMeetings(searchParams, firstResult, maxResults);
		if(firstResult == 0 && viewResults.size() < maxResults) {
			count = Integer.valueOf(viewResults.size());
		}
		return new DefaultResultInfos<>(firstResult + viewResults.size(), -1, viewResults);
	}
}
