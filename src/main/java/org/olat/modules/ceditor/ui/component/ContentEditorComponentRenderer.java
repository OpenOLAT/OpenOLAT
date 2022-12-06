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
public class ContentEditorComponentRenderer extends AbstractContentEditorComponentRenderer {
	
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		ContentEditorComponent cmp = (ContentEditorComponent)source;

		Renderer fr = Renderer.getInstance(cmp, translator, ubu, new RenderResult(), renderer.getGlobalSettings(), renderer.getCsrfToken());
		sb.append("<div id='o_c").append(cmp.getDispatchID()).append("' class='o_page_content_editor' data-oo-content-editor-url='")
		  .append(fr.getUrlBuilder().getJavascriptURI()).append("'>");
		
		boolean implicitContainer = false;
		
		for(Component subCmp:cmp.getComponents()) {
			if(!(subCmp instanceof ContentEditorContainerComponent) && !implicitContainer) {
				sb.append("<div class='o_legacy_container' data-oo-content-editor-url='")
				  .append(fr.getUrlBuilder().getJavascriptURI()).append("'>");
				implicitContainer = true;
			} else if((subCmp instanceof ContentEditorContainerComponent) && implicitContainer) {
				sb.append("</div>");
				implicitContainer = false;
			}
			
			subCmp.getHTMLRendererSingleton().render(renderer, sb, subCmp, ubu, translator, renderResult, args);
			subCmp.setDirty(false);
		}
		
		if(implicitContainer) {
			sb.append("</div>");
		}
		
		renderEditJavascript(fr, sb, cmp);
		sb.append("</div>");
	}
	
	private void renderEditJavascript(Renderer renderer, StringOutput sb, ContentEditorComponent cmp) {
		sb.append("<script>\n")
		  .append("\"use strict\";\n")
		  .append("jQuery(function() {\n")
		  .append(" jQuery('#o_c").append(cmp.getDispatchID()).append("').ceditor({\n")
		  .append("  componentUrl: '").append(renderer.getUrlBuilder().getJavascriptURI()).append("',\n")
		  .append("  csrfToken: '").append(renderer.getCsrfToken()).append("'\n")
		  .append(" });\n")
		  .append("});\n")
		  .append("</script>");
	}
}
