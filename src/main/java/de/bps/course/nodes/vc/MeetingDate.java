//<OLATCE-103>
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
package de.bps.course.nodes.vc;

import java.io.Serializable;
import java.util.Date;

/**
 * Description:<br>
 * Virtual Classroom appointment model to be used in course module configuration.
 * Initial Date: 30.08.2010 <br>
 * 
 * @author Jens Lindner (jlindne4@hs-mittweida.de)
 * @author skoeber
 */
public class MeetingDate implements Serializable {
	
	private String title;
	private String description;
	private Date start;
	private Date end;
	
	public MeetingDate() {
		// nothing to do
	}

	public MeetingDate(final String title, final String description, Date start, Date end) {
		this.title = title;
		this.description = description;
		this.start= start;
		this.end = end;
	}

	public final String getTitle() {
		return title;
	}

	public final void setTitle(final String title) {
		this.title = title;
	}

	public final String getDescription() {
		return description;
	}

	public final void setDescription(final String description) {
		this.description = description;
	}
	
	public Date getBegin() {
		return start;
	}

	public void setBegin(Date start) {
			this.start = start;
	}
	
	public Date getEnd() {
		return end;
	}
	
	public void setEnd(Date end) {
		this.end = end;
	}
}
//</OLATCE-103>