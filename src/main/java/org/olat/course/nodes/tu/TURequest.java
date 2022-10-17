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

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.gui.UserRequest;
import org.olat.modules.ModuleConfiguration;

/**
 * Initial Date:  Feb 4, 2004
 * @author Mike Stock
 */
public class TURequest {

	private String method;
	private String uri;
	private String queryString;
	private Map<String,String[]> parameterMap;
	private String contentType;
	
	private String userName, lastName, firstName, email, userIPAddress;
	
	/**
	 * 
	 */
	public TURequest() {
		//
	}
	
	/**
	 * Constructor for a tunneling request
	 * @param config
	 * @param ureq
	 */
	public TURequest(ModuleConfiguration config, UserRequest ureq) {
		HttpServletRequest hreq = ureq.getHttpReq();
		method = hreq.getMethod();
		uri = ureq.getModuleURI();
		String startUri = (String)config.get(TUConfigForm.CONFIGKEY_URI);
		if (uri == null) uri = (startUri == null) ? "" : startUri;

		if (uri.length() > 0 && uri.charAt(0) != '/')	uri = "/" + uri;
		queryString = hreq.getQueryString();
		parameterMap = hreq.getParameterMap();
		contentType = hreq.getContentType();
	}
	
	/**
	 * @return The http request method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @return The http request parameter map
	 */
	public Map<String,String[]> getParameterMap() {
		return parameterMap;
	}

	/**
	 * @return The http request module uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param string
	 */
	public void setMethod(String string) {
		method = string;
	}

	/**
	 * @param map
	 */
	public void setParameterMap(Map<String,String[]> map) {
		parameterMap = map;
	}

	/**
	 * @param string
	 */
	public void setUri(String string) {
		uri = string;
	}

	/**
	 * @return The http request query string
	 */
	public String getQueryString() {
		return queryString;
	}

	/**
	 * @param string
	 */
	public void setQueryString(String string) {
		queryString = string;
	}

	/**
	 * @return The http request content type
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param string
	 */
	public void setContentType(String string) {
		contentType = string;
	}

	/**
	 * @return Returns the email.
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email The email to set.
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return Returns the firstName.
	 */
	public String getFirstName() {
		return firstName;
	}
	/**
	 * @param firstName The firstName to set.
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	/**
	 * @return Returns the lastName.
	 */
	public String getLastName() {
		return lastName;
	}
	/**
	 * @param lastName The lastName to set.
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	/**
	 * @return Returns the userName.
	 */
	public String getUserName() {
		return userName;
	}
	/**
	 * @param userName The userName to set.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserIPAddress() {
		return userIPAddress;
	}
	
	public void setUserIPAddress(String address){
		this.userIPAddress = address;
	}
	
	
}
