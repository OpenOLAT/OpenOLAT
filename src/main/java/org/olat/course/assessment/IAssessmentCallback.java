/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.assessment;

/**
 * Initial Date:  Jun 18, 2004
 * @author gnaegi
 */
public interface IAssessmentCallback {
    /**
     * @return True if all users assessments should be visible
     * Guarantees read only mode
     */
	public boolean mayViewAllUsersAssessments();
	/**
	 * @return True if all users assessments should be visible 
	 * and editable. Guarantees read-write mode.
	 */
	public boolean mayAssessAllUsers();
	/**
	 * @return True if coached users assessments should be visible 
	 * and editable. Guarantees read-write mode for a coach to its
	 * learners.
	 */
	public boolean mayAssessCoachedUsers();
	
	/**
	 * @return True if the user can trigger the recalculation of
	 * all efficiency statements.
	 */
	public boolean mayRecalculateEfficiencyStatements();
}
