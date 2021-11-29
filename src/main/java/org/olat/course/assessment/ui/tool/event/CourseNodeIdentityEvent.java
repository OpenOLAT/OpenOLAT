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

import java.util.function.Supplier;

import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.ui.AssessedIdentityListState;

/**
 * 
 * Initial date: 26 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeIdentityEvent extends Event {

	private static final long serialVersionUID = 1276978243212471948L;
	
	private final Identity assessedIdentity;
	private final String courseNodeIdent;
	private final Supplier<AssessedIdentityListState> filter;
	
	public CourseNodeIdentityEvent(String courseNodeIdent, Identity assessedIdentity, Supplier<AssessedIdentityListState> filter) {
		super("assessment-review");
		this.courseNodeIdent = courseNodeIdent;
		this.assessedIdentity = assessedIdentity;
		this.filter = filter;
	}

	public String getCourseNodeIdent() {
		return courseNodeIdent;
	}

	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}

	public Supplier<AssessedIdentityListState> getFilter() {
		return filter;
	}

}
