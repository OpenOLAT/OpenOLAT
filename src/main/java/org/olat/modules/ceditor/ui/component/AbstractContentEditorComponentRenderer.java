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
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
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
public abstract class AbstractContentEditorComponentRenderer extends DefaultComponentRenderer {
	
	protected void renderInspector(Renderer renderer, StringOutput sb, Component cmp, URLBuilder containerUbu,
			Translator translator, RenderResult renderResult, String[] args) {
		if(cmp != null) {
			cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, containerUbu, translator, renderResult, args);
			cmp.setDirty(false);
		}
	}
	
	protected void renderAddAbove(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {
		if(cmp.isCreate()) {
			sb.append("<a id='o_ccaab_").append(cmp.getDispatchID()).append("' ")
				  .append("href='javascript:;' onclick=\"");// add elements directly in container
			ubu.buildXHREvent(sb, "", false, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, "add_element_above"),
					new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
			sb.append(" return false;\" class='o_sel_add_element_above' title='").append(translator.translate("add.element"))
			  .append("'><i class='o_icon o_icon_add'> </i> ").append(translator.translate("add.element.above")).append("</a>");
		}
	}
	
	protected void renderAddBelow(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {
		if(cmp.isCreate()) {
			sb.append("<a id='o_ccabe_").append(cmp.getDispatchID()).append("' ")
				  .append("href='javascript:;' onclick=\"");// add elements directly in container
			ubu.buildXHREvent(sb, "", false, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, "add_element_below"),
					new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
			sb.append(" return false;\" class='o_sel_add_element_below' title='").append(translator.translate("add.element.below"))
			  .append("'><i class='o_icon o_icon_add'> </i> ").append(translator.translate("add.element.below")).append("</a>");
		}
	}
	
	protected void renderClose(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {
		sb.append("<a id='o_ccclose_").append(cmp.getDispatchID()).append("' ")
			  .append("href='javascript:;' onclick=\"");// add elements directly in container
		ubu.buildXHREvent(sb, "", false, true,
				new NameValuePair(VelocityContainer.COMMAND_ID, "save_element"),
				new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
		sb.append(" return false;\" class='o_sel_save_element' title='").append(translator.translate("save.and.close"))
		  .append("'><i class='o_icon o_icon-fw o_icon_close'> </i> <span>").append(translator.translate("save.and.close")).append("</span></a>");
	}
	
	protected void renderToggleInspector(Renderer renderer, StringOutput sb, ContentEditorFragment cmp, URLBuilder containerUbu,
			Translator translator, RenderResult renderResult, String[] args) {
		if(cmp instanceof ContentEditorFragmentComponent fragCmp) {
			renderLink(renderer, sb, fragCmp.getToggleInspectorButton(), containerUbu, translator,  renderResult, args);
		} else if(cmp instanceof ContentEditorContainerComponent containerCmp) {
			renderLink(renderer, sb, containerCmp.getToggleInspectorButton(), containerUbu, translator,  renderResult, args);	
		}
	}
	
	private void renderLink(Renderer renderer, StringOutput sb, Link button, URLBuilder containerUbu,
			Translator translator, RenderResult renderResult, String[] args) {
		URLBuilder linkUbu = containerUbu.createCopyFor(button);
		button.getHTMLRendererSingleton().render(renderer, sb, button, linkUbu, translator, renderResult, args);
		button.setDirty(false);
	}
	
	protected void renderMoveUp(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {
		if(cmp.isEditable()) {
			sb.append("<a id='o_ccup_").append(cmp.getDispatchID()).append("' ")
				  .append("href='javascript:;' onclick=\"");// add elements directly in container
			ubu.buildXHREvent(sb, "", false, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, "move_up"),
					new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
			sb.append(" return false;\" class='o_sel_move_up_element' title='").append(translator.translate("move.up"))
			  .append("'><i class='o_icon o_icon-fw o_icon_slide_up'> </i> <span>").append(translator.translate("move.up")).append("</span></a>");
		}
	}
	
	protected void renderMoveDown(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {
		if(cmp.isEditable()) {
			sb.append("<a id='o_ccdown_").append(cmp.getDispatchID()).append("' ")
				  .append("href='javascript:;' onclick=\"");// add elements directly in container
			ubu.buildXHREvent(sb, "", false, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, "move_down"),
					new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
			sb.append(" return false;\" class='o_sel_move_down_element' title='").append(translator.translate("move.down"))
			  .append("'><i class='o_icon o_icon-fw o_icon_slide_down'> </i> <span>").append(translator.translate("move.down")).append("</span></a>");
		}
	}
	
	protected void renderDelete(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {
		if(cmp.isDeleteable()) {
			sb.append("<a id='o_ccdelete_").append(cmp.getDispatchID()).append("' ");
			if(!cmp.isDeleteLinkDisabled()) {
				sb.append("href='javascript:;' onclick=\"");// add elements directly in container
				ubu.buildXHREvent(sb, "", false, true,
						new NameValuePair(VelocityContainer.COMMAND_ID, "delete_element"),
						new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
				sb.append(" return false;\" ");
			}
			sb.append("class='o_sel_delete_element");
			if(cmp.isDeleteLinkDisabled()) {
				sb.append(" o_disabled");
			}
			sb.append("' ");
			if(cmp.isDeleteLinkDisabled()) {
				sb.append("role='link' aria-disabled='true' ");
			}
			sb.append("title='").append(translator.translate("delete"))
			  .append("'><i class='o_icon o_icon-fw o_icon_delete_item'> </i> <span>").append(translator.translate("delete")).append("</span></a>");
		}
	}
	
	protected void renderDuplicate(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {
		if(cmp.isCloneable()) {
			sb.append("<button id='o_ccclone_").append(cmp.getDispatchID()).append("' ")
			  .append("onclick=\"");// add elements directly in container
			ubu.buildXHREvent(sb, "", false, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, "clone_element"),
					new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
			sb.append(" return false;\" class='o_sel_clone_element' title='").append(translator.translate("duplicate"))
			  .append("'><i class='o_icon o_icon-fw o_icon_copy'> </i> <span>").append(translator.translate("duplicate")).append("</span></button>");
		}
	}
	
	protected void renderDragZone(StringOutput sb, ContentEditorFragment cmp, Translator translator) {
		sb.append("<button id='o_ccclone_").append(cmp.getDispatchID()).append("' class='o_page_drag_handle'")
		  .append(" title='").append(translator.translate("drag.element")).append("'>")
		  .append("<i class='o_icon o_icon-fw o_icon_move'> </i></button></a>");
	}
	
	protected final void renderMoreMenu(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {
		if(!cmp.isDeleteable() && !cmp.isCreate()) return;
		
		// More button
		sb.append("<button id='o_cmore_").append(cmp.getDispatchID()).append("' tabindex='0' type='button' class='dropdown-toggle' data-toggle='dropdown' aria-expanded='false'")
		  .append(" title='").append(translator.translate("more.title")).append("'>")
		  .append("<i class='o_icon o_icon-fw o_icon_commands'> </i></button>");
		// Menu
		sb.append("<ul class='dropdown-menu dropdown-menu-right' role='menu' style=''>");
		
		if(cmp.isCreate()) {
			sb.append("<li>");
			renderAddAbove(sb, cmp, ubu, translator);
			sb.append("</li><li>");
			renderAddBelow(sb, cmp, ubu, translator);
			sb.append("</li>");
		}
		
		if(cmp.isDeleteable()) {
			sb.append("<li class='divider'></li>")
			  .append("<li>");
			renderDelete(sb, cmp, ubu, translator);
			sb.append("</li>");
		}
		sb.append("</ul>");
	}
}
