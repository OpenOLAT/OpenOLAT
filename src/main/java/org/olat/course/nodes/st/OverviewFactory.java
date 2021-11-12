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
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.LearningPathStatus;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NoAccessResolver;
import org.olat.course.nodeaccess.NoAccessResolver.NoAccess;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.CourseNodeHelper;
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
	private final ICourse course;
	private final boolean smallPeekview;
	private final ColorCategoryResolver colorCategoryResolver;
	private final NoAccessResolver noAccessResolver;
	private final String mapperPrefix;
	private final Date now;
	private ScoreAccounting scoreAccounting;
	private final CourseStyleService courseStyleService;
	private LearningPathService learningPathService;
	
	public OverviewFactory(UserCourseEnvironment userCourseEnv, CourseNodeFilter courseNodeFilter, CourseNodeFilter peekViewFilter, boolean smallPeekview) {
		this.userCourseEnv = userCourseEnv;
		this.courseNodeFilter = courseNodeFilter;
		this.peekViewFilter = peekViewFilter;
		this.smallPeekview = smallPeekview;
		
		course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		courseStyleService = CoreSpringFactory.getImpl(CourseStyleService.class);
		courseConfig = userCourseEnv.getCourseEnvironment().getCourseConfig();
		colorCategoryResolver = courseStyleService.getColorCategoryResolver(null, courseConfig.getColorCategoryIdentifier());
		noAccessResolver = CoreSpringFactory.getImpl(NodeAccessService.class).getNoAccessResolver(userCourseEnv);
		mapperPrefix = CodeHelper.getUniqueID();
		
		now = new Date();
		if (LearningPathNodeAccessProvider.TYPE.equals(courseConfig.getNodeAccessType().getType())) {
			if (userCourseEnv.isParticipant()) {
				scoreAccounting = userCourseEnv.getScoreAccounting();
				scoreAccounting.evaluateAll();
			} else {
				learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
			}
		}
	}

	public Controller create(UserRequest ureq, WindowControl wControl, CourseTreeNode courseTreeNode) {
		CourseNode courseNode = courseTreeNode.getCourseNode();
		if (!courseNodeFilter.accept(courseNode)) return null;
		
		Builder builder = Overview.builder();
		builder.withNodeIdent(courseNode.getIdent());
		
		String colorCategoryCss = colorCategoryResolver.getColorCategoryCss(courseNode);
		builder.withColorCategoryCss(colorCategoryCss);
		
		VFSMediaMapper mapper = courseStyleService.getTeaserImageMapper(course, courseNode);
		if (mapper != null) {
			TeaserImageStyle teaserImageStyle = courseStyleService.getTeaserImageStyle(course, courseNode);
			builder.withTeaserImage(mapper, teaserImageStyle);
			
			// Same ID for the same image during one structure run view
			String teaserImageID = mapperPrefix + mapper.getVfsLeaf().getRelPath() + mapper.getVfsLeaf().getName();
			builder.withTeaserImageID(teaserImageID);
		}
		
		builder.withIconCss(CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass());
		builder.withTitle(courseNode.getLongTitle());
		builder.withSubTitle(CourseNodeHelper.getDifferentlyStartingShortTitle(courseNode));
		
		if (scoreAccounting != null) {
			AssessmentEvaluation evaluation = scoreAccounting.getScoreEvaluation(courseNode);
			LearningPathStatus learningPathStatus = LearningPathStatus.of(evaluation);
			builder.withLearningPathStatus(learningPathStatus);
			
			if (LearningPathStatus.done != learningPathStatus) {
				builder.withDuration(evaluation.getDuration());
				
				Date startDate = evaluation.getStartDate();
				if (startDate != null && startDate.after(now)) {
					builder.withStartDateConfig(DueDateConfig.absolute(startDate));
				}
				Date currentEndDate = evaluation.getEndDate().getCurrent();
				if (currentEndDate != null) {
					builder.withEndDateConfig(DueDateConfig.absolute(currentEndDate));
				}
			}
		} else if (learningPathService != null) {
			LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(courseNode);
			builder.withDuration(learningPathConfigs.getDuration());
			builder.withStartDateConfig(learningPathConfigs.getStartDateConfig());
			builder.withEndDateConfig(learningPathConfigs.getEndDateConfig());
		}
		
		Controller peekViewCtrl = null;
		if (courseTreeNode.isAccessible()) {
			builder.withDescription(courseNode.getDescription());
			
			if (peekViewFilter.accept(courseNode)) {
				peekViewCtrl = courseNode.createPeekViewRunController(ureq, wControl, userCourseEnv, courseTreeNode, smallPeekview);
			}
		} else {
			NoAccess noAccessMessage = noAccessResolver.getNoAccessMessage(courseNode);
			builder.withNoAccessMessage(noAccessMessage);
		}
		
		return new OverviewController(ureq, wControl, builder.build(), peekViewCtrl);
	}
	
	public interface CourseNodeFilter {
		
		public boolean accept(CourseNode courseNode);
		
	}

}
