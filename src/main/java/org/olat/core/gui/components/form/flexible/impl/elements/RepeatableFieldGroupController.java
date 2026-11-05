/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;

/**
 * Repeatable field group: a list of rows of form fields with the actions
 * "Duplicate" and "Remove" per row and an action to add a row at the end.
 * The controller is embedded in the form of the parent controller. The parent
 * implements the {@link RepeatableFieldGroupDelegate} to create and validate
 * the rows and reads them in formOK().
 *
 * Initial date: 28 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class RepeatableFieldGroupController<R extends RepeatableFieldGroupRow> extends FormBasicController {
	
	private static final String CMD_ADD = "rfg_add";
	private static final String CMD_DUPLICATE = "rfg_duplicate";
	private static final String CMD_REMOVE = "rfg_remove";
	private static final int NUMBERED_MIN_ROWS = 6;
	
	private FormLink addLink;
	
	private int count = 0;
	private int minRows = 1;
	private final String idPrefix;
	private final String objectName;
	private final String addText;
	private final List<RowWrapper> rows = new ArrayList<>();
	private final RepeatableFieldGroupDelegate<R> delegate;
	
	/**
	 * @param rootForm The form of the parent controller
	 * @param translator The translator of the parent, used for the labels of the row items
	 * @param objectName The translated name of one row, e.g. "Date"
	 * @param addText The translated text of the add action, e.g. "Add date"
	 * @param delegate Creates the rows
	 */
	public RepeatableFieldGroupController(UserRequest ureq, WindowControl wControl, Form rootForm, Translator translator,
			String objectName, String addText, RepeatableFieldGroupDelegate<R> delegate) {
		super(ureq, wControl, LAYOUT_CUSTOM, "repeatable_field_group", rootForm);
		setTranslator(Util.createPackageTranslator(RepeatableFieldGroupController.class, getLocale(), translator));
		flc.setTranslator(getTranslator());
		this.idPrefix = "rfg" + CodeHelper.getRAMUniqueID() + "_";
		this.objectName = objectName;
		this.addText = addText;
		this.delegate = delegate;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addLink = uifactory.addFormLink(idPrefix + "add", CMD_ADD, addText, null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
		addLink.setIconLeftCSS("o_icon o_icon_add");
		flc.contextPut("addName", addLink.getName());
		flc.contextPut("rows", rows);
	}
	
	/**
	 * @param minRows The rows can not be removed below this number. Default is 1.
	 */
	public void setMinRows(int minRows) {
		this.minRows = minRows;
		updateRows();
	}
	
	public List<R> getRows() {
		return rows.stream().map(RowWrapper::getRow).toList();
	}
	
	/**
	 * Add an empty row at the end, e.g. to load the existing values.
	 */
	public R addRow() {
		return addRow(rows.size(), null).getRow();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == addLink) {
			doAdd();
		} else if(source instanceof FormLink link && CMD_DUPLICATE.equals(link.getCmd())) {
			findRow(link).ifPresent(this::doDuplicate);
		} else if(source instanceof FormLink link && CMD_REMOVE.equals(link.getCmd())) {
			findRow(link).ifPresent(this::doRemove);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private Optional<RowWrapper> findRow(FormLink link) {
		return rows.stream().filter(wrapper -> wrapper == link.getUserObject()).findFirst();
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		for(RowWrapper wrapper:rows) {
			allOk &= delegate.validateRow(wrapper.getRow());
		}
		return allOk;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doAdd() {
		RowWrapper wrapper = addRow(rows.size(), null);
		focus(wrapper);
	}
	
	private void doDuplicate(RowWrapper source) {
		RowWrapper wrapper = addRow(rows.indexOf(source) + 1, source.getRow());
		focus(wrapper);
	}
	
	private void doRemove(RowWrapper wrapper) {
		if(rows.size() <= minRows) {
			return;
		}
		
		int index = rows.indexOf(wrapper);
		rows.remove(wrapper);
		for(FormItem item:wrapper.getRow().getFormItems()) {
			flc.remove(item);
		}
		flc.remove(wrapper.getDuplicateLink());
		flc.remove(wrapper.getRemoveLink());
		updateRows();
		
		if(rows.isEmpty()) {
			focus(addLink);
		} else {
			focus(rows.get(Math.min(index, rows.size() - 1)));
		}
	}
	
	private RowWrapper addRow(int index, R source) {
		String id = idPrefix + (++count);
		R row = delegate.createRow(id, flc);
		if(source != null) {
			delegate.copyRow(source, row);
		}
		
		FormLink duplicateLink = uifactory.addFormLink(id + "_duplicate", CMD_DUPLICATE, translate("rfg.duplicate"), null, flc, Link.BUTTON_SMALL + Link.NONTRANSLATED);
		duplicateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_duplicate");
		duplicateLink.setGhost(true);
		FormLink removeLink = uifactory.addFormLink(id + "_remove", CMD_REMOVE, translate("rfg.remove"), null, flc, Link.BUTTON_SMALL + Link.NONTRANSLATED);
		removeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_remove_entry");
		removeLink.setGhost(true);
		
		RowWrapper wrapper = new RowWrapper(id, row, duplicateLink, removeLink);
		duplicateLink.setUserObject(wrapper);
		removeLink.setUserObject(wrapper);
		rows.add(index, wrapper);
		updateRows();
		return wrapper;
	}
	
	private void updateRows() {
		for(int i = 0; i < rows.size(); i++) {
			RowWrapper wrapper = rows.get(i);
			String title = translate("rfg.row.title", objectName, Integer.toString(i + 1));
			wrapper.setTitle(title);
			String duplicateText = translate("rfg.duplicate.aria", title);
			wrapper.getDuplicateLink().setAriaLabel(duplicateText);
			wrapper.getDuplicateLink().setTitle(duplicateText);
			String removeText = translate("rfg.remove.aria", title);
			wrapper.getRemoveLink().setAriaLabel(removeText);
			wrapper.getRemoveLink().setTitle(removeText);
			wrapper.getRemoveLink().setEnabled(rows.size() > minRows);
		}
		flc.contextPut("numbered", Boolean.valueOf(rows.size() >= NUMBERED_MIN_ROWS));
		flc.setDirty(true);
	}
	
	private void focus(RowWrapper wrapper) {
		wrapper.getRow().getFormItems().stream()
			.filter(FormItem::isVisible)
			.findFirst()
			.ifPresent(this::focus);
	}
	
	private void focus(FormItem item) {
		Command focusCommand = FormJSHelper.getFormFocusCommand(mainForm.getFormName(), item.getForId());
		getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
	}
	
	public class RowWrapper {
		
		private final String id;
		private final R row;
		private final FormLink duplicateLink;
		private final FormLink removeLink;
		private final List<Cell> cells;
		private String title;
		
		private RowWrapper(String id, R row, FormLink duplicateLink, FormLink removeLink) {
			this.id = id;
			this.row = row;
			this.duplicateLink = duplicateLink;
			this.removeLink = removeLink;
			List<FormItem> items = row.getFormItems();
			cells = new ArrayList<>(items.size());
			for(int i = 0; i < items.size(); i++) {
				cells.add(new Cell(items.get(i).getName(), delegate.getColumnCssClass(i)));
			}
		}
		
		public String getId() {
			return id;
		}
		
		public R getRow() {
			return row;
		}
		
		public String getTitle() {
			return title;
		}
		
		private void setTitle(String title) {
			this.title = title;
		}
		
		public FormLink getDuplicateLink() {
			return duplicateLink;
		}
		
		public FormLink getRemoveLink() {
			return removeLink;
		}
		
		public List<Cell> getCells() {
			return cells;
		}
	}
	
	public static class Cell {
		
		private final String name;
		private final String cssClass;
		
		private Cell(String name, String cssClass) {
			this.name = name;
			this.cssClass = cssClass;
		}
		
		public String getName() {
			return name;
		}
		
		public String getCssClass() {
			return cssClass;
		}
	}
	
}
