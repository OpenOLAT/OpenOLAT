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

import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;

/**
 * 
 * Initial date: 17 août 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterButton {
	
	private final FormLink button;
	private final FlexiTableExtendedFilter filter;
	
	private boolean enabled;
	private boolean implicit = false;
	
	public FlexiFilterButton(FormLink button, FlexiTableExtendedFilter filter, boolean enabled) {
		this.button = button;
		this.filter = filter;
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @param enabled Enable the filter and make the button visible
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		button.setVisible(enabled && !implicit);
	}

	public boolean isImplicit() {
		return implicit;
	}

	public void setImplicit(boolean implicit) {
		this.implicit = implicit;
		button.setVisible(enabled && !implicit);
	}

	public FormLink getButton() {
		return button;
	}

	public FlexiTableExtendedFilter getFilter() {
		return filter;
	}

	@Override
	public int hashCode() {
		return filter == null || filter.getFilter()  == null
				? 24789990 : filter.getFilter().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof FlexiFilterButton) {
			FlexiFilterButton filterButton = (FlexiFilterButton)obj;
			return filter != null && filter.getFilter() != null && filterButton.getFilter() != null
					&& filter.getFilter().equals(filterButton.getFilter().getFilter());
		}
		return false;
	}
}
