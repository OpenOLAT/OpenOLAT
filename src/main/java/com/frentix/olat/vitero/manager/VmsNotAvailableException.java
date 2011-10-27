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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package com.frentix.olat.vitero.manager;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class VmsNotAvailableException extends Exception {
	
	private static final long serialVersionUID = 3260533359384969602L;
	
	public static final String I18N_KEY = "error.vmsNotAvailable";

	public VmsNotAvailableException() {
		//
	}
	
	public VmsNotAvailableException(String message) {
		super(message);
	}
	
	public VmsNotAvailableException(String message, Exception cause) {
		super(message, cause);
	}
	
	public VmsNotAvailableException(Exception cause) {
		super(cause);
	}

}
