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

package org.olat.fileresource.types;



/**
 * Execption to throw if you need to show the user detailled information about
 * the error and to track the Olatresourcable if case we have to delete the
 * failed entry
 * <P>
 * Initial Date: 12.01.2006 <br>
 * 
 * @author guido
 */
public class AddingResourceException extends Exception {

	private static final long serialVersionUID = 4598004122080558856L;
	private String errorKey;
	
	/**
	 * creates a new exception with a key pointing to a translation key with a
	 * specific message
	 * 
	 * @param errorKey
	 */
	public AddingResourceException(String errorKey) {
		this.errorKey = errorKey;
	}

	/**
	 * get the translation key for the error message
	 * 
	 * @return key
	 */
	public String getErrorKey() {
		return errorKey;
	}

}
