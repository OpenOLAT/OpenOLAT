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

package org.olat.registration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.id.Identity;


/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for AbstractUserNameCreationInterceptor
 * 
 * <P>
 * Initial Date:  5 mars 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public abstract class AbstractUserNameCreationInterceptor implements UserNameCreationInterceptor {

	private boolean allowChangeOfUsername;
	private boolean makeUniqueProposal;
	
	public boolean isAllowChangeOfUsername() {
		return allowChangeOfUsername;
	}

	public void setAllowChangeOfUsername(boolean allowChangeOfUsername) {
		this.allowChangeOfUsername = allowChangeOfUsername;
	}

	public boolean isMakeUniqueProposal() {
		return makeUniqueProposal;
	}

	public void setMakeUniqueProposal(boolean makeUniqueProposal) {
		this.makeUniqueProposal = makeUniqueProposal;
	}

	@Override
	public boolean allowChangeOfUsername() {
		return allowChangeOfUsername;
	}
	
	protected String makeUniqueProposal(String proposedUsername) {
		List<Identity> identities = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(proposedUsername, null, true, null, null, null, null, null, null, null, null);
		if(identities.isEmpty()) {
			return proposedUsername;
		}
		
		Set<String> names = new HashSet<>();
		for(Identity identity:identities) {
			names.add(identity.getName());
		}
		
		String nextPropsedUsername = proposedUsername;
		for (int i=1; names.contains(nextPropsedUsername); i++) {
			nextPropsedUsername = proposedUsername + i;
		} 
		return nextPropsedUsername;
	}
}
