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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;

/**
 * 
 * Initial date: 6 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFiltersTabFactory {
	
	private FlexiFiltersTabFactory() {
		//
	}
	
	public static FlexiFiltersTab tab(String id, String label, TabSelectionBehavior selectionBehavior) {
		return new FlexiFiltersTabImpl(id, label, selectionBehavior);
	}
	
	/**
	 * 
	 * @param id The id of the tab, will be used for the business path
	 * @param label The label of the tab
	 * @param selectionBehavior Behavior if the tab is selected
	 * @param implicitValueFilters a list of filters and theirs values, the filters
	 * 		will be implicit, always selected and invisible to the user.
	 * @return The configuration of a tab
	 */
	public static FlexiFiltersTab tabWithImplicitFilters(String id, String label,
			TabSelectionBehavior selectionBehavior, List<FlexiTableFilterValue> implicitValueFilters) {
		FlexiFiltersTabImpl preset = new FlexiFiltersTabImpl(id, label, selectionBehavior);
		
		List<String> implicitFilters = new ArrayList<>(implicitValueFilters.size());
		for(FlexiTableFilterValue implicitValueFilter:implicitValueFilters) {
			implicitFilters.add(implicitValueFilter.getFilter());
		}
		preset.setImplicitFilters(implicitFilters);
		preset.setDefaultFiltersValues(implicitValueFilters);
		return preset;
	}
	
	/**
	 * 
	 * @param id The id of the tab, will be used for the business path
	 * @param label The label of the tab
	 * @param selectionBehavior Behavior if the tab is selected
	 * @param valueFilters a list of filters and theirs values, the filters
	 * 		will be explicit, selected and visible to the user which can change them.
	 * @return The configuration of a tab
	 */
	public static FlexiFiltersTab tabWithFilters(String id, String label,
			TabSelectionBehavior selectionBehavior, List<FlexiTableFilterValue> valueFilters) {
		FlexiFiltersTabImpl preset = new FlexiFiltersTabImpl(id, label, selectionBehavior);
		preset.setImplicitFilters(new ArrayList<>());
		preset.setDefaultFiltersValues(valueFilters);
		return preset;
	}
	
	/**
	 * 
	 * @param preset A tab to copy
	 * @return The configuration of a tab
	 */
	public static FlexiFiltersTab copyOf(FlexiFiltersTab preset) {
		FlexiFiltersTabImpl copy = new FlexiFiltersTabImpl(preset.getId(), preset.getLabel());
		copy.setElementCssClass(preset.getElementCssClass());

		List<String> enabled = preset.getEnabledFilters() == null ? null : new ArrayList<>(preset.getEnabledFilters());
		copy.setEnabledFilters(enabled);
		List<String> impFilters = preset.getImplicitFilters() == null ? null : new ArrayList<>(preset.getImplicitFilters());
		copy.setImplicitFilters(impFilters);
		List<FlexiTableFilterValue> values = preset.getDefaultFiltersValues() == null ? null : new ArrayList<>(preset.getDefaultFiltersValues());
		copy.setDefaultFiltersValues(values);
		return copy;
	}

}
