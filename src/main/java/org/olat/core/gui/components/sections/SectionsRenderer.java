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
import org.olat.core.gui.components.sections.SectionHeaderRenderer.Level;
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
		// Sections never has its own translator assigned by callers; fall back to
		// the renderer's root translator, which is always available.
		Translator headerTranslator = translator != null ? translator : renderer.getTranslator();

		sb.append("<div id=\"").append(rootId).append("\" class=\"o_sections");
		if (StringHelper.containsNonWhitespace(sections.getElementCssClass())) {
			sb.append(" ").append(sections.getElementCssClass());
		}
		sb.append("\">");

		for (Section section : sections.getSections()) {
			renderSection(renderer, sb, rootId, section, headerTranslator, args);
		}

		sb.append("</div>");
	}

	private void renderSection(Renderer renderer, StringOutput sb, String rootId, Section section,
			Translator translator, String[] args) {
		String collapseId = rootId + "_" + section.getId();

		sb.append("<div class=\"o_section\">");
		sb.append("<fieldset>");
		sb.append("<legend>");
		SectionHeaderRenderer.render(sb, collapseId, section.getTitle(), Level.TITLE, true, section.isInitiallyOpen(), translator);
		sb.append("</legend>");
		sb.append("<div id=\"").append(collapseId).append("\" class=\"collapse o_section_content");
		if (section.isInitiallyOpen()) {
			sb.append(" in");
		}
		sb.append("\">");
		renderer.render(section.getContent(), sb, args);
		sb.append("</div>");
		sb.append("</fieldset>");
		sb.append("</div>");
	}

}
