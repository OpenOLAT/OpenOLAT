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

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
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
	
	private static final Logger log = Tracing.createLoggerFor(BinderConfiguration.class);
	
	private final boolean withScore;
	private final boolean withPassed;
	private final boolean assessable;
	private final boolean timeline;
	private final boolean shareable;
	private final boolean options;
	private final Float maxScore;
	private final Float minScore;
	private final String displayname;
	
	public BinderConfiguration(boolean assessable, boolean withScore, Float maxScore, Float minScore,
			boolean withPassed, boolean timeline, boolean shareable, boolean options, String displayname) {
		this.assessable = assessable;
		this.withScore = withScore;
		this.withPassed = withPassed;
		this.timeline = timeline;
		this.shareable = shareable;
		this.options = options;
		this.maxScore = maxScore;
		this.minScore = minScore;
		this.displayname = displayname;
	}
	
	public String getDisplayname() {
		return displayname;
	}
	
	public boolean isAssessable() {
		return assessable;
	}

	public boolean isWithScore() {
		return withScore;
	}
	
	public Float getMaxScore() {
		return maxScore;
	}
	
	public Float getMinScore() {
		return minScore;
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
	
	public boolean isOptions() {
		return options;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("binderconfig[assessable=").append(assessable).append(";")
		  .append("withScore=").append(withScore).append(";")
		  .append("withPassed=").append(withPassed).append("]");
		return sb.toString() + super.toString();
	}
	
	public static BinderConfiguration createBusinessGroupConfig() {
		return new BinderConfiguration(false, false, null, null, false, true, false, false, null);
	}
	
	public static BinderConfiguration createTemplateConfig(boolean optionsEditable) {
		return new BinderConfiguration(false, false, null, null, false, false, false, optionsEditable, null);
	}
	
	public static BinderConfiguration createInvitationConfig() {
		return new BinderConfiguration(false, false, null, null, false, true, false, false, null);
	}
	
	public static BinderConfiguration createMyPagesConfig() {
		return new BinderConfiguration(false, false, null, null, false, true, true, false, null);
	}
	
	public static BinderConfiguration createSelectPagesConfig() {
		return new BinderConfiguration(false, false, null, null, false, false, false, false, null);
	}
	
	public static BinderConfiguration createDeletedPagesConfig() {
		return new BinderConfiguration(false, false, null, null, false, false, false, false, null);
	}

	public static BinderConfiguration createConfig(Binder binder) {
		boolean withScore = false;
		boolean withPassed = false;
		boolean assessable = false;
		Float maxScore = null;
		Float minScore = null;
		
		
		String displayname;
		RepositoryEntry entry = binder.getEntry();
		if(binder.getSubIdent() != null) {
			try {
				ICourse course = CourseFactory.loadCourse(entry);
				displayname = course.getCourseTitle();
				CourseNode courseNode = course.getRunStructure().getNode(binder.getSubIdent());
				if(courseNode instanceof PortfolioCourseNode) {
					CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
					AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
					withScore = Mode.none != assessmentConfig.getScoreMode();
					if(withScore) {
						maxScore = assessmentConfig.getMaxScore();
						minScore = assessmentConfig.getMinScore();
					}
					withPassed = Mode.none != assessmentConfig.getPassedMode();
					assessable = withPassed || withScore;
				} else {
					withPassed = true;
					withScore = false;
					assessable = true;
				}
			} catch (CorruptedCourseException e) {
				displayname = entry.getDisplayname();
				withPassed = withScore = assessable = false;
				log.error("Corrupted course: " + entry, e);
			}
		} else if(entry != null) {
			displayname = entry.getDisplayname();
			withPassed = true;
			withScore = false;
			assessable = true;
		} else {
			displayname = null;
			withPassed = withScore = assessable = false;
		}
		return new BinderConfiguration(assessable, withScore, maxScore, minScore, withPassed, true, true, false, displayname);
	}
}
