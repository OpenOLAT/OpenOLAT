//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc.provider.wimba;

/**
 * 
 * Description:<br>
 * API return codes, see Wimba Classroom 6.0 API Guide, page 5
 * 
 * <P>
 * Initial Date:  07.01.2011 <br>
 * @author skoeber
 */
public enum StatusCode {
	
	/** 100 OK */																						OK(100),
	/** 200 Database access error (database unavailable) */	DB_ACCESS_ERROR(200),
	/** 201 Permission denied */														DENIED(201),
	/** 204 Not authenticated, invalid authentication */		NO_AUTH(204),
	/** 300 Database constraint error */										DB_CONSTRAINT_ERROR(300),
	/** 301 Target already exists */												ALREADY_EXISTS(301),
	/** 302 Target not found */															NOT_FOUND(302),
	/** 400 Miscellaneous */																MISC(400),
	/** 401 Not implemented */															NOT_IMPLEMENTED(401),
	/** 402 Malformed query */															MALFORMED_QUERY(402),
	/** 404 API HTTP server error */												SERVER_ERROR(404),
	/** Undefined */																				UNDEFINED(666);
	
	private int code;
	
	private StatusCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
	public static StatusCode getStatus(int code) {
		switch (code) {
			case 100: return OK;
			case 200: return DB_ACCESS_ERROR;
			case 201: return DENIED;
			case 204: return NO_AUTH;
			case 300: return DB_CONSTRAINT_ERROR;
			case 301: return ALREADY_EXISTS;
			case 302: return NOT_FOUND;
			case 400: return MISC;
			case 401: return NOT_IMPLEMENTED;
			case 402: return MALFORMED_QUERY;
			case 404: return SERVER_ERROR;
			default: return UNDEFINED;
		}
	}
}
//</OLATCE-103>