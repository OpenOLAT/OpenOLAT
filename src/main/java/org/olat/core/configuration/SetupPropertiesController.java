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
package org.olat.core.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.WebappHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Initial Date:  02.02.2010 <br>
 * @author guido
 */
public class SetupPropertiesController extends BasicController {
	
	Properties defaultProperties = new Properties();
	Properties overwriteProperties = new Properties();
	VelocityContainer content = createVelocityContainer("setup");
	private List<OLATProperty> defaultProps = new ArrayList<>();
	private List<OLATProperty> overwriteProps = new ArrayList<>();

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public SetupPropertiesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		Panel main = new Panel("setup");
		Resource olatDefaultPropertiesRes = new ClassPathResource("/serviceconfig/olat.properties");
		Resource overwritePropertiesRes = new ClassPathResource("olat.local.properties");
		
		try {
			defaultProperties.load(olatDefaultPropertiesRes.getInputStream());
			overwriteProperties.load(overwritePropertiesRes.getInputStream());
		} catch (IOException e) {
			logError("Could not load properties files from classpath", e);
		}
		
		analyzeProperties();
		
		
		
		content.contextPut("defaultProperties",defaultProps);
		content.contextPut("overwriteProperties", overwriteProps);
		try {
			content.contextPut("overwritePropertiesLocation", overwritePropertiesRes.getURL().toString());
		} catch (IOException e) {
			logWarn("olat.local.properties not found or not readable.", e);
			content.contextPut("overwritePropertiesLocation", translate("overwrite.properties.not.found"));
		}
		
		content.contextPut("userDataRoot",WebappHelper.getUserDataRoot());

		main.setContent(content);
		
		putInitialPanel(main);
		
		
		
	}

	private void analyzeProperties() {
		Set<Object> defaultKeySet = defaultProperties.keySet();
		Set<Object> overwriteKeySet = overwriteProperties.keySet();
		for (Object key : defaultKeySet) {
			String keyValue = (String) key;
			OLATProperty prop = new OLATProperty(keyValue, defaultProperties.getProperty(keyValue));
			if (overwriteProperties.containsKey(keyValue)) {
				prop.setOverwritten(true);
				prop.setOverwriteValue(overwriteProperties.getProperty(keyValue));
			}
			if (defaultProperties.getProperty(keyValue+".comment") != null) {
				prop.setComment(defaultProperties.getProperty(keyValue+".comment"));
			}
			if (defaultProperties.getProperty(keyValue+".values") != null) {
				prop.setAvailableValues(defaultProperties.getProperty(keyValue+".values"));
			}
			if (!keyValue.endsWith(".comment") && !keyValue.endsWith(".values")) {
				defaultProps.add(prop);
			}
		}

		Collections.sort(defaultProps);
		//insert delimiters between property groups
		groupProperties(defaultProps, "db.");
		groupProperties(defaultProps, "ldap.");
		groupProperties(defaultProps, "instantMessaging.");

		for (Object key : overwriteKeySet) {
			String keyValue = (String) key;
			
			OLATProperty prop = new OLATProperty(keyValue.trim(), overwriteProperties.getProperty(keyValue).trim());
			overwriteProps.add(prop);
		}
		Collections.sort(overwriteProps);
		
		
	}



	private void groupProperties(List<OLATProperty> defProps, String group) {
		int i = 0;
		int firstPos = 0;
		int lastPos = 0;
		boolean found = false;
		OLATProperty delimiter = new OLATProperty("delimiter", "delimiter");
		
		for (OLATProperty olatProperty : defProps) {
			if (!found && olatProperty.getKey().startsWith(group)) {
				firstPos = i;
				found = true;
			} else if (olatProperty.getKey().startsWith(group)){
				lastPos = i;
			}
			i++;
		}
		defProps.add(firstPos, delimiter);
		defProps.add(lastPos+2, delimiter);
		
	}



	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public class OLATProperty implements Comparable<OLATProperty> {
		
		String key;
		String value;
		String comment;
		boolean overwritten;
		boolean hasComment;
		List<String> availableValues = new ArrayList<>(3);
		private String overwriteValue;
		

		public OLATProperty(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public boolean isOverwritten() {return overwritten;}
		
		public String getOverwriteValue() {
			return overwriteValue;
		}

		public void setOverwriteValue(String overwriteValue) {
			this.overwriteValue = "Property is overwritten with value: "+overwriteValue;
		}

		public void setOverwritten(boolean overwritten) {this.overwritten = overwritten;}

		public boolean hasComment() {return hasComment;}

		public void setComment(String comment) {
			this.hasComment = true;
			this.comment = comment;
			}
		
		public String getComment() {return comment;}

		public List<String> getAvailableValues() {return availableValues;}

		public void setAvailableValues(String availableValuesDelimited) {
			StringTokenizer tokens = new StringTokenizer(availableValuesDelimited, ",");
			
			while (tokens.hasMoreElements()) {
				availableValues.add(tokens.nextToken());
			}
		}

		public String getKey() {return key;}

		public String getValue() {return value;}

		@Override
		public int compareTo(OLATProperty prop) {
			return this.getKey().compareTo(prop.getKey());
		}
		
	}

}
