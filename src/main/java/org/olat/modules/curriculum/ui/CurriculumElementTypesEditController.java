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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeManagedFlag;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumElementTypesTableModel.TypesCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementTypesEditController extends FormBasicController implements Activateable2 {
	
	private FormLink addNewElementTypeButton;
	private FlexiTableElement tableEl;
	private CurriculumElementTypesTableModel model;
	
	private ToolsController toolsCtrl;
	private TypeNamesCalloutController typeNamesCalloutCtrl;
	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteDialog;
	private EditCurriculumElementTypeController rootElementTypeCtrl;
	private EditCurriculumElementTypeController editElementTypeCtrl;
	protected CloseableCalloutWindowController toolsCalloutCtrl;
	protected CloseableCalloutWindowController typeNamesCalloutWindowCtrl;

	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumElementTypesEditController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_types");
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addNewElementTypeButton = uifactory.addFormLink("add.new.element.type", formLayout, Link.BUTTON);
		addNewElementTypeButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TypesCols.key));
		DefaultFlexiColumnModel displayNameCol = new DefaultFlexiColumnModel(TypesCols.displayName);
		displayNameCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(displayNameCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TypesCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.forUseAs));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.typeOfElement));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.content));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.parents));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.children));
		
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("edit", -1);
		editColumn.setCellRenderer(new StaticFlexiCellRenderer(null, "edit", null, "o_icon o_icon-lg o_icon_edit", translate("edit")));
		editColumn.setIconHeader("o_icon o_icon-lg o_icon_edit");
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);
		
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(TypesCols.tools));
		
		model = new CurriculumElementTypesTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "types", model, 25, false, getTranslator(), formLayout);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("table.type.empty")
				.build());
		tableEl.setAndLoadPersistedPreferences(ureq, "cur-el-types");
	}
	
	private void loadModel() {
		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
		List<CurriculumElementTypeToType> relations = curriculumService.getAllCurriculumElementTypeRelations();
		Map<Long, List<CurriculumElementType>> parentTypesBySubTypeKey = new HashMap<>();
		Map<Long, List<CurriculumElementType>> childTypesByParentTypeKey = new HashMap<>();
		for(CurriculumElementTypeToType relation : relations) {
			Long subTypeKey = relation.getAllowedSubType().getKey();
			parentTypesBySubTypeKey
				.computeIfAbsent(subTypeKey, k -> new ArrayList<>())
				.add(relation.getType());
			Long parentTypeKey = relation.getType().getKey();
			childTypesByParentTypeKey
				.computeIfAbsent(parentTypeKey, k -> new ArrayList<>())
				.add(relation.getAllowedSubType());
		}
		List<CurriculumElementTypeRow> rows = types.stream()
				.map(t -> forgeRow(t,
						parentTypesBySubTypeKey.getOrDefault(t.getKey(), List.of()),
						childTypesByParentTypeKey.getOrDefault(t.getKey(), List.of())))
				.collect(Collectors.toList());
		model.setObjects(rows);
		tableEl.reset(false, true, true);
	}

	private CurriculumElementTypeRow forgeRow(CurriculumElementType type, List<CurriculumElementType> parents, List<CurriculumElementType> children) {
		CurriculumElementTypeRow row = new CurriculumElementTypeRow(type);
		String forUseAsKey;
		if(type.isImplOnly()) {
			forUseAsKey = "table.type.for.use.as.implementation";
		} else if(!type.isAllowedAsRootElement()) {
			forUseAsKey = "table.type.for.use.as.element";
		} else {
			forUseAsKey = "table.type.for.use.as.implementation.or.element";
		}
		row.setForUseAsLabel(translate(forUseAsKey));
		String typeOfElementKey = type.isSingleElement()
				? "table.type.type.of.element.single.element"
				: "table.type.type.of.element.structural.element";
		row.setTypeOfElementLabel(translate(typeOfElementKey));
		String contentKey;
		if(type.getMaxRepositoryEntryRelations() == 0) {
			contentKey = "table.type.content.no.content";
		} else if(type.getMaxRepositoryEntryRelations() == 1) {
			contentKey = "table.type.content.single.course";
		} else if(type.getMaxRepositoryEntryRelations() == -1) {
			contentKey = "table.type.content.course.bundle";
		} else {
			contentKey = null;
		}
		row.setContentLabel(contentKey != null ? translate(contentKey) : null);
		row.setParentTypes(parents);
		if(!parents.isEmpty()) {
			FormLink parentsLink = uifactory.addFormLink("parents_" + type.getKey(), "parents",
					String.valueOf(parents.size()), null, null, Link.NONTRANSLATED);
			parentsLink.setUserObject(row);
			row.setParentsLink(parentsLink);
		}
		row.setChildTypes(children);
		if(!children.isEmpty()) {
			FormLink childrenLink = uifactory.addFormLink("children_" + type.getKey(), "children",
					String.valueOf(children.size()), null, null, Link.NONTRANSLATED);
			childrenLink.setUserObject(row);
			row.setChildrenLink(childrenLink);
		}
		if(isToolsEnable(type)) {
			FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
			toolsLink.setUserObject(row);
			row.setToolsLink(toolsLink);
		}
		return row;
	}
	
	private boolean isToolsEnable(CurriculumElementType type) {
		return !CurriculumElementTypeManagedFlag.isManaged(type.getManagedFlags(), CurriculumElementTypeManagedFlag.copy)
				|| !CurriculumElementTypeManagedFlag.isManaged(type.getManagedFlags(), CurriculumElementTypeManagedFlag.delete);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(rootElementTypeCtrl == source || editElementTypeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDeleteDialog == source) {
			if (DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				CurriculumElementTypeRow row = (CurriculumElementTypeRow)confirmDeleteDialog.getUserObject();
				doDelete(row);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(rootElementTypeCtrl);
		removeAsListenerAndDispose(cmc);
		rootElementTypeCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addNewElementTypeButton == source) {
			doAddNewElementType(ureq);
		} else if (source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("tools".equals(cmd) && link.getUserObject() instanceof CurriculumElementTypeRow row) {
				doOpenTools(ureq, row, link);
			} else if("parents".equals(cmd) && link.getUserObject() instanceof CurriculumElementTypeRow row) {
				doOpenParents(ureq, row, link);
			} else if("children".equals(cmd) && link.getUserObject() instanceof CurriculumElementTypeRow row) {
				doOpenChildren(ureq, row, link);
			}
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if("edit".equals(cmd)) {
					CurriculumElementTypeRow row = model.getObject(se.getIndex());
					doEditCurriculElementType(ureq, row.getType());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumElementTypeRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElementType type = curriculumService.getCurriculumElementType(row);
		if(type == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.type.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row, type);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}

	private void doOpenParents(UserRequest ureq, CurriculumElementTypeRow row, FormLink link) {
		doOpenTypeNamesCallout(ureq, link, translate("table.type.callout.child.of"), row.getParentTypes());
	}

	private void doOpenChildren(UserRequest ureq, CurriculumElementTypeRow row, FormLink link) {
		doOpenTypeNamesCallout(ureq, link, translate("table.type.callout.parent.of"), row.getChildTypes());
	}

	private void doOpenTypeNamesCallout(UserRequest ureq, FormLink link, String title, List<CurriculumElementType> types) {
		removeAsListenerAndDispose(typeNamesCalloutCtrl);
		removeAsListenerAndDispose(typeNamesCalloutWindowCtrl);

		typeNamesCalloutCtrl = new TypeNamesCalloutController(ureq, getWindowControl(), title, types);
		listenTo(typeNamesCalloutCtrl);
		typeNamesCalloutWindowCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				typeNamesCalloutCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(typeNamesCalloutWindowCtrl);
		typeNamesCalloutWindowCtrl.activate();
	}

	private void doAddNewElementType(UserRequest ureq) {
		rootElementTypeCtrl = new EditCurriculumElementTypeController(ureq, getWindowControl(), null);
		listenTo(rootElementTypeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), 
				rootElementTypeCtrl.getInitialComponent(), true, translate("add.new.element.type"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditCurriculElementType(UserRequest ureq, CurriculumElementTypeRef type) {
		CurriculumElementType reloadedType = curriculumService.getCurriculumElementType(type);
		editElementTypeCtrl = new EditCurriculumElementTypeController(ureq, getWindowControl(), reloadedType);
		listenTo(editElementTypeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editElementTypeCtrl.getInitialComponent(), true, translate("edit"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCopy(CurriculumElementTypeRow row) {
		curriculumService.cloneCurriculumElementType(row);
		loadModel();
		showInfo("info.copy.element.type.sucessfull", row.getDisplayName());
	}
	
	private void doConfirmDelete(UserRequest ureq, CurriculumElementTypeRow row) {
		String[] args = new String[] { StringHelper.escapeHtml(row.getDisplayName()) };
		String title = translate("confirmation.delete.type.title", args);
		String text = translate("confirmation.delete.type", args);
		confirmDeleteDialog = activateOkCancelDialog(ureq, title, text, confirmDeleteDialog);
		confirmDeleteDialog.setUserObject(row);
	}
	
	private void doDelete(CurriculumElementTypeRow row) {
		if(curriculumService.deleteCurriculumElementType(row)) {
			showInfo("confirm.delete.element.type.sucessfull", row.getDisplayName());
			loadModel();
			tableEl.reset(true, true, true);
		} else {
			showWarning("warning.delete.element.type", row.getDisplayName());
		}
	}

	private class ToolsController extends BasicController {
		
		private final CurriculumElementTypeRow row;

		private final VelocityContainer mainVC;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CurriculumElementTypeRow row, CurriculumElementType type) {
			super(ureq, wControl);
			setTranslator(CurriculumElementTypesEditController.this.getTranslator());
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();
			addLink("edit", "edit", "o_icon o_icon-fw o_icon_edit", links);
			if(!CurriculumElementTypeManagedFlag.isManaged(type.getManagedFlags(), CurriculumElementTypeManagedFlag.copy)) {
				addLink("details.copy", "copy", "o_icon o_icon-fw o_icon_copy", links);
			}
			if(!CurriculumElementTypeManagedFlag.isManaged(type.getManagedFlags(), CurriculumElementTypeManagedFlag.delete)) {
				links.add("-");
				addLink("details.delete", "delete", "o_icon o_icon-fw o_icon_delete_item", links);
			}

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS, List<String> links) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			mainVC.put(name, link);
			links.add(name);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link link) {
				String cmd = link.getCommand();
				if("edit".equals(cmd)) {
					close();
					doEditCurriculElementType(ureq, row);
				} else if("copy".equals(cmd)) {
					close();
					doCopy(row);
				} else if("delete".equals(cmd)) {
					close();
					doConfirmDelete(ureq, row);
				}
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}

	private class TypeNamesCalloutController extends BasicController {

		private final VelocityContainer mainVC;

		public TypeNamesCalloutController(UserRequest ureq, WindowControl wControl, String title, List<CurriculumElementType> types) {
			super(ureq, wControl);
			setTranslator(CurriculumElementTypesEditController.this.getTranslator());
			mainVC = createVelocityContainer("element_type_names_callout");
			mainVC.contextPut("title", title);
			mainVC.contextPut("items", types);
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
}
