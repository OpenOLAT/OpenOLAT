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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.helpTooltip.HelpTooltip;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OfferImpl;
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
	
	private static final String[] onKeys = new String[] { "on" };

	private MultipleSelectionElement confirmationEmailEl;
	private final List<FormLink> addMethods = new ArrayList<>();
	
	private final String displayName;
	private final OLATResource resource;

	private CloseableModalController cmc;
	private FormLayoutContainer confControllerContainer;
	private AbstractConfigurationMethodController newMethodCtrl, editMethodCtrl;

	private final List<AccessInfo> confControllers = new ArrayList<>();

	private final boolean embbed;
	private final boolean emptyConfigGrantsFullAccess;
	private boolean allowPaymentMethod;
	private final boolean editable;

	private final Formatter formatter;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;

	public AccessConfigurationController(UserRequest ureq, WindowControl wControl, OLATResource resource,
			String displayName, boolean allowPaymentMethod, boolean editable) {
		super(ureq, wControl, "access_configuration");

		this.resource = resource;
		this.displayName = displayName;
		this.allowPaymentMethod = allowPaymentMethod;
		embbed = false;
		this.editable = editable;
		emptyConfigGrantsFullAccess = true;
		formatter = Formatter.getInstance(getLocale());

		initForm(ureq);
	}

	public AccessConfigurationController(UserRequest ureq, WindowControl wControl, OLATResource resource,
			String displayName, boolean allowPaymentMethod, boolean editable, Form form) {
		super(ureq, wControl, FormBasicController.LAYOUT_CUSTOM, "access_configuration", form);

		this.editable = editable;
		this.resource = resource;
		this.displayName = displayName;
		this.allowPaymentMethod = allowPaymentMethod;
		embbed = true;
		emptyConfigGrantsFullAccess = false;
		formatter = Formatter.getInstance(getLocale());

		initForm(ureq);
	}

	public int getNumOfBookingConfigurations() {
		return confControllers.size();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//contextHelptexts for Label
		HelpTooltip acMethodsLabelHelp = new HelpTooltip("acMethodsLabelHelp", "Legen Sie fest unter welchen Bedingungen Benutzer diese Ressource buchen können.", "Course Settings#_buchungsmethode", getLocale());
		((FormLayoutContainer)formLayout).put("acMethodsLabelHelp", acMethodsLabelHelp);

		if(editable) {
			List<AccessMethod> methods = acService.getAvailableMethods(getIdentity(), ureq.getUserSession().getRoles());
			for(AccessMethod method:methods) {
				AccessMethodHandler handler = acModule.getAccessMethodHandler(method.getType());
				if((handler.isPaymentMethod() && !allowPaymentMethod) || !method.isVisibleInGui()) {
					continue;
				}

				String title = handler.getMethodName(getLocale());
				FormLink add = uifactory.addFormLink("create." + handler.getType(), title, null, formLayout, Link.LINK | Link.NONTRANSLATED);
				add.setUserObject(method);
				add.setIconLeftCSS( ("o_icon " + method.getMethodCssClass() + "_icon o_icon-lg").intern());
				addMethods.add(add);
				formLayout.add(add.getName(), add);
			}
			((FormLayoutContainer)formLayout).contextPut("methods", addMethods);
		}
		
		String[] onValues = new String[] { "" };
		confirmationEmailEl = uifactory.addCheckboxesHorizontal("confirmation.email", formLayout, onKeys, onValues);
		confirmationEmailEl.addActionListener(FormEvent.ONCHANGE);
		confirmationEmailEl.setVisible(false);

		String confPage = velocity_root + "/configuration_list.html";
		confControllerContainer = FormLayoutContainer.createCustomFormLayout("conf-controllers", getTranslator(), confPage);
		confControllerContainer.setRootForm(mainForm);
		formLayout.add(confControllerContainer);

		loadConfigurations();

		confControllerContainer.contextPut("confControllers", confControllers);

		boolean confirmationEmail = false;
		for(AccessInfo info:confControllers) {
			Offer offer = info.getLink().getOffer();
			confirmationEmail |= offer.isConfirmationEmail();
		}
		if(confirmationEmail) {
			confirmationEmailEl.select(onKeys[0], true);
		}

		if(!embbed) {
			setFormTitle("accesscontrol.title");

			if(editable) {
				final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
				buttonGroupLayout.setRootForm(mainForm);
				formLayout.add(buttonGroupLayout);
				formLayout.add("buttonLayout", buttonGroupLayout);

				uifactory.addFormSubmitButton("save", buttonGroupLayout);
			}
		}

		confControllerContainer.contextPut("emptyConfigGrantsFullAccess", Boolean.valueOf(emptyConfigGrantsFullAccess));
	}

	public void setAllowPaymentMethod(boolean allowPayment) {
		this.allowPaymentMethod = allowPayment;
	}

	public boolean isPaymentMethodInUse() {
		boolean paymentMethodInUse = false;
		for(AccessInfo info:confControllers) {
			paymentMethodInUse |= info.isPaymentMethod();
		}
		return paymentMethodInUse;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newMethodCtrl == source) {
			if(event.equals(Event.DONE_EVENT)) {
				OfferAccess newLink = newMethodCtrl.commitChanges();
				newLink = acService.saveOfferAccess(newLink);
				addConfiguration(newLink);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editMethodCtrl == source) {
			if(event.equals(Event.DONE_EVENT)) {
				OfferAccess newLink = editMethodCtrl.commitChanges();
				newLink = acService.saveOfferAccess(newLink);
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

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addMethods.contains(source)) {
			AccessMethod method = (AccessMethod)source.getUserObject();
			addMethod(ureq, method);
		} else if (source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if("delete".equals(cmd)) {
				AccessInfo infos = (AccessInfo)source.getUserObject();
				removeMethod(infos);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if("edit".equals(cmd)) {
				AccessInfo infos = (AccessInfo)source.getUserObject();
				editMethod(ureq, infos);
			}
		} else if(confirmationEmailEl == source) {
			setConfirmationEmail(confirmationEmailEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void formOK(UserRequest ureq) {
		//
	}

	protected void loadConfigurations() {
		List<Offer> offers = acService.findOfferByResource(resource, true, null);
		for(Offer offer:offers) {
			List<OfferAccess> offerAccess = acService.getOfferAccess(offer, true);
			for(OfferAccess access:offerAccess) {
				addConfiguration(access);
			}
		}
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

	protected void addConfiguration(OfferAccess link) {
		AccessMethodHandler handler = acModule.getAccessMethodHandler(link.getMethod().getType());
		AccessInfo infos = new AccessInfo(handler.getMethodName(getLocale()), handler.isPaymentMethod(), null, link);
		confControllers.add(infos);

		if(editable) {
			FormLink editLink = uifactory.addFormLink("edit_" + link.getKey(), "edit", "edit", null, confControllerContainer, Link.BUTTON_SMALL);
			editLink.setUserObject(infos);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			confControllerContainer.add(editLink.getName(), editLink);

			FormLink delLink = uifactory.addFormLink("del_" + link.getKey(), "delete", "delete", null, confControllerContainer, Link.BUTTON_SMALL);
			delLink.setUserObject(infos);
			delLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			confControllerContainer.add(delLink.getName(), delLink);
		}
		
		updateConfirmationEmail();
	}
	
	private void updateConfirmationEmail() {
		if(confirmationEmailEl.isVisible() != !confControllers.isEmpty()) {
			confirmationEmailEl.setVisible(!confControllers.isEmpty());
			flc.setDirty(true);
		}
	}

	private void editMethod(UserRequest ureq, AccessInfo infos) {
		OfferAccess link = infos.getLink();

		removeAsListenerAndDispose(editMethodCtrl);
		AccessMethodHandler handler = acModule.getAccessMethodHandler(link.getMethod().getType());
		if (handler != null) {
			editMethodCtrl = handler.editConfigurationController(ureq, getWindowControl(), link);
		}

		if(editMethodCtrl != null) {
			listenTo(editMethodCtrl);

			String title = handler.getMethodName(getLocale());
			cmc = new CloseableModalController(getWindowControl(), translate("close"), editMethodCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		}
	}

	protected void addMethod(UserRequest ureq, AccessMethod method) {
		boolean confirmationEmail = confirmationEmailEl.isVisible() && confirmationEmailEl.isAtLeastSelected(1);
		Offer offer = acService.createOffer(resource, displayName);
		offer.setConfirmationEmail(confirmationEmail);
		OfferAccess link = acService.createOfferAccess(offer, method);

		removeAsListenerAndDispose(newMethodCtrl);
		AccessMethodHandler handler = acModule.getAccessMethodHandler(link.getMethod().getType());
		if (handler != null) {
			newMethodCtrl = handler.createConfigurationController(ureq, getWindowControl(), link);
		}
		if(newMethodCtrl != null) {
			listenTo(newMethodCtrl);

			String title = handler.getMethodName(getLocale());
			cmc = new CloseableModalController(getWindowControl(), translate("close"), newMethodCtrl.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		} else {
			OfferAccess newLink = acService.saveOfferAccess(link);
			addConfiguration(newLink);
		}
	}
	
	private void removeMethod(AccessInfo infos) {
		acService.deleteOffer(infos.getLink().getOffer());
		confControllers.remove(infos);
		updateConfirmationEmail();
	}
	
	private void setConfirmationEmail(boolean confirmationEmail) {
		for(AccessInfo info:confControllers) {
			Offer offer = info.getLink().getOffer();
			offer.setConfirmationEmail(confirmationEmail);
			offer = acService.save(offer);
		}
		dbInstance.commit();//make sure all is on the dabatase
		confControllers.clear();
		loadConfigurations();
	}

	public class AccessInfo {
		private String name;
		private String infos;
		private String dates;
		private OfferAccess link;
		private final boolean paymentMethod;

		public AccessInfo(String name, boolean paymentMethod, String infos, OfferAccess link) {
			this.name = name;
			this.paymentMethod = paymentMethod;
			this.infos = infos;
			this.link = link;
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
}