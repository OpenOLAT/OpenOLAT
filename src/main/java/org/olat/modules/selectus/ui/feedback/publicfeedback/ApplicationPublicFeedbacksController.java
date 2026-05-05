/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.publicfeedback;

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
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PublicFeedback;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.feedback.publicfeedback.ApplicationPublicFeedbacksDataModel.FeedbackCols;

/**
 * 
 * Initial date: 27 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationPublicFeedbacksController extends FormBasicController implements FlexiTableComponentDelegate {

	private FlexiTableElement tableEl;
	private ApplicationPublicFeedbacksDataModel tableModel;
	
	private int counter = 0;
	private Position position;
	private Application application;
	private final boolean canDeleteFeedbacks;
	
	private CloseableModalController cmc;
	private ConfirmDeletePublicFeedbackController confirmDeleteFeedbackCtrl;
	
	@Autowired
	private FeedbackService feedbackService;
	
	public ApplicationPublicFeedbacksController(UserRequest ureq, WindowControl wControl,
			Position position, Application application, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "feedback_list", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.application = application;
		canDeleteFeedbacks = secCallback.canDeletePublicFeedbacks();
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String link = feedbackService.getPublicFeedbackLink(application);
		formLayout.contextPut("link", link);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedbackCols.email));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedbackCols.comment));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedbackCols.lastModified));

		tableModel = new ApplicationPublicFeedbacksDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "feedbacks", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "public-feedbacks-list");
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setExportEnabled(false);
		tableEl.setElementCssClass("o_sel_public_feedback_list");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("public.feedback.list.empty")
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
		if(rowObject instanceof PublicFeedbackRow) {
			PublicFeedbackRow mode = (PublicFeedbackRow)rowObject;
			if(mode.getDeleteLink() != null) {
				cmps.add(mode.getDeleteLink().getComponent());
			}
		}
		return cmps;
	}

	private void loadModel() {
		final AtomicInteger feedbackNumber = new AtomicInteger();
		final List<PublicFeedback> feedbacks = feedbackService.getPublicFeedbacks(application);
		final List<PublicFeedbackRow> rows = feedbacks.stream()
				.filter(f -> StringHelper.containsNonWhitespace(f.getComment()))
				.map(f -> forgeRow(f, feedbackNumber))
				.collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private PublicFeedbackRow forgeRow(PublicFeedback feedback, AtomicInteger feedbackNumber) {
		FormLink deleteLink = canDeleteFeedbacks
				? uifactory.addFormLink("delete_" + (counter++), "delete", "delete", null, flc, Link.BUTTON) : null;
		PublicFeedbackRow row = new PublicFeedbackRow(feedback, deleteLink, feedbackNumber.incrementAndGet(), getLocale());
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
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("delete".equals(link.getCmd())) {
				doConfirmDelete(ureq, (PublicFeedbackRow)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmDelete(UserRequest ureq, PublicFeedbackRow row) {
		if(guardModalController(confirmDeleteFeedbackCtrl)) return;
		
		confirmDeleteFeedbackCtrl = new ConfirmDeletePublicFeedbackController(ureq, getWindowControl(), position, application, row.getFeedback());
		listenTo(confirmDeleteFeedbackCtrl);

		String title = translate("confirm.delete.public.feedback.title");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmDeleteFeedbackCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
