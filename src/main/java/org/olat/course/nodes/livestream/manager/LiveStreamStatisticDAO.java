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

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 17 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class LiveStreamStatisticDAO {

	@Autowired
	private DB dbInstance;
	
	public Long getViewers(String courseResId, String nodeIdent, Date from, Date to) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select count(distinct log.userId)");
		sb.append("  from loggingobject log");
		sb.and().append("log.actionVerb = 'launch'");
		sb.and().append("log.targetResType = 'livestream'");
		sb.and().append("log.targetResId = :targetResId");
		sb.and().append("log.parentResId = :parentResId");
		sb.and().append("log.creationDate >= :from");
		sb.and().append("log.creationDate <= :to");
		
		List<Long> counts = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("targetResId", nodeIdent)
				.setParameter("parentResId", courseResId)
				.setParameter("from", from)
				.setParameter("to", to)
				.getResultList();
		return !counts.isEmpty()? counts.get(0): null;
	}
}
