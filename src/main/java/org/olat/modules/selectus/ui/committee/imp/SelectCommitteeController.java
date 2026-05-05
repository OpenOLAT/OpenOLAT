/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.imp;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.committee.imp.SelectCommitteeTableModel.SelectCols;
import org.olat.modules.selectus.ui.committee.list.PositionCommitteeController;

/**
 * 
 * Initial date: 29 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectCommitteeController extends StepFormBasicController {
	
	private static final String COLUMN_PREFS = "importCommitteeListv1";
	
	private FlexiTableElement tableEl;
	private SelectCommitteeTableModel membersDataModel;
	
	private int count = 0;
	private final SelectionValues rolesValues = new SelectionValues();
	private final ImportCommitteeMembers importCommittee;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public SelectCommitteeController(UserRequest ureq, WindowControl wControl,
			StepsRunContext runContext, Form form, ImportCommitteeMembers importCommittee) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "position_list");
		setTranslator(Util.createPackageTranslator(RecruitingHelper.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.importCommittee = importCommittee;
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(PositionCommitteeController.formIdentifyer, true);

		rolesValues.add(SelectionValues.entry("-", translate("not.imported")));
		rolesValues.add(SelectionValues.entry(PositionRole.member.name(), translate(PositionRole.member.role())));
		rolesValues.add(SelectionValues.entry(PositionRole.secretary.name(), translate(PositionRole.secretary.role())));
		rolesValues.add(SelectionValues.entry(PositionRole.head.name(), translate(PositionRole.head.role())));
		if(recruitingModule.isRoleExOfficioEnabled()) {
			rolesValues.add(SelectionValues.entry(PositionRole.exofficio.name(), translate(PositionRole.exofficio.role())));
		}
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectCols.institution));
		
		RecruitingTableOption userPropertiesOption = recruitingModule.getTableCommitteeUserPropertiesOption();
		if(userPropertiesOption != RecruitingTableOption.disabled) {
			int colIndex = PositionCommitteeController.USER_PROP_OFFSET;
			for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				if (userPropertyHandler == null) continue;
				FlexiColumnModel col = new DefaultFlexiColumnModel(false, userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex++, true, userPropertyHandler.getName());
				columnsModel.addFlexiColumnModel(col);
			}
		}
		DefaultFlexiColumnModel roleCol = new DefaultFlexiColumnModel(SelectCols.role);
		roleCol.setAlwaysVisible(true);
		columnsModel.addFlexiColumnModel(roleCol);
		
		membersDataModel = new SelectCommitteeTableModel(columnsModel, userPropertyHandlers, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", membersDataModel, 24, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setExportEnabled(false);
		tableEl.setAndLoadPersistedPreferences(ureq, COLUMN_PREFS);
	}
	
	private void loadModel() {
		PositionLight positionLight = importCommittee.getSourcePosition();
		Position position = recruitingService.getPosition(positionLight.getKey());
		
		List<CommitteeRow> members = new ArrayList<>();
		List<Identity> secretaries = recruitingService.getSecretaries(position);
		for(Identity secretary: secretaries) {
			members.add(forgeRow(secretary, PositionRole.secretary));
		}

		List<Identity> heads = recruitingService.getHeads(position);
		for(Identity head: heads) {
			members.add(forgeRow(head, PositionRole.head));
		}

		if(recruitingModule.isRoleExOfficioEnabled()) {
			List<Identity> exOfficios = recruitingService.getExOfficios(position);
			for(Identity exOfficio: exOfficios) {
				members.add(forgeRow(exOfficio, PositionRole.exofficio));
			}
		}
		
		List<Identity> membersOnly = recruitingService.getCommitteeMembers(position);
		for(Identity memberOnly: membersOnly) {
			members.add(forgeRow(memberOnly, PositionRole.member));
		}

		membersDataModel.setObjects(members);
		tableEl.reset(true, true, true);
	}
	
	private CommitteeRow forgeRow(Identity member, PositionRole role) {
		CommitteeRow row = new CommitteeRow(member, role);
		SingleSelection roleEl = uifactory.addDropdownSingleselect("role_" + (++count), null, flc,
				rolesValues.keys(), rolesValues.values(), null);
		roleEl.select(role.name(), true);
		row.setSelectRole(roleEl);
		return row;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		importCommittee.clearCommittee();
		List<CommitteeRow> rows = membersDataModel.getObjects();
		for(CommitteeRow row:rows) {
			Identity identity = row.getIdentity();
			String selectKey = row.getSelectRole().getSelectedKey();
			if(!"-".equals(selectKey)) {
				PositionRole role = PositionRole.valueOf(selectKey);
				importCommittee.addCommitteeMember(identity, role);
			}
		}
		fireEvent(ureq, StepsEvent.INFORM_FINISHED);
	}
}
