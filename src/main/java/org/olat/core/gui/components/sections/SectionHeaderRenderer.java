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
package org.olat.core.gui.components.sections;

import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;

/**
 * Renders the header row of a titled, optionally collapsible section: the
 * title in the requested heading style, and, if collapsible, a focusable
 * toggle carrying the a11y attributes and icon that {@code sections.js}
 * expects.
 *
 * Shared between {@link SectionsRenderer} (display side, {@link Sections})
 * and the form side ({@code FormSection}), so both render identical markup
 * and behave identically.
 *
 * Initial date: 2026-09-10<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SectionHeaderRenderer {

	public enum Level {
		TITLE,
		SUB_TITLE
	}

	private SectionHeaderRenderer() {
		//
	}

	/**
	 * @param sb the target
	 * @param id the id of the collapsible content element this header controls (used for aria-controls / data-target)
	 * @param title the already translated title
	 * @param level heading style
	 * @param collapsible whether the header is a toggle or plain text
	 * @param expanded initial state, ignored if not collapsible
	 * @param translator used to translate the sr-only toggle label (core keys details.expand / details.collapse)
	 */
	public static void render(StringOutput sb, String id, String title, Level level,
			boolean collapsible, boolean expanded, Translator translator) {
		String tag = level == Level.SUB_TITLE ? "div" : "h4";
		String titleCssClass = level == Level.SUB_TITLE ? "o_section_sub_title" : "o_section_title";

		sb.append("<").append(tag).append(" class=\"").append(titleCssClass);
		if (collapsible) {
			String labelExpand = translator.translate("details.expand");
			String labelCollapse = translator.translate("details.collapse");
			sb.append(" o_section_toggle o_link_plain").append(expanded ? "" : " collapsed")
			  .append("\" id=\"").append(id).append("_toggle\" role=\"button\" tabindex=\"0\"")
			  .append(" data-target=\"").append(id).append("\"")
			  .append(" data-label-expand=\"").appendHtmlAttributeEscaped(labelExpand).append("\"")
			  .append(" data-label-collapse=\"").appendHtmlAttributeEscaped(labelCollapse).append("\"")
			  .append(" aria-controls=\"").append(id).append("\"")
			  .append(" aria-expanded=\"").append(Boolean.toString(expanded)).append("\">");
		} else {
			sb.append("\">");
		}

		sb.append("<span class=\"o_section_title_text\">");
		if (collapsible) {
			sb.append("<i id=\"").append(id).append("_toggler\" aria-hidden=\"true\" class=\"o_icon o_icon-fw ")
			  .append(expanded ? "o_icon_close_togglebox" : "o_icon_open_togglebox").append("\"> </i>");
		}
		sb.appendHtmlEscaped(title);
		if (collapsible) {
			sb.append(" <span id=\"").append(id).append("_togglerLabel\" class=\"sr-only\">")
			  .appendHtmlEscaped(expanded ? translator.translate("details.collapse") : translator.translate("details.expand"))
			  .append("</span>");
		}
		sb.append("</span>");
		sb.append("</").append(tag).append(">");
	}

}
