package org.olat.core.commons.services.sms.model;

import java.util.Date;

/**
 * 
 * Initial date: 7 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MessageStatistics {
	
	private final Date date;
	private final String service;
	private final long numOfMessages;

	public MessageStatistics(String service, Date date, long numOfMessages) {
		this.service = service;
		this.date = date;
		this.numOfMessages = numOfMessages;
	}

	public Date getDate() {
		return date;
	}

	public String getService() {
		return service;
	}

	public long getNumOfMessages() {
		return numOfMessages;
	}
}
