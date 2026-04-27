/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
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
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.OverviewMembersToContactDataModel.OverviewFeedCols;

/**
 * 
 * Initial date: 6 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectMembersToContactController extends StepFormBasicController {

	private static final String PREFS_ID = "recruitingOverviewFeedbackMembersFlexiList";
	public static final int USER_PROP_OFFSET = 500; //only used in wizard
	public static final String formIdentifyer = "Committee";
	
	private FlexiTableElement tableEl;
	private OverviewMembersToContactDataModel tableModel;
	
	private final AbstractFeedbackMembersContext feedbacksContext;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public SelectMembersToContactController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			Form form, AbstractFeedbackMembersContext feedbacksContext) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "members_overview");
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(formIdentifyer, true);
		this.feedbacksContext = feedbacksContext;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewFeedCols.fullName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewFeedCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OverviewFeedCols.email));
		
		RecruitingTableOption userPropertiesOption = recruitingModule.getTableFeedbacksUserPropertiesOption();
		if(userPropertiesOption != RecruitingTableOption.disabled) {
			boolean visible = userPropertiesOption == RecruitingTableOption.enabled;
			
			int colIndex = USER_PROP_OFFSET;
			for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
				if (userPropertyHandler == null) continue;
				FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex++, true, userPropertyHandler.getName());
				columnsModel.addFlexiColumnModel(col);
			}
		}
		
		tableModel = new OverviewMembersToContactDataModel(columnsModel, userPropertyHandlers, getTranslator(), getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "members", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, PREFS_ID);
		tableEl.setExportEnabled(false);
		tableEl.setElementCssClass("o_sel_position_reference_list");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("apps.feedbacks.emtpy")
				.build());
		tableEl.setPageSize(20);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
	}
	
	private void loadModel() {
		tableModel.setObjects(feedbacksContext.getMembers());
		tableEl.reset(true, true, true);
		tableEl.selectAll();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formNext(UserRequest ureq) {	
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		List<Identity> members = selectedIndex.stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.collect(Collectors.toList());
		feedbacksContext.setSelectedMembers(members);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
