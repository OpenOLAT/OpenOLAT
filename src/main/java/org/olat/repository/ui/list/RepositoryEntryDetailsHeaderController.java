/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageComponent;
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
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroupService;
import org.olat.repository.LeavingStatusList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.AccessDeniedFactory;
import org.olat.repository.ui.AccessDeniedFactory.AccessDeniedMessage;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryDetailsHeaderController extends FormBasicController {
	
	public static final Event START_EVENT = new Event("start");
	public static final Event BOOK_EVENT = new Event("book");
	
	private FormLink startLink;
	private FormLink leaveLink;
	
	private DialogBoxController leaveDialogBox;

	private final RepositoryEntry entry;
	private final boolean isMember;
	private final boolean showStart;
	private final boolean closeTabOnLeave;
	private List<PriceMethod> types = new ArrayList<>(1);
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	protected ACService acService;

	public RepositoryEntryDetailsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			boolean isMember, boolean showStart, boolean closeTabOnLeave) {
		super(ureq, wControl, Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class) + "/details_header.html");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.isMember = isMember;
		this.showStart = showStart;
		this.closeTabOnLeave = closeTabOnLeave;
		
		initForm(ureq);
	}

	public List<PriceMethod> getTypes() {
		return types;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			layoutCont.contextPut("v", entry);
			
			// thumbnail and movie
			VFSLeaf movie = repositoryService.getIntroductionMovie(entry);
			VFSLeaf image = repositoryService.getIntroductionImage(entry);
			if (image != null || movie != null) {
				ImageComponent ic = new ImageComponent(ureq.getUserSession(), "thumbnail");
				if (movie != null) {
					ic.setMedia(movie);
					ic.setMaxWithAndHeightToFitWithin(RepositoryManager.PICTURE_WIDTH, RepositoryManager.PICTURE_HEIGHT);
					if (image != null) {
						ic.setPoster(image);
					}
				} else {
					ic.setMedia(image);
					ic.setMaxWithAndHeightToFitWithin(RepositoryManager.PICTURE_WIDTH, RepositoryManager.PICTURE_HEIGHT);
				}
				layoutCont.put("thumbnail", ic);
			}
			
			String cssClass = RepositoyUIFactory.getIconCssClass(entry);
			layoutCont.contextPut("cssClass", cssClass);
			
			if (entry.getEducationalType() != null) {
				String educationalType = translate(RepositoyUIFactory.getI18nKey(entry.getEducationalType()));
				layoutCont.contextPut("educationalType", educationalType);
			}
			
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
						showAccessDenied(AccessDeniedFactory.createBookingPending(ureq, getWindowControl()));
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
			
			if (startLink != null) {
				startLink.setIconRightCSS("o_icon o_icon_start o_icon-lg");
				startLink.setPrimary(true);
				startLink.setFocus(true);
				startLink.setVisible(showStart);
				startLink.setElementCssClass(startLink.getElementCssClass() + " o_button_call_to_action");
			}
		}
	}

	private void initOffers(UserRequest ureq, FormLayoutContainer layoutCont, boolean guestOnly, Boolean webPublish) {
		if (webPublish != null && webPublish.booleanValue()) {
			if (acService.isGuestAccessible(entry, true)) {
				String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
				String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath) + "?guest=true";
				ExternalLinkItem guestLink = uifactory.addExternalLink("start.guest", url, "_self", layoutCont);
				guestLink.setCssClass("btn btn-default");
				guestLink.setName(translate("start.guest"));
				return;
			}
		}
		
		AccessResult acResult = acService.isAccessible(entry, getIdentity(), isMember, guestOnly, webPublish, false);
		if (acResult.isAccessible()) {
			startLink = createStartLink(layoutCont);
		} else if (!acResult.getAvailableMethods().isEmpty()) {
			for(OfferAccess access:acResult.getAvailableMethods()) {
				AccessMethod method = access.getMethod();
				String type = (method.getMethodCssClass() + "_icon").intern();
				Price p = access.getOffer().getPrice();
				String price = p == null || p.isEmpty() ? "" : PriceFormat.fullFormat(p);
				AccessMethodHandler amh = acModule.getAccessMethodHandler(method.getType());
				String displayName = amh.getMethodName(getLocale());
				types.add(new PriceMethod(price, type, displayName));
			}
			String linkText = translate("book.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
			startLink = uifactory.addFormLink("start", "book", linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
			startLink.setCustomEnabledLinkCSS("btn btn-success"); // custom style
			startLink.setElementCssClass("o_book");
			startLink.setVisible(!guestOnly);
		} else if (!getOffersNowNotInRange(entry, getIdentity()).isEmpty()) {
			showAccessDenied(AccessDeniedFactory.createOfferNotNow(ureq, getWindowControl(), getOffersNowNotInRange(entry, getIdentity())));
		} else {
			showAccessDenied(AccessDeniedFactory.createNoAccess(ureq, getWindowControl()));
		}
	}
	
	private FormLink createStartLink(FormLayoutContainer layoutCont) {
		String linkText = translate("start.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
		FormLink link = uifactory.addFormLink("start", "start", linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
		link.setElementCssClass("o_start");
		return link;
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
	
	private void showAccessDenied(Controller ctrl) {	
		listenTo(ctrl);
		flc.put("access.refused", ctrl.getInitialComponent());
	}
	
	private List<Offer> getOffersNowNotInRange(RepositoryEntry re, Identity identity) {
		List<? extends OrganisationRef> offerOrganisations = CoreSpringFactory.getImpl(ACService.class).getOfferOrganisations(identity);
		return CoreSpringFactory.getImpl(ACService.class).getOffers(re, true, false, null, true, null, offerOrganisations);
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
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if ("start".equals(cmd)) {
				fireEvent(ureq, START_EVENT);
			} else if ("book".equals(cmd)) {
				fireEvent(ureq, BOOK_EVENT);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
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
