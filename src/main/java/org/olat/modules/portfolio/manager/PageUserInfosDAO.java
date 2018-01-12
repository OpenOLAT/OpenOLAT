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

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageUserInformations;
import org.olat.modules.portfolio.PageUserStatus;
import org.olat.modules.portfolio.model.PageUserInformationsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 11 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PageUserInfosDAO {
	
	@Autowired
	private DB dbInstance;
	
	public PageUserInformations create(PageUserStatus status, Page page, Identity identity) {
		PageUserInformationsImpl infos = new PageUserInformationsImpl();
		infos.setCreationDate(new Date());
		infos.setLastModified(infos.getCreationDate());
		infos.setMark(false);
		infos.setStatus(status);
		infos.setRecentLaunch(infos.getCreationDate());
		infos.setPage(page);
		infos.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(infos);
		return infos;
	}
	
	public PageUserInformations getPageUserInfos(Page page, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select infos from pfpageuserinfos as infos")
		  .append(" where infos.page.key=:pageKey and infos.identity.key=:identityKey");
		
		List<PageUserInformations> infos = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), PageUserInformations.class)
			.setParameter("pageKey", page.getKey())
			.setParameter("identityKey", identity.getKey())
			.getResultList();
		return infos == null || infos.isEmpty() ? null : infos.get(0);
	}
	
	public List<PageUserInformations> getPageUserInfos(BinderRef binder, IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select infos from pfpageuserinfos as infos")
		  .append(" inner join fetch infos.page page")
		  .append(" inner join page.section section")
		  .append(" where section.binder.key=:binderKey and infos.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), PageUserInformations.class)
			.setParameter("binderKey", binder.getKey())
			.setParameter("identityKey", identity.getKey())
			.getResultList();
	}
	
	public PageUserInformations update(PageUserInformations infos) {
		((PageUserInformationsImpl)infos).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(infos);
	}
	
	public void updateStatus(Page page, PageUserStatus status) {
		String q = "update pfpageuserinfos infos set infos.userStatus=:status where infos.page.key=:pageKey";
		dbInstance.getCurrentEntityManager()
			.createQuery(q)
			.setParameter("status", status.name())
			.setParameter("pageKey", page.getKey())
			.executeUpdate();
	}
	
	public void updateStatus(Page page, PageUserStatus newStatus, PageUserStatus restrictionTo) {
		String q = "update pfpageuserinfos infos set infos.userStatus=:status where infos.page.key=:pageKey and infos.userStatus=:restrictionTo";
		dbInstance.getCurrentEntityManager()
			.createQuery(q)
			.setParameter("status", newStatus.name())
			.setParameter("restrictionTo", restrictionTo.name())
			.setParameter("pageKey", page.getKey())
			.executeUpdate();
	}
	
	public int delete(Page page) {
		String q = "delete from pfpageuserinfos where page.key=:pageKey";
		return dbInstance.getCurrentEntityManager()
			.createQuery(q)
			.setParameter("pageKey", page.getKey())
			.executeUpdate();
	}
}
