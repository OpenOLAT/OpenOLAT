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
	circle_info("alert.box.icon.info", "o_icon_circle_info"),
	lightbulb("alert.box.icon.lightbulb", "o_icon_lightbulb"),
	hand_point_up("alert.box.icon.hand", "o_icon_hand_point_up"),
	circle_exclamation("alert.box.icon.circle.exclamation", "o_icon_circle_exclamation"),
	circle_check("alert.box.icon.check", "o_icon_circle_check"),
	triangle_exclamation("alert.box.icon.triangle.exclamation", "o_icon_triangle_exclamation"),
	circle_xmark("alert.box.icon.xmark", "o_icon_circle_xmark"),
	fire("alert.box.icon.fire", "o_icon_fire"),
	circle_radiation("alert.box.icon.radiation", "o_icon_circle_radiation"),
	flask("alert.box.icon.flask", "o_icon_flask"),
	pencil("alert.box.icon.pencil", "o_icon_pencil"),
	quote_right("alert.box.icon.quote", "o_icon_quote_right"),
	bolt("alert.box.icon.bolt", "o_icon_bolt"),
	bug("alert.box.icon.bug", "o_icon_bug"),
	heart("alert.box.icon.heart", "o_icon_heart"),
	eye("alert.box.icon.eye", "o_icon_eye"),
	globe("alert.box.icon.globe", "o_icon_globe"),
	magnifying_glass("alert.box.icon.magnifying.glass", "o_icon_magnifying_glass"),
	star("alert.box.icon.star", "o_icon_star"),
	bomb("alert.box.icon.bomb", "o_icon_bomb"),
	gear("alert.box.icon.gear", "o_icon_gear"),
	thumbtack("alert.box.icon.thumbtack", "o_icon_thumbtack"),
	compass("alert.box.icon.compass", "o_icon_compass");

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
