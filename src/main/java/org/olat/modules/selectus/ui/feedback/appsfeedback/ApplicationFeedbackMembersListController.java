/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import static org.olat.modules.selectus.ui.RecruitingHelper.formatFullName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.components.DateCellRenderer;
import org.olat.modules.selectus.ui.components.ReferenceStatusCellRenderer;
import org.olat.modules.selectus.ui.feedback.appsfeedback.FeedbackMembersDataModel.FeedCols;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.AddFeedbacksMemberFinishCallback;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.Feedback1EmailStep;
import org.olat.modules.selectus.ui.feedback.appsfeedback.wizard.FeedbackMembersContext;

/**
 * Enable/disable public feedback<br>
 * The list of faculty members and feedbacks in the panel to edit
 * an application.
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationFeedbackMembersListController extends FormBasicController {
	
	private static final String PREFS_ID = "recruitingAppFeedbackMembersFlexiList";
	private static final String[] enableKeys = new String[]{ "on" };
	private static final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
	
	private FormLink addMemberButton;
	private FlexiTableElement membersFeedbackTableEl;
	private FeedbackMembersDataModel membersFeedbackModel;

	private StaticTextElement feedbackLinkEl;
	private MultipleSelectionElement enablePublicFeedbackEl;
	private TextElement feedbackDeadlineDayElement;
	private SingleSelection feedbackDeadlineMonthElement;
	private TextElement feedbackDeadlineYearElement;
	private FormLayoutContainer feedbackDeadlineContainer;

	private CloseableModalController cmc;
	private StepsMainRunController addMembersFeedbackWizard;
	private ApplicationFeedbackMemberEditController memberEditCtrl;
	private SendFeedbackInvitationController sendFeedbackInvitationCtrl;
	private ConfirmRemoveFeedbackMemberController confirmRemoveFeedbackCtrl;
	
	private int counter = 0;
	private Application app;
	private Position position;
	private final boolean canEditFeedbacks;
	private boolean membersFeedbackEnabled;
	private final boolean publicFeedbackEnabled;
	
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public ApplicationFeedbackMembersListController(UserRequest ureq, WindowControl wControl, Form rootForm,
			Application application, Position position, RecruitingPositionSecurityCallback secCallback,
			boolean publicFeedbackEnabled, boolean membersFeedbackEnabled) {
		super(ureq, wControl, LAYOUT_CUSTOM, "app_feedback_list", rootForm);
		setTranslator(Util.createPackageTranslator(PositionController.class, ureq.getLocale(), getTranslator()));
		app = application;
		this.position = position;
		this.publicFeedbackEnabled = publicFeedbackEnabled;
		this.membersFeedbackEnabled = membersFeedbackEnabled;
		canEditFeedbacks = secCallback.canEditApplicationMembersFeedback();
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}
		
		initForm(ureq);
		loadModel();
		updateGUI();
	}
	
	public void setApplication(Application application) {
		app = application;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(publicFeedbackEnabled) {
			initPublicFeedbackForm(formLayout);
		}
		
		if(membersFeedbackEnabled) {
			initFeedbackForm(formLayout, ureq);
		}
	}

	private void initPublicFeedbackForm(FormItemContainer parentFormLayout) {
		FormLayoutContainer formLayout = FormLayoutContainer.createDefaultFormLayout("public.feedback", getTranslator());
		parentFormLayout.add("public.feedback", formLayout);
		formLayout.setRootForm(mainForm);
		formLayout.setFormTitle(translate("edit.application.public.feedback.title"));
		
		String[] enableValues = new String[]{ translate("enable") };
		
		enablePublicFeedbackEl = uifactory.addCheckboxesHorizontal("edit.application.public.feedback.enable", formLayout, enableKeys, enableValues);
		enablePublicFeedbackEl.addActionListener(FormEvent.ONCHANGE);
		if(app.isPublicFeedbackEnabled()) {
			enablePublicFeedbackEl.select(enableKeys[0], true);
		}
		
		String link = feedbackService.getPublicFeedbackLink(app);
		feedbackLinkEl = uifactory.addStaticTextElement("edit.application.public.feedback.link", "edit.application.public.feedback.link", link, formLayout);
		
		// deadline container
		String feedbackDeadlineCont = velocity_root + "/edit_public_feedback.html";
		feedbackDeadlineContainer = FormLayoutContainer.createCustomFormLayout("public.feedback.deadline", getTranslator(), feedbackDeadlineCont);
		feedbackDeadlineContainer.setRootForm(mainForm);
		feedbackDeadlineContainer.setLabel("edit.public.feedback.deadline", null);
		feedbackDeadlineContainer.setMandatory(true);
		formLayout.add(feedbackDeadlineContainer);
		
		String feedbackDay = "";
		String feedbackMonth= "0";
		String feedbackYear = "";
		Date feedbackDeadline = app.getPublicFeedbackDeadline();
		if(feedbackDeadline == null) {
			feedbackDeadline = position.getPublicFeedbackDeadline();
		}
		if(feedbackDeadline != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(feedbackDeadline);
			feedbackDay = Integer.toString(cal.get(Calendar.DATE));
			feedbackMonth = Integer.toString(cal.get(Calendar.MONTH));
			feedbackYear = Integer.toString(cal.get(Calendar.YEAR));
		}
		
		feedbackDeadlineDayElement = uifactory.addTextElement("public.feedback.deadline.day", null, 2, feedbackDay, feedbackDeadlineContainer);
		feedbackDeadlineDayElement.setDomReplacementWrapperRequired(false);
		feedbackDeadlineDayElement.setDisplaySize(2);
		feedbackDeadlineDayElement.setMandatory(true);
		
		feedbackDeadlineMonthElement = uifactory.addDropdownSingleselect("public.feedback.deadline.month", null, feedbackDeadlineContainer, monthKeys, monthValues, null);
		feedbackDeadlineMonthElement.setDomReplacementWrapperRequired(false);
		feedbackDeadlineMonthElement.setMandatory(true);
		feedbackDeadlineMonthElement.select(feedbackMonth, true);
		
		feedbackDeadlineYearElement = uifactory.addTextElement("public.feedback.deadline.year", null, 4, feedbackYear, feedbackDeadlineContainer);
		feedbackDeadlineYearElement.setDomReplacementWrapperRequired(false);
		feedbackDeadlineYearElement.setDisplaySize(4);
		feedbackDeadlineYearElement.setMandatory(true);
	}

	private void initFeedbackForm(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedCols.fullName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedCols.status, new ReferenceStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedCols.submissionDeadline, new DateCellRenderer()));
		if(canEditFeedbacks) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(FeedCols.sendMail));
			
			StaticFlexiCellRenderer editCol = new StaticFlexiCellRenderer(translate("edit"), "edit-feed");
			editCol.setDirtyCheck(false);
			editCol.setIconLeftCSS("o_icon o_icon_edit");
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "edit", null, -1, "edit-feed", false, null,
					FlexiColumnModel.ALIGNMENT_LEFT, editCol));
			
			StaticFlexiCellRenderer deleteCol = new StaticFlexiCellRenderer(translate("delete"), "delete-feed");
			deleteCol.setDirtyCheck(false);
			deleteCol.setIconLeftCSS("o_icon o_icon_delete_item");
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "delete", null, -1, "delete-feed", false, null,
					FlexiColumnModel.ALIGNMENT_LEFT, deleteCol));
		}

		membersFeedbackModel = new FeedbackMembersDataModel(columnsModel, getLocale());
		membersFeedbackTableEl = uifactory.addTableElement(getWindowControl(), "feedbacks", membersFeedbackModel, 20, false, getTranslator(), formLayout);
		membersFeedbackTableEl.setAndLoadPersistedPreferences(ureq, PREFS_ID);
		membersFeedbackTableEl.setExportEnabled(false);
		membersFeedbackTableEl.setElementCssClass("o_sel_position_reference_list");
		membersFeedbackTableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("apps.feedbacks.members.emtpy")
				.build());
		membersFeedbackTableEl.setPageSize(20);
		
		addMemberButton = uifactory.addFormLink("add.feedback", formLayout, Link.BUTTON);
		addMemberButton.getComponent().setSuppressDirtyFormWarning(true);
		addMemberButton.setVisible(canEditFeedbacks);
	}
	
	private void updateGUI() {
		if(enablePublicFeedbackEl != null) {
			boolean feedbackEnabled = enablePublicFeedbackEl.isAtLeastSelected(1);
			feedbackLinkEl.setVisible(feedbackEnabled);
			feedbackDeadlineContainer.setVisible(feedbackEnabled);
		}
	}
	
	private void loadModel() {
		List<FeedbackMember> memberRows = new ArrayList<>();
		List<ApplicationFeedback> feedbacks = feedbackService.getApplicationFeedbacks(app);
		for(ApplicationFeedback feedback:feedbacks) {

			String cmd = null;
			String i18nLink = null;
			ReferenceStatus status = feedback.getReferenceStatus();
			switch(status) {
				case notSent:
					i18nLink = translate("reference.invite");
					cmd = "feed-invite";
					break;
				case late:
				case sentAwaiting:
					i18nLink = translate("reference.remind");
					cmd = "feed-invite";
					break;
				case submitted:
					i18nLink = translate("reference.reopen");
					cmd = "feed-reopen";
					break;
				default: break;
			}

			FormLink mailLink = uifactory.addFormLink("send-" + (++counter), cmd, i18nLink, null, flc, Link.LINK | Link.NONTRANSLATED);
			mailLink.setIconLeftCSS("o_icon o_icon_mail");
			mailLink.getComponent().setSuppressDirtyFormWarning(true);
			FeedbackMember feedbackRef = new FeedbackMember(feedback.getIdentity(), feedback, mailLink);
			mailLink.setUserObject(feedbackRef);
			memberRows.add(feedbackRef);
		}
		
		if(membersFeedbackTableEl != null) {
			membersFeedbackModel.setObjects(memberRows);
			membersFeedbackTableEl.reset(true, true, true);
		}
	}
	
	private Date getPublicFeedbackDeadline() {
		String dayStr = feedbackDeadlineDayElement.getValue();
		String monthStr = feedbackDeadlineMonthElement.getSelectedKey();
		String yearStr = feedbackDeadlineYearElement.getValue();
		
		try {
			int day = Integer.parseInt(dayStr);
			int month = Integer.parseInt(monthStr);
			int year = Integer.parseInt(yearStr);
			return getDeadline(day, month, year, 0, 0);
		} catch (NumberFormatException e) {
			logDebug("Cannot parse date from: " + dayStr + "." + monthStr + "." + yearStr);
			return null;
		}
	}
	
	private Date getDeadline(int day, int month, int year, int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	private boolean validateYearElement(TextElement textEl) {
		boolean ok = true;
		if(StringHelper.containsNonWhitespace(textEl.getValue())) {
			int currentYear = Calendar.getInstance().get(Calendar.YEAR) + 5;
			try {
				int year = Integer.parseInt(textEl.getValue());
				if(year < 2010 || year > currentYear) {
					ok &= false;
					textEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear) });
				}
			} catch (NumberFormatException e) {
				ok =false;
				textEl.setErrorKey("deadline.error", new String[] { Integer.toString(currentYear) });
			}
		}
		return ok;
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(feedbackDeadlineYearElement != null) {
			feedbackDeadlineYearElement.clearError();
			if(getPublicFeedbackDeadline() == null) {
				feedbackDeadlineYearElement.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else {
				allOk &= validateYearElement(feedbackDeadlineYearElement);
			}
		}

		return allOk;
	}
	
	public void commitChanges(Application application) {
		if(enablePublicFeedbackEl != null && enablePublicFeedbackEl.isAtLeastSelected(1)) {
			if(!application.isPublicFeedbackEnabled()) {
				logPublicFeedback(Action.add, "audit.log.public.feedback.link.add");
			}
			application.setPublicFeedbackEnabled(true);
			application.setPublicFeedbackDeadline(getPublicFeedbackDeadline());
		} else {
			if(application.isPublicFeedbackEnabled()) {
				logPublicFeedback(Action.remove, "audit.log.public.feedback.link.delete");
			}
			application.setPublicFeedbackEnabled(false);
			application.setPublicFeedbackDeadline(null);
		}
	}
	
	private void logPublicFeedback(Action action, String messageI18n) {
		String appName = salutationGenerator.getTitleFullname(app, getLocale());
		String appId = app.getId() == null ? "" : app.getId().toString();
		String[] args = new String[] { appName, appId };
		auditService.auditPublicFeedbackLinkLog(action, null, null, messageI18n, args, getTranslator(), position, app, getIdentity());
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addMemberButton == source) {
			doAddFeedbackMember(ureq);
		} else if(enablePublicFeedbackEl == source) {
			updateGUI();
		} else if(membersFeedbackTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				FeedbackMember feedbackRef = membersFeedbackModel.getObject(se.getIndex());
				if("delete-feed".equals(cmd)) {
					doConfirmDelete(ureq, feedbackRef);
				} else if("edit-feed".equals(cmd)) {
					doEdit(ureq, feedbackRef);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if(("feed-invite".equals(cmd) || "feed-reopen".equals(cmd) || "feed-mail".equals(cmd))
					&& link.getUserObject() instanceof FeedbackMember) {
				FeedbackMember appReference = (FeedbackMember)link.getUserObject();
				doSendInvitation(ureq, appReference);
			}
		}

		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmRemoveFeedbackCtrl == source
				|| sendFeedbackInvitationCtrl == source
				|| memberEditCtrl == source) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(addMembersFeedbackWizard == source) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().removeTopModalDialog(addMembersFeedbackWizard.getInitialComponent());
				//reload the list
				loadModel();
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(sendFeedbackInvitationCtrl);
		removeAsListenerAndDispose(confirmRemoveFeedbackCtrl);
		removeAsListenerAndDispose(addMembersFeedbackWizard);
		removeAsListenerAndDispose(memberEditCtrl);
		removeAsListenerAndDispose(cmc);
		sendFeedbackInvitationCtrl = null;
		confirmRemoveFeedbackCtrl = null;
		addMembersFeedbackWizard = null;
		memberEditCtrl = null;
		cmc = null;
	}

	private void doAddFeedbackMember(UserRequest ureq) {
		List<ApplicationsFeedbackConfiguration> configs = feedbackService.getApplicationsFeedbackConfigurations(position);
		if(configs.isEmpty()) {
			return;
		}
		
		ApplicationsFeedbackConfiguration defaultConfig = configs.get(0);
		ApplicationLight appLight = recruitingService.getApplicationLight(position, app);
		if(appLight == null) {
			showWarning("warning.application.deleted");
		} else {
			String applicationFullname = formatFullName(appLight, getTranslator());
			String title = translate("add.apps.feedbacks.member.title.singular", new String[] { applicationFullname });
			List<ApplicationLight> apps = Collections.singletonList(appLight);
		
			Identity secretary = recruitingService.getSecretary(position);
			Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
	
			RecruitingMailTemplate mailTemplate = FeedbackHelper.feedbackTemplate(headOfCommittee, secretary, position, apps,
					null, null, defaultConfig, salutationGenerator, getTranslator());
	
			FeedbackMembersContext feedbackMembersContext = new FeedbackMembersContext(position, defaultConfig,
					apps, secretary, headOfCommittee, mailTemplate);
			feedbackMembersContext.setDeadline(defaultConfig.getDeadline());
			Feedback1EmailStep start = new Feedback1EmailStep(ureq, feedbackMembersContext);
			AddFeedbacksMemberFinishCallback finish = new AddFeedbacksMemberFinishCallback(feedbackMembersContext, getTranslator());
			addMembersFeedbackWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, null);
			listenTo(addMembersFeedbackWizard);
			getWindowControl().pushAsTopModalDialog(addMembersFeedbackWizard.getInitialComponent());
		}
	}
	
	private void doEdit(UserRequest ureq, FeedbackMember feedbackRef) {
		if(guardModalController(memberEditCtrl)) return;
		
		Identity member = feedbackRef.getMember();
		Application application = feedbackRef.getFeedback().getApplication();
		memberEditCtrl = new ApplicationFeedbackMemberEditController(ureq, getWindowControl(),
				member, feedbackRef.getFeedback(), application, position);
		listenTo(memberEditCtrl);
		
		String title = translate("edit.feedback.member");
		cmc = new CloseableModalController(getWindowControl(), "c", memberEditCtrl.getInitialComponent(), title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmDelete(UserRequest ureq, FeedbackMember feedbackRef) {
		if(guardModalController(confirmRemoveFeedbackCtrl)) return;
		
		confirmRemoveFeedbackCtrl = new ConfirmRemoveFeedbackMemberController(ureq, getWindowControl(), position,
				feedbackRef.getFeedback().getApplication(), feedbackRef.getFeedback());
		listenTo(confirmRemoveFeedbackCtrl);

		String title = translate("confirm.remove.feedback.member.title");
		cmc = new CloseableModalController(getWindowControl(), "c", confirmRemoveFeedbackCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doSendInvitation(UserRequest ureq, FeedbackMember appReference) {
		if(guardModalController(sendFeedbackInvitationCtrl)) return;
		
		Identity headOfCommittee = recruitingService.getHeadOfCommittee(position);
		Identity secretary = recruitingService.getSecretary(position);
		ApplicationFeedback feedback = appReference.getFeedback();
		Identity member = feedback.getIdentity();
		List<ApplicationFeedback> feedbacks = Collections.singletonList(feedback);
		List<Application> apps = Collections.singletonList(app);
		
		ApplicationMailTemplate template = FeedbackHelper.feedbackTemplate(headOfCommittee, secretary, position, apps,
				member, feedbacks, feedback.getConfiguration(), salutationGenerator, getTranslator());

		sendFeedbackInvitationCtrl = new SendFeedbackInvitationController(ureq, getWindowControl(), position, feedback, template);
		listenTo(sendFeedbackInvitationCtrl);
		cmc = new CloseableModalController(getWindowControl(), "c", sendFeedbackInvitationCtrl.getInitialComponent(), translate("reference.send.title"));
		cmc.activate();
		listenTo(cmc);
	}
}
