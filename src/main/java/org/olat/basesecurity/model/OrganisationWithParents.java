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

import java.util.List;

import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 7 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationWithParents implements OrganisationRef, Comparable<OrganisationWithParents> {
	
	private final int order;
	private final Organisation organisation;
	private final List<Organisation> parents;
	
	public OrganisationWithParents(Organisation organisation, List<Organisation> parents, int order) {
		this.organisation = organisation;
		this.parents = parents == null ? List.of() : List.copyOf(parents);
		this.order = order;
	}
	
	@Override
	public Long getKey() {
		return organisation.getKey();
	}
	
	public String getDisplayName() {
		return organisation.getDisplayName();
	}
	
	public Organisation getOrganisation() {
		return organisation;
	}
	
	public List<Organisation> getParents() {
		return parents;
	}
	
	public int depth() {
		return parents.size() + 1;
	}
	
	@Override
	public int compareTo(OrganisationWithParents o) {
		return Integer.compare(order, o.order);
	}

	@Override
	public int hashCode() {
		return organisation.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrganisationWithParents) {
			OrganisationWithParents org = (OrganisationWithParents)obj;
			return organisation.equals(org.getOrganisation());
		}
		return false;
	}
}
