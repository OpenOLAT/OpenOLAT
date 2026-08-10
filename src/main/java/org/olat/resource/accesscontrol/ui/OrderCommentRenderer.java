/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol.ui;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionDelegateCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 10 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OrderCommentRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {

	public static final String CMD_COMMENT = "comment";
	
	private final String suffix;
	private final Translator translator;
	
	public OrderCommentRenderer(String suffix, Translator translator) {
		this.suffix = suffix;
		this.translator = translator;
	}
	
	public final String getId(int row) {
		return "o_c" + suffix + CMD_COMMENT + "_" + row;
	}
	
	@Override
	public List<String> getActions() {
		return List.of(CMD_COMMENT);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof String comment
				&& StringHelper.containsNonWhitespace(comment)) {
			if(renderer == null) {
				target.append(comment);
			} else {
				renderOpen(target, row, source);
			}
		}
	}
	
	private void renderOpen(StringOutput target, int row, FlexiTableComponent source) {
		FlexiTableElementImpl ftE = source.getFormItem();
		String id = source.getFormDispatchId();
		Form rootForm = ftE.getRootForm();
		String actionId = getId(row);
		
		NameValuePair pair = new NameValuePair(CMD_COMMENT, Integer.toString(row));
		String jsCode = FormJSHelper.getXHRFnCallFor(rootForm, id, 1, false, true, false, pair);
		target.append("<a id=\"").append(actionId).append("\" href=\"javascript:;\" onclick=\"")
		      .append(jsCode).append("; return false;\"")
		      .append(FormJSHelper.triggerClickOnKeyDown(false))
		      .append(" class=\"o_validation_open_results\"")
		      .append(" title=\"").append(translator.translate("table.header.order.comment")).append("\">")
		      .append("<i class='o_icon o_icon-lg o_icon_notes'> </i> ")
		      .append("</a>");
	}
}
