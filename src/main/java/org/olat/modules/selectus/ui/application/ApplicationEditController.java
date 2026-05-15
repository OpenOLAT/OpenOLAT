/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.application;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.app_wizard.AcademicalBackgroundController;
import org.olat.modules.selectus.ui.app_wizard.CustomAttributesController;
import org.olat.modules.selectus.ui.app_wizard.DocumentsController;
import org.olat.modules.selectus.ui.app_wizard.EditPersonController;
import org.olat.modules.selectus.ui.app_wizard.ProjectController;
import org.olat.modules.selectus.ui.comment.ApplicationCommitteeCommentController;
import org.olat.modules.selectus.ui.events.RequestPersistEvent;
import org.olat.modules.selectus.ui.feedback.appsfeedback.ApplicationFeedbackMembersListController;
import org.olat.modules.selectus.ui.reference.ApplicationReferenceListController;


/**
 * 
 * A wrapper for a segment view.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationEditController extends FormBasicController {

	private ProjectController projectCtrl;
	private EditPersonController personCtrl;
	private DocumentsController documentsCtrl;
	private ApplicationMemoController memoCtrl;
	private ApplicationEditStatusController statusCtrl;
	private AcademicalBackgroundController academicalCtrl;
	private ApplicationReferenceListController referencesCtrl;
	private ApplicationCommitteeCommentController commentCtrl;
	private ApplicationFeedbackMembersListController feedbacksCtrl;
	private List<CustomAttributesController> customAttributesCtrls = new ArrayList<>();
	
	private FormLink personLink;
	private FormLink documentsLink;
	private FormLink projectLink;
	private FormLink referencesLink;
	private FormLink feedbackLink;
	private FormLink memoLink;
	private FormLink commentLink;
	private FormLink statusLink;
	private FormLink academicalBackgroundLink;
	private List<FormLink> customTabsLinks = new ArrayList<>();
	private SegmentViewComponent segmentView;
	
	private Application application;
	private final Position position;
	private final List<Tab> customTabs;
	private final boolean projectEnabled;
	private final boolean referencesEnabled;
	private final boolean customTabsEnabled;
	private final boolean publicFeedbackEnabled;
	private final boolean membersFeedbackEnabled;
	private final boolean academicalBackgroundEnabled;
	private final RecruitingPositionSecurityCallback secCallback;

	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ApplicationEditController(UserRequest ureq, WindowControl wControl,
			Application application, Position position, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "edit_application", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		
		this.position = position;
		this.application = application;
		this.secCallback = secCallback;
		referencesEnabled = recruitingModule.isReferenceEnabled()
				&& (position.isExpertRecommendationEnabled() || position.isRefereeRecommendationEnabled()
						|| (recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()));
		publicFeedbackEnabled = recruitingModule.isPublicFeedbackEnabled()
				&& position.isPublicFeedbackEnabled();
		membersFeedbackEnabled = recruitingModule.isMembersFeedbackEnabled()
				&& feedbackService.hasFeedbackConfigurationEnabled(position);
		projectEnabled = recruitingModule.isApplicationProjectEnabled()
				&& position.isApplicationProject();
		academicalBackgroundEnabled = recruitingModule.isApplicationAcademicalBackgroundEnabled(position);
		customTabs = position.getCustomTabsList();
		customTabsEnabled = recruitingModule.isPositionCustomStepsEnabled() && !customTabs.isEmpty();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			segmentView = SegmentViewFactory.createSegmentView("segments", layoutCont, this);
			segmentView.setElementCssClass("o_segments_adjusted");
			
			boolean canEditPersonalData = secCallback.canEditApplicationPersonalData();
			List<String> excludedAttributesList = position.getExcludedAttributesList();
			personCtrl = new EditPersonController(ureq, getWindowControl(), mainForm, application,
					null, excludedAttributesList, true, true, canEditPersonalData);
			listenTo(personCtrl);

			if(academicalBackgroundEnabled) {
				boolean canEditAcademicalBackground = secCallback.canEditApplicationAcademicalBackground();
				academicalCtrl = new AcademicalBackgroundController(ureq, getWindowControl(), mainForm, application,
					null, excludedAttributesList, true, true, canEditAcademicalBackground);
				listenTo(academicalCtrl);
			}
			
			if(projectEnabled) {
				boolean canEditProject = secCallback.canEditApplicationProject();
				projectCtrl = new ProjectController(ureq, getWindowControl(), mainForm, application, null, true, true, canEditProject);
				listenTo(projectCtrl);
			}
			
			if(customTabsEnabled) {
				for(Tab tab:customTabs) {
					TabConfiguration tabConfiguration = position.getTabConfiguration(tab);
					CustomAttributesController attributesCtrl = new CustomAttributesController(ureq, getWindowControl(), mainForm, application,
							tabConfiguration, true, true, canEditPersonalData);
					listenTo(attributesCtrl);
					customAttributesCtrls.add(attributesCtrl);
				}
			}
			
			boolean canEditDocuments = secCallback.canEditApplicationDocuments();
			documentsCtrl = new DocumentsController(ureq, getWindowControl(), mainForm, position, application,
					null, true, true, canEditDocuments);
			listenTo(documentsCtrl);

			if(referencesEnabled) {
				referencesCtrl = new ApplicationReferenceListController(ureq, getWindowControl(), mainForm, application, position, secCallback);
				listenTo(referencesCtrl);
			}
			
			if((publicFeedbackEnabled || membersFeedbackEnabled) && secCallback.canEditApplicationMembersFeedback()) {
				feedbacksCtrl = new ApplicationFeedbackMembersListController(ureq, getWindowControl(), mainForm, application, position,
						secCallback, publicFeedbackEnabled, membersFeedbackEnabled);
				listenTo(feedbacksCtrl);
			}
			
			boolean canEditMemo = secCallback.canEditApplicationMemo();
			memoCtrl = new ApplicationMemoController(ureq, getWindowControl(), mainForm, application, canEditMemo);
			listenTo(memoCtrl);
			
			boolean canEditComment = secCallback.canEditApplicationCommitteeComment();
			commentCtrl = new ApplicationCommitteeCommentController(ureq, getWindowControl(), mainForm, application, canEditComment);
			listenTo(commentCtrl);
			
			statusCtrl = new ApplicationEditStatusController(ureq, getWindowControl(), mainForm, application, position, secCallback);
			listenTo(statusCtrl);

			personLink = uifactory.addFormLink(Tabs.person.getComponentName(), formLayout);
			personLink.setElementCssClass("o_sel_edit_person_nav");
			segmentView.addSegment(personLink.getComponent(), true);
			if(academicalBackgroundEnabled) {
				academicalBackgroundLink = uifactory.addFormLink(Tabs.academicalBackground.getComponentName(), formLayout);
				academicalBackgroundLink.setElementCssClass("o_sel_edit_app_background_nav");
				academicalBackgroundLink.setTitle(Tabs.academicalBackground.getComponentName());
				segmentView.addSegment(academicalBackgroundLink.getComponent(), false);
			}
			
			if(projectEnabled) {
				projectLink = uifactory.addFormLink(Tabs.project.getComponentName(), formLayout);
				projectLink.setElementCssClass("o_sel_edit_app_project_nav");	
				projectLink.setTitle(Tabs.project.getComponentName());
				segmentView.addSegment(projectLink.getComponent(), false);
			}
			
			if(customTabsEnabled) {
				for(Tab tab:customTabs) {
					TabConfiguration tabConfiguration = position.getTabConfiguration(tab);
					if(!tabConfiguration.isDisabled()) {
						FormLink customTabLink = uifactory.addFormLink(tab.name(), tabConfiguration.getTitle(getLocale()), null, formLayout, Link.LINK | Link.NONTRANSLATED);
						customTabLink.setElementCssClass("o_sel_edit_app_" + tab + "_nav");
						customTabLink.setUserObject(tab);
						segmentView.addSegment(customTabLink.getComponent(), false);
						customTabsLinks.add(customTabLink);
					}
				}
			}
			
			documentsLink = uifactory.addFormLink(Tabs.documentsAttachments.getComponentName(), formLayout);
			documentsLink.setElementCssClass("o_sel_edit_app_documents_nav");
			segmentView.addSegment(documentsLink.getComponent(), false);
			
			if(recruitingModule.isApplicationsMemoEnabled()) {
				memoLink = uifactory.addFormLink(Tabs.memo.getComponentName(), formLayout);
				memoLink.setElementCssClass("o_sel_edit_app_memo_nav");
				segmentView.addSegment(memoLink.getComponent(), false);
			}
			
			if(recruitingModule.isApplicationsCommitteeCommentEnabled() && position.isCommitteeCommentEnabled()
					&& (secCallback.canEditApplicationCommitteeComment() || secCallback.canViewCommitteeComment())) {
				commentLink = uifactory.addFormLink(Tabs.feedbackComment.getComponentName(), formLayout);
				commentLink.setElementCssClass("o_sel_edit_app_feedback_comment_nav");
				segmentView.addSegment(commentLink.getComponent(), false);
			}
			
			if(referencesEnabled) {
				referencesLink = uifactory.addFormLink(Tabs.references.getComponentName(), formLayout);
				referencesLink.setElementCssClass("o_sel_edit_app_recommendations_nav");	
				segmentView.addSegment(referencesLink.getComponent(), false);
			}
			
			//TODO feedback
			if((publicFeedbackEnabled || membersFeedbackEnabled) && secCallback.canEditApplicationMembersFeedback()) {
				feedbackLink = uifactory.addFormLink(Tabs.feedback.getComponentName(), formLayout);
				feedbackLink.setElementCssClass("o_sel_edit_app_feedbacks_nav");	
				segmentView.addSegment(feedbackLink.getComponent(), false);
			}
			
			statusLink = uifactory.addFormLink(Tabs.status.getComponentName(), formLayout);
			statusLink.setElementCssClass("o_sel_edit_app_status_nav");
			segmentView.addSegment(statusLink.getComponent(), false);

			final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
			formLayout.add(buttonLayout);
			uifactory.addFormSubmitButton("submit", buttonLayout);
			uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
			
			setMainContent(Tabs.person);
		}
	}
	
	private void setMainContent(Tabs selectedTab) {
		switch(selectedTab) {
			case person: flc.add("content", personCtrl.getInitialFormItem()); break;
			case academicalBackground: flc.add("content", academicalCtrl.getInitialFormItem()); break;
			case documentsAttachments: flc.add("content", documentsCtrl.getInitialFormItem()); break;
			case project: flc.add("content", projectCtrl.getInitialFormItem()); break;
			case references: flc.add("content", referencesCtrl.getInitialFormItem()); break;
			case feedback: flc.add("content", feedbacksCtrl.getInitialFormItem()); break;
			case memo: flc.add("content", memoCtrl.getInitialFormItem()); break;
			case feedbackComment: flc.add("content", commentCtrl.getInitialFormItem()); break;
			case status: flc.add("content", statusCtrl.getInitialFormItem()); break;
		}
	}
	
	private void setMainContent(Tab selectedTab) {
		for(CustomAttributesController customAttributesCtrl:customAttributesCtrls) {
			if(customAttributesCtrl.tab() == selectedTab) {
				flc.add("content", customAttributesCtrl.getInitialFormItem());
				break;
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(referencesCtrl == source) {
			if(event instanceof RequestPersistEvent) {
				doPersistApplication();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == personLink) {
			setMainContent(Tabs.person);
			segmentView.select(personLink.getComponent());
		} else if (source == academicalBackgroundLink) {
			setMainContent(Tabs.academicalBackground);
			segmentView.select(academicalBackgroundLink.getComponent());
		} else if (source == documentsLink) {
			setMainContent(Tabs.documentsAttachments);
			segmentView.select(documentsLink.getComponent());
		}  else if (source == projectLink) {
			setMainContent(Tabs.project);
			segmentView.select(projectLink.getComponent());
		} else if (source == memoLink) {
			setMainContent(Tabs.memo);
			segmentView.select(memoLink.getComponent());
		} else if (source == commentLink) {
			setMainContent(Tabs.feedbackComment);
			segmentView.select(commentLink.getComponent());
		} else if (source == referencesLink) {
			setMainContent(Tabs.references);
			segmentView.select(referencesLink.getComponent());
		} else if (source == feedbackLink) {
			setMainContent(Tabs.feedback);
			segmentView.select(feedbackLink.getComponent());
		} else if(source == statusLink) {
			setMainContent(Tabs.status);
			segmentView.select(statusLink.getComponent());
		} else if(source instanceof FormLink link && link.getUserObject() instanceof Tab tab) {
			setMainContent(tab);
			segmentView.select(source.getComponent());
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		boolean personOk = personCtrl.validateFormLogic(ureq);
		personLink.setIconLeftCSS(personOk ? null : "o_icon o_icon_error");
		
		return allOk;
	}
	
	private void doPersistApplication() {
		if(application.getKey() == null) {
			application = erFrontendManager.saveTempApplication(application, false);
			logAudit("Staff create temp application: " + application.toString(), null);
		}
		
		if(referencesCtrl != null) {
			referencesCtrl.setApplication(application);
		}
		if(feedbacksCtrl != null) {
			feedbacksCtrl.setApplication(application);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean logCreation = false;
		String before;
		if(application.getKey() != null) {
			//reload and update
			application = erFrontendManager.getApplicationByKey(application.getKey());
			before = auditService.toAuditXml(application);
		} else {
			logCreation = true;
			before = null;
		}
		
		
		personCtrl.commitChanges(application);
		if(academicalCtrl != null) {
			academicalCtrl.commitChanges(application);
		}
		if(projectCtrl != null) {
			projectCtrl.commitChanges(application);
		}
		application = documentsCtrl.commitChanges(application);
		application = memoCtrl.commitChanges(application);
		application = commentCtrl.commitChanges(application);
		application = statusCtrl.commitChanges(application);
		if(feedbacksCtrl != null) {
			feedbacksCtrl.commitChanges(application);
		}
		for(CustomAttributesController customAttributesCtrl:customAttributesCtrls) {
			customAttributesCtrl.commitChanges(application);
		}
		
		application = erFrontendManager.saveTempApplication(application, true);
		
		String after = auditService.toAuditXml(application);
		if(logCreation) {
			logAudit("Staff create application: " + application.toString(), null);
			String messageI18n = "audit.log.application.add";
			String[] messageArgs = new String[] { salutationGenerator.getTitleFullname(application, getLocale()), application.getId().toString() };
			auditService.auditApplicationLog(Action.add, ActionTarget.application, before, after, messageI18n, messageArgs, getTranslator(), application.getPosition(), application, getIdentity());
		} else {
			logAudit("Update application: " + application.toString(), null);
			if(before == null || !before.equals(after)) {
				String messageI18n = "audit.log.application.update";
				String[] messageArgs = new String[] { salutationGenerator.getTitleFullname(application, getLocale()), application.getId().toString() };
				auditService.auditApplicationLog(Action.update, ActionTarget.application, before, after, messageI18n, messageArgs, getTranslator(), application.getPosition(), application, getIdentity());
			}		
		}
		dbInstance.commit();//make sure all data are committed before sending events
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private enum Tabs {
		person("edit.application.person"),
		status("edit.application.status"),
		academicalBackground("edit.application.background"),
		documentsAttachments("edit.application.documents"),
		references("edit.application.references"),
		feedback("edit.application.feedback"),
		project("edit.application.project"),
		memo("edit.application.memo"),
		feedbackComment("edit.application.committee.comment");
		
		private final String componentName;
		
		private Tabs(String componentName) {
			this.componentName = componentName;
		}

		public String getComponentName() {
			return componentName;
		}
	}
}