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
* <p>
*/
package org.olat.admin.registration;

import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.configuration.PersistedProperties;
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
	private TextElement summary, webSiteDescription;
	// where is your instance running, e.g. "Winterthurerstrasse 190, Zürich" or "Dresden"
	private TextElement locationBox;
	
	/**
	 * Constructor for the system registration controller.
	 * @param ureq
	 * @param control
	 */
	public SystemRegistrationAdminController(UserRequest ureq, WindowControl control) {
		super(ureq, control, "registration");
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SystemRegistrationManager sysRegMgr = SystemRegistrationManager.getInstance();
		PersistedProperties registrationConfig = sysRegMgr.getRegistrationConfiguration();
		//
		// Add statistics 
		//always send statistics
		this.flc.contextPut("isRegisteredStatistics", Boolean.valueOf(true));
		//
		// Add website
		publishWebSiteSelection = uifactory.addCheckboxesVertical("registration.publishWebSiteSelection", null,  formLayout,
				new String[] { YES }, new String[] { "" }, null, 1);
		publishWebSiteSelection.addActionListener(this, FormEvent.ONCLICK);
		boolean publishWebsiteCofig = registrationConfig.getBooleanPropertyValue(SystemRegistrationManager.CONF_KEY_PUBLISH_WEBSITE);
		publishWebSiteSelection.select(YES, publishWebsiteCofig);
		this.flc.contextPut("isRegisteredWeb", Boolean.valueOf(publishWebsiteCofig));
		
		// Add website description
		String webSiteDesc = registrationConfig.getStringPropertyValue(SystemRegistrationManager.CONF_KEY_WEBSITE_DESCRIPTION, true);
		webSiteDescription = uifactory.addTextAreaElement("registration.webSiteDescription", 5, 60, webSiteDesc, formLayout);
		webSiteDescription.addActionListener(this, FormEvent.ONCHANGE);
		this.flc.contextPut("webSiteURL", Settings.getServerContextPathURI());
		RulesFactory.createHideRule(publishWebSiteSelection, null, webSiteDescription, formLayout);
		RulesFactory.createShowRule(publishWebSiteSelection, YES, webSiteDescription, formLayout);
		// Add location input
		String location = registrationConfig.getStringPropertyValue(SystemRegistrationManager.CONF_KEY_LOCATION, true);
		locationBox = uifactory.addTextElement("registration.location", "registration.location", -1, location, formLayout);
		locationBox.setExampleKey("registration.location.example", null);
		String locationCSV = registrationConfig.getStringPropertyValue(SystemRegistrationManager.CONF_KEY_LOCATION_COORDS, false);
		if(locationCSV != null){
			this.flc.contextPut("locationCoordinates", locationCSV);
		}
		//
		// Add announce list
		addToAnnounceListSelection = uifactory.addCheckboxesVertical("registration.addToAnnounceListSelection", null, formLayout,
				new String[] { YES }, new String[] { "" }, null, 1);
		addToAnnounceListSelection.addActionListener(this, FormEvent.ONCLICK);
		addToAnnounceListSelection.select(YES, registrationConfig.getBooleanPropertyValue(SystemRegistrationManager.CONF_KEY_NOTIFY_RELEASES));
		//
		// Add email field
		String emailValue = registrationConfig.getStringPropertyValue(SystemRegistrationManager.CONF_KEY_EMAIL, true);
		email = uifactory.addTextElement("registration.email", "registration.email", 60, emailValue, formLayout);
		RulesFactory.createHideRule(addToAnnounceListSelection, null, email, formLayout);
		RulesFactory.createShowRule(addToAnnounceListSelection, YES, email, formLayout);
		//
		// Add summary field
		String summaryText = sysRegMgr.getRegistrationPropertiesMessage(registrationConfig.createPropertiesFromPersistedProperties());
		summaryText = StringEscapeUtils.escapeHtml(summaryText).toString();
		summary = uifactory.addTextAreaElement("registration.summary", null, -1, 5, 60, true, summaryText, formLayout);
		summary.setEnabled(false);
		//
		// Add submit button
		uifactory.addFormSubmitButton("save", formLayout);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		// Now collect valid data
		SystemRegistrationManager sysRegMgr = SystemRegistrationManager.getInstance();
		PersistedProperties registrationConfig = sysRegMgr.getRegistrationConfiguration();
	  //always send statistics
		this.flc.contextPut("isRegisteredStatistics", Boolean.valueOf(true));
		boolean publishWebsiteCofig = publishWebSiteSelection.isSelected(0);
		this.flc.contextPut("isRegisteredWeb", Boolean.valueOf(publishWebsiteCofig));
		registrationConfig.setBooleanProperty(SystemRegistrationManager.CONF_KEY_PUBLISH_WEBSITE, publishWebsiteCofig, false);
		String webSiteDesc = webSiteDescription.getValue();
		registrationConfig.setStringProperty(SystemRegistrationManager.CONF_KEY_WEBSITE_DESCRIPTION, webSiteDesc, false);
		if (MailHelper.isValidEmailAddress(email.getValue()) && StringHelper.containsNonWhitespace(email.getValue())) {
			boolean notifyConfig = addToAnnounceListSelection.isSelected(0);
			registrationConfig.setBooleanProperty(SystemRegistrationManager.CONF_KEY_NOTIFY_RELEASES, notifyConfig, false);
			registrationConfig.setStringProperty(SystemRegistrationManager.CONF_KEY_EMAIL, email.getValue(), false);			
		} else {
			registrationConfig.setBooleanProperty(SystemRegistrationManager.CONF_KEY_NOTIFY_RELEASES, false, false);			
		}
		
		//
		// Extract location -> uses http request for geolocation resolving
		String location = locationBox.getValue();
		
		registrationConfig.setStringProperty(SystemRegistrationManager.CONF_KEY_LOCATION, location, false);
		String locationCSV = SystemRegistrationManager.getInstance().getLocationCoordinates(location);
		if(locationCSV != null){
				registrationConfig.setStringProperty(SystemRegistrationManager.CONF_KEY_LOCATION_COORDS, locationCSV, false);
				this.flc.contextPut("locationCoordinates", locationCSV);
		}
		
		
		// Save to disk
		registrationConfig.savePropertiesAndFireChangedEvent();
		// Update summary view
		String summaryText = sysRegMgr.getRegistrationPropertiesMessage(registrationConfig.createPropertiesFromPersistedProperties());
		summaryText = StringEscapeUtils.escapeHtml(summaryText).toString();
		summary.setValue(summaryText);
		// Submit to olat.org
		sysRegMgr.sendRegistrationData();
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
		Properties tmp = new Properties();
		SystemRegistrationManager sysRegMgr = SystemRegistrationManager.getInstance();
		PersistedProperties registrationConfig = sysRegMgr.getRegistrationConfiguration();
		tmp.setProperty(SystemRegistrationManager.CONF_KEY_IDENTIFYER, registrationConfig.getStringPropertyValue(SystemRegistrationManager.CONF_KEY_IDENTIFYER, true));		
		boolean publishWebsiteCofig = publishWebSiteSelection.isSelected(0);
		tmp.setProperty(SystemRegistrationManager.CONF_KEY_PUBLISH_WEBSITE, publishWebsiteCofig + "");
		String webSiteDesc = webSiteDescription.getValue();
		tmp.setProperty(SystemRegistrationManager.CONF_KEY_WEBSITE_DESCRIPTION, webSiteDesc);
		if (MailHelper.isValidEmailAddress(email.getValue()) && StringHelper.containsNonWhitespace(email.getValue())) {
			boolean notifyConfig = addToAnnounceListSelection.isSelected(0);
			tmp.setProperty(SystemRegistrationManager.CONF_KEY_NOTIFY_RELEASES, notifyConfig + "");
			tmp.setProperty(SystemRegistrationManager.CONF_KEY_EMAIL, email.getValue());			
		} else {
			tmp.setProperty(SystemRegistrationManager.CONF_KEY_NOTIFY_RELEASES, false + "");			
		}
		// Update summary view
		String summaryText = sysRegMgr.getRegistrationPropertiesMessage(tmp);
		summaryText = StringEscapeUtils.escapeHtml(summaryText).toString();
		summary.setValue(summaryText);
	}
	

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing to dispose
	}

}
