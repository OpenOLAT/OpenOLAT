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
*/
package org.olat.admin.registration;

import java.util.Map;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.rules.RulesFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;

/**
 * Description:<br>
 * The system registration controller allows the administrator to enable or
 * disable elements from the system registration process:
 * <ul>
 * <li>anonymous statistics about the server setup. enabled by default (opt-out)
 * </li>
 * <li>register installation on olat.org (opt-in)</li>
 * <li>register email address (opt-in)</li>
 * </ul>
 * <p>
 * The configuration is stored in
 * <code>olatdata/system/config/org.olat.admin.registration.SystemRegistrationAdminController.properties</code>
 * <P>
 * Initial Date: 11.12.2008 <br>
 * 
 * @author gnaegi
 */
public class SystemRegistrationAdminController extends FormBasicController {
	private static final String YES = "yes";
	// personal information
	private MultipleSelectionElement addToAnnounceListSelection;
	private TextElement email;
	private MultipleSelectionElement publishWebSiteSelection;
	
	// summary of data that will be sent to server
	private TextElement webSiteDescription;
	private TextElement locationBox;
	
	private final SystemRegistrationModule registrationModule;
	private final SystemRegistrationManager registrationManager;
	
	/**
	 * Constructor for the system registration controller.
	 * @param ureq
	 * @param control
	 */
	public SystemRegistrationAdminController(UserRequest ureq, WindowControl control) {
		super(ureq, control, "registration");
		
		registrationModule = (SystemRegistrationModule)CoreSpringFactory.getBean("systemRegistrationModule");
		registrationManager = (SystemRegistrationManager)CoreSpringFactory.getBean("systemRegistrationManager");
		
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//always send statistics
		flc.contextPut("isRegisteredStatistics", Boolean.valueOf(true));
		//
		// Add website
		publishWebSiteSelection = uifactory.addCheckboxesVertical("registration.publishWebSiteSelection", null,  formLayout,
				new String[] { YES }, new String[] { "" }, null, 1);
		publishWebSiteSelection.addActionListener(FormEvent.ONCLICK);
		boolean publishWebsiteConfig = registrationModule.isPublishWebsite();
		publishWebSiteSelection.select(YES, publishWebsiteConfig);
		flc.contextPut("isRegisteredWeb", Boolean.valueOf(publishWebsiteConfig));
		
		// Add website description
		String webSiteDesc = registrationModule.getWebsiteDescription();
		webSiteDescription = uifactory.addTextAreaElement("registration.webSiteDescription", 5, 60, webSiteDesc, formLayout);
		webSiteDescription.addActionListener(FormEvent.ONCHANGE);
		flc.contextPut("webSiteURL", Settings.getServerContextPathURI());
		RulesFactory.createHideRule(publishWebSiteSelection, null, webSiteDescription, formLayout);
		RulesFactory.createShowRule(publishWebSiteSelection, YES, webSiteDescription, formLayout);
		// Add location input
		String location = registrationModule.getLocation();
		locationBox = uifactory.addTextElement("registration.location", "registration.location", -1, location, formLayout);
		locationBox.setExampleKey("registration.location.example", null);
		String locationCSV = registrationModule.getLocationCoordinates();
		if(locationCSV != null){
			flc.contextPut("locationCoordinates", locationCSV);
		}
		//
		// Add announce list
		addToAnnounceListSelection = uifactory.addCheckboxesVertical("registration.addToAnnounceListSelection", null, formLayout,
				new String[] { YES }, new String[] { "" }, null, 1);
		addToAnnounceListSelection.addActionListener(FormEvent.ONCLICK);
		addToAnnounceListSelection.select(YES, registrationModule.isNotifyReleases());
		//
		// Add email field
		String emailValue = registrationModule.getEmail();
		email = uifactory.addTextElement("registration.email", "registration.email", 60, emailValue, formLayout);
		RulesFactory.createHideRule(addToAnnounceListSelection, null, email, formLayout);
		RulesFactory.createShowRule(addToAnnounceListSelection, YES, email, formLayout);
		
		Set<Map.Entry<String,String>> properties = registrationManager.getRegistrationPropertiesMessage().entrySet();
		flc.contextPut("properties", properties);

		// Add submit button
		uifactory.addFormSubmitButton("save", formLayout);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		//always send statistics
		flc.contextPut("isRegisteredStatistics", Boolean.valueOf(true));
		boolean publishWebsiteCofig = publishWebSiteSelection.isSelected(0);
		flc.contextPut("isRegisteredWeb", Boolean.valueOf(publishWebsiteCofig));
		
		registrationModule.setPublishWebsite(publishWebsiteCofig);
		String webSiteDesc = webSiteDescription.getValue();
		registrationModule.setWebsiteDescription(webSiteDesc);
		if (MailHelper.isValidEmailAddress(email.getValue()) && StringHelper.containsNonWhitespace(email.getValue())) {
			boolean notifyConfig = addToAnnounceListSelection.isSelected(0);
			registrationModule.setNotifyReleases(notifyConfig);
			registrationModule.setEmail(email.getValue());		
		} else {
			registrationModule.setNotifyReleases(false);		
		}
		
		//
		// Extract location -> uses http request for geolocation resolving
		String location = locationBox.getValue();
		registrationModule.setLocation(location);
		String locationCSV = registrationManager.getLocationCoordinates(location);
		if(locationCSV != null){
			registrationModule.setLocationCoordinates(locationCSV);
			flc.contextPut("locationCoordinates", locationCSV);
		}

		// Submit to olat.org
		registrationManager.send();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {				
		// First check for valid email address
		if (source == email && addToAnnounceListSelection.isSelected(0)) {
			if (!MailHelper.isValidEmailAddress(email.getValue()) || !StringHelper.containsNonWhitespace(email.getValue())) {
				email.setErrorKey("registration.email.error", null);
			}
		}
		// Now collect temporary valid data
		registrationModule.setPublishWebsite(publishWebSiteSelection.isSelected(0));
		registrationModule.setWebsiteDescription(webSiteDescription.getValue());
		if (MailHelper.isValidEmailAddress(email.getValue()) && StringHelper.containsNonWhitespace(email.getValue())) {
			boolean notifyConfig = addToAnnounceListSelection.isSelected(0);
			registrationModule.setNotifyReleases(notifyConfig);
			registrationModule.setEmail(email.getValue());		
		} else {
			registrationModule.setNotifyReleases(false);			
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing to dispose
	}
}