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

/**
 * Initial Date:  31.10.2007 <br>
 * @author Lavinia Dumitrescu
 */
public class ShibbolethException extends Exception {

	private static final long serialVersionUID = 9055605164344300611L;
	//error codes
	public static final int GENERAL_SAML_ERROR = 0;
	public static final int UNIQUE_ID_NOT_FOUND = 1;

	private int errorCode;
	private String contactPersonEmail;

	public ShibbolethException(int errorCode, String msg) {
		super(msg);
		this.errorCode = errorCode;
	}

	public ShibbolethException(int errorCode, Throwable throwable) {
		super(throwable.getMessage());
		this.errorCode = errorCode;
	}

	public ShibbolethException(int errorCode, String contactPersonEmail, Throwable throwable) {
		this(errorCode, throwable);
		this.contactPersonEmail = contactPersonEmail;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getContactPersonEmail() {
		return contactPersonEmail;
	}

	public void setContactPersonEmail(String contactPersonEmail) {
		this.contactPersonEmail = contactPersonEmail;
	}
}
