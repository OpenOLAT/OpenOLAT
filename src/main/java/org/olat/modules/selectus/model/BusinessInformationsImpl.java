/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 
 * Initial date: 17.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Embeddable
public class BusinessInformationsImpl implements BusinessInformations {
	
	@Column(name="organization", nullable=true, insertable=true, updatable=true)
	private String organization;
	@Column(name="affiliation", nullable=true, insertable=true, updatable=true)
	private String affiliation;
	@Column(name="unit", nullable=true, insertable=true, updatable=true)
	private String unit;
	@Column(name="currentposition", nullable=true, insertable=true, updatable=true)
	private String currentPosition;

	public String getOrganization() {
		return organization;
	}
	
	public void setOrganization(String organization) {
		this.organization = organization;
	}
	
	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(String currentPosition) {
		this.currentPosition = currentPosition;
	}
	
	
	

}
