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
package org.olat.modules.lecture.ui.filter;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.modules.lecture.LectureBlock;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 16 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FilterHelper {
	
	private FilterHelper() {
		//
	}
	
	public static boolean test(RepositoryEntry entry, String searchString) {
		if(entry == null) return false;
		return test(entry.getDisplayname(), searchString)
			|| test(entry.getExternalRef(), searchString)
			|| test(entry.getExternalId(), searchString);
	}
	
	public static boolean test(LectureBlock lectureBlock, String searchString) {
		if(lectureBlock == null) return false;
		return test(lectureBlock.getTitle(), searchString)
			|| test(lectureBlock.getLocation(), searchString);
	}
	
	public static boolean test(Identity identity, String searchString) {
		if(identity == null || identity.getUser() == null) return false;
		User user = identity.getUser();
		return test(user.getLastName(), searchString)
			|| test(user.getFirstName(), searchString);
	}
	
	public static boolean test(User user, String searchString,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		if(user == null || userPropertyHandlers == null) return false;
		
		for(UserPropertyHandler handler:userPropertyHandlers) {
			String val = handler.getUserProperty(user, locale);
			if(test(val, searchString)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean test(UserPropertiesRow userPropertiesRow, String searchString) {
		if(userPropertiesRow == null || userPropertiesRow.getIdentityProps() == null) return false;
		
		for(String val:userPropertiesRow.getIdentityProps()) {
			if(test(val, searchString)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean test(String value, String searchString) {
		return value != null && value.toLowerCase().contains(searchString);
	}
}
