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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * Description:<br>
 * Response object for API calls to Wimba Classroom
 * 
 * <P>
 * Initial Date:  07.01.2011 <br>
 * @author skoeber
 */
public class WimbaResponse {
	
	private StatusCode status = StatusCode.UNDEFINED;
	private List<Map<String, String>> records = new ArrayList<Map<String,String>>();
	
	/*
	 * several constructors for efficiency
	 */
	
	public WimbaResponse() {
		//
	}
	
	public WimbaResponse(StatusCode status) {
		setStatus(status);
	}
	
	public WimbaResponse(int code) {
		setStatus(code);
	}
	
	protected StatusCode getStatus() {
		return status;
	}
	
	protected void setStatus(StatusCode status) {
		this.status = status;
	}
	
	protected void setStatus(int code) {
		this.status = StatusCode.getStatus(code);
	}

	protected void addRecord(Map<String, String> record) {
		records.add(record);
	}
	
	protected List<Map<String, String>> getRecords() {
		return records;
	}
	
	/**
	 * Will return the first value found, but there may be more than this one
	 * @param key
	 * @return value found or <code>null</code>
	 */
	protected String findRecord(String key) {
		String value = null;
		for(Map<String, String> record : records) {
			value = record.get(key);
		}
		return value;
	}
	
	protected boolean hasRecords() {
		return !records.isEmpty();
	}
	
	protected int numRecords() {
		return records.size();
	}
	
}
//</OLATCE-103>