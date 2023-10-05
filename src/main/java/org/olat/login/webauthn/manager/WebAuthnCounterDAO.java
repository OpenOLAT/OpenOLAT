/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.login.webauthn.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.login.webauthn.WebAuthnStatistics;
import org.olat.login.webauthn.model.WebAuthnStatisticsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class WebAuthnCounterDAO {
	
	@Autowired
	private DB dbInstance;
	
	public WebAuthnStatistics createStatistics(Identity identity, long laterStartValue) {
		WebAuthnStatisticsImpl stats = new WebAuthnStatisticsImpl();
		stats.setCreationDate(new Date());
		stats.setLastModified(stats.getCreationDate());
		stats.setLaterCounter(laterStartValue);
		stats.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(stats);
		return stats;
	}
	
	public List<WebAuthnStatistics> getStatistics(IdentityRef identity) {
		String query = "select stats from webauthnstats as stats where stats.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager().createQuery(query, WebAuthnStatistics.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public WebAuthnStatistics updateStatistics(WebAuthnStatistics statistics) {
		statistics.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(statistics);
	}
	
	public int deleteStatistics(IdentityRef identity) {
		String query = "delete from webauthnstats as stats where stats.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
}
