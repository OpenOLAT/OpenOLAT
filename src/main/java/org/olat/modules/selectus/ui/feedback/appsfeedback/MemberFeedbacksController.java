/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingTableOption;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.RecruitingPositionSecurityCallbackForReviewer;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.feedback.appsfeedback.MemberFeedbacksTableModel.MemberFeedCols;

/**
 * The list of feedbacks request for a faculty member. Appears
 * at the root of the application.
 * 
 * Initial date: 29 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberFeedbacksController extends FormBasicController {
	
	private static final String PREFS_ID = "recruitingMemFeedbackMembersFlexiList";
	
	private FlexiTableElement tableEl;
	private MemberFeedbacksTableModel tableModel;
	private final TooledStackedPanel stackPanel;
	
	private int count = 0;
	
	private ApplicationMemberFeedbackMainController feedbackCtrl;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public MemberFeedbacksController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel) {
		super(ureq, wControl, "member_feedbacks", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.stackPanel = stackPanel;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberFeedCols.positionTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberFeedCols.application));
		RecruitingTableOption dueDateOption = recruitingModule.getTableFeedbacksForMembersDueDateOption();
		if(!dueDateOption.isDisabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberFeedCols.submissionDeadline, new DateCellRenderer()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberFeedCols.myFeedback));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberFeedCols.organisation));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberFeedCols.department));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberFeedCols.planingsNumber));

		tableModel = new MemberFeedbacksTableModel(columnsModel, getTranslator(), getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "feedbacks", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, PREFS_ID);
		tableEl.setExportEnabled(false);
		tableEl.setElementCssClass("o_sel_position_reference_list");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("apps.feedbacks.emtpy")
				.build());
		tableEl.setPageSize(20);
		tableEl.setSearchEnabled(true);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MemberFeedCols.positionTitle.name(), true));
		tableEl.setSortSettings(sortOptions);
	}
	
	public void loadModel() {
		List<ApplicationFeedback> feedbacks = feedbackService.getApplicationFeedbacks(getIdentity());
		List<MemberFeedbackRow> rows = new ArrayList<>(feedbacks.size());
		
		Date now = new Date();
		for(ApplicationFeedback feedback:feedbacks) {
			Date deadline = feedback.getDeadline();
			if(deadline == null) {
				deadline = feedback.getConfiguration().getDeadline();
			}
			if(deadline != null && RecruitingHelper.endOfDay(deadline).before(now)) {
				continue;
			}
			
			String i18nLink = StringHelper.containsNonWhitespace(feedback.getComment()) ? "edit" : "create";
			FormLink editLink = uifactory.addFormLink("edit_" + (count++), "edit", i18nLink, null, flc, Link.BUTTON_SMALL);
			MemberFeedbackRow row = new MemberFeedbackRow(feedback, editLink);
			editLink.setUserObject(row);
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(feedbackCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				stackPanel.popController(feedbackCtrl);
				cleanUp();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(feedbackCtrl);
		feedbackCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			 if(event instanceof FlexiTableSearchEvent ftse) {
				tableModel.filter(ftse.getSearch(), ftse.getFilters());
				tableEl.reset(true, true, false);
			}
		} else if(source instanceof FormLink link) {
			if("edit".equals(link.getCmd())) {
				doEdit(ureq, (MemberFeedbackRow)link.getUserObject());
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEdit(UserRequest ureq, MemberFeedbackRow row) {
		removeAsListenerAndDispose(feedbackCtrl);
		
		ApplicationFeedback feedback = feedbackService.getApplicationFeedback(row.getFeedback());
		ApplicationsFeedbackConfiguration configuration = feedback.getConfiguration();
		Set<String> visibleDocs = feedback.getConfiguration().getDocuments();
		Set<String> visibleFields = feedback.getConfiguration().getFields();
		RecruitingPositionSecurityCallback secCallback = RecruitingPositionSecurityCallbackForReviewer.membersFeedback(visibleFields, visibleDocs,
				configuration.isExpertsDocs(), configuration.isRefereesDocs(), configuration.isExpertsComparativeAssessmentDocs());
		feedbackCtrl = new ApplicationMemberFeedbackMainController(ureq, getWindowControl(),
				feedback, secCallback);
		listenTo(feedbackCtrl);
		String name = RecruitingHelper.formatFullName(feedback.getApplication(), getTranslator());
		stackPanel.pushController(name, feedbackCtrl);
	}
}
