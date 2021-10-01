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
package org.olat.modules.assessment.ui;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 07.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityListState implements StateEntry {

	private static final long serialVersionUID = -6546620154750599626L;
	private static final Logger log = Tracing.createLoggerFor(AssessedIdentityListState.class);

	public static final String FILTER_STATUS = "status";
	public static final String FILTER_GROUPS = "groups";
	public static final String FILTER_OBLIGATION = "obligation";
	
	private String tabId;
	private boolean filtersExpanded;
	private String status;
	private List<String> groupKeys;
	private List<String> obligations;
	
	public AssessedIdentityListState() {
		//
	}
	
	public AssessedIdentityListState(String status, List<String> groupKeys, List<String> obligations, String tabId, boolean filtersExpanded) {
		this.tabId = tabId;
		this.status = status;
		this.groupKeys = groupKeys;
		this.obligations = obligations;
		this.filtersExpanded = filtersExpanded;
	}
	
	public static AssessedIdentityListState valueOf(String statusValue) {
		return new AssessedIdentityListState(statusValue, null, null, null, false);
	}
	
	public static AssessedIdentityListState valueOf(FlexiFiltersTab tab, List<FlexiTableFilter> filters, boolean filtersExpanded) {
		AssessedIdentityListState state = new AssessedIdentityListState();
		state.tabId = tab == null ? null : tab.getId();
		state.filtersExpanded = filtersExpanded;
		for(FlexiTableFilter filter:filters) {
			if(FILTER_STATUS.equals(filter.getFilter()) && filter.isSelected()) {
				if(filter instanceof FlexiTableSingleSelectionFilter) {
					state.setStatus(((FlexiTableSingleSelectionFilter)filter).getValue());
				} else {
					log.warn("Filter cannot pass value to state in assessment tool: {}", filter.getFilter());
				}
			} else if(FILTER_GROUPS.equals(filter.getFilter())) {
				if(filter instanceof FlexiTableMultiSelectionFilter) {
					state.setGroupKeys(((FlexiTableMultiSelectionFilter)filter).getValues());
				} else {
					log.warn("Filter cannot pass value to state in assessment tool: {}", filter.getFilter());
				}
			} else if(FILTER_OBLIGATION.equals(filter.getFilter())) {
				if(filter instanceof FlexiTableMultiSelectionFilter) {
					state.setObligations(((FlexiTableMultiSelectionFilter)filter).getValues());
				} else {
					log.warn("Filter cannot pass value to state in assessment tool: {}", filter.getFilter());
				}
			}
		}
		return state;
	}
	
	public void setValuesToFilter(List<FlexiTableExtendedFilter> filters) {
		for(FlexiTableExtendedFilter filter:filters) {
			if(FILTER_STATUS.equals(filter.getFilter())
					&& StringHelper.containsNonWhitespace(getStatus())
					&& filter instanceof FlexiTableSingleSelectionFilter) {
				((FlexiTableSingleSelectionFilter)filter).setValue(getStatus());
			} else if(FILTER_GROUPS.equals(filter.getFilter())
					&& getGroupKeys() != null
					&& filter instanceof FlexiTableMultiSelectionFilter) {
				((FlexiTableMultiSelectionFilter)filter).setValues(getGroupKeys());
			} else if(FILTER_OBLIGATION.equals(filter.getFilter())
					&& getObligations() != null
					&& filter instanceof FlexiTableMultiSelectionFilter) {
				((FlexiTableMultiSelectionFilter)filter).setValues(getObligations());
			}
		}
	}
	
	public String getTabId() {
		return tabId;
	}
	
	public boolean isFiltersExpanded() {
		return filtersExpanded;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<String> getGroupKeys() {
		return groupKeys;
	}

	public void setGroupKeys(List<String> groupKeys) {
		this.groupKeys = groupKeys;
	}

	public List<String> getObligations() {
		return obligations;
	}

	public void setObligations(List<String> obligations) {
		this.obligations = obligations;
	}

	@Override
	public AssessedIdentityListState clone() {
		return new AssessedIdentityListState(status, groupKeys, obligations, tabId, filtersExpanded);
	}
}
