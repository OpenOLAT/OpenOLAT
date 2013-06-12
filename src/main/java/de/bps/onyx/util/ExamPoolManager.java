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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti.QTIResultSet;

import de.bps.onyx.plugin.wsserver.TestState;

public abstract class ExamPoolManager {

	protected final static OLog log = Tracing.createLoggerFor(ExamPoolManager.class);

	protected final static Map<Long, ExamPool> examPools = new ConcurrentHashMap<Long, ExamPool>();

	protected final static Lock lock = new ReentrantLock();

	public final static String CONFIG_KEY_EXAM_CONTROL = "examcontrol";
	public final static String CONFIG_KEY_EXAM_CONTROL_SYNCHRONIZED_START = "examsynchronizedstart";

	private static ExamPoolManager INSTANCE;

	protected ExamPoolManager() {
		super();
		try {
			lock.lock();
			if (INSTANCE == null) {
				INSTANCE = this;
				log.info("Set instance to " + (INSTANCE != null ? INSTANCE.getClass() : "NULL"));
			}
		} finally {
			lock.unlock();
		}
	}

	public static ExamPoolManager getInstance() {
		return INSTANCE;
	}

	protected abstract ExamPool getExamPool(ICourse course, CourseNode courseNode);

	protected abstract ExamPool getExamPool(Long testSessionId);


	public abstract Long addStudentToExamPool(ICourse course, CourseNode courseNode, Identity student, TestState state, QTIResultSet resultSet);

	public abstract void controllExam(Long testSessionId, List<Identity> selectedIdentites, TestState state);

	public abstract void changeExamState(Long testSessionId, List<Identity> identities, TestState state);

	public Map<Identity, TestState> getStudentStates(Long testSessionId) {
		Map<Identity, TestState> result = null;
		ExamPool pool = getExamPool(testSessionId);
		if (pool != null) {
			result = pool.getStudentStates();
		} else {
			result = new HashMap<Identity, TestState>();
		}

		return result;
	}

	public TestState getStudentState(Long testSessionId, Identity identity) {
		TestState state = TestState.UNKNOWN;
		ExamPool pool = getExamPool(testSessionId);
		if (pool != null) {
			state = pool.getStudentState(identity);
			if (state == null) {
				state = TestState.NOT_ENTERED;
			}
		}
		return state;
	}

	public Long getExamPoolId(ICourse course, CourseNode courseNode) {
		Long result = null;
		ExamPool pool = getExamPool(course, courseNode);

		if (pool != null) {
			result = pool.getTestSessionId();
		}

		return result;
	}

	public Identity getStudentForAssessment(Long testSessionId, Long assessmentId) {
		Identity student = null;
		ExamPool pool = getExamPool(testSessionId);
		if (pool != null) {
			student = pool.getStudentForAssessment(assessmentId);
		} else {
			log.warn("Tried to get student for none-existing exampool: " + testSessionId);
		}
		return student;
	}

	public QTIResultSet getAssessmentForStudent(Long testSessionId, Identity identity) {
		QTIResultSet result = null;
		ExamPool pool = getExamPool(testSessionId);
		if (pool != null && identity != null) {
			result = pool.getAssessmentForStudent(identity);
		} else {
			log.warn("Tried to get assessment for none-existing " + (pool == null ? ("exampool: " + testSessionId) : "identity"));
		}
		return result;
	}

}
/*
history:

$Log: ExamPoolManager.java,v $
Revision 1.10  2012-04-18 12:10:01  blaw
OLATCE-1425
* enable test-resume for tests with restricted number of tries

Revision 1.9  2012-04-10 13:37:02  blaw
OLATCE-1425
* more logging

Revision 1.8  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/