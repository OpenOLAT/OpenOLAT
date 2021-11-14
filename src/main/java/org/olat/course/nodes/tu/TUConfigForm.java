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

package org.olat.course.nodes.tu;

import java.net.MalformedURLException;
import java.net.URL;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.StringHelper;
import org.olat.modules.ModuleConfiguration;

/**
 * Initial Date:  Oct 12, 2004
 *
 * @author Felix Jost
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class TUConfigForm extends FormBasicController {
	/** config option: password */
	public static final String CONFIGKEY_PASS = "pass";
	/** config option: username */
	public static final String CONFIGKEY_USER = "user";
	/** config option: port */
	public static final String CONFIGKEY_PORT = "port";
	/** config option: uri */
	public static final String CONFIGKEY_URI = "uri";
	/** config option: query */
	public static final String CONFIGKEY_QUERY = "query";
	/** config option: ref (part after the anchor) */
	public static final String CONFIGKEY_REF = "ref";
	/** config option: hostname */
	public static final String CONFIGKEY_HOST = "host";
	/** config option: protocol */
	public static final String CONFIGKEY_PROTO = "proto";
	
	 /** Configuration key: use tunnel for iframe or display directly ("<iframe src='www.ethz.ch'></iframe>"). Values: true, false **/
  public static final String CONFIG_TUNNEL = "useframetunnel"; // don't change value, used in config
  
  /** Configuration key: display content in iframe: Values: true, false **/
  public static final String CONFIG_IFRAME = "iniframe";
	
  /** Configuration key: display content in new browser window: Values: true, false **/
  public static final String CONFIG_EXTERN = "extern";

  
  /*
   *  They are only used inside this form and will not be saved
   *  anywhere, so feel free to change them...
   */ 
  
  private static final String OPTION_TUNNEL_THROUGH_OLAT_INLINE = "tunnelInline";
  private static final String OPTION_TUNNEL_THROUGH_OLAT_IFRAME = "tunnelIFrame";
  private static final String OPTION_SHOW_IN_OLAT_IN_AN_IFRAME  = "directIFrame";
  private static final String OPTION_SHOW_IN_NEW_BROWSER_WINDOW = "extern";

  
  /*
   * NLS support:
   */

  private static final String NLS_OPTION_TUNNEL_INLINE_LABEL			= "option.tunnel.inline.label";
  private static final String NLS_OPTION_TUNNEL_IFRAME_LABEL			= "option.tunnel.iframe.label";
  private static final String NLS_OPTION_OLAT_IFRAME_LABEL				= "option.olat.iframe.label";
  private static final String NLS_OPTION_EXTERN_PAGE_LABEL				= "option.extern.page.label";
  private static final String NLS_DESCRIPTION_LABEL								= "description.label";
  private static final String NLS_DESCRIPTION_PREAMBLE						= "description.preamble";
  private static final String NLS_DISPLAY_CONFIG_EXTERN 					= "display.config.extern";

  private ModuleConfiguration config;
	
	private TextElement thost;
	private TextElement tuser;
	private TextElement tpass;
	
	private SingleSelection selectables;
	private String[] selectableValues, selectableLabels;
	

	String user, pass;
	String fullURI;
	private MultipleSelectionElement checkboxPagePasswordProtected;
	/**
	 * Constructor for the tunneling configuration form
	 * @param name
	 * @param config
	 * @param withCancel
	 */
	public TUConfigForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl);
		this.config = config;
		int configVersion = config.getConfigurationVersion();
		
		String proto = (String)config.get(CONFIGKEY_PROTO);
		String host = (String)config.get(CONFIGKEY_HOST);
		String uri = (String)config.get(CONFIGKEY_URI);
		
		if (uri != null && uri.length() > 0 && uri.charAt(0) == '/')
			uri = uri.substring(1);
		String query = null;
		if (configVersion == 2) {
			//query string is available since config version 2
			query = (String) config.get(TUConfigForm.CONFIGKEY_QUERY);
		}
		String ref = config.getStringValue(TUConfigForm.CONFIGKEY_REF);
		Integer port = (Integer)config.get(CONFIGKEY_PORT);
		
		user = (String)config.get(CONFIGKEY_USER);
		pass = (String)config.get(CONFIGKEY_PASS);

		fullURI = getFullURL(proto, host, port, uri, query, ref).toString();
		
		selectableValues = new String[] { OPTION_TUNNEL_THROUGH_OLAT_IFRAME, OPTION_SHOW_IN_OLAT_IN_AN_IFRAME,
				OPTION_SHOW_IN_NEW_BROWSER_WINDOW, OPTION_TUNNEL_THROUGH_OLAT_INLINE };

		selectableLabels = new String[] { translate(NLS_OPTION_TUNNEL_IFRAME_LABEL), translate(NLS_OPTION_OLAT_IFRAME_LABEL),
				translate(NLS_OPTION_EXTERN_PAGE_LABEL), translate(NLS_OPTION_TUNNEL_INLINE_LABEL) };
		
		initForm (ureq);
	}

	public static StringBuilder getFullURL(String proto, String host, Integer port, String uri, String query, String ref) {
		StringBuilder fullURL = new StringBuilder();
		if (proto != null && host != null) {
			fullURL.append(proto).append("://");
			fullURL.append(host);
			if (port != null) {
				if (proto.equals("http") || proto.equals("https")) {
					if (proto.equals("http") && port.intValue() != 80) fullURL.append(":" + port);
					else if (proto.equals("https") && port.intValue() != 443) fullURL.append(":" + port);
				}	else fullURL.append(":" + port);
			}
			if (uri == null) {
				fullURL.append("/");
			} else {
				// append "/" if not already there, old configurations might have no "/" 
				if (uri.indexOf("/") != 0) fullURL.append("/");
				fullURL.append(uri);
			}
			if (query != null) {
				fullURL.append("?").append(query);
			}
			if(StringHelper.containsNonWhitespace(ref)) {
				fullURL.append("#").append(ref);
			}
		}
		return fullURL;
	}


	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		try {
			URL url = new URL(thost.getValue());
			allOk &= StringHelper.containsNonWhitespace(url.getHost());
		} catch (MalformedURLException e) {
			thost.setErrorKey("TUConfigForm.invalidurl", null);
			allOk &= false;
		}
		return allOk;
	}
	
	
	private String convertConfigToNewStyle(ModuleConfiguration cfg) {
		Boolean tunnel = cfg.getBooleanEntry(CONFIG_TUNNEL);
		Boolean iframe = cfg.getBooleanEntry(CONFIG_IFRAME);
		Boolean extern = cfg.getBooleanEntry(CONFIG_EXTERN);
		if (tunnel == null && iframe == null && extern == null) {				// nothing saved yet
			return OPTION_TUNNEL_THROUGH_OLAT_IFRAME;
		} else {																												// something is saved ...
			if (extern != null && extern.booleanValue()) {								// ... it was extern...
				return OPTION_SHOW_IN_NEW_BROWSER_WINDOW;
			} else if (tunnel != null && tunnel.booleanValue()) {					// ... it was tunneled
				if (iframe != null && iframe.booleanValue()) {							// ... and in a iframe
					return OPTION_TUNNEL_THROUGH_OLAT_IFRAME;
				} else {																										// ... no iframe
					return OPTION_TUNNEL_THROUGH_OLAT_INLINE;
				}
			} else {																											// ... no tunnel means inline
				return OPTION_SHOW_IN_OLAT_IN_AN_IFRAME;
			}
		}
	}
	
	/**
	 * @return the updated module configuration using the form data
	 */
	public ModuleConfiguration getUpdatedConfig() {
		URL url = null;
		try {
			url = new URL(thost.getValue());
		} catch (MalformedURLException e) {
			throw new OLATRuntimeException("MalformedURL in TUConfigForm which should not happen, since we've validated before. URL: " + thost.getValue(), e);
		}
		config.setConfigurationVersion(2);
		config.set(CONFIGKEY_PROTO, url.getProtocol());
		config.set(CONFIGKEY_HOST, url.getHost());
		config.set(CONFIGKEY_URI, url.getPath());
		config.set(CONFIGKEY_QUERY, url.getQuery());
		config.set(CONFIGKEY_REF, url.getRef());
		int portHere = url.getPort();
		config.set(CONFIGKEY_PORT, Integer.valueOf(portHere != -1 ? portHere : url.getDefaultPort()));
		config.set(CONFIGKEY_USER, getFormUser());
		config.set(CONFIGKEY_PASS, getFormPass());
		
		// now save new mapped config:
		String selected = selectables.getSelectedKey();
		
		// if content should be show in extern window
		config.setBooleanEntry(CONFIG_EXTERN, selected.equals(OPTION_SHOW_IN_NEW_BROWSER_WINDOW));
		// if content should be tunneled
		config.setBooleanEntry(CONFIG_TUNNEL, (selected.equals(OPTION_TUNNEL_THROUGH_OLAT_INLINE) || selected.equals(OPTION_TUNNEL_THROUGH_OLAT_IFRAME)));
		// if content should be displayed in iframe
		config.setBooleanEntry(CONFIG_IFRAME, (selected.equals(OPTION_TUNNEL_THROUGH_OLAT_IFRAME) || selected.equals(OPTION_SHOW_IN_OLAT_IN_AN_IFRAME)));			
		return config;
	}

	private String getFormUser() {
		if (StringHelper.containsNonWhitespace(tuser.getValue()))
			return tuser.getValue();
		else 
			return null;
	}

	private String getFormPass() {
		return tpass.getValue();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void initForm (FormItemContainer formLayout, Controller listener, UserRequest ureq) {
	
		thost = uifactory.addTextElement("st", "TUConfigForm.url", 255, fullURI, formLayout);
		thost.setExampleKey("form.url.example", null);
		thost.setMandatory(true);
		
		uifactory.addStaticTextElement("expl", NLS_DESCRIPTION_LABEL, translate(NLS_DESCRIPTION_PREAMBLE), formLayout);

		String loadedConfig = convertConfigToNewStyle(config);
		selectables = uifactory.addRadiosVertical("selectables", NLS_DISPLAY_CONFIG_EXTERN, formLayout, selectableValues, selectableLabels);
		selectables.select(loadedConfig, true);
		selectables.addActionListener(FormEvent.ONCLICK);
		
		checkboxPagePasswordProtected = uifactory.addCheckboxesHorizontal("checkbox", "TUConfigForm.protected", formLayout, new String[] { "ison" }, new String[] { "" });
		
		checkboxPagePasswordProtected.select("ison", (user != null) && !user.equals(""));
		// register for on click event to hide/disable other elements
		checkboxPagePasswordProtected.addActionListener(FormEvent.ONCLICK);
		
		tuser = uifactory.addTextElement("user", "TUConfigForm.user", 255, user == null ? "" : user, formLayout);
		tpass = uifactory.addPasswordElement("pass", "TUConfigForm.pass", 255, pass == null ? "" : pass, formLayout);
		tpass.setAutocomplete("new-password");
		
		uifactory.addFormSubmitButton("submit", formLayout);
		
		update();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		update();
	}
	
	private void update () {
		// Checkbox 'page password protected' only visible when OPTION_TUNNEL_THROUGH_OLAT_INLINE or OPTION_TUNNEL_THROUGH_OLAT_IFRAME
		checkboxPagePasswordProtected.setVisible( selectables.isSelected(0) || selectables.isSelected(3) );
		if (checkboxPagePasswordProtected.isSelected(0) && checkboxPagePasswordProtected.isVisible()) {
			tuser.setVisible(true);
			tpass.setVisible(true);
		} else {
			tuser.setValue("");
			tuser.setVisible(false);
			tpass.setValue("");
			tpass.setVisible(false);
		}
	}
}