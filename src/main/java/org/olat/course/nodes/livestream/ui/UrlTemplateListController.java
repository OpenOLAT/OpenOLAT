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
package org.olat.course.nodes.livestream.ui;

import java.util.Comparator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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
import org.olat.course.nodes.livestream.LiveStreamService;
import org.olat.course.nodes.livestream.model.UrlTemplate;
import org.olat.course.nodes.livestream.ui.UrlTemplateDataModel.UrlTemplateCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UrlTemplateListController extends FormBasicController {
	
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";

	private static final Comparator<UrlTemplate> NAME_ASC = (t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName());
	
	private FormLink addUrlTemplateButton;
	private FlexiTableElement tableEl;
	private UrlTemplateDataModel dataModel;
	
	private CloseableModalController cmc;
	private UrlTemplateEditController editUrlTemplateCtrl;
	private DialogBoxController deleteDialogCtrl;
	
	@Autowired
	private LiveStreamService liveStreamService;

	public UrlTemplateListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer buttonsTopCont = FormLayoutContainer.createButtonLayout("buttons.top", getTranslator());
		buttonsTopCont.setElementCssClass("o_button_group o_button_group_right");
		buttonsTopCont.setRootForm(mainForm);
		formLayout.add(buttonsTopCont);
		
		addUrlTemplateButton = uifactory.addFormLink("url.template.add", buttonsTopCont, Link.BUTTON);
		addUrlTemplateButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, UrlTemplateCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UrlTemplateCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UrlTemplateCols.url1));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UrlTemplateCols.url2));
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("url.template.edit", -1, CMD_EDIT,
				new StaticFlexiCellRenderer(translate("url.template.edit"), CMD_EDIT, "", "", null));
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);
		DefaultFlexiColumnModel deleteColumn = new DefaultFlexiColumnModel("url.template.delete", -1, CMD_DELETE,
				new StaticFlexiCellRenderer(translate("url.template.delete"), CMD_DELETE, "", "", null));
		deleteColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(deleteColumn);
		
		dataModel = new UrlTemplateDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "livestream-url-templates");
	}

	private void loadModel() {
		List<UrlTemplate> urlTemplates = liveStreamService.getAllUrlTemplates();
		urlTemplates.sort(NAME_ASC);
		dataModel.setObjects(urlTemplates);
		tableEl.reset(false, false, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addUrlTemplateButton) {
			doAddUrlTemplate(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				UrlTemplate urlTemplate = dataModel.getObject(se.getIndex());
				if (CMD_EDIT.equals(cmd)) {
					doEditUrlTemplate(ureq, urlTemplate);
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDeletUrlTemplate(ureq, urlTemplate);
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editUrlTemplateCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if (deleteDialogCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				UrlTemplate urlTemplate = (UrlTemplate)deleteDialogCtrl.getUserObject();
				liveStreamService.deleteUrlTemplate(urlTemplate);
				loadModel();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editUrlTemplateCtrl);
		removeAsListenerAndDispose(cmc);
		cmc = null;
	}

	private void doAddUrlTemplate(UserRequest ureq) {
		editUrlTemplateCtrl = new UrlTemplateEditController(ureq, getWindowControl(), null);
		listenTo(editUrlTemplateCtrl);
		
		String title = translate("url.template.add");
		cmc = new CloseableModalController(getWindowControl(), "close", editUrlTemplateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditUrlTemplate(UserRequest ureq, UrlTemplate urlTemplate) {
		editUrlTemplateCtrl = new UrlTemplateEditController(ureq, getWindowControl(), urlTemplate);
		listenTo(editUrlTemplateCtrl);
		
		String title = translate("url.template.edit.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editUrlTemplateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDeletUrlTemplate(UserRequest ureq, UrlTemplate urlTemplate) {
		String text = translate("url.template.delete.confirm", new String[] { urlTemplate.getName() });
		deleteDialogCtrl = activateYesNoDialog(ureq, translate("url.template.delete"), text, deleteDialogCtrl);
		deleteDialogCtrl.setUserObject(urlTemplate);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
