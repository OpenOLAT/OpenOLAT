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
package org.olat.modules.selectus.ui.position.model;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;

import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;

/**
 * 
 * Initial date: 12 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditStepRow {
	
	private final Tab step;
	private final String name;
	private String customName;
	private String customNameDe;
	private String customNameFr;
	
	private final String explain;
	private boolean enabled;
	private boolean configurable;
	private boolean staffOnly;
	private boolean deleted;
	
	private TextElement titleEl;
	private FormLink editLabelButton;
	
	public EditStepRow(Tab step, String name, String explain, boolean enabled, boolean configurable) {
		this(step, name, explain, enabled, configurable, false);
	}
	
	public EditStepRow(Tab step, String name, String explain, boolean enabled, boolean configurable, boolean staffOnly) {
		this.step = step;
		this.name = name;
		this.explain = explain;
		this.enabled = enabled;
		this.staffOnly = staffOnly;
		this.configurable = configurable;
	}
	
	public Tab step() {
		return step;
	}
	
	public String getName() {
		return name;
	}

	public String getExplain() {
		return explain;
	}
	
	public String getCustomName(Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			return getCustomNameDe();
		}
		if(locale != null && locale.getLanguage().equals("fr")) {
			return getCustomNameFr();
		}
		return getCustomName();
	}
	
	public void setCustomName(String text, Locale locale) {
		if(locale != null && locale.getLanguage().equals("de")) {
			setCustomNameDe(text);
		} else if(locale != null && locale.getLanguage().equals("fr")) {
			setCustomNameFr(text);
		} else {
			setCustomName(text);
		}
	}
	
	public String getCustomNameDe() {
		return customNameDe;
	}

	public void setCustomNameDe(String customNameDe) {
		this.customNameDe = customNameDe;
	}
	
	public String getCustomNameFr() {
		return customNameFr;
	}

	public void setCustomNameFr(String customNameFr) {
		this.customNameFr = customNameFr;
	}

	public String getCustomName() {
		return customName;
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isConfigurable() {
		return configurable;
	}
	
	public boolean isStaffOnly() {
		return staffOnly;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public TextElement getTitleEl() {
		return titleEl;
	}

	public void setTitleEl(TextElement titleEl) {
		this.titleEl = titleEl;
	}

	public FormLink getEditLabelButton() {
		return editLabelButton;
	}

	public void setEditLabelButton(FormLink editLabelButton) {
		this.editLabelButton = editLabelButton;
	}



	public enum StepStatus {
		enabled,
		disabled,
		staffOnly
	}
}
