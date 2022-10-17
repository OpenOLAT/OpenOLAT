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
package org.olat.course.nodes.livestream.manager;

import java.util.Date;
import java.util.List;

import jakarta.persistence.TypedQuery;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.livestream.Launch;
import org.olat.course.nodes.livestream.model.LaunchImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 17 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class LaunchDAO {

	@Autowired
	private DB dbInstance;
	
	public Launch create(RepositoryEntry courseEntry, String subIdent, Identity identity, Date launchDate) {
		LaunchImpl launchImpl = new LaunchImpl();
		launchImpl.setCreationDate(new Date());
		launchImpl.setLaunchDate(launchDate);
		launchImpl.setCourseEntry(courseEntry);
		launchImpl.setSubIdent(subIdent);
		launchImpl.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(launchImpl);
		return launchImpl;
	}

	public Long getLaunchers(RepositoryEntryRef courseEntry, String subIdent, Date from, Date to) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(distinct launch.identity.key)");
		sb.append("  from livestreamlaunch launch");
		sb.and().append("launch.courseEntry.key = :courseEntryKey");
		sb.and().append("launch.launchDate >= :from");
		sb.and().append("launch.launchDate <= :to");
		if (StringHelper.containsNonWhitespace(subIdent)) {
			sb.and().append("launch.subIdent = :subIdent");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("courseEntryKey", courseEntry.getKey())
				.setParameter("from", from)
				.setParameter("to", to);
		if (StringHelper.containsNonWhitespace(subIdent)) {
			query.setParameter("subIdent", subIdent);
		}
		List<Long> counts = query.getResultList();
		return !counts.isEmpty()? counts.get(0): null;
	}

}
