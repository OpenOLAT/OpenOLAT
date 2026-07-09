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
 * Initial date: 01.07.2026<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
package org.olat.course.config.ui;

import java.util.Arrays;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.IconSelectorElement;
import org.olat.core.gui.translator.Translator;

public enum ExternalToolIcon {

	link("external.tool.icon.link", "o_icon_link"),
	linkExtern("external.tool.icon.linkExtern", "o_icon_link_extern"),
	globe("external.tool.icon.globe", "o_icon_globe"),
	mail("external.tool.icon.mail", "o_icon_mail"),
	mailto("external.tool.icon.mailto", "o_icon_mailto"),
	message("external.tool.icon.message", "o_icon_message"),
	comments("external.tool.icon.comments", "o_icon_comments"),
	phone("external.tool.icon.phone", "o_icon_phone"),
	calendar("external.tool.icon.calendar", "o_icon_calendar"),
	calendarDay("external.tool.icon.calendarDay", "o_icon_calendar_day"),
	calendarSync("external.tool.icon.calendarSync", "o_icon_calendar_sync"),
	time("external.tool.icon.time", "o_icon_time"),
	absence("external.tool.icon.absence", "o_icon_absence"),
	home("external.tool.icon.home", "o_icon_home"),
	user("external.tool.icon.user", "o_icon_user"),
	graduate("external.tool.icon.graduate", "o_icon_graduate"),
	group("external.tool.icon.group", "o_icon_group"),
	coach("external.tool.icon.coach", "o_icon_coach"),
	certificate("external.tool.icon.certificate", "o_icon_certificate"),
	booking("external.tool.icon.booking", "o_icon_booking"),
	files("external.tool.icon.files", "o_icon_files"),
	fileLines("external.tool.icon.fileLines", "o_icon_file_lines"),
	legalFolder("external.tool.icon.legalFolder", "o_icon_legal_folder"),
	video("external.tool.icon.video", "o_icon_video"),
	videoPlay("external.tool.icon.videoPlay", "o_icon_video_play"),
	news("external.tool.icon.news", "o_icon_news"),
	qrcode("external.tool.icon.qrcode", "o_icon_qrcode"),
	userAuthentication("external.tool.icon.userAuthentication", "o_icon_user_authentication"),
	database("external.tool.icon.database", "o_icon_database"),
	chartSimple("external.tool.icon.chartSimple", "o_icon_chart_simple"),
	diagramProject("external.tool.icon.diagramProject", "o_icon_diagram_project");

	private final String i18nKey;
	private final String cssClass;

	ExternalToolIcon(String i18nKey, String cssClass) {
		this.i18nKey = i18nKey;
		this.cssClass = cssClass;
	}

	public String getI18nKey() {
		return i18nKey;
	}

	public String getCssClass() {
		return cssClass;
	}

	public static List<IconSelectorElement.Icon> getIcons(Translator translator) {
		return Arrays.stream(ExternalToolIcon.values()).map(i ->
			new IconSelectorElement.Icon(i.name(),
					translator.translate(i.getI18nKey()) + " (" + i.getCssClass() + ")",
					i.getCssClass())
		).toList();
	}

	public static ExternalToolIcon forCssClass(String cssClass) {
		for (ExternalToolIcon icon : values()) {
			if (icon.cssClass.equals(cssClass)) {
				return icon;
			}
		}
		return linkExtern;
	}
}
