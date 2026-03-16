/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

/**
 * 
 * Initial date: 20 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RejectionEmailLogFull extends RejectionEmailLog {
	
	public String getMailSubject();
	
	public String getMailContent();
	
	public Attachment getLetter();

}
