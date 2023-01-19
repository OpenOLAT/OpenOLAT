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

import org.olat.basesecurity.AuthHelper;
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
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.accesscontrol.Offer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RepositoryEntryResourceInfoDetailsHeaderController extends FormBasicController {

	public static final Event START_EVENT = new Event("start");
	private final RepositoryEntry entry;
	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected OAIPmhModule oaiPmhModule;
	private List<PriceMethod> types = new ArrayList<>(1);

	public RepositoryEntryResourceInfoDetailsHeaderController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class) + "/details_header.html");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.entry = entry;
		initForm(ureq);
	}

	public List<PriceMethod> getTypes() {
		return types;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (formLayout instanceof FormLayoutContainer formLayoutContainer) {
			FormLayoutContainer layoutCont = formLayoutContainer;

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

			boolean isGuestAccess = oaiPmhModule.getOffers(entry.getOlatResource()).stream().anyMatch(Offer::isGuestAccess);

			if (!ureq.getUserSession().getRoles().isGuestOnly() || isGuestAccess) {
				createStartLink(layoutCont);
			} else {
				createLoginLink(layoutCont);
			}
		}
	}

	private FormLink createStartLink(FormLayoutContainer layoutCont) {
		String linkText = translate("start.with.type", translate(entry.getOlatResource().getResourceableTypeName()));
		FormLink link = uifactory.addFormLink("start", START_EVENT.getCommand(), linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
		link.setIconRightCSS("o_icon o_icon_start o_icon-lg");
		link.setPrimary(true);
		link.setFocus(true);
		link.setElementCssClass("o_start btn-block");
		return link;
	}

	private FormLink createLoginLink(FormLayoutContainer layoutCont) {
		String linkText = "Zum Login";
		FormLink link = uifactory.addFormLink("start", "login", linkText, null, layoutCont, Link.BUTTON + Link.NONTRANSLATED);
		link.setPrimary(true);
		link.setElementCssClass("o_start btn-block");
		return link;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink formLinkSource) {
			FormLink link = formLinkSource;
			String cmd = link.getCmd();
			if ("start".equals(cmd)) {
				fireEvent(ureq, START_EVENT);
			} else if ("login".equals(cmd)) {
				AuthHelper.doLogout(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
