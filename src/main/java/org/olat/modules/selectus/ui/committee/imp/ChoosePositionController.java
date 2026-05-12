/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionLightWithStatistics;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.StatusCellRenderer;
import org.olat.modules.selectus.ui.position.PositionsDataModel;
import org.olat.modules.selectus.ui.position.PositionsDataModel.Fields;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChoosePositionController extends StepFormBasicController {
	
	private FlexiTableElement tableEl;
	private PositionsDataModel positionsDataModel;

	private final ChoosePosition importCommittee;

	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private OrganisationModule organisationModule;
	
	public ChoosePositionController(UserRequest ureq, WindowControl wControl,
			StepsRunContext runContext, Form form, ChoosePosition importCommittee) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "position_list");
		setTranslator(Util.createPackageTranslator(RecruitingHelper.class, getLocale(), getTranslator()));
		this.importCommittee = importCommittee;
		initForm(ureq);
		loadModel(ureq);
	}
	
	@Override
	public void setFormDescription(String i18nKey) {
		super.setFormDescription(i18nKey);
	}
	
	@Override
	public void setFormTranslatedDescription(String text) {
		super.setFormTranslatedDescription(text);
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
		
		DefaultFlexiColumnModel copyColumn = new DefaultFlexiColumnModel("table.header.action", translate("select.position"),
				"select", "o_icon o_icon-fw o_icon_files");
		copyColumn.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(copyColumn);

		positionsDataModel = new PositionsDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", positionsDataModel, 20, false, getTranslator(), formLayout);
		
		tableEl.setExportEnabled(false);
		tableEl.setElementCssClass("o_sel_select_position_list");
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
		Set<Long> excludedPositions = importCommittee.getExcludedPositions().stream()
				.map(Position::getKey)
				.collect(Collectors.toSet());
		
		Roles roles = ureq.getUserSession().getRoles();
		List<PositionLightWithStatistics> allPositions = recruitingService
				.getPositionsLightWithStatistics(getIdentity(), roles, List.of(), getLocale());
		List<PositionLightWithStatistics> positions = new ArrayList<>(allPositions.size());
		for(PositionLightWithStatistics position:allPositions) {
			if(!excludedPositions.contains(position.getKey())) {
				positions.add(position);
			}	
		}
		positionsDataModel.setObjects(positions);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		tableEl.clearError();
		if(importCommittee.getSelectedPosition() == null) {
			tableEl.setErrorKey("error.need.position");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				PositionLight position = positionsDataModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					importCommittee.setSelectedPosition(position);
					fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	

}
