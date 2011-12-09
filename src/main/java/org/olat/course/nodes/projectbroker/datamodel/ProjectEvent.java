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

package org.olat.course.nodes.projectbroker.datamodel;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Each project can have enrollment-events, dropbox-event.
 * Each event can have start- and/or end-date. 
 * When no date is defined, the access is allowed 
 * e.g. enrollment-event :end-date = 1.2.2010 / start-date = null
 * => Enrollment possible until 1.2.2010
 * @author guretzki
 */

public class ProjectEvent {
	
	private Project.EventType  eventType;
	private Date               startDate;
	private Date               endDate;
	private SimpleDateFormat dateFormatter;
	

	public ProjectEvent(Project.EventType eventType, Date startDate, Date endDate) {
		super();
		this.eventType = eventType;
		this.startDate = startDate;
		this.endDate   = endDate;
		dateFormatter = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm");
	}

	public Project.EventType getEventType() {
		return eventType;
	}

	public void setEventType(Project.EventType eventType) {
		this.eventType = eventType;
	}

	public Date getStartDate() {
		return startDate;
	}

	public String getFormattedStartDate() {
		if (startDate != null) {
			return dateFormatter.format(startDate);
		} else {
			return "-";
		}
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getFormattedEndDate() {
		if (endDate != null) {
			return dateFormatter.format(endDate);
		} else {
			return "-";
		}
		
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String toString() {
		return "ProjectEvent ["+ eventType.toString() + "," + startDate + " - " + endDate + "]";
	}
	
}
