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
package org.olat.modules.curriculum;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 19 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum CurriculumElementStatus {
	
	preparation,
	provisional,
	confirmed,
	active,
	cancelled,
	finished,
	@Deprecated
	inactive,
	deleted;
	
	
	/**
	 * @return true if finished, inactive or deleted
	 */
	public boolean isClosed() {
		return this == finished || this == inactive || this == deleted;
	}
	
	public static boolean isValueOf(String val) {
		boolean ok = false;
		if(StringHelper.containsNonWhitespace(val)) {
			for(CurriculumElementStatus status:values()) {
				if(status.name().equals(val)) {
					ok = true;
				}
			}
		}
		return ok;
	}
	
	public static CurriculumElementStatus[] visibleUser() {
		return new CurriculumElementStatus[] {
				CurriculumElementStatus.provisional, CurriculumElementStatus.confirmed,
				CurriculumElementStatus.active, CurriculumElementStatus.cancelled,
				CurriculumElementStatus.finished };
	}
	
	public static CurriculumElementStatus[] visibleAdmin() {
		return new CurriculumElementStatus[] { CurriculumElementStatus.preparation,
				CurriculumElementStatus.provisional, CurriculumElementStatus.confirmed,
				CurriculumElementStatus.active, CurriculumElementStatus.cancelled,
				CurriculumElementStatus.finished, CurriculumElementStatus.deleted };
	}
	
	public static CurriculumElementStatus[] notDeleted() {
		return new CurriculumElementStatus[] { CurriculumElementStatus.preparation,
				CurriculumElementStatus.provisional, CurriculumElementStatus.confirmed,
				CurriculumElementStatus.active, CurriculumElementStatus.cancelled,
				CurriculumElementStatus.finished, CurriculumElementStatus.inactive };
	}
}
