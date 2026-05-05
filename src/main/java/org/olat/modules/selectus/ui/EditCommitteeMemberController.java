/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Preferences;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.registration.RegistrationManager;
import org.olat.registration.RegistrationModule;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.committee.wizard.MembersController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  20 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EditCommitteeMemberController extends FormBasicController {
	
	private static final String formIdentifyer = MembersController.formIdentifyer;
	private static final String LOGINNAME = "loginname";

	private final String[] roleKeys;
	private final String[] roleValues;
	private Map<String, String> formContext;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	private TextElement emailTextElement;
	private SingleSelection roleElement;
	private FormLink sendPasswordLink;
	
	private final Identity member;
	private final PositionRole role;
	private final Position position;
	
	@Autowired
	private UserManager um;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private AuditService auditService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private RegistrationManager registrationManager;
	
	public EditCommitteeMemberController(UserRequest ureq, WindowControl wControl, Position position, Identity member, PositionRole role) {
		super(ureq, wControl, null, UserManager.getInstance().getPropertyHandlerTranslator(Util.createPackageTranslator(MembersController.class, ureq.getLocale())));
		this.member = member;
		this.role = role;
		this.position = position;
		formContext = new HashMap<>();

		if(recruitingModule.isRoleExOfficioEnabled()) {
			roleKeys = PositionRole.roles();
		} else {
			roleKeys = new String[]{ PositionRole.member.role(), PositionRole.head.role(), PositionRole.secretary.role() };
		}
		
		roleValues = new String[roleKeys.length];
		for(int i=roleKeys.length; i-->0; ) {
			roleValues[i] = translate(roleKeys[i]);
		}
		initForm(ureq);
	}
	
	public Identity getMember() {
		return member;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		switch(role) {
			case member:
				setFormDescription("edit.committee.member.description");
				break;
			case secretary:
				setFormDescription("edit.committee.secretary.description");
				break;
			case head:
				setFormDescription("edit.committee.head.description");
				break;
			case exofficio:
				setFormDescription("edit.committee.exofficio.description");
				break;
		}

		final User user = member.getUser();
		final String username = member.getName();
		
		uifactory.addStaticTextElement(LOGINNAME, "username", username, formLayout);
		formContext.put("username", username);

		sendPasswordLink = uifactory.addFormLink("tmp.password", "edit.committee.password.tmp", "edit.committee.password", formLayout, Link.LINK);
		sendPasswordLink.setIconLeftCSS("o_icon o_icon_external_link");
		sendPasswordLink.setHelpText(translate("edit.committee.password.hint"));

		userPropertyHandlers = um.getUserPropertyHandlersFor(formIdentifyer, true);
		// Add all available user fields to this form
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			userPropertyHandler.addFormItem(ureq.getLocale(), user, formIdentifyer, true, formLayout);
		}
		
		roleElement = uifactory.addDropdownSingleselect("role", "role", formLayout, roleKeys, roleValues, null);
		if(StringHelper.containsNonWhitespace(role.role())) {
			roleElement.select(role.role(), true);
		} else {
			roleElement.select(roleKeys[0], true);
		}
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		// validate special rules for each user property
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {			
			//we assume here that there are only textElements for the user properties
			FormItem formItem = flc.getFormComponent(userPropertyHandler.getName());
			formItem.clearError();
			if ( ! userPropertyHandler.isValid(member.getUser(), formItem, formContext) || formItem.hasError()) {
				allOk &= false;				
			}
		}
		// special test on email address: validate if email is already used
		if (emailTextElement != null) {	
			emailTextElement.clearError();
			
			String email = emailTextElement.getValue();
			// Check if email is not already taken
			List<Identity> exists = um.findIdentitiesByEmail(List.of(email));
			//TODO selectus
			if (exists.size() > 1 || (exists.size() == 0 && !exists.get(0).equals(member))) {
				// Oups, email already taken, display error
				emailTextElement.setErrorKey("new.error.email.choosen");
				allOk &= false;
			}
		}
		
		//only security, it must always one be selected 
		allOk &= roleElement.isOneSelected();
		
		return allOk && super.validateFormLogic(ureq);
	}
	
	private PositionRole getRole() {
		String selectedRole = roleElement.getSelectedKey();
		if(role.role().equals(selectedRole)) {
			return null;
		}
		return PositionRole.role(selectedRole);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(sendPasswordLink == source) {
			doSendMailWithToken(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		final Identity memberToModify = getMember();
		erFrontendManager.createOLATResource(position);
		
		// update the user profile data
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(
			OresHelper.createOLATResourceableInstance(Identity.class, memberToModify.getKey()), new SyncerExecutor() {
				@Override
				public void execute() {
					Identity identityToModify = securityManager.loadIdentityByKey(memberToModify.getKey());
					String before = auditService.toAuditXml(identityToModify);
					
					User user = identityToModify.getUser();
					// Now add data from user fields (firstName,lastName and email are mandatory)
					for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
						FormItem propertyItem = flc.getFormComponent(userPropertyHandler.getName());
						userPropertyHandler.updateUserFromFormItem(user, propertyItem);
					}
					
					if (!um.updateUserFromIdentity(identityToModify)) {
						// reload user data from db
						logError("Cannot update committee member", null);
					}
					
					PositionRole newRole = getRole();
					if(newRole != null) {
						switch(newRole) {
							case member: erFrontendManager.addToCommittee(position, identityToModify); break;
							case head: erFrontendManager.addToCommitteeAsHead(position, identityToModify); break;
							case secretary: erFrontendManager.addToCommitteeAsSecretary(position, identityToModify); break;
							case exofficio: erFrontendManager.addToCommitteeAsExOfficio(position, identityToModify); break;
						}
						
						String messageI18n = "audit.log.committe.update.member.role";
						String[] messageArgs = new String[] {
								member.getKey().toString(),
								RecruitingHelper.formatFullNameWithTitle(member, getLocale()),
								translate(newRole.role()), translate(role.role())
							};
						auditService.auditCommitteeLog(Action.update, ActionTarget.committee, role.name(), newRole.name(),
								messageI18n, messageArgs, getTranslator(), position, member, getIdentity());
					}
					
					String after = auditService.toAuditXml(identityToModify);
					if(!before.equals(after)) {
						String messageI18n = "audit.log.committe.edit.member";
						String[] messageArgs = new String[] {
								member.getKey().toString(),
								RecruitingHelper.formatFullNameWithTitle(member, getLocale())
							};
						auditService.auditCommitteeLog(Action.update, ActionTarget.committee, before, after,
								messageI18n, messageArgs, getTranslator(), position, member, getIdentity());
					}
					
					CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new MultiUserEvent("changed"),
						OresHelper.createOLATResourceableInstance(Identity.class, identityToModify.getKey()));
				}
			});
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doSendMailWithToken(UserRequest ureq) {
		Preferences prefs = member.getUser().getPreferences();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(prefs.getLanguage());
		String emailAdress = member.getUser().getProperty(UserConstants.EMAIL, locale);
		if (StringHelper.containsNonWhitespace(emailAdress)) {
			String serverpath = Settings.getServerContextPathURI();
			Translator userTrans = Util.createPackageTranslator(RegistrationManager.class, locale);
			String toolName = Util.createPackageTranslator(RecruitingMainController.class, getLocale())
					.translate("topnav.home");
			
			String ip = ureq.getHttpReq().getRemoteAddr();
			registrationManager.createAndDeleteOldTemporaryKey(member.getKey(), emailAdress, ip,
					RegistrationManager.PW_CHANGE, registrationModule.getRESTValidityOfTemporaryKey());

			String subject = userTrans.translate("pwchange.subject", toolName);
			String body = userTrans.translate("pwchange.intro", member.getName(), toolName, serverpath)
					+ userTrans.translate("pwchange.body.send", serverpath, emailAdress, i18nModule.getLocaleKey(locale));
			sendToken(body, subject);
		} else {
			showWarning("");
		}
	}
	
	private void sendToken(String body, String subject) {
		MailBundle bundle = new MailBundle();
		bundle.setToId(member);
		bundle.setContent(subject, body);
		MailerResult result = mailManager.sendExternMessage(bundle, new MailerResult(), true);
		if(result.getReturnCode() == MailerResult.OK) {
			showInfo("email.sent");
		} else {
			showError("email.notsent");
		}
	}
}