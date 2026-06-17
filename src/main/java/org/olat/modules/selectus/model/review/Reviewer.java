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
package org.olat.modules.selectus.model.review;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;

import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 21 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Reviewer implements IdentityRef, Comparable<Reviewer> {
	
	private final Long identityKey;
	private final String fullName;
	
	public Reviewer(Identity identity) {
		identityKey = identity.getKey();
		fullName = RecruitingHelper.formatFullName(identity);
	}

	@Override
	public Long getKey() {
		return identityKey;
	}
	
	public String getFullName() {
		return fullName;
	}

	@Override
	public int compareTo(Reviewer o) {
		int c;
		if(o == null) {
			c = -1;
		} else if(fullName == null && o.fullName == null) {
			c = 0;
		} else if(fullName == null) {
			c = 1;
		} else if(o.fullName == null) {
			c = -1;
		} else {
			c = fullName.compareTo(o.fullName);
		}
		
		if(c == 0 && o != null) {
			c = identityKey.compareTo(o.identityKey);
		}
		return c;
	}
	
	

}
