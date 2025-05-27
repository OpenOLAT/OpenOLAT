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
package org.olat.modules.curriculum.manager;

/**
 * 
 * Initial date: 27 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public record BookingKey(Long curriculumElementKey, Long identityKey) {
	
	@Override
	public int hashCode() {
		return (curriculumElementKey == null ? 36528947 : curriculumElementKey.hashCode())
				+ (identityKey == null ? -24528 : identityKey.hashCode());
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof BookingKey bookingKey) {
			return curriculumElementKey() != null && curriculumElementKey().equals(bookingKey.curriculumElementKey())
					&& identityKey() != null && identityKey().equals(bookingKey.identityKey());
		}
		return false;
	}
}
