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
package org.olat.shibboleth;

public class SwitchShibbolethAuthenticationConfigurator {

	private String wayfSPEntityID;
	private String wayfSPHandlerURL;
	private String wayfSPSamlDSURL;
	private String wayfReturnUrl;
	private String wayfReturnMobileUrl;
	private String additionalIDPs;
	
	/**
	 * 
	 */
	public SwitchShibbolethAuthenticationConfigurator() {
		//used by spring
	}
	
	public String getWayfSPEntityID() {		
		return wrapWithQuotes(wayfSPEntityID);
	}
	
	public void setWayfSPEntityID(String wayfSPEntityID) {
		this.wayfSPEntityID = wayfSPEntityID;
	}

	/**
	 * Adds a double quotes at the beginning and at the end of the aString if not already there.
	 * @param aString
	 * @return
	 */
	private String wrapWithQuotes(String aString) {
		if(aString==null) return aString;
		if(!aString.startsWith("\"")) {
			aString = "\"" + aString;
		}
		if(!aString.endsWith("\"")) {
			aString += "\"";		
		}
		return aString;
	}

	public String getWayfSPHandlerURL() {
		return wrapWithQuotes(wayfSPHandlerURL);
	}

	public void setWayfSPHandlerURL(String wayfSPHandlerURL) {
		this.wayfSPHandlerURL = wayfSPHandlerURL;
	}

	public String getWayfSPSamlDSURL() {
		return wrapWithQuotes(wayfSPSamlDSURL);
	}

	public void setWayfSPSamlDSURL(String wayfSPSamlDSURL) {
		this.wayfSPSamlDSURL = wayfSPSamlDSURL;
	}

	public String getWayfReturnUrl() {
		return wrapWithQuotes(wayfReturnUrl);
	}

	public void setWayfReturnUrl(String wayfReturnUrl) {
		this.wayfReturnUrl = wayfReturnUrl;
	}
	
	public String getWayfReturnMobileUrl() {
		return wrapWithQuotes(wayfReturnMobileUrl);
	}

	public void setWayfReturnMobileUrl(String wayfReturnMobileUrl) {
		this.wayfReturnMobileUrl = wayfReturnMobileUrl;
	}

	public void setAdditionalIdentityProviders(String additionalIDPs) {
		this.additionalIDPs = additionalIDPs;
	}
	public String getAdditionalIdentityProviders() {
		return additionalIDPs;
	}
}
