/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.references;

import java.util.Collections;
import java.util.List;

import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;

/**
 * 
 * Initial date: 7 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceSearchParameters {
	
	private Position position;
	private List<ReferenceType> types;
	private List<ReferenceStatus> status;
	private List<? extends ApplicationRef> applications;
	
	public ReferenceSearchParameters() {
		//
	}
	
	public ReferenceSearchParameters(ReferenceStatus status) {
		this.status = Collections.singletonList(status);
	}

	public List<ReferenceStatus> getStatus() {
		return status;
	}

	public void setStatus(List<ReferenceStatus> status) {
		this.status = status;
	}

	public List<ReferenceType> getTypes() {
		return types;
	}

	public void setTypes(List<ReferenceType> types) {
		this.types = types;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public List<? extends ApplicationRef> getApplications() {
		return applications;
	}

	public void setApplications(List<? extends ApplicationRef> applications) {
		this.applications = applications;
	}
}
