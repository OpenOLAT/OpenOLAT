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
package org.olat.course.assessment.manager;

import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Work with the datas for the assessment tool
 * 
 * 
 * Initial date: 21.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentToolManagerImpl implements AssessmentToolManager {
	
	@Autowired
	private DB dbInstance;
	
	@Override
	public List<Identity> getAssessedIdentities(SearchAssessedIdentityParams params) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ident")
		  .append(" from ").append(IdentityImpl.class.getName()).append(" as ident ")
		  .append(" where exists (")
		  .append("  select infos from usercourseinfos infos where infos.identity=ident")
		  .append("     and infos.resource.key=:resourceKey")
		  .append(" ) or exists (select rel from repoentrytogroup as rel, bgroup as baseGroup, bgroupmember as membership")
		  .append("    where rel.entry.key=:courseEntryKey and rel.group=baseGroup and membership.group=baseGroup and membership.identity=ident")
		  .append("      and membership.role='").append(GroupRoles.participant.name()).append("'")
		  .append(" )");
		
		List<Identity> list = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Identity.class)
			.setParameter("courseEntryKey", params.getCourseEntry().getKey())
			.setParameter("resourceKey", params.getCourseEntry().getOlatResource().getKey())
			.getResultList();
		return list;	
	}
	


}
