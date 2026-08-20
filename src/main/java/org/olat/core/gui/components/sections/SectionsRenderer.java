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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SectionsRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		Sections sections = (Sections)source;
		if (sections.getSections().isEmpty()) {
			return;
		}

		String rootId = "o_sections_" + sections.getDispatchID();

		sb.append("<div id=\"").append(rootId).append("\" class=\"o_sections");
		if (StringHelper.containsNonWhitespace(sections.getElementCssClass())) {
			sb.append(" ").append(sections.getElementCssClass());
		}
		sb.append("\">");

		for (Section section : sections.getSections()) {
			renderSection(renderer, sb, rootId, section, args);
		}

		sb.append("</div>");

		renderScript(sb, rootId);
	}

	private void renderSection(Renderer renderer, StringOutput sb, String rootId, Section section, String[] args) {
		String collapseId = rootId + "_" + section.getId();

		sb.append("<div class=\"o_section\">");
		sb.append("<fieldset><legend>");
		sb.append("<h4 class=\"o_section_toggle\" data-target=\"").append(collapseId).append("\" tabindex=\"0\" role=\"button\"");
		sb.append(" aria-controls=\"").append(collapseId).append("\"");
		sb.append(" aria-expanded=\"").append(section.isInitiallyOpen()).append("\">");
		sb.append("<i id=\"").append(collapseId).append("_toggler\" aria-hidden=\"true\" class=\"o_icon o_icon-fw ");
		sb.append(section.isInitiallyOpen() ? "o_icon_close_togglebox" : "o_icon_open_togglebox");
		sb.append("\"> </i> ");
		sb.append(StringHelper.escapeHtml(section.getTitle()));
		sb.append("</h4>");
		sb.append("</legend></fieldset>");
		sb.append("<div id=\"").append(collapseId).append("\" class=\"collapse");
		if (section.isInitiallyOpen()) {
			sb.append(" in");
		}
		sb.append("\">");
		renderer.render(section.getContent(), sb, args);
		sb.append("</div>");
		sb.append("</div>");
	}

	private void renderScript(StringOutput sb, String rootId) {
		sb.append("<script>")
			.append("\"use strict\";")
			.append("jQuery(function() {")
			.append("var root = jQuery('#").append(rootId).append("');")
			.append("root.off('.oSections').on('click.oSections keydown.oSections', '.o_section_toggle', function(event) {")
			.append("if (event.type === 'keydown') { triggerClick(event, true, true); return; }")
			.append("jQuery('#' + jQuery(this).data('target')).collapse('toggle');")
			.append("});")
			.append("root.off('.oSectionsCollapse').on('hide.bs.collapse.oSectionsCollapse show.bs.collapse.oSectionsCollapse', '.collapse', function(e) {")
			.append("var toggler = jQuery('#' + e.target.id + '_toggler');")
			.append("var toggle = jQuery('[data-target=\"' + e.target.id + '\"]');")
			.append("if (e.type === 'hide') { toggler.removeClass('o_icon_close_togglebox').addClass('o_icon_open_togglebox'); toggle.attr('aria-expanded', 'false'); }")
			.append("else { toggler.removeClass('o_icon_open_togglebox').addClass('o_icon_close_togglebox'); toggle.attr('aria-expanded', 'true'); }")
			.append("});")
			.append("});")
			.append("</script>");
	}

}
