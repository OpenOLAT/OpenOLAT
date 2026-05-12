/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionLightWithStatistics;
import org.olat.modules.selectus.model.PositionMLHelper;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.StatusCellRenderer;
import org.olat.modules.selectus.ui.events.NewPositionEvent;
import org.olat.modules.selectus.ui.position.PositionsDataModel.Fields;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyPositionListController extends FormBasicController {

	private static final String PREFS_ID = "recruitingCopyPositionFlexiList";
	
	private FlexiTableElement tableEl;
	private PositionsDataModel positionsDataModel; 
	
	private CloseableModalController cmc;
	private CopyPositionConfigurationController copyConfigurationCtrl;

	private final List<PositionAttributeDefinition> globalAttributes;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private OrganisationModule organisationModule;
	
	public CopyPositionListController(UserRequest ureq, WindowControl wControl, SortKey[] orderBys) {
		super(ureq, wControl, "copy_position_list", Util.createPackageTranslator(RecruitingHelper.class, ureq.getLocale()));
		globalAttributes = recruitingService.getGlobalAttributeDefinition();
		
		initForm(ureq);
		loadModel(ureq);
		if(orderBys != null && orderBys.length > 0 && orderBys[0] != null) {
			tableEl.sort(orderBys[0]);
		}
		tableEl.reloadData();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.positionTitle));
		if(organisationModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.organisation));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.status, new StatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.deadline, new DateCellRenderer()));
		
		if(recruitingModule.isPositionPlannigIdEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.planingsNumber));
		}
		if(recruitingModule.isPositionDepartmentEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Fields.department));
		}
		
		DefaultFlexiColumnModel copyColumn = new DefaultFlexiColumnModel("table.header.action", translate("copy.configuration"),
				"copy.configuration", "o_icon o_icon_copy");
		copyColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(copyColumn);

		positionsDataModel = new PositionsDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "positions", positionsDataModel, 20, false, getTranslator(), formLayout);
		
		tableEl.setAndLoadPersistedPreferences(ureq, PREFS_ID);
		tableEl.setExportEnabled(false);
		tableEl.setElementCssClass("o_sel_copy_position_list");
		tableEl.setPageSize(40);
		initFilters();
	}
	
	private void initFilters() {
		List<FlexiTableFilter> filters = new ArrayList<>();
		StatusCellRenderer renderer = new StatusCellRenderer();
		filters.add(new FlexiTableFilter(translate("status." + PositionStatus.preparation.name()),
				PositionStatus.preparation.name(), renderer));
		filters.add(new FlexiTableFilter(translate("status." + PositionStatus.published.name()),
				PositionStatus.published.name(), renderer));
		filters.add(new FlexiTableFilter(translate("status." + PositionStatus.publishedAndInScreening.name()),
				PositionStatus.publishedAndInScreening.name(), renderer));
		filters.add(new FlexiTableFilter(translate("status." + PositionStatus.closedAndInScreening.name()),
				PositionStatus.closedAndInScreening.name(), renderer));
		filters.add(new FlexiTableFilter(translate("status." + PositionStatus.closedAndNoRating.name()),
				PositionStatus.closedAndNoRating.name(), renderer));
		filters.add(new FlexiTableFilter(translate("status." + PositionStatus.closed.name()),
				PositionStatus.closed.name(), renderer));

		tableEl.setFilters("", filters, true);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("position.list.empty")
				.build());
	}
	
	private void loadModel(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		List<PositionLightWithStatistics> positions = recruitingService
				.getPositionsLightWithStatistics(getIdentity(), roles, globalAttributes, getLocale());
		positionsDataModel.setObjects(positions);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(copyConfigurationCtrl == source) {
			if(event == Event.DONE_EVENT) {
				Position newPosition = copyConfigurationCtrl.getNewPosition();
				fireEvent(ureq, new NewPositionEvent(newPosition));
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(copyConfigurationCtrl);
		removeControllerListener(cmc);
		copyConfigurationCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				PositionLight position = positionsDataModel.getObject(se.getIndex());
				if("copy.configuration".equals(cmd)) {
					doCopyConfiguration(ureq, position);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doCopyConfiguration(UserRequest ureq, PositionLight position) {
		Position fullPosition = recruitingService.getPosition(position.getKey());
		copyConfigurationCtrl = new CopyPositionConfigurationController(ureq, getWindowControl(), fullPosition);
		listenTo(copyConfigurationCtrl);
		
		String positionTitle = PositionMLHelper.getPositionMLTitle(position, getLocale());
		String title = translate("copy.configuration.of", new String[] { positionTitle });
		cmc = new CloseableModalController(getWindowControl(), "c", copyConfigurationCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
