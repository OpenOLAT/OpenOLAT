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
	public static final String FILTER_PASSED = "passed";
	public static final String FILTER_USER_VISIBILITY = "userVisibility";
	public static final String FILTER_MEMBERS = "members";
	public static final String FILTER_GROUPS = "groups";
	public static final String FILTER_OBLIGATION = "obligation";
	
	private String tabId;
	private boolean filtersExpanded;
	private List<String> status;
	private List<String> passed;
	private String userVisibility;
	private String members;
	private List<String> obligations;
	private List<String> groupKeys;
	
	public AssessedIdentityListState() {
		//
	}
	
	public AssessedIdentityListState(List<String> status, List<String> passed, String userVisibility, String members,
			List<String> obligations, List<String> groupKeys, String tabId, boolean filtersExpanded) {
		this.tabId = tabId;
		this.status = status;
		this.passed = passed;
		this.userVisibility = userVisibility;
		this.members = members;
		this.obligations = obligations;
		this.filtersExpanded = filtersExpanded;
		this.groupKeys = groupKeys;
	}
	
	public static AssessedIdentityListState valueOf(FlexiFiltersTab tab, List<FlexiTableFilter> filters, boolean filtersExpanded) {
		AssessedIdentityListState state = new AssessedIdentityListState();
		state.tabId = tab == null ? null : tab.getId();
		state.filtersExpanded = filtersExpanded;
		for(FlexiTableFilter filter:filters) {
			if(FILTER_STATUS.equals(filter.getFilter()) && filter.isSelected()) {
				if(filter instanceof FlexiTableMultiSelectionFilter) {
					state.setStatus(((FlexiTableMultiSelectionFilter)filter).getValues());
				} else {
					log.warn("Filter cannot pass value to state in assessment tool: {}", filter.getFilter());
				}
			} else if(FILTER_PASSED.equals(filter.getFilter())) {
				if(filter instanceof FlexiTableMultiSelectionFilter) {
					state.setPassed(((FlexiTableMultiSelectionFilter)filter).getValues());
				} else {
					log.warn("Filter cannot pass value to state in assessment tool: {}", filter.getFilter());
				}
			} else if(FILTER_USER_VISIBILITY.equals(filter.getFilter()) && filter.isSelected()) {
				if(filter instanceof FlexiTableSingleSelectionFilter) {
					state.setUserVisibility(((FlexiTableSingleSelectionFilter)filter).getValue());
				} else {
					log.warn("Filter cannot pass value to state in assessment tool: {}", filter.getFilter());
				}
			} else if(FILTER_MEMBERS.equals(filter.getFilter())) {
				if(filter instanceof FlexiTableSingleSelectionFilter) {
					state.setMembers(((FlexiTableSingleSelectionFilter)filter).getValue());
				} else {
					log.warn("Filter cannot pass value to state in assessment tool: {}", filter.getFilter());
				}
			} else if(FILTER_OBLIGATION.equals(filter.getFilter())) {
				if(filter instanceof FlexiTableMultiSelectionFilter) {
					state.setObligations(((FlexiTableMultiSelectionFilter)filter).getValues());
				} else {
					log.warn("Filter cannot pass value to state in assessment tool: {}", filter.getFilter());
				}
			} else if(FILTER_GROUPS.equals(filter.getFilter())) {
				if(filter instanceof FlexiTableMultiSelectionFilter) {
					state.setGroupKeys(((FlexiTableMultiSelectionFilter)filter).getValues());
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
					&& getStatus() != null
					&& filter instanceof FlexiTableMultiSelectionFilter) {
				((FlexiTableMultiSelectionFilter)filter).setValues(getStatus());
			} else if(FILTER_PASSED.equals(filter.getFilter())
					&& getPassed() != null
					&& filter instanceof FlexiTableMultiSelectionFilter) {
				((FlexiTableMultiSelectionFilter)filter).setValues(getPassed());
			} else if(FILTER_USER_VISIBILITY.equals(filter.getFilter())
					&& StringHelper.containsNonWhitespace(getUserVisibility())
					&& filter instanceof FlexiTableSingleSelectionFilter) {
				((FlexiTableSingleSelectionFilter)filter).setValue(getUserVisibility());
			} else if(FILTER_MEMBERS.equals(filter.getFilter())
					&& getMembers() != null
					&& filter instanceof FlexiTableSingleSelectionFilter) {
				((FlexiTableSingleSelectionFilter)filter).setValue(getMembers());
			} else if(FILTER_OBLIGATION.equals(filter.getFilter())
					&& getObligations() != null
					&& filter instanceof FlexiTableMultiSelectionFilter) {
				((FlexiTableMultiSelectionFilter)filter).setValues(getObligations());
			} else if(FILTER_GROUPS.equals(filter.getFilter())
					&& getGroupKeys() != null
					&& filter instanceof FlexiTableMultiSelectionFilter) {
				((FlexiTableMultiSelectionFilter)filter).setValues(getGroupKeys());
			}
		}
	}
	
	public String getTabId() {
		return tabId;
	}
	
	public boolean isFiltersExpanded() {
		return filtersExpanded;
	}

	public List<String>  getStatus() {
		return status;
	}

	public void setStatus(List<String>  status) {
		this.status = status;
	}

	public List<String> getPassed() {
		return passed;
	}

	public void setPassed(List<String> passed) {
		this.passed = passed;
	}

	public String getUserVisibility() {
		return userVisibility;
	}

	public void setUserVisibility(String userVisibility) {
		this.userVisibility = userVisibility;
	}

	public List<String> getObligations() {
		return obligations;
	}

	public void setObligations(List<String> obligations) {
		this.obligations = obligations;
	}

	public String getMembers() {
		return members;
	}

	public void setMembers(String members) {
		this.members = members;
	}

	public List<String> getGroupKeys() {
		return groupKeys;
	}

	public void setGroupKeys(List<String> groupKeys) {
		this.groupKeys = groupKeys;
	}

	@Override
	public AssessedIdentityListState clone() {
		return new AssessedIdentityListState(status, passed, userVisibility, members, obligations, groupKeys, tabId, filtersExpanded);
	}
}
