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
package org.olat.core.commons.services.notifications.manager;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.SubscriptionMail;
import org.olat.core.commons.services.notifications.model.SubscriptionMailImpl;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 16 déc. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class SubscriptionMailDAO {
	
	@Autowired
	private DB dbInstance;
	
	public SubscriptionMail create(Identity identity, Date lastEmail, Date nextMail) {
		SubscriptionMailImpl mail = new SubscriptionMailImpl();
		mail.setCreationDate(new Date());
		mail.setLastModified(mail.getCreationDate());
		mail.setLastMail(lastEmail);
		mail.setNextMail(nextMail);
		mail.setIdentity(identity);
		dbInstance.getCurrentEntityManager().persist(mail);
		return mail;
	}
	
	public SubscriptionMail update(SubscriptionMail mail, Date lastEmail, Date nextMail) {
		mail.setLastMail(lastEmail);
		mail.setNextMail(nextMail);
		mail.setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(mail);
	}
	

	public SubscriptionMail loadByIdentity(IdentityRef identity) {
		String query = """
				select nmail from notimail as nmail
				where nmail.identity.key=:identityKey""";
		
		List<SubscriptionMail> mails = dbInstance.getCurrentEntityManager().createQuery(query, SubscriptionMail.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return mails == null || mails.isEmpty() ? null : mails.get(0);
	}

}
