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
