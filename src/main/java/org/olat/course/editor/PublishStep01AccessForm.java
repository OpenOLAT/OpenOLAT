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
package org.olat.course.editor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.helpTooltip.HelpTooltip;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.fileresource.types.ScormCPFileResource;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.login.LoginModule;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.AuthoringEntryPublishController;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OfferImpl;
import org.olat.resource.accesscontrol.ui.AbstractConfigurationMethodController;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * @author fkiefer
 */
public class PublishStep01AccessForm extends StepFormBasicController {

	private final String[] leaveKeys = new String[]{
			RepositoryEntryAllowToLeaveOptions.atAnyTime.name(),
			RepositoryEntryAllowToLeaveOptions.afterEndDate.name(),
			RepositoryEntryAllowToLeaveOptions.never.name()
		};

	private static final String YES_KEY = "y";
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] yesKeys = new String[] { YES_KEY };
	
	private SingleSelection leaveEl;
	
	private RepositoryEntry entry;
	private SelectionElement canCopy;
	private SelectionElement canReference;
	private SelectionElement canDownload;
	private SelectionElement allUsers;
	private SelectionElement guests;
	private SingleSelection publishedStatus;
	private FormLayoutContainer accessLayout;
	
	private RepositoryHandler handler;

	private MultipleSelectionElement confirmationEmailEl;
	private List<FormLink> addMethods = new ArrayList<>();
	private List<OfferAccess> offerAccess = new ArrayList<>();
	private List<Offer> deletedOffer = new ArrayList<>();
	private final String displayName;
	
	private CloseableModalController cmc;
	private FormLayoutContainer confControllerContainer;
	private AbstractConfigurationMethodController newMethodCtrl;
	private AbstractConfigurationMethodController editMethodCtrl;
	
	private final List<AccessInfo> confControllers = new ArrayList<>();
	
	private int buttonId;
	private final boolean emptyConfigGrantsFullAccess;
	private boolean allowPaymentMethod;
	private final boolean editable;
	
	private final Formatter formatter;
	private final OLATResource resource;

	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;
	
	public PublishStep01AccessForm(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
		super(ureq, control, rootForm, runContext, LAYOUT_BAREBONE, null);
		Translator translator = Util.createPackageTranslator(
				Util.createPackageTranslator(RepositoryService.class, AuthoringEntryPublishController.class, getLocale()),
				Util.createPackageTranslator(ChooseNodeController.class, AccessConfigurationController.class, getLocale()), getLocale());
		setTranslator(translator);
		entry = (RepositoryEntry) getFromRunContext("repoEntry");
		displayName = entry.getDisplayname();
		resource = entry.getOlatResource();
		emptyConfigGrantsFullAccess = true;
		buttonId = 0;
		editable = !RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.bookings);
		
		formatter = Formatter.getInstance(getLocale());		
		
		initForm(ureq);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		RepositoryEntryAllowToLeaveOptions setting;
		if(leaveEl.isOneSelected()) {
			setting = RepositoryEntryAllowToLeaveOptions.valueOf(leaveEl.getSelectedKey());
		} else {
			setting = RepositoryEntryAllowToLeaveOptions.atAnyTime;
		}
		
		boolean accessGuests = isGuests();
		boolean accessAllUsers = isAllUsers();
		RepositoryEntryStatusEnum accessStatus = getEntryStatus();
		CourseAccessAndProperties accessProperties = new CourseAccessAndProperties(entry, setting,
				accessStatus, accessAllUsers, accessGuests, canCopy.isSelected(0), canReference.isSelected(0), canDownload.isSelected(0));
		
		accessProperties.setOfferAccess(offerAccess);
		accessProperties.setDeletedOffer(deletedOffer);
		if(confirmationEmailEl.isVisible()) {
			accessProperties.setConfirmationEmail(confirmationEmailEl.isAtLeastSelected(1));
		} else {
			accessProperties.setConfirmationEmail(null);
		}
					
		addToRunContext("accessAndProperties", accessProperties);	
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {			
		FormLayoutContainer headersLayout = FormLayoutContainer.createCustomFormLayout("access", getTranslator(), velocity_root + "/publish_courseaccess.html");
		formLayout.add(headersLayout);
		headersLayout.contextPut("catalogEnabled", repositoryModule.isCatalogEnabled());

		FormLayoutContainer publishLayout = FormLayoutContainer.createDefaultFormLayout("publish", getTranslator());
		formLayout.add(publishLayout);
		publishLayout.setFormTitle(translate("rentry.publish"));
		publishLayout.setFormContextHelp("Course Settings#_zugriff");
		publishLayout.setElementCssClass("o_sel_repositoryentry_access");

		String resourceType = entry.getOlatResource().getResourceableTypeName();
		if (TestFileResource.TYPE_NAME.equals(resourceType)
			|| SurveyFileResource.TYPE_NAME.equals(resourceType)
			|| ScormCPFileResource.TYPE_NAME.equals(resourceType)) {
			String warning = translate("warn.resource.need.course");
			flc.contextPut("off_warn", warning);
		}
		if (CourseModule.ORES_TYPE_COURSE.equals(resourceType)) {
			publishLayout.setFormDescription(translate("rentry.publish.course.desc"));			
		} else {
			publishLayout.setFormDescription(translate("rentry.publish.other.desc"));			
		}
		if (resourceType != null) {
			handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(resourceType);
		}
		
		// make configuration read only when managed by external system
		final boolean managedSettings = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.settings);
		final boolean managedAccess = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.access);
		
		String[] publishedKeys = new String[] {
			RepositoryEntryStatusEnum.preparation.name(), RepositoryEntryStatusEnum.review.name(),
			RepositoryEntryStatusEnum.coachpublished.name(), RepositoryEntryStatusEnum.published.name()
		};
		String[] publishedValues = new String[] {
			translate("cif.status." + RepositoryEntryStatusEnum.preparation), translate("cif.status." + RepositoryEntryStatusEnum.review),
			translate("cif.status." + RepositoryEntryStatusEnum.coachpublished), translate("cif.status." + RepositoryEntryStatusEnum.published),
		};
		
		publishedStatus = uifactory.addDropdownSingleselect("publishedStatus", "cif.publish", publishLayout, publishedKeys, publishedValues, null);
		publishedStatus.setElementCssClass("o_sel_repositoryentry_access_publication");
		publishedStatus.setEnabled(!managedAccess);
		publishedStatus.addActionListener(FormEvent.ONCHANGE);
	
		allUsers = uifactory.addCheckboxesVertical("cif.allusers", "cif.allusers", publishLayout, yesKeys, new String[] { translate("cif.release") }, 1);
		allUsers.addActionListener(FormEvent.ONCHANGE);
		allUsers.setElementCssClass("o_sel_repositoryentry_access_all_users");
		allUsers.setEnabled(!managedAccess);
		guests = uifactory.addCheckboxesVertical("cif.guests", "cif.guests", publishLayout, yesKeys, new String[] { translate("cif.release") }, 1);
		guests.setElementCssClass("o_sel_repositoryentry_access_all_users");
		guests.setEnabled(!managedAccess);
		guests.setVisible(loginModule.isGuestLoginEnabled());
		
		uifactory.addSpacerElement("userSpacer", formLayout, true);

		canReference = uifactory.addCheckboxesVertical("cif_canReference", "cif.author.can", publishLayout, yesKeys, new String[] { translate("cif.canReference") }, 1);
		canReference.setEnabled(!managedSettings);
		canCopy = uifactory.addCheckboxesVertical("cif_canCopy", null, publishLayout, yesKeys, new String[] { translate("cif.canCopy") }, 1);
		canCopy.setEnabled(!managedSettings);
		canDownload = uifactory.addCheckboxesVertical("cif_canDownload", null, publishLayout, yesKeys, new String[] { translate("cif.canDownload") }, 1);
		canDownload.setEnabled(!managedSettings);
		canDownload.setVisible(handler.supportsDownload());
		uifactory.addSpacerElement("authorSpacer", formLayout, true);

		// Part 2
		
		FormLayoutContainer membershipLayout = FormLayoutContainer.createDefaultFormLayout("membership", getTranslator());
		formLayout.add(membershipLayout);
		
		membershipLayout.setFormTitle(translate("rentry.leaving.title"));
		
		String[] leaveValues = new String[]{
				translate("rentry.leave.atanytime"),
				translate("rentry.leave.afterenddate"),
				translate("rentry.leave.never")
		};
		
		final boolean managedLeaving = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.membersmanagement);
		leaveEl = uifactory.addDropdownSingleselect("entry.leave", "rentry.leave.option", membershipLayout, leaveKeys, leaveValues, null);
		boolean found = false;
		for(String leaveKey:leaveKeys) {
			if(leaveKey.equals(entry.getAllowToLeaveOption().name())) {
				leaveEl.select(leaveKey, true);
				found = true;
			}
		}
		if(!found) {
			if(managedLeaving) {
				leaveEl.select(RepositoryEntryAllowToLeaveOptions.never.name(), true);
			} else {
				RepositoryEntryAllowToLeaveOptions defaultOption = repositoryModule.getAllowToLeaveDefaultOption();
				leaveEl.select(defaultOption.name(), true);
			}
		}
		leaveEl.setEnabled(!managedLeaving);
		
		// Part 3
		
		accessLayout = FormLayoutContainer.createCustomFormLayout("accessConfig", getTranslator(), velocity_root + "/access_configuration.html");
		formLayout.add(accessLayout);
		accessLayout.setVisible(entry.isAllUsers()
				|| loginModule.isGuestLoginLinksEnabled() && entry.isGuests()
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.published);
		
		accessLayout.setFormTitle(translate("accesscontrol.title"));
		
		HelpTooltip acMethodsLabelHelp = new HelpTooltip("acMethodsLabelHelp", "Legen Sie fest unter welchen Bedingungen Benutzer diese Ressource buchen können.", "Course Settings#_buchungsmethode", getLocale());
		accessLayout.put("acMethodsLabelHelp", acMethodsLabelHelp);

		if(editable) {
			List<AccessMethod> methods = acService.getAvailableMethods(getIdentity(), ureq.getUserSession().getRoles());
			for(AccessMethod method:methods) {
				AccessMethodHandler methodHandler = acModule.getAccessMethodHandler(method.getType());
				if(methodHandler.isPaymentMethod() && !allowPaymentMethod) {
					continue;
				}
				
				String title = methodHandler.getMethodName(getLocale());
				FormLink add = uifactory.addFormLink("create." + methodHandler.getType(), title, null, accessLayout, Link.LINK | Link.NONTRANSLATED);
				add.setUserObject(method);
				add.setIconLeftCSS( ("o_icon " + method.getMethodCssClass() + "_icon o_icon-lg").intern());
				addMethods.add(add);
				accessLayout.add(add.getName(), add);
			}
			accessLayout.contextPut("methods", addMethods);
		}
		
		String[] onValues = new String[] { "" };
		confirmationEmailEl = uifactory.addCheckboxesHorizontal("confirmation.email", accessLayout, onKeys, onValues);
		confirmationEmailEl.addActionListener(FormEvent.ONCHANGE);
		confirmationEmailEl.setVisible(false);

		String confPage = velocity_root + "/configuration_list.html";
		confControllerContainer = FormLayoutContainer.createCustomFormLayout("conf-controllers", getTranslator(), confPage);
		accessLayout.add(confControllerContainer);
		
		loadConfigurations();
		
		boolean confirmationEmail = false;
		for(AccessInfo info:confControllers) {
			confirmationEmail |= info.getLink().getOffer().isConfirmationEmail();
		}
		if(confirmationEmail) {
			confirmationEmailEl.select(onKeys[0], true);
		}
		
		confControllerContainer.contextPut("confControllers", confControllers);
		
		confControllerContainer.contextPut("emptyConfigGrantsFullAccess", Boolean.valueOf(emptyConfigGrantsFullAccess));		
		
		initFormData();
	}
	
	protected void loadConfigurations() {
		List<Offer> offers = acService.findOfferByResource(resource, true, null);
		for(Offer offer:offers) {
			List<OfferAccess> offerAccessList = acService.getOfferAccess(offer, true);
			for(OfferAccess access:offerAccessList) {
				buttonId++;
				addConfiguration(access);
			}
		}
	}
	
	protected void addConfiguration(OfferAccess link) {
		AccessMethodHandler methodHandler = acModule.getAccessMethodHandler(link.getMethod().getType());
		AccessInfo infos = new AccessInfo(methodHandler.getMethodName(getLocale()), methodHandler.isPaymentMethod(), null, link);
		infos.setButtonId(buttonId);
		confControllers.add(infos);
		
		if(editable) {
			FormLink editLink = uifactory.addFormLink("edit_" + infos.getButtonId(), "edit", "edit", null, confControllerContainer, Link.BUTTON_SMALL);
			editLink.setUserObject(infos);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			confControllerContainer.add(editLink.getName(), editLink);
			
			FormLink delLink = uifactory.addFormLink("del_" + infos.getButtonId(), "delete", "delete", null, confControllerContainer, Link.BUTTON_SMALL);
			delLink.setUserObject(infos);
			delLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			confControllerContainer.add(delLink.getName(), delLink);
		}
		updateConfirmationEmail();
	}
	
	private void updateConfirmationEmail() {
		if(confirmationEmailEl.isVisible() != !confControllers.isEmpty()) {
			confirmationEmailEl.setVisible(!confControllers.isEmpty());
			accessLayout.setDirty(true);
		}
	}
	
	public class AccessInfo {
		private String name;
		private String infos;
		private String dates;
		private OfferAccess link;
		private final boolean paymentMethod;
		private int infoButtonId;
		
		public AccessInfo(String name, boolean paymentMethod, String infos, OfferAccess link) {
			this.name = name;
			this.paymentMethod = paymentMethod;
			this.infos = infos;
			this.link = link;
		}
				
		public int getButtonId() {
			return infoButtonId;
		}

		public void setButtonId(int infoButtonId) {
			this.infoButtonId = infoButtonId;
		}

		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public boolean isPaymentMethod() {
			return paymentMethod;
		}

		public String getDates() {
			if(dates == null && link.getOffer() != null) {
				Date from = link.getValidFrom();
				Date to = link.getValidTo();
				if(from != null && to != null) {
					dates = translate("ac.fromto.label", new String[]{ formatter.formatDate(from), formatter.formatDate(to) });
				} else if(from != null) {
					dates = translate("ac.from.label", new String[]{ formatter.formatDate(from) });
				} else if(to != null) {
					dates = translate("ac.to.label", new String[]{ formatter.formatDate(to) });
				}
			}
			return dates;
		}

		public String getInfos() {
			if(infos == null && link.getOffer() != null) {
				OfferImpl casted = (OfferImpl)link.getOffer();
				if(StringHelper.containsNonWhitespace(casted.getToken())) {
					return casted.getToken();
				}
				if(!link.getOffer().getPrice().isEmpty()) {
					String price = PriceFormat.fullFormat(link.getOffer().getPrice());
					if(acModule.isVatEnabled()) {
						BigDecimal vat = acModule.getVat();
						String vatStr = vat == null ? "" : vat.setScale(3, BigDecimal.ROUND_HALF_EVEN).toPlainString();
						return translate("access.info.price.vat", new String[]{price, vatStr});
						
					} else {
						return translate("access.info.price.noVat", new String[]{price});
					}
				}
			}
			if(StringHelper.containsNonWhitespace(infos)) {
				return infos;
			}
			return "";
		}
		
		public void setInfos(String infos) {
			this.infos = infos;
		}

		public OfferAccess getLink() {
			return link;
		}

		public void setLink(OfferAccess link) {
			this.link = link;
			this.dates = null;
		}
	}
	
	private void initFormData() {
		// init author visibility and flags
		canReference.select(YES_KEY, entry.getCanReference()); 
		canCopy.select(YES_KEY, entry.getCanCopy()); 
		canDownload.select(YES_KEY, entry.getCanDownload());
		
		publishedStatus.select(entry.getEntryStatus().name(), true);
		allUsers.select(YES_KEY, entry.isAllUsers());
		guests.select(YES_KEY, entry.isGuests());
		
		accessLayout.setVisible(allUsers.isSelected(0));
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == allUsers) {
			if (allUsers.isSelected(0)) {
				accessLayout.setVisible(true);
			} else {
				accessLayout.setVisible(false);
			}
		} else if(addMethods.contains(source)) {
			AccessMethod method = (AccessMethod)source.getUserObject();
			addMethod(ureq, method);
		} else if(confirmationEmailEl == source) {
			for(AccessInfo info:confControllers) {
				if(!offerAccess.contains(info.getLink())) {
					offerAccess.add(info.getLink());
				}
			}
		} else if (source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if("delete".equals(cmd)) {
				AccessInfo infos = (AccessInfo)source.getUserObject();
				OfferAccess deleteOffer = infos.getLink();
				offerAccess.remove(deleteOffer);
				if (deleteOffer.getKey() != null && deleteOffer.getOffer() != null && deleteOffer.getOffer().getKey() != null) {
					deletedOffer.add(deleteOffer.getOffer());			
				}
				confControllers.remove(infos);
				updateConfirmationEmail();
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if("edit".equals(cmd)) {
				AccessInfo infos = (AccessInfo)source.getUserObject();
				editMethod(ureq, infos);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newMethodCtrl == source) {
			if(event.equals(Event.DONE_EVENT)) {
				OfferAccess newLink = newMethodCtrl.commitChanges();
				buttonId++;
				if (!offerAccess.contains(newLink)) {
					offerAccess.add(newLink);
				}				
				addConfiguration(newLink);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editMethodCtrl == source) {
			if(event.equals(Event.DONE_EVENT)) {
				OfferAccess newLink = editMethodCtrl.commitChanges();
				if (!offerAccess.contains(newLink)) {
					offerAccess.add(newLink);
				}	
				replace(newLink);
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
		removeAsListenerAndDispose(editMethodCtrl);
		removeAsListenerAndDispose(newMethodCtrl);
		removeAsListenerAndDispose(cmc);
		editMethodCtrl = null;
		newMethodCtrl = null;
		cmc = null;
	}
	
	protected void replace(OfferAccess link) {
		boolean updated = false;
		for(AccessInfo confController:confControllers) {
			if(confController.getLink().equals(link)) {
				confController.setLink(link);
				updated = true;
			}
		}
		
		if(!updated) {
			addConfiguration(link);
		} else {
			confControllerContainer.setDirty(true);
		}
	}
	
	private RepositoryEntryStatusEnum getEntryStatus() {
		// default only for owners
		String selectedKey = publishedStatus.getSelectedKey();
		return RepositoryEntryStatusEnum.valueOf(selectedKey);
	}
	
	private boolean isAllUsers() {
		return allUsers.isSelected(0);
	}
	
	private boolean isGuests() {
		return guests.isSelected(0);
	}
	
	private void editMethod(UserRequest ureq, AccessInfo infos) {
		OfferAccess link = infos.getLink();
		
		removeAsListenerAndDispose(editMethodCtrl);
		AccessMethodHandler methodHandler = acModule.getAccessMethodHandler(link.getMethod().getType());
		if (methodHandler != null) {
			editMethodCtrl = methodHandler.editConfigurationController(ureq, getWindowControl(), link);
		}
		
		if(editMethodCtrl != null) {
			listenTo(editMethodCtrl);

			String title = methodHandler.getMethodName(getLocale());
			cmc = new CloseableModalController(getWindowControl(), translate("close"), editMethodCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	protected void addMethod(UserRequest ureq, AccessMethod method) {
		Offer offer = acService.createOffer(resource, displayName);
		OfferAccess link = acService.createOfferAccess(offer, method);
		
		removeAsListenerAndDispose(newMethodCtrl);
		AccessMethodHandler methodHandler = acModule.getAccessMethodHandler(link.getMethod().getType());
		if (methodHandler != null) {
			newMethodCtrl = methodHandler.createConfigurationController(ureq, getWindowControl(), link);
		}
		if(newMethodCtrl != null) {
			listenTo(newMethodCtrl);

			String title = methodHandler.getMethodName(getLocale());
			cmc = new CloseableModalController(getWindowControl(), translate("close"), newMethodCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		} else {
			if (!offerAccess.contains(link)) {
				offerAccess.add(link);
			}
			addConfiguration(link);
		}
	}
}