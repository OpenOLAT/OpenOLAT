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
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.link.Link;
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
import org.olat.group.BusinessGroupService;
import org.olat.repository.LeavingStatusList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.AccessDeniedFactory;
import org.olat.repository.ui.AccessDeniedFactory.AccessDeniedMessage;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.Offer;
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
	protected RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;

	public RepositoryEntryDetailsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			boolean isMember, boolean closeTabOnLeave) {
		super(ureq, wControl);
		this.entry = entry;
		this.isMember = isMember;
		this.closeTabOnLeave = closeTabOnLeave;
		
		initForm(ureq);
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
	protected void initAccess(UserRequest ureq, FormLayoutContainer layoutCont) {
		if (ureq.getUserSession().getRoles() == null) {
			initOffers(ureq, layoutCont, false, Boolean.TRUE);
			return;
		}
		
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, entry);
		boolean guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		boolean inviteeOnly  = ureq.getUserSession().getRoles().isInviteeOnly();
		
		leaveLink = createLeaveLink(layoutCont, guestOnly, reSecurity.isParticipant());
		
		if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach()) {
			startLink = createStartLink(layoutCont);
		} else {
			if (reSecurity.canLaunch()) {
				startLink = createStartLink(layoutCont);
			} else if (isMember && acService.isAccessRefusedByStatus(entry, getIdentity())) {
				AccessDeniedMessage accessDeniedMessage = AccessDeniedFactory.createRepositoryEntryStatusNotPublishedMessage(ureq, entry);
				layoutCont.contextPut("warning", translate(accessDeniedMessage.messageI18nKey()));
				layoutCont.contextPut("warningHint", translate(accessDeniedMessage.hintI18nKey(), accessDeniedMessage.hintArgs()));
				
				startLink = createStartLink(layoutCont);
				startLink.setEnabled(false);
			} else if (isMember || reSecurity.isMasterCoach()) {
				AccessDeniedMessage accessDeniedMessage = AccessDeniedFactory.createRepositoryEntryStatusNotPublishedMessage(ureq, entry);
				layoutCont.contextPut("warning", translate(accessDeniedMessage.messageI18nKey()));
				layoutCont.contextPut("warningHint", translate(accessDeniedMessage.hintI18nKey(), accessDeniedMessage.hintArgs()));
				
				startLink = createStartLink(layoutCont);
				startLink.setEnabled(false);
			} else if(inviteeOnly) {
				showAccessDenied(AccessDeniedFactory.createNoAccess(ureq, getWindowControl()));
			} else if (!isMember && entry.isPublicVisible()) {
				if (acService.isAccessToResourcePending(entry.getOlatResource(), getIdentity())) {
					layoutCont.contextPut("info", translate("access.denied.not.accepted.yet"));
					
					startLink = createStartLink(layoutCont);
					startLink.setEnabled(false);
				} else {
					initOffers(ureq, layoutCont, guestOnly, null);
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

	private void initOffers(UserRequest ureq, FormLayoutContainer layoutCont, boolean guestOnly, Boolean webPublish) {
		if (webPublish != null && webPublish.booleanValue()) {
			boolean created = initGuestLink(layoutCont);
			if (created) {
				return;
			}
		}
		
		AccessResult acResult = acService.isAccessible(entry, getIdentity(), isMember, guestOnly, webPublish, false);
		if (acResult.isAccessible()) {
			startLink = createStartLink(layoutCont);
		} else if (!acResult.getAvailableMethods().isEmpty()) {
			formatOffers(acResult);
			createGoToOffersLink(layoutCont, guestOnly);
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
	
	private boolean initGuestLink(FormLayoutContainer layoutCont) {
		if (acService.isGuestAccessible(entry, true)) {
			String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath) + "?guest=true";
			ExternalLinkItem guestLink = uifactory.addExternalLink("start.guest", url, "_self", layoutCont);
			guestLink.setCssClass("btn btn-default");
			guestLink.setName(translate("start.guest"));
			return true;
		}
		return false;
	}
	
	private FormLink createLeaveLink(FormLayoutContainer layoutCont, boolean guestOnly, boolean isParticipant) {
		if (!guestOnly && isParticipant && repositoryService.isParticipantAllowedToLeave(entry)) {
			FormLink link = uifactory.addFormLink("leave", "leave", translate("sign.out"), null, layoutCont, Link.NONTRANSLATED);
			link.setElementCssClass("o_sign_out");
			link.setIconLeftCSS("o_icon o_icon_sign_out");
			link.setGhost(true);
			return link;
		}
		
		return null;
	}
	
	@Override
	protected String getStartLinkText() {
		return translate("start.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(leaveDialogBox == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doLeave(ureq);
				if (!closeTabOnLeave) {
					fireEvent(ureq, new LeavingEvent());
				}
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == leaveLink) {
			doConfirmLeave(ureq);
		}
		super.formInnerEvent(ureq, source, event);
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
