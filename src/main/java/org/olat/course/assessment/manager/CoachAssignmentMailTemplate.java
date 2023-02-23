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
package org.olat.course.assessment.manager;

import org.apache.velocity.VelocityContext;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.mail.MailTemplate;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 23 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachAssignmentMailTemplate extends MailTemplate {
	
	private static final String COURSE_NAME = "courseName";
	private static final String COURSE_DESCRIPTION = "courseDescription";
	private static final String COURSE_URL = "courseUrl";
	private static final String COURSE_REF = "courseRef";
	private static final String COURSE_ELEMENT_TITLE = "courseElementTitle";
	private static final String COURSE_ELEMENT_SHORT_TITLE = "courseElementShortTitle";
	
	private final RepositoryEntry courseEntry;
	private final CourseNode courseNode;
	
	public CoachAssignmentMailTemplate(String subjectTemplate, String bodyTemplate,
			RepositoryEntry courseEntry, CourseNode courseNode) {
		super(subjectTemplate, bodyTemplate, null);
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
	}
	
	@Override
	public void putVariablesInMailContext(VelocityContext context, Identity recipient) {
		// Put user variables into velocity context			
		String reName = courseEntry.getDisplayname();
		putVariablesInMailContext(context, COURSE_NAME, reName);
		
		String redescription = (StringHelper.containsNonWhitespace(courseEntry.getDescription()) ? FilterFactory.getHtmlTagAndDescapingFilter().filter(courseEntry.getDescription()) : ""); 
		putVariablesInMailContext(context, COURSE_DESCRIPTION, redescription);
		
		String reUrl = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + courseEntry.getKey();
		putVariablesInMailContext(context, COURSE_URL, reUrl);
		
		String courseRef = courseEntry.getExternalRef() == null ? "" : courseEntry.getExternalRef();
		putVariablesInMailContext(context, COURSE_REF, courseRef);

		putVariablesInMailContext(context, COURSE_ELEMENT_TITLE, courseNode.getLongTitle());
		putVariablesInMailContext(context, COURSE_ELEMENT_SHORT_TITLE, courseNode.getShortTitle());
	}
}
