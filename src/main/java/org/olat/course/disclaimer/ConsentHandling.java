/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.disclaimer;

/**
 * What happens with the existing consents when the disclaimer
 * configuration of a course is updated.
 *
 * Initial date: 2 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public enum ConsentHandling {

	/** The consents stay as they are */
	KEEP,
	/** The consents are revoked, the participants must accept the disclaimer again */
	REVOKE,
	/** The consents are deleted, only allowed if both disclaimers are disabled */
	REMOVE;
	
	public static final ConsentHandling secureValueOf(String val, ConsentHandling defaultValue) {
		for(ConsentHandling handling:values()) {
			if(handling.name().equalsIgnoreCase(val)) {
				return handling;
			}
		}
		return defaultValue;
	}

}
