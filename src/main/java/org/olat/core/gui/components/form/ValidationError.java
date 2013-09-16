/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form;

/**
 * <h3>Description:</h3>
 * This class encapsulates a validation error. The error is an i18n key and not
 * yet translated
 * This class is mostly used by propertyHandler, but can also be used for others.
 * <p>
 * Initial Date: 24.01.2008 <br>
 * 
 * @author Florian Gnaegi, Roman Haag, frentix GmbH, http://www.frentix.com
 */
public class ValidationError {
	private String errorKey;
	private String[] args;

	/**
	 * @return The i18n key for this error or NULL for no error
	 */
	public String getErrorKey() {
		return errorKey;
	}
	
	/**
	 * @param errorKey The i18n key for this error or NULL for no error
	 */
	public void setErrorKey(String errorKey) {
		this.errorKey = errorKey;
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}
	
	
}
