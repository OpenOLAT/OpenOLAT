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

package org.olat.course.nodes.basiclti;

import java.net.MalformedURLException;
import java.net.URL;

import org.olat.core.gui.UserRequest;

import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.StringHelper;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<BR/>
 * TODO: Class Description for LTConfigForm
 * <P/>
 *
* @author guido
 * @author Charles Severance
 */
public class LTIConfigForm extends FormBasicController {

	public static final String CONFIGKEY_PASS = "pass";
	public static final String CONFIGKEY_KEY = "key";
	public static final String CONFIGKEY_PORT = "port";
	public static final String CONFIGKEY_URI = "uri";
	public static final String CONFIGKEY_QUERY = "query";
	public static final String CONFIGKEY_HOST = "host";
	public static final String CONFIGKEY_PROTO = "proto";

	public static final String[] PROTOCOLS = new String[] {"http", "https"};

  public static final String CONFIG_KEY_DEBUG = "debug";
  public static final String CONFIG_KEY_CUSTOM = "custom";
  public static final String CONFIG_KEY_SENDNAME = "sendname";
  public static final String CONFIG_KEY_SENDEMAIL = "sendemail";
	
	private ModuleConfiguration config;
	
	private TextElement thost;
	private TextElement tkey;
	private TextElement tpass;
	
	private SelectionElement sendName;
	private SelectionElement sendEmail;
	private SelectionElement doDebug;
	
	private TextElement tcustom;
	
	private String fullURI;
	private String customConfig;
	private Boolean sendNameConfig;
	private Boolean sendEmailConfig;
	private Boolean doDebugConfig;
	private String key, pass;
	
	/**
	 * Constructor for the tunneling configuration form
	 * @param name
	 * @param config
	 * @param withCancel
	 */
	public LTIConfigForm(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
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
			query = (String) config.get(LTIConfigForm.CONFIGKEY_QUERY);
		}
		Integer port = (Integer)config.get(CONFIGKEY_PORT);
		
		key = (String)config.get(CONFIGKEY_KEY);
		if (key == null) key = "";
		
		pass = (String)config.get(CONFIGKEY_PASS);
		if (pass == null) pass = "";
		
		fullURI = getFullURL(proto, host, port, uri, query).toString();
		
		sendNameConfig = config.getBooleanEntry(CONFIG_KEY_SENDNAME);
    if (sendNameConfig == null) sendNameConfig = Boolean.FALSE;

		sendEmailConfig = config.getBooleanEntry(CONFIG_KEY_SENDEMAIL);
    if (sendEmailConfig == null) sendEmailConfig = Boolean.FALSE;

		customConfig = (String) config.get(CONFIG_KEY_CUSTOM);
    if (customConfig == null) customConfig = " ";

		doDebugConfig = config.getBooleanEntry(CONFIG_KEY_DEBUG);
    if (doDebugConfig == null) doDebugConfig = Boolean.FALSE;

    initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("form.title");
		setFormContextHelp("org.olat.course.nodes.basiclti","ced-lti-conf.html","help.hover.lt.conf");
			
		thost = uifactory.addTextElement("host", "LTConfigForm.url", 255, fullURI, formLayout);
		thost.setExampleKey("LTConfigForm.url.example", null);
		
		tkey  = uifactory.addTextElement ("key","LTConfigForm.key", 255, key, formLayout);
		tkey.setExampleKey ("LTConfigForm.key.example", null);
		
		tpass = uifactory.addTextElement ("pass","LTConfigForm.pass", 255, pass, formLayout);
		tpass.setExampleKey("LTConfigForm.pass.example", null);
		
		sendName = uifactory.addCheckboxesVertical("sendName", "display.config.sendName", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		sendName.select("xx", sendNameConfig);
		
		sendEmail = uifactory.addCheckboxesVertical("sendEmail", "display.config.sendEmail", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		sendEmail.select("xx", sendEmailConfig);
		
		tcustom = uifactory.addTextAreaElement("tcustom", "display.config.custom", -1, 6, 40, true, customConfig, formLayout);
		
		doDebug = uifactory.addCheckboxesVertical("doDebug", "display.config.doDebug", formLayout, new String[]{"xx"}, new String[]{null}, null, 1);
		doDebug.select("xx", doDebugConfig);
		
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	
	protected static StringBuilder getFullURL(String proto, String host, Integer port, String uri, String query) {
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
			if (query != null) fullURL.append("?").append(query);
		}
		return fullURL;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) { 
		try {
			new URL(thost.getValue());
		} catch (MalformedURLException e) {
			thost.setErrorKey("LTConfigForm.invalidurl", null);
			return false;
		}
		return true;
	}
	
	/**
	 * @return the updated module configuration using the form data
	 */
	protected ModuleConfiguration getUpdatedConfig() {
		URL url = null;
		try {
			url = new URL(thost.getValue());
		} catch (MalformedURLException e) {
			throw new OLATRuntimeException("MalformedURL in LTConfigForm which should not happen, since we've validated before. URL: " + thost.getValue(), e);
		}
		config.setConfigurationVersion(2);
		config.set(CONFIGKEY_PROTO, url.getProtocol());
		config.set(CONFIGKEY_HOST, url.getHost());
		config.set(CONFIGKEY_URI, url.getPath());
		config.set(CONFIGKEY_QUERY, url.getQuery());
		int port = url.getPort();
		config.set(CONFIGKEY_PORT, new Integer(port != -1 ? port : url.getDefaultPort()));
		config.set(CONFIGKEY_KEY, getFormKey());
		config.set(CONFIGKEY_PASS, tpass.getValue());
		config.set(CONFIG_KEY_DEBUG, Boolean.toString(doDebug.isSelected(0)));
		config.set(CONFIG_KEY_CUSTOM, tcustom.getValue());
		config.set(CONFIG_KEY_SENDNAME, Boolean.toString(sendName.isSelected(0)));
		config.set(CONFIG_KEY_SENDEMAIL, Boolean.toString(sendEmail.isSelected(0)));
		return config;
	}

	private String getFormKey() {
		if (StringHelper.containsNonWhitespace(tkey.getValue()))
			return tkey.getValue();
		else 
			return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}
}
