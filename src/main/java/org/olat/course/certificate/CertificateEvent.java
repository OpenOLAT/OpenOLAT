package org.olat.course.certificate;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 19.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateEvent extends MultiUserEvent {

	private static final long serialVersionUID = -1854321062161208157L;
	private Long ownerKey;
	private Long resourceKey;
	private Long certificateKey;
	
	public CertificateEvent(Long ownerKey, Long certificateKey, Long resourceKey) {
		super("certificate-update");
		this.ownerKey = ownerKey;
		this.resourceKey = resourceKey;
		this.certificateKey = certificateKey;
	}

	public Long getOwnerKey() {
		return ownerKey;
	}

	public void setOwnerKey(Long ownerKey) {
		this.ownerKey = ownerKey;
	}

	public Long getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(Long resourceKey) {
		this.resourceKey = resourceKey;
	}

	public Long getCertificateKey() {
		return certificateKey;
	}

	public void setCertificateKey(Long certificateKey) {
		this.certificateKey = certificateKey;
	}
}
