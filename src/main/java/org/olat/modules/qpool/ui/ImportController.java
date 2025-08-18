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
package org.olat.modules.qpool.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FileElementInfos;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportController extends FormBasicController {

	private static final String[] keys = {"yes","no"};
	
	private FileElement fileEl;
	private SingleSelection editableEl;
	
	private final QuestionItemsSource source;
	@Autowired
	private QPoolService qpoolService;
	
	public ImportController(UserRequest ureq, WindowControl wControl, QuestionItemsSource source) {
		super(ureq, wControl);
		this.source = source;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_user/area_modules/Data_Management/#import");

		if(source.askEditable()) {
			String[] values = new String[]{
					translate("yes"),
					translate("no")
			};
			editableEl = uifactory.addRadiosVertical("share.editable", "share.editable", formLayout, keys, values);
			editableEl.select("no", true);
		}
		fileEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "item", "import.item", formLayout);
		fileEl.addActionListener(FormEvent.ONCHANGE);
		fileEl.limitToMimeType(Set.of("application/zip"), null, null);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}


	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(fileEl == source) {
			validateFormItem(ureq, fileEl);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		fileEl.clearError();
		if(fileEl.getUploadFile() == null) {
			fileEl.setErrorKey("form.mandatory.hover");
			allOk = false;
		} else {
			allOk &= validateFormItem(ureq, fileEl);
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String filename = fileEl.getUploadFileName();
		
		boolean importError = false;
		List<QuestionItem> importItems = new ArrayList<>();
		List<FileElementInfos> fileInfosList = fileEl.getUploadFilesInfos();
		for(FileElementInfos fileInfos: fileInfosList) {
			File file = fileInfos.file();
			List<QuestionItem> partialImportItems = qpoolService.importItems(getIdentity(), getLocale(), filename, file);
			if(partialImportItems != null) {
				importItems.addAll(partialImportItems);
			} else {
				importError = true;
			}
		}

		if(importError || importItems.isEmpty()) {
			fireEvent(ureq, Event.DONE_EVENT);
			showWarning("import.failed");
		} else {
			boolean editable = editableEl == null ? true : editableEl.isSelected(0);
			source.postImport(importItems, editable);
			for (QuestionItem item: importItems) {
				QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
						Action.CREATE_QUESTION_ITEM_BY_IMPORT);
				builder.withAfter(item);
				qpoolService.persist(builder.create());
			}
			fireEvent(ureq, Event.DONE_EVENT);
			showInfo("import.success", Integer.toString(importItems.size()));
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
