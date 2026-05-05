/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceToApplication;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.ReferenceStatusCellRenderer;
import org.olat.modules.selectus.ui.reference.SelectForInvitationEmailDataModel.IRCols;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Initial date: 20.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbstractInvitationEmailController extends StepFormBasicController {
	
	protected FlexiTableElement tableEl;
	protected SelectForInvitationEmailDataModel dataModel;
	
	private SortKey sortKey;
	private final List<Reference> references;
	
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public AbstractInvitationEmailController(UserRequest ureq, WindowControl wControl, List<Reference> references,
			SortKey sortKey, StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.references = references;
		this.sortKey = sortKey;
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IRCols.fullName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IRCols.email));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IRCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IRCols.application));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IRCols.status, new ReferenceStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IRCols.submissionDeadline, new DateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IRCols.dateInvitation, new DateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IRCols.dateLastReminder, new DateCellRenderer()));
		
		dataModel = new SelectForInvitationEmailDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(false);
		tableEl.setElementCssClass("o_sel_position_invitation_list");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
					.withMessageI18nKey("recommendation.list.empty")
					.build());
		tableEl.setPageSize(40);
		if(!isSortKeyValid()) {
			sortKey = new SortKey(IRCols.fullName.sortKey(), true);
		}
		tableEl.setSortSettings(new FlexiTableSortOptions(true, sortKey));
		tableEl.reset(true, true, true);
	}
	
	private boolean isSortKeyValid() {
		if(sortKey == null) return false;
		
		try {
			IRCols col = IRCols.valueOf(sortKey.getKey());
			return col != null;
		} catch (Exception e) {
			return false;
		}
	}
	
	protected void loadModel() {
		List<ReferenceInvitationRow> rows = references.stream()
				.map(ReferenceInvitationRow::new)
				.collect(Collectors.toList());
		Map<Long,ReferenceInvitationRow> refToRows = rows.stream()
				.collect(Collectors.toMap(ReferenceInvitationRow::getReferenceKey, row -> row, (u, v) -> u));
		
		List<ReferenceToApplication> refToApplications = recruitingService.getReferenceToApplications(references);
		for(ReferenceToApplication refToApplication:refToApplications) {
			ReferenceInvitationRow row = refToRows.get(refToApplication.getReference().getKey());
			if(row != null) {
				row.addApplication(refToApplication.getApplication());
			}
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}	
}