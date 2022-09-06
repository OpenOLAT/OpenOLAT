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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MethodSelectionController extends FormBasicController {
	
	public final static String KEY_OPEN_ACCESS = "open.access";
	public final static String KEY_GUEST_ACCESS = "guest.access";

	private SingleSelection methodEl;
	
	private final boolean openAccessSupported;
	private final boolean guestSupported;
	private final List<AccessMethod> methods;

	@Autowired
	private AccessControlModule acModule;

	public MethodSelectionController(UserRequest ureq, WindowControl wControl, boolean openAccessSupported, boolean guestSupported, List<AccessMethod> methods) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.openAccessSupported = openAccessSupported;
		this.guestSupported = guestSupported;
		this.methods = methods;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("create.offer.first", null, translate("create.offer.first"), formLayout);
		
		SelectionValues methodSV = new SelectionValues();
		
		for (AccessMethod method:methods) {
			AccessMethodHandler handler = acModule.getAccessMethodHandler(method.getType());
			methodSV.add(new SelectionValue(handler.getType(), handler.getMethodName(getLocale()),
					handler.getDescription(getLocale()), "o_icon " + method.getMethodCssClass() + "_icon",
					null, true));
		}
		
		if (openAccessSupported) {
			methodSV.add(new SelectionValue(KEY_OPEN_ACCESS, translate("create.offer.open"),
					translate("create.offer.open.desc"), "o_icon o_ac_openaccess_icon", null, true));
		}
		
		if (guestSupported) {
			methodSV.add(new SelectionValue(KEY_GUEST_ACCESS, translate("create.offer.guest"),
					translate("create.offer.guest.desc"), "o_icon o_ac_guests_icon", null, true));
		}
		
		methodEl = uifactory.addCardSingleSelectHorizontal("create.offer.method", null, formLayout, methodSV.keys(),
				methodSV.values(), methodSV.descriptions(), methodSV.icons());
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonsCont.setElementCssClass("o_button_group o_button_group_right o_button_group_bottom");
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("create", buttonsCont);
	}

	public String getSelectedType() {
		return methodEl.getSelectedKey();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
