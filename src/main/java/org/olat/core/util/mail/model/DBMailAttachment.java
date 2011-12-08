/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.core.util.mail.model;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.gui.util.CSSHelper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 sept. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DBMailAttachment extends PersistentObject {

	private static final long serialVersionUID = -1713863670528439651L;

	private Long size;
	private String name;
	private String mimetype;
	private DBMailImpl mail;
	
	public DBMailAttachment() {
		//
	}
	
	public DBMailImpl getMail() {
		return mail;
	}

	public void setMail(DBMailImpl mail) {
		this.mail = mail;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}
	
	public String getCssClass() {
		return CSSHelper.createFiletypeIconCssClassFor(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 2951 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DBMailAttachment) {
			DBMailAttachment attachment = (DBMailAttachment)obj;
			return getKey() != null && getKey().equals(attachment.getKey());
		}
		return false;
	}
}
