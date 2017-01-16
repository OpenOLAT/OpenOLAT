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

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.id.context.StateEntry;

/**
 * 
 * Initial date: 07.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityListState implements StateEntry {

	private static final long serialVersionUID = -6546620154750599626L;
	
	private String filter;
	private List<FlexiTableFilter> extendedFilters;
	
	public AssessedIdentityListState() {
		//
	}
	
	public AssessedIdentityListState(String filter) {
		this.filter = filter;
	}
	
	public AssessedIdentityListState(String filter, List<FlexiTableFilter> extendedFilters) {
		this.filter = filter;
		this.extendedFilters = extendedFilters;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public List<FlexiTableFilter> getExtendedFilters() {
		return extendedFilters;
	}

	public void setExtendedFilters(List<FlexiTableFilter> extendedFilters) {
		this.extendedFilters = extendedFilters;
	}

	@Override
	public AssessedIdentityListState clone() {
		AssessedIdentityListState clone = new AssessedIdentityListState();
		clone.setFilter(getFilter());
		clone.setExtendedFilters(getExtendedFilters());
		return clone;
	}
}
