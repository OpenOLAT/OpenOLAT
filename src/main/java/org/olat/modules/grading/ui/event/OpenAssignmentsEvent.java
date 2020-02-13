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
package org.olat.modules.grading.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.grading.model.GradingAssignmentSearchParameters.SearchStatus;

/**
 * 
 * Initial date: 29 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenAssignmentsEvent extends Event implements StateEntry {

	private static final long serialVersionUID = -2048108169550370169L;

	public static final String OPEN_ASSIGNMENTS = "open-assignments-search";
	
	private final Identity grader;
	private final SearchStatus searchStatus;
	
	public OpenAssignmentsEvent(Identity grader, SearchStatus searchStatus) {
		super(OPEN_ASSIGNMENTS);
		this.grader = grader;
		this.searchStatus = searchStatus;
	}

	public Identity getGrader() {
		return grader;
	}

	public SearchStatus getSearchStatus() {
		return searchStatus;
	}

	@Override
	public OpenAssignmentsEvent clone() {
		return new OpenAssignmentsEvent(grader, searchStatus);
	}
}
