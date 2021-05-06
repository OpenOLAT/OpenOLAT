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
import org.olat.core.gui.components.velocity.VelocityContainer;
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
	
	
	protected void renderAddAbove(StringOutput sb, Component cmp, URLBuilder ubu, Translator translator) {
		sb.append("<div class='o_page_add_above'>");
		sb.append("<a id='o_ccaab_").append(cmp.getDispatchID()).append("' ")
			  .append("href='javascript:;' onclick=\"");// add elements directly in container
		ubu.buildXHREvent(sb, "", false, true,
				new NameValuePair(VelocityContainer.COMMAND_ID, "add_element_above"),
				new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
		sb.append(" return false;\" class='o_sel_add_element_above' title='").append(translator.translate("add.element"))
		  .append("'><i class='o_icon o_icon_add'> </i></a>")
		  .append("</div>");
	}
	
	protected void renderAddBelow(StringOutput sb, Component cmp, URLBuilder ubu, Translator translator) {
		sb.append("<div class='o_page_add_below'>");
		sb.append("<a id='o_ccabe_").append(cmp.getDispatchID()).append("' ")
			  .append("href='javascript:;' onclick=\"");// add elements directly in container
		ubu.buildXHREvent(sb, "", false, true,
				new NameValuePair(VelocityContainer.COMMAND_ID, "add_element_below"),
				new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
		sb.append(" return false;\" class='o_sel_add_element_below' title='").append(translator.translate("add.element"))
		  .append("'><i class='o_icon o_icon_add'> </i></a>")
		  .append("</div>");
	}
	
	protected void renderPageUpDown(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {	
		if(cmp.isMoveable()) {
			sb.append("<div class='o_page_tools o_page_tools_dd'>");
			
			sb.append("<a id='o_ccup_").append(cmp.getDispatchID()).append("' ")
			  .append("href='javascript:;' onclick=\"");// add elements directly in container
			ubu.buildXHREvent(sb, "", false, true,
				new NameValuePair(VelocityContainer.COMMAND_ID, "move_up"),
				new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
			sb.append(" return false;\" class='o_sel_move_up_element' title='").append(translator.translate("move.up"))
			  .append("'><i class='o_icon o_icon-sm o_icon_move_up'> </i></a>");
			
			sb.append("<a id='o_ccdown_").append(cmp.getDispatchID()).append("' ")
			  .append("href='javascript:;' onclick=\"");// add elements directly in container
			ubu.buildXHREvent(sb, "", false, true,
				new NameValuePair(VelocityContainer.COMMAND_ID, "move_down"),
				new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
			sb.append(" return false;\" class='o_sel_move_down_element' title='").append(translator.translate("move.down"))
			  .append("'><i class='o_icon o_icon-sm o_icon_move_down'> </i></a>");
			
			sb.append("</div>");
		} else {
			sb.append("<div class='o_page_tools'> </div>");
		}
	}
	
	protected void renderClose(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {
		sb.append("<a id='o_ccclose_").append(cmp.getDispatchID()).append("' ")
			  .append("href='javascript:;' onclick=\"");// add elements directly in container
		ubu.buildXHREvent(sb, "", false, true,
				new NameValuePair(VelocityContainer.COMMAND_ID, "save_element"),
				new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
		sb.append(" return false;\" class='o_sel_save_element' title='").append(translator.translate("save.and.close"))
		  .append("'><i class='o_icon o_icon-sm o_icon_close'> </i> <span>").append(translator.translate("save.and.close")).append("</span></a>");
	}
	
	protected void renderDelete(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {
		if(cmp.isDeleteable()) {
			sb.append("<a id='o_ccdelete_").append(cmp.getDispatchID()).append("' ")
				  .append("href='javascript:;' onclick=\"");// add elements directly in container
			ubu.buildXHREvent(sb, "", false, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, "delete_element"),
					new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
			sb.append(" return false;\" class='o_sel_delete_element' title='").append(translator.translate("delete"))
			  .append("'><i class='o_icon o_icon-sm o_icon_delete_item'> </i> <span>").append(translator.translate("delete")).append("</span></a>");
		}
	}
	
	protected void renderDuplicate(StringOutput sb, ContentEditorFragment cmp, URLBuilder ubu, Translator translator) {
		if(cmp.isCloneable()) {
			sb.append("<a id='o_ccclone_").append(cmp.getDispatchID()).append("' ")
				  .append("href='javascript:;' onclick=\"");// add elements directly in container
			ubu.buildXHREvent(sb, "", false, true,
					new NameValuePair(VelocityContainer.COMMAND_ID, "clone_element"),
					new NameValuePair("fragment", cmp.getComponentName())); // EditorFragment cmpFragment.getCmpId()
			sb.append(" return false;\" class='o_sel_clone_element' title='").append(translator.translate("duplicate"))
			  .append("'><i class='o_icon o_icon-sm o_icon_copy'> </i> <span>").append(translator.translate("duplicate")).append("</span></a>");
		}
	}
}
