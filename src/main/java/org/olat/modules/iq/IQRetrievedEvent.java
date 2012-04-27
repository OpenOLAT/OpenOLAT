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
package org.olat.modules.iq;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class IQRetrievedEvent extends MultiUserEvent implements OLATResourceable {

	private static final long serialVersionUID = -4054560436205827712L;
	
	private Long assessedIdentityKey;
	private Long courseResourceableId;
	private String nodeIdent;

	/**
	 * constructor for a retrieved survey event
	 */
	public IQRetrievedEvent(Identity assessedIdentity, Long courseResourceableId, String nodeIdent) {
		super("iqretrieved");
		this.assessedIdentityKey = assessedIdentity.getKey();
		this.courseResourceableId = courseResourceableId;
		this.nodeIdent = nodeIdent;
	}

	public Long getAssessedIdentityKey() {
		return assessedIdentityKey;
	}

	@Override
	public String getResourceableTypeName() {
		return "iqretrieved";
	}

	@Override
	public Long getResourceableId() {
		return assessedIdentityKey;
	}

	public Long getCourseResourceableId() {
		return courseResourceableId;
	}

	public String getNodeIdent() {
		return nodeIdent;
	}
	
	public boolean isConcerned(Identity identity, Long courseResourceableId, String nodeIdent) {
		if(identity != null && identity.getKey().equals(getAssessedIdentityKey())
				&& getCourseResourceableId() != null && getCourseResourceableId().equals(courseResourceableId)
				&& getNodeIdent() != null && getNodeIdent().equals(nodeIdent)) {
			//it's me -> it's finished
			return true;
		}
		return false;
	}
}
