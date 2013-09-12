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

package org.olat.commons.info.manager;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.commons.info.model.InfoMessage;
import org.olat.commons.info.notification.InfoSubscriptionManager;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  28 juil. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoMessageFrontendManagerImpl extends BasicManager implements InfoMessageFrontendManager {
	
	private MailManager mailManager;
	private CoordinatorManager coordinatorManager;
	private InfoMessageManager infoMessageManager;
	private InfoSubscriptionManager infoSubscriptionManager;

	/**
	 * [used by Spring]
	 * @param mailManager
	 */
	public void setMailManager(MailManager mailManager) {
		this.mailManager = mailManager;
	}

	/**
	 * [used by Spring]
	 * @param coordinatorManager
	 */
	public void setCoordinatorManager(CoordinatorManager coordinatorManager) {
		this.coordinatorManager = coordinatorManager;
	}

	/**
	 * [used by Spring]
	 * @param infoMessageManager
	 */
	public void setInfoMessageManager(InfoMessageManager infoMessageManager) {
		this.infoMessageManager = infoMessageManager;
	}

	/**
	 * [used by Spring]
	 * @param infoSubscriptionManager
	 */
	public void setInfoSubscriptionManager(InfoSubscriptionManager infoSubscriptionManager) {
		this.infoSubscriptionManager = infoSubscriptionManager;
	}

	@Override
	public InfoMessage loadInfoMessage(Long key) {
		return infoMessageManager.loadInfoMessageByKey(key);
	}

	@Override
	public InfoMessage createInfoMessage(OLATResourceable ores, String subPath, String businessPath, Identity author) {
		return infoMessageManager.createInfoMessage(ores, subPath, businessPath, author);
	}

	@Override
	public boolean sendInfoMessage(InfoMessage infoMessage, MailFormatter mailFormatter, Locale locale, Identity from, List<Identity> tos) {
		infoMessageManager.saveInfoMessage(infoMessage);
		
		boolean send = false;
		if(tos != null && !tos.isEmpty()) {
			Set<Long> identityKeySet = new HashSet<Long>();
			ContactList contactList = new ContactList("Infos");
			for(Identity to:tos) {
				if(identityKeySet.contains(to.getKey())) continue;
				contactList.add(to);
				identityKeySet.add(to.getKey());
			}
			
			try {
				String subject = null;
				String body = null;
				if(mailFormatter != null) {
					subject = mailFormatter.getSubject(infoMessage);
					body = mailFormatter.getBody(infoMessage);
				}
				if(!StringHelper.containsNonWhitespace(subject)) {
					subject = infoMessage.getTitle();
				}
				if(!StringHelper.containsNonWhitespace(body)) {
					body = infoMessage.getMessage();
				}
				//fxdiff VCRP-16: intern mail system
				MailContext context = new MailContextImpl(mailFormatter.getBusinessPath());
				MailBundle bundle = new MailBundle();
				bundle.setContext(context);
				bundle.setFromId(from);
				bundle.setContactList(contactList);
				bundle.setContent(subject, body);
				
				MailerResult result = mailManager.sendMessage(bundle);
				send = result.isSuccessful();
			} catch (Exception e) {
				logError("Cannot send info messages", e);
			}
		}

		infoSubscriptionManager.markPublisherNews(infoMessage.getOLATResourceable(), infoMessage.getResSubPath());
		MultiUserEvent mue = new MultiUserEvent("new_info_message");
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(mue, oresFrontend);
		return send;
	}
	
	@Override
	public void deleteInfoMessage(InfoMessage infoMessage) {
		infoMessageManager.deleteInfoMessage(infoMessage);
		infoSubscriptionManager.markPublisherNews(infoMessage.getOLATResourceable(), infoMessage.getResSubPath());
	}

	@Override
	public List<InfoMessage> loadInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before, int firstResult, int maxReturn) {
		return infoMessageManager.loadInfoMessageByResource(ores, subPath, businessPath, after, before, firstResult, maxReturn);
	}
	
	@Override
	public int countInfoMessageByResource(OLATResourceable ores, String subPath, String businessPath,
			Date after, Date before) {
		return infoMessageManager.countInfoMessageByResource(ores, subPath, businessPath, after, before);
	}

	@Override
	public List<Identity> getInfoSubscribers(OLATResourceable resource, String subPath) {
		return infoSubscriptionManager.getInfoSubscribers(resource, subPath);
	}
}
