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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.Offer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GuestOfferController extends FormBasicController {
	
	private static final String CATALOG_WEB = "web";
	
	private TextElement descEl;
	private MultipleSelectionElement catalogEl;

	private final Offer offer;
	private final CatalogInfo catalogInfo;
	private final boolean edit;

	@Autowired
	private CatalogV2Module catalogModule;
	
	public GuestOfferController(UserRequest ureq, WindowControl wControl, Offer offer, CatalogInfo catalogInfo, boolean edit) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.offer = offer;
		this.catalogInfo = catalogInfo;
		this.edit = edit;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("generalCont", getTranslator());
		generalCont.setElementCssClass("o_sel_accesscontrol_guest_form");
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);

		descEl = uifactory.addTextAreaElement("offer-desc", "offer.description", 2000, 6, 80, false, false, offer.getDescription(), generalCont);
		descEl.setElementCssClass("o_sel_accesscontrol_description");
		descEl.setHelpTextKey("offer.description.help", null);

		FormLayoutContainer catalogCont = FormLayoutContainer.createDefaultFormLayout("catalogCont", getTranslator());
		catalogCont.setFormTitle(translate("offer.catalog.title"));
		catalogCont.setRootForm(mainForm);
		formLayout.add(catalogCont);

		SelectionValues catalogSV = new SelectionValues();
		if (catalogModule.isEnabled() && catalogModule.isWebPublishEnabled()) {
			catalogSV.add(SelectionValues.entry(CATALOG_WEB, translate("offer.publish.in.extern"), null, "o_icon o_icon-fw o_icon_catalog_extern", null, true));
		}
		catalogEl = uifactory.addCheckboxesButtonGroup("offer.publish.in", "offer.publish.in", catalogCont, catalogSV);
		catalogEl.setElementCssClass("o_sel_accesscontrol_catalog");
		catalogEl.select(CATALOG_WEB, offer != null && offer.isCatalogWebPublish());
		catalogEl.setVisible(catalogInfo.isCatalogSupported() && !catalogEl.getKeys().isEmpty());

		uifactory.addStaticTextElement("offer.available.if", catalogInfo.getStatusPeriodOption(), catalogCont);

		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("buttonsWrapper", getTranslator());
		buttonsWrapperCont.setElementCssClass("o_sel_accesscontrol_buttons");
		buttonsWrapperCont.setRootForm(mainForm);
		formLayout.add(buttonsWrapperCont);
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		buttonsWrapperCont.add(buttonGroupLayout);

		if(edit) {
			uifactory.addFormSubmitButton("save", buttonGroupLayout);
		} else {
			uifactory.addFormSubmitButton("create", buttonGroupLayout);
		}
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public Offer getOffer() {
		offer.setGuestAccess(true);
		offer.setDescription(descEl.getValue());
		offer.setCatalogWebPublish(catalogEl.isKeySelected(CATALOG_WEB));
		return offer;
	}

}
