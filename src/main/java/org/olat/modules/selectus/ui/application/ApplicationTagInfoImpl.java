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
package org.olat.modules.selectus.ui.application;

import java.util.Date;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;

/**
 * 
 * Initial date: 29 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ApplicationTagInfoImpl implements TagInfo {
	
	private boolean selected;
	private final boolean editable;
	private final Category category;
	private final boolean administrative;
	
	public ApplicationTagInfoImpl(Category category, boolean administrative, boolean editable) {
		selected = false;
		this.editable = editable;
		this.category = category;
		this.administrative = administrative;
	}
	
	public ApplicationTagInfoImpl(ApplicationCategoryInfos categoryInfos, boolean selected, boolean editable) {
		this.selected = selected;
		this.editable = editable;
		category = categoryInfos.getCategory();
		administrative = categoryInfos.isAdministrative();
	}

	@Override
	public Long getKey() {
		if(administrative) {
			return Long.valueOf(-category.getKey().longValue());
		}
		return category.getKey();
	}
	
	@Override
	public Date getCreationDate() {
		return category.getCreationDate();
	}

	@Override
	public String getDisplayName() {
		String name = category.getName();
		if(administrative) {
			name = "a:" + name;
		}
		return name;
	}

	@Override
	public Long getCount() {
		return null;
	}

	@Override
	public String getColor() {
		return category.getColor();
	}
	
	@Override
	public String getIconCssClass() {
		return administrative ? "o_icon_eye_slash" : null;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public int hashCode() {
		return category.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ApplicationTagInfoImpl other) {
			return administrative == other.administrative
					&& category.equals(other.category);
		}
		return false;
	}
}
