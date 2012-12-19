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
package org.olat.admin.sysinfo;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.LogFileParser;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 16.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ErrorSearchController extends FormBasicController {

	private TextElement errorNumberEl;
	private DateChooser dateChooserEl;
	private VelocityContainer errorCont;
	
	public ErrorSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "errors");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("error.title");

		FormLayoutContainer fieldsCont = FormLayoutContainer.createDefaultFormLayout("fields", getTranslator());
		formLayout.add(fieldsCont);
		formLayout.add("fields", fieldsCont);
		
		errorNumberEl = uifactory.addTextElement("error.number", "error.number", 32, "", fieldsCont);
		//errorNumberEl.setExampleKey(exampleKey, params)
		//myErrors.contextPut("example_error", Settings.getNodeInfo()  + "-E12 "+ Settings.getNodeInfo()  + "-E64..." );
		dateChooserEl = uifactory.addDateChooser("error.date", "error.date", null, fieldsCont);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		fieldsCont.add(buttonCont);
		uifactory.addFormSubmitButton("search", "error.retrieve", buttonCont);
		
		if(formLayout instanceof FormLayoutContainer) {
			errorCont = createVelocityContainer("error_list");
			((FormLayoutContainer)formLayout).put("errors", errorCont);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String errorNr = errorNumberEl.getValue();
		Date date = dateChooserEl.getDate();
		if(date != null && StringHelper.containsNonWhitespace(errorNr)) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			String err_dd = Integer.toString(cal.get(Calendar.DATE));
			String err_mm = Integer.toString(cal.get(Calendar.MONTH) + 1);
			String err_yyyy = Integer.toString(cal.get(Calendar.YEAR));
			errorCont.contextPut("highestError", Tracing.getErrorCount());
			errorCont.contextPut("errormsgs", LogFileParser.getError(errorNr, err_dd, err_mm, err_yyyy, true));
		}
	}


	
	
/*
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == myErrors) {
			HttpServletRequest hreq = ureq.getHttpReq();
			err_nr = hreq.getParameter("mynr");
			if (hreq.getParameter("mydd") != null)
				err_dd = hreq.getParameter("mydd");
			if (hreq.getParameter("mymm") != null)
				err_mm = hreq.getParameter("mymm");
			if (hreq.getParameter("myyyyy") != null)
				err_yyyy = hreq.getParameter("myyyyy");
			if (err_nr != null) {
				myErrors.contextPut("mynr", err_nr);
				myErrors.contextPut("errormsgs", LogFileParser.getError(err_nr, err_dd, err_mm, err_yyyy, true));
			}

			myErrors.contextPut("highestError", Tracing.getErrorCount());
			myErrors.contextPut("mydd", err_dd);
			myErrors.contextPut("mymm", err_mm);
			myErrors.contextPut("myyyyy", err_yyyy);
			myErrors.contextPut("olat_formatter", Formatter.getInstance(ureq.getLocale()));
		} 
	}*/

}