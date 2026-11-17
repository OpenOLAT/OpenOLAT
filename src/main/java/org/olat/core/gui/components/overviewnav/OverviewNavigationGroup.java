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
package org.olat.core.gui.components.overviewnav;

import java.util.List;

/**
 *
 * Initial date: 16 Nov 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OverviewNavigationGroup {
	
	private String heading;
	private List<OverviewNavigationItem> items = List.of();
	private OverviewNavigation navigation;
	
	public String getHeading() {
		return heading;
	}
	
	public void setHeading(String heading) {
		this.heading = heading;
		setDirty();
	}
	
	public List<OverviewNavigationItem> getItems() {
		return items;
	}
	
	public void setItems(List<OverviewNavigationItem> items) {
		this.items = items == null ? List.of() : items;
		for (OverviewNavigationItem item : this.items) {
			item.setNavigation(navigation);
		}
		setDirty();
	}
	
	void setNavigation(OverviewNavigation navigation) {
		this.navigation = navigation;
		for (OverviewNavigationItem item : items) {
			item.setNavigation(navigation);
		}
	}
	
	private void setDirty() {
		if (navigation != null) {
			navigation.itemsChanged();
		}
	}
	
	public boolean isVisible() {
		return items.stream().anyMatch(item -> item.getLink().isVisible());
	}
	
}
