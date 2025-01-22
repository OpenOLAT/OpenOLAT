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
package org.olat.modules.curriculum.ui.wizard;

import java.util.List;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.ui.member.MembershipModification;
import org.olat.modules.curriculum.ui.wizard.MembersContext.AccessInfos;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.ResourceReservation;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddMemberFinishCallback extends AbstractMemberCallback {
	
	private final MembersContext membersContext;
	

	public AddMemberFinishCallback(MembersContext membersContext) {
		super();
		this.membersContext = membersContext;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		final String comment = membersContext.getOrderComment();
		final String poNumber = membersContext.getPurchaseOrderNumber();
		final List<Identity> identities = membersContext.getSelectedIdentities();
		List<CurriculumElement> curriculumElements = membersContext.getAllCurriculumElements();
		List<MembershipModification> modifications = membersContext.getModifications();
		List<CurriculumElementMembershipChange> changes = applyModification(identities, curriculumElements, modifications);
		
		if(!changes.isEmpty()) {
			MailerResult result = new MailerResult();
			MailTemplate template = membersContext.getMailTemplate();
			MailPackage mailPackage = new MailPackage(template, result, (MailContext)null, template != null);
			curriculumService.updateCurriculumElementMemberships(ureq.getIdentity(), ureq.getUserSession().getRoles(), changes, mailPackage);
			
			AccessInfos offer = membersContext.getSelectedOffer();
			if(offer != null) {
				for(Identity identity:identities) {
					acService.createAndSaveOrder(identity, offer.offerAccess(), OrderStatus.PREPAYMENT, poNumber, comment);
				}
			}
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
	
	@Override
	protected boolean allowModification(MembershipModification modification, CurriculumElementMembership membership, ResourceReservation reservation) {
		return (membership == null || !membership.getRoles().contains(modification.role()))
				&& (reservation == null || modification.nextStatus() != GroupMembershipStatus.reservation);
	}
}
