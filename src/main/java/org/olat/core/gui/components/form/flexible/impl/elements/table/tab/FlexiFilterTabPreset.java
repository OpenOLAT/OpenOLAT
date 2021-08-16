package org.olat.core.gui.components.form.flexible.impl.elements.table.tab;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;

/**
 * 
 * Initial date: 10 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterTabPreset extends FlexiFiltersTabImpl implements FlexiFiltersPreset {
	
	private List<String> visibleFilters;
	private List<FlexiTableFilterValue> filtersValues;
	
	public FlexiFilterTabPreset(String id, String label,
			List<String> visibleFilters, List<FlexiTableFilterValue> filtersValues) {
		super( id, label);
		this.visibleFilters = (visibleFilters == null ? null : List.copyOf(visibleFilters));
		this.filtersValues = (filtersValues == null ? null : List.copyOf(filtersValues));
	}

	@Override
	public List<String> getVisibleFilters() {
		return visibleFilters;
	}

	@Override
	public List<FlexiTableFilterValue> getDefaultFiltersValues() {
		return filtersValues;
	}
}
