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
package org.olat.modules.ceditor.ui;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.modules.ceditor.Category;

/**
 * 
 * Initial date: 30 oct. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CategoryInfos implements TagInfo {
	
	private boolean selected;
	private final Category category;
	
	public CategoryInfos(Category category) {
		this.category = category;
	}
	
	public CategoryInfos(Category category, boolean selected) {
		this.category = category;
		this.selected = selected;
	}

	/**
	 * Merge the categories of the element with the other selectable ones, without duplicates.
	 *
	 * @param currentCategories The categories of the element (can be null)
	 * @param otherCategories The other selectable categories
	 * @return A list of categories
	 */
	public static List<CategoryInfos> valueOf(Collection<Category> currentCategories, Collection<Category> otherCategories) {
		Map<Long,CategoryInfos> categoriesMap = new LinkedHashMap<>();
		if(currentCategories != null) {
			currentCategories.forEach(category -> categoriesMap.put(category.getKey(), new CategoryInfos(category, true)));
		}
		otherCategories.forEach(category -> categoriesMap.putIfAbsent(category.getKey(), new CategoryInfos(category, false)));
		return categoriesMap.values().stream()
				.collect(Collectors.toList());
	}

	@Override
	public Long getKey() {
		return category.getKey();
	}
	
	@Override
	public String getDisplayName() {
		return category.getName();
	}

	@Override
	public Date getCreationDate() {
		return category.getCreationDate();
	}
	
	public Category getCategory() {
		return category;
	}

	@Override
	public Long getCount() {
		return null;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
