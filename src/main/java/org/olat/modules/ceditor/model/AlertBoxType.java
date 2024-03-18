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

import org.olat.core.util.StringHelper;

/**
 * Initial date: 2024-02-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum AlertBoxType {
	note("alert.box.type.note", "lightgray", "o_alert_box_type_note", "o_icon_lightbulb"),
	info("alert.box.type.info", "lightblue", "o_alert_box_type_info", "o_icon_circle_info"),
	tip("alert.box.type.tip", "purple", "o_alert_box_type_tip", "o_icon_hand_point_up"),
	important("alert.box.type.important", "lightblue", "o_alert_box_type_important", "o_icon_circle_exclamation"),
	warning("alert.box.type.warning", "yellow", "o_alert_box_type_warning", "o_icon_triangle_exclamation"),
	error("alert.box.type.error", "red", "o_alert_box_type_error", "o_icon_circle_xmark"),
	success("alert.box.type.success", "lightgreen", "o_alert_box_type_success", "o_icon_circle_check"),
	custom("alert.box.type.custom", null, "o_alert_box_type_custom", null);

	private final String i18nKey;
	private final String color;
	private final String cssClass;
	private final String iconCssClass;

	AlertBoxType(String i18nKey, String color, String cssClass, String iconCssClass) {
		this.i18nKey = i18nKey;
		this.color = color;
		this.cssClass = cssClass;
		this.iconCssClass = iconCssClass;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public String getColor() {
		return color;
	}

	public String getCssClass(String color) {
		if (StringHelper.containsNonWhitespace(color) && this.equals(custom)) {
			return cssClass + " o_alert_box_color_" + color;
		}
		return cssClass;
	}

	public String getIconCssClass() {
		return iconCssClass;
	}
}
