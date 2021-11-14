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
package org.olat.user.propertyhandlers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.user.propertyhandlers.ui.UsrPropHandlerCfgController;

/**
 * <h3>Description:</h3> The YearPropertyHandlerController displays a simple form for the admin to enter two values:  from and to<br />
 * These values can either be years (1998)  or time-spans like  "+4"  or "-5".<br />
 * the <code>YearPropertyHandler</code> will render a dropdown with years according to this config
 * <p>
 * 
 * Initial Date: 15.12.2011 <br>
 * 
 * @author strentini, sergio.trentini@frentix.com, www.frentix.com
 */
public class YearPropertyHandlerController extends FormBasicController implements UsrPropHandlerCfgController {

	private YearPropertyHandler handler2Configure;

	private TextElement txeFrom;
	private TextElement txeTo;

	public YearPropertyHandlerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		this.initForm(ureq);
	}

	@Override
	public void setHandlerToConfigure(UserPropertyHandler handler) {
		if (handler instanceof YearPropertyHandler) {
			this.handler2Configure = (YearPropertyHandler) handler;
			Map<String, String> handlerConfig = handler2Configure.getHandlerConfigFactory().loadConfigForHandler(handler2Configure);
			if (handlerConfig.containsKey(YearPropertyHandler.PROP_FROM) && handlerConfig.containsKey(YearPropertyHandler.PROP_TO)) {
				txeFrom.setValue(handlerConfig.get(YearPropertyHandler.PROP_FROM));
				txeTo.setValue(handlerConfig.get(YearPropertyHandler.PROP_TO));
			}
		} else {
			throw new RuntimeException("given Handler must be of type 'YearPropertyHandler'");
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		
		// let's check if given values are either valid integers or start with + / - and are followed by a valid integer
		 // ( we do not check here if "TO" is bigger than "FROM )
		
		String fV = txeFrom.getValue();
		String tV = txeTo.getValue();
		
		if (StringUtils.isBlank(fV) && StringUtils.isBlank(tV))
			return true;// both fields empty, that is ok, we'll use default cfg

		try {
			if (fV.startsWith("+") || fV.startsWith("-")) {
				fV = fV.substring(1);
			}
			Integer.parseInt(fV);
		} catch (NumberFormatException e) {
			txeFrom.setErrorKey("yph.err", null);
			return false;
		}

		try {
			if (tV.startsWith("+") || tV.startsWith("-")) {
				tV = tV.substring(1);
			}
			Integer.parseInt(tV);
		} catch (NumberFormatException e) {
			txeTo.setErrorKey("yph.err", null);
			return false;
		}
		
		return true;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("yph.infomsg", null, translate("yph.infomsg"), formLayout);
		txeFrom = uifactory.addTextElement("yph.from", "yph.from", 4, "", formLayout);
		txeTo = uifactory.addTextElement("yph.to", "yph.to", 4, "", formLayout);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("ok_cancel", getTranslator());
		buttonLayout.setRootForm(mainForm);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		formLayout.add(buttonLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {

		// let's save the config
		String fV = txeFrom.getValue();
		String tV = txeTo.getValue();
		if (!StringUtils.isBlank(fV) || !StringUtils.isBlank(tV)) {
			Map<String, String> handlerConfig = new HashMap<>();
			handlerConfig.put(YearPropertyHandler.PROP_FROM, fV);
			handlerConfig.put(YearPropertyHandler.PROP_TO, tV);
			handler2Configure.getHandlerConfigFactory().saveConfigForHandler(handler2Configure, handlerConfig);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
