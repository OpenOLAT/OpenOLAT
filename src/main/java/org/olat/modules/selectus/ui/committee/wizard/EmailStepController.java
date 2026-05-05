/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.OAuth2Tokens;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.ui.RecruitingHelper;

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

	public static final String formIdentifyer = MembersController.formIdentifyer;
	
	private final EmailController emailController;
	
	@Autowired
	private RecruitingService erFrontendManager;
	
	public EmailStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_VERTICAL, null);
		emailController = new EmailController(ureq, wControl, form);
		listenTo(emailController);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}

	@Override
	public FormItem getStepFormItem() {
		return emailController.getInitialFormItem();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		return emailController.validateFormLogic(ureq);
	}

	@Override
	protected void formFinish(UserRequest ureq) {
		//do nothing
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Committee committee = (Committee)getFromRunContext(CommitteeWizard.COMMITTEE);
		if(committee == null) {
			committee = new Committee();
			addToRunContext(CommitteeWizard.COMMITTEE, committee);
		}
		
		String role = emailController.getRole();
		String email = emailController.getEMail();
		MembersValidator membersValidator = new MembersValidator(getLocale());
		List<String> emailTokens = RecruitingHelper.splitEmails(email);
		for(String emailToken:emailTokens) {
			if(StringHelper.containsNonWhitespace(emailToken)) {
				emailToken = emailToken.trim();
				if(!committee.hasMember(emailToken)) {
					OAuth2Tokens accessToken = ureq.getUserSession().getOAuth2Tokens();
					Identity identity = erFrontendManager.findCommitteeMember(emailToken, accessToken);
					CommitteeMember member;
					if(identity == null) {
						identity = new TransientIdentity();
						identity.getUser().setProperty(UserConstants.EMAIL, emailToken);
						member = new CommitteeMember(role, emailToken, identity);
					} else if(!validateIdentity(identity)) {
						member = new CommitteeMember(role, emailToken, identity);
						member.setStatus(CommitteeMemberStatus.notValid);
					} else {
						member = new CommitteeMember(role, emailToken, identity);
						member.setStatus(membersValidator.valid(member, new ArrayList<>()));
					}
					committee.addMember(member);
				} else {
					committee.getMember(emailToken).setRole(role);
				}
			}
		}

		if(committee.isComplete()) {
			fireEvent(ureq, StepsEvent.INFORM_FINISHED);
		} else {
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}
	
	private boolean validateIdentity(Identity identity) {
		boolean allOk = true;
		
		if(identity instanceof TransientIdentity) {
			TransientIdentity tmpIdentity = (TransientIdentity)identity;
			if(tmpIdentity.isAzure()) {
				if(!StringHelper.containsNonWhitespace(tmpIdentity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()))) {
					allOk &= false;
				}
				if(!StringHelper.containsNonWhitespace(tmpIdentity.getUser().getProperty(UserConstants.LASTNAME, getLocale()))) {
					allOk &= false;
				}
				if(!StringHelper.containsNonWhitespace(tmpIdentity.getName()) || "0_".equals(tmpIdentity.getName())) {
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}
}
