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
package org.olat.user;

import java.util.Comparator;

import org.olat.basesecurity.IdentityShort;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 7 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IdentityComporatorFactory {

	private static final Comparator<Identity> IDENTITY_LAST_FIRST_COMPARATOR = new IdentityComporator();
	private static final Comparator<IdentityShort> IDENTITY_SHORT_LAST_FIRST_COMPARATOR = new IdentityShortComporator();
	
	public static Comparator<Identity> createLastnameFirstnameComporator() {
		return IDENTITY_LAST_FIRST_COMPARATOR;
	}
	
	public static Comparator<IdentityShort> createLastnameFirstnameShortComporator() {
		return IDENTITY_SHORT_LAST_FIRST_COMPARATOR;
	}
	
	private IdentityComporatorFactory() {
	}
	
	private static int compareIdentities(String lastName1, String lastName2, String firstName1, String firstName2) {
		// Compare last name...
		// nulls last
		if (lastName1 == null) return 1;
		if (lastName2 == null) return -1;
		
		int lastNameComp = lastName1.toLowerCase().compareTo(lastName2.toLowerCase());
		if (lastNameComp != 0) return lastNameComp;
		
		// ...and then the fist name
		// nulls last
		if (firstName1 == null) return 1;
		if (firstName2 == null) return -1;
		
		return firstName1.toLowerCase().compareTo(firstName2.toLowerCase());
	}
	
	private static final class IdentityComporator implements Comparator<Identity> {

		@Override
		public int compare(Identity i1, Identity i2) {
			return compareIdentities(i1.getUser().getLastName(), i2.getUser().getLastName(),
					i1.getUser().getFirstName(), i2.getUser().getFirstName());
		}
	}	
	
	private static final class IdentityShortComporator implements Comparator<IdentityShort> {

		@Override
		public int compare(IdentityShort i1, IdentityShort i2) {
			return compareIdentities(i1.getLastName(), i2.getLastName(), i1.getFirstName(), i2.getFirstName());
		}
		
	}
	
}