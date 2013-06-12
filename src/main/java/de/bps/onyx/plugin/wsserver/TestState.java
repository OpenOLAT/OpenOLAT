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

public enum TestState {
	// generic
	OK(42),

	// states
	WAITING(0), WORKING(1), FINISHED(2), RESUME_ALLOWED(3), NOT_ENTERED(4), CANCELED(5), RESUME_REQUESTED(6), RESUMED(7), DISCONNECTED(8), SUSPENDED(9), RESUME_SUSPENDED(
			10),

	// ERRORs
	ERROR_RETURN_RESULTS(0xFF01),
	ERROR_MULTIPLE_REGISTER_STUDENT_WITH_SAME_ID_CALLS(0xFF02),
	ERROR_REGISTER_STUDENT_WITH_UNKNOWN_TEST_ID(0xFF03),
	ERROR_REGISTER_STUDENT_WITH_EMPTY_TEST_ID(0xFF04),
	ERROR_REGISTER_STUDENT_WITH_EMPTY_STUDENT_ID(0xFF05),

	// Other
	UNKNOWN(-1);

	private final int value;

	private TestState(final int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Returns the enum value for the given state or UNKNOWN, if unknown state.
	 * 
	 * @param state
	 * @return The enum value for the given state or UNKNOWN, if unknown state.
	 */
	public static TestState getState(final long state) {
		TestState result = UNKNOWN;
		for (final TestState status : values()) {
			if (status.value == state) {
				result = status;
				break;
			}
		}
		return result;
	}
}
/*
history:

$Log: TestState.java,v $
Revision 1.10  2012-05-09 16:03:48  blaw
OLATCE-2007
* allow suspend and resume of tests

Revision 1.9  2012-05-07 13:28:28  laeb
OPEN - issue OLATCE-2010: Unterbrechen: Zwischenergebnisse verwalten
https://www.bps-system.de/devel/browse/OLATCE-2010
* added TestState SUSPENDED

Revision 1.8  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/