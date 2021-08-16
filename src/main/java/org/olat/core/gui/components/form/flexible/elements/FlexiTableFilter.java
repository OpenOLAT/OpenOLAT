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
package org.olat.core.gui.components.form.flexible.elements;

import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 12.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableFilter {

	public static final FlexiTableFilter SPACER = new FlexiTableFilter("oo-spacer-xx", "oo-spacer-xx");
	
	private final String label;
	private final String filter;
	private final String iconLeftCSS;
	private final FlexiTableFilterIconRenderer renderer;
	
	private boolean selected;
	private boolean showAll = false;
	private boolean visible = true;
	private final boolean alwaysVisible;
	
	public FlexiTableFilter(String label, String filter) {
		this(label, filter, true, true);
	}
	
	public FlexiTableFilter(String label, String filter, boolean visible, boolean alwaysVisible) {
		this.label = label;
		this.filter = filter;
		this.renderer = null;
		this.iconLeftCSS = null;
		this.visible = visible;
		this.alwaysVisible = alwaysVisible;
	}
	
	public FlexiTableFilter(String label, String filter, boolean showAll) {
		this.label = label;
		this.filter = filter;
		this.showAll = showAll;
		this.renderer = null;
		this.iconLeftCSS = null;
		this.alwaysVisible = true;
	}
	
	public FlexiTableFilter(String label, String filter, String iconLeftCSS) {
		this.label = label;
		this.filter = filter;
		this.renderer = null;
		this.iconLeftCSS = iconLeftCSS;
		this.alwaysVisible = true;
	}
	
	public FlexiTableFilter(String label, String filter, FlexiTableFilterIconRenderer renderer) {
		this.label = label;
		this.filter = filter;
		this.renderer = renderer;
		this.iconLeftCSS = null;
		this.alwaysVisible = true;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getFilter() {
		return filter;
	}

	public boolean isShowAll() {
		return showAll;
	}

	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}

	public String getIconLeftCSS() {
		return iconLeftCSS;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isAlwaysVisible() {
		return alwaysVisible;
	}

	public FlexiTableFilterIconRenderer getIconRenderer() {
		return renderer;
	}
	
	public static FlexiTableFilter getFilter(List<FlexiTableFilter> filters, String filter) {
		if(filters != null && !filters.isEmpty() && StringHelper.containsNonWhitespace(filter)) {
			for(FlexiTableFilter ftf:filters) {
				if(filter.equals(ftf.getFilter())) {
					return ftf;
				}
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		return filter == null ? 864587 : filter.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if(obj instanceof FlexiTableFilter) {
			FlexiTableFilter other = (FlexiTableFilter) obj;
			return showAll == other.showAll
					&& ((filter == null && other.filter == null) || (filter != null && filter.equals(other.filter)));
		}
		return false;
	}
}