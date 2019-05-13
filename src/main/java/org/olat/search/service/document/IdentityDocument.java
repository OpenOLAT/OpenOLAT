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
package org.olat.search.service.document;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nModule;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageConfigManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * <h3>Description:</h3>
 * <p>
 * The IdentityDocument creates a search engine view for a certain identity
 * <p>
 * Initial Date: 21.08.2009 <br>
 * 
 * @author gnaegi, gnaegi@frentix.com, www.frentix.com
 */
public class IdentityDocument extends OlatDocument {

	private static final long serialVersionUID = -7433744122379433733L;
	private static final Logger log = Tracing.createLoggerFor(IdentityDocument.class);
	
	
	/**
	 * Factory method to create a new IdentityDocument
	 * @param searchResourceContext
	 * @param wikiPage
	 * @return
	 */
	public static Document createDocument(SearchResourceContext searchResourceContext, Identity identity) {		

		UserManager userMgr = CoreSpringFactory.getImpl(UserManager.class);
		HomePageConfigManager homepageMgr = CoreSpringFactory.getImpl(HomePageConfigManager.class);
		HomePageConfig publishConfig = homepageMgr.loadConfigFor(identity.getName());

		User user = identity.getUser();
		IdentityDocument identityDocument = new IdentityDocument();
		identityDocument.setTitle(identity.getName());
		identityDocument.setCreatedDate(user.getCreationDate());
		
		// loop through all user properties and collect the content string and the last modified
		List<UserPropertyHandler> userPropertyHanders = userMgr.getUserPropertyHandlersFor(IdentityDocument.class.getName(), false);
		StringBuilder content = new StringBuilder();
		for (UserPropertyHandler userPropertyHandler : userPropertyHanders) {
			String propertyName = userPropertyHandler.getName();
			// only index fields the user has published!
			if (publishConfig.isEnabled(propertyName)) {
				String value = user.getProperty(propertyName, I18nModule.getDefaultLocale());
				if (value != null) {
					content.append(value).append(" ");
				}
			}
		}
		// user text
		String text = publishConfig.getTextAboutMe();
		if (StringHelper.containsNonWhitespace(text)) {
			text = FilterFactory.getHtmlTagsFilter().filter(text);
			content.append(text).append(' ');
		}
		// finally use the properties as the content for this identity
		if (content.length() > 0) {
			identityDocument.setContent(content.toString());			
		}
		
		identityDocument.setResourceUrl(searchResourceContext.getResourceUrl());
		identityDocument.setDocumentType(searchResourceContext.getParentContextType());
		identityDocument.setCssIcon(CSSHelper.CSS_CLASS_USER);
		
		if (log.isDebugEnabled()) log.debug(identityDocument.toString());
		return identityDocument.getLuceneDocument();
	}	
}