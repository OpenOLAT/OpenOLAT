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
package org.olat.basesecurity.model;

import org.olat.basesecurity.IdentityRef;

/**
 * 
 * Initial date: 09.02.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityRefImpl implements IdentityRef {
	
	private final Long identityKey;
	
	public IdentityRefImpl(Long identityKey) {
		this.identityKey = identityKey;
	}

	@Override
	public Long getKey() {
		return identityKey;
	}

	@Override
	public int hashCode() {
		return identityKey == null ? 86583206 : identityKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if(obj instanceof IdentityRefImpl) {
			IdentityRefImpl ref = (IdentityRefImpl)obj;
			return identityKey != null && identityKey.equals(ref.identityKey);
		}
		return false;
	}
}
