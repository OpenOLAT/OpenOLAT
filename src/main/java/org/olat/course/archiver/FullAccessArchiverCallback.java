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
package org.olat.course.archiver;

/**
 * 
 * Initial date: 25.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FullAccessArchiverCallback implements IArchiverCallback {

	@Override
	public boolean mayArchiveQtiResults() {
		return true;
	}
	
	@Override
	public boolean mayArchiveQtiTestResults() {
		return true;
	}

	@Override
	public boolean mayArchiveLogfiles() {
		return true;
	}

	@Override
	public boolean mayArchiveCoursestructure() {
		return true;
	}

	@Override
	public boolean mayArchiveProperties() {
		return true;
	}

	@Override
	public boolean mayArchiveHandedInTasks() {
		return true;
	}

	@Override
	public boolean mayArchiveForums() {
		return true;
	}

	@Override
	public boolean mayArchiveDialogs() {
		return true;
	}

	@Override
	public boolean mayArchiveWikis() {
		return true;
	}

	@Override
	public boolean mayArchiveScorm() {
		return true;
	}

	@Override
	public boolean mayArchiveChecklist() {
		return true;
	}
	
	@Override
	public boolean mayArchiveParticipantFolder() {
		return true;
	}

	@Override
	public boolean mayArchiveProjectBroker() {
		return true;
	}

	@Override
	public boolean mayArchiveSurveys() {
		return true;
	}

	@Override
	public boolean mayArchiveForms() {
		return true;
	}

	@Override
	public boolean mayArchiveVideoTasks() {
		return true;
	}
}
