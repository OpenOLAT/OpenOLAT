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
package org.olat.modules.selectus.model.category;

import org.olat.modules.selectus.model.Category;

/**
 * 
 * Initial date: 16 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationCategoryInfos {
	
	private final Long applicationKey;
	private final Category category;
	private final boolean administrative;
	
	public ApplicationCategoryInfos(Long applicationKey, Category category, boolean administrative) {
		this.applicationKey = applicationKey;
		this.category = category;
		this.administrative = administrative;
	}
	
	public String tagName() {
		if(administrative) {
			return "a:".concat(category.getName());
		}
		return category.getName();
	}

	public Long getApplicationKey() {
		return applicationKey;
	}

	public Category getCategory() {
		return category;
	}
	
	public boolean isAdministrative() {
		return administrative;
	}
}
