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
package org.olat.modules.selectus.ui.feedback.appsfeedback.wizard;

import java.util.List;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.OAuth2Tokens;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.ldap.LDAPLoginModule;
import org.olat.login.oauth.OAuthLoginModule;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.committee.wizard.CommitteeMember;
import org.olat.modules.selectus.ui.committee.wizard.CommitteeMemberStatus;
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
public class EmailStepController extends StepFormBasicController {

	private FormLink searchButton;
	private TextElement emailElement;
	
	private final FeedbackMembersContext feedbacksContext;
	
	private CloseableModalController cmc;
	private ImportExternalUserSearchController searchListCtrl;

	@Autowired
	private LDAPLoginModule ldapModule;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired
	private OAuthLoginModule oauthLoginModule;
	
	public EmailStepController(UserRequest ureq, WindowControl wControl,
			FeedbackMembersContext feedbacksContext, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		this.feedbacksContext = feedbacksContext;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("wizard.email.title");
		if(ldapModule.isLDAPEnabled() && ldapModule.isLdapLookupEnabled()) {
			setFormDescription("wizard.email.explanation.ldap");
		} else if(oauthLoginModule.isAzureAdfsEnabled() && oauthLoginModule.isAzureLookupEnabled()) {
			setFormDescription("wizard.email.explanation.azure");
			if(ureq.getUserSession().getOAuth2Tokens() == null) {
				setFormWarning("warning.azure.without.token");
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
	protected void formNext(UserRequest ureq) {
		String email = getEMail();
		List<String> emailTokens = RecruitingHelper.splitEmails(email);
		for(String emailToken:emailTokens) {
			if(StringHelper.containsNonWhitespace(emailToken)) {
				emailToken = emailToken.trim();
				if(!feedbacksContext.hasMember(emailToken)) {
					OAuth2Tokens accessToken = ureq.getUserSession().getOAuth2Tokens();
					Identity identity = recruitingService.findCommitteeMember(emailToken, accessToken);
					CommitteeMember member;
					if(identity == null) {
						identity = new TransientIdentity();
						identity.getUser().setProperty(UserConstants.EMAIL, emailToken);
						member = new CommitteeMember(FeedbackMembersContext.FACULTY_MEMBER_PSEUDO_ROLE, emailToken, identity);
					} else {
						member = new CommitteeMember(FeedbackMembersContext.FACULTY_MEMBER_PSEUDO_ROLE, emailToken, identity);
						member.setStatus(CommitteeMemberStatus.ok);
					}
					feedbacksContext.addMember(member);
				}
			}
		}

		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
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
