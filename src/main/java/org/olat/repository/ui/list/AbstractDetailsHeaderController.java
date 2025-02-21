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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.catalog.ui.BookEvent;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ui.OffersController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractDetailsHeaderController extends BasicController {
	
	public static final Event START_EVENT = new Event("start");
	public static final Event LEAVE_EVENT = new Event("leave");
	
	private VelocityContainer mainVC;

	protected final HeaderStartController startCtrl;
	protected OffersController offersCtrl;

	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected ACService acService;
	
	public AbstractDetailsHeaderController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setVelocityRoot(Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class));
		mainVC = createVelocityContainer("details_header");
		putInitialPanel(mainVC);
		
		startCtrl = new HeaderStartController(ureq, wControl);
		listenTo(startCtrl);
		startCtrl.getInitialComponent().setVisible(false);
		mainVC.put("start", startCtrl.getInitialComponent());
	}

	protected void init(UserRequest ureq) {
		startCtrl.getStartLink().setCustomDisplayText(getStartLinkText());
		
		mainVC.contextPut("iconCssClass", getIconCssClass());
		mainVC.contextPut("externalRef", getExternalRef());
		mainVC.contextPut("title", getTitle());
		mainVC.contextPut("authors", getAuthors());
		mainVC.contextPut("teaser", getTeaser());
		
		VFSLeaf image = getTeaserImage();
		VFSLeaf movie = getTeaserMovie();
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
			mainVC.put("thumbnail", ic);
		}
		
		if (getEducationalType() != null) {
			String educationalType = translate(RepositoyUIFactory.getI18nKey(getEducationalType()));
			mainVC.contextPut("educationalType", educationalType);
		}
		
		initAccess(ureq);
	}
	
	protected abstract String getIconCssClass();
	protected abstract String getExternalRef();
	protected abstract String getTitle();
	protected abstract String getAuthors();
	protected abstract String getTeaser();
	protected abstract VFSLeaf getTeaserImage();
	protected abstract VFSLeaf getTeaserMovie();
	protected abstract RepositoryEntryEducationalType getEducationalType();
	
	protected abstract void initAccess(UserRequest ureq);
	protected abstract String getStartLinkText();
	protected abstract boolean tryAutoBooking(UserRequest ureq);
	protected abstract Long getResourceKey();

	protected void showOffers(UserRequest ureq, List<OfferAccess> offers, boolean guestOnly, Boolean webPublish, Identity bookedIdentity) {
		if (guestOnly) {
			return;
		}
		
		offersCtrl = new OffersController(ureq, getWindowControl(), offers, false, webPublish, bookedIdentity);
		listenTo(offersCtrl);
		mainVC.put("offers", offersCtrl.getInitialComponent());
	}
	
	protected void showAccessDenied(Controller ctrl) {
		listenTo(ctrl);
		mainVC.put("access.refused", ctrl.getInitialComponent());
	}
	
	protected void setWarning(String warning, String warningHint) {
		mainVC.contextPut("warning", warning);
		mainVC.contextPut("warningHint", warningHint);
	}
	
	protected void showInfoMessage(String info) {
		mainVC.contextPut("info", info);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == startCtrl) {
			if (event == START_EVENT) {
				if (startCtrl.isAutoBooking()) {
					doAutoBooking(ureq);
				} else {
					fireEvent(ureq, START_EVENT);
				}
			}
		} else if (source == offersCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private void doAutoBooking(UserRequest ureq) {
		if (getIdentity() == null) {
			fireEvent(ureq, new BookEvent(getResourceKey()));
		} else {
			boolean success = tryAutoBooking(ureq);
			if (success) {
				fireEvent(ureq, START_EVENT);
			}
		}
	}

}
