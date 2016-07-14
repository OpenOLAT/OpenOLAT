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
package org.olat.modules.portfolio;

import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 24.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderConfiguration {
	
	private final boolean withScore;
	private final boolean withPassed;
	private final boolean assessable;
	private final boolean timeline;
	private final boolean shareable;
	
	public BinderConfiguration(boolean assessable, boolean withScore, boolean withPassed,
			boolean timeline, boolean shareable) {
		this.assessable = assessable;
		this.withScore = withScore;
		this.withPassed = withPassed;
		this.timeline = timeline;
		this.shareable = shareable;
	}
	
	public boolean isAssessable() {
		return assessable;
	}

	public boolean isWithScore() {
		return withScore;
	}

	public boolean isWithPassed() {
		return withPassed;
	}
	
	public boolean isTimeline() {
		return timeline;
	}
	
	public boolean isShareable() {
		return shareable;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("binderconfig[assessable=").append(assessable).append(";")
		  .append("withScore=").append(withScore).append(";")
		  .append("withPassed=").append(withPassed).append("]");
		return sb.toString() + super.toString();
	}
	
	public static BinderConfiguration createTemplateConfig() {
		return new BinderConfiguration(false, false, false, false, false);
	}
	
	public static BinderConfiguration createInvitationConfig() {
		return new BinderConfiguration(false, false, false, true, false);
	}
	
	public static BinderConfiguration createMyPagesConfig() {
		return new BinderConfiguration(false, false, false, true, true);
	}

	public static BinderConfiguration createConfig(Binder binder) {
		boolean withScore = false;
		boolean withPassed = false;
		boolean assessable = false;
		
		RepositoryEntry entry = binder.getEntry();
		if(binder.getSubIdent() != null) {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseNode courseNode = course.getRunStructure().getNode(binder.getSubIdent());
			if(courseNode instanceof PortfolioCourseNode) {
				PortfolioCourseNode pfNode = (PortfolioCourseNode)courseNode;
				withScore = pfNode.hasScoreConfigured();
				withPassed = pfNode.hasPassedConfigured();
				assessable = withPassed || withScore;
			} else {
				withPassed = true;
				withScore = false;
				assessable = true;
			}
		} else if(entry != null) {
			withPassed = true;
			withScore = false;
			assessable = true;
		} else {
			withPassed = withScore = assessable = false;
		}
		return new BinderConfiguration(assessable, withScore, withPassed, true, true);
	}
}
