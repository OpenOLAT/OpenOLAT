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
package org.olat.course.nodeaccess.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NoAccessResolver;
import org.olat.course.nodeaccess.NoAccessResolver.NoAccess;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.style.ColorCategoryResolver;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.Header;
import org.olat.course.style.Header.Builder;
import org.olat.course.style.TeaserImageStyle;
import org.olat.course.style.ui.CourseStyleUIFactory;
import org.olat.course.style.ui.HeaderController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NoAccessController extends BasicController {
	
	private HeaderController headerCtrl;
	private final EmptyState emptyState;

	private final CourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;
	private final NoAccess noAccessMessage;

	@Autowired
	private CourseStyleService courseStyleService;
	@Autowired
	private NodeAccessService nodeAccessService;

	public NoAccessController(UserRequest ureq, WindowControl wControl, CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		
		VelocityContainer mainVC = createVelocityContainer("no_access");
		
		Header header = createHeader();
		if (header != null) {
			headerCtrl = new HeaderController(ureq, wControl, header);
			listenTo(headerCtrl);
			mainVC.put("header", headerCtrl.getInitialComponent());
		}
		
		noAccessMessage = nodeAccessService.getNoAccessResolver(userCourseEnv).getNoAccessMessage(courseNode);
		emptyState = EmptyStateFactory.create("no.access", mainVC, this);
		emptyState.setIndicatorIconCss("o_no_icon");
		emptyState.setIconCss("o_icon_status_not_ready");
		emptyState.setMessageI18nKey("placeholder");
		String message = translate("no.access.course.node", new String[] {courseNode.getLongTitle()});
		emptyState.setMessageI18nArgs(new String[] {message});
		emptyState.setHintI18nKey("placeholder");
		emptyState.setHintI18nArgs(new String[] {NoAccessResolver.translate(getTranslator(), noAccessMessage, false)});
		if (StringHelper.containsNonWhitespace(noAccessMessage.getGoToNodeIdent())) {
			emptyState.setButtonI18nKey("no.access.goto.node");
		}
		
		putInitialPanel(mainVC);
	}
	
	public Header createHeader() {
		String displayOption = courseNode.getDisplayOption();
		if (CourseNode.DISPLAY_OPTS_CONTENT.equals(displayOption) || CourseNode.DISPLAY_OPTS_DESCRIPTION_CONTENT.equals(displayOption)) {
			return null;
		}
		
		Builder builder = Header.builder();
		
		if (CourseNode.DISPLAY_OPTS_TITLE_CONTENT.equals(displayOption)) {
			builder.withTitle(courseNode.getLongTitle());
		} else if (CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT.equals(displayOption)) {
			builder.withTitle(courseNode.getLongTitle());
		}
		
		CourseConfig courseConfig = userCourseEnv.getCourseEnvironment().getCourseConfig();
		ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
		VFSMediaMapper teaserImageMapper = courseStyleService.getTeaserImageMapper(course, courseNode);
		if (teaserImageMapper != null) {
			boolean teaserImageTransparent = courseStyleService.isImageTransparent(teaserImageMapper);
			TeaserImageStyle teaserImageStyle = courseStyleService.getTeaserImageStyle(course, courseNode);
			builder.withTeaserImage(teaserImageMapper, teaserImageTransparent, teaserImageStyle);
		}
		
		ColorCategoryResolver colorCategoryResolver = courseStyleService.getColorCategoryResolver(null, courseConfig.getColorCategoryIdentifier());
		builder.withColorCategoryCss(colorCategoryResolver.getColorCategoryCss(courseNode));
		String iconCssClass = CourseNodeFactory.getInstance()
				.getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass();
		builder.withIconCss(iconCssClass);
		
		if (LearningPathNodeAccessProvider.TYPE.equals(courseConfig.getNodeAccessType().getType())) {
			if (userCourseEnv.isParticipant()) {
				AssessmentEvaluation evaluation = userCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
				if (evaluation != null) {
					CourseStyleUIFactory.addHandlingRangeData(builder, evaluation);
				}
			}
		}
		
		return builder.build();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == emptyState && event == EmptyState.EVENT) {
			fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, noAccessMessage.getGoToNodeIdent()));
		}
	}

}
