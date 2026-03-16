/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.model;

import org.olat.modules.selectus.model.ApplicationAssignmentLight;

/**
 * 
 * Initial date: 10 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationAssignmentLightTransient implements ApplicationAssignmentLight {
	
	private final Long assigneeKey;
	private final Long applicationKey;
	
	public ApplicationAssignmentLightTransient(Long assigneeKey, Long applicationKey) {
		this.assigneeKey = assigneeKey;
		this.applicationKey = applicationKey;
	}

	@Override
	public Long getKey() {
		return null;
	}

	@Override
	public Long getApplicationKey() {
		return applicationKey;
	}

	@Override
	public Long getAssigneeKey() {
		return assigneeKey;
	}
}
