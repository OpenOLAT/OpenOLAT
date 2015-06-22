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
package org.olat.course.nodes.gta;

/**
 * 
 * Initial date: 05.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum TaskProcess {
	//the list is in the chronological order
	assignment, 	// Student has to get the assignment, assignment not yet done 
	submit,			// Student has assignment and is now working on his assignment. In the end he has to submit his work
	review,			// Submission is done, the coach has now access to the submitted docs and reviews them. The coach uploads a corrected version or some feedback.
	revision,		// The coach decided that the submitted docs are not yet good enough. The student can read the coach feedback and can submit a revised version or additional docs.
	correction,		// The submission of the revised docs is done, the coach has now access to them. If satisfied he closes the review or sets the process to another revision state.
	solution,		// The example solutions are now accessible to the student
	grading,		// The review and correction process are done, the coach can now grade the word
	graded			// Grading is done and the students gets his results
}
