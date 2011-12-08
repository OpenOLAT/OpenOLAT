/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.core.util.mail.model;

import org.olat.core.commons.persistence.PersistentObject;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  8 sept. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DBMailAttachmentData extends PersistentObject {

	private static final long serialVersionUID = -3741636430048220733L;
	
	private Long size;
	private String name;
	private String mimetype;
	private byte[] datas;
	private DBMailImpl mail;
	
	public DBMailAttachmentData() {
		//
	}
	
	public DBMailImpl getMail() {
		return mail;
	}

	public void setMail(DBMailImpl mail) {
		this.mail = mail;
	}

	public byte[] getDatas() {
		return datas;
	}

	public void setDatas(byte[] datas) {
		this.datas = datas;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
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
		return getKey() == null ? 921536 : getKey().intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DBMailAttachmentData) {
			DBMailAttachmentData data = (DBMailAttachmentData)obj;
			return getKey() != null && getKey().equals(data.getKey());
		}
		return false;
	}
}
