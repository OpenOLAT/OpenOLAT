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
package org.olat.modules.curriculum;

import java.util.Collection;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 9 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementMembershipEvent extends MultiUserEvent {

	private static final long serialVersionUID = -4147709424125454372L;
	
	public static final String MEMEBER_ADDED = "curriculum.element.member.added.event";

	private final Collection<Long> curriculumElementKeys;
	private final Long identityKey;
	private final CurriculumRoles role;
	
	public CurriculumElementMembershipEvent(String command, Collection<? extends CurriculumElementRef> elements, Identity identity, CurriculumRoles role) {
		super(command);
		this.curriculumElementKeys = elements.stream().map(CurriculumElementRef::getKey).collect(Collectors.toList());
		this.identityKey = identity.getKey();
		this.role = role;
	}

	public Collection<Long>  getCurriculumElementKeys() {
		return curriculumElementKeys;
	}

	public Long getIdentityKey() {
		return identityKey;
	}

	public CurriculumRoles getRole() {
		return role;
	}

}
