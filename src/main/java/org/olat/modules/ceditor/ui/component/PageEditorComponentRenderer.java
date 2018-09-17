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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.model.ContainerColumn;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.ui.model.EditorFragment;

/**
 * 
 * Initial date: 11 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditorComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		PageEditorComponent cmp = (PageEditorComponent)source;

		Renderer fr = Renderer.getInstance(cmp, translator, ubu, new RenderResult(), renderer.getGlobalSettings());
		sb.append("<div id='o_c").append(cmp.getDispatchID()).append("' class='o_page_content_editor' data-oo-content-editor-url='")
		  .append(fr.getUrlBuilder().getJavascriptURI()).append("'>");
		renderFlatFragments(fr, sb, cmp, ubu, translator, renderResult, args);
		sb.append("</div>");
		
		renderEditJavascript(fr, sb, cmp);
	}
	
	private void renderFlatFragments(Renderer renderer, StringOutput sb, PageEditorComponent cmp,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		
		List<EditorFragment> fragments = cmp.getModel().getFragments();
		Map<String, EditorFragment> elementIdToFragments = fragments.stream().collect(Collectors
				.toMap(EditorFragment::getElementId, f -> f, (u,v) -> u));
		
		Set<EditorFragment> containerized = new HashSet<>();
		for(EditorFragment fragment:fragments) {
			if(fragment.getPageElement() instanceof ContainerElement) {
				ContainerElement container = (ContainerElement)fragment.getPageElement();
				List<String> allElementIds = container.getContainerSettings().getAllElementIds();
				for(String elementId:allElementIds) {
					EditorFragment containerizedFragment = elementIdToFragments.get(elementId);
					if(containerizedFragment != null) {
						containerized.add(containerizedFragment);
					}
				}
			}
		}

		for(EditorFragment fragment:fragments) {
			if(!containerized.contains(fragment)) {
				render(renderer, sb, cmp, fragment, elementIdToFragments, ubu, translator, renderResult, args);
			}
		}
	}
	
	private void render(Renderer renderer, StringOutput sb, PageEditorComponent cmp, EditorFragment fragment,
			Map<String, EditorFragment> elementIdToFragments, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		PageElement element  = fragment.getPageElement();
		if(!elementIdToFragments.containsKey(element.getId())) {
			return;// already rendered fragments
		}
		elementIdToFragments.remove(element.getId());

		if(fragment.isEditMode()) {
			renderEdit(renderer, sb, cmp, fragment, elementIdToFragments, ubu, translator, renderResult, args);
		} else {
			renderReadOnly(renderer, sb, cmp, fragment, elementIdToFragments, ubu, translator, renderResult, args);
		}
	}
	
	private void renderEdit(Renderer renderer, StringOutput sb, PageEditorComponent cmp, EditorFragment fragment,
			Map<String, EditorFragment> elementIdToFragments, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		sb.append("<div class='o_page_fragment_edit' data-oo-page-fragment='").append(fragment.getComponentName()).append("'>");

		renderAboveTools(renderer, sb, cmp, fragment, ubu, translator, renderResult, args);
		renderPageUpDown(renderer, sb, fragment, ubu, translator, renderResult, args);
		
		sb.append("<div id='oce_").append(fragment.getElementId()).append("' data-oo-page-fragment='").append(fragment.getComponentName()).append("'  class='o_page_part o_page_edit");
		if(fragment.getPageElement() instanceof ContainerElement) {
			sb.append(" clearfix'>");
			ContainerElement container = (ContainerElement)fragment.getPageElement();
			renderContainer(renderer, sb, cmp, container, elementIdToFragments, ubu, translator, renderResult, args);
		} else {
			sb.append("'>");
			Component subCmp = fragment.getComponent();
			subCmp.getHTMLRendererSingleton().render(renderer, sb, subCmp, ubu, translator, renderResult, args);
			subCmp.setDirty(false);
		}
		sb.append("</div>");
		
		// add below
		renderBelowTools(renderer, sb, fragment, ubu, translator, renderResult, args);
		
		sb.append("</div>");
	}

	private void renderAboveTools(Renderer renderer, StringOutput sb, PageEditorComponent cmp, EditorFragment fragment,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		sb.append("<div class='o_page_tools_above clearfix'>");
		
		if(fragment.getAddElementAboveLink() != null && fragment.getAddElementAboveLink().isVisible()) {
			sb.append("<div class='o_page_add_above'>");
			Component aboveLink = fragment.getAddElementAboveLink();
			aboveLink.getHTMLRendererSingleton().render(renderer, sb, aboveLink, ubu, translator, renderResult, args);
			aboveLink.setDirty(false);
			sb.append("</div>");
		}

		sb.append("<div class='o_page_others_above'>")
		  .append("<span class='o_page_type'><i class='o_icon $fragment.typeCssClass'> </i> $r.translate($fragment.type)</span>");
		
		Component saveLink = fragment.getSaveLink();
		saveLink.getHTMLRendererSingleton().render(renderer, sb, saveLink, ubu, translator, renderResult, args);
		saveLink.setDirty(false);
		
		Component deleteLink = fragment.getDeleteLink();
		if(deleteLink != null && deleteLink.isVisible()) {
			deleteLink.getHTMLRendererSingleton().render(renderer, sb, deleteLink, ubu, translator, renderResult, args);
			deleteLink.setDirty(false);
		}
		
		List<Link> additionalTools = fragment.getAdditionalTools();
		if(additionalTools != null && !additionalTools.isEmpty()) {
			for(Link additionalTool:additionalTools) {
				ComponentCollection col = (ComponentCollection)fragment.getComponent();
				Renderer fr = Renderer.getInstance(col, translator, ubu, new RenderResult(), renderer.getGlobalSettings());
				URLBuilder aubu = ubu.createCopyFor(additionalTool);
				additionalTool.getHTMLRendererSingleton().render(fr, sb, additionalTool, aubu, translator, renderResult, args);
				additionalTool.setDirty(false);
			}
		} else if(fragment.getPageElement() instanceof ContainerElement) {
			renderContainerColumnLinks(renderer, sb, cmp, (ContainerElement)fragment.getPageElement(), ubu, translator, renderResult, args);
		}

		sb.append("</div>") // o_page_others_above
		  .append("</div>");// o_page_tools_above
	}
	
	private void renderContainerColumnLinks(Renderer renderer, StringOutput sb, PageEditorComponent cmp,
			ContainerElement container, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		ContainerSettings settings = container.getContainerSettings();
		
		int numOfColumns = settings.getNumOfColumns();
		renderColumnLink(renderer, sb, cmp.getContainer4Columns(), numOfColumns == 4, ubu, translator, renderResult, args);
		renderColumnLink(renderer, sb, cmp.getContainer3Columns(), numOfColumns == 3, ubu, translator, renderResult, args);
		renderColumnLink(renderer, sb, cmp.getContainer2Columns(), numOfColumns == 2, ubu, translator, renderResult, args);
	}
	
	private void renderColumnLink(Renderer renderer, StringOutput sb, Link link, boolean selected,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		if(selected) {
			link.setIconLeftCSS("o_icon o_icon_check");
		} else {
			link.setIconLeftCSS("o_icon o_icon_columns");
		}
		URLBuilder aubu = ubu.createCopyFor(link);
		link.getHTMLRendererSingleton().render(renderer, sb, link, aubu, translator, renderResult, args);
		link.setDirty(false);
	}

	
	private void renderPageUpDown(Renderer renderer, StringOutput sb, EditorFragment fragment,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		sb.append("<div class='o_page_tools_dd'>");
		if(fragment.getMoveUpLink() != null && fragment.getMoveUpLink().isVisible()) {
			Component moveUpLink = fragment.getMoveUpLink();
			moveUpLink.getHTMLRendererSingleton().render(renderer, sb, moveUpLink, ubu, translator, renderResult, args);
			moveUpLink.setDirty(false);
		}
		if(fragment.getMoveDownLink() != null && fragment.getMoveDownLink().isVisible()) {
			Component moveDownLink = fragment.getMoveDownLink();
			moveDownLink.getHTMLRendererSingleton().render(renderer, sb, moveDownLink, ubu, translator, renderResult, args);
			moveDownLink.setDirty(false);
		}
		sb.append("</div>");
	}
	
	private void renderBelowTools(Renderer renderer, StringOutput sb, EditorFragment fragment,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		if(fragment.getAddElementBelowLink().isVisible()) {
			sb.append("<div class='o_page_add_below'>");
			Component addBelowLink = fragment.getAddElementBelowLink();
			addBelowLink.getHTMLRendererSingleton().render(renderer, sb, addBelowLink, ubu, translator, renderResult, args);
			addBelowLink.setDirty(false);
			sb.append("</div>");
		}
	}
	
	private void renderReadOnly(Renderer renderer, StringOutput sb, PageEditorComponent cmp, EditorFragment fragment,
			Map<String, EditorFragment> elementIdToFragments, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		PageElement element  = fragment.getPageElement();
		sb.append("<div id='oce_").append(fragment.getElementId()).append("' data-oo-page-fragment='").append(fragment.getComponentName()).append("'")
		  .append(" class='o_page_part ");
		if(element instanceof ContainerElement) {
			sb.append("o_page_container clearfix'>");
			ContainerElement container = (ContainerElement)element;
			renderContainer(renderer, sb, cmp, container, elementIdToFragments, ubu, translator, renderResult, args);
		} else {
			sb.append(" o_page_drop", fragment.isDroppable()).append("'>");
			Component subCmp = fragment.getComponent();
			subCmp.getHTMLRendererSingleton().render(renderer, sb, subCmp, ubu, translator, renderResult, args);
			subCmp.setDirty(false);
		}
		fragment.getComponent().setDirty(false);
		sb.append("</div>");
	}
	
	private void renderContainer(Renderer renderer, StringOutput sb, PageEditorComponent cmp, ContainerElement container, Map<String, EditorFragment> elementIdToFragments,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		ContainerSettings settings =  container.getContainerSettings();
		List<ContainerColumn> columns = settings.getColumns();
		int numOfColumns = settings.getNumOfColumns();
		List<String> cssColumns = ContainerCSSColumns.getCssColumns(numOfColumns);
		for(int i=0; i<numOfColumns; i++) {
			sb.append("<div id='occ_").append(container.getId()).append("_").append(i).append("' class='")
			  .append(cssColumns.get(i)).append(" o_page_container_slot o_page_drop' data-oo-slot='").append(i).append("'>");
			if(columns != null && i < columns.size()) {
				ContainerColumn column = columns.get(i);
				for(String elementId:column.getElementIds()) {
					EditorFragment fragment = elementIdToFragments.get(elementId);
					if(fragment != null) {
						render(renderer, sb, cmp, fragment,  elementIdToFragments, ubu, translator, renderResult, args);
					}
				}
			}
			sb.append("</div>");
		}	
	}

	private void renderEditJavascript(Renderer renderer, StringOutput sb, PageEditorComponent cmp) {
		sb.append("<script>\n")
		  .append("/* <![CDATA[ */\n")
		  .append("jQuery(function() {\n")
		  .append(" jQuery('#o_c").append(cmp.getDispatchID()).append("').ceditor({\n")
		  .append("  componentUrl: '").append(renderer.getUrlBuilder().getJavascriptURI()).append("'\n")
		  .append(" });\n")
		  .append("});\n")
		  .append("/* ]]> */\n")
		  .append("</script>");
	}
}
