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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.onyx.plugin.wsserver;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "studentIdsWrapper")
public class StudentIdsWrapper {
	@XmlElement(name = "studentId")
	private ArrayList<Long> studentsIds;

	public ArrayList<Long> getStudentsIds() {
		return studentsIds;
	}

	public void setStudentsIds(final ArrayList<Long> studentsIds) {
		this.studentsIds = studentsIds;
	}

	@Override
	public String toString() {
		final StringBuffer result = new StringBuffer();
		if (studentsIds != null) {
			result.append('[');
			for (final Long elem : studentsIds) {
				if (result.length() > 1) {
					result.append(',');
				}
				result.append(elem);
			}
			result.append(']');
		} else {
			result.append("null");
		}

		return result.toString();
	}
}
/*
history:

$Log: StudentIdsWrapper.java,v $
Revision 1.3  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/