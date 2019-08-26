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
import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 23 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ShowDetailsEvent extends Event {

	private static final long serialVersionUID = 5711383799013235370L;
	public static final String SHOW_DETAILS = "show-details";
	
	private final Identity assessedIdentity;
	private final CourseNode courseNode;
	
	public ShowDetailsEvent(CourseNode courseNode, Identity assessedIdentity) {
		super(SHOW_DETAILS);
		this.assessedIdentity = assessedIdentity;
		this.courseNode = courseNode;
	}

	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}

	public CourseNode getCourseNode() {
		return courseNode;
	}
}
