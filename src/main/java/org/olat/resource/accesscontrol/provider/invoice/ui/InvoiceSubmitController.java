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
package org.olat.resource.accesscontrol.provider.invoice.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ui.AccessEvent;

/**
 * 
 * Initial date: 6 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InvoiceSubmitController extends FormBasicController implements Controller {
	
	private CloseableModalController cmc;
	private InvoiceSubmitDetailsController detailsCtrl;

	private OfferAccess link;
	private final Identity bookedIdentity;

	public InvoiceSubmitController(UserRequest ureq, WindowControl wControl, OfferAccess link, Identity bookedIdentity) {
		super(ureq, wControl, "submit");
		this.link = link;
		this.bookedIdentity = bookedIdentity;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormSubmit submitButton = uifactory.addFormSubmitButton("access.button", formLayout);
		submitButton.setElementCssClass("o_button_call_to_action");
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (detailsCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if (event instanceof AccessEvent) {
				fireEvent(ureq, event);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(detailsCtrl);
		removeAsListenerAndDispose(cmc);
		detailsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doOpenDetailsSubmit(ureq);
	}

	private void doOpenDetailsSubmit(UserRequest ureq) {
		if (guardModalController(detailsCtrl)) return;
		
		detailsCtrl = new InvoiceSubmitDetailsController(ureq, getWindowControl(), link, bookedIdentity);
		listenTo(detailsCtrl);
		
		String title = translate("access.invoice.details.title", StringHelper.escapeHtml(link.getOffer().getResourceDisplayName()));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), detailsCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}

}
