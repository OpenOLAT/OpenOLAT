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
package org.olat.admin.restapi;

import java.util.List;
import java.util.Objects;

import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.util.StringHelper;
import org.olat.restapi.audit.ApiAuditChannel;
import org.olat.restapi.audit.ApiAuditLog;
import org.olat.restapi.audit.ApiAuditLogSearchParams;
import org.olat.restapi.audit.ApiAuditLogSearchParams.OrderBy;
import org.olat.restapi.audit.ApiAuditLogService;
import org.olat.restapi.audit.ApiAuditStatusClass;

/**
 * Initial date: 17 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class ApiAuditLogDataSource implements FlexiTableDataSourceDelegate<ApiAuditLog> {
	
	static final String FILTER_USER = "user";
	static final String FILTER_CHANNEL = "channel";
	static final String FILTER_METHOD = "method";
	static final String FILTER_RESOURCE = "resource";
	static final String FILTER_STATUS = "status";
	
	private final ApiAuditLogService auditLogService;
	private final ApiAuditLogSearchParams searchParams = new ApiAuditLogSearchParams();
	private Integer count;
	
	public ApiAuditLogDataSource(ApiAuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}
	
	public ApiAuditLogSearchParams getSearchParams() {
		return searchParams;
	}
	
	public void reset() {
		count = null;
	}
	
	public void applyFilters(List<FlexiTableFilter> filters) {
		if(filters == null) {
			return;
		}
		
		for(FlexiTableFilter filter:filters) {
			if(FILTER_USER.equals(filter.getFilter())) {
				searchParams.setUserSearch(cleanValue(filter.getValue()));
			} else if(FILTER_RESOURCE.equals(filter.getFilter())) {
				searchParams.setResourceClass(cleanValue(filter.getValue()));
			} else if(FILTER_CHANNEL.equals(filter.getFilter()) && filter instanceof FlexiTableMultiSelectionFilter multiFilter) {
				List<String> values = multiFilter.getValues();
				searchParams.setChannels(values == null || values.isEmpty() ? null
						: values.stream().map(ApiAuditChannel::secureValueOf).filter(Objects::nonNull).toList());
			} else if(FILTER_METHOD.equals(filter.getFilter()) && filter instanceof FlexiTableMultiSelectionFilter multiFilter) {
				List<String> values = multiFilter.getValues();
				searchParams.setMethods(values == null || values.isEmpty() ? null : values);
			} else if(FILTER_STATUS.equals(filter.getFilter()) && filter instanceof FlexiTableMultiSelectionFilter multiFilter) {
				List<String> values = multiFilter.getValues();
				searchParams.setStatusClasses(values == null || values.isEmpty() ? null
						: values.stream().map(ApiAuditStatusClass::secureValueOf).filter(Objects::nonNull).toList());
			}
		}
	}
	
	private String cleanValue(String value) {
		return StringHelper.containsNonWhitespace(value) ? value : null;
	}
	
	@Override
	public int getRowCount() {
		if(count == null) {
			count = Integer.valueOf(auditLogService.count(searchParams));
		}
		return count.intValue();
	}
	
	@Override
	public List<ApiAuditLog> reload(List<ApiAuditLog> rows) {
		return rows;
	}
	
	@Override
	public ResultInfos<ApiAuditLog> getRows(String query, List<FlexiTableFilter> filters,
			int firstResult, int maxResults, SortKey... orderBy) {
		if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
			searchParams.setOrder(OrderBy.secureValueOf(orderBy[0].getKey()));
			searchParams.setOrderAsc(orderBy[0].isAsc());
		} else {
			searchParams.setOrder(null);
		}
		applyFilters(filters);
		
		List<ApiAuditLog> rows = auditLogService.search(searchParams, firstResult, maxResults);
		return new DefaultResultInfos<>(firstResult + rows.size(), -1, rows);
	}
}
