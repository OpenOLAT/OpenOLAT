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
package org.olat.modules.curriculum.ui.wizard;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupMembershipStatus;
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
import org.olat.core.util.Util;
import org.olat.modules.curriculum.ui.CurriculumManagerController;

/**
 * 
 * Initial date: 20 avr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class AddGroupMembershipStatusRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {

	public static final String PARAM = "col";
	public static final String CMD_ACTIONS = "addrole";
	private static final List<String> actions = List.of(CMD_ACTIONS);
	
	private final Translator translator;
	
	public AddGroupMembershipStatusRenderer(Locale locale) {
		translator = Util.createPackageTranslator(CurriculumManagerController.class, locale);
	}

	@Override
	public List<String> getActions() {
		return actions;
	}
	
	public static String getId(int row) {
		return "o_c" + CMD_ACTIONS + "_" + row;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof GroupMembershipStatus status) {
			render(target, status);
		} else {
			renderAdd(target, row, source);
		}
	}
	
	public void render(StringOutput target, GroupMembershipStatus status) {
		String statusName = status.name().toLowerCase();
		String label = translator.translate("membership.".concat(status.name()));
		target.append("<span class='o_labeled_light o_gmembership_status_").append(statusName).append("'>")
		  .append("<i class='o_icon o_membership_status_").append(statusName.toLowerCase()).append(" o_icon-fw' title='").append(label).append("'> </i> ")
		  .append(label)
	      .append("</span");
	}
	
	private void renderAdd(StringOutput target, int row, FlexiTableComponent source) {
		FlexiTableElementImpl ftE = source.getFormItem();
		String id = source.getFormDispatchId();
		Form rootForm = ftE.getRootForm();
		String actionId = getId(row);
		
		NameValuePair pair = new NameValuePair(CMD_ACTIONS, Integer.toString(row));
		String jsCode = FormJSHelper.getXHRFnCallFor(rootForm, id, 1, false, true, false, pair);
		target.append("<a id=\"").append(actionId).append("\" href=\"javascript:;\" onclick=\"")
		      .append(jsCode).append("; return false;\"")
		      .append(FormJSHelper.triggerClickOnKeyDown(false))
		      .append(" class='o_validation_open_results'>")
		      .append("<i class='o_icon o_icon-fw o_icon_plus'> </i> ").append(translator.translate("add"))
		      .append("</a>");
	}
}
