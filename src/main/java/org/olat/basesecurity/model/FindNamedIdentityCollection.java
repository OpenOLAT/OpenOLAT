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
import java.util.Map;
import java.util.Set;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 7 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FindNamedIdentityCollection {
	
	private Map<String, Set<Identity>> nameToIdentities;
	private Set<Identity> unique;
	private Set<Identity> ambiguous;
	private Set<String> ambiguousNames;
	private List<String> notFoundNames;
	
	public Map<String, Set<Identity>> getNameToIdentities() {
		return nameToIdentities;
	}

	public void setNameToIdentities(Map<String, Set<Identity>> nameToIdentities) {
		this.nameToIdentities = nameToIdentities;
	}
	
	public Set<Identity> getUnique() {
		return unique;
	}

	public void setUnique(Set<Identity> unique) {
		this.unique = unique;
	}

	public Set<Identity> getAmbiguous() {
		return ambiguous;
	}

	public void setAmbiguous(Set<Identity> ambiguous) {
		this.ambiguous = ambiguous;
	}

	public Set<String> getAmbiguousNames() {
		return ambiguousNames;
	}

	public void setAmbiguousNames(Set<String> ambiguousNames) {
		this.ambiguousNames = ambiguousNames;
	}

	public List<String> getNotFoundNames() {
		return notFoundNames;
	}
	
	public void setNotFoundNames(List<String> notFoundNames) {
		this.notFoundNames = notFoundNames;
	}

}
