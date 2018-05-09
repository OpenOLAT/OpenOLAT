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

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.model.CurriculumElementMember;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CurriculumService {
	
	/**
	 * Create and persist a curriculum
	 * @param identifier The identifier
	 * @param displayName The name
	 * @param description The description
	 * @param organisation The organisation
	 * @return A persisted curriculum
	 */
	public Curriculum createCurriculum(String identifier, String displayName, String description, Organisation organisation);
	
	public Curriculum getCurriculum(CurriculumRef ref);
	
	public Curriculum updateCurriculum(Curriculum curriculum);
	
	public List<Curriculum> getCurriculums(CurriculumSearchParameters params);
	
	
	
	public CurriculumElement createCurriculumElement(String identifier, String displayName,
			CurriculumElementRef parent, Curriculum curriculum);
	
	public CurriculumElement getCurriculumElement(CurriculumElementRef element);
	
	/**
	 * Return all the elements of a curriculum, but flat.
	 * 
	 * @param curriculum The curriculum
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElement> getCurriculumElements(CurriculumRef element);
	
	public CurriculumElement updateCurriculumElement(CurriculumElement element);
	
	/**
	 * The list of members of the specified curriculum element.
	 * 
	 * @param element The curriculum element
	 * @return The list of memberships
	 */
	public List<CurriculumElementMember> getMembers(CurriculumElement element);
	
	/**
	 * Add a member with the specified role to the curriculum element. The
	 * inheritance mode of the membership is per default "none".
	 * 
	 * @param element The curriculum element
	 * @param member The identity to make a member of
	 * @param role The role of the member
	 */
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role);
	
	/**
	 * Remove a member of the curriculum element and discard all its roles.
	 * 
	 * @param element The curriculum element
	 * @param member The identity to remove
	 */
	public void removeMember(CurriculumElement element, IdentityRef member);
	
	/**
	 * The all list of repository entries hold by the specified curriculum element.
	 * 
	 * @param element The curriculum element
	 * @return A list of repository entries
	 */
	public List<RepositoryEntry> getRepositoryEntries(CurriculumElementRef element);
	
	/**
	 * This will add a relation between the curriculum element and the repository
	 * entry and it will add the base group of the curriculum to the set of groups
	 * of the repository entry.
	 * 
	 * 
	 * @param element The curriculum element
	 * @param entry The repository entry
	 * @param master If the relation is the master one
	 */
	public void addRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry, boolean master);

}
