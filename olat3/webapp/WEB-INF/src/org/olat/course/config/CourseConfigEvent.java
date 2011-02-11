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
* <p>
*/ 
package org.olat.course.config;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Description:<br>
 * CourseConfigEvent - fired at any change of the course config that should be known by listeners.
 * 
 * <P>
 * Initial Date:  09.12.2008 <br>
 * @author Lavinia Dumitrescu
 */
public class CourseConfigEvent extends MultiUserEvent {
	
	public static final String EFFICIENCY_STATEMENT_TYPE = "efficiency";
	public static final String CALENDAR_TYPE = "calendar";
	public static final String LAYOUT_TYPE = "css";
	
  private final Long ResourceableId;
	
	/**
	 * The command should be one of the above defined: EFFICIENCY_STATEMENT_TYPE, CALENDAR_TYPE, LAYOUT_TYPE, etc.
	 * @param command
	 */
	public CourseConfigEvent(String command, Long resourceableId) {
		super(command);	
		this.ResourceableId = resourceableId;
	}

	public Long getResourceableId() {
		return ResourceableId;
	}

}
