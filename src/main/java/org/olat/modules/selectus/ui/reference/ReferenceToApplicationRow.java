/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
