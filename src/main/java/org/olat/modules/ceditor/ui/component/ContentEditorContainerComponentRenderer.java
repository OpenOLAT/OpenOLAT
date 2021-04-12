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
import org.olat.modules.ceditor.model.ContainerColumn;
import org.olat.modules.ceditor.model.ContainerSettings;

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
		
		if(cmp.isEditMode()) {
			renderEditContainer(renderer, sb, cmp, containerUbu, translator, renderResult, args);
		} else {
			renderPreviewContainer(renderer, sb, cmp, containerUbu, translator, renderResult, args);
		}
	}
	
	private void renderPreviewContainer(Renderer renderer, StringOutput sb, ContentEditorContainerComponent cmp, URLBuilder containerUbu,
			Translator translator, RenderResult renderResult, String[] args) {
		Renderer fr = Renderer.getInstance(cmp, translator, containerUbu, new RenderResult(), renderer.getGlobalSettings(), renderer.getCsrfToken());

		sb.append("<div id='o_c").append(cmp.getDispatchID()).append("' data-oo-page-fragment='").append(cmp.getComponentName()).append("'")
		  .append(" data-oo-page-element-id='").append(cmp.getElementId()).append("'")
		  .append(" data-oo-content-editor-url='").append(fr.getUrlBuilder().getJavascriptURI()).append("'")
		  .append(" class='o_page_part o_page_part_view o_page_container clearfix'>");
		
		renderContainer(fr, sb, cmp, containerUbu, translator, renderResult, args);
		
		sb.append("</div>");
	}
	
	private void renderEditContainer(Renderer renderer, StringOutput sb, ContentEditorContainerComponent cmp, URLBuilder containerUbu,
			Translator translator, RenderResult renderResult, String[] args) {
	
		sb.append("<div id='o_c").append(cmp.getDispatchID()).append("' class='o_page_fragment_edit' data-oo-page-fragment='").append(cmp.getComponentName()).append("'>");

		renderAboveTools(sb, cmp, containerUbu, translator);
		renderPageUpDown(sb, cmp, containerUbu, translator);
		
		sb.append("<div id='oce_").append(cmp.getElementId()).append("' data-oo-page-fragment='").append(cmp.getComponentName()).append("'  class='o_page_part o_page_edit clearfix'>");
		renderContainer(renderer, sb, cmp, containerUbu, translator, renderResult, args);
		sb.append("</div>");
		
		renderAddBelow(sb, cmp, containerUbu, translator);

		sb.append("</div>");
	}
	
	private void renderAboveTools(StringOutput sb, ContentEditorContainerComponent cmp, URLBuilder containerUbu, Translator translator) {
		sb.append("<div class='o_page_tools_above clearfix'>");

		renderAddAbove(sb, cmp, containerUbu, translator);

		sb.append("<div class='o_page_others_above'>");
		
		renderClose(sb, cmp, containerUbu, translator);
		renderDelete(sb, cmp, containerUbu, translator);
		renderContainerColumnLinks(sb, cmp, containerUbu, translator);
		renderNameLink(sb, cmp, containerUbu, translator);
		renderRuleLink(sb, cmp, containerUbu, translator);
		
		sb.append("</div>") // o_page_others_above
		  .append("</div>");// o_page_tools_above
	}
	
	private void renderContainerColumnLinks(StringOutput sb, ContentEditorContainerComponent cmp,
			URLBuilder ubu, Translator translator) {
		
		ContainerSettings settings = cmp.getContainerSettings();
		int numOfColumns = settings.getNumOfColumns();
		
		renderColumnLink(sb, 4, numOfColumns == 4, cmp, ubu, translator);
		renderColumnLink(sb, 3, numOfColumns == 3, cmp, ubu, translator);
		renderColumnLink(sb, 2, numOfColumns == 2, cmp, ubu, translator);
		renderColumnLink(sb, 1, numOfColumns == 1, cmp, ubu, translator);
	}
	
	private void renderColumnLink(StringOutput sb, int columns, boolean selected, ContentEditorContainerComponent cmp,
			URLBuilder ubu, Translator translator) {
		sb.append("<a id='o_cccols_").append(cmp.getElementId()).append("_").append(columns).append("' ")
		  .append("href='javascript:;' onclick=\"");// add elements directly in container
		ubu.buildXHREvent(sb, "", false, true,
				new NameValuePair(VelocityContainer.COMMAND_ID, "change_nbre_columns"),
				new NameValuePair("fragment", cmp.getComponentName()), // EditorFragment cmpFragment.getCmpId()
				new NameValuePair("column", Integer.toString(columns)));
		sb.append(" return false;\" class=''><i class='o_icon ");
		if(selected) {
			sb.append("o_icon_check");
		} else {
			sb.append("o_icon_columns");
		}
		sb.append("'> </i> <span>").append(translator.translate("text.column." + columns)).append("</span></a>");
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
	
	private void renderContainer(Renderer renderer, StringOutput sb, ContentEditorContainerComponent cmp, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		ContainerSettings settings = cmp.getContainerSettings();
		List<ContainerColumn> columns = settings.getColumns();
		int numOfColumns = settings.getNumOfColumns();
		List<String> cssColumns = ContainerCSSColumns.getCssColumns(numOfColumns);
		for(int i=0; i<numOfColumns; i++) {
			sb.append("<div id='occ_").append(cmp.getElementId()).append("_").append(i).append("' class='")
			  .append(cssColumns.get(i)).append(" o_page_container_slot o_page_drop' data-oo-slot='").append(i).append("'")
			  .append(" data-oo-content-editor-url='").append(ubu.getJavascriptURI()).append("'")
			  .append(" data-oo-page-element-id='").append(cmp.getElementId()).append("'")
			  .append(" data-oo-page-fragment='").append(cmp.getComponentName()).append("'")
			  .append(">");
			
			sb.append("<div class='o_page_container_slot-inner'>");
			
			if(columns != null && i < columns.size()) {
				ContainerColumn column = columns.get(i);
				for(String elementId:column.getElementIds()) {
					Component fragment = cmp.getComponentByElementId(elementId);
					if(fragment != null) {
						fragment.getHTMLRendererSingleton().render(renderer, sb, fragment, ubu, translator, renderResult, args);
						fragment.setDirty(false);
					}
				}
			}
			
			sb.append("</div>");
			
			if(cmp != null) {
				sb.append("<div class='o_button_group o_page_add_in_container_grp'><a id='o_ccad_").append(cmp.getElementId()).append("_").append(i).append("' ")
				  .append("href='#' onclick=\"");// add elements directly in container
				ubu.buildXHREvent(sb, "", false, true,
						new NameValuePair(VelocityContainer.COMMAND_ID, "add_to_container"),
						new NameValuePair("fragment", cmp.getComponentName()), // EditorFragment cmpFragment.getCmpId()
						new NameValuePair("column", Integer.toString(i)));
				sb.append(" return false;\" class='btn btn-default btn-xs o_page_add_in_container'><i class='o_icon o_icon_add'> </i>");
				sb.append(" <span>").append(translator.translate("add.element")).append("</span></a></div>");
			}
			sb.append("</div>");
		}
	}
}
