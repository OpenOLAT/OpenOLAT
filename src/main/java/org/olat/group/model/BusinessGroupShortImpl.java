package org.olat.group.model;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupShort;

/**
 * This a short summary of the business group without any
 * relation.<br>
 * !!!This class is IMMUTABLE
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupShortImpl extends PersistentObject implements BusinessGroupShort {

	private static final long serialVersionUID = -5404538852842562897L;
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
	@Override
	public String getResourceableTypeName() {
		return OresHelper.calculateTypeName(BusinessGroup.class);
	}

	@Override
	public Long getResourceableId() {
		return getKey();
	}

	/**
	 * Compares the keys.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if (obj instanceof BusinessGroupShortImpl) {
			BusinessGroupShortImpl bg = (BusinessGroupShortImpl)obj;
			return getKey() != null && getKey().equals(bg.getKey());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 2901 : getKey().hashCode();
	}
}
