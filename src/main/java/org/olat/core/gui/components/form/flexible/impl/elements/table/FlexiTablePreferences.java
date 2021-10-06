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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.io.Serializable;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;

/**
 * 
 * Initial date: 27.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTablePreferences implements Serializable {

	private static final long serialVersionUID = 220256298006571339L;

	private int pageSize;
	private boolean sortDirection;
	private String sortedColumnKey;
	private List<String> enabledColumnKey;
	private List<FlexiFiltersTab> customTabs;
	private FlexiTableRendererType rendererType;
	
	public FlexiTablePreferences(int pageSize, String sortedColumnKey, boolean sortDirection,
			List<String> enabledColumnKey, List<FlexiFiltersTab> customTabs,
			FlexiTableRendererType rendererType) {
		this.pageSize = pageSize;
		this.sortedColumnKey = sortedColumnKey;
		this.sortDirection = sortDirection;
		this.enabledColumnKey = enabledColumnKey;
		this.customTabs = customTabs;
		this.rendererType = rendererType;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public boolean isSortDirection() {
		return sortDirection;
	}

	public void setSortDirection(boolean sortDirection) {
		this.sortDirection = sortDirection;
	}

	public String getSortedColumnKey() {
		return sortedColumnKey;
	}

	public void setSortedColumnKey(String sortedColumnKey) {
		this.sortedColumnKey = sortedColumnKey;
	}

	public List<String> getEnabledColumnKeys() {
		return enabledColumnKey;
	}

	public void setEnabledColumnKeys(List<String> enabledColumnKey) {
		this.enabledColumnKey = enabledColumnKey;
	}
	
	public List<FlexiFiltersTab> getCustomTabs() {
		return customTabs;
	}

	public FlexiTableRendererType getRendererType() {
		return rendererType;
	}

	public void setRendererType(FlexiTableRendererType rendererType) {
		this.rendererType = rendererType;
	}
}
