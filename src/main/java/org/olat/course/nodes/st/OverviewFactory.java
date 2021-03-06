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
package org.olat.course.nodes.st;

import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.st.Overview.Builder;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.style.ColorCategoryResolver;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.TeaserImageStyle;

/**
 * 
 * Initial date: 13 Jul 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OverviewFactory {
	
	private final UserCourseEnvironment userCourseEnv;
	private final CourseConfig courseConfig;
	private final CourseNodeFilter courseNodeFilter;
	private final CourseNodeFilter peekViewFilter;
	private final boolean smallPeekview;
	private final ColorCategoryResolver colorCategoryResolver;
	private final String mapperPrefix;
	private final ScoreAccounting scoreAccounting;
	private final Date now;
	
	private final CourseStyleService courseStyleService;
	
	public OverviewFactory(UserCourseEnvironment userCourseEnv, CourseNodeFilter courseNodeFilter, CourseNodeFilter peekViewFilter, boolean smallPeekview) {
		this.userCourseEnv = userCourseEnv;
		this.courseNodeFilter = courseNodeFilter;
		this.peekViewFilter = peekViewFilter;
		this.smallPeekview = smallPeekview;
		
		courseStyleService = CoreSpringFactory.getImpl(CourseStyleService.class);
		courseConfig = userCourseEnv.getCourseEnvironment().getCourseConfig();
		colorCategoryResolver = courseStyleService.getColorCategoryResolver(null, courseConfig.getColorCategoryIdentifier());
		mapperPrefix = CodeHelper.getUniqueID();
		
		scoreAccounting = userCourseEnv.getScoreAccounting();
		scoreAccounting.evaluateAll();
		now = new Date();
	}

	public Controller create(UserRequest ureq, WindowControl wControl, CourseTreeNode courseTreeNode) {
		CourseNode courseNode = courseTreeNode.getCourseNode();
		if (!courseNodeFilter.accept(courseNode)) return null;
		
		Builder builder = Overview.builder();
		builder.withNodeIdent(courseNode.getIdent());
		
		String colorCategoryCss = colorCategoryResolver.getColorCategoryCss(courseNode);
		builder.withColorCategoryCss(colorCategoryCss);
		
		VFSMediaMapper mapper = courseStyleService.getTeaserImageMapper(userCourseEnv.getCourseEnvironment(), courseNode);
		if (mapper != null) {
			TeaserImageStyle teaserImageStyle = courseConfig.getTeaserImageStyle() != null
					? courseConfig.getTeaserImageStyle()
					: TeaserImageStyle.gradient;
			builder.withTeaserImage(mapper, teaserImageStyle);
			
			// Same ID for the same image during one structure run view
			String teaserImageID = mapperPrefix + mapper.getVfsLeaf().getRelPath() + mapper.getVfsLeaf().getName();
			builder.withTeaserImageID(teaserImageID);
		}
		
		builder.withIconCss(CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass());
		builder.withTitle(courseNode.getShortTitle());
		
		AssessmentEvaluation evaluation = scoreAccounting.getScoreEvaluation(courseNode);
		if (evaluation != AssessmentEvaluation.EMPTY_EVAL) {
			LearningPathStatus learningPathStatus = LearningPathStatus.of(evaluation);
			builder.withLearningPathStatus(learningPathStatus);
			
			Date startDate = evaluation.getStartDate();
			if (startDate != null && startDate.after(now) && LearningPathStatus.done != learningPathStatus) {
				builder.withStartDate(startDate);
			}
			
			Date currentEndDate = evaluation.getEndDate().getCurrent();
			if (currentEndDate != null && currentEndDate.after(now) && LearningPathStatus.done != learningPathStatus) {
				builder.withEndDate(currentEndDate);
			}
		}
		
		Controller peekViewCtrl = null;
		if (courseTreeNode.isAccessible()) {
			builder.withSubTitle(courseNode.getLongTitle());
			builder.withDescription(courseNode.getDescription());
			
			if (peekViewFilter.accept(courseNode)) {
				peekViewCtrl = courseNode.createPeekViewRunController(ureq, wControl, userCourseEnv, courseTreeNode, smallPeekview);
			}
		} else {
			builder.withNoAccessMessage(courseNode.getNoAccessExplanation());
		}
		
		return new OverviewController(ureq, wControl, builder.build(), peekViewCtrl);
	}
	
	public interface CourseNodeFilter {
		
		public boolean accept(CourseNode courseNode);
		
	}

}
