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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.AccessTransaction;
import org.olat.resource.accesscontrol.Order;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.ui.OrderDetailController.OrderItemWrapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * show the details of the OLAT transaction, plus look up a specific
 * controller for the ugly details of a PSP transaction as Paypal
 * 
 * <P>
 * Initial Date:  27 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class TransactionDetailsController extends FormBasicController {
	
	private final Order order;
	private final OrderItemWrapper wrapper;
	
	@Autowired
	private AccessControlModule acModule;
	
	public TransactionDetailsController(UserRequest ureq, WindowControl wControl, Order order, OrderItemWrapper wrapper) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		this.order = order;
		this.wrapper = wrapper;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String page = velocity_root + "/transaction_details.html";
		FormLayoutContainer detailsLayout = FormLayoutContainer.createCustomFormLayout("transaction-details-layout", getTranslator(), page);
		formLayout.add(detailsLayout);
		detailsLayout.setRootForm(mainForm);
		
		AccessTransaction transaction = wrapper.getTransaction();
		DetailsForm detailsForm = new DetailsForm(ureq, getWindowControl(), transaction, mainForm);
		detailsLayout.add("simple", detailsForm.getInitialFormItem());

		AccessMethod method = transaction.getMethod();
		AccessMethodHandler handler = acModule.getAccessMethodHandler(method.getType());
		FormController controller = handler.createTransactionDetailsController(ureq, getWindowControl(), order, wrapper.getPart(), method, mainForm);
		if(controller != null) {
			uifactory.addSpacerElement("details-spacer", detailsLayout, false);
			detailsLayout.add("custom", controller.getInitialFormItem());
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private class DetailsForm extends FormBasicController implements FormController {
		
		private final AccessTransaction transaction;
	
		public DetailsForm(UserRequest ureq, WindowControl wControl, AccessTransaction transaction, Form form) {
			super(ureq, wControl, LAYOUT_DEFAULT, null, form);
			
			this.transaction = transaction;
			
			initForm(ureq);
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			Date date = transaction.getCreationDate();
			String dateStr = Formatter.getInstance(getLocale()).formatDateAndTime(date);
			uifactory.addStaticTextElement("transaction.date", dateStr, formLayout);
		}

		@Override
		public FormItem getInitialFormItem() {
			return flc;
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
}
