/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.commons.calendar.ui;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ImportCalendarManager;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Initial Date:  4 feb. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class ImportCalendarController extends FormBasicController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(CalendarManager.class);
	
	private FormLink importTypeUrlButton;
	private FormLink importTypeFileButton;
	private FormSubmit importButton;
	private TextElement importUrl;
	private FileElement importFile;
	private final KalendarRenderWrapper calendarWrapper;
	
	private FormLayoutContainer urlLayout;
	private FormLayoutContainer fileLayout;
	private FormLayoutContainer chooseLayout;

	public ImportCalendarController(UserRequest ureq, WindowControl wControl, KalendarRenderWrapper calendarWrapper) {
		super(ureq, wControl, "importEvents");
		this.calendarWrapper = calendarWrapper;
		
		initForm(ureq);
	}

	@Override
	protected void constructorInit(String id, String pageName) {
		velocity_root = VELOCITY_ROOT;
		super.constructorInit(id, pageName);
		setBasePackage(CalendarManager.class);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//choose panel
		chooseLayout = FormLayoutContainer.createVerticalFormLayout("chooseContainer", getTranslator());
		chooseLayout.setRootForm(mainForm);
		uifactory.addStaticTextElement("cal.import.type.file.desc", "", chooseLayout);
		importTypeFileButton = uifactory.addFormLink("cal.import.type.file", chooseLayout, Link.BUTTON);
		uifactory.addSpacerElement("choose-spacer", chooseLayout, false);
		uifactory.addStaticTextElement("cal.import.type.url.desc", "", chooseLayout);
		importTypeUrlButton = uifactory.addFormLink("cal.import.type.url", chooseLayout, Link.BUTTON);
		chooseLayout.setVisible(true);
		formLayout.add("chooseContainer", chooseLayout);
		
		//url panel
		urlLayout = FormLayoutContainer.createDefaultFormLayout("urlContainer", getTranslator());
		urlLayout.setRootForm(mainForm);
		importUrl = uifactory.addTextElement("cal.import.url.prompt", "cal.import.url.prompt", 200, "", urlLayout);
		urlLayout.setVisible(false);
		formLayout.add("urlContainer", urlLayout);
		
		//file panel
		fileLayout = FormLayoutContainer.createDefaultFormLayout("fileContainer", getTranslator());
		fileLayout.setRootForm(mainForm);
		importFile = uifactory.addFileElement("cal.import.form.prompt", "cal.import.form.prompt", fileLayout);
		fileLayout.setVisible(false);
		formLayout.add("fileContainer", fileLayout);

		//standard cancel panel
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		formLayout.add("buttonGroupLayout", buttonGroupLayout);
		importButton = uifactory.addFormSubmitButton("ok", buttonGroupLayout);
		importButton.setVisible(false);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		importUrl.clearError();
		if(urlLayout.isVisible()) {
			String url = importUrl.getValue();
			if(StringHelper.containsNonWhitespace(url)) {
				try {
					String host = new URL(url).getHost();
					if(host == null) {
						importUrl.setErrorKey("cal.import.url.invalid", null);
					}
				} catch (MalformedURLException e) {
					importUrl.setErrorKey("cal.import.url.invalid", null);
					allOk &= false;
				}
			} else {
				importUrl.setErrorKey("cal.import.url.empty.error", null);
				allOk &= false;
			}
		}
		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(urlLayout.isVisible()) {
			String url = importUrl.getValue();
			if(ImportCalendarManager.importCalendarIn(calendarWrapper.getKalendar(), url)) {
				showInfo("cal.import.success");
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				showError("cal.import.url.content.invalid");
			}
		} else if(fileLayout.isVisible()) {
			InputStream in = importFile.getUploadInputStream();
			if(ImportCalendarManager.importCalendarIn(calendarWrapper, in)) {
				showInfo("cal.import.success");
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				showError("cal.import.form.format.error");
			}
			FileUtils.closeSafely(in);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == importTypeFileButton) {
			chooseLayout.setVisible(false);
			fileLayout.setVisible(true);
			importButton.setVisible(true);
		} else if (source == importTypeUrlButton) {
			chooseLayout.setVisible(false);
			urlLayout.setVisible(true);
			importButton.setVisible(true);
		}
		super.formInnerEvent(ureq, source, event);
	}
}
