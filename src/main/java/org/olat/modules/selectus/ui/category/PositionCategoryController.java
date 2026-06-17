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
package org.olat.modules.selectus.ui.category;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.category.CategoryDataModel.CategoryCols;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.position.PositionEditableController;

/**
 * 
 * Initial date: 12 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionCategoryController extends FormBasicController implements PositionEditableController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private FormLink addPositionCategoryButton;
	private FlexiTableElement systemTableEl;
	private CategoryDataModel systemTableModel;
	private MultipleSelectionElement systemTagsEl;

	private FlexiTableElement positionTableEl;
	private CategoryDataModel positionTableModel;
	private MultipleSelectionElement positionTagsEl;
	
	private Position position;
	
	private CloseableModalController cmc;
	private EditCategoryController editCategoryCtrl;
	private ConfirmDeleteCategoryController confirmDeleteCategoryCtrl;
	private ConfirmDisableCategoriesController confirmDisableCategoryCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionCategoryController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl, "position_categories", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		
		initForm(ureq);
		loadSystemModel();
		loadPositionModel();
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		position = updatedPosition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(recruitingModule.isSystemTagsEnabled()) {
			String[] onValues = new String[] { translate("system.tags.enabled") };
			systemTagsEl = uifactory.addCheckboxesHorizontal("system.tags.enabled", null, formLayout, onKeys, onValues);
			systemTagsEl.addActionListener(FormEvent.ONCHANGE);
			if(position.isSystemTagsEnabled()) {
				systemTagsEl.select(onKeys[0], true);
			}
			
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CategoryCols.category));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CategoryCols.color, new CategoryColorCellRenderer()));

			systemTableModel = new CategoryDataModel(columnsModel, getLocale());
			systemTableEl = uifactory.addTableElement(getWindowControl(), "systemCategories", systemTableModel, 40, false, getTranslator(), formLayout);
			setDefaultTableElementSettings(systemTableEl);
			systemTableEl.setVisible(systemTagsEl.isAtLeastSelected(1));
		}
		
		if(recruitingModule.isPositionTagsEnabled()) {
			String[] onValues = new String[] { translate("position.tags.enabled") };
			positionTagsEl = uifactory.addCheckboxesHorizontal("position.tags.enabled", null, formLayout, onKeys, onValues);
			positionTagsEl.addActionListener(FormEvent.ONCHANGE);
			if(position.isPositionTagsEnabled()) {
				positionTagsEl.select(onKeys[0], true);
			}
			
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CategoryCols.category));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CategoryCols.color, new CategoryColorCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
			
			positionTableModel = new CategoryDataModel(columnsModel, getLocale());
			positionTableEl = uifactory.addTableElement(getWindowControl(), "positionCategories", positionTableModel, 40, false, getTranslator(), formLayout);
			setDefaultTableElementSettings(positionTableEl);
			positionTableEl.setVisible(positionTagsEl.isAtLeastSelected(1));
			
			addPositionCategoryButton = uifactory.addFormLink("add.position.category", formLayout, Link.BUTTON);
			addPositionCategoryButton.setIconLeftCSS("o_icon o_icon_add");
			addPositionCategoryButton.setVisible(positionTagsEl.isAtLeastSelected(1));
		}
	}
	
	private void setDefaultTableElementSettings(FlexiTableElement tableEl) {
		tableEl.setExportEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("category.table.empty")
				.build());
		tableEl.setSortSettings(new FlexiTableSortOptions(true, new SortKey(CategoryCols.category.name(), true)));
	}
	
	private void loadSystemModel() {
		if(systemTableModel != null) {
			List<Category> categories = taggingService.getSystemCategories();
			systemTableModel.setObjects(categories);
			systemTableEl.reset(true, true, true);
		}
	}
	
	private void loadPositionModel() {
		if(positionTableModel != null) {
			List<Category> categories = taggingService.getPositionCategories(position);
			positionTableModel.setObjects(categories);
			positionTableEl.reset(true, true, true);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editCategoryCtrl == source || confirmDeleteCategoryCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadPositionModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDisableCategoryCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doDisableCategories(ureq, confirmDisableCategoryCtrl.getType());
			} else if(event == Event.CANCELLED_EVENT) {
				doCancelCategories(confirmDisableCategoryCtrl.getType());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDisableCategoryCtrl);
		removeAsListenerAndDispose(confirmDeleteCategoryCtrl);
		removeAsListenerAndDispose(editCategoryCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDisableCategoryCtrl = null;
		confirmDeleteCategoryCtrl = null;
		editCategoryCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(systemTagsEl == source) {
			systemTableEl.setVisible(systemTagsEl.isAtLeastSelected(1));
			doConfirmCategories(ureq, CategoriesType.system, systemTagsEl.isAtLeastSelected(1));
		} else if(positionTagsEl == source) {
			positionTableEl.setVisible(positionTagsEl.isAtLeastSelected(1));
			addPositionCategoryButton.setVisible(positionTagsEl.isAtLeastSelected(1));
			doConfirmCategories(ureq, CategoriesType.position, positionTagsEl.isAtLeastSelected(1));
		} else if(addPositionCategoryButton == source) {
			doNewCategory(ureq);
		} else if(positionTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					Category row = positionTableModel.getObject(se.getIndex());
					doConfirmDelete(ureq, row);
				} else if("edit".equals(se.getCommand())) {
					Category row = positionTableModel.getObject(se.getIndex());
					doEditCategory(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmCategories(UserRequest ureq, CategoriesType type, boolean enable) {
		if(enable) {
			commit(ureq);
		} else {
			boolean needConfirmation = taggingService.hasApplicationCategories(position,
						type == CategoriesType.system, type == CategoriesType.position);
			if(needConfirmation) {
				confirmDisableCategoryCtrl = new ConfirmDisableCategoriesController(ureq, getWindowControl(), type);
				listenTo(confirmDisableCategoryCtrl);
				
				String title = type == CategoriesType.position ? translate("disable.position.categories") : translate("disable.system.categories");
				cmc = new CloseableModalController(getWindowControl(), "c", confirmDisableCategoryCtrl.getInitialComponent(), title);
				listenTo(cmc);
				cmc.activate();
			} else {
				commit(ureq);
			}
		}
	}
	
	private void doDisableCategories(UserRequest ureq, CategoriesType type) {
		commit(ureq);
		taggingService.removeApplicationCategories(position, type == CategoriesType.system, type == CategoriesType.position);
	}
	
	private void doCancelCategories(CategoriesType type) {
		if(type == CategoriesType.position) {
			positionTagsEl.select(onKeys[0], true);
			positionTableEl.setVisible(true);
		} else {
			systemTagsEl.select(onKeys[0], true);
			systemTableEl.setVisible(true);
		}
	}
	
	private void doNewCategory(UserRequest ureq) {
		if(guardModalController(editCategoryCtrl)) return;
		
		editCategoryCtrl = new EditCategoryController(ureq, getWindowControl(), position);
		listenTo(editCategoryCtrl);
		
		String title = translate("add.category");
		cmc = new CloseableModalController(getWindowControl(), "c", editCategoryCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditCategory(UserRequest ureq, Category category) {
		if(guardModalController(editCategoryCtrl)) return;
		
		editCategoryCtrl = new EditCategoryController(ureq, getWindowControl(), category);
		listenTo(editCategoryCtrl);
		
		String title = translate("edit.category", new String[] { category.getName() });
		cmc = new CloseableModalController(getWindowControl(), "c", editCategoryCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, Category category) {
		Category reloadedCategory = taggingService.getCategoryByKey(category.getKey());
		if(reloadedCategory == null) {
			loadPositionModel();
		} else {
			if(guardModalController(confirmDeleteCategoryCtrl)) return;
			
			confirmDeleteCategoryCtrl = new ConfirmDeleteCategoryController(ureq, getWindowControl(), reloadedCategory);
			listenTo(confirmDeleteCategoryCtrl);
	
			String title = translate("confirm.delete.category.title");
			cmc = new CloseableModalController(getWindowControl(), "c", confirmDeleteCategoryCtrl.getInitialComponent(), title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void commit(UserRequest ureq) {
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
		}
		
		String before = auditService.toAuditXml(position);
		
		boolean systemTagsEnabled = systemTagsEl != null && systemTagsEl.isAtLeastSelected(1);
		if(systemTagsEnabled != position.isSystemTagsEnabled()) {
			logAudit("System tags " + (systemTagsEnabled ? "enabled" : "disabled") + " for position: " + position.toString(), null);
		}
		position.setSystemTagsEnabled(systemTagsEnabled);
		
		boolean positionTagsEnabled = positionTagsEl != null && positionTagsEl.isAtLeastSelected(1);
		if(positionTagsEnabled != position.isPositionTagsEnabled()) {
			logAudit("System tags " + (positionTagsEnabled ? "enabled" : "disabled") + " for position: " + position.toString(), null);
		}
		position.setPositionTagsEnabled(positionTagsEnabled);

		position = recruitingService.savePosition(position);
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Update referees / experts position: " + position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}
	
	public enum CategoriesType {
		system,
		position
	}
}
