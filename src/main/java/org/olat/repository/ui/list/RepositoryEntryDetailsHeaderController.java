/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.repository.ui.list;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.group.BusinessGroupService;
import org.olat.repository.LeavingStatusList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.AccessDeniedFactory;
import org.olat.repository.ui.AccessDeniedFactory.AccessDeniedMessage;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Jan 15, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryDetailsHeaderController extends AbstractDetailsHeaderController {

	private final RepositoryEntry entry;
	private final boolean isMember;
	private final boolean closeTabOnLeave;
	
	private DialogBoxController leaveDialogBox;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private BusinessGroupService businessGroupService;

	public RepositoryEntryDetailsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			boolean isMember, boolean closeTabOnLeave) {
		super(ureq, wControl);
		this.entry = entry;
		this.isMember = isMember;
		this.closeTabOnLeave = closeTabOnLeave;
		
		init(ureq);
	}

	@Override
	protected String getIconCssClass() {
		return RepositoyUIFactory.getIconCssClass(entry);
	}

	@Override
	protected String getExternalRef() {
		return entry.getExternalRef();
	}

	@Override
	protected String getTranslatedTechnicalType() {
		if (StringHelper.containsNonWhitespace(entry.getTechnicalType())) {
			NodeAccessType type = NodeAccessType.of(entry.getTechnicalType());
			return ConditionNodeAccessProvider.TYPE.equals(type.getType())
					? translate("CourseModule")
					: nodeAccessService.getNodeAccessTypeName(type, getLocale());
		}
		return null;
	}

	@Override
	protected String getTitle() {
		return entry.getDisplayname();
	}

	@Override
	protected String getAuthors() {
		return entry.getAuthors();
	}

	@Override
	protected String getTeaser() {
		return entry.getTeaser();
	}

	@Override
	protected VFSLeaf getTeaserImage() {
		return repositoryService.getIntroductionImage(entry);
	}

	@Override
	protected VFSLeaf getTeaserMovie() {
		return repositoryService.getIntroductionMovie(entry);
	}

	@Override
	protected RepositoryEntryEducationalType getEducationalType() {
		return entry.getEducationalType();
	}

	@Override
	protected boolean isPreview() {
		return false;
	}
	
	@Override
	protected void initAccess(UserRequest ureq) {
		if (ureq.getUserSession().getRoles() == null) {
			initOffers(ureq, false, Boolean.TRUE);
			return;
		}
		
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, entry);
		boolean guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		boolean inviteeOnly  = ureq.getUserSession().getRoles().isInviteeOnly();
		
		if (!guestOnly && reSecurity.isParticipant() && repositoryService.isParticipantAllowedToLeave(entry)) {
			startCtrl.getLeaveLink().setVisible(true);
		}
		
		if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach()) {
			startCtrl.getInitialComponent().setVisible(true);
		} else {
			if (reSecurity.canLaunch()) {
				startCtrl.getInitialComponent().setVisible(true);
			} else if (isMember && acService.isAccessRefusedByStatus(entry, getIdentity())) {
				startCtrl.getInitialComponent().setVisible(true);
				startCtrl.getStartLink().setEnabled(false);
				
				AccessDeniedMessage accessDeniedMessage = AccessDeniedFactory.createRepositoryEntryStatusNotPublishedMessage();
				setWarning(translate(accessDeniedMessage.messageI18nKey()), translate(accessDeniedMessage.hintI18nKey(), accessDeniedMessage.hintArgs()));
			} else if (isMember || reSecurity.isMasterCoach()) {
				startCtrl.getInitialComponent().setVisible(true);
				startCtrl.getStartLink().setEnabled(false);
				
				AccessDeniedMessage accessDeniedMessage = AccessDeniedFactory.createRepositoryEntryStatusNotPublishedMessage();
				setWarning(translate(accessDeniedMessage.messageI18nKey()), translate(accessDeniedMessage.hintI18nKey(), accessDeniedMessage.hintArgs()));
			} else if(inviteeOnly) {
				showAccessDenied(AccessDeniedFactory.createNoAccess(ureq, getWindowControl()));
			} else if (!isMember && entry.isPublicVisible()) {
				ResourceReservation reservation = acService.getReservation(getIdentity(), entry.getOlatResource());
				if (acService.isAccessToResourcePending(entry.getOlatResource(), getIdentity())
						|| reservation != null) {
					if(acService.canReservationBeSkipped(getIdentity(), reservation)) {
						// Has cancelled the payment process and start it again
						initOffers(ureq, guestOnly, null);
					} else {
						startCtrl.getInitialComponent().setVisible(true);
						startCtrl.getStartLink().setEnabled(false);
						showInfoMessage(translate("access.denied.not.accepted.yet"));
					}
				} else {
					initOffers(ureq, guestOnly, null);
				}
			} else if (guestOnly) {
				showAccessDenied(AccessDeniedFactory.createNoGuestAccess(ureq, getWindowControl()));
			} else if (!AccessDeniedFactory.isNotInAuthorOrganisation(entry, ureq.getUserSession().getRoles())) {
				showAccessDenied(AccessDeniedFactory.createNotInAuthorOrganisation(ureq, getWindowControl(), getIdentity()));
			} else if (!reSecurity.isMember()) {
				showAccessDenied(AccessDeniedFactory.createNotMember(ureq, getWindowControl(), entry));
			} else {
				showAccessDenied(AccessDeniedFactory.createNoAccess(ureq, getWindowControl()));
			}
		}
	}

	private void initOffers(UserRequest ureq, boolean guestOnly, Boolean webPublish) {
		if (webPublish != null && webPublish.booleanValue()) {
			boolean created = showGuestStartLink();
			if (created) {
				return;
			}
		}
		
		AccessResult acResult = acService.isAccessible(entry, getIdentity(), isMember, guestOnly, webPublish, false);
		if (acResult.isAccessible()) {
			startCtrl.getInitialComponent().setVisible(true);
		} else if (!acResult.getAvailableMethods().isEmpty()) {
			if (acResult.getAvailableMethods().size() == 1 && acResult.getAvailableMethods().get(0).getOffer().isAutoBooking()) {
				startCtrl.getInitialComponent().setVisible(true);
				startCtrl.setAutoBooking(true);
			} else {
				showOffers(ureq, acResult.getAvailableMethods(), false, webPublish != null && webPublish, getIdentity());
			}
		} else if (!getOffersNowNotInRange(entry, getIdentity()).isEmpty()) {
			showAccessDenied(AccessDeniedFactory.createOfferNotNow(ureq, getWindowControl(), getOffersNowNotInRange(entry, getIdentity())));
		} else {
			showAccessDenied(AccessDeniedFactory.createNoAccess(ureq, getWindowControl()));
		}
	}
	
	private List<Offer> getOffersNowNotInRange(RepositoryEntry re, Identity identity) {
		List<? extends OrganisationRef> offerOrganisations = CoreSpringFactory.getImpl(ACService.class).getOfferOrganisations(identity);
		return CoreSpringFactory.getImpl(ACService.class).getOffers(re, true, false, null, true, null, offerOrganisations);
	}
	
	private boolean showGuestStartLink() {
		if (acService.isGuestAccessible(entry, true)) {
			String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath) + "?guest=true";
			
			startCtrl.getInitialComponent().setVisible(true);
			startCtrl.getStartLink().setVisible(false);
			startCtrl.getGuestStartLink().setVisible(true);
			startCtrl.getGuestStartLink().setUrl(url);
			return true;
		}
		return false;
	}
	
	@Override
	protected String getStartLinkText() {
		return translate("open.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
	}

	@Override
	protected boolean tryAutoBooking(UserRequest ureq) {
		AccessResult acResult = acService.isAccessible(entry, getIdentity(), null, false, null, false);
		return acService.tryAutoBooking(getIdentity(), entry, acResult);
	}

	@Override
	protected Long getResourceKey() {
		return entry.getOlatResource().getKey();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == startCtrl) {
			if (event == LEAVE_EVENT) {
				doConfirmLeave(ureq);
			}
		} else if (leaveDialogBox == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doLeave(ureq);
				if (!closeTabOnLeave) {
					fireEvent(ureq, new LeavingEvent(entry));
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doConfirmLeave(UserRequest ureq) {
		String reName = StringHelper.escapeHtml(entry.getDisplayname());
		String title = translate("sign.out");
		String text = "<div class='o_warning'>" + translate("sign.out.dialog.text", reName) + "</div>";
		leaveDialogBox = activateYesNoDialog(ureq, title, text, leaveDialogBox);
	}
	
	private void doLeave(UserRequest ureq) {
		MailerResult result = new MailerResult();
		MailPackage reMailing = new MailPackage(result, getWindowControl().getBusinessControl().getAsString(), true);
		LeavingStatusList status = new LeavingStatusList();
		//leave course
		repositoryManager.leave(getIdentity(), entry, status, reMailing);
		//leave groups
		businessGroupService.leave(getIdentity(), entry, status, reMailing);
		DBFactory.getInstance().commit();//make sure all changes are committed
		
		if(status.isWarningManagedGroup() || status.isWarningManagedCourse()) {
			showWarning("sign.out.warning.managed");
		} else if(status.isWarningGroupWithMultipleResources()) {
			showWarning("sign.out.warning.mutiple.resources");
		} else {
			showInfo("sign.out.success", new String[]{ StringHelper.escapeHtml(entry.getDisplayname()) });
			if (closeTabOnLeave) {
				getWindowControl().getWindowBackOffice().getWindow().getDTabs().closeDTab(ureq, entry.getOlatResource(), null);
			}
		}
	}

}
