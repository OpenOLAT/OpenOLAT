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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.OrganisationModule;
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
import org.olat.core.gui.components.panel.IconPanelItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.Roles;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.ui.CatalogBCFactory;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.author.AuthoringEditAccessShareController.ExtLink;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.CatalogInfo;
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
	
	private static final String ICON_CATALOG_EXTERN = "<i class=\"o_icon o_icon-fw o_icon_catalog_extern\"> </i> ";
	private static final String ICON_CATALOG_INTERN = "<i class=\"o_icon o_icon-fw o_icon_catalog_intern\"> </i> ";
	
	private FormLink addButton;
	private DropdownItem addMethodDropdown;
	private FormLink addOpenAccessLink;
	private FormLink addOpenAccessButton;
	private FormLink addGuestLink;
	private FormLink addGuestButton;
	private final List<FormLink> addOfferLinks = new ArrayList<>();

	private CloseableModalController cmc;
	private FormLayoutContainer overviewContainer;
	private FormLayoutContainer offersContainer;
	private OpenAccessOfferController openAccessOfferCtrl;
	private GuestOfferController guestOfferCtrl;
	private AbstractConfigurationMethodController newMethodCtrl;
	private AbstractConfigurationMethodController editMethodCtrl;
	private MethodSelectionController methodSelectionCtrl;

	private final List<Offer> deletedOfferList = new ArrayList<>(1);
	private final List<AccessInfo> accessInfos = new ArrayList<>(3);
	
	private int counter = 0;
	private final String displayName;
	private final OLATResource resource;
	private String notAvailableStatus;
	private boolean allowPaymentMethod;
	private final boolean openAccessSupported;
	private final boolean guestSupported;
	private final boolean offerOrganisationsSupported;
	private Collection<Organisation> defaultOfferOrganisations;
	private final CatalogInfo catalogInfo;
	private final boolean readOnly;
	private final boolean managedBookings;
	private final String helpUrl;
	private final Formatter formatter;
	private List<AccessMethod> methods;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private OrganisationModule organisationModule;

	public AccessConfigurationController(UserRequest ureq, WindowControl wControl, OLATResource resource,
			String displayName, boolean allowPaymentMethod, boolean openAccessSupported, boolean guestSupported,
			boolean offerOrganisationsSupported, Collection<Organisation> defaultOfferOrganisations,
			CatalogInfo catalogInfo, boolean readOnly, boolean managedBookings, String helpUrl) {
		super(ureq, wControl, "access_configuration");
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.resource = resource;
		this.displayName = displayName;
		this.allowPaymentMethod = allowPaymentMethod;
		this.openAccessSupported = openAccessSupported;
		this.guestSupported = guestSupported;
		this.offerOrganisationsSupported = offerOrganisationsSupported;
		this.defaultOfferOrganisations = defaultOfferOrganisations;
		this.catalogInfo = catalogInfo;
		this.notAvailableStatus = catalogInfo.getNotAvailableStatus();
		this.readOnly = readOnly;
		this.managedBookings = managedBookings;
		this.helpUrl = helpUrl;
		this.formatter = Formatter.getInstance(getLocale());
		
		initMethods(ureq.getUserSession().getRoles());
		initForm(ureq);
	}

	public AccessConfigurationController(UserRequest ureq, WindowControl wControl, Form form, OLATResource resource,
			String displayName, boolean allowPaymentMethod, boolean openAccessSupported, boolean guestSupported,
			boolean offerOrganisationsSupported, Collection<Organisation> defaultOfferOrganisations,
			CatalogInfo catalogInfo, boolean readOnly, boolean managedBookings, String helpUrl) {
		super(ureq, wControl, LAYOUT_CUSTOM, "access_configuration", form);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.resource = resource;
		this.displayName = displayName;
		this.allowPaymentMethod = allowPaymentMethod;
		this.openAccessSupported = openAccessSupported;
		this.guestSupported = guestSupported;
		this.offerOrganisationsSupported = offerOrganisationsSupported;
		this.defaultOfferOrganisations = defaultOfferOrganisations;
		this.catalogInfo = catalogInfo;
		this.readOnly = readOnly;
		this.managedBookings = managedBookings;
		this.helpUrl = helpUrl;
		this.formatter = Formatter.getInstance(getLocale());
		
		initMethods(ureq.getUserSession().getRoles());
		initForm(ureq);
	}
	
	private void initMethods(Roles roles) {
		methods = acService.getAvailableMethods(resource, getIdentity(), roles).stream()
				.filter(this::isAddable)
				.collect(Collectors.toList());
	}
	
	private boolean isAddable(AccessMethod method) {
		AccessMethodHandler handler = acModule.getAccessMethodHandler(method.getType());
		if ((handler.isPaymentMethod() && !allowPaymentMethod) || !method.isVisibleInGui()) {
			return false;
		}
		return true;
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
	
	public List<Offer> getDeletedOffers() {
		return deletedOfferList;
	}

	public void setNotAvailableStatus(String notAvailableStatus) {
		this.notAvailableStatus = notAvailableStatus;
		updateCatalogOverviewUI();
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
		setFormContextHelp(helpUrl);
		
		forgeCatalogInfos(formLayout);
		
		String confPage = velocity_root + "/configuration_list.html";
		offersContainer = FormLayoutContainer.createCustomFormLayout("offers", getTranslator(), confPage);
		offersContainer.setRootForm(mainForm);
		formLayout.add(offersContainer);
		
		loadOffers();
		offersContainer.contextPut("offers", accessInfos);
		
		EmptyState emptyState = EmptyStateFactory.create("empty", offersContainer.getFormItemComponent(), this);
		emptyState.setIconCss("o_icon o_icon_booking");
		emptyState.setMessageI18nKey("offers.empty.message");
		
		offersContainer.contextPut("addOfferLinks", addOfferLinks);
		
		if(!readOnly) {
			if (!managedBookings) {
				addButton = uifactory.addFormLink("create.offer", formLayout, Link.BUTTON);
				
				addMethodDropdown = uifactory.addDropdownMenu("create.more", null, null, formLayout, getTranslator());
				addMethodDropdown.setElementCssClass("o_sel_accesscontrol_create");
				addMethodDropdown.setOrientation(DropdownOrientation.right);
				addMethodDropdown.setAriaLabel("action.nore");
				addMethodDropdown.setExpandContentHeight(true);
				
				for(AccessMethod method:methods) {
					AccessMethodHandler handler = acModule.getAccessMethodHandler(method.getType());
					
					String title = handler.getMethodName(getLocale());
					FormLink addLink = uifactory.addFormLink("create." + handler.getType(), title, null, formLayout, Link.LINK | Link.NONTRANSLATED);
					addLink.setUserObject(method);
					addLink.setIconLeftCSS("o_icon " + method.getMethodCssClass() + "_icon o_icon-fw");
					addMethodDropdown.addElement(addLink);
					formLayout.add(addLink.getName(), addLink);
					
					FormLink addButton = uifactory.addFormLink("create.b." + handler.getType(), title, null, offersContainer, Link.BUTTON | Link.NONTRANSLATED);
					addButton.setUserObject(method);
					addButton.setIconLeftCSS("o_icon " + method.getMethodCssClass() + "_icon o_icon-lg");
					addOfferLinks.add(addButton);
				}
				
				if (openAccessSupported || guestSupported) {
					addMethodDropdown.addElement(new SpacerItem("spacer"));
				}
				
				addOpenAccessLink = uifactory.addFormLink("create.offer.open.link", "create.offer.open", null, formLayout, Link.LINK);
				addOpenAccessLink.setIconLeftCSS("o_icon o_ac_openaccess_icon o_icon-fw");
				addOpenAccessLink.setElementCssClass("o_sel_ac_add_open");
				addOpenAccessLink.setVisible(openAccessSupported);
				addMethodDropdown.addElement(addOpenAccessLink);
				formLayout.add(addOpenAccessLink.getName(), addOpenAccessLink);
				
				addOpenAccessButton = uifactory.addFormLink("create.offer.open", offersContainer, Link.BUTTON);
				addOpenAccessButton.setIconLeftCSS( ("o_icon o_ac_openaccess_icon o_icon-lg"));
				addOpenAccessButton.setElementCssClass("o_sel_ac_add_open");
				addOpenAccessButton.setVisible(openAccessSupported);
				addOfferLinks.add(addOpenAccessButton);
				
				boolean addGuest = isAddGuest();
				addGuestLink = uifactory.addFormLink("create.offer.guest.link", "create.offer.guest", null, formLayout, Link.LINK);
				addGuestLink.setIconLeftCSS( ("o_icon o_ac_guests_icon o_icon-fw"));
				addGuestLink.setElementCssClass("o_sel_ac_add_guest");
				addGuestLink.setVisible(addGuest);
				addMethodDropdown.addElement(addGuestLink);
				formLayout.add(addGuestLink.getName(), addGuestLink);
				
				addGuestButton = uifactory.addFormLink("create.offer.guest", offersContainer, Link.BUTTON);
				addGuestButton.setIconLeftCSS("o_icon o_ac_guests_icon o_icon-lg");
				addGuestButton.setElementCssClass("o_sel_ac_add_guest");
				addGuestButton.setVisible(addGuest);
				addOfferLinks.add(addGuestButton);
			}
			
			updateAddUI();
		}
		
		updateCatalogOverviewUI();
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
				updateCatalogOverviewUI();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(guestOfferCtrl == source) {
			if(event.equals(Event.DONE_EVENT)) {
				Offer offer = guestOfferCtrl.getOffer();
				addGuestOffer(offer);
				updateCatalogOverviewUI();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(newMethodCtrl == source) {
			if(event.equals(Event.DONE_EVENT)) {
				OfferAccess offerAccess = newMethodCtrl.getOfferAccess();
				List<Organisation> organisations = newMethodCtrl.getOfferOrganisations();
				addOffer(offerAccess, organisations);
				updateCatalogOverviewUI();
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
				updateCatalogOverviewUI();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if (methodSelectionCtrl == source) {
			if(event.equals(Event.DONE_EVENT)) {
				String methodType = methodSelectionCtrl.getSelectedType();
				cmc.deactivate();
				cleanUp();
				doMethodSelected(ureq, methodType);
			} else {
				cmc.deactivate();
				cleanUp();
			}
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
		} else if (source == addButton) {
			doCreateOffer(ureq);
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
			} else if("catalog".equals(cmd)) {
				editCatalogInfo(ureq);
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
		
		IconPanelItem iconPanel = new IconPanelItem("offer_" + counter++);
		iconPanel.setElementCssClass("o_block_bottom o_sel_ac_offer");
		iconPanel.setIconCssClass("o_icon o_icon-fw " + link.getMethod().getMethodCssClass() + "_icon");
		iconPanel.setTitle(handler.getMethodName(getLocale()));
		offersContainer.add(iconPanel.getName(), iconPanel);
		
		AccessInfo infos = new AccessInfo(iconPanel, link, handler);
		accessInfos.add(infos);
		FormLayoutContainer cont = FormLayoutContainer.createCustomFormLayout("offer_cont_" + counter++, getTranslator(), velocity_root + "/configuration_content.html");
		cont.setRootForm(mainForm);
		iconPanel.setContent(cont.getComponent());
		infos.setConfigCont(cont);
		
		infos.setOfferOrganisations(offerOrganisations);
		cont.contextPut("offer", infos);
		
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
			IconPanelItem iconPanel = new IconPanelItem("offer_" + counter++);
			iconPanel.setElementCssClass("o_block_bottom o_sel_ac_offer");
			iconPanel.setIconCssClass("o_icon o_icon-fw o_ac_openaccess_icon");
			iconPanel.setTitle(translate("offer.open.access.name"));
			offersContainer.add(iconPanel.getName(), iconPanel);
			
			AccessInfo infos = new AccessInfo(iconPanel);
			accessInfos.add(infos);
			FormLayoutContainer cont = FormLayoutContainer.createCustomFormLayout("offer_cont_" + counter++, getTranslator(), velocity_root + "/configuration_content.html");
			cont.setRootForm(mainForm);
			iconPanel.setContent(cont.getComponent());
			infos.setConfigCont(cont);
			
			infos.setOffer(offer);
			infos.setOfferOrganisations(offerOrganisations);
			cont.contextPut("offer", infos);
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
		
		IconPanelItem iconPanel = new IconPanelItem("offer_" + counter++);
		iconPanel.setElementCssClass("o_block_bottom o_sel_ac_offer");
		iconPanel.setIconCssClass("o_icon o_icon-fw o_ac_guest_icon");
		iconPanel.setTitle(translate("offer.guest.name"));
		offersContainer.add(iconPanel.getName(), iconPanel);
		
		AccessInfo infos = new AccessInfo(iconPanel);
		accessInfos.add(infos);
		FormLayoutContainer cont = FormLayoutContainer.createCustomFormLayout("offer_cont_" + counter++, getTranslator(), velocity_root + "/configuration_content.html");
		cont.setRootForm(mainForm);
		iconPanel.setContent(cont.getComponent());
		infos.setConfigCont(cont);
		
		infos.setOffer(offer);
		
		if (!readOnly) {
			forgeLinks(infos);
		}
		
		offersContainer.setDirty(true);
		updateAddUI();
	}

	private void forgeCatalogInfos(FormItemContainer formLayout) {
		if (catalogInfo.isCatalogSupported()) {
			String overviewPage = velocity_root + "/access_overview.html";
			overviewContainer = FormLayoutContainer.createCustomFormLayout("overview", getTranslator(), overviewPage);
			overviewContainer.setRootForm(mainForm);
			formLayout.add(overviewContainer);
			
			if (catalogInfo.isShowDetails()) {
				overviewContainer.contextPut("detailsLabel", catalogInfo.getDetailsLabel());
				overviewContainer.contextPut("details", catalogInfo.getDetails());
				overviewContainer.contextPut("showQRCode", catalogInfo.isShowQRCode());
				
				if (StringHelper.containsNonWhitespace(catalogInfo.getEditBusinessPath())) {
					FormLink catEditLink = uifactory.addFormLink("catEdit", "catalog", null, "", overviewContainer, Link.NONTRANSLATED + Link.LINK);
					catEditLink.setI18nKey(catalogInfo.getEditLabel());
					catEditLink.setIconLeftCSS("o_icon o_icon_link_extern");
				}
			}
			
			if (StringHelper.containsNonWhitespace(catalogInfo.getCatalogBusinessPath())) {
				long id = CodeHelper.getRAMUniqueID();
				overviewContainer.contextPut("id", String.valueOf(id));
				overviewContainer.contextPut("catalogUrl", catalogInfo.getCatalogBusinessPath());
				
				if (catalogInfo.getMicrosites() != null && !catalogInfo.getMicrosites().isEmpty()) {
					FormLink showMicrositeLinks = uifactory.addFormLink("show.additional" + id, "nodeConfigForm.show.additional", null, overviewContainer, Link.LINK);
					showMicrositeLinks.setIconLeftCSS("o_icon o_icon-lg o_icon_open_togglebox");
					
					HashSet<TaxonomyLevel> taxonomyLevels = new HashSet<>(catalogInfo.getMicrosites());
					List<ExtLink> taxonomyLinks = new ArrayList<>(taxonomyLevels.size());
					for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
						String url = CatalogBCFactory.get(false).getTaxonomyLevelUrl(taxonomyLevel);
						String name = translate("cif.catalog.links.microsite", TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel));
						ExtLink extLink = new ExtLink(taxonomyLevel.getKey().toString() + id, url, name);
						taxonomyLinks.add(extLink);
						overviewContainer.contextPut("taxonomyLinks", taxonomyLinks);
					}
				}
			}
		}
	}
	
	private void updateCatalogOverviewUI() {
		if (overviewContainer == null) {
			return;
		}
		
		String internalCatalog = getCatalogStatus(accessInfos);
		overviewContainer.contextPut("internalCatalog", internalCatalog);
		
		if (catalogInfo.isWebCatalogSupported()) {
			List<AccessInfo> externalCatalogInfos = accessInfos.stream().filter(info -> info.getOffer().isCatalogWebPublish()).toList();
			String externalCatalog = getCatalogStatus(externalCatalogInfos);
			overviewContainer.contextPut("externalCatalog", externalCatalog);
		}
	}
	
	private String getCatalogStatus(List<AccessInfo> catalogAccessInfo) {
		String catalogStatus = null;
		if (catalogInfo.isCatalogSupported()) {
			if (!catalogInfo.isNotAvailableEntry()) {
				boolean atLeastOneActive = catalogAccessInfo.stream().anyMatch(AccessInfo::isActive);
				if (atLeastOneActive) {
					if (catalogInfo.isFullyBooked()) {
						catalogStatus = "<span class=\"o_labeled_light o_ac_fully_booked\"><i class=\"o_icon o_ac_fully_booked_icon\"> </i> "
										+ translate("offers.overview.fully.booked")
										+ "</span>";
					} else {
						if (StringHelper.containsNonWhitespace(notAvailableStatus)) {
							boolean onlyWithOpenPeriod = !catalogAccessInfo.stream().anyMatch(AccessInfo::isWithPeriod);
							if (onlyWithOpenPeriod) {
								catalogStatus = "<span class=\"o_labeled_light o_ac_not_available\"><i class=\"o_icon o_ac_fully_booked_icon\"> </i> "
												+ translate("offers.overview.not.available")
												+ "</span> "
												+ translate("offers.overview.not.available.status", notAvailableStatus);
							}
						}
					}
					if (!StringHelper.containsNonWhitespace(catalogStatus)) {
						catalogStatus = "<span class=\"o_labeled_light o_ac_bookable\"><i class=\"o_icon o_ac_bookable_icon\"> </i> "
								+ translate("offers.overview.bookable")
								+ "</span>";
					}
				}
			}
			if (!StringHelper.containsNonWhitespace(catalogStatus)) {
				catalogStatus = "<span class=\"o_labeled_light o_ac_not_available\"><i class=\"o_icon o_ac_not_available_icon\"> </i> "
								+ translate("offers.overview.not.available")
								+ "</span>";
			}
		}
		return catalogStatus;
	}

	protected void forgeLinks(AccessInfo infos) {
		if(managedBookings) return;
		
		infos.getIconPanel().removeAllLinks();
		
		FormLink editLink = uifactory.addFormLink("edit_" + (++counter), "edit", "offer.edit", null, offersContainer, Link.BUTTON);
		editLink.setUserObject(infos);
		editLink.setGhost(true);
		editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		offersContainer.add(editLink.getName(), editLink);
		infos.getIconPanel().addLink(editLink);

		FormLink delLink = uifactory.addFormLink("del_" + (++counter), "delete", "offer.delete", null, offersContainer, Link.BUTTON);
		delLink.setUserObject(infos);
		delLink.setGhost(true);
		delLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		offersContainer.add(delLink.getName(), delLink);
		infos.getIconPanel().addLink(delLink);
	}

	private void editOpenAccessOffer(UserRequest ureq, Offer offer) {
		guardModalController(openAccessOfferCtrl);
		
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
		openAccessOfferCtrl = new OpenAccessOfferController(ureq, getWindowControl(), openAccessOffer, offerOrganisationsSupported, offerOrganisations, catalogInfo, offer != null);
		listenTo(openAccessOfferCtrl);
		String title = translate("offer.open.access.name");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), openAccessOfferCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}

	private void editGuestOffer(UserRequest ureq, Offer offer) {
		guardModalController(guestOfferCtrl);
		
		Offer guestOffer = offer;
		if (guestOffer == null) {
			guestOffer = acService.createOffer(resource, displayName);
			guestOffer.setGuestAccess(true);
		}
		
		removeAsListenerAndDispose(guestOfferCtrl);
		guestOfferCtrl = new GuestOfferController(ureq, getWindowControl(), guestOffer, catalogInfo, offer != null);
		listenTo(guestOfferCtrl);
		String title = translate("offer.guest.name");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), guestOfferCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}

	private void editOffer(UserRequest ureq, AccessInfo infos) {
		guardModalController(editMethodCtrl);
		
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
			editMethodCtrl = handler.editConfigurationController(ureq, getWindowControl(), link, offerOrganisationsSupported, offerOrganisations, catalogInfo);
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
		guardModalController(newMethodCtrl);
		
		Offer offer = acService.createOffer(resource, displayName);
		OfferAccess link = acService.createOfferAccess(offer, method);
		
		removeAsListenerAndDispose(newMethodCtrl);
		AccessMethodHandler handler = acModule.getAccessMethodHandler(link.getMethod().getType());
		if (handler != null) {
			newMethodCtrl = handler.createConfigurationController(ureq, getWindowControl(), link, offerOrganisationsSupported, defaultOfferOrganisations, catalogInfo);
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
	
	public void doCreateOffer(UserRequest ureq) {
		guardModalController(methodSelectionCtrl);
		if (!readOnly && !managedBookings) {
			methodSelectionCtrl = new MethodSelectionController(ureq, getWindowControl(), openAccessSupported, guestSupported, methods);
			listenTo(methodSelectionCtrl);
			String title = translate("offer.add");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), methodSelectionCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doMethodSelected(UserRequest ureq, String methodType) {
		if (MethodSelectionController.KEY_OPEN_ACCESS.equals(methodType)) {
			editOpenAccessOffer(ureq, null);
		} else if (MethodSelectionController.KEY_GUEST_ACCESS.equals(methodType)) {
			editGuestOffer(ureq, null);
		} else {
			for (AccessMethod method : methods) {
				if (method.getType().equals(methodType)) {
					addOffer(ureq, method);
				}
			}
		}
	}
	
	private void removeOffer(AccessInfo infos) {
		Offer offer = infos.getOffer();
		if (offer.getKey() != null) {
			deletedOfferList.add(offer);
		}
		accessInfos.remove(infos);
		updateCatalogOverviewUI();
		updateAddUI();
	}

	private void editCatalogInfo(UserRequest ureq) {
		try {
			if (catalogInfo.isCatalogSupported() && StringHelper.containsNonWhitespace(catalogInfo.getEditBusinessPath())) {
				NewControllerFactory.getInstance().launch(catalogInfo.getEditBusinessPath(), ureq, getWindowControl());
			}
		} catch (Exception e) {
			//
		}
	}

	public void commitChanges() {
		for(AccessInfo info:accessInfos) {
			if (info.getOffer().isGuestAccess()) {
				acService.save(info.getOffer());
			} else if (info.getOffer().isOpenAccess()) {
				acService.save(info.getOffer());
				acService.updateOfferOrganisations(info.getOffer(), info.getOfferOrganisations());
			} else {
				OfferAccess link = info.getLink();
				if (link != null) {
					acService.saveOfferAccess(link);
					acService.updateOfferOrganisations(info.getOffer(), info.getOfferOrganisations());
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
		Map<Long, List<AccessInfo>> organisationKeyToAccessInfo = new HashMap<>(1);
		if (organisationModule.isEnabled()) {
			for (AccessInfo accessInfo : accessInfos) {
				if (accessInfo.getOfferOrganisations() != null && !accessInfo.getOfferOrganisations().isEmpty()) {
					for (Organisation organisation : accessInfo.getOfferOrganisations()) {
						organisationKeyToAccessInfo.computeIfAbsent(organisation.getKey(), key -> new ArrayList<>(2)).add(accessInfo);
					}
				} else {
					organisationKeyToAccessInfo.computeIfAbsent(-1l, key -> new ArrayList<>(2)).add(accessInfo);
				}
			}
		} else {
			organisationKeyToAccessInfo.put(-1l, accessInfos);
		}
		
		boolean overlap = false;
		boolean overlapAllowed = true;
		for (List<AccessInfo> accessInfosToCheck : organisationKeyToAccessInfo.values()) {
			OverlapCheckResult checkResult = checkOverlap(accessInfosToCheck);
			if (checkResult.overlap) {
				overlap = checkResult.overlap;
				overlapAllowed = checkResult.overlapAllowed;
				break;
			}
		}
		
		// Display a warning
		offersContainer.contextPut("overlappingConfigs", overlap);
		offersContainer.contextPut("overlappingErrorConfigs", !overlapAllowed);
		offersContainer.setDirty(true);
	}
	
	private OverlapCheckResult checkOverlap(List<AccessInfo> accessInfosToCheck) {
		boolean overlap = false;
		boolean overlapAllowed = true;
		
		// Take a controller from the list
		for (AccessInfo confControllerA : accessInfosToCheck) {
			if (confControllerA.getLink() == null) continue;
			// Compare it to every other from the list
			for (AccessInfo confControllerB : accessInfosToCheck) {
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
				return new OverlapCheckResult(overlap, overlapAllowed);
			}
		}
		
		return new OverlapCheckResult(overlap, overlapAllowed);
	}
	
	private record OverlapCheckResult(boolean overlap, boolean overlapAllowed) {}
	
	public interface OfferWithOrganisation {
		
		public Offer getOffer();
		
		public Collection<Organisation> getOfferOrganisations();
		
	}
	
	public interface OfferAccessWithOrganisation {
		
		public OfferAccess getLink();
		
		public Collection<Organisation> getOfferOrganisations();
		
	}

	public class AccessInfo implements OfferWithOrganisation, OfferAccessWithOrganisation {
		
		private final IconPanelItem iconPanel;
		private Offer offer;
		private boolean active;
		private boolean withPeriod;
		private String dates;
		private Collection<Organisation> offerOrganisations;
		private OfferAccess link;
		private AccessMethodHandler handler;
		private FormLayoutContainer configCont;
		
		public AccessInfo(IconPanelItem iconPanel) {
			this.iconPanel = iconPanel;
		}

		public AccessInfo(IconPanelItem iconPanel, OfferAccess link, AccessMethodHandler handler) {
			this.iconPanel = iconPanel;
			this.offer = link != null? link.getOffer(): null;
			this.link = link;
			this.handler = handler;
			initDates();
		}

		public IconPanelItem getIconPanel() {
			return iconPanel;
		}

		public boolean isActive() {
			return active;
		}

		public boolean isWithPeriod() {
			return withPeriod;
		}
		
		public String getDates() {
			return dates;
		}

		private void initDates() {
			Date from = offer.getValidFrom();
			Date to = offer.getValidTo();
			if (to != null && to.before(new Date())) {
				dates = "<span class=\"o_labeled_light o_ac_offer_ended\"><i class=\"o_icon o_icon_offer_ended\"> </i> " + translate("access.period.ended") + "</span> "
					+ "<del>" + formatPeriod(from, to) + "</del>";
				active = false;
				withPeriod = true;
			} else if (from != null && from.after(new Date())) {
				dates = "<span class=\"o_labeled_light o_ac_offer_planned\"><i class=\"o_icon o_icon_offer_planned\"> </i> " + translate("access.period.planned") + "</span> "
					+ formatPeriod(from, to)
					+ " | <strong>" + translate("access.period.starts.in", String.valueOf(DateUtils.countDays(new Date(), from))) + "</strong>";
				active = false;
				withPeriod = true;
			} else if (to != null && to.after(new Date())) {
				dates = "<span class=\"o_labeled_light o_ac_offer_ongoing\"><i class=\"o_icon o_icon_offer_ongoing\"> </i> " + translate("access.period.ongoing") + "</span> "
					+ formatPeriod(from, to)
					+ " | <strong>" + translate("access.period.ends.in", String.valueOf(DateUtils.countDays(new Date(), to))) + "</strong>";
				active = true;
				withPeriod = true;
			} else {
				dates = "<span class=\"o_labeled_light o_ac_offer_ongoing\"><i class=\"o_icon o_icon_offer_ongoing\"> </i> " + translate("access.period.ongoing") + "</span> "
					+ translate("access.period.none");
				active = true;
				withPeriod = false;
			}
		}
		
		private String formatPeriod(Date from, Date to) {
			if (from != null && to != null) {
				return translate("access.period.range", formatter.formatDate(from), formatter.formatDate(to));
			} else if (from != null) {
				return translate("access.period.range.from", formatter.formatDate(from));
			} else if (to != null) {
				return translate("access.period.range.to", formatter.formatDate(to));
			}
			return "";
		}
		
		public boolean isPaymentMethod() {
			return handler != null? handler.isPaymentMethod(): false;
		}
		
		public boolean isOverlapAllowed(AccessInfo info) {
			return handler != null? handler.isOverlapAllowed(info.handler): false;
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
			initDates();
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

		public String getPublishIn() {
			String publishIn = null;
			if (offer != null) {
				if (offer.isCatalogPublish()) {
					publishIn = ICON_CATALOG_INTERN + translate("offer.publish.in.intern");
					if (offer.isCatalogWebPublish()) {
						publishIn += ", " + ICON_CATALOG_EXTERN + translate("offer.publish.in.extern");
					}
				}
			}
			return publishIn;
		}

		public FormLayoutContainer getConfigCont() {
			return configCont;
		}

		public void setConfigCont(FormLayoutContainer configCont) {
			this.configCont = configCont;
		}

		@Override
		public OfferAccess getLink() {
			return link;
		}

		public void setLink(OfferAccess link) {
			this.link = link;
			this.offer = link != null? link.getOffer(): null;
			this.iconPanel.setIconCssClass("o_icon o_icon-fw " + (link != null? link.getMethod().getMethodCssClass() + "_icon": ""));
		}

	}
	
}