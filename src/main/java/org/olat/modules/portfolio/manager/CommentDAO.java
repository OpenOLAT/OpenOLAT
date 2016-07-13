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
package org.olat.modules.portfolio.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.SectionRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * This extends the capabilities of UserCommentsDAO with some specialized
 * queries.
 * 
 * Initial date: 29.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CommentDAO {

	@Autowired
	private DB dbInstance;
	
	public Map<Long,Long> getNumberOfComments(BinderRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ucomment.resId, count(ucomment.key) from usercomment as ucomment")
		  .append(" inner join pfpage as page on (ucomment.resId=page.key and ucomment.resName='Page')")
		  .append(" left join pfsection as section on (section.key=page.section.key)")
		  .append(" left join pfbinder as binder on (binder.key=section.binder.key)")
		  .append(" where binder.key=:binderKey")
		  .append(" group by ucomment.resId");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("binderKey", binder.getKey())
			.getResultList();
		return mapPageKeyCount(objects);
	}
	
	public Map<Long,Long> getNumberOfComments(SectionRef section) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ucomment.resId, count(ucomment.key) from usercomment as ucomment")
		  .append(" inner join pfpage as page on (ucomment.resId=page.key and ucomment.resName='Page')")
		  .append(" where page.section.key=:sectionKey")
		  .append(" group by ucomment.resId");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("sectionKey", section.getKey())
			.getResultList();
		return mapPageKeyCount(objects);
	}
	
	public Map<Long,Long> getNumberOfCommentsOnOwnedPage(IdentityRef owner) {
		StringBuilder sb = new StringBuilder();
		sb.append("select ucomment.resId, count(ucomment.key) from usercomment as ucomment")
		  .append(" inner join pfpage as page on (ucomment.resId=page.key and ucomment.resName='Page')")
		  .append(" left join pfsection as section on (section.key = page.section.key)")
		  .append(" left join pfbinder as binder on (binder.key=section.binder.key)")
		  .append(" where exists (select pageMember from bgroupmember as pageMember")
		  .append("   inner join pageMember.identity as ident on (ident.key=:ownerKey and pageMember.role='").append(PortfolioRoles.owner.name()).append("')")
		  .append("   where pageMember.group.key=page.baseGroup.key or pageMember.group.key=binder.baseGroup.key")
		  .append(" )")
		  .append(" group by ucomment.resId");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("ownerKey", owner.getKey())
			.getResultList();
		return mapPageKeyCount(objects);
	}
	
	private static Map<Long,Long> mapPageKeyCount(List<Object[]> objects) {
		Map<Long,Long> map = new HashMap<>();
		for(Object[] object:objects) {
			Long pageKey = (Long)object[0];
			Object comments = object[1];
			
			Long numOfComments = null;
			if(comments instanceof Number) {
				numOfComments = ((Number)comments).longValue();
			} else {
				numOfComments = 0l;
			}
			map.put(pageKey, numOfComments);
		}
		return map;
	}
}
