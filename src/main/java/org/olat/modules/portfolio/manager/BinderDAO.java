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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.BinderImpl;
import org.olat.modules.portfolio.model.SectionImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class BinderDAO {

	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	
	public BinderImpl createAndPersist(String title, String summary, String imagePath) {
		BinderImpl binder = new BinderImpl();
		binder.setCreationDate(new Date());
		binder.setLastModified(binder.getCreationDate());
		binder.setTitle(title);
		binder.setSummary(summary);
		binder.setImagePath(imagePath);
		binder.setBaseGroup(groupDao.createGroup());
		dbInstance.getCurrentEntityManager().persist(binder);
		return binder;
	}
	
	public Binder updateBinder(Binder binder) {
		return dbInstance.getCurrentEntityManager().merge(binder);
	}
	
	public List<Binder> searchOwnedBinders(Identity owner) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder from pfbinder as binder")
		  .append(" inner join fetch binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and membership.role=:role");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("identityKey", owner.getKey())
			.setParameter("role", GroupRoles.owner.name())
			.getResultList();
	}
	
	public Binder loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder from pfbinder as binder")
		  .append(" inner join fetch binder.baseGroup as baseGroup")
		  .append(" where binder.key=:portfolioKey");
		
		List<Binder> binders = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("portfolioKey", key)
			.getResultList();
		return binders == null || binders.isEmpty() ? null : binders.get(0);
	}
	
	public Binder loadBySection(SectionRef section) {
		StringBuilder sb = new StringBuilder();
		sb.append("select binder from pfsection as section")
		  .append(" inner join section.binder as binder")
		  .append(" inner join fetch binder.baseGroup as baseGroup")
		  .append(" where section.key=:sectionKey");
		
		List<Binder> binders = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Binder.class)
			.setParameter("sectionKey", section.getKey())
			.getResultList();
		return binders == null || binders.isEmpty() ? null : binders.get(0);
	}
	
	public List<Identity> getMembers(BinderRef binder, String... roles)  {
		if(binder == null || roles == null || roles.length == 0 || roles[0] == null) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select membership.identity from pfbinder as binder")
		  .append(" inner join binder.baseGroup as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" inner join membership.identity as ident")
		  .append(" inner join fetch ident.user as identUser")
		  .append(" where binder.key=:binderKey and membership.role in (:roles)");
		
		List<String> roleList = new ArrayList<>(roles.length);
		for(String role:roles) {
			if(StringHelper.containsNonWhitespace(role)) {
				roleList.add(role);
			}
		}
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Identity.class)
			.setParameter("binderKey", binder.getKey())
			.setParameter("roles", roleList)
			.getResultList();
	}
	
	/**
	 * Add a section to a binder. The binder must be a fresh reload one
	 * with the possibility to lazy load the sections.
	 * 
	 * @param title
	 * @param description
	 * @param begin
	 * @param end
	 * @param binder
	 */
	public SectionImpl createSection(String title, String description, Date begin, Date end, Binder binder) {
		SectionImpl section = new SectionImpl();
		section.setCreationDate(new Date());
		section.setLastModified(section.getCreationDate());
		
		section.setBaseGroup(groupDao.createGroup());
		section.setTitle(title);
		section.setDescription(description);
		section.setBeginDate(begin);
		section.setEndDate(end);
		//force load of the list
		((BinderImpl)binder).getSections().size();
		section.setBinder(binder);
		((BinderImpl)binder).getSections().add(section);
		dbInstance.getCurrentEntityManager().persist(section);
		dbInstance.getCurrentEntityManager().merge(binder);
		return section;
	}
	
	public Section merge(Section section) {
		return dbInstance.getCurrentEntityManager().merge(section);
	}
	
	public Section loadSectionByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select section from pfsection as section")
		  .append(" inner join fetch section.baseGroup as baseGroup")
		  .append(" where section.key=:sectionKey");
		
		List<Section> sections = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Section.class)
			.setParameter("sectionKey", key)
			.getResultList();
		return sections == null || sections.isEmpty() ? null : sections.get(0);
	}
	
	public List<Section> getSections(BinderRef binder) {
		StringBuilder sb = new StringBuilder();
		sb.append("select section from pfsection as section")
		  .append(" inner join fetch section.baseGroup as baseGroup")
		  .append(" where section.binder.key=:binderKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Section.class)
			.setParameter("binderKey", binder.getKey())
			.getResultList();
	}
}
