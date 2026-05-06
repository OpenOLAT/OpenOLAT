/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.UserCreateController;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailHelper;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.UsernameValidationRulesFactory;
import org.olat.login.validation.ValidationResult;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.importexternal.ImportExternalUserSearchController;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.ui.RecruitingMainController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MembersController extends FormBasicController {

	public static final String formIdentifyer = "org.olat.modules.selectus.ui.committee_wizard.MembersController";
	private static final String LOGINNAME = "loginname";
	private List<UserPropertyHandler> userPropertyHandlers;

	private TextElement searchEl;
	private TextElement emailTextElement;
	private TextElement usernameTextElement;
	private SingleSelection roleElement;
	private FormLink quickSearchButton;
	
	private CloseableModalController cmc;
	private ImportExternalUserSearchController searchListCtrl;
	
	private CommitteeMember member;
	
	private final String[] roleKeys;
	private final String[] roleValues;
	private final boolean withQuickSearch;
	private final SyntaxValidator usernameSyntaxValidator;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private UsernameValidationRulesFactory usernameRulesFactory;
	
	public MembersController(UserRequest ureq, WindowControl wControl, Form rootForm, CommitteeMember member, String[] availableRoles) {
		super(ureq, wControl, null, UserManager.getInstance().getPropertyHandlerTranslator(Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale(), Util.createPackageTranslator(UserCreateController.class, ureq.getLocale()))));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}

		this.member = member;
		withQuickSearch = showQuickSearch(member);
		this.roleKeys = availableRoles;

		roleValues = new String[roleKeys.length];
		for(int i=roleKeys.length; i-->0; ) {
			roleValues[i] = translate(roleKeys[i]);
		}
		usernameSyntaxValidator = new SyntaxValidator(usernameRulesFactory.createRules(false), false);
		
		initForm(ureq);
	}
	
	private boolean showQuickSearch(CommitteeMember cMember) {
		Identity idMember = cMember.getIdentity();
		if(idMember instanceof TransientIdentity) {
			TransientIdentity transientIdentity = (TransientIdentity)idMember;
			return !transientIdentity.isLdap() && !transientIdentity.isAzure();
		}
		return false;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("wizard.members.title");
		setFormDescription("wizard.members.explanation");
		if(withQuickSearch) {
			initSearchElements(formLayout);
		}
		initFormElements(formLayout);
	}
	
	private void initSearchElements(FormItemContainer formLayout) {
		searchEl = uifactory.addTextElement("search", "search", 64, "", formLayout);
		searchEl.setExampleKey("quick.search.example", null);
		quickSearchButton = uifactory.addFormLink("user.search", formLayout, Link.BUTTON);
		quickSearchButton.setIconLeftCSS("o_icon o_icon-lg o_icon_search");
		
		uifactory.addSpacerElement("space", formLayout, false);
	}
	
	private void initFormElements(FormItemContainer formLayout) {
		setFormTitle("wizard.members.title");

		String email = member.getEmail();
		String proposedUsername = getProposedUsername(email);
		usernameTextElement = uifactory.addTextElement(LOGINNAME, "username", 64, proposedUsername, formLayout);
		usernameTextElement.setMandatory(true);
		usernameTextElement.setDisplaySize(30);
		usernameTextElement.setTranslator(getTranslator());
		if(StringHelper.containsNonWhitespace(proposedUsername)
				&& member != null && member.getIdentity() instanceof TransientIdentity
				&& ((TransientIdentity)member.getIdentity()).isLdap()) {
			usernameTextElement.setEnabled(false);
		}
		
		User user = member.getIdentity() == null ? null : member.getIdentity().getUser();
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(formIdentifyer, true);
		// Add all available user fields to this form
		boolean focused = false;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;
			FormItem formItem = userPropertyHandler.addFormItem(getLocale(), user, formIdentifyer, true, formLayout);
			formItem.setElementCssClass("o_sel_member_" + userPropertyHandler.getName());
			formItem.setTranslator(getTranslator());
			if(!focused) {
				formItem.setFocus(true);
				focused = true;
			}
			// special case to handle email field
			if(userPropertyHandler.getName().equals(UserConstants.GENDER) && formItem instanceof SingleSelection) {
				SingleSelection genderItem = (SingleSelection)formItem;
				if(genderItem.isOneSelected() && "-".equals(genderItem.getSelectedKey()) && user instanceof TransientIdentity) {
					genderItem.select(genderItem.getSelectedKey(), false);
				}
			} else if(userPropertyHandler.getName().equals(UserConstants.EMAIL)) {
				emailTextElement = (TextElement) formItem;
				if(email != null) {
					emailTextElement.setValue(email);
				}
			}
		}
		
		String role = member.getRole();
		roleElement = uifactory.addDropdownSingleselect("role", "role", formLayout, roleKeys, roleValues, null);
		roleElement.setTranslator(getTranslator());
		if(StringHelper.containsNonWhitespace(role)) {
			roleElement.select(role, true);
		} else {
			roleElement.select(roleKeys[0], true);
		}
		roleElement.setVisible(roleKeys.length > 1);
	}
	
	public String getRole() {
		if(roleKeys.length == 1) {
			return roleKeys[0];
		}
		if(roleElement.isOneSelected()) {
			return roleElement.getSelectedKey();
		}
		return null;
	}
	
	private String getProposedUsername(String email) {
		if(member != null
				&& !(member.getIdentity() instanceof TransientIdentity)
				&& member.getIdentity().getKey() != null) {
			String username = member.getIdentity().getUser().getNickName();
			if(!StringHelper.containsNonWhitespace(username)) {
				username = member.getIdentity().getName();
			}
			return username;
		}
		if(!StringHelper.containsNonWhitespace(email)) {
			return "";
		}
		
		if(member != null && member.getIdentity() instanceof TransientIdentity) {
			TransientIdentity tIdentity = (TransientIdentity)member.getIdentity();
			if(tIdentity.isLdap() && StringHelper.containsNonWhitespace(tIdentity.getName())) {
				return tIdentity.getName();
			}
		}
		
		int index = email.indexOf('@');
		if(index > 0) {
			String name = email.substring(0, index);
			if(!StringHelper.containsNonWhitespace(name)) return "";
			
			StringBuilder cleanedName = new StringBuilder();
			
			name = Normalizer.normalize(name, java.text.Normalizer.Form.NFD);
			for(int i=0; i<name.length(); i++) {
				char ch = name.charAt(i);
				if(Character.isDigit(ch)) {
					cleanedName.append(ch);
				} else if (Character.isLetter(ch)) {
					ch = Character.isUpperCase(ch) ? Character.toLowerCase(ch) : ch;				
					cleanedName.append(Character.toLowerCase(ch));
				}
			}
			name = cleanedName.toString();
			if(name.length() < 3){
				for(int i=name.length(); i<3; i++) {
					 name += "0";
				}
			}
			
			List<Identity> identities = securityManager.getIdentitiesByPowerSearch(name, null, false, null, null, null, null, null, null, null);
			if(identities.isEmpty()) {
				return name;
			}
			
			Set<String> usedUsername = new HashSet<>();
			for(Identity identity:identities) {
				usedUsername.add(identity.getName());
			}
			
			//the search used a like
			if(!usedUsername.contains(name)) {
				return name;
			}
			
			for(int i=1; i<100; i++){
				String username = name + i;
				if(!usedUsername.contains(username)) {
					return username;
				}
			}
		}
		return "";
	}
	
	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
	}

	@Override
	public FormItem getInitialFormItem() {
		return flc;
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(member.getStatus() != CommitteeMemberStatus.skipped) {
			// validate if username does match the syntactical login requirements
			String loginName = usernameTextElement.getValue();
			usernameTextElement.clearError();
			ValidationResult validationResult = usernameSyntaxValidator.validate(loginName, member.getIdentity());
			if (usernameTextElement.isEmpty() || !validationResult.isValid()) {			
				usernameTextElement.setErrorKey("new.error.loginname.empty");
				allOk &= false;
			} else if (loginName.length() > 128) {			
				usernameTextElement.setErrorKey("error.username.notlongerthan");
				allOk &= false;
			} else {
				// Check if login is still available
				Identity identity = securityManager.findIdentityByName(loginName);
				if ((identity != null && member.getIdentity() == null) ||
						(identity != null && member.getIdentity() != null && !identity.equals(member.getIdentity()))) {			
					usernameTextElement.setErrorKey("new.error.loginname.choosen");
					allOk &=  false;
				}
			}
	
			// validate special rules for each user property
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {			
				//we assume here that there are only textElements for the user properties
				FormItem formItem = flc.getFormComponent(userPropertyHandler.getName());
				if (!UserConstants.EMAIL.equals(userPropertyHandler.getName())
						&& (!userPropertyHandler.isValid(null, formItem, null) || formItem.hasError())) {
					allOk &= false;	
				} else {
					formItem.clearError();
				}
			}
			// special test on email address: validate if email is already used
			if (emailTextElement != null && !emailTextElement.hasError()) {
				String email = emailTextElement.getValue();
				
				if(MailHelper.isValidEmailAddress(email)) {
					// Check if email is not already taken
					List<Identity> exists = userManager.findIdentitiesByEmail(List.of(email));
					if ((!exists.isEmpty() && member.getIdentity() == null)
							|| (exists.size() == 1 && member.getIdentity() != null && !exists.get(0).equals(member.getIdentity()))
							|| exists.size() > 1) {
						// Oups, email already taken, display error
						emailTextElement.setErrorKey("new.error.email.choosen");
						allOk &= false;
					}
				} else {
					emailTextElement.setErrorKey("email.error.valid");
					allOk &= false;
				}
			}
			//must always return true but...
			roleElement.clearError();
			if(roleElement.isVisible() && !roleElement.isOneSelected()) {
				roleElement.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	public void commitChanges() {
		String username = usernameTextElement.getValue();

		Identity identity = member.getIdentity();
		if(member.getIdentity() == null) {
			identity = new TransientIdentity();
			member.setIdentity(identity);
		}
		if(identity instanceof TransientIdentity) {
			((TransientIdentity)identity).setName(username);	
		}	
		// Now add data from user fields (firstName,lastName and email are mandatory)
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			FormItem propertyItem = flc.getFormComponent(userPropertyHandler.getName());
			userPropertyHandler.updateUserFromFormItem(identity.getUser(), propertyItem);
		}
		
		member.setRole(getRole());
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(searchListCtrl == source) {
			if(event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent mse = (SingleIdentityChosenEvent)event;
				doSelect(ureq, mse.getChosenIdentity());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cmc.deactivate();
			cleanUp();
		}
		
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(searchListCtrl);
		removeAsListenerAndDispose(cmc);
		searchListCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == quickSearchButton) {
			doQuickSearch(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doQuickSearch(UserRequest ureq) {
		String searchString = searchEl.getValue();
		
		searchEl.clearError();
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchListCtrl = new ImportExternalUserSearchController(ureq, getWindowControl(), searchString, false);
			listenTo(searchListCtrl);
			cmc = new CloseableModalController(getWindowControl(), "c", searchListCtrl.getInitialComponent(), translate("results"));
			listenTo(cmc);
			cmc.activate();
		} else {
			searchEl.setErrorKey("form.legende.mandatory");
		}
	}
	
	private void doSelect(UserRequest ureq, Identity identity) {
		member.setIdentity(identity);
		if(identity instanceof IdentityImpl && identity.getKey() != null) {
			// a real saved user
			member.setStatus(CommitteeMemberStatus.ok);
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			usernameTextElement.setValue(identity.getName());
			if(StringHelper.containsNonWhitespace(identity.getName())
					&& identity instanceof TransientIdentity && ((TransientIdentity)identity).isLdap()) {
				usernameTextElement.setEnabled(false);
			} else {
				usernameTextElement.setEnabled(true);
			}
			emailTextElement.setValue(identity.getUser().getProperty(UserConstants.EMAIL, getLocale()));
			
			// Now add data from user fields (firstName,lastName and email are mandatory)
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				FormItem propertyItem = flc.getFormComponent(userPropertyHandler.getName());
				if(propertyItem instanceof TextElement) {
					TextElement textItem = (TextElement)propertyItem;
					String value = identity.getUser().getProperty(userPropertyHandler.getName(), getLocale());
					textItem.setValue(value);
				}
			}
		}
	}
}
