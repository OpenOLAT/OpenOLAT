/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.control.generic.dashboard;

import java.util.ArrayList;
import java.util.List;

/**
 * User preferences for a dashboard, stored as XML in GuiPreferences.
 * Contains the ordered list of enabled widget names and the list of disabled widget names.
 *
 * Initial date: Mar 06, 2026<br>
 * @author gnaegi, https://www.frentix.com
 */
public class DashboardPrefs {

	private List<String> enabledWidgets;
	private List<String> disabledWidgets;

	public DashboardPrefs() {
		this.enabledWidgets = new ArrayList<>();
		this.disabledWidgets = new ArrayList<>();
	}

	public DashboardPrefs(List<String> enabledWidgets, List<String> disabledWidgets) {
		this.enabledWidgets = new ArrayList<>(enabledWidgets);
		this.disabledWidgets = new ArrayList<>(disabledWidgets);
	}

	public List<String> getEnabledWidgets() {
		return enabledWidgets != null ? enabledWidgets : List.of();
	}

	public void setEnabledWidgets(List<String> enabledWidgets) {
		this.enabledWidgets = enabledWidgets;
	}

	public List<String> getDisabledWidgets() {
		return disabledWidgets != null ? disabledWidgets : List.of();
	}

	public void setDisabledWidgets(List<String> disabledWidgets) {
		this.disabledWidgets = disabledWidgets;
	}
}
