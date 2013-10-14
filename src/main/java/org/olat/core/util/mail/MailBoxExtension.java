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
package org.olat.core.util.mail;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.manager.MailManager;
import org.olat.core.util.mail.model.DBMailLight;
import org.olat.core.util.mail.ui.MailContextResolver;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.home.HomeSite;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserDataDeletable;

/**
 * 
 * Description:<br>
 * The MailBoxExtensio for 
 * 
 * <P>
 * Initial Date:  25 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailBoxExtension extends BasicManager implements MailContextResolver, UserDataDeletable {
	
	private static final OLog log = Tracing.createLoggerFor(MailBoxExtension.class);

	private MailManager mailManager;
	private RepositoryManager repositoryManager;
	private BusinessGroupService businessGroupService;
	
	public MailBoxExtension() {
		//
	}
	
	public void init() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Inbox", new DefaultContextEntryControllerCreator(){
			@Override
			public Controller createController(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
				return null;
			}

			@Override
			public String getTabName(ContextEntry ce, UserRequest ureq) {
				// opens in home-tab
				return null;
			}

			@Override
			public String getSiteClassName(ContextEntry ce, UserRequest ureq) {
				return HomeSite.class.getName();
			}

			@Override
			public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
				return true;
			}
		});	
	}
	
	/**
	 * [used by Spring]
	 * @param mailManager
	 */
	public void setMailManager(MailManager mailManager) {
		this.mailManager = mailManager;
	}

	/**
	 * [used by Spring]
	 * @param mailManager
	 */
	public void setRepositoryManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}

	/**
	 * [used by Spring]
	 * @param mailManager
	 */
	public void setBusinessGroupService(BusinessGroupService businessGroupService) {
		this.businessGroupService = businessGroupService;
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		//set as deleted all recipients
		logInfo("Delete intern messages");
		
		Collection<DBMailLight> inbox = new HashSet<DBMailLight>(mailManager.getInbox(identity, null, Boolean.FALSE, null, 0, 0));
		for(DBMailLight inMail:inbox) {
			mailManager.delete(inMail, identity, true);
		}

		Collection<DBMailLight> outbox = new HashSet<DBMailLight>(mailManager.getOutbox(identity, 0, 0, false));
		for(DBMailLight outMail:outbox) {
			mailManager.delete(outMail, identity, true);
		}
		
		logInfo("Delete " + inbox.size() + " messages in INBOX and " + outbox.size() + " in OUTBOX");
	}

	@Override
	public String getName(String businessPath, Locale locale) {
		if(!StringHelper.containsNonWhitespace(businessPath)) return null;
		
		try {
			List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
			Collections.reverse(entries);
			
			for(ContextEntry entry:entries) {
				String resourceTypeName = entry.getOLATResourceable().getResourceableTypeName();
				Long resourceId = entry.getOLATResourceable().getResourceableId();
				if("BusinessGroup".equals(resourceTypeName)) {
					List<Long> ids = Collections.singletonList(resourceId);
					List<BusinessGroupShort> groups = businessGroupService.loadShortBusinessGroups(ids);
					if(groups == null || groups.isEmpty()) {
						return null;
					}
					return groups.get(0).getName();
				} else if ("RepositoryEntry".equals(resourceTypeName)) {
					return repositoryManager.lookupDisplayName(resourceId);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}

	@Override
	public void open(UserRequest ureq, WindowControl wControl, String businessPath) {
		BusinessControl bc = BusinessControlFactory.getInstance().createFromString(businessPath);
	  WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, wControl);
	  NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
}
