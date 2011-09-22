/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.user.propertyhandlers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <h3>Description:</h3>
 * The property usage context describes the context in with some user properties
 * are used. This could be a form, a table or another context like a downloa of
 * some course score data that do also include user properties.
 * <p>
 * The class specifies a list containing all the properties and several maps
 * that point to objects from the list that have a special attribute.
 * <p>
 * Initial Date: 31.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class UserPropertyUsageContext {
	
	private List<UserPropertyHandler> propertyHandlers = new ArrayList<UserPropertyHandler>();
	private Set<UserPropertyHandler> mandatoryProperties = new HashSet<UserPropertyHandler>();
	private Set<UserPropertyHandler> adminViewOnlyProperties  = new HashSet<UserPropertyHandler>();
	private Set<UserPropertyHandler> userViewReadOnlyProperties  = new HashSet<UserPropertyHandler>();
	
	/**
	 * Spring setter
	 * @param propertyHandlers
	 */
	public void setPropertyHandlers(List<UserPropertyHandler> propertyHandlers) {
		this.propertyHandlers = propertyHandlers;
	}
	/**
	 * Spring setter
	 * @param mandatoryProperties
	 */
	public void setMandatoryProperties(Set<UserPropertyHandler> mandatoryProperties) {
		this.mandatoryProperties = mandatoryProperties;
	}
	/**
	 * Spring setter
	 * @param adminViewOnlyProperties
	 */
	public void setAdminViewOnlyProperties(Set<UserPropertyHandler> adminViewOnlyProperties) {
		this.adminViewOnlyProperties = adminViewOnlyProperties;
	}
	/**
	 * Spring setter
	 * @param userViewReadOnlyProperties
	 */
	public void setUserViewReadOnlyProperties(Set<UserPropertyHandler> userViewReadOnlyProperties) {
		this.userViewReadOnlyProperties = userViewReadOnlyProperties;
	}

	/**
	 * Get a list of all all property handlers available in this context
	 * @return
	 */
	public List<UserPropertyHandler> getPropertyHandlers() {
		return propertyHandlers;
	}
	
	/**
	 * Check if this property handler is mandatory in this context. In forms this
	 * means that the input field is marked as mandatory, in tables it means that
	 * this column is displayed in the default configuration.
	 * 
	 * @param propertyHandler
	 * @return
	 */
	public boolean isMandatoryUserProperty(UserPropertyHandler propertyHandler) {
		return mandatoryProperties.contains(propertyHandler);
	}

	/**
	 * Check if this property handler is only visible to administrative users.
	 * Normal users won't see it in this context. This value overrides the entry
	 * from the getPropertyHandlers() list
	 * 
	 * @param propertyHandler
	 * @return
	 */
	public boolean isForAdministrativeUserOnly(UserPropertyHandler propertyHandler) {
		return adminViewOnlyProperties.contains(propertyHandler);
	}
	
	/**
	 * Check if this property is read only for normal user of read/write.
	 * Administrative users will override this configuration
	 * 
	 * @param propertyHandler
	 * @return
	 */
	public boolean isUserViewReadOnly(UserPropertyHandler propertyHandler) {
		return userViewReadOnlyProperties.contains(propertyHandler);
	}

	
}
