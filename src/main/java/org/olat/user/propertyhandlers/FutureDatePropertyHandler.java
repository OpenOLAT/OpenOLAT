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

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.id.User;

/**
 * <h3>Description:</h3> The FutureDatePropertyHandler offers the functionality
 * of a date which lies in the future. (i.e. does not validate on dates that are
 * in the past !)
 * <p>
 * Initial Date: 12.04.2012 <br>
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 */
public class FutureDatePropertyHandler extends DatePropertyHandler {

	/**
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#isValid(org.olat.core.gui.components.form.flexible.FormItem,
	 *      java.util.Map)
	 */
	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		boolean isValidDate = super.isValid(user, formItem, formContext);
		if (!isValidDate)
			return false;

		if (isDateInTheFuture(((DateChooser) formItem).getDate())) {
			return true;
		} else {
			formItem.setErrorKey("form.name.date.future.error");
			return false;
		}
	}

	/**
	 * checks whether the given date is in the future.
	 * if the two dates are on the same date, false is returned!
	 * 
	 * @param dateToCheck
	 * @return true if the given date is in the future
	 */
	private static boolean isDateInTheFuture(Date dateToCheck) {
		Date now = new Date();
		return (!DateUtils.isSameDay(now, dateToCheck)) && dateToCheck.after(now);
	}

}
