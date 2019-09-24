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
package org.olat.modules.lecture.ui.event;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.modules.lecture.LectureBlockAppealStatus;

/**
 * 
 * Initial date: 6 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchAppealsEvent extends Event {

	private static final long serialVersionUID = 5063491566782096798L;
	public static final String SEARCH_EVENT = "search-appeals-event";
	
	private final Date startDate;
	private final Date endDate;
	private final String searchString;
	private final List<LectureBlockAppealStatus> status;
	
	public SearchAppealsEvent(String searchString, List<LectureBlockAppealStatus> status,
			Date startDate, Date endDate) {
		super(SEARCH_EVENT);
		this.searchString = searchString;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getSearchString() {
		return searchString;
	}

	public List<LectureBlockAppealStatus> getStatus() {
		return status;
	}
}
