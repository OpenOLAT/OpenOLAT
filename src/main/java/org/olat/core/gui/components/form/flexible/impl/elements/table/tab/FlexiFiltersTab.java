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
public interface FlexiFiltersTab {
	
	public String getId();
	
	public String getLabel();
	
	public FlexiFilterTabPosition getPosition();
	
	public void setPosition(FlexiFilterTabPosition position);

	public String getElementCssClass();
	
	public void setElementCssClass(String cssClass);
	
	
	public boolean isFiltersExpanded();
	
	public void setFiltersExpanded(boolean expanded);
	
	/**
	 * The small left quick search is replaced by a bigger search field
	 * placed in the center.
	 * 
	 * @return true if the large search field is visible.
	 */
	public boolean isLargeSearch();
	
	public void setLargeSearch(boolean largeSearch);
	
	public TabSelectionBehavior getSelectionBehavior();
	
	/**
	 * List of invisible but always selected filters.
	 * 
	 * @return The list of filters ID.
	 */
	public List<String> getImplicitFilters();
	
	/**
	 * defined the list of visible filter buttons. If NULL, fallback
	 * to the preferences of the filters.
	 * 
	 * @return List of visible filter buttons.
	 */
	public List<String> getEnabledFilters();
	
	public List<FlexiTableFilterValue> getDefaultFiltersValues();
	
	public void setDefaultFiltersValues(List<FlexiTableFilterValue> filtersValues);

}
