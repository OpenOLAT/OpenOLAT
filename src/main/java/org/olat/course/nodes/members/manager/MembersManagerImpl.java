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
package org.olat.course.nodes.members.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.MembersCourseNode;
import org.olat.course.nodes.members.MembersManager;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGtoAreaRelationImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MembersManagerImpl implements MembersManager {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	
	@Override
	public List<Long> getOwnersKeys(RepositoryEntryRef re) {
		return repositoryEntryRelationDao
				.getMemberKeys(re, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.owner.name());
	}

	@Override
	public List<Identity> getOwners(RepositoryEntryRef re) {
		return repositoryEntryRelationDao
				.getMembers(re, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.owner.name());
	}

	@Override
	public List<Long> getCoachesKeys(RepositoryEntryRef re, ModuleConfiguration moduleConfiguration) {
		return getCoaches(re, moduleConfiguration, Long.class);
	}

	@Override
	public List<Identity> getCoaches(RepositoryEntryRef re, ModuleConfiguration moduleConfiguration) {
		return getCoaches(re, moduleConfiguration, Identity.class);
	}

	@Override
	public List<Long> getParticipantsKeys(RepositoryEntryRef re, ModuleConfiguration moduleConfiguration) {
		return getParticipants(re, moduleConfiguration, Long.class);
	}

	@Override
	public List<Identity> getParticipants(RepositoryEntryRef re, ModuleConfiguration moduleConfiguration) {
		return getParticipants(re, moduleConfiguration, Identity.class);
	}
	
	private <U> List<U> getCoaches(RepositoryEntryRef entry, ModuleConfiguration moduleConfiguration, Class<U> resultClass) {
		List<Long> areaKeys = null;
		List<Long> groupKeys = null;
		boolean entryAndCurriculums = false;
		List<Long> curriculumElementKeys = null;
		if(moduleConfiguration == null || moduleConfiguration.anyTrue(MembersCourseNode.CONFIG_KEY_COACHES_ALL)) {
			// all
		} else {
			if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_COACHES_GROUP)
					|| moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_COACHES_GROUP_ID)) {
				String coachGroupNames = moduleConfiguration.getStringValue(MembersCourseNode.CONFIG_KEY_COACHES_GROUP);
				groupKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_COACHES_GROUP_ID, Long.class);
				if(groupKeys == null && StringHelper.containsNonWhitespace(coachGroupNames)) {
					groupKeys = businessGroupService.toGroupKeys(coachGroupNames, entry);
				}
			}
	
			if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_COACHES_AREA)
					|| moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_COACHES_AREA_IDS)) {
				String coachAreaNames = moduleConfiguration.getStringValue(MembersCourseNode.CONFIG_KEY_COACHES_AREA);
				areaKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_COACHES_AREA_IDS, Long.class);
				if(areaKeys == null && StringHelper.containsNonWhitespace(coachAreaNames)) {
					areaKeys = businessGroupService.toGroupKeys(coachAreaNames, entry);
				}
			}
			
			if(moduleConfiguration.anyTrue(MembersCourseNode.CONFIG_KEY_COACHES_COURSE)) {
				entryAndCurriculums = true;
			} else if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_COACHES_CUR_ELEMENT)
					|| moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_COACHES_CUR_ELEMENT_ID)) {
				curriculumElementKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_COACHES_CUR_ELEMENT_ID, Long.class);
			}
		}
		
		return getMembers(entry.getKey(), GroupRoles.coach.name(), entryAndCurriculums,
				groupKeys, curriculumElementKeys, areaKeys, resultClass);
	}
	
	private <U> List<U> getParticipants(RepositoryEntryRef entry,  ModuleConfiguration moduleConfiguration, Class<U> resultClass) {
		List<Long> areaKeys = null;
		List<Long> groupKeys = null;
		boolean entryAndCurriculums = false;
		List<Long> curriculumElementKeys = null;
		
		if(moduleConfiguration == null || moduleConfiguration.anyTrue(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_ALL)) {
			// take all
		} else {
			if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP)
					|| moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP_ID)) {
				String participantGroupNames = moduleConfiguration.getStringValue(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP);
				groupKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP_ID, Long.class);
				if(groupKeys == null && StringHelper.containsNonWhitespace(participantGroupNames)) {
					groupKeys = businessGroupService.toGroupKeys(participantGroupNames, entry);
				}
			}
			
			if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA)
					|| moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA_ID)) {
				String participantAreaNames = moduleConfiguration.getStringValue(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA);
				areaKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA_ID, Long.class);
				if(areaKeys == null && StringHelper.containsNonWhitespace(participantAreaNames)) {
					areaKeys = businessGroupService.toGroupKeys(participantAreaNames, entry);
				}
			}
			
			if(moduleConfiguration.anyTrue(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_COURSE)) {
				entryAndCurriculums = true;
			} else if(moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT)
					|| moduleConfiguration.has(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT_ID)) {
				curriculumElementKeys = moduleConfiguration.getList(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT_ID, Long.class);
			}
		}
		
		return getMembers(entry.getKey(), GroupRoles.participant.name(), entryAndCurriculums,
				groupKeys, curriculumElementKeys, areaKeys, resultClass);
	}
	
	private <U> List<U> getMembers(Long entryKey, String role, boolean entryAndCurriculums,
			List<Long> businessGroupKeys, List<Long> curriculumElementKeys, List<Long> areaKeys,
			Class<U> resultClass) {
		QueryBuilder sb = new QueryBuilder(512);
		if(resultClass.equals(Identity.class)) {
			sb.append("select ident");
		} else {
			sb.append("select memberships.identity.key");
		}

		sb.append(" from repositoryentry as v")
		  .append(" inner join v.groups as relGroup")
		  .append(" inner join relGroup.group as baseGroup")
		  .append(" inner join baseGroup.members as memberships");
		if(resultClass.equals(Identity.class)) {
			sb.append(" inner join memberships.identity as ident")
			  .append(" inner join fetch ident.user as identUser");
		}
		
		if(businessGroupKeys != null || areaKeys != null) {
			sb.append(" inner", " left", curriculumElementKeys == null && !entryAndCurriculums);
			sb.append(" join businessgroup as businessGroup on (businessGroup.baseGroup.key=baseGroup.key)");
		}
		if(areaKeys != null) {
			sb.append(" inner", " left", businessGroupKeys == null && curriculumElementKeys == null && !entryAndCurriculums);
			sb.append(" join ").append(BGtoAreaRelationImpl.class.getName()).append(" as areaRel on (businessGroup.key=areaRel.businessGroup.key)");
		}
		
		if(curriculumElementKeys != null) {
			sb.append(" inner", " left", areaKeys == null && businessGroupKeys == null && !entryAndCurriculums);
			sb.append(" join curriculumelement as curEl on (curEl.group.key=baseGroup.key)");
		} else if(entryAndCurriculums) {
			sb.append(" left join curriculumelement as curEl on (curEl.group.key=baseGroup.key)");
		}

		sb.append(" where v.key=:entryKey and memberships.role=:role");
		
		boolean hasRestrictions = businessGroupKeys != null || areaKeys != null || curriculumElementKeys != null || entryAndCurriculums;
		if(hasRestrictions) {
			sb.append(" and (");
			
			boolean or = false;
			if(businessGroupKeys != null) {
				or = appendOr(sb, or);
				sb.append("businessGroup.key in (:businessGroupKeys)");
			}
			if(areaKeys != null) {
				or = appendOr(sb, or);
				sb.append("areaRel.groupArea.key in (:areaKeys)");
			}
			
			if(curriculumElementKeys != null) {
				or = appendOr(sb, or);
				sb.append("curEl.key in (:curriculumElementKeys)");
			} else if(entryAndCurriculums) {
				or = appendOr(sb, or);
				sb.append("(relGroup.defaultGroup=true or curEl.key is not null)");
			}
			
			sb.append(")");
		}
		
		TypedQuery<U> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), resultClass)
				.setFlushMode(FlushModeType.COMMIT)
				.setParameter("entryKey", entryKey)
				.setParameter("role", role);
		if(curriculumElementKeys != null) {
			query.setParameter("curriculumElementKeys", curriculumElementKeys);
		}
		if(businessGroupKeys != null) {
			query.setParameter("businessGroupKeys", businessGroupKeys);
		}

		if(areaKeys != null) {
			query.setParameter("areaKeys", areaKeys);
		}
		
		List<U> list = query.getResultList();
		return new ArrayList<>(new HashSet<>(list));
	}
	
	private boolean appendOr(QueryBuilder sb, boolean or) {
		if(or) {
			sb.append(" or ");
		}
		return true;
	}
	
	
}
