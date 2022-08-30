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
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.model.ContainerColumn;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.ui.PageEditorUIFactory;

/**
 * 
 * Initial date: 6 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentEditorContainerComponentRenderer extends AbstractContentEditorComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		ContentEditorContainerComponent cmp = (ContentEditorContainerComponent)source;
		URLBuilder containerUbu = ubu.createCopyFor(cmp);
	
		renderContainerWrapper(renderer, sb, cmp, containerUbu, translator, renderResult, args);
	}
	
	private void renderContainerWrapper(Renderer renderer, StringOutput sb, ContentEditorContainerComponent cmp, URLBuilder containerUbu,
			Translator translator, RenderResult renderResult, String[] args) {

		Renderer fr = Renderer.getInstance(cmp, translator, containerUbu, new RenderResult(), renderer.getGlobalSettings(), renderer.getCsrfToken());
		ContainerSettings settings = cmp.getContainerSettings();
		
		// Container with editor elements
		sb.append("<div id='o_c").append(cmp.getDispatchID()).append("'")
		  .append(" data-oo-page-fragment='").append(cmp.getComponentName()).append("'")
		  .append(" data-oo-page-element-id='").append(cmp.getElementId()).append("'")
		  .append(" data-oo-content-editor-url='").append(fr.getUrlBuilder().getJavascriptURI()).append("'")
		  .append(" class='o_page_fragment_edit o_page_container_edit o_page_drop ").append(" o_fragment_edited ", cmp.isEditMode()).append("'>");
		// Tools
		renderTools(fr, sb, cmp, containerUbu, translator);
		
		// Container only
		sb.append("<div id='oce_").append(cmp.getElementId()).append("'")
		  .append(" data-oo-page-fragment='").append(cmp.getComponentName()).append("'")
		  .append(" data-oo-page-element-id='").append(cmp.getElementId()).append("'")
		  .append(" data-oo-content-editor-url='").append(fr.getUrlBuilder().getJavascriptURI()).append("'")
		  .append(" class='o_container_part o_page_layout ")
		  .append(settings.getType().cssClass()).append("'>");
		renderContainer(renderer, sb, cmp, containerUbu, translator, renderResult, args);
		sb.append("</div>");
		
		if(cmp.isEditMode()) {
			renderInspector(renderer, sb, cmp.getInspectorComponent(), containerUbu, translator, renderResult, args);
		}

		sb.append("</div>");
	}
	
	private void renderTools(Renderer fr, StringOutput sb, ContentEditorContainerComponent cmp, URLBuilder containerUbu, Translator translator) {
		sb.append("<div class='o_page_container_tools'")
		  .append(" data-oo-page-fragment='").append(cmp.getComponentName()).append("'")
		  .append(" data-oo-page-element-id='").append(cmp.getElementId()).append("'")
		  .append(" data-oo-content-editor-url='").append(fr.getUrlBuilder().getJavascriptURI()).append("'")
		  .append(">");
		
		if (cmp.isRuleLinkEnabled() || cmp.supportsName()) {
			renderName(sb, cmp, translator);
		}

		renderDelete(sb, cmp, containerUbu, translator);
		
		renderEdit(sb, cmp, containerUbu, translator);
		
		renderDuplicate(sb, cmp, containerUbu, translator);
		renderNameLink(sb, cmp, containerUbu, translator);
		renderRuleLink(sb, cmp, containerUbu, translator);
		
		renderMoreMenu(sb, cmp, containerUbu, translator);
		renderDragZone(sb, cmp, translator);
		
		sb.append("</div>");
	}
	
	private void renderNameLink(StringOutput sb, ContentEditorContainerComponent cmp, URLBuilder ubu,
			Translator translator) {
		if (cmp.supportsName()) {
			sb.append("<a id='o_cname_").append(cmp.getElementId()).append("' ")
			  .append("href='javascript:;' onclick=\"");
			ubu.buildXHREvent(sb, "", false, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, "change_name"),
					new NameValuePair("fragment", cmp.getComponentName()));
			sb.append(" return false;\" class=''><i class='o_icon o_icon_settings'> </i>");
			sb.append(" <span>").append(translator.translate("container.name")).append("</span></a>");
		}
	}
	
	private void renderRuleLink(StringOutput sb, ContentEditorContainerComponent cmp, URLBuilder ubu,
			Translator translator) {
		if (cmp.isRuleLinkEnabled()) {
			sb.append("<a id='o_crule_").append(cmp.getElementId()).append("' ")
			  .append("href='javascript:;' onclick=\"");
			ubu.buildXHREvent(sb, "", false, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, "open_rules"),
					new NameValuePair("fragment", cmp.getComponentName()));
			sb.append(" return false;\" class='o_ce_rule'><i class='o_icon o_icon_branch'> </i>");
			sb.append(" <span>").append(translator.translate("container.rule")).append("</span></a>");
		}
	}
	
	private void renderContainer(Renderer renderer, StringOutput sb, ContentEditorContainerComponent cmp,
			URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		ContainerSettings settings = cmp.getContainerSettings();
		List<ContainerColumn> columns = settings.getColumns();
		int numOfBlocks = settings.getNumOfBlocks();
		for(int i=0; i<numOfBlocks; i++) {
			ContainerColumn column = null;
			if(columns != null && i < columns.size()) {
				column = columns.get(i);
			}
			
			renderContainerSlot(renderer, sb, cmp, column, i, ubu, translator, renderResult,  args);
		}
	}
	
	private void renderContainerSlot(Renderer renderer, StringOutput sb, ContentEditorContainerComponent cmp,
			ContainerColumn column, int i, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		sb.append("<div id='occ_").append(cmp.getElementId()).append("_").append(i).append("' class='")
		  .append(" o_page_container_slot o_page_drop' data-oo-slot='").append(i).append("'")
		  .append(" data-oo-content-editor-url='").append(ubu.getJavascriptURI()).append("'")
		  .append(" data-oo-page-element-id='").append(cmp.getElementId()).append("'")
		  .append(" data-oo-page-fragment='").append(cmp.getComponentName()).append("'")
		  .append(">");
		
		sb.append("<div class='o_page_container_slot-inner'>");
		if(column != null) {
			for(String elementId:column.getElementIds()) {
				Component fragment = cmp.getComponentByElementId(elementId);
				if(fragment != null) {
					fragment.getHTMLRendererSingleton().render(renderer, sb, fragment, ubu, translator, renderResult, args);
					fragment.setDirty(false);
				}
			}
		}
		sb.append("</div>");
		
		sb.append("<div class='o_button_group o_page_add_in_container_grp'><a id='o_ccad_").append(cmp.getElementId()).append("_").append(i).append("' ")
		  .append("href='#' onclick=\"");// add elements directly in container
		ubu.buildXHREvent(sb, "", false, true,
				new NameValuePair(VelocityContainer.COMMAND_ID, "add_to_container"),
				new NameValuePair("fragment", cmp.getComponentName()), // EditorFragment cmpFragment.getCmpId()
				new NameValuePair("column", Integer.toString(i)));
		sb.append(" return false;\" class='btn btn-default btn-xs o_page_add_in_container'><i class='o_icon o_icon_add'> </i>");
		sb.append(" <span>").append(translator.translate("add.element")).append("</span></a></div>");

		sb.append("</div>");
	}

	private void renderName(StringOutput sb, ContentEditorContainerComponent cmp, Translator translator) {
		String name = cmp.getContainerSettings().getName();
		sb.append("<span class='o_container_name'>");
		if (cmp.isRuleLinkEnabled()) {
			sb.append("<span>")
			  .append("<i class='o_icon o_icon_branch'> </i> ")
			  .append(translator.translate("container.rule"))
			  .append("</span> ");
		}
		String displayName = StringHelper.containsNonWhitespace(name)
				? Formatter.truncate(name, 20)
				: StringHelper.escapeHtml(PageEditorUIFactory.formatUntitled(translator, cmp.getElementId()));
		sb.append("<span><i class='o_icon o_icon_name'> </i> ")
		  .append(translator.translate("container.name.ref", displayName))
		  .append("</span>");
		sb.append("</span>");
	}
}
