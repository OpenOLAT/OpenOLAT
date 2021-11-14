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
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CircleCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.updown.UpDown;
import org.olat.core.gui.components.updown.UpDownEvent;
import org.olat.core.gui.components.updown.UpDownEvent.Direction;
import org.olat.core.gui.components.updown.UpDownFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategorySearchParams;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.ui.ColorCategoryDataModel.ColorCategoryCols;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ColorCategoryAdminController extends FormBasicController {
	
	private static final ColorCategorySearchParams SEARCH_PARAMS = ColorCategorySearchParams.builder().addColorTypes().build();
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";
	
	private FormLink addColorCategoryLink;
	private FlexiTableElement tableEl;
	private ColorCategoryDataModel dataModel;
	
	private CloseableModalController cmc;
	private ColorCategoryEditController editCtrl;
	private DialogBoxController deleteDialogCtrl;
	
	@Autowired
	private CourseStyleService courseStyleService;

	public ColorCategoryAdminController(UserRequest ureq, WindowControl wControl) {
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
		
		addColorCategoryLink = uifactory.addFormLink("color.category.add", topCont, Link.BUTTON);
		addColorCategoryLink.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ColorCategoryCols.upDown));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ColorCategoryCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ColorCategoryCols.translaton));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ColorCategoryCols.enabled));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ColorCategoryCols.color, new CircleCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ColorCategoryCols.cssClass));
		DefaultFlexiColumnModel editCol = new DefaultFlexiColumnModel(ColorCategoryCols.edit.i18nHeaderKey(),
				ColorCategoryCols.edit.ordinal(), CMD_EDIT,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(ColorCategoryCols.edit.i18nHeaderKey()), CMD_EDIT), null));
		columnsModel.addFlexiColumnModel(editCol);
		DefaultFlexiColumnModel deleteCol = new DefaultFlexiColumnModel(ColorCategoryCols.delete.i18nHeaderKey(),
				ColorCategoryCols.delete.ordinal(), CMD_DELETE,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(ColorCategoryCols.delete.i18nHeaderKey()), CMD_DELETE), null));
		columnsModel.addFlexiColumnModel(deleteCol);
		
		dataModel = new ColorCategoryDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "coourse-color-categories");
	}
	
	private void loadModel() {
		List<ColorCategory> colorCategories = courseStyleService.getColorCategories(SEARCH_PARAMS);
		Collections.sort(colorCategories);
		List<ColorCategoryRow> rows = new ArrayList<>(colorCategories.size());
		for (int i = 0; i < colorCategories.size(); i++) {
			ColorCategory colorCategory = colorCategories.get(i);
			ColorCategoryRow row = new ColorCategoryRow(colorCategory);
			String translation = CourseStyleUIFactory.translate(getTranslator(), colorCategory);
			row.setTranslation(translation);
			
			UpDown upDown = UpDownFactory.createUpDown("up_down_" + colorCategory.getKey(), UpDown.Layout.LINK_HORIZONTAL, flc.getFormItemComponent(), this);
			upDown.setUserObject(colorCategory);
			if (i == 0) {
				upDown.setTopmost(true);
			} else if (i == colorCategories.size() - 1) {
				upDown.setLowermost(true);
			} 
			row.setUpDown(upDown);
			
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof UpDownEvent && source instanceof UpDown) {
			UpDownEvent ude = (UpDownEvent) event;
			UpDown upDown = (UpDown)source;
			Object userObject = upDown.getUserObject();
			if (userObject instanceof ColorCategory) {
				ColorCategory colorCategory = (ColorCategory)userObject;
				doMove(colorCategory, ude.getDirection());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addColorCategoryLink == source) {
			doAddColorCategory(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ColorCategoryRow row = dataModel.getObject(se.getIndex());
				if(CMD_EDIT.equals(cmd)) {
					doEditColorCategory(ureq, row.getColorCategory());
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDeletion(ureq, row.getColorCategory());
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
				ColorCategory colorCategory = (ColorCategory)deleteDialogCtrl.getUserObject();
				doDelete(colorCategory);
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

	private void doAddColorCategory(UserRequest ureq) {
		editCtrl = new ColorCategoryEditController(ureq, getWindowControl(), null);
		listenTo(editCtrl);
		
		String title = translate("color.category.add.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true,
				title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditColorCategory(UserRequest ureq, ColorCategory colorCategory) {
		editCtrl = new ColorCategoryEditController(ureq, getWindowControl(), colorCategory);
		listenTo(editCtrl);
		
		String title = translate("color.category.edit.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true,
				title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doMove(ColorCategory colorCategory, Direction direction) {
		courseStyleService.doMove(colorCategory, Direction.UP == direction);
		loadModel();
	}

	private void doConfirmDeletion(UserRequest ureq, ColorCategory colorCategory) {
		String title = translate("color.category.delete.confirm.title");
		String translatedType = CourseStyleUIFactory.translate(getTranslator(), colorCategory);
		String text = translate("color.category.delete.confirm.text", new String[] { translatedType });
		deleteDialogCtrl = activateYesNoDialog(ureq, title, text, deleteDialogCtrl);
		deleteDialogCtrl.setUserObject(colorCategory);
	}

	private void doDelete(ColorCategory colorCategory) {
		courseStyleService.deleteColorCategory(colorCategory);
	}

}
