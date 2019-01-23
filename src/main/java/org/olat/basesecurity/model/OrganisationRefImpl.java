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

import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 25 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationRefImpl implements OrganisationRef {
	
	private final Long key;
	
	public OrganisationRefImpl(Long key) {
		this.key = key;
	}
	
	public OrganisationRefImpl(OrganisationRef ref) {
		this(ref.getKey());
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public int hashCode() {
		return key == null ? 2678 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if(obj instanceof OrganisationRefImpl) {
			OrganisationRefImpl ref = (OrganisationRefImpl) obj;
			return key != null && key.equals(ref.key);
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(255);
		sb.append("organisationRef[key=").append(getKey() == null ? "" : getKey().toString()).append("]")
		  .append(super.toString());
		return sb.toString();
	}
}
