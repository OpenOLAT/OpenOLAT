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
package org.olat.repository.ui.settings;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 1 Nov 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReloadSettingsEvent extends Event {

	private static final long serialVersionUID = -6750322705437476311L;
	public static final String RELOAD_SETTINGS = "reload-settings";
	
	private final boolean changedTitle;
	private final boolean changedToolbar;
	private final boolean changedToolsMenu;
	
	public ReloadSettingsEvent() {
		this(false, false, false);
	}

	public ReloadSettingsEvent(boolean changedToolsMenu, boolean changedToolbar, boolean changedTitle) {
		super(RELOAD_SETTINGS);
		this.changedTitle = changedTitle;
		this.changedToolbar = changedToolbar;
		this.changedToolsMenu = changedToolsMenu;
	}

	public boolean isChangedTitle() {
		return changedTitle;
	}

	public boolean isChangedToolsMenu() {
		return changedToolsMenu;
	}
	
	public boolean isChangedToolbar() {
		return changedToolbar;
	}
}
