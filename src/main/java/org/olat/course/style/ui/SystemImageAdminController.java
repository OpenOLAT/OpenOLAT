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
package org.olat.course.style.ui;

import java.util.ArrayList;
import java.util.List;

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
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.ImageSource;
import org.olat.course.style.ui.SystemImageDataModel.SystemImageCols;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 July 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SystemImageAdminController extends FormBasicController {
	
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";
	
	private FormLink addSystemImageLink;
	private FlexiTableElement tableEl;
	private SystemImageDataModel dataModel;
	
	private CloseableModalController cmc;
	private SystemImageEditController editCtrl;
	private DialogBoxController deleteDialogCtrl;
	
	@Autowired
	private CourseStyleService courseStyleService;

	public SystemImageAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer topCont = FormLayoutContainer.createVerticalFormLayout("top", getTranslator());
		topCont.setElementCssClass("o_button_group_right");
		topCont.setRootForm(mainForm);
		formLayout.add(topCont);
		
		addSystemImageLink = uifactory.addFormLink("system.image.add", topCont, Link.BUTTON);
		addSystemImageLink.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
//		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SystemImageCols.preview));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SystemImageCols.filename));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SystemImageCols.translaton));
		DefaultFlexiColumnModel editCol = new DefaultFlexiColumnModel(SystemImageCols.edit.i18nHeaderKey(),
				SystemImageCols.edit.ordinal(), CMD_EDIT,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(SystemImageCols.edit.i18nHeaderKey()), CMD_EDIT), null));
		columnsModel.addFlexiColumnModel(editCol);
		DefaultFlexiColumnModel deleteCol = new DefaultFlexiColumnModel(SystemImageCols.delete.i18nHeaderKey(),
				SystemImageCols.delete.ordinal(), CMD_DELETE,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(SystemImageCols.delete.i18nHeaderKey()), CMD_DELETE), null));
		columnsModel.addFlexiColumnModel(deleteCol);
		
		dataModel = new SystemImageDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "course-system-image");
	}
	
	private void loadModel() {
		List<ImageSource> systemImageSources = courseStyleService.getSystemTeaserImageSources();
		systemImageSources.sort((i1, i2) -> i1.getFilename().compareToIgnoreCase(i2.getFilename()));
		List<SystemImageRow> rows = new ArrayList<>(systemImageSources.size());
		for (ImageSource systemImageSource: systemImageSources) {
			SystemImageRow row = new SystemImageRow();
			
			row.setFilename(systemImageSource.getFilename());
			String translation = CourseStyleUIFactory.translateSystemImage(getTranslator(), systemImageSource.getFilename());
			row.setTranslation(translation);
			
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addSystemImageLink == source) {
			doAddSystemImage(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				SystemImageRow row = dataModel.getObject(se.getIndex());
				if(CMD_EDIT.equals(cmd)) {
					doEditSystemImage(ureq, row.getFilename());
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDeletion(ureq, row.getFilename());
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
				String filename = (String)deleteDialogCtrl.getUserObject();
				doDelete(filename);
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

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

	private void doAddSystemImage(UserRequest ureq) {
		editCtrl = new SystemImageEditController(ureq, getWindowControl(), null);
		listenTo(editCtrl);
		
		String title = translate("system.image.add.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true,
				title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditSystemImage(UserRequest ureq, String filename) {
		editCtrl = new SystemImageEditController(ureq, getWindowControl(), filename);
		listenTo(editCtrl);
		
		String title = translate("system.image.edit.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true,
				title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmDeletion(UserRequest ureq, String filename) {
		String title = translate("system.image.delete.confirm.title");
		String translatedFilename = CourseStyleUIFactory.translateSystemImage(getTranslator(), filename);
		String text = translate("system.image.delete.confirm.text", new String[] { translatedFilename, filename });
		deleteDialogCtrl = activateYesNoDialog(ureq, title, text, deleteDialogCtrl);
		deleteDialogCtrl.setUserObject(filename);
	}

	private void doDelete(String filename) {
		courseStyleService.deleteSystemImage(filename);
	}

}
