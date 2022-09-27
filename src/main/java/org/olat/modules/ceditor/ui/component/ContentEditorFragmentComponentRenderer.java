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
package org.olat.modules.ceditor.ui.component;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 6 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentEditorFragmentComponentRenderer extends AbstractContentEditorComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		ContentEditorFragmentComponent cmp = (ContentEditorFragmentComponent)source;
		render(renderer, sb, cmp, ubu, translator, renderResult, args);
	}
	
	private void render(Renderer renderer, StringOutput sb, ContentEditorFragmentComponent cmp, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		if(cmp.isEditMode()) {
			renderEdit(renderer, sb, cmp, ubu, translator, renderResult, args);
		} else {
			renderReadOnly(renderer, sb, cmp, ubu, translator, renderResult, args);
		}
	}
	
	private void renderEdit(Renderer renderer, StringOutput sb, ContentEditorFragmentComponent cmp,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {

		URLBuilder fragmentUbu = ubu.createCopyFor(cmp);
		Renderer fr = Renderer.getInstance(cmp, translator, fragmentUbu, new RenderResult(), renderer.getGlobalSettings(), renderer.getCsrfToken());

		// Container with editor elements
		sb.append("<div id='o_c").append(cmp.getDispatchID()).append("' class='o_page_fragment_edit o_fragment_edited' data-oo-page-fragment='").append(cmp.getComponentName()).append("'>");
		// Tools
		renderTools(sb, cmp, fragmentUbu, translator);

		sb.append("<div id='o_cce").append(cmp.getDispatchID()).append("' data-oo-page-fragment='").append(cmp.getComponentName()).append("'")
		  .append(" data-oo-page-element-id='").append(cmp.getElementId()).append("'")
		  .append(" data-oo-content-editor-url='").append(fr.getUrlBuilder().getJavascriptURI()).append("'")
		  .append(" class='o_page_part")
		  .append("'>");
		Component editorCmp = cmp.getEditorPageElementComponent();
		Component viewCmp = cmp.getViewPageElementComponent();
		if(editorCmp != null) {
			renderPartComponent(renderer, sb, editorCmp, true, fragmentUbu, translator, renderResult, args);
			renderPartComponent(renderer, sb, viewCmp, false, fragmentUbu, translator, renderResult, args);
		} else if(viewCmp != null) {
			renderPartComponent(renderer, sb, viewCmp, true, fragmentUbu, translator, renderResult, args);
		}
		sb.append("</div>");

		renderInspector(renderer, sb, cmp.getInspectorComponent(), fragmentUbu, translator, renderResult, args);
		
		sb.append("<script>\n")
		  .append("\"use strict\";\n")
		  .append("jQuery(function() {\n")
		  .append(" jQuery('.o_page_content_editor').ceditor('editFragment');\n")
		  .append("});\n")
		  .append("</script>");

		sb.append("</div>");
	}
	
	private void renderPartComponent(Renderer renderer, StringOutput sb, Component cmp, boolean visible,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		if(cmp == null) return;
		
		cmp.setVisible(visible);
		if(visible) {
			// Takes control of the wrapper markup
			cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, translator, renderResult, args);
		} else {
			// Reuse the code to mark the element as invisible
			renderer.render(sb, cmp, args);
		}
		cmp.setDirty(false);
	}
	
	private void renderTools(StringOutput sb, ContentEditorFragmentComponent cmp, URLBuilder fragmentUbu, Translator translator) {
		sb.append("<div class='o_page_element_tools'>");

		renderToggleInspector(sb, cmp, fragmentUbu, translator);
		renderDuplicate(sb, cmp, fragmentUbu, translator);
		renderMoreMenu(sb, cmp, fragmentUbu, translator);
		renderDragZone(sb, cmp, translator);
		
		sb.append("</div>");
	}
	
	private void renderReadOnly(Renderer renderer, StringOutput sb, ContentEditorFragmentComponent cmp,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {

		URLBuilder fragmentUbu = ubu.createCopyFor(cmp);
		Renderer fr = Renderer.getInstance(cmp, translator, fragmentUbu, new RenderResult(), renderer.getGlobalSettings(), renderer.getCsrfToken());

		sb.append("<div id='o_c").append(cmp.getDispatchID()).append("' data-oo-page-fragment='").append(cmp.getComponentName()).append("'")
		  .append(" data-oo-page-element-id='").append(cmp.getElementId()).append("'")
		  .append(" data-oo-content-editor-url='").append(fr.getUrlBuilder().getJavascriptURI()).append("'")
		  .append(" class='o_page_part o_page_part_view o_page_drop'>");
		
		Component editorCmp = cmp.getEditorPageElementComponent();
		Component viewCmp = cmp.getViewPageElementComponent();
		if(viewCmp != null) {
			renderPartComponent(renderer, sb, viewCmp, true, fragmentUbu, translator, renderResult, args);
			renderPartComponent(renderer, sb, editorCmp, false, fragmentUbu, translator, renderResult, args);
		} else if(editorCmp != null) {
			renderPartComponent(renderer, sb, editorCmp, true, fragmentUbu, translator, renderResult, args);
		}
		sb.append("</div>");
	}
}
