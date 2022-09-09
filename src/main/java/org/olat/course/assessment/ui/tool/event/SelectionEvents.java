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
package org.olat.course.assessment.ui.tool.event;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 26 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SelectionEvents {
	
	public static final Event USERS_EVENT = new Event("assessment-tool-select-users");
	public static final Event PASSED_EVENT = new Event("assessment-tool-select-passed");
	public static final Event FAILED_EVENT = new Event("assessment-tool-select-failed");
	public static final Event UNDEFINED_EVENT = new Event("assessment-tool-select-undfined");
	public static final Event DONE_EVENT = new Event("assessment-tool-select-done");
	public static final Event NOT_DONE_EVENT = new Event("assessment-tool-select-not-done");
	public static final Event MEMBERS_EVENT = new Event("assessment-tool-select-members");
	public static final Event NON_MEMBERS_EVENT = new Event("assessment-tool-select-non-members");
	public static final Event FAKE_PARTICIPANTS_EVENT = new Event("assessment-tool-select-fake-participants");

}
