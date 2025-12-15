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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.catalog.ui.BookEvent;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;
import org.olat.resource.accesscontrol.ui.OffersController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractDetailsHeaderController extends BasicController {
	
	public static final Event START_EVENT = new Event("details.start");
	public static final Event START_ADMIN_EVENT = new Event("details.start.admin");
	public static final Event LEAVE_EVENT = new Event("details.leave");
	
	private VelocityContainer mainVC;

	protected final HeaderStartController startCtrl;
	protected OffersController offersCtrl;
	
	protected final DetailsHeaderConfig config;

	@Autowired
	protected RepositoryService repositoryService;
	@Autowired
	protected ACService acService;
	
	public AbstractDetailsHeaderController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null);
	}

	public AbstractDetailsHeaderController(UserRequest ureq, WindowControl wControl,
			DetailsHeaderConfig config) {
		super(ureq, wControl);
		this.config = config;
		
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
		mainVC.contextPut("translatedTechnicalType", getTranslatedTechnicalType());
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
		
		if (config != null) {
			initByConfig(ureq);
		} else {
			initAccess(ureq);
		}
	}
	
	protected abstract String getIconCssClass();
	protected abstract String getExternalRef();
	protected abstract String getTranslatedTechnicalType();
	protected abstract String getTitle();
	protected abstract String getAuthors();
	protected abstract String getTeaser();
	protected abstract VFSLeaf getTeaserImage();
	protected abstract VFSLeaf getTeaserMovie();
	protected abstract boolean hasTeaser();
	protected abstract RepositoryEntryEducationalType getEducationalType();
	protected abstract String getPendingMessageElementName();
	protected abstract String getLeaveText(boolean withFee);
	
	protected abstract boolean isPreview();
	protected abstract void initAccess(UserRequest ureq);
	protected abstract String getStartLinkText();
	protected abstract boolean tryAutoBooking(UserRequest ureq);
	protected abstract Long getResourceKey();
	


	private void initByConfig(UserRequest ureq) {
		// Start
		startCtrl.getStartLink().setVisible(config.isOpenAvailable());
		startCtrl.getStartLink().setEnabled(config.isOpenEnabled());
		
		if (config.isNoContentYetMessage() || config.isNotPublishedYetMessage()) {
			// Same message if one of this two reasons
			setWarning(translate("access.denied.preparation"), translate("access.denied.preparation.hint"));
		} else if (config.isConfirmationPendingMessage()) {
			setWarning(translate("access.denied.preparation.element", StringHelper.escapeHtml(getPendingMessageElementName())),
					translate("access.denied.preparation.hint"));
		}
		if (config.isOwnerCoachMessage()) {
			setWarning2(translate("access.available.roles"), translate("access.available.roles.hint"));
		}
		
		// Book
		if (!startCtrl.getStartLink().isVisible() && config.isBookAvailable()) {
			startCtrl.getStartLink().setVisible(config.isBookAvailable());
			startCtrl.getStartLink().setEnabled(config.isBookEnabled());
			startCtrl.getStartLink().setIconRightCSS(null);
			startCtrl.getStartLink().setCustomDisplayText(translate("book"));
		}
		
		if (config.isOffersAvailable()) {
			showOffers(ureq, config.getAvailableMethods(), false, config.isOffersWebPublish(), config.getBookedIdentity());
		}

		if (config.isAvailabilityMessage()) {
			ParticipantsAvailabilityNum availabilityNum = config.getParticipantsAvailabilityNum();
			if (availabilityNum.availability() == ParticipantsAvailability.fullyBooked) {
				startCtrl.setError(getAvailabilityText(availabilityNum));
			} else if (availabilityNum.availability() == ParticipantsAvailability.fewLeft) {
				if (offersCtrl != null) {
					offersCtrl.setWarning(getAvailabilityText(availabilityNum));
				}
			}
		}
		
		// Leave
		if (config.isLeaveAvailable()) {
			startCtrl.getLeaveLink().setVisible(true);
			startCtrl.getLeaveLink().setCustomDisplayText(getLeaveText(config.isLeaveWithCancellationFee()));
		}
		
		// Administrative access
		if (config.isAdministrativOpenAvailable()) {
			startCtrl.getStartAdminLink().setVisible(config.isAdministrativOpenAvailable());
			startCtrl.getStartAdminLink().setEnabled(config.isAdministrativOpenEnabled());
			
			if (offersCtrl != null) {
				offersCtrl.getStartAdminLink().setVisible(config.isAdministrativOpenAvailable());
				offersCtrl.getStartAdminLink().setEnabled(config.isAdministrativOpenEnabled());
				offersCtrl.getStartAdminLink().setCustomDisplayText(translate("start.admin"));
			}
		}
		
		startCtrl.getInitialComponent().setVisible(startCtrl.getStartLink().isVisible());
	}
	
	protected String getAvailabilityText(ParticipantsAvailabilityNum participantsAvailabilityNum) {
		return "<i class=\"o_icon " + ParticipantsAvailability.getIconCss(participantsAvailabilityNum) + "\"> </i> " 
				+ ParticipantsAvailability.getText(getTranslator(), participantsAvailabilityNum);
	}

	protected void showOffers(UserRequest ureq, List<OfferAccess> offers, boolean guestOnly, Boolean webPublish, Identity bookedIdentity) {
		if (guestOnly) {
			return;
		}
		
		offersCtrl = new OffersController(ureq, getWindowControl(), bookedIdentity, offers, webPublish, false, isPreview());
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
	
	protected void setWarning2(String warning, String warningHint) {
		mainVC.contextPut("warning2", warning);
		mainVC.contextPut("warning2Hint", warningHint);
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
			} else if (event == START_ADMIN_EVENT) {
				fireEvent(ureq, START_EVENT);
			}
		} else if (source == offersCtrl) {
			if (event == OffersController.START_ADMIN_EVENT) {
				fireEvent(ureq, START_EVENT);
			} else {
				fireEvent(ureq, event);
			}
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
