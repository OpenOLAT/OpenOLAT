/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Embeddable
public class PolicyLink implements Serializable {
	
	private static final long serialVersionUID = -5437178904309823863L;
	
	@Column(name = "label")
	private String label;
	@Column(name = "url")
	private String url;

	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public PolicyLink clone() {
		PolicyLink link = new PolicyLink();
		link.setLabel(label);
		link.setUrl(url);
		return link;
	}
}