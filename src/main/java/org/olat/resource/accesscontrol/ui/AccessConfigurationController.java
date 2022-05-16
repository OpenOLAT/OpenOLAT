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

package org.olat.resource.accesscontrol.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.Dropdown.SpacerItem;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Description:<br>
 *
 *
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AccessConfigurationController extends FormBasicController {
	
	private DropdownItem addMethodDropdown;
	private FormLink addOpenAccessLink;
	private FormLink addOpenAccessButton;
	private FormLink addGuestLink;
	private FormLink addGuestButton;
	private final List<FormLink> addOfferLinks = new ArrayList<>();
	
	private int counter = 0;
	private final String displayName;
	private final OLATResource resource;
	private Collection<Organisation> defaultOfferOrganisations;

	private CloseableModalController cmc;
	private FormLayoutContainer offersContainer;
	private OpenAccessOfferController openAccessOfferCtrl;
	private GuestOfferController guestOfferCtrl;
	private AbstractConfigurationMethodController newMethodCtrl;
	private AbstractConfigurationMethodController editMethodCtrl;

	private final List<Offer> deletedOfferList = new ArrayList<>();
	private final List<AccessInfo> accessInfos = new ArrayList<>();

	private boolean allowPaymentMethod;
	private RepositoryEntryStatusEnum reStatus;
	private final boolean openAccessSupported;
	private final boolean guestSupported;
	private final boolean readOnly;
	private final boolean managedBookings;
	private final String url;
	private final Formatter formatter;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;

	public AccessConfigurationController(UserRequest ureq, WindowControl wControl, OLATResource resource,
			String displayName, Collection<Organisation> defaultOfferOrganisations, boolean allowPaymentMethod,
			boolean openAccessSupported, boolean guestSupported, boolean readOnly, boolean managedBookings, String url) {
		super(ureq, wControl, "access_configuration");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.resource = resource;
		this.displayName = displayName;
		this.defaultOfferOrganisations = defaultOfferOrganisations;
		this.allowPaymentMethod = allowPaymentMethod;
		this.openAccessSupported = openAccessSupported;
		this.guestSupported = guestSupported;
		this.readOnly = readOnly;
		this.managedBookings = managedBookings;
		this.url = url;
		this.formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
	}

	public AccessConfigurationController(UserRequest ureq, WindowControl wControl, Form form, OLATResource resource,
			String displayName, Collection<Organisation> defaultOfferOrganisations, boolean allowPaymentMethod,
			boolean openAccessSupported, boolean guestSupported, boolean readOnly, boolean managedBookings, String url) {
		super(ureq, wControl, LAYOUT_CUSTOM, "access_configuration", form);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.resource = resource;
		this.displayName = displayName;
		this.defaultOfferOrganisations = defaultOfferOrganisations;
		this.allowPaymentMethod = allowPaymentMethod;
		this.openAccessSupported = openAccessSupported;
		this.guestSupported = guestSupported;
		this.readOnly = readOnly;
		this.managedBookings = managedBookings;
		this.url = url;
		this.formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
	}

	public void setDefaultOfferOrganisations(Collection<Organisation> defaultOfferOrganisations) {
		this.defaultOfferOrganisations = defaultOfferOrganisations;
	}

	public int getNumOfBookingConfigurations() {
		return accessInfos.size();
	}
	
	public List<OfferAccessWithOrganisation> getOfferAccess() {
		return accessInfos.stream()
				.filter(info -> info.getLink() != null)
				.collect(Collectors.toList());
	}
	
	public List<OfferWithOrganisation> getOpenAccessOffers() {
		return accessInfos.stream()
				.filter(info -> info.getOffer().isOpenAccess())
				.collect(Collectors.toList());
	}
	
	public Offer getGuestOffer() {
		return accessInfos.stream()
				.map(AccessInfo::getOffer)
				.filter(Offer::isGuestAccess)
				.findFirst().orElse(null);
	}

	public void setReStatus(RepositoryEntryStatusEnum reStatus) {
		this.reStatus = reStatus;
	}

	public void setAllowPaymentMethod(boolean allowPayment) {
		this.allowPaymentMethod = allowPayment;
	}

	public boolean isPaymentMethodInUse() {
		boolean paymentMethodInUse = false;
		for(AccessInfo info:accessInfos) {
			paymentMethodInUse |= info.isPaymentMethod();
		}
		return paymentMethodInUse;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("offers.title");
		
		String confPage = velocity_root + "/configuration_list.html";
		offersContainer = FormLayoutContainer.createCustomFormLayout("offers", getTranslator(), confPage);
		offersContainer.setRootForm(mainForm);
		formLayout.add(offersContainer);
		
		loadOffers();
		offersContainer.contextPut("offers", accessInfos);
		offersContainer.contextPut("extlink", url);
		
		EmptyState emptyState = EmptyStateFactory.create("empty", offersContainer.getFormItemComponent(), this);
		emptyState.setIconCss("o_icon o_icon_booking");
		emptyState.setMessageI18nKey("offers.empty.message");
		
		offersContainer.contextPut("addOfferLinks", addOfferLinks);
		
		if(!readOnly) {
			if (!managedBookings) {
				addMethodDropdown = uifactory.addDropdownMenu("create.offer", "create.offer", null, formLayout, getTranslator());
				addMethodDropdown.setOrientation(DropdownOrientation.normal);
				addMethodDropdown.setExpandContentHeight(true);
				
				List<AccessMethod> methods = acService.getAvailableMethods(getIdentity(), ureq.getUserSession().getRoles());
				for(AccessMethod method:methods) {
					AccessMethodHandler handler = acModule.getAccessMethodHandler(method.getType());
					if((handler.isPaymentMethod() && !allowPaymentMethod) || !method.isVisibleInGui()) {
						continue;
					}
					
					String title = handler.getMethodName(getLocale());
					FormLink addLink = uifactory.addFormLink("create." + handler.getType(), title, null, formLayout, Link.LINK | Link.NONTRANSLATED);
					addLink.setUserObject(method);
					addLink.setIconLeftCSS( ("o_icon " + method.getMethodCssClass() + "_icon o_icon-fw"));
					addMethodDropdown.addElement(addLink);
					formLayout.add(addLink.getName(), addLink);
					
					FormLink addButton = uifactory.addFormLink("create.b." + handler.getType(), title, null, offersContainer, Link.BUTTON | Link.NONTRANSLATED);
					addButton.setUserObject(method);
					addButton.setIconLeftCSS( ("o_icon " + method.getMethodCssClass() + "_icon o_icon-lg"));
					addOfferLinks.add(addButton);
				}
			}
			
			if (!managedBookings && (openAccessSupported || guestSupported)) {
				addMethodDropdown.addElement(new SpacerItem("spacer"));
			}
			
			addOpenAccessLink = uifactory.addFormLink("create.offer.open.link", "create.offer.open", null, formLayout, Link.LINK);
			addOpenAccessLink.setVisible(openAccessSupported);
			addMethodDropdown.addElement(addOpenAccessLink);
			formLayout.add(addOpenAccessLink.getName(), addOpenAccessLink);
			
			addOpenAccessButton = uifactory.addFormLink("create.offer.open", offersContainer, Link.BUTTON);
			addOpenAccessButton.setVisible(openAccessSupported);
			addOfferLinks.add(addOpenAccessButton);
			
			boolean addGuest = isAddGuest();
			addGuestLink = uifactory.addFormLink("create.offer.guest.link", "create.offer.guest", null, formLayout, Link.LINK);
			addGuestLink.setVisible(addGuest);
			addMethodDropdown.addElement(addGuestLink);
			formLayout.add(addGuestLink.getName(), addGuestLink);
			
			addGuestButton = uifactory.addFormLink("create.offer.guest", offersContainer, Link.BUTTON);
			addGuestButton.setVisible(addGuest);
			addOfferLinks.add(addGuestButton);
			
			updateAddUI();
		}
	}
	
	private void updateAddUI() {
		boolean guestVisible = isAddGuest();
		if (addGuestLink != null) {
			addGuestLink.setVisible(guestVisible);
		}
		if (addGuestButton != null) {
			addGuestButton.setVisible(guestVisible);
		}
	}

	protected boolean isAddGuest() {
		boolean guestVisible = guestSupported;
		if (guestVisible) {
			for (AccessInfo accessInfo : accessInfos) {
				if (accessInfo.getOffer().isGuestAccess()) {
					guestVisible = false;
					break;
				}
			}
		}
		return guestVisible;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(openAccessOfferCtrl == source) {
			if(event.equals(Event.DONE_EVENT)) {
				Offer offer = openAccessOfferCtrl.getOffer();
				List<Organisation> organisations = openAccessOfferCtrl.getOfferOrganisations();
				addOpenAccessOffer(offer, organisations);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(guestOfferCtrl == source) {
			if(event.equals(Event.DONE_EVENT)) {
				Offer offer = guestOfferCtrl.getOffer();
				addGuestOffer(offer);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(newMethodCtrl == source) {
			if(event.equals(Event.DONE_EVENT)) {
				OfferAccess offerAccess = newMethodCtrl.getOfferAccess();
				List<Organisation> organisations = newMethodCtrl.getOfferOrganisations();
				addOffer(offerAccess, organisations);
				checkOverlap();
				offersContainer.setDirty(true);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editMethodCtrl == source) {
			if(event.equals(Event.DONE_EVENT)) {
				OfferAccess newLink = editMethodCtrl.getOfferAccess();
				List<Organisation> organisations = editMethodCtrl.getOfferOrganisations();
				replace(newLink, organisations);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else {
			super.event(ureq, source, event);
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(openAccessOfferCtrl);
		removeAsListenerAndDispose(guestOfferCtrl);
		removeAsListenerAndDispose(editMethodCtrl);
		removeAsListenerAndDispose(newMethodCtrl);
		removeAsListenerAndDispose(cmc);
		openAccessOfferCtrl = null;
		guestOfferCtrl = null;
		editMethodCtrl = null;
		newMethodCtrl = null;
		cmc = null;
	}
	

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addOpenAccessLink || source == addOpenAccessButton) {
			editOpenAccessOffer(ureq, null);
		} else if (source == addGuestLink || source == addGuestButton) {
			editGuestOffer(ureq, null);
		} else if (source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if("delete".equals(cmd)) {
				AccessInfo infos = (AccessInfo)source.getUserObject();
				removeOffer(infos);
				checkOverlap();
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if("edit".equals(cmd)) {
				AccessInfo infos = (AccessInfo)source.getUserObject();
				editOffer(ureq, infos);
			} else if (cmd.startsWith("create.")) {
				AccessMethod method = (AccessMethod)source.getUserObject();
				addOffer(ureq, method);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void formOK(UserRequest ureq) {
		//
	}

	private void loadOffers() {
		List<Offer> offers = acService.findOfferByResource(resource, true, null, null);
		Map<Long,List<Organisation>> offerKeyToOrganisations = acService.getOfferKeyToOrganisations(offers);
		for(Offer offer:offers) {
			Collection<Organisation> offerOrganisations = offerKeyToOrganisations.getOrDefault(offer.getKey(), null);
			if (offer.isOpenAccess()) {
				addOpenAccessOffer(offer, offerOrganisations);
			} else if (offer.isGuestAccess()) {
				addGuestOffer(offer);
			} else {
				List<OfferAccess> offerAccess = acService.getOfferAccess(offer, true);
				for(OfferAccess access:offerAccess) {
					addOffer(access, offerOrganisations);
				}
			}
		}
		
		checkOverlap();
	}

	private void replace(OfferAccess link, Collection<Organisation> offerOrganisations) {
		boolean updated = false;
		for(AccessInfo accessInfo : accessInfos) {
			if(accessInfo.getLink() != null && accessInfo.getLink().equals(link)) {
				accessInfo.setLink(link);
				accessInfo.setOfferOrganisations(offerOrganisations);
				updated = true;
			}
		}
		
		if(!updated) {
			addOffer(link, offerOrganisations);
		} else {
			offersContainer.setDirty(true);
		}
		checkOverlap();
	}

	private void addOffer(OfferAccess link, Collection<Organisation> offerOrganisations) {
		AccessMethodHandler handler = acModule.getAccessMethodHandler(link.getMethod().getType());
		AccessInfo infos = new AccessInfo(handler.getMethodName(getLocale()), link, handler);
		infos.setOfferOrganisations(offerOrganisations);
		accessInfos.add(infos);
		
		if (!readOnly && !managedBookings) {
			forgeLinks(infos);
		}
		
		offersContainer.setDirty(true);
	}
	
	private void addOpenAccessOffer(Offer offer, Collection<Organisation> offerOrganisations) {
		boolean updated = false;
		for(AccessInfo accessInfo : accessInfos) {
			if (accessInfo.getOffer().equals(offer)) {
				accessInfo.setOffer(offer);
				accessInfo.setOfferOrganisations(offerOrganisations);
				updated = true;
			}
		}
		
		if(!updated) {
			AccessInfo infos = new AccessInfo();
			infos.setOffer(offer);
			infos.setOfferOrganisations(offerOrganisations);
			infos.setName(translate("offer.open.access.name"));
			accessInfos.add(infos);
			if (!readOnly) {
				forgeLinks(infos);
			}
		}
		
		offersContainer.setDirty(true);
		updateAddUI();
	}
	
	private void addGuestOffer(Offer offer) {
		for(AccessInfo accessInfo : accessInfos) {
			if (accessInfo.getOffer().equals(offer)) {
				return;
			}
		}
		
		AccessInfo infos = new AccessInfo();
		infos.setOffer(offer);
		infos.setName(translate("offer.guest.name"));
		infos.setIconCss("o_ac_guest");
		accessInfos.add(infos);
		
		if (!readOnly) {
			forgeLinks(infos);
		}
		
		offersContainer.setDirty(true);
		updateAddUI();
	}

	protected void forgeLinks(AccessInfo infos) {
		FormLink editLink = uifactory.addFormLink("edit_" + (++counter), "edit", "offer.edit", null, offersContainer, Link.LINK);
		editLink.setUserObject(infos);
		editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		offersContainer.add(editLink.getName(), editLink);
		infos.setEditButton(editLink);

		FormLink delLink = uifactory.addFormLink("del_" + (++counter), "delete", "offer.delete", null, offersContainer, Link.LINK);
		delLink.setUserObject(infos);
		delLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		offersContainer.add(delLink.getName(), delLink);
		infos.setDeleteButton(delLink);
	}

	private void editOpenAccessOffer(UserRequest ureq, Offer offer) {
		Offer openAccessOffer = offer;
		Collection<Organisation> offerOrganisations = null;
		if (openAccessOffer == null) {
			openAccessOffer = acService.createOffer(resource, displayName);
			openAccessOffer.setOpenAccess(true);
			offerOrganisations = defaultOfferOrganisations;
		} else {
			offerOrganisations = acService.getOfferOrganisations(offer);
		}
		
		removeAsListenerAndDispose(openAccessOfferCtrl);
		openAccessOfferCtrl = new OpenAccessOfferController(ureq, getWindowControl(), openAccessOffer, offerOrganisations, offer != null);
		listenTo(openAccessOfferCtrl);
		String title = translate("offer.open.access.name");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), openAccessOfferCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}

	private void editGuestOffer(UserRequest ureq, Offer offer) {
		Offer guestOffer = offer;
		if (guestOffer == null) {
			guestOffer = acService.createOffer(resource, displayName);
			guestOffer.setGuestAccess(true);
		}
		
		removeAsListenerAndDispose(guestOfferCtrl);
		guestOfferCtrl = new GuestOfferController(ureq, getWindowControl(), guestOffer, offer != null);
		listenTo(guestOfferCtrl);
		String title = translate("offer.guest.name");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), guestOfferCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}

	private void editOffer(UserRequest ureq, AccessInfo infos) {
		if (infos.getOffer().isOpenAccess()) {
			editOpenAccessOffer(ureq, infos.getOffer());
			return;
		} else if (infos.getOffer().isGuestAccess()) {
			editGuestOffer(ureq, infos.getOffer());
			return;
		}
		
		OfferAccess link = infos.getLink();
		
		removeAsListenerAndDispose(editMethodCtrl);
		AccessMethodHandler handler = acModule.getAccessMethodHandler(link.getMethod().getType());
		if (handler != null) {
			Collection<Organisation> offerOrganisations = acService.getOfferOrganisations(link.getOffer());
			editMethodCtrl = handler.editConfigurationController(ureq, getWindowControl(), link, offerOrganisations);
			if(editMethodCtrl != null) {
				listenTo(editMethodCtrl);
	
				String title = handler.getMethodName(getLocale());
				cmc = new CloseableModalController(getWindowControl(), translate("close"), editMethodCtrl.getInitialComponent(), true, title);
				cmc.activate();
				listenTo(cmc);
			}
		}
	}
	
	private void addOffer(UserRequest ureq, AccessMethod method) {
		Offer offer = acService.createOffer(resource, displayName);
		OfferAccess link = acService.createOfferAccess(offer, method);
		
		removeAsListenerAndDispose(newMethodCtrl);
		AccessMethodHandler handler = acModule.getAccessMethodHandler(link.getMethod().getType());
		if (handler != null) {
			newMethodCtrl = handler.createConfigurationController(ureq, getWindowControl(), link, defaultOfferOrganisations);
		}
		if(newMethodCtrl != null && handler != null) {
			listenTo(newMethodCtrl);

			String title = handler.getMethodName(getLocale());
			cmc = new CloseableModalController(getWindowControl(), translate("close"), newMethodCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		} else {
			addOffer(link, defaultOfferOrganisations);
			checkOverlap();
		}
	}
	
	private void removeOffer(AccessInfo infos) {
		Offer offer = infos.getOffer();
		if (offer.getKey() != null) {
			deletedOfferList.add(offer);
		}
		accessInfos.remove(infos);
		updateAddUI();
	}

	public void commitChanges() {
		for(AccessInfo info:accessInfos) {
			if (info.getOffer().isGuestAccess()) {
				acService.save(info.getOffer());
			} else if (info.getOffer().isOpenAccess()) {
				acService.save(info.getOffer());
				acService.updateOfferOrganisations(info.getOffer(), info.offerOrganisations);
			} else {
				OfferAccess link = info.getLink();
				if (link != null) {
					acService.saveOfferAccess(link);
					acService.updateOfferOrganisations(info.getOffer(), info.offerOrganisations);
				}
			}
		}
		accessInfos.clear();
		
		for(Offer offerToDelete:deletedOfferList) {
			acService.deleteOffer(offerToDelete);
		}
		deletedOfferList.clear();
		
		dbInstance.commit();
		loadOffers();
	}
	
	private void checkOverlap() {
		boolean overlap = false;
		boolean overlapAllowed = true;

		// Take a controller from the list
		for (AccessInfo confControllerA : accessInfos) {
			if (confControllerA.getLink() == null) continue;
			// Compare it to every other from the list
			for (AccessInfo confControllerB : accessInfos) {
				if (confControllerB.getLink() == null) continue;
				// Don't compare a confController with itself
				if (!confControllerA.equals(confControllerB)) {
					Date aFrom = confControllerA.getLink().getValidFrom();
					Date aTo = confControllerA.getLink().getValidTo();
					Date bFrom = confControllerB.getLink().getValidFrom();
					Date bTo = confControllerB.getLink().getValidTo();

					// One unlimited booking method and another
					if (aFrom == null && aTo == null) {
						overlap |= true;
						overlapAllowed &= confControllerA.isOverlapAllowed(confControllerB);
					} 
					// Start and end overlap
					else if (aTo != null && bFrom != null && aTo.compareTo(bFrom) >= 0){
						// Exclude not overlapping methods
						// Negate condition for no overlap => condition for overlap
						if (!(aFrom != null && bTo != null && aFrom.compareTo(bTo) > 0)) {
							overlap |= true; 
						}
					} 
					// Two booking methods without start date
					else if (aFrom == null && bFrom == null) {
						overlap |= true;
						overlapAllowed &= confControllerA.isOverlapAllowed(confControllerB);
					} 
					// Two booking methods without end date
					else if (aTo == null && bTo == null) {
						overlap |= true;
						overlapAllowed &= confControllerA.isOverlapAllowed(confControllerB);
					}
				}
			}
			// If there is an overlap, don't go for extra checks
			if (overlap) {
				break;
			}
		}
		
		// Display a warning
		offersContainer.contextPut("overlappingConfigs", overlap);
		offersContainer.contextPut("overlappingErrorConfigs", !overlapAllowed);
		offersContainer.setDirty(true);
	}
	
	public interface OfferWithOrganisation {
		
		public Offer getOffer();
		
		public Collection<Organisation> getOfferOrganisations();
		
	}
	
	public interface OfferAccessWithOrganisation {
		
		public OfferAccess getLink();
		
		public Collection<Organisation> getOfferOrganisations();
		
	}

	public class AccessInfo implements OfferWithOrganisation, OfferAccessWithOrganisation {
		
		private static final String ICON_ACTIVE = "<i class=\"o_icon o_icon-fw o_icon_offer_active\"> </i> ";
		private static final String ICON_INACTIVE = "<i class=\"o_icon o_icon-fw o_icon_offer_inactive\"> </i> ";
		
		private String name;
		private String iconCss;
		private Offer offer;
		private Collection<Organisation> offerOrganisations;
		private OfferAccess link;
		private AccessMethodHandler handler;
		
		private FormLink editButton;
		private FormLink deleteButton;
		
		public AccessInfo() {
		}

		public AccessInfo(String name, OfferAccess link, AccessMethodHandler handler) {
			this.name = name;
			this.iconCss = link != null? link.getMethod().getMethodCssClass(): null;
			this.offer = link != null? link.getOffer(): null;
			this.link = link;
			this.handler = handler;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getIconCss() {
			return iconCss;
		}

		public void setIconCss(String iconCss) {
			this.iconCss = iconCss;
		}

		public boolean isPaymentMethod() {
			return handler != null? handler.isPaymentMethod(): false;
		}
		
		public boolean isOverlapAllowed(AccessInfo info) {
			return handler != null? handler.isOverlapAllowed(info.handler): false;
		}

		public String getDates() {
			if(offer != null) {
				if (reStatus != null) {
					if (offer.isGuestAccess()) {
						return RepositoryEntryStatusEnum.isInArray(reStatus, ACService.RESTATUS_ACTIVE_GUEST)
								? ICON_ACTIVE + translate("access.status.active.status", translate("cif.status." + reStatus.name()))
								: ICON_INACTIVE + translate("access.status.inactive", translate("cif.status." + reStatus.name()));
					} else if (offer.isOpenAccess()) {
						return RepositoryEntryStatusEnum.isInArray(reStatus, ACService.RESTATUS_ACTIVE_OPEN)
								? ICON_ACTIVE + translate("access.status.active.status", translate("cif.status." + reStatus.name()))
								: ICON_INACTIVE + translate("access.status.inactive", translate("cif.status." + reStatus.name()));
					} else {
						Date from = offer.getValidFrom();
						Date to = offer.getValidTo();
						if (from == null && to == null) {
							return RepositoryEntryStatusEnum.isInArray(reStatus, ACService.RESTATUS_ACTIVE_METHOD)
									? ICON_ACTIVE + translate("access.status.active.status", translate("cif.status." + reStatus.name()))
									: ICON_INACTIVE + translate("access.status.inactive", translate("cif.status." + reStatus.name()));
						} else if(from != null && to != null) {
							return RepositoryEntryStatusEnum.isInArray(reStatus, ACService.RESTATUS_ACTIVE_METHOD_PERIOD)
									? ICON_ACTIVE + translate("access.status.active.from.to", formatter.formatDate(to), formatter.formatDate(from))
									: ICON_INACTIVE + translate("access.status.inactive", translate("cif.status." + reStatus.name()));
						} else if(from != null) {
							return RepositoryEntryStatusEnum.isInArray(reStatus, ACService.RESTATUS_ACTIVE_METHOD_PERIOD)
									? ICON_ACTIVE + translate("access.status.active.from", formatter.formatDate(from), translate("cif.status.closed"))
									: ICON_INACTIVE + translate("access.status.inactive", translate("cif.status." + reStatus.name()));
						} else if(to != null) {
							return RepositoryEntryStatusEnum.isInArray(reStatus, ACService.RESTATUS_ACTIVE_METHOD_PERIOD)
									? ICON_ACTIVE + translate("access.status.active.to", translate("cif.status." + reStatus.name()), formatter.formatDate(from))
									: ICON_INACTIVE + translate("access.status.inactive", translate("cif.status." + reStatus.name()));
						}
					}
				} else {
					Date from = offer.getValidFrom();
					Date to = offer.getValidTo();
					if (from == null && to == null) {
						return ICON_ACTIVE + translate("access.active");
					} else if(from != null && to != null) {
						return ICON_ACTIVE + translate("access.active.from.to", formatter.formatDate(to), formatter.formatDate(from));
					} else if(from != null) {
						return  ICON_ACTIVE + translate("access.active.from", formatter.formatDate(from));
					} else if(to != null) {
						return ICON_ACTIVE + translate("access.active.to", formatter.formatDate(to));
					}
				}
			}
			return null;
		}
		
		public String getDescriptionInfo() {
			if (offer != null && offer.getDescription() != null) {
				return Formatter.truncate(StringHelper.stripLineBreaks(offer.getDescription()), 100);
			}
			return null;
		}
		
		public String getPrice() {
			if (offer != null &&  !offer.getPrice().isEmpty()) {
				String price = PriceFormat.fullFormat(link.getOffer().getPrice());
				if(acModule.isVatEnabled()) {
					BigDecimal vat = acModule.getVat();
					String vatStr = vat == null ? "" : vat.setScale(3, RoundingMode.HALF_EVEN).toPlainString();
					return translate("access.info.price.vat", new String[]{price, vatStr});
				}
				return translate("access.info.price.noVat", new String[]{price});
			}
			return null;
		}
		
		@Override
		public Offer getOffer() {
			return offer;
		}

		public void setOffer(Offer offer) {
			this.offer = offer;
		}

		@Override
		public Collection<Organisation> getOfferOrganisations() {
			return offerOrganisations;
		}
		
		public void setOfferOrganisations(Collection<Organisation> offerOrganisations) {
			this.offerOrganisations = offerOrganisations;
		}
		
		public String getOrganisationInfo() {
			if (offerOrganisations == null || offerOrganisations.isEmpty()) return null;
			
			return offerOrganisations.stream()
					.sorted(new OrganisationNameComparator(getLocale()))
					.map(Organisation::getDisplayName)
					.collect(Collectors.joining(", "));
		}
		
		@Override
		public OfferAccess getLink() {
			return link;
		}

		public void setLink(OfferAccess link) {
			this.link = link;
			this.offer = link != null? link.getOffer(): null;
			this.iconCss = link != null? link.getMethod().getMethodCssClass(): null;
		}

		public FormLink getEditButton() {
			return editButton;
		}

		public void setEditButton(FormLink editButton) {
			this.editButton = editButton;
		}

		public FormLink getDeleteButton() {
			return deleteButton;
		}

		public void setDeleteButton(FormLink deleteButton) {
			this.deleteButton = deleteButton;
		}
	}
}