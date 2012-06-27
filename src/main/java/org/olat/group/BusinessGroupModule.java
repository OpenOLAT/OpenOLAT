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
	
	public static final String LAST_USAGE_DURATION_PROPERTY_NAME = "LastUsageDuration";
	public static final int DEFAULT_LAST_USAGE_DURATION = 24;
	public static final String DELETE_EMAIL_DURATION_PROPERTY_NAME = "DeleteEmailDuration";
	public static final int DEFAULT_DELETE_EMAIL_DURATION = 30;
	
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
		NewControllerFactory.getInstance().addContextEntryControllerCreator(GroupsSite.class.getSimpleName(),
				new SiteContextEntryControllerCreator(GroupsSite.class));
	}

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#initDefaultProperties()
	 */
	@Override
	protected void initDefaultProperties() {
	// nothing to init
	}

	/**
	 * @see org.olat.core.configuration.AbstractOLATModule#initFromChangedProperties()
	 */
	@Override
	protected void initFromChangedProperties() {
	// nothing to init
	}

	@Override
	public void setPersistedProperties(PersistedProperties persistedProperties) {
		this.moduleConfigProperties = persistedProperties;
	}
	
	public void setLastUsageDuration(int lastUsageDuration) {
		setIntProperty(LAST_USAGE_DURATION_PROPERTY_NAME, lastUsageDuration, true);
	}

	public void setDeleteEmailDuration(int deleteEmailDuration) {
		setIntProperty(DELETE_EMAIL_DURATION_PROPERTY_NAME, deleteEmailDuration, true);
	}

	public int getLastUsageDuration() {
		return DEFAULT_LAST_USAGE_DURATION;// getIntProperty(LAST_USAGE_DURATION_PROPERTY_NAME, DEFAULT_LAST_USAGE_DURATION);
	}

	public int getDeleteEmailDuration() {
		return DEFAULT_DELETE_EMAIL_DURATION;//getIntProperty(DELETE_EMAIL_DURATION_PROPERTY_NAME, DEFAULT_DELETE_EMAIL_DURATION);
	}

}
