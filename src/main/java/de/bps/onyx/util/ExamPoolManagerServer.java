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

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti.QTIResultSet;

import de.bps.onyx.plugin.wsserver.TestState;

public class ExamPoolManagerServer extends ExamPoolManager {

	/**
	 * used by spring
	 */
	public ExamPoolManagerServer() {
		super();
		log.info("Created new ExamPoolManagerServer");
	}

	@Override
	protected ExamPool getExamPool(ICourse course, CourseNode courseNode) {

		String sessionIdString = null;

		String latestPublished = String.valueOf(course.getEditorTreeModel().getLatestPublishTimestamp());
		if (latestPublished != null && latestPublished.length() > 5) {
			latestPublished = latestPublished.substring(latestPublished.length() - 4);
		}
		sessionIdString = latestPublished + courseNode.getIdent();
		if(log.isDebug()){
			log.debug("calculated id : " + sessionIdString);
		}

		Long testSessionId = null;

		try {
			testSessionId = Long.valueOf(sessionIdString);
		} catch (Exception e) {
			log.error("Unable to parse testSessionId : " + sessionIdString, e);
		}

		ExamPool pool = getExamPool(testSessionId);
		if (pool == null) {
			try {
				lock.lock();
				pool = getExamPool(testSessionId);
				if (pool == null) {
					log.info("Add new ExamPool for testSessionId : " + testSessionId);
					pool = new ExamPool(testSessionId);
					pool.initExamPool(course, courseNode);
					examPools.put(testSessionId, pool);
				}
			} catch (Exception e) {
				log.error("Unable to connect to onyxExamMode ", e);
			} finally {
				lock.unlock();
			}
		}
		return pool;
	}

	@Override
	protected ExamPool getExamPool(Long testSessionId) {
		ExamPool pool = null;
		if(testSessionId != null){
			pool = examPools.get(testSessionId);
		}
		return pool;
	}

	@Override
	public Long addStudentToExamPool(ICourse course, CourseNode courseNode, Identity student, TestState state, QTIResultSet resultSet) {
		Long result = null;
		ExamPool pool = getExamPool(course, courseNode);
		if (pool != null) {
			if (resultSet != null) {
				result = pool.registerStudentTest(student, resultSet, state);
				TestState resultState = TestState.getState(result);
				if (resultState == TestState.ERROR_REGISTER_STUDENT_WITH_UNKNOWN_TEST_ID) {
					log.info("Onyx does not know the test, re-register it and register the student again.");
					pool.initExamPool(course, courseNode);
					result = pool.registerStudentTest(student, resultSet, state);
				}
			} else {
				pool.addStudent(student, state);
			}
			ExamEvent.fireEvent(JMSExamMessageCommand.ADD_STUDENT.toString(), pool.getTestSessionId());
		} else {
			log.warn("Tried to add student to non-existing exampool / testSessionId. Course: " + (course != null ? course.getResourceableId() : "NULL")
					+ " and CourseNode: " + (courseNode != null ? courseNode.getIdent() : "NULL"));
		}

		return result;
	}


	@Override
	public void controllExam(Long testSessionId, List<Identity> selectedIdentites, TestState state) {
		ExamPool pool = getExamPool(testSessionId);
		if (pool != null) {
			log.info("Tried to control exam for testSessionId: " + testSessionId + " to state: " + (state != null ? state.toString() : "NULL")
					+ " with identities : " + selectedIdentites);
			pool.controllExam(selectedIdentites, state);
			ExamEvent.fireEvent(JMSExamMessageCommand.CONTROLL_EXAM.toString(), pool.getTestSessionId());
		} else {
			log.warn("Tried to control exam for none-existing exampool: " + testSessionId + " to state: " + (state != null ? state.toString() : "NULL")
					+ " with identities : " + selectedIdentites);
		}
	}

	@Override
	public void changeExamState(Long testSessionId, List<Identity> identities, TestState state) {
		ExamPool pool = getExamPool(testSessionId);
		if (pool != null) {
			pool.changeExamState(identities, state);
			ExamEvent.fireEvent(JMSExamMessageCommand.CHANGE_STATE.toString(), pool.getTestSessionId());
		} else {
			log.warn("Tried to change exam state for none-existing exampool: " + testSessionId);
		}
	}
}
/*
history:

$Log: ExamPoolManagerServer.java,v $
Revision 1.10  2012-06-01 11:54:32  blaw
OLATCE-2319
* bind generated test-session-id to last-publish-date of course to use changes in the course-node-configuration immediately then they are published

Revision 1.9  2012-05-24 15:01:26  blaw
OLATCE-2007
* improvement for resuming tests
* re-register test if onyx had been restartet since first test-registration

Revision 1.8  2012-05-16 13:30:34  blaw
OLATCE-2007
* improved resume of suspended tests

Revision 1.7  2012-05-09 16:03:48  blaw
OLATCE-2007
* allow suspend and resume of tests

Revision 1.6  2012-04-10 13:37:02  blaw
OLATCE-1425
* more logging

Revision 1.5  2012-04-05 14:16:55  blaw
OLATCE-1425
* use examcode from gui as testsessionId

Revision 1.4  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/
