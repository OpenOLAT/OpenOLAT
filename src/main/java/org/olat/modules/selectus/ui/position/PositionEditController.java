/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.category.PositionCategoryController;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.mail.PositionMailTemplatesController;
import org.olat.modules.selectus.ui.report.PositionReportAttributesController;

/**
 * 
 * Initial date: 12.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditController extends BasicController implements Activateable2, TooledController {
	
	private final Link statusLink;
	private final Link profileLink;
	private final Link applicationLink;
	private final Link referencesLink;
	private final Link evaluationLink;
	private final Link feedbacksLink;
	private final Link categoryLink;
	private final Link mailTemplatesLink;
	private final Link reportingLink;
	private final Link profileVisibilityLink;
	
	private final TooledStackedPanel stackPanel;
	private StackedPanel mainPanel = new SimpleStackedPanel("position-settings-panel");
	private final ButtonGroupComponent buttonsGroup = new ButtonGroupComponent("position-settings-groups");

	private PositionEditStatusController statusCtrl;
	private PositionEditProfileController profileCtrl;
	private PositionCategoryController categoriesCtrl;
	private PositionEditFeedbackController feedbackCtrl;
	private PositionEditReferenceController referenceCtrl;
	private PositionMailTemplatesController mailTemplatesCtrl;
	private PositionReportAttributesController reportAttributesCtrl;
	private PositionEditApplicationStepsController applicationsConfigCtrl;
	private PositionEditProfileVisibilityController profileVisibilityCtrl;
	private PositionEditEvaluationsConfigurationController evaluationsConfigCtrl;
	
	private Position position;
	private final boolean newPosition;
	private final boolean reportingOnly;
	private final RecruitingPositionSecurityCallback secCallback;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public PositionEditController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, Position position, boolean newPosition,
			RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		
		this.position = position;
		this.newPosition = newPosition;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		reportingOnly = PositionStatus.reporting.name().equals(position.getStatus());
		
		statusLink = LinkFactory.createLink("edit.status", getTranslator(), this);
		statusLink.setElementCssClass("o_sel_edit_position_status");
		statusLink.setVisible(secCallback.canEditPositionStatus() && !reportingOnly);
		buttonsGroup.addButton(statusLink, false);
		
		profileLink = LinkFactory.createLink("edit.profile", getTranslator(), this);
		profileLink.setElementCssClass("o_sel_edit_position_profile");
		profileLink.setVisible(secCallback.canEditPositionProfile() && !reportingOnly);
		buttonsGroup.addButton(profileLink, false);
		
		referencesLink = LinkFactory.createLink("edit.recommendation", getTranslator(), this);
		referencesLink.setElementCssClass("o_sel_edit_position_recommendation");
		referencesLink.setVisible(recruitingModule.isReferenceEnabled()
				&& secCallback.canEditPositionReferencesSettings() && !reportingOnly);
		buttonsGroup.addButton(referencesLink, false);
		
		applicationLink = LinkFactory.createLink("edit.config.application", getTranslator(), this);
		applicationLink.setElementCssClass("o_sel_edit_position_configuration");
		applicationLink.setVisible(secCallback.canEditPositionApplicationsSettings() && !reportingOnly);
		buttonsGroup.addButton(applicationLink, false);
		
		feedbacksLink = LinkFactory.createLink("edit.feedback", getTranslator(), this);
		feedbacksLink.setElementCssClass("o_sel_edit_position_feedback");
		feedbacksLink.setVisible((recruitingModule.isPublicFeedbackEnabled() || recruitingModule.isMembersFeedbackEnabled())
				&& secCallback.canEditPositionFeedbackSettings() && !reportingOnly);
		buttonsGroup.addButton(feedbacksLink, false);
		
		profileVisibilityLink = LinkFactory.createLink("edit.profile.visibility", getTranslator(), this);
		profileVisibilityLink.setElementCssClass("o_sel_edit_position_profile_visibility");
		profileVisibilityLink.setVisible(secCallback.canEditPositionApplicationsSettings() && !reportingOnly);
		buttonsGroup.addButton(profileVisibilityLink, false);

		evaluationLink = LinkFactory.createLink("edit.config.application.evaluation", getTranslator(), this);
		evaluationLink.setElementCssClass("o_sel_edit_position_evaluation");
		evaluationLink.setVisible(secCallback.canEditPositionEvaluationSettings() && !reportingOnly);
		buttonsGroup.addButton(evaluationLink, false);
		
		categoryLink = LinkFactory.createLink("edit.config.application.category", getTranslator(), this);
		categoryLink.setElementCssClass("o_sel_edit_position_category");
		categoryLink.setVisible(secCallback.canEditPositionCategoriesSettings() && !reportingOnly);
		buttonsGroup.addButton(categoryLink, false);
		
		mailTemplatesLink = LinkFactory.createLink("edit.config.mail.templates", getTranslator(), this);
		mailTemplatesLink.setElementCssClass("o_sel_edit_position_mail_templates");
		mailTemplatesLink.setVisible(secCallback.canEditPositionMailTemplates() && !reportingOnly);
		buttonsGroup.addButton(mailTemplatesLink, false);
		
		reportingLink = LinkFactory.createLink("edit.reporting.attributes", getTranslator(), this);
		reportingLink.setElementCssClass("o_sel_edit_position_attributes");
		reportingLink.setVisible(secCallback.canEditReportAttributes() && recruitingModule.isReportingEnabled());
		buttonsGroup.addButton(reportingLink, false);
		
		mainPanel = putInitialPanel(mainPanel);
		activateFirst(ureq);
	}
	
	public Position getPosition() {
		return position;
	}

	@Override
	public void initTools() {
		stackPanel.addTool(buttonsGroup, false);
	}

	public void removeTools() {
		stackPanel.removeTool(buttonsGroup);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			activateFirst(ureq);
			return;
		}
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Status".equalsIgnoreCase(type) && secCallback.canEditPositionStatus()) {
			doOpenStatusController(ureq);
		} else if("Profile".equalsIgnoreCase(type) && secCallback.canEditPositionProfile()) {
			doOpenProfile(ureq);
		} else if("Application".equalsIgnoreCase(type) && secCallback.canEditPositionApplicationsSettings()) {
			doOpenApplicationsConfiguration(ureq);
		} else if("References".equalsIgnoreCase(type) && secCallback.canEditPositionReferencesSettings()) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenReferences(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("Feedbacks".equalsIgnoreCase(type) && secCallback.canEditPositionFeedbackSettings()) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenFeedbacks(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("Visibility".equalsIgnoreCase(type) && secCallback.canEditPositionApplicationsSettings()) {
			doOpenProfileVisibility(ureq);
		} else if("Evaluation".equalsIgnoreCase(type) && secCallback.canEditPositionEvaluationSettings()) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			doOpenEvaluationsConfiguration(ureq).activate(ureq, subEntries, entries.get(0).getTransientState());
		} else if("Tags".equalsIgnoreCase(type) && secCallback.canEditPositionCategoriesSettings()) {
			doOpenCategoriesConfiguration(ureq);
		} else if("Templates".equalsIgnoreCase(type) && secCallback.canEditPositionMailTemplates()) {
			doOpenMailTemplates(ureq);
		}
	}
	
	private void activateFirst(UserRequest ureq) {
		if(position.getKey() == null && secCallback.canEditPositionProfile()) {
			doOpenProfile(ureq);
		} else if(reportingOnly && secCallback.canReportingPosition()) {
			doOpenReporting(ureq);
		} else if(secCallback.canEditPositionStatus()) {
			doOpenStatusController(ureq);
		} else if(secCallback.canEditPositionProfile()) {
			doOpenProfile(ureq);
		} else if(secCallback.canEditPositionApplicationsSettings()) {
			doOpenApplicationsConfiguration(ureq);
		} else if(secCallback.canEditPositionReferencesSettings()) {
			doOpenReferences(ureq);
		} else if(secCallback.canEditPositionFeedbackSettings()) {
			doOpenFeedbacks(ureq);
		} else if(secCallback.canEditPositionEvaluationSettings()) {
			doOpenEvaluationsConfiguration(ureq);
		} else if(secCallback.canEditPositionCategoriesSettings()) {
			doOpenCategoriesConfiguration(ureq);
		} else if(secCallback.canEditPositionMailTemplates()) {
			doOpenMailTemplates(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == statusLink) {
			doOpenStatusController(ureq);
		} else if(source == profileLink) {
			doOpenProfile(ureq);
		} else if(source == applicationLink) {
			doOpenApplicationsConfiguration(ureq);
		} else if(source == referencesLink) {
			doOpenReferences(ureq);
		} else if(source == evaluationLink) {
			doOpenEvaluationsConfiguration(ureq);
		} else if(source == categoryLink) {
			doOpenCategoriesConfiguration(ureq);
		} else if(source == mailTemplatesLink) {
			doOpenMailTemplates(ureq);
		} else if(source == feedbacksLink) {
			doOpenFeedbacks(ureq);
		} else if(source == profileVisibilityLink) {
			doOpenProfileVisibility(ureq);
		} else if(source == reportingLink) {
			doOpenReporting(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof PositionEditableController
				&& (event == Event.DONE_EVENT  || event == Event.CHANGED_EVENT || event instanceof NewPositionSavedEvent)) {
			updatePosition(((PositionEditableController)source).getPosition());
		}

		if(event == Event.CANCELLED_EVENT || event instanceof NewPositionSavedEvent) {
			fireEvent(ureq, event);
		} else if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		
		super.event(ureq, source, event);
	}
	
	private void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		if(profileCtrl != null) {
			profileCtrl.updatePosition(updatedPosition);
		}
		if(statusCtrl != null) {
			statusCtrl.updatePosition(updatedPosition);
		}
		if(referenceCtrl != null) {
			referenceCtrl.updatePosition(updatedPosition);
		}
		if(applicationsConfigCtrl != null) {
			applicationsConfigCtrl.updatePosition(updatedPosition);
		}
		if(feedbackCtrl != null) {
			feedbackCtrl.updatePosition(updatedPosition);
		}
		if(evaluationsConfigCtrl != null) {
			evaluationsConfigCtrl.updatePosition(updatedPosition);
		}
		if(mailTemplatesCtrl != null) {
			mailTemplatesCtrl.updatePosition(updatedPosition);
		}
		if(reportAttributesCtrl != null) {
			reportAttributesCtrl.updatePosition(updatedPosition);
		}
	}
	
	private PositionEditStatusController doOpenStatusController(UserRequest ureq) {
		if(statusCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Status"), null);
			statusCtrl = new PositionEditStatusController(ureq, swControl,
					position, secCallback.isReadOnly());
			listenTo(statusCtrl);
		}
		addToHistory(ureq, statusCtrl);
		mainPanel.setContent(statusCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(statusLink);
		return statusCtrl;
	}
	
	private PositionEditProfileController doOpenProfile(UserRequest ureq) {
		if(profileCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Profile"), null);
			profileCtrl = new PositionEditProfileController(ureq, swControl, position, newPosition, secCallback.isReadOnly());
			listenTo(profileCtrl);
		}
		addToHistory(ureq, profileCtrl);
		mainPanel.setContent(profileCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(profileLink);
		return profileCtrl;
	}
	
	private PositionEditApplicationStepsController doOpenApplicationsConfiguration(UserRequest ureq) {
		if(applicationsConfigCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Application"), null);
			applicationsConfigCtrl = new PositionEditApplicationStepsController(ureq, swControl,
					position, secCallback.isReadOnly());
			listenTo(applicationsConfigCtrl);
		}
		addToHistory(ureq, applicationsConfigCtrl);
		mainPanel.setContent(applicationsConfigCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(applicationLink);
		return applicationsConfigCtrl;
	}
	
	private PositionEditReferenceController doOpenReferences(UserRequest ureq) {
		if(referenceCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("References"), null);
			referenceCtrl = new PositionEditReferenceController(ureq, swControl,
					position, secCallback.isReadOnly());
			listenTo(referenceCtrl);
		}
		addToHistory(ureq, referenceCtrl);
		mainPanel.setContent(referenceCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(referencesLink);
		return referenceCtrl;
	}
	
	private PositionEditFeedbackController doOpenFeedbacks(UserRequest ureq) {
		if(feedbackCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Feedbacks"), null);
			feedbackCtrl = new PositionEditFeedbackController(ureq, swControl, position);
			listenTo(feedbackCtrl);
			updatePosition(feedbackCtrl.getPosition());
		}
		addToHistory(ureq, feedbackCtrl);
		mainPanel.setContent(feedbackCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(feedbacksLink);
		return feedbackCtrl;
	}
	
	private PositionEditProfileVisibilityController doOpenProfileVisibility(UserRequest ureq) {
		removeAsListenerAndDispose(profileVisibilityCtrl);
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Visibility"), null);
		profileVisibilityCtrl = new PositionEditProfileVisibilityController(ureq, swControl, position, false);
		listenTo(profileVisibilityCtrl);
		updatePosition(profileVisibilityCtrl.getPosition());
		
		addToHistory(ureq, profileVisibilityCtrl);
		mainPanel.setContent(profileVisibilityCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(profileVisibilityLink);
		return profileVisibilityCtrl;
	}
	
	private PositionEditEvaluationsConfigurationController doOpenEvaluationsConfiguration(UserRequest ureq) {
		if(evaluationsConfigCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Evaluation"), null);
			evaluationsConfigCtrl = new PositionEditEvaluationsConfigurationController(ureq, swControl,
					position, secCallback.isReadOnly());
			listenTo(evaluationsConfigCtrl);
			updatePosition(evaluationsConfigCtrl.getPosition());
		}
		addToHistory(ureq, evaluationsConfigCtrl);
		mainPanel.setContent(evaluationsConfigCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(evaluationLink);
		return evaluationsConfigCtrl;
	}
	
	private PositionCategoryController doOpenCategoriesConfiguration(UserRequest ureq) {
		if(categoriesCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Tags"), null);
			categoriesCtrl = new PositionCategoryController(ureq, swControl, position);
			listenTo(categoriesCtrl);
		}
		
		addToHistory(ureq, categoriesCtrl);
		mainPanel.setContent(categoriesCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(categoryLink);
		return categoriesCtrl;
	}
	
	private PositionMailTemplatesController doOpenMailTemplates(UserRequest ureq) {
		if(mailTemplatesCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Templates"), null);
			mailTemplatesCtrl = new PositionMailTemplatesController(ureq, swControl, position, secCallback.isReadOnly());
			listenTo(mailTemplatesCtrl);
		}
		
		addToHistory(ureq, mailTemplatesCtrl);
		mainPanel.setContent(mailTemplatesCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(mailTemplatesLink);
		return mailTemplatesCtrl;
	}
	
	private PositionReportAttributesController doOpenReporting(UserRequest ureq) {
		if(reportAttributesCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Reporting"), null);
			reportAttributesCtrl = new PositionReportAttributesController(ureq, swControl, position, true, true);
			listenTo(reportAttributesCtrl);
		}
		
		addToHistory(ureq, reportAttributesCtrl);
		mainPanel.setContent(reportAttributesCtrl.getInitialComponent());
		buttonsGroup.setSelectedButton(reportingLink);
		return reportAttributesCtrl;
	}
}
