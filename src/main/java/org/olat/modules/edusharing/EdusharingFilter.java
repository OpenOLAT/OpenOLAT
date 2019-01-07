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
package org.olat.modules.edusharing;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingFilter implements Filter {
	
	private static final OLog log = Tracing.createLoggerFor(EdusharingFilter.class);
	
	private final Identity identity;
	private final EdusharingProvider provider;

	@Autowired
	private EdusharingModule edusharingModule;
	@Autowired
	private EdusharingService edusharingService;
	@Autowired
	private EdusharingHtmlService htmlService;
	
	public EdusharingFilter(Identity identity, EdusharingProvider provider) {
		this.identity = identity;
		this.provider = provider;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public String filter(String original) {
		if (edusharingModule.isEnabled()) {
			// Get all edu-sharing elements from html
			List<EdusharingHtmlElement> htmlElements = htmlService.parse(original);
			Map<String, EdusharingHtmlElement> htmlIdentifierToElement = htmlElements.stream().collect(Collectors.toMap(u -> u.getIdentifier(), u -> u));
			Set<String> htmlIdentifiers = htmlIdentifierToElement.keySet();
			log.debug("edu-sharing filter identifiers in html: " + htmlIdentifiers.toString());
			
			// Get all usages from database
			List<EdusharingUsage> usages = edusharingService.loadUsages(provider.getOlatResourceable());
			Map<String, EdusharingUsage> usageIdentifierToUsage = usages.stream().collect(Collectors.toMap(u -> u.getIdentifier(), u -> u));
			Set<String> usageIdentifiers = usageIdentifierToUsage.keySet();
			log.debug("edu-sharing filter identifiers in database: " + usageIdentifiers.toString());
			
			// create
			Collection<String> createIdentifiers = new HashSet<>(htmlIdentifiers);
			createIdentifiers.removeAll(usageIdentifiers);
			for (String identifier: createIdentifiers) {
				try {
					EdusharingHtmlElement element = htmlIdentifierToElement.get(identifier);
					edusharingService.createUsage(identity, element, provider);
				} catch (Exception e) {
					original = htmlService.deleteNode(original, identifier);
					log.warn("edu-sharing usage creation failed. identifier=" + identifier + ", resType="
							+ provider.getOlatResourceable().getResourceableTypeName() + ", resId="
							+ provider.getOlatResourceable().getResourceableId(), e);
				}
			}
			
			// delete
			Collection<String> deleteIdentifiers = new HashSet<>(usageIdentifiers);
			deleteIdentifiers.removeAll(htmlIdentifiers);
			for (String identifier: deleteIdentifiers) {
				try {
					edusharingService.deleteUsage(identity, identifier);
				} catch (EdusharingException e) {
					log.warn("edu-sharing usage deletion failed. identifier=" + identifier + ", resType="
							+ provider.getOlatResourceable().getResourceableTypeName() + ", resId="
							+ provider.getOlatResourceable().getResourceableId(), e);
				}
			}
		}
		
		return original;
	}

}
