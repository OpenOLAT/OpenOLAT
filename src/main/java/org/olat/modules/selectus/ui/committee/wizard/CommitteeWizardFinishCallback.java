/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.wizard;

import java.util.List;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.User;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 20 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CommitteeWizardFinishCallback implements StepRunnerCallback {
	
	private final Position position;
	private final Translator translator;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingService erFrontendManager;
	@Autowired
	private OrganisationService organisationService;
	
	public CommitteeWizardFinishCallback(Position position, Translator translator) {
		CoreSpringFactory.autowireObject(this);
		this.position = position;
		this.translator = translator;
	}
	
	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		Committee committee = (Committee)runContext.get(CommitteeWizard.COMMITTEE);
		if(committee.getMembers() != null && !committee.getMembers().isEmpty()) {
			for(CommitteeMember member:committee.getMembers()) {
				if(member != null && member.getStatus() != CommitteeMemberStatus.skipped && member.getIdentity() != null) {
					Identity identity = member.getIdentity();
					if(identity instanceof TransientIdentity) {
						identity = createMember((TransientIdentity)identity, ureq.getIdentity());
					} else {
						userManager.updateUser(identity, identity.getUser());
					}

					switch(PositionRole.role(member.getRole())) {
						case head: erFrontendManager.addToCommitteeAsHead(position, identity); break;
						case secretary: erFrontendManager.addToCommitteeAsSecretary(position, identity); break;
						case exofficio: erFrontendManager.addToCommitteeAsExOfficio(position, identity); break;
						default: erFrontendManager.addToCommittee(position, identity); break;
					}
					
					// log
					String messageI18n = "audit.log.committe.add.member";
					String[] messageArgs = new String[] {
							identity.getKey().toString(),
							RecruitingHelper.formatFullNameWithTitle(identity, translator.getLocale()),
							translator.translate(PositionRole.role(member.getRole()).role())
						};
					auditService.auditCommitteeLog(Action.add, ActionTarget.committee, messageI18n, messageArgs, translator, position, identity, ureq.getIdentity());
				}
			}
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private Identity createMember(TransientIdentity tIdentity, Identity doer) {
		// Create new user and identity and put user to users group
		// Create transient user without firstName,lastName, email
		User newUser = userManager.createUser(tIdentity.getFirstName(), tIdentity.getLastName(), tIdentity.getEmail());
		// Now add data from user fields (firstName,lastName and email are mandatory)
		//TODO selectus
		List<UserPropertyHandler> handlers = userManager.getUserPropertyHandlersFor(MembersController.formIdentifyer, true);
		for (UserPropertyHandler handler: handlers) {
			String value = tIdentity.getProperty(handler.getName());
			newUser.setProperty(handler.getName(), value);
		}
		Organisation organisation = organisationService.getDefaultOrganisation();//
		return erFrontendManager.createCommitteeIdentity(tIdentity.getName(), newUser, tIdentity.isLdap(), tIdentity.isAzure(), position, organisation, doer);
	}
}
