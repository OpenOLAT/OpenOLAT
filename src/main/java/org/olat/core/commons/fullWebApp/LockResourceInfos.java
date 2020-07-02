/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.fullWebApp;

import org.olat.core.commons.fullWebApp.BaseFullWebappController.LockStatus;
import org.olat.core.id.OLATResourceable;
import org.olat.course.assessment.model.TransientAssessmentMode;

/**
 * 
 * Initial date: 2 juil. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LockResourceInfos {
	
	private final LockStatus lockStatus;
	private final OLATResourceable lockResource;
	private final TransientAssessmentMode lockMode; 
	
	public LockResourceInfos(LockStatus lockStatus, OLATResourceable lockResource, TransientAssessmentMode lockMode) {
		this.lockStatus = lockStatus;
		this.lockResource = lockResource;
		this.lockMode = lockMode;
	}

	public LockStatus getLockStatus() {
		return lockStatus;
	}

	public OLATResourceable getLockResource() {
		return lockResource;
	}

	public TransientAssessmentMode getLockMode() {
		return lockMode;
	}
}
