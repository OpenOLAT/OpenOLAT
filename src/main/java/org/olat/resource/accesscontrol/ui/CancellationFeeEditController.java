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
package org.olat.resource.accesscontrol.ui;

import java.math.BigDecimal;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Mar 6, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CancellationFeeEditController extends FormBasicController {

	private TextElement cancellationFeeEl;
	
	private Order order;
	
	@Autowired
	private ACService acService;

	public CancellationFeeEditController(UserRequest ureq, WindowControl wControl, Order order) {
		super(ureq, wControl);
		this.order = order;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String cancellationFeeOriginal = PriceFormat.fullFormat(order.getCancellationFeesLines());
		uifactory.addStaticTextElement("cancellation.fee.original", "order.cancellation.fee.original", cancellationFeeOriginal, formLayout);
		
		String cancellationFee = null;
		if (order.getCancellationFees()!= null && order.getCancellationFees().getAmount() != null) {
			cancellationFee = PriceFormat.format(order.getCancellationFees().getAmount());
		}
		cancellationFeeEl = uifactory.addTextElement("order.cancellation.fee", 10, cancellationFee, formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		cancellationFeeEl.clearError();
		if (StringHelper.containsNonWhitespace(cancellationFeeEl.getValue())) {
			try {
				Double.parseDouble(cancellationFeeEl.getValue());
			} catch (NumberFormatException e) {
				cancellationFeeEl.setErrorKey("form.error.nofloat");
				allOk &= false;
			}
		} else {
			cancellationFeeEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		order = acService.loadOrderByKey(order.getKey());
		order.setCancellationFees(new PriceImpl(new BigDecimal(cancellationFeeEl.getValue()), order.getCancellationFeesLines().getCurrencyCode()));
		order = acService.updateOrder(order);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
