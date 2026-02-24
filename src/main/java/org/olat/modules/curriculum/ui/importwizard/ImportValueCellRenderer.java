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
import java.util.Locale;

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
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.AbstractExcelReader.ReaderLocalDate;
import org.olat.core.util.openxml.AbstractExcelReader.ReaderLocalTime;
import org.olat.modules.curriculum.ui.importwizard.ImportCurriculumsReviewTableModel.ImportCurriculumsCols;

/**
 * 
 * Initial date: 10 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportValueCellRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {

	public static final String PARAM = "col";
	public static final String CMD_ACTIONS = "oValidation";
	private static final String OBFUSCATED_STRING = "********";
	private static final List<String> actions = List.of(CMD_ACTIONS);
	private static final ImportCurriculumsCols[] COLS = ImportCurriculumsCols.values();
	
	private final int index;
	private final String name;
	private final Formatter format;
	private final ImportCurriculumsCols col;
	
	private boolean obsfuscate;
	
	public ImportValueCellRenderer(ImportCurriculumsCols col, Locale locale) {
		this.col = col;
		this.name = null;
		this.index = col.ordinal();
		format = Formatter.getInstance(locale);
	}
	
	public ImportValueCellRenderer(int index, String name, Locale locale) {
		this.col = null;
		this.name = name;
		this.index = index;
		format = Formatter.getInstance(locale);
	}

	public boolean isObsfuscate() {
		return obsfuscate;
	}

	public void setObsfuscate(boolean obsfuscate) {
		this.obsfuscate = obsfuscate;
	}

	@Override
	public List<String> getActions() {
		return actions;
	}
	
	public static String getId(int row, ImportCurriculumsCols column) {
		return getId(row, column.ordinal());
	}
	
	public static String getId(int row, int index) {
		return "o_c" + CMD_ACTIONS + "_" + row + "_" + index;
	}
	
	public static ImportCurriculumsCols getCol(UserRequest ureq) {
		String param = ureq.getParameter(PARAM);
		if(StringHelper.isLong(param)) {
			int ordinal = Integer.parseInt(param);
			if(ordinal >= 0 && ordinal < COLS.length) {
				return COLS[ordinal];
			}
		}
		return null;
	}
	
	public static int getColIndex(UserRequest ureq) {
		String param = ureq.getParameter(PARAM);
		if(StringHelper.isLong(param)) {
			int index = Integer.parseInt(param);
			if(index >= 0) {
				return index;
			}
		}
		return -1;
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
		if(obj instanceof ImportedUserRow importedRow && name != null) {
			CurriculumImportedValue val = importedRow.getHandlerValidation(name);
			if(val != null) {
				renderValue(target, cellValue, row, val, source);
			} else {
				renderCellValue(target, cellValue);
			}
		} else if(obj instanceof AbstractImportRow importedRow) {
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
			if(obsfuscate) {
				target.append(OBFUSCATED_STRING);
			} else {
				target.appendHtmlEscaped(string);
			}
		} else if(cellValue instanceof ReaderLocalDate readerDate) {
			if(readerDate.date() != null) {
				target.append(format.formatDate(readerDate.date()));
			} else if(readerDate.val() != null) {
				target.appendHtmlEscaped(readerDate.val());
			}
		} else if(cellValue instanceof ReaderLocalTime readerTime) {
			if(readerTime.time() != null) {
				target.append(format.formatTimeShort(readerTime.time()));
			} else if(readerTime.val() != null) {
				target.appendHtmlEscaped(readerTime.val());
			}
		}
	}
	
	private void renderValue(StringOutput target, Object cellValue, int row, CurriculumImportedValue val, FlexiTableComponent source) {
		renderStart(target, row, source);
		target.append("<i class='o_icon ").append(val.getLevel().iconCssClass()).append ("'> </i> ");
		if(StringHelper.containsNonWhitespace(val.getPlaceholder())) {
			target.appendHtmlEscaped(val.getPlaceholder());
		} else {
			renderCellValue(target, cellValue);
		}
		renderEnd(target);
	}
	
	private void renderStart(StringOutput target, int row, FlexiTableComponent source) {
		FlexiTableElementImpl ftE = source.getFormItem();
		String id = source.getFormDispatchId();
		Form rootForm = ftE.getRootForm();
		String actionId = getId(row, index);
		
		NameValuePair pair = new NameValuePair(CMD_ACTIONS, Integer.toString(row));
		NameValuePair colPair = new NameValuePair(PARAM, Integer.toString(index));
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
