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
package org.olat.modules.lecture.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.lecture.Reason;
import org.olat.modules.lecture.model.ReasonImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ReasonDAO {
	
	@Autowired
	private DB dbInstance;
	
	public Reason createReason(String title, String description, boolean enabled) {
		ReasonImpl reason = new ReasonImpl();
		reason.setCreationDate(new Date());
		reason.setLastModified(reason.getCreationDate());
		reason.setEnabled(enabled);
		reason.setTitle(title);
		reason.setDescription(description);
		dbInstance.getCurrentEntityManager().persist(reason);
		return reason;
	}
	
	public Reason updateReason(Reason reason) {
		return dbInstance.getCurrentEntityManager().merge(reason);
	}
	
	public Reason loadReason(Long key) {
		String sb = "select reason from lecturereason reason where reason.key=:reasonKey";
		List<Reason> reasons = dbInstance.getCurrentEntityManager()
				.createQuery(sb, Reason.class)
				.setParameter("reasonKey", key)
				.getResultList();
		return reasons == null || reasons.isEmpty() ? null : reasons.get(0);
	}
	
	public List<Reason> getReasons() {
		String sb = "select reason from lecturereason reason";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, Reason.class)
				.getResultList();
	}
	
	public boolean isReasonInUse(Reason reason) {
		StringBuilder sb = new StringBuilder();
		sb.append("select block.key from lectureblock block")
		  .append(" inner join block.reasonEffectiveEnd reason")
		  .append(" where reason.key=:reasonKey");
		List<Number> keys = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("reasonKey", reason.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return keys != null && !keys.isEmpty() && keys.get(0) != null && keys.get(0).longValue() > 0;
	}
	
	public boolean delete(Reason reason) {
		Reason reloadReference = dbInstance.getCurrentEntityManager()
				.getReference(ReasonImpl.class, reason.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadReference);
		return true;
	}
}
