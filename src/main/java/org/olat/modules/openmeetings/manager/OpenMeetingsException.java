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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.manager;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenMeetingsException extends Exception {
	
	private static final long serialVersionUID = 3260533359384969602L;
	
	public static final String SERVER_NOT_I18N_KEY = "error.notAvailable";

	private final Type type;
	
	public OpenMeetingsException(Type type) {
		this.type = type;
	}
	
	public OpenMeetingsException(String message, Type type) {
		super(message);
		this.type = type;
	}
	
	public OpenMeetingsException(String message, Exception cause, Type type) {
		super(message, cause);
		this.type = type;
	}
	
	public OpenMeetingsException(Exception cause, Type type) {
		super(cause);
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}

	public enum Type {
		unkown("error.unkown"),
		serverNotAvailable("error.notAvailable");

		private final String i18nKey;
		
		private Type(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
