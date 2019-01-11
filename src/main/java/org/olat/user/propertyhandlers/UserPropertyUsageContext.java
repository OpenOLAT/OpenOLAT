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

	private String description = "";

	private List<UserPropertyHandler> propertyHandlers = new ArrayList<>();
	private Set<UserPropertyHandler> mandatoryProperties = new HashSet<>();
	private Set<UserPropertyHandler> adminViewOnlyProperties = new HashSet<>();
	private Set<UserPropertyHandler> userViewReadOnlyProperties = new HashSet<>();

	/**
	 * Spring setter
	 * 
	 * @param propertyHandlers
	 */
	public void setPropertyHandlers(List<UserPropertyHandler> propertyHandlers) {
		this.propertyHandlers = propertyHandlers;
	}

	/**
	 * Spring setter
	 * 
	 * @param mandatoryProperties
	 */
	public void setMandatoryProperties(Set<UserPropertyHandler> mandatoryProperties) {
		this.mandatoryProperties = mandatoryProperties;
	}

	/**
	 * Spring setter
	 * 
	 * @param adminViewOnlyProperties
	 */
	public void setAdminViewOnlyProperties(Set<UserPropertyHandler> adminViewOnlyProperties) {
		this.adminViewOnlyProperties = adminViewOnlyProperties;
	}

	/**
	 * Spring setter
	 * 
	 * @param userViewReadOnlyProperties
	 */
	public void setUserViewReadOnlyProperties(Set<UserPropertyHandler> userViewReadOnlyProperties) {
		this.userViewReadOnlyProperties = userViewReadOnlyProperties;
	}

	/**
	 * Spring setter
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * returns the description of this context (description is defined injected
	 * via spring (xml) )
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Get a list of all all property handlers available in this context
	 * 
	 * @return
	 */
	public List<UserPropertyHandler> getPropertyHandlers() {
		return propertyHandlers;
	}

	/**
	 * adds the given handler to this context
	 * 
	 * @param propertyHandler
	 */
	public void addPropertyHandler(UserPropertyHandler propertyHandler) {
		if (propertyHandlers.contains(propertyHandler)) return; // do not add twice
		propertyHandlers.add(propertyHandler);
	}
	
	public void addPropertyHandler(int index, UserPropertyHandler propertyHandler) {
		if (propertyHandlers.contains(propertyHandler)) return; // do not add twice
		
		if(index < 0 && index >= propertyHandlers.size()) {
			propertyHandlers.add(propertyHandler);
		} else {
			propertyHandlers.add(index, propertyHandler);
		}
	}

	/**
	 * removes the given propertyHandler from this context
	 * 
	 * @param propertyHandler
	 */
	public void removePropertyHandler(UserPropertyHandler propertyHandler) {
		propertyHandlers.remove(propertyHandler);
		// could be in one of the sets, remove there as well! (consistency)
		mandatoryProperties.remove(propertyHandler);
		adminViewOnlyProperties.remove(propertyHandler);
		userViewReadOnlyProperties.remove(propertyHandler);
	}

	/**
	 * checks whether this context contains the given handler
	 * 
	 * @param propertyHandler
	 * @return returns true if the given handler is part of this context
	 */
	public boolean contains(UserPropertyHandler propertyHandler) {
		return propertyHandlers.contains(propertyHandler);
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
	 * adds or removes the given propertyHandler to the set of mandatory handlers.
	 * if given propertyHandler is not part of this context, the handler is not
	 * added to the set. (consistency)
	 * 
	 * @param propertyHandler The propertyHandler to add to the set of mandatory
	 *          Handlers
	 */
	public void setAsMandatoryUserProperty(UserPropertyHandler propertyHandler, boolean isMandatory) {
		if (isMandatory) {
			if (!this.propertyHandlers.contains(propertyHandler)) return;
			this.mandatoryProperties.add(propertyHandler);
		} else {
			this.mandatoryProperties.remove(propertyHandler);
		}
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
	 * adds or removes the given propertyHandler to the set of adminOnly handlers.
	 * if given propertyHandler is not part of this context, the handler is not
	 * added to the set. (consistency)
	 * 
	 * @param propertyHandler The propertyHandler to add to the set of
	 *          adminUserOnly Handlers
	 * @param isAdminOnly
	 */
	public void setAsAdminstrativeUserOnly(UserPropertyHandler propertyHandler, boolean isAdminOnly) {
		if (isAdminOnly) {
			if (!this.propertyHandlers.contains(propertyHandler)) return;
			this.adminViewOnlyProperties.add(propertyHandler);
		} else {
			this.adminViewOnlyProperties.remove(propertyHandler);
		}
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

	/**
	 * adds or removes the given propertyHandler to the set of userReadOnly
	 * handlers. if given propertyHandler is not part of this context, the handler
	 * is not added to the set. (consistency)
	 * 
	 * @param propertyHandler The propertyHandler to add to the set of
	 *          userReadOnly Handlers
	 * @param isUserReadOnly
	 */
	public void setAsUserViewReadOnly(UserPropertyHandler propertyHandler, boolean isUserReadOnly) {
		if (isUserReadOnly) {
			if (!this.propertyHandlers.contains(propertyHandler)) return;
			this.userViewReadOnlyProperties.add(propertyHandler);
		} else {
			this.userViewReadOnlyProperties.remove(propertyHandler);
		}
	}

	/**
	 * Moves the given Handler one position up in the propertyHandlers-List of
	 * this context If the given Handler is already at the first position, it is
	 * moved to the end of the list
	 * 
	 * if the given Handler is not part of this context, nothing is changed.
	 * 
	 * @param propertyHandler
	 */
	public void moveHandlerUp(UserPropertyHandler propertyHandler) {
		int indexBefore = propertyHandlers.indexOf(propertyHandler);
		if (indexBefore < 0) return;
		if (indexBefore == 0) {
			propertyHandlers.remove(indexBefore);
			propertyHandlers.add(propertyHandler);
		} else {
			propertyHandlers.remove(indexBefore);
			propertyHandlers.add(indexBefore - 1, propertyHandler);
		}

	}

	/**
	 * moves the given Handler one position down in the propertyHandlers-List of
	 * this context. If the given Handler is already at the last position, it is
	 * moved to top (first position)
	 * 
	 * if the given Handler is not part of this context, nothing is changed.
	 * 
	 * @param propertyHandler
	 */
	public void moveHandlerDown(UserPropertyHandler propertyHandler) {
		int indexBefore = propertyHandlers.indexOf(propertyHandler);
		if (indexBefore < 0) return;

		if (indexBefore == (propertyHandlers.size() - 1)) {
			propertyHandlers.remove(indexBefore);
			propertyHandlers.add(0, propertyHandler);
		} else {
			propertyHandlers.remove(indexBefore);
			propertyHandlers.add(indexBefore + 1, propertyHandler);
		}
	}

}
