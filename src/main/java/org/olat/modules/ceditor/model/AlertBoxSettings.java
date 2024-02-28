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
package org.olat.modules.ceditor.model;

import java.beans.Transient;

/**
 * Initial date: 2024-02-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AlertBoxSettings {

	private boolean showAlertBox;
	private AlertBoxType type;
	private String title;
	private boolean withIcon;
	private boolean collapsible;
	private AlertBoxIcon icon;
	private String color;

	public boolean isShowAlertBox() {
		return showAlertBox;
	}

	public void setShowAlertBox(boolean showAlertBox) {
		this.showAlertBox = showAlertBox;
	}

	public AlertBoxType getType() {
		return type;
	}

	public void setType(AlertBoxType type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isWithIcon() {
		return withIcon;
	}

	public void setWithIcon(boolean withIcon) {
		this.withIcon = withIcon;
	}

	public boolean isCollapsible() {
		return collapsible;
	}

	public void setCollapsible(boolean collapsible) {
		this.collapsible = collapsible;
	}

	public AlertBoxIcon getIcon() {
		return icon;
	}

	public void setIcon(AlertBoxIcon icon) {
		this.icon = icon;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public static AlertBoxSettings getPredefined() {
		AlertBoxSettings alertBoxSettings = new AlertBoxSettings();
		alertBoxSettings.setShowAlertBox(false);
		alertBoxSettings.setWithIcon(true);
		alertBoxSettings.setCollapsible(false);
		alertBoxSettings.setType(AlertBoxType.note);
		return alertBoxSettings;
	}

	@Transient
	public String getIconCssClass() {
		if (type == null) {
			return null;
		}
		if (!type.equals(AlertBoxType.custom)) {
			return type.getIconCssClass();
		} else {
			return getIcon() != null ? getIcon().getCssClass() : null;
		}
	}
}
