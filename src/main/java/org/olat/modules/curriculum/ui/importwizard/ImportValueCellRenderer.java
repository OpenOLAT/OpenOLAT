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
package org.olat.modules.curriculum.ui.importwizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
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
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewCurriculumsTableModel.ImportCurriculumsCols;

/**
 * 
 * Initial date: 10 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportValueCellRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {

	public static final String PARAM = "col";
	public static final String CMD_ACTIONS = "oValidation";
	private static final List<String> actions = List.of(CMD_ACTIONS);
	private static final ImportCurriculumsCols[] COLS = ImportCurriculumsCols.values();
	
	private final ImportCurriculumsCols col;
	
	public ImportValueCellRenderer(ImportCurriculumsCols col) {
		this.col = col;
	}
	
	@Override
	public List<String> getActions() {
		return actions;
	}
	
	public static String getId(int row, ImportCurriculumsCols column) {
		return "o_c" + CMD_ACTIONS + "_" + row + "_" + column.ordinal();
	}
	
	public static ImportCurriculumsCols getCol(UserRequest ureq) {
		String param = ureq.getParameter(PARAM);
		if(StringHelper.isLong(param)) {
			int ordinal = Integer.parseInt(param);
			if(ordinal >= 0 && ordinal < COLS.length)
			return COLS[ordinal];
		}
		return null;
	}
	
	public static String getId(int row, UserRequest ureq) {
		String param = ureq.getParameter(PARAM);
		if(StringHelper.isLong(param)) {
			return "o_c" + CMD_ACTIONS + "_" + row + "_" + param;
		}
		return null;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		Object obj = source.getFormItem().getTableDataModel().getObject(row);
		if(obj instanceof CurriculumImportedRow importedRow) {
			CurriculumImportedValue val = importedRow.getValidation(col);
			if(val != null) {
				renderValue(target, cellValue, row, val, source);
			} else {
				renderCellValue(target, cellValue);
			}
		} else {
			renderCellValue(target, cellValue);
		}
	}
	
	private void renderCellValue(StringOutput target, Object cellValue) {
		if(cellValue instanceof String string) {
			target.appendHtmlEscaped(string);
		}
	}
	
	private void renderValue(StringOutput target, Object cellValue, int row, CurriculumImportedValue val, FlexiTableComponent source) {
		renderStart(target, row, source);
		target.append("<i class='o_icon ").append(val.getLevel().iconCssClass()).append ("'> </i> ");
		if(StringHelper.containsNonWhitespace(val.getPlaceholder())) {
			target.appendHtmlEscaped(val.getPlaceholder());
		} else if(cellValue instanceof String string) {
			target.appendHtmlEscaped(string);
		}
		renderEnd(target);
	}
	
	private void renderStart(StringOutput target, int row, FlexiTableComponent source) {
		FlexiTableElementImpl ftE = source.getFormItem();
		String id = source.getFormDispatchId();
		Form rootForm = ftE.getRootForm();

		String actionId = getId(row, col);
		
		NameValuePair pair = new NameValuePair(CMD_ACTIONS, Integer.toString(row));
		NameValuePair colPair = new NameValuePair(PARAM, Integer.toString(col.ordinal()));
		String jsCode = FormJSHelper.getXHRFnCallFor(rootForm, id, 1, false, true, false, pair, colPair);
		target.append("<a id=\"").append(actionId).append("\" href=\"javascript:;\" onclick=\"")
		      .append(jsCode).append("; return false;\"")
		      .append(FormJSHelper.triggerClickOnKeyDown(false))
		      .append(" class='o_validation_open_results'>");
	}
	
	private void renderEnd(StringOutput target) {
		target.append("</a>");
	}
}
