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
package org.olat.repository.ui.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryEducationalTypeStat;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.admin.EducationalTypeDataModel.EducationalTypeCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EducationalTypeAdminController extends FormBasicController {
	
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";
	
	private FormLink addEducationalTypeLink;
	private FlexiTableElement tableEl;
	private EducationalTypeDataModel dataModel;
	
	private CloseableModalController cmc;
	private EducationalTypeEditController editCtrl;
	private DialogBoxController deleteDialogCtrl;

	public EducationalTypeAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		initForm(ureq);
		loadModel();
	}
	
	@Autowired
	private RepositoryManager repositoryManager;

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer topCont = FormLayoutContainer.createVerticalFormLayout("top", getTranslator());
		topCont.setElementCssClass("o_button_group_right");
		topCont.setRootForm(mainForm);
		formLayout.add(topCont);
		
		addEducationalTypeLink = uifactory.addFormLink("educational.type.add", topCont, Link.BUTTON);
		addEducationalTypeLink.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EducationalTypeCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EducationalTypeCols.translaton));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EducationalTypeCols.cssClass));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EducationalTypeCols.numberOfCourses));
		DefaultFlexiColumnModel editCol = new DefaultFlexiColumnModel(EducationalTypeCols.edit.i18nHeaderKey(),
				EducationalTypeCols.edit.ordinal(), CMD_EDIT,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(EducationalTypeCols.edit.i18nHeaderKey()), CMD_EDIT), null));
		columnsModel.addFlexiColumnModel(editCol);
		DefaultFlexiColumnModel deleteCol = new DefaultFlexiColumnModel(EducationalTypeCols.delete.i18nHeaderKey(),
				EducationalTypeCols.delete.ordinal(), CMD_DELETE,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(EducationalTypeCols.delete.i18nHeaderKey()), CMD_DELETE), null));
		columnsModel.addFlexiColumnModel(deleteCol);
		
		dataModel = new EducationalTypeDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "re-educational-type");
	}
	
	private void loadModel() {
		List<RepositoryEntryEducationalType> types = repositoryManager.getAllEducationalTypes();
		Map<Long, Long> typeKeyToNumberOfEntries = repositoryManager.getEducationalTypeStats().stream()
				.collect(Collectors.toMap(RepositoryEntryEducationalTypeStat::getEducationalTypeKey, RepositoryEntryEducationalTypeStat::getNumberOfRepositoryEntries));
		List<EducationalTypeRow> rows = new ArrayList<>(types.size());
		for (RepositoryEntryEducationalType type : types) {
			EducationalTypeRow row = new EducationalTypeRow(type);
			String translation = translate(RepositoyUIFactory.getI18nKey(type));
			row.setTranslation(translation);
			Long numberOfCourse = typeKeyToNumberOfEntries.get(type.getKey());
			row.setNumberOfCourse(numberOfCourse);
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addEducationalTypeLink == source) {
			doAddEducationalType(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				EducationalTypeRow row = dataModel.getObject(se.getIndex());
				if(CMD_EDIT.equals(cmd)) {
					doEditEducationalType(ureq, row.getEducationalType());
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDeletion(ureq, row.getEducationalType());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (editCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(deleteDialogCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				RepositoryEntryEducationalType educationalType = (RepositoryEntryEducationalType)deleteDialogCtrl.getUserObject();
				doDelete(educationalType);
				loadModel();
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}

	private void doAddEducationalType(UserRequest ureq) {
		editCtrl = new EducationalTypeEditController(ureq, getWindowControl(), null);
		listenTo(editCtrl);
		
		String title = translate("educational.type.add.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true,
				title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditEducationalType(UserRequest ureq, RepositoryEntryEducationalType educationalType) {
		editCtrl = new EducationalTypeEditController(ureq, getWindowControl(), educationalType);
		listenTo(editCtrl);
		
		String title = translate("educational.type.edit.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true,
				title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmDeletion(UserRequest ureq, RepositoryEntryEducationalType educationalType) {
		String title = translate("educational.type.delete.confitm.title");
		String translatedType = translate(RepositoyUIFactory.getI18nKey(educationalType));
		String text = translate("educational.type.delete.confitm.text", new String[] { translatedType });
		deleteDialogCtrl = activateYesNoDialog(ureq, title, text, deleteDialogCtrl);
		deleteDialogCtrl.setUserObject(educationalType);
	}

	private void doDelete(RepositoryEntryEducationalType educationalType) {
		repositoryManager.deleteEducationalType(educationalType);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
