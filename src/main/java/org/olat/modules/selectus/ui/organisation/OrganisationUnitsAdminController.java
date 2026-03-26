/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.organisation.OrganisationUnitsAdminDataModel.OrgUnitCols;

/**
 * 
 * Initial date: 13 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
//TODO selectus replace 
public class OrganisationUnitsAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	
	private FlexiTableElement tableEl;
	private MultipleSelectionElement enableEl;
	private OrganisationUnitsAdminDataModel dataModel;
	private FormLink addOrgUnitButton;
	private FormLink mergeOrgUnitButton;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteBox;
	private OrganisationUnitEditController editUnitCtrl;
	private MergeOrganisationUnitsController mergeCtrl;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingFrontendManager;
	
	public OrganisationUnitsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl,"orgunit_list");
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean enabled = recruitingModule.isOrganisationUnitEnabled();
		formLayout.contextPut("warningDisabled", Boolean.FALSE);

		if(ureq.getUserSession().getRoles().isSystemAdmin()) {
			String[] onValues = new String[]{ translate("enable.organisation.unit") };
			enableEl = uifactory.addCheckboxesHorizontal("enable.org.unit", "enable.organisation.unit.label", formLayout, onKeys, onValues);
			enableEl.addActionListener(FormEvent.ONCHANGE);
			if(enabled) {
				enableEl.select(onKeys[0], true);
			}
		} else if(!enabled) {
			formLayout.contextPut("warningDisabled", Boolean.TRUE);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrgUnitCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrgUnitCols.nameDe));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrgUnitCols.nameFr));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrgUnitCols.staffMail));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrgUnitCols.staffBcc));

		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("edit", translate("edit"), "edit", "o_icon o_icon_edit");
		editColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(editColumn);

		DefaultFlexiColumnModel deleteColumn = new DefaultFlexiColumnModel("delete", translate("delete"), "delete", "o_icon o_icon_delete_item");
		deleteColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(deleteColumn);
		
		dataModel = new OrganisationUnitsAdminDataModel(columnsModel, getTranslator(), getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 25, false, getTranslator(), formLayout);
		tableEl.setVisible(enabled);
		tableEl.setMultiSelect(true);
		tableEl.setElementCssClass("o_sel_org_units");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("table.empty.organisation")
				.build());
		
		addOrgUnitButton = uifactory.addFormLink("add.organisation.unit", formLayout, Link.BUTTON);
		addOrgUnitButton.setElementCssClass("o_sel_org_unit_add");
		addOrgUnitButton.setVisible(enabled);
		
		mergeOrgUnitButton = uifactory.addFormLink("merge.organisation.unit", formLayout, Link.BUTTON);
		mergeOrgUnitButton.setElementCssClass("o_sel_org_unit_merge");
		mergeOrgUnitButton.setVisible(enabled);
	}
	
	private void loadModel() {
		List<OrganisationUnit> units = recruitingFrontendManager.getOrganisationUnits();
		dataModel.setObjects(units);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		// 
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteBox == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				OrganisationUnit unit = (OrganisationUnit)confirmDeleteBox.getUserObject();
				doDelete(unit);
				loadModel();
			}
		} else if(editUnitCtrl == source || mergeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editUnitCtrl);
		removeAsListenerAndDispose(mergeCtrl);
		removeAsListenerAndDispose(cmc);
		editUnitCtrl = null;
		mergeCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addOrgUnitButton == source) {
			doAddOrganisationUnit(ureq);
		} else if(mergeOrgUnitButton == source) {
			doMergeOrganisationUnits(ureq);
		} else if(enableEl == source) {
			boolean enabled = enableEl.isAtLeastSelected(1);
			tableEl.setVisible(enabled);
			addOrgUnitButton.setVisible(enabled);
			recruitingModule.setOrganisationUnitEnabled(enabled);
			showInfo("info.saved");
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("delete".equals(se.getCommand())) {
					OrganisationUnit row = dataModel.getObject(se.getIndex());
					doConfirmDelete(ureq, row);
				} else if("edit".equals(se.getCommand())) {
					OrganisationUnit row = dataModel.getObject(se.getIndex());
					doEditOrganisationUnit(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAddOrganisationUnit(UserRequest ureq) {
		editUnitCtrl = new OrganisationUnitEditController(ureq, getWindowControl());
		listenTo(editUnitCtrl);
		
		String title = translate("add.organisation.unit.title");
		cmc = new CloseableModalController(getWindowControl(), "c", editUnitCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doEditOrganisationUnit(UserRequest ureq, OrganisationUnit unit) {
		editUnitCtrl = new OrganisationUnitEditController(ureq, getWindowControl(), unit);
		listenTo(editUnitCtrl);
		
		String title = translate("edit.organisation.unit.title", new String[] { unit.getName() });
		cmc = new CloseableModalController(getWindowControl(), "c", editUnitCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmDelete(UserRequest ureq, OrganisationUnit unit) {
		List<Position> positions = recruitingFrontendManager.getPositions(unit);
		String title = translate("confirm.delete.organisation.unit.title");
		String text = translate("confirm.delete.organisation.unit", new String[]{ unit.getName(), Integer.toString(positions.size()) });
		confirmDeleteBox = activateYesNoDialog(ureq, title, text, confirmDeleteBox);
		confirmDeleteBox.setUserObject(unit);
	}
	
	private void doMergeOrganisationUnits(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		if(selectedIndex.size() < 2) {
			showWarning("warning.atleast.two");
		} else {
			List<OrganisationUnit> orgUnits = new ArrayList<>(selectedIndex.size());
			for(Integer index:selectedIndex) {
				orgUnits.add(dataModel.getObject(index));
			}
			mergeCtrl = new MergeOrganisationUnitsController(ureq, getWindowControl(), orgUnits);
			listenTo(mergeCtrl);
			
			String title = translate("merge.organisation.unit");
			cmc = new CloseableModalController(getWindowControl(), "c", mergeCtrl.getInitialComponent(), title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doDelete(OrganisationUnit unit) {
		recruitingFrontendManager.deleteOrganisationUnit(unit);
		showInfo("deleted");
	}
}
