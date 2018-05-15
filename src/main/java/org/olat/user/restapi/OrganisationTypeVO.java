package org.olat.user.restapi;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeManagedFlag;

/**
 * 
 * Initial date: 14 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "organisationTypeVO")
public class OrganisationTypeVO {
	
	private Long key;
	private String identifier;
	private String displayName;
	private String description;
	private String cssClass;
	private String externalId;
	private String managedFlagsString;
	
	public OrganisationTypeVO() {
		//
	}
	
	public static final OrganisationTypeVO valueOf(OrganisationType type) {
		OrganisationTypeVO vo = new OrganisationTypeVO();
		vo.setKey(type.getKey());
		vo.setCssClass(type.getCssClass());
		vo.setDescription(type.getDescription());
		vo.setDisplayName(type.getDisplayName());
		vo.setExternalId(type.getExternalId());
		vo.setIdentifier(type.getIdentifier());
		vo.setManagedFlagsString(OrganisationTypeManagedFlag.toString(type.getManagedFlags()));
		return vo;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	public void setManagedFlagsString(String managedFlagsString) {
		this.managedFlagsString = managedFlagsString;
	}
}
