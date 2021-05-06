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

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
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
			
			// prevent dirty components which are not linked to the DOM tree
			List<Link> additionalTools = cmp.getAdditionalTools();
			if(additionalTools != null && !additionalTools.isEmpty()) {
				for(Link additionalTool:additionalTools) {
					additionalTool.setDirty(false);
				}
			}
		}
	}
	
	private void renderEdit(Renderer renderer, StringOutput sb, ContentEditorFragmentComponent cmp,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {

		URLBuilder fragmentUbu = ubu.createCopyFor(cmp);
		Renderer fr = Renderer.getInstance(cmp, translator, fragmentUbu, new RenderResult(), renderer.getGlobalSettings(), renderer.getCsrfToken());

		sb.append("<div id='o_c").append(cmp.getDispatchID()).append("' class='o_page_fragment_edit' data-oo-page-fragment='").append(cmp.getComponentName()).append("'>");

		renderAboveTools(renderer, sb, cmp, fragmentUbu, translator, renderResult, args);
		renderPageUpDown(sb, cmp, fragmentUbu, translator);

		sb.append("<div id='o_cce").append(cmp.getDispatchID()).append("' data-oo-page-fragment='").append(cmp.getComponentName()).append("'")
		  .append(" data-oo-page-element-id='").append(cmp.getElementId()).append("'")
		  .append(" data-oo-content-editor-url='").append(fr.getUrlBuilder().getJavascriptURI()).append("'")
		  .append(" class='o_page_part o_page_edit'>");

		Component subCmp = cmp.getPageElementComponent();
		subCmp.getHTMLRendererSingleton().render(fr, sb, subCmp, fragmentUbu, translator, renderResult, args);
		subCmp.setDirty(false);
		sb.append("</div>");
		
		renderAddBelow(sb, cmp, fragmentUbu, translator);
		
		sb.append("</div>");
	}
	
	private void renderAboveTools(Renderer renderer, StringOutput sb, ContentEditorFragmentComponent cmp, URLBuilder fragmentUbu,
			Translator translator, RenderResult renderResult, String[] args) {
		sb.append("<div class='o_page_tools_above clearfix'>");
		
		renderAddAbove(sb, cmp, fragmentUbu, translator);
		
		sb.append("<div class='o_page_others_above'>");

		renderClose(sb, cmp, fragmentUbu, translator);
		renderDelete(sb, cmp, fragmentUbu, translator);
		renderDuplicate(sb, cmp, fragmentUbu, translator);
		
		List<Link> additionalTools = cmp.getAdditionalTools();
		if(additionalTools != null && !additionalTools.isEmpty()) {
			for(Link additionalTool:additionalTools) {
				Renderer fr = Renderer.getInstance(cmp, translator, fragmentUbu, new RenderResult(), renderer.getGlobalSettings(), renderer.getCsrfToken());
				URLBuilder aubu = fragmentUbu.createCopyFor(additionalTool);
				additionalTool.getHTMLRendererSingleton().render(fr, sb, additionalTool, aubu, translator, renderResult, args);
				additionalTool.setDirty(false);
			}
		}
		
		sb.append("</div>") // o_page_others_above
		  .append("</div>");// o_page_tools_above
	}
	
	private void renderReadOnly(Renderer renderer, StringOutput sb, ContentEditorFragmentComponent cmp,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {

		URLBuilder fragmentUbu = ubu.createCopyFor(cmp);
		Renderer fr = Renderer.getInstance(cmp, translator, fragmentUbu, new RenderResult(), renderer.getGlobalSettings(), renderer.getCsrfToken());

		sb.append("<div id='o_c").append(cmp.getDispatchID()).append("' data-oo-page-fragment='").append(cmp.getComponentName()).append("'")
		  .append(" data-oo-page-element-id='").append(cmp.getElementId()).append("'")
		  .append(" data-oo-content-editor-url='").append(fr.getUrlBuilder().getJavascriptURI()).append("'")
		  .append(" class='o_page_part o_page_part_view o_page_drop'>");
		Component subCmp = cmp.getPageElementComponent();
		subCmp.getHTMLRendererSingleton().render(fr, sb, subCmp, fragmentUbu, translator, renderResult, args);
		subCmp.setDirty(false);
		sb.append("</div>");
	}
}
