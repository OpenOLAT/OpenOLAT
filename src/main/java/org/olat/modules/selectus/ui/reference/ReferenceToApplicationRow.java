/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ReferenceToApplication;

/**
 * 
 * Initial date: 6 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceToApplicationRow {
	
	private final boolean newRelation;
	private final Application application;
	private final ReferenceToApplication referenceToApplication;
	
	private boolean deleted;
	
	public ReferenceToApplicationRow(Application application) {
		this.newRelation = true;
		this.application = application;
		this.referenceToApplication = null;
	}
	
	public ReferenceToApplicationRow(ReferenceToApplication referenceToApplication) {
		this.newRelation = false;
		this.application = referenceToApplication.getApplication();
		this.referenceToApplication = referenceToApplication;
	}
	
	public ReferenceToApplicationRow(ReferenceToApplication referenceToApplication, boolean newRelation) {
		this.newRelation = newRelation;
		this.application = referenceToApplication.getApplication();
		this.referenceToApplication = referenceToApplication;
	}

	public boolean isNewRelation() {
		return newRelation;
	}
	
	public Application getApplication() {
		return application;
	}

	public ReferenceToApplication getReferenceToApplication() {
		return referenceToApplication;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
