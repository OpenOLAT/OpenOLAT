//<OLATCE-103>
/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2011 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * 
 * All rights reserved.
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