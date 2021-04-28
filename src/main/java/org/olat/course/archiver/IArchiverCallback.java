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

package org.olat.course.archiver;

/**
 * Initial Date:  May 26, 2004
 * @author gnaegi
 */
public interface IArchiverCallback {
	/**
	 * @return true if user has rights to archive course qti results, false otherwhise
	 */
	public boolean mayArchiveQtiResults();
	
	/**
	 * @return true if user has rights to archive course qti test results, false otherwhise
	 */
	public boolean mayArchiveQtiTestResults();
	/**
	 * @return true if user has rights to archive logfiles, false otherwhise
	 */
	public boolean mayArchiveLogfiles();
	/**
	 * @return true if user has rights to archive the course structure, false otherwhise
	 */
	public boolean mayArchiveCoursestructure();
	/**
	 * @return true if user has rights to archive the course properties, false otherwhise
	 */
	public boolean mayArchiveProperties();
	/**
	 * @return true if user has rights to archive the dropbox of tasknodes
	 */
	public boolean mayArchiveHandedInTasks();
	/**
	 * @return true if user has rights to archive forums
	 */
	public boolean mayArchiveForums();
	/**
	 * @return true if user has rights to archive dialogs
	 */
	public boolean mayArchiveDialogs();
	
	/**
	 * 
	 * @return true if user has rights to archive wikis
	 */
	public boolean mayArchiveWikis();
	
	public boolean mayArchiveScorm();
	
	public boolean mayArchiveChecklist();
	
	public boolean mayArchiveParticipantFolder();
	
	/**
	 * @return true if user has rights to archive project-broker data
	 */
	public boolean mayArchiveProjectBroker();

	public boolean mayArchiveSurveys();
	
	public boolean mayArchiveForms();
	
}
