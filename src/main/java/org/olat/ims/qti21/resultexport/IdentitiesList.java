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
package org.olat.ims.qti21.resultexport;

import java.util.List;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 1 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentitiesList {
	
	private final boolean all;
	private final List<Identity> identities;
	private final boolean withNonParticipants;
	private final List<String> humanReadableFiltersValues;
	
	public IdentitiesList(List<Identity> identities, List<String> humanReadableFiltersValues,
			boolean withNonParticipants, boolean all) {
		this.all = all;
		this.humanReadableFiltersValues = humanReadableFiltersValues;
		this.identities = identities;
		this.withNonParticipants = withNonParticipants;
	}

	public List<Identity> getIdentities() {
		return identities;
	}
	
	public int getNumOfIdentities() {
		return identities.size();
	}
	
	public List<String> getHumanReadableFiltersValues() {
		return humanReadableFiltersValues;
	}
	
	public boolean isAll() {
		return all;
	}

	public boolean isWithNonParticipants() {
		return withNonParticipants;
	}
	
	public boolean isEmpty() {
		return identities == null || identities.isEmpty();
	}
}
