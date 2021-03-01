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
package org.olat.course.noderight.model;

import java.io.Serializable;
import java.util.Date;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.course.noderight.NodeRightGrant;
import org.olat.group.BusinessGroupRef;
import org.olat.group.model.BusinessGroupRefImpl;

/**
 * 
 * Initial date: 29 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeRightGrantImpl implements NodeRightGrant, Serializable {

	private static final long serialVersionUID = -6847132339318421933L;
	
	private NodeRightRole role;
	private Long identityKey;
	private transient IdentityRef identityRef;
	private Long businessGroupKey;
	private transient BusinessGroupRef businessGroupRef;
	private Date start;
	private Date end;

	@Override
	public NodeRightRole getRole() {
		return role;
	}
	
	public void setRole(NodeRightRole role) {
		this.role = role;
	}
	
	@Override
	public IdentityRef getIdentityRef() {
		if (identityKey != null && identityRef == null) {
			identityRef = new IdentityRefImpl(identityKey);
		}
		return identityRef;
	}
	
	public void setIdentityRef(IdentityRef identityRef) {
		this.identityRef = identityRef;
		if (identityRef != null) {
			this.identityKey = identityRef.getKey();
		} else {
			this.identityKey = null;
		}
	}
	
	@Override
	public BusinessGroupRef getBusinessGroupRef() {
		if (businessGroupKey != null && businessGroupRef == null) {
			businessGroupRef = new BusinessGroupRefImpl(businessGroupKey);
		}
		return businessGroupRef;
	}
	
	public void setBusinessGroupRef(BusinessGroupRef businessGroupRef) {
		this.businessGroupRef = businessGroupRef;
		if (businessGroupRef != null) {
			this.businessGroupKey = businessGroupRef.getKey();
		} else {
			this.businessGroupKey = null;
		}
	}
	
	@Override
	public Date getStart() {
		return start;
	}
	
	@Override
	public void setStart(Date start) {
		this.start = start != null? new Date(start.getTime()): null;
	}
	
	@Override
	public Date getEnd() {
		return end;
	}
	
	@Override
	public void setEnd(Date end) {
		this.end = end != null? new Date(end.getTime()): null;
	}

}
