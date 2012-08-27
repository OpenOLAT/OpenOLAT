/**
 * <a href=“http://www.openolat.org“>
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
 * 28. August 2012 by frentix GmbH, http://www.frentix.com
 * <p>
 **/

package org.olat.user;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * <h3>Description:</h3> This interceptor checks if the user has filled all
 * mandatory fields of the profile form. If yes, the controller creator method
 * creates the profile form controller, if no it returns null
 * <p>
 * Note that the controller does not actually validate each field, it only
 * checks if any mandatory field is empty.
 * <p>
 * <h3>Events thrown by this controller:</h3>
 * <p>
 * none
 * <p>
 * Initial Date: 27.08.2012 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class ProfileAndHomePageEditIfMissingMandatoryControllerCreator
		extends AutoCreator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.olat.core.gui.control.creator.ControllerCreator#createController(
	 * org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public ProfileAndHomePageEditController createController(UserRequest lureq,
			WindowControl lwControl) {

		// get all userproperties from the profile form
		UserManager um = UserManager.getInstance();
		String usageIdentifier = ProfileFormController.class.getCanonicalName();
		List<UserPropertyHandler> userPropertyHandlers = um
				.getUserPropertyHandlersFor(usageIdentifier, false);
		// check if all mandatory properties are set
		boolean foundMissingMandatory = false; // think positive!
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (um.isMandatoryUserProperty(usageIdentifier, userPropertyHandler)) {
				String curValue = userPropertyHandler.getUserProperty(lureq
						.getIdentity().getUser(), lureq.getLocale());
				if (!StringHelper.containsNonWhitespace(curValue)) {
					foundMissingMandatory = true;
					break;
				}
			}
		}
		// only create profile controller when profile contains empty mandatory
		// fields
		if (foundMissingMandatory) {
			return new ProfileAndHomePageEditController(lureq, lwControl);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.olat.core.gui.control.creator.AutoCreator#getClassName()
	 */
	@Override
	public String getClassName() {
		return ProfileAndHomePageEditController.class.getName();
	}

	
}
