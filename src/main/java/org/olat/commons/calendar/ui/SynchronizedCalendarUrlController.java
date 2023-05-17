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
package org.olat.commons.calendar.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.manager.ImportToCalendarManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SynchronizedCalendarUrlController extends FormBasicController {

	private TextElement importUrl;
	private final CalendarPersonalConfigurationRow row;

	@Autowired
	private ImportToCalendarManager importToCalendarManager;

	public SynchronizedCalendarUrlController(UserRequest ureq, WindowControl wControl, CalendarPersonalConfigurationRow row) {
		super(ureq, wControl);
		this.row = row;
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("cal.synchronize.type.url.desc");
		
		//choose panel
		importUrl = uifactory.addTextElement("cal.import.url.prompt", "cal.import.url.prompt", 200, "", formLayout);

		//standard cancel panel
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		formLayout.add("buttonGroupLayout", buttonGroupLayout);
		uifactory.addFormSubmitButton("ok", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		importUrl.clearError();
		String url = importUrl.getValue();
		if(StringHelper.containsNonWhitespace(url)) {
			try {
				String host = new URL(url).getHost();
				if(host == null) {
					importUrl.setErrorKey("cal.import.url.invalid");
				}
			} catch (MalformedURLException e) {
				importUrl.setErrorKey("cal.import.url.invalid");
				allOk &= false;
			}
		} else {
			importUrl.setErrorKey("cal.import.url.empty.error");
			allOk &= false;
		}

		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String url = importUrl.getValue();
		if(importToCalendarManager.importCalendarIn(row.getWrapper().getKalendar(), url)) {
			showInfo("cal.import.success");
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			showError("cal.import.url.content.invalid");
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
