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

import java.io.Serializable;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.ims.qti.QTIResultSet;

import de.bps.onyx.plugin.wsserver.TestState;

public class JMSExamMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7693248725265317323L;

	private final JMSExamMessageCommand command;

	private final Long testSessionId;

	private final Long courseId;

	private final String nodeIdent;

	private final List<Identity> students;

	private final List<QTIResultSet> results;

	private final TestState testState;

	public JMSExamMessage(JMSExamMessageCommand command, Long testSessionId, Long courseId, String nodeIdent, List<Identity> students,
			List<QTIResultSet> results, TestState testState) {
		super();
		this.command = command;
		this.testSessionId = testSessionId;
		this.courseId = courseId;
		this.nodeIdent = nodeIdent;
		this.students = students;
		this.results = results;
		this.testState = testState;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public JMSExamMessageCommand getCommand() {
		return command;
	}

	public Long getTestSessionId() {
		return testSessionId;
	}

	public Long getCourseId() {
		return courseId;
	}

	public String getNodeIdent() {
		return nodeIdent;
	}

	public List<Identity> getStudents() {
		return students;
	}

	public List<QTIResultSet> getResults() {
		return results;
	}

	public TestState getTestState() {
		return testState;
	}
}
/*
history:

$Log: JMSExamMessage.java,v $
Revision 1.2  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/
