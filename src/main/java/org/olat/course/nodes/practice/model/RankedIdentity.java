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
package org.olat.course.nodes.practice.model;

import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 13 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RankedIdentity {
	
	private final int rank;
	private final boolean me;
	private final Identity identity;
	
	public RankedIdentity(Identity identity, int rank, boolean me) {
		this.identity = identity;
		this.rank = rank;
		this.me = me;
	}
	
	public boolean isMe() {
		return me;
	}
	
	public int getRank() {
		return rank;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public Long getAvatarKey() {
		return identity.getKey();
	}
	
	public String getFullname() {
		StringBuilder sb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(identity.getUser().getFirstName())) {
			sb.append(identity.getUser().getFirstName());
		}
		
		if(StringHelper.containsNonWhitespace(identity.getUser().getLastName())) {
			if(sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(identity.getUser().getLastName());
		}
		return sb.toString();
	}
}
