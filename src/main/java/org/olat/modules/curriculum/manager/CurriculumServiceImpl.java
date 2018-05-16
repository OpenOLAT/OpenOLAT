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
package org.olat.modules.curriculum.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementMember;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CurriculumServiceImpl implements CurriculumService {
	
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CurriculumElementTypeDAO curriculumElementTypeDao;
	@Autowired
	private CurriculumElementTypeToTypeDAO curriculumElementTypeToTypeDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private CurriculumRepositoryEntryRelationDAO curriculumRepositoryEntryRelationDao;

	@Override
	public Curriculum createCurriculum(String identifier, String displayName, String description, Organisation organisation) {
		return curriculumDao.createAndPersist(identifier, displayName, description, organisation);
	}

	@Override
	public Curriculum getCurriculum(CurriculumRef ref) {
		return curriculumDao.loadByKey(ref.getKey());
	}

	@Override
	public Curriculum updateCurriculum(Curriculum curriculum) {
		return curriculumDao.update(curriculum);
	}

	@Override
	public List<CurriculumElementType> getCurriculumElementTypes() {
		return curriculumElementTypeDao.load();
	}

	@Override
	public CurriculumElementType getCurriculumElementType(CurriculumElementTypeRef typeRef) {
		return curriculumElementTypeDao.loadByKey(typeRef.getKey());
	}

	@Override
	public CurriculumElementType createCurriculumElementType(String identifier, String displayName,
			String description, String externalId) {
		return curriculumElementTypeDao.createCurriculumElementType(identifier, displayName, description, externalId);
	}
	
	@Override
	public CurriculumElementType updateCurriculumElementType(CurriculumElementType elementType) {
		return curriculumElementTypeDao.update(elementType);
	}

	@Override
	public CurriculumElementType updateCurriculumElementType(CurriculumElementType elementType, List<CurriculumElementType> allowedSubTypes) {
		curriculumElementTypeToTypeDao.setAllowedSubType(elementType, allowedSubTypes);
		return curriculumElementTypeDao.update(elementType);
	}
	
	@Override
	public void allowCurriculumElementSubType(CurriculumElementType parentType, CurriculumElementType allowedSubType) {
		curriculumElementTypeToTypeDao.addAllowedSubType(parentType, allowedSubType);
	}

	@Override
	public void disallowCurriculumElementSubType(CurriculumElementType parentType, CurriculumElementType disallowedSubType) {
		curriculumElementTypeToTypeDao.disallowedSubType(parentType, disallowedSubType);
	}

	@Override
	public CurriculumElementType cloneCurriculumElementType(CurriculumElementTypeRef typeRef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteCurriculumElementType(CurriculumElementTypeRef typeRef) {
		return false;
	}

	@Override
	public List<Curriculum> getCurriculums(CurriculumSearchParameters params) {
		return curriculumDao.search(params);
	}

	@Override
	public CurriculumElement createCurriculumElement(String identifier, String displayName, Date beginDate, Date endDate,
			CurriculumElementRef parentRef, CurriculumElementType elementType, Curriculum curriculum) {
		return curriculumElementDao.createCurriculumElement(identifier, displayName, beginDate, endDate, parentRef, elementType, curriculum);
	}

	@Override
	public CurriculumElement getCurriculumElement(CurriculumElementRef element) {
		return curriculumElementDao.loadByKey(element.getKey());
	}

	@Override
	public CurriculumElement updateCurriculumElement(CurriculumElement element) {
		return curriculumElementDao.update(element);
	}

	@Override
	public CurriculumElement moveCurriculumElement(CurriculumElement elementToMove, CurriculumElement newParent) {
		return curriculumElementDao.move(elementToMove, newParent);
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(CurriculumRef curriculum) {
		return curriculumElementDao.loadElements(curriculum);
	}

	@Override
	public List<CurriculumElement> getCurriculumElementParentLine(CurriculumElement element) {
		return curriculumElementDao.getParentLine(element);
	}

	@Override
	public List<CurriculumElementMember> getMembers(CurriculumElement element) {
		return curriculumElementDao.getMembers(element);
	}

	@Override
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role) {
		if(!groupDao.hasRole(element.getGroup(), member, role.name())) {
			groupDao.addMembershipOneWay(element.getGroup(), member, role.name(), GroupMembershipInheritance.none);
		} 
	}

	@Override
	public void removeMember(CurriculumElement element, IdentityRef member) {
		groupDao.removeMembership(element.getGroup(), member);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntries(CurriculumElementRef element) {
		return curriculumRepositoryEntryRelationDao.getRepositoryEntries(element);
	}

	@Override
	public void addRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry, boolean master) {
		RepositoryEntry repoEntry = repositoryEntryDao.loadByKey(entry.getKey());
		repositoryEntryRelationDao.createRelation(element.getGroup(), repoEntry);
		curriculumRepositoryEntryRelationDao.createRelation(repoEntry, element, master);
	}
}
