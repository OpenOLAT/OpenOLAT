/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.scope;

import java.util.List;

/**
 * 
 * Initial date: 10 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DateScopeDropdown extends DateScopeImpl {
	
	private final String dropdownLabel;
	private final List<DateScopeOption> options;
	private final DateScopeOption preselectedOption;
	private final DateScopeOption initialOption;
	
	DateScopeDropdown(String key, String displayName, String hint, DateScopeOption preselectedOption,
			String dropdownLabel, List<DateScopeOption> options, DateScopeOption initialOption) {
		super(key, displayName, hint, (preselectedOption == null ? null : preselectedOption.scope().getDateRange()));
		this.options = options;
		this.dropdownLabel = dropdownLabel;
		this.preselectedOption = preselectedOption;
		this.initialOption = initialOption;
	}
	
	public String getDropdownLabel() {
		return dropdownLabel;
	}
	
	public DateScopeOption getPreselectedOption() {
		return preselectedOption;
	}
	
	public List<DateScopeOption> getOptions() {
		return options;
	}
	
	public DateScopeOption getInitialOption() {
		return initialOption;
	}
	
	public record DateScopeOption(String displayName, DateScope scope) {
		//
	}
}
