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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessResult;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.ui.AccessRefusedController;
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

	private final RepositoryEntry entry;
	private final boolean isMember;
	private final RepositoryEntrySecurity reSecurity;
	private final boolean guestOnly;
	private final boolean inviteeOnly;
	private final boolean showStart;
	private List<PriceMethod> types = new ArrayList<>(1);
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	protected ACService acService;

	public RepositoryEntryDetailsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean isMember, boolean showStart) {
		super(ureq, wControl, Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class) + "/details_header.html");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.isMember = isMember;
		this.showStart = showStart;
		reSecurity = repositoryManager.isAllowed(ureq, entry);
		Roles roles = ureq.getUserSession().getRoles();
		guestOnly = roles.isGuestOnly();
		inviteeOnly = roles.isInviteeOnly();
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
			
			if (reSecurity.isEntryAdmin() || reSecurity.isPrincipal() || reSecurity.isMasterCoach()) {
				startLink = createStartLink(layoutCont);
			} else {
				if (reSecurity.canLaunch()) {
					startLink = createStartLink(layoutCont);
				} else if(inviteeOnly) {
					accessRefused(ureq);
				} else if (!isMember && entry.isPublicVisible()) {
					AccessResult acResult = acService.isAccessible(entry, getIdentity(), isMember, guestOnly, false);
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
						startLink.setElementCssClass("o_book btn-block");
						startLink.setVisible(!guestOnly);
					}
				} else {
					accessRefused(ureq);
				}
			}
			
			if (startLink != null) {
				startLink.setIconRightCSS("o_icon o_icon_start o_icon-lg");
				startLink.setPrimary(true);
				startLink.setFocus(true);
				startLink.setVisible(showStart);
			}
		}
	}
	
	private FormLink createStartLink(FormLayoutContainer layoutCont) {
		String linkText = translate("start.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
		FormLink link = uifactory.addFormLink("start", "start", linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
		link.setElementCssClass("o_start btn-block");
		return link;
	}
	
	private void accessRefused(UserRequest ureq) {
		Controller ctrl = new AccessRefusedController(ureq, getWindowControl(), entry, false);
		listenTo(ctrl);
		flc.put("access.refused", ctrl.getInitialComponent());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
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

}
