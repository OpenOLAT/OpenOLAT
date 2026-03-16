package org.olat.modules.selectus.model.application;

import org.olat.modules.selectus.model.PositionLight;

/**
 * 
 * Initial date: 25 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ParallelApplication {
	
	private final Long applicationKey;
	private final String applicationEmail;
	private final PositionLight position;
	
	
	public ParallelApplication(Long applicationKey, String applicationEmail, PositionLight position) {
		this.applicationKey = applicationKey;
		this.applicationEmail = applicationEmail;
		this.position = position;
	}


	public Long getApplicationKey() {
		return applicationKey;
	}


	public String getApplicationEmail() {
		return applicationEmail;
	}


	public PositionLight getPosition() {
		return position;
	}
}
