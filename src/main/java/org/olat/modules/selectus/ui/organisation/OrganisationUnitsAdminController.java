/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.organisation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.OrganisationStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Organisation;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.ui.organisation.OrganisationUnitsAdminDataModel.OrgUnitCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OrganisationUnitsAdminController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private OrganisationUnitsAdminDataModel dataModel;
	
	private CloseableModalController cmc;
	private OrganisationUnitEditController editUnitCtrl;

	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	
	public OrganisationUnitsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl,"orgunit_list");
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean enabled = organisationModule.isEnabled();
		formLayout.contextPut("warningDisabled", Boolean.FALSE);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrgUnitCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrgUnitCols.nameDe));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrgUnitCols.nameFr));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrgUnitCols.staffMail));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OrgUnitCols.staffBcc));
		
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel(OrgUnitCols.edit.i18nHeaderKey(), OrgUnitCols.edit.ordinal(), "edit",
				new StaticFlexiCellRenderer("", "edit", null, "o_icon-lg o_icon_edit", translate("infos")));
		editColumn.setIconHeader("o_icon o_icon-fw o_icon-lg o_icon_edit");
		editColumn.setHeaderLabel(translate("edit"));
		editColumn.setExportable(false);
		editColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(editColumn);
		
		dataModel = new OrganisationUnitsAdminDataModel(columnsModel, getTranslator(), getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 25, false, getTranslator(), formLayout);
		tableEl.setVisible(enabled);
		tableEl.setElementCssClass("o_sel_org_units");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("table.empty.organisation")
				.build());
	}
	
	private void loadModel() {
		List<Organisation> organisations = organisationService.getOrganisations(new OrganisationStatus[] { OrganisationStatus.active });
		List<OrganisationUnit> settings = recruitingService.getOrganisationUnits();
		Map<Organisation,OrganisationUnit> settingsMap = settings.stream()
				.collect(Collectors.toMap(OrganisationUnit::getOrganisation, o -> o, (u, v) -> u));
		
		List<OrganisationSettingsRow> rows = new ArrayList<>();
		for(Organisation organisation:organisations) {
			OrganisationUnit setting = settingsMap.get(organisation);
			rows.add(new OrganisationSettingsRow(organisation, setting));
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editUnitCtrl == source) {
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
		removeAsListenerAndDispose(cmc);
		editUnitCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if("edit".equals(se.getCommand())) {
					OrganisationSettingsRow row = dataModel.getObject(se.getIndex());
					doEditOrganisationUnit(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doEditOrganisationUnit(UserRequest ureq, OrganisationSettingsRow row) {
		OrganisationUnit unit = row.setting();
		if(unit == null) {
			unit = recruitingService.createOrganisationUnit(row.organisation());
		}
		editUnitCtrl = new OrganisationUnitEditController(ureq, getWindowControl(), unit);
		listenTo(editUnitCtrl);
		
		String title = translate("edit.organisation.unit.title", new String[] { row.organisation().getDisplayName() });
		cmc = new CloseableModalController(getWindowControl(), "c", editUnitCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
}
