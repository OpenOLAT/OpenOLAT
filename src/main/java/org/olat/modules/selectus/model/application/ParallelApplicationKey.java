/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.model.application;

import java.util.Objects;

import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.RecruitingDuplicateApplicationAlgorithm;
import org.olat.modules.selectus.model.Person;

/**
 * 
 * Initial date: 17 juin 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ParallelApplicationKey {
	
	private final String email;
	private final String firstName;
	private final String lastName;
	
	private ParallelApplicationKey(String email, String firstName, String lastName) {
		this.email = toLowerCase(email);
		this.firstName = toLowerCase(firstName);
		this.lastName = toLowerCase(lastName);
	}
	
	public boolean isEmpty() {
		return !StringHelper.containsNonWhitespace(email)
				&& !StringHelper.containsNonWhitespace(firstName)
				&& !StringHelper.containsNonWhitespace(lastName);
	}
	
	private static final String toLowerCase(String val) {
		return val == null ? null : val.toLowerCase();
	}
	
	public static final ParallelApplicationKey valueOf(ParallelApplication app, RecruitingDuplicateApplicationAlgorithm algorithm) {
		if(algorithm == RecruitingDuplicateApplicationAlgorithm.EMAIL) {
			return new ParallelApplicationKey(app.getApplicationEmail(), null, null);
		}
		return new ParallelApplicationKey(app.getApplicationEmail(), app.getApplicationFirstName(), app.getApplicationLastName());
	}
	
	public static final ParallelApplicationKey valueOf(Person person, RecruitingDuplicateApplicationAlgorithm algorithm) {
		if(algorithm == RecruitingDuplicateApplicationAlgorithm.EMAIL) {
			return new ParallelApplicationKey(person.getMail(), null, null);
		}
		return new ParallelApplicationKey(person.getMail(), person.getFirstName(), person.getLastName());
	}

	@Override
	public int hashCode() {
		return (email == null ? 473658 : email.hashCode())
				+ (firstName == null ? 19208 : firstName.hashCode())
				+ (lastName == null ? -238479 : lastName.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ParallelApplicationKey) {
			ParallelApplicationKey other = (ParallelApplicationKey) obj;
			return Objects.equals(email, other.email) && Objects.equals(firstName, other.firstName)
					&& Objects.equals(lastName, other.lastName);
		}
		return false;
	}
}
