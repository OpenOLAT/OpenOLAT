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
package org.olat.core.gui.components.form.flexible.impl.elements.table.tab;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;

/**
 * 
 * Initial date: 12 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFiltersTabImpl implements FlexiFiltersTab {
	
	private final String id;
	private final String label;
	private String elementCssClass;
	private FlexiFilterTabPosition position = FlexiFilterTabPosition.left;
	
	private boolean filtersExpanded;
	private boolean largeSearch;
	private TabSelectionBehavior selectionBehavior = TabSelectionBehavior.nothing;
	
	private List<String> enabledFilters;
	private List<String> implicitFilters;
	private List<FlexiTableFilterValue> defaultFiltersValues;
	
	public FlexiFiltersTabImpl(String id, String label) {
		this(id, label, TabSelectionBehavior.nothing);
	}
	
	public FlexiFiltersTabImpl(String id, String label, TabSelectionBehavior selectionBehavior) {
		this.id = id;
		this.label = label;
		this.selectionBehavior = selectionBehavior;
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getElementCssClass() {
		return elementCssClass;
	}
	
	@Override
	public void setElementCssClass(String elementCssClass) {
		this.elementCssClass = elementCssClass;
	}

	@Override
	public FlexiFilterTabPosition getPosition() {
		return position;
	}

	@Override
	public void setPosition(FlexiFilterTabPosition position) {
		this.position = position;
	}

	@Override
	public boolean isFiltersExpanded() {
		return filtersExpanded;
	}

	@Override
	public void setFiltersExpanded(boolean filtersExpanded) {
		this.filtersExpanded = filtersExpanded;
	}

	@Override
	public boolean isLargeSearch() {
		return largeSearch;
	}

	@Override
	public void setLargeSearch(boolean largeSearch) {
		this.largeSearch = largeSearch;
	}

	@Override
	public TabSelectionBehavior getSelectionBehavior() {
		return selectionBehavior;
	}

	public void setSelectionBehavior(TabSelectionBehavior selectionBehavior) {
		this.selectionBehavior = selectionBehavior;
	}

	@Override
	public List<String> getEnabledFilters() {
		return enabledFilters;
	}

	public void setEnabledFilters(List<String> enabledFilters) {
		this.enabledFilters = enabledFilters;
	}

	@Override
	public List<String> getImplicitFilters() {
		return implicitFilters;
	}

	public void setImplicitFilters(List<String> implicitFilters) {
		this.implicitFilters = implicitFilters;
	}
	
	@Override
	public List<FlexiTableFilterValue> getDefaultFiltersValues() {
		return defaultFiltersValues;
	}

	@Override
	public void setDefaultFiltersValues(List<FlexiTableFilterValue> filtersValues) {
		this.defaultFiltersValues = filtersValues;
	}
}
