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
package org.olat.modules.vitero.model;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  16 janv. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public enum ErrorCode {
	remoteException(-2, "error.remoteException"),
	unkown(-1, "error.unkown"),
	unsufficientRights(4, "error.unkown"),
	userDoesntExist(53, "error.userDoesntExist"),
	customerDoesntExist(101,"error.customerDoesntExist"),
	userNotAttachedToCustomer(103, "error.userNotAttachedToCustomer"),
	groupDoesntExist(151, "error.groupDoesntExist"),
	userNotAssignedToGroup(153, "error.userNotAssignedToGroup"),
	invalidAttribut(303, "error.invalidAttribut"),//invalid attribute or ids <= 0
	invalidTimezone(304, "error.invalidTimezone"),
	noAdministrationPro(307, "error.noAdministrationPro"),
	bookingCollision(501, "error.bookingCollision"),
	moduleCollision(502, "error.moduleCollision"),
	bookingInPast(505, "error.bookingInPast"),
	bookingDoesntExist(506, "error.bookingDoesntExist"),
	noRoomsAvailable(508, "error.noRoomsAvailable"),
	bookingDoesntExistPrime(509, "error.bookingDoesntExist"),
	licenseExpired(703, "error.licenseExpired");
	
	private final int code;
	private final String i18nKey;
	private final String codeString;
	
	private ErrorCode(int code, String i18nKey) {
		this.code = code;
		this.i18nKey = i18nKey;
		codeString = Integer.toString(code);
	}
	
	public int code() {
		return code;
	}
	
	public String codeString() {
		return codeString;
	}
	
	public String i18nKey() {
		return i18nKey;
	}
	
	public static ErrorCode find(int code) {
		for(ErrorCode error :values()) {
			if(error.code() == code) {
				return error;
			}	
		}
		return unkown;
	}

}
