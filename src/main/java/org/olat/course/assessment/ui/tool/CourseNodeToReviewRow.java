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
package org.olat.course.assessment.ui.tool;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.course.assessment.IndentedNodeRenderer.IndentedCourseNode;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 26 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeToReviewRow implements IndentedCourseNode {
	
	private final CourseNode courseNode;
	private final List<Identity> identities;
	private FormLink identityLink;
	
	public CourseNodeToReviewRow(CourseNode courseNode, List<Identity> identities) {
		this.courseNode = courseNode;
		this.identities = identities;
	}

	public String getCourseNodeIdent() {
		return courseNode.getIdent();
	}

	@Override
	public String getType() {
		return courseNode.getType();
	}

	@Override
	public String getShortTitle() {
		return courseNode.getShortTitle();
	}

	@Override
	public String getLongTitle() {
		return courseNode.getLongTitle();
	}

	@Override
	public int getRecursionLevel() {
		return 0;
	}

	public List<Identity> getIdentities() {
		return identities;
	}

	public FormLink getIdentityLink() {
		return identityLink;
	}

	public void setIdentityLink(FormLink identityLink) {
		this.identityLink = identityLink;
	}
	
}