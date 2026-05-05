/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.feedback.appsfeedback.ApplicationFeedbacksTableModel.AppFeedCols;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * Initial date: 27 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationFeedbacksController extends FormBasicController implements FlexiTableComponentDelegate {

	private FlexiTableElement tableEl;
	private ApplicationFeedbacksTableModel tableModel;
	
	private int counter = 0;
	private Position position;
	private Application application;
	private final boolean canDeleteFeedbacks;
	
	private CloseableModalController cmc;
	private ConfirmDeleteFeedbackController confirmDeleteFeedbackCtrl;
	
	@Autowired
	private FeedbackService feedbackService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ApplicationFeedbacksController(UserRequest ureq, WindowControl wControl,
			Position position, Application application, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "feedback_list", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.application = application;
		canDeleteFeedbacks = secCallback.canEditApplicationMembersFeedback();
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppFeedCols.fullName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppFeedCols.feedbackStatus));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppFeedCols.submissionDeadline));

		tableModel = new ApplicationFeedbacksTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "feedbacks", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "app-member-feedbacks-list");
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setExportEnabled(false);
		tableEl.setElementCssClass("o_sel_feedback_list");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("app.feedbacks.emtpy")
				.build());
		tableEl.setPageSize(20);
		tableEl.setNumOfRowsEnabled(false);
		
		VelocityContainer row = createVelocityContainer("feedback_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(2);
		if(rowObject instanceof ApplicationFeedbackRow) {
			ApplicationFeedbackRow mode = (ApplicationFeedbackRow)rowObject;
			if(mode.getDeleteLink() != null) {
				cmps.add(mode.getDeleteLink().getComponent());
			}
		}
		return cmps;
	}

	private void loadModel() {
		final AtomicInteger feedbackNumber = new AtomicInteger(0);
		final List<ApplicationFeedback> feedbacks = feedbackService.getApplicationFeedbacks(application);
		final List<ApplicationFeedbackRow> rows = feedbacks.stream()
				.filter(f -> StringHelper.containsNonWhitespace(f.getComment()))
				.map(f -> forgeRow(f, feedbackNumber))
				.collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private ApplicationFeedbackRow forgeRow(ApplicationFeedback feedback, AtomicInteger feedbackNumber) {
		FormLink deleteLink = canDeleteFeedbacks
				? uifactory.addFormLink("delete_" + (counter++), "delete", "delete", null, flc, Link.BUTTON) : null;
		ApplicationFeedbackRow row = new ApplicationFeedbackRow(feedback.getIdentity(), feedback, deleteLink,
				feedbackNumber.incrementAndGet(), getLocale());
		if(deleteLink != null) {
			deleteLink.setUserObject(row);
		}
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDeleteFeedbackCtrl == source) {
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
		removeAsListenerAndDispose(confirmDeleteFeedbackCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteFeedbackCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("delete".equals(link.getCmd())) {
				doConfirmDelete(ureq, (ApplicationFeedbackRow)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmDelete(UserRequest ureq, ApplicationFeedbackRow row) {
		if(guardModalController(confirmDeleteFeedbackCtrl)) return;
		
		confirmDeleteFeedbackCtrl = new ConfirmDeleteFeedbackController(ureq, getWindowControl(), position, application, row.getFeedback());
		listenTo(confirmDeleteFeedbackCtrl);

		String title = translate("confirm.delete.public.feedback.title");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmDeleteFeedbackCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
