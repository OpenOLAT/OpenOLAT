/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.assignment;

import org.olat.modules.selectus.model.ApplicationAssignmentLight;

/**
 * 
 * Initial date: 25 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationAssignmentLightImpl implements ApplicationAssignmentLight {

	private final Long key;
	private final Long applicationKey;
	private final Long assigneeKey;
	
	public ApplicationAssignmentLightImpl(Long key, Long applicationKey, Long assigneeKey) {
		this.key = key;
		this.applicationKey = applicationKey;
		this.assigneeKey = assigneeKey;
	}

	@Override
	public Long getKey() {
		return key;
	}


	@Override
	public Long getApplicationKey() {
		return applicationKey;
	}


	@Override
	public Long getAssigneeKey() {
		return assigneeKey;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 394857 : getKey().intValue();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ApplicationAssignmentLightImpl) {
			ApplicationAssignmentLightImpl cat = (ApplicationAssignmentLightImpl)obj;
			return getKey() != null && getKey().equals(cat.getKey());
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("assignmentLight[key=").append(getKey() == null ? "" : getKey()).append(";")
			.append("applicationKey=").append(applicationKey == null ? "" : applicationKey).append(";")
			.append("assigneeKey=").append(assigneeKey == null ? "" : assigneeKey).append("]");
		return sb.toString();
	}
}
