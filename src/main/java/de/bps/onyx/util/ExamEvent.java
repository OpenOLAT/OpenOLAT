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
package de.bps.onyx.util;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;

public class ExamEvent extends MultiUserEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7467023441639979511L;

	private final static String RES_TYPE = "onyx_exam_control_event";
	private final static OLATResourceable ores = OresHelper.createOLATResourceableInstance(RES_TYPE, 0l);

	private final Long testSessionId;


	public ExamEvent(String command, Long testSessionId) {
		super(command);
		this.testSessionId = testSessionId;
	}

	public Long getTestSessionId() {
		return testSessionId;
	}

	public static OLATResourceable getCoordinatorResourceable() {
		return ores;
	}

	public static void fireEvent(String command, Long testSessionId) {
		ExamEvent event = new ExamEvent(command, testSessionId);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, ores);
	}
}
/*
history:

$Log: ExamEvent.java,v $
Revision 1.3  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/