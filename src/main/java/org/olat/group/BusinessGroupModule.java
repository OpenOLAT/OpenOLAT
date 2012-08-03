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
package org.olat.group;

import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractOLATModule;
import org.olat.core.configuration.PersistedProperties;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.site.GroupsSite;

/**
 * Description:<br>
 * The business group module initializes the OLAT groups environment.
 * Configurations are loaded from here.
 * <P>
 * Initial Date: 04.11.2009 <br>
 * 
 * @author gnaegi
 */
public class BusinessGroupModule extends AbstractOLATModule {

	public static String ORES_TYPE_GROUP = OresHelper.calculateTypeName(BusinessGroup.class);
	
	private static final String USER_ALLOW_CREATE_BG = "user.allowed.create";
	private static final String AUTHOR_ALLOW_CREATE_BG = "author.allowed.create";
	private static final String CONTACT_BUSINESS_CARD = "contact.business.card";
	
	public static final String CONTACT_BUSINESS_CARD_NEVER = "never";
	public static final String CONTACT_BUSINESS_CARD_ALWAYS = "always";
	public static final String CONTACT_BUSINESS_CARD_GROUP_CONFIG = "groupconfig";


	private boolean userAllowedCreate;
	private boolean authorAllowedCreate;
	private String contactBusinessCard;
	
	
	/**
	 * [used by spring]
	 */
	private BusinessGroupModule() {
		//
	}

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#init()
	 */
	@Override
	public void init() {
		// Add controller factory extension point to launch groups
		NewControllerFactory.getInstance().addContextEntryControllerCreator(BusinessGroup.class.getSimpleName(),
				new BusinessGroupContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("GroupCard",
				new BusinessGroupCardContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator(GroupsSite.class.getSimpleName(),
				new SiteContextEntryControllerCreator(GroupsSite.class));
		
		//set properties
		String userAllowed = getStringPropertyValue(USER_ALLOW_CREATE_BG, true);
		if(StringHelper.containsNonWhitespace(userAllowed)) {
			userAllowedCreate = "true".equals(userAllowed);
		}
		String authorAllowed = getStringPropertyValue(AUTHOR_ALLOW_CREATE_BG, true);
		if(StringHelper.containsNonWhitespace(authorAllowed)) {
			authorAllowedCreate = "true".equals(authorAllowed);
		}
		
		String contactAllowed = getStringPropertyValue(CONTACT_BUSINESS_CARD, true);
		if(StringHelper.containsNonWhitespace(contactAllowed)) {
			contactBusinessCard = contactAllowed;
		}
	}

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#initDefaultProperties()
	 */
	@Override
	protected void initDefaultProperties() {
		userAllowedCreate = getBooleanConfigParameter(USER_ALLOW_CREATE_BG, true);
		authorAllowedCreate = getBooleanConfigParameter(AUTHOR_ALLOW_CREATE_BG, true);
		contactBusinessCard = getStringConfigParameter(CONTACT_BUSINESS_CARD, CONTACT_BUSINESS_CARD_NEVER, true);
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}
	
	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}

	public boolean isUserAllowedCreate() {
		return userAllowedCreate;
	}

	public void setUserAllowedCreate(boolean userAllowedCreate) {
		setStringProperty(USER_ALLOW_CREATE_BG, Boolean.toString(userAllowedCreate), true);
	}

	public boolean isAuthorAllowedCreate() {
		return authorAllowedCreate;
	}

	public void setAuthorAllowedCreate(boolean authorAllowedCreate) {
		setStringProperty(AUTHOR_ALLOW_CREATE_BG, Boolean.toString(authorAllowedCreate), true);
	}

	public String getContactBusinessCard() {
		return contactBusinessCard;
	}

	public void setContactBusinessCard(String contactBusinessCard) {
		setStringProperty(CONTACT_BUSINESS_CARD, contactBusinessCard, true);
	}
	
	
}