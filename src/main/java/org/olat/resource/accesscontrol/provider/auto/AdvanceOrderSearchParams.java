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
package org.olat.resource.accesscontrol.provider.auto;

import java.util.Collection;
import java.util.Collections;

import org.olat.basesecurity.IdentityRef;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.provider.auto.AdvanceOrder.Status;

/**
 * 
 * Initial date: 23 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AdvanceOrderSearchParams {
	
	private IdentityRef identitfRef;
	private Collection<Status> status;
	private IdentifierKey identifierKey;
	private AccessMethod method;
	
	public IdentityRef getIdentitfRef() {
		return identitfRef;
	}

	public void setIdentitfRef(IdentityRef identitfRef) {
		this.identitfRef = identitfRef;
	}

	public Collection<Status> getStatus() {
		return status;
	}

	public void setStatus(Collection<Status> status) {
		this.status = status;
	}
	
	public void setStatus(Status status) {
		setStatus(Collections.singletonList(status));
	}

	public IdentifierKey getIdentifierKey() {
		return identifierKey;
	}

	public void setIdentifierKey(IdentifierKey identifierKey) {
		this.identifierKey = identifierKey;
	}

	public AccessMethod getMethod() {
		return method;
	}
	
	public void setMethod(AccessMethod method) {
		this.method = method;
	}

}
