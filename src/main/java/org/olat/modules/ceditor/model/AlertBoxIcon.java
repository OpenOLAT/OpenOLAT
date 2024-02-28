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

/**
 * Initial date: 2024-02-26<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum AlertBoxIcon {
	radiation("alert.box.icon.radiation", "o_icon_radiation"),
	error("alert.box.icon.error", "o_icon_error_alert"),
	warning("alert.box.icon.warning", "o_icon_warning"),
	info("alert.box.icon.info", "o_icon_info_alert"),
	note("alert.box.icon.note", "o_icon_notes"),
	tip("alert.box.icon.tip", "o_icon_tip"),
	check("alert.box.icon.check", "o_icon_check");

	private final String i18nKey;
	private final String cssClass;

	AlertBoxIcon(String i18nKey, String cssClass) {
		this.i18nKey = i18nKey;
		this.cssClass = cssClass;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public String getCssClass() {
		return cssClass;
	}
}
