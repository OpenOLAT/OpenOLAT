/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import org.olat.basesecurity.events.MultiIdentityChosenEvent;
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
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ldap.LDAPLoginModule;
import org.olat.login.oauth.OAuthLoginModule;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.user.ui.importexternal.ImportExternalUserSearchController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EmailController extends FormBasicController {

	private FormLink searchButton;
	private TextElement emailElement;
	private SingleSelection roleElement;
	
	private final String[] roleKeys;
	private final String[] roleValues;
	
	private CloseableModalController cmc;
	private ImportExternalUserSearchController searchListCtrl;

	@Autowired
	private LDAPLoginModule ldapModule;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private OAuthLoginModule oauthLoginModule;
	
	public EmailController(UserRequest ureq, WindowControl wControl, Form rootForm) {
		super(ureq, wControl, null, Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale()));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		
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

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("wizard.email.title");
		if(ldapModule.isLDAPEnabled() && ldapModule.isLdapLookupEnabled()) {
			setFormDescription("wizard.email.explanation.ldap");
		} if(oauthLoginModule.isAzureAdfsEnabled() && oauthLoginModule.isAzureLookupEnabled()) {
			setFormDescription("wizard.email.explanation.azure");
			if(ureq.getUserSession().getOAuth2Tokens() == null) {
				setFormWarning("warning.wizard.azure.without.token");
			}
		} else {
			setFormDescription("wizard.email.explanation");
		}
		formLayout.setElementCssClass("o_sel_position_committee_email_step");
		
		emailElement = uifactory.addTextElement("email", "email", 4096, "", formLayout);
		emailElement.setElementCssClass("o_sel_committee_email");
		emailElement.setFocus(true);
		
		searchButton = uifactory.addFormLink("user.search", "user.search", null, formLayout,  Link.LINK);
		searchButton.setIconLeftCSS("o_icon o_icon-lg o_icon_search");
		
		roleElement = uifactory.addDropdownSingleselect("role", "role", formLayout, roleKeys, roleValues, null);
		roleElement.setElementCssClass("o_sel_committee_role");
		roleElement.select(roleKeys[0], true);
	}

	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String mail = emailElement.getValue();
		if(!StringHelper.containsNonWhitespace(mail)) {
			emailElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}

		return allOk;
	}

	public String getEMail() {
		return emailElement.getValue();
	}
	
	public String getRole() {
		if(roleElement.isOneSelected()) {
			return roleElement.getSelectedKey();
		}
		return null;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchListCtrl == source) {
			if(event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent sice = (SingleIdentityChosenEvent)event;
				appendEmail(sice.getChosenIdentity());
			} else if(event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent mice = (MultiIdentityChosenEvent)event;
				if(mice.getChosenIdentities() != null && !mice.getChosenIdentities().isEmpty()) {
					for(Identity identity:mice.getChosenIdentities()) {
						appendEmail(identity);
					}
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
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
		if(searchButton == source) {
			doSearch(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void appendEmail(Identity identity) {
		String email = identity.getUser().getProperty(UserConstants.EMAIL, getLocale());
		if(!StringHelper.containsNonWhitespace(email)) {
			email = identity.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, getLocale());
		}
		if(StringHelper.containsNonWhitespace(email)) {
			String current = emailElement.getValue();
			if(StringHelper.containsNonWhitespace(current)) {
				current += ";";
			}
			current += email;
			emailElement.setValue(current);
		}
	}
	
	private void doSearch(UserRequest ureq) {
		searchListCtrl = new ImportExternalUserSearchController(ureq, getWindowControl(), true, true, false, false);
		listenTo(searchListCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "c", searchListCtrl.getInitialComponent(), translate("results"));
		listenTo(cmc);
		cmc.activate();
	}
}
