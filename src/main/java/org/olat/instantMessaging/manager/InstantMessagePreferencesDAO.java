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
package org.olat.instantMessaging.manager;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.instantMessaging.model.ImPreferencesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 05.12.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InstantMessagePreferencesDAO {
	
	@Autowired
	private DB dbInstance;
	
	public ImPreferencesImpl createPreferences(Identity from) {
		ImPreferencesImpl msg = new ImPreferencesImpl();
		msg.setCreationDate(new Date());
		dbInstance.getCurrentEntityManager().persist(msg);
		return msg;
	}

	public ImPreferencesImpl getPreferences(Identity from) {
		StringBuilder sb = new StringBuilder();
		sb.append("select msg from ").append(ImPreferencesImpl.class.getName()).append(" msg ")
		  .append(" where msg.identity.key=:identityKey");
		
		List<ImPreferencesImpl> msgs = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), ImPreferencesImpl.class)
				.setParameter("identityKey", from.getKey())
				.getResultList();
		
		if(msgs.isEmpty()) {
			return null;
		}
		return msgs.get(0);
	}
}
