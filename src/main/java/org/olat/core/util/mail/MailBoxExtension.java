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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ui.MailContextResolver;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.home.HomeSite;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * The MailBoxExtensio for 
 * 
 * <P>
 * Initial Date:  25 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("mailBoxExtension")
public class MailBoxExtension implements MailContextResolver, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(MailBoxExtension.class);

	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Inbox", new InboxContextEntry());	
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
				if("BusinessGroup".equals(resourceTypeName) && !Long.valueOf(0l).equals(resourceId)) {
					List<Long> ids = Collections.singletonList(resourceId);
					List<BusinessGroupShort> groups = businessGroupService.loadShortBusinessGroups(ids);
					if(groups == null || groups.isEmpty()) {
						return null;
					}
					return groups.get(0).getName();
				} else if ("RepositoryEntry".equals(resourceTypeName) && !Long.valueOf(0l).equals(resourceId)) {
					return repositoryManager.lookupDisplayName(resourceId);
				} else if ("CoachSite".equals(resourceTypeName)) {
					return Util.createPackageTranslator(MailBoxExtension.class, locale).translate("coaching");
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
	
	private static class InboxContextEntry extends DefaultContextEntryControllerCreator {

		@Override
		public ContextEntryControllerCreator clone() {
			return this;
		}

		@Override
		public String getSiteClassName(List<ContextEntry> ces, UserRequest ureq) {
			return HomeSite.class.getName();
		}
	}
}
