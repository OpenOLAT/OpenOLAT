package org.olat.core.util.mail;

/**
 * 
 * Initial date: 28.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface MailAttachment {
	
	public String getName();
	
	public String getPath();
	
	public Long getChecksum();
	
	public Long getSize();
	
	public String getMimetype();

}
