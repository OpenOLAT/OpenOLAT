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
package org.olat.resource.accesscontrol.ui.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.CachedRunContextController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.resource.accesscontrol.provider.invoice.ui.InvoiceSubmitDetailsController;

/**
 * Wizard step wrapping one {@link InvoiceSubmitDetailsController}, kept
 * alive across back and forward navigation via {@link CachedRunContextController}.
 *
 * Initial date: 3 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BookingDetailsStepController extends StepFormBasicController {

	private static final String CACHE_KEY = "invoiceDetails";

	private final CachedRunContextController<InvoiceSubmitDetailsController> detailsCtrl;

	public BookingDetailsStepController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			BookingContext bookingContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		detailsCtrl = CachedRunContextController.of(runContext, CACHE_KEY,
				() -> new InvoiceSubmitDetailsController(ureq, wControl, bookingContext, rootForm), this);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//
	}

	@Override
	public FormItem getStepFormItem() {
		return detailsCtrl.getStepFormItem();
	}

	@Override
	public void back() {
		// Nothing to commit: the values stay in detailsCtrl, which is kept
		// alive across navigation, see CachedRunContextController.
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void doDispose() {
		detailsCtrl.release(this);
		super.doDispose();
	}

}
