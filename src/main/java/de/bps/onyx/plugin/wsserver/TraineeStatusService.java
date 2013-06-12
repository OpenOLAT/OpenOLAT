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
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

import de.bps.onyx.util.ExamPoolManager;

@WebService(name = "TraineeStatusService", serviceName = "TraineeStatusService", targetNamespace = "http://test.plugin.bps.de/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class TraineeStatusService {
	private final static OLog log = Tracing.createLoggerFor(TraineeStatusService.class);

	@WebMethod(operationName = "requestExamsRestart")
	public void requestExamsRestart(@WebParam(name = "params") MapWrapper params) {
		log.info("requestExamsRestart: " + params);
	}

	@WebMethod(operationName = "updateStatus")
	public void updateStatus(@WebParam(name = "testSessionId") Long testSessionId, @WebParam(name = "studentIds") StudentIdsWrapper studentIds,
			@WebParam(name = "status") Integer status) {
		TestState testState = TestState.getState(status);
		log.info("updateStatus: " + testSessionId + " # " + studentIds + " # " + testState);

		List<Identity> students = new ArrayList<Identity>();
		for (Long studentId : studentIds.getStudentsIds()) {
			students.add(ExamPoolManager.getInstance().getStudentForAssessment(testSessionId, studentId));
		}
		ExamPoolManager.getInstance().changeExamState(testSessionId, students, testState);
	}

	@WebMethod(operationName = "updateTraineeTestState")
	public void updateTraineeTestState(@WebParam(name = "testSessionId") Long testSessionId, @WebParam(name = "studentId") Long studentId,
			@WebParam(name = "params") MapWrapper params, @WebParam(name = "data") byte[] data) {
		log.info("updateTraineeTestState: " + testSessionId + " # " + studentId + " # " + data.length + "bytes" + " # " + params);
	}
}
/*
history:

$Log: TraineeStatusService.java,v $
Revision 1.5  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/