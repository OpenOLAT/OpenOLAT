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
	public static final long SERVER_NOT_AVAILABLE = 0;
	public static final long UNKOWN = -1;

	private final long errorCode;

	public OpenMeetingsException(long errorCode) {
		this.errorCode = errorCode;
	}
	
	public OpenMeetingsException(Exception cause, long errorCode) {
		super(cause);
		this.errorCode = errorCode;
	}
	
	public String i18nKey() {
		if(errorCode == 0) {
			return SERVER_NOT_I18N_KEY;
		} else if (errorCode >= -56 && errorCode < 0) {
			return "error." + errorCode;
		}
		return "error.-1";//or unkown
	}
}
