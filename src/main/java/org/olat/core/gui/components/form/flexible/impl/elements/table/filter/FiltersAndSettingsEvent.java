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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 24 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FiltersAndSettingsEvent extends Event {

	private static final long serialVersionUID = -8314660538483434910L;

	public static final String FILTERS_AND_SETTINGS = "filters-and-settings";
	public static final String FILTERS_RESET = "filters-reset";

	private SortKey sortKey;
	private Choice customizedColumns;
	private FlexiTableRendererType renderType;
	private boolean resetCustomizedColumns = false;
	private List<FlexiTableFilterValue> filterValues;
	
	public FiltersAndSettingsEvent(String cmd) {
		super(cmd);
	}
	
	public FiltersAndSettingsEvent(FlexiTableRendererType renderType) {
		super(FILTERS_AND_SETTINGS);
		this.renderType = renderType;
	}

	public SortKey getSortKey() {
		return sortKey;
	}

	public void setSortKey(SortKey sortKey) {
		this.sortKey = sortKey;
	}

	public FlexiTableRendererType getRenderType() {
		return renderType;
	}

	public void setRenderType(FlexiTableRendererType renderType) {
		this.renderType = renderType;
	}

	public Choice getCustomizedColumns() {
		return customizedColumns;
	}

	public void setCustomizedColumns(Choice customizedColumns) {
		this.customizedColumns = customizedColumns;
	}

	public boolean isResetCustomizedColumns() {
		return resetCustomizedColumns;
	}

	public void setResetCustomizedColumns(boolean resetCustomizedColumns) {
		this.resetCustomizedColumns = resetCustomizedColumns;
	}

	public List<FlexiTableFilterValue> getFilterValues() {
		return filterValues;
	}

	public void setFilterValues(List<FlexiTableFilterValue> filterValues) {
		this.filterValues = filterValues;
	}
}
