/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.st;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.nodes.INode;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.config.CourseConfig;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * The course structure run controller provides an automatically generated view
 * of the children of this course node. There are two modi to display the
 * childs:
 * <ul>
 * <li>simple TOC: a list of titles and descriptions</li>
 * <li>peek view: for each child the peek view controller is displayed. This
 * gives each child the possibility to render a preview or simplified view of
 * the content. This view is visually more appealing than the simple TOC view.</li>
 * </ul>
 * 
 * @author Felix Jost, Florian Gnägi frentix GmbH
 */
public class STCourseNodeRunController extends BasicController {
	
	private Link certificationLink;
	private final VelocityContainer myContent;
	
	private final UserCourseEnvironment userCourseEnv;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private NodeAccessService nodeAccessService;

	public STCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, STCourseNode stCourseNode, ScoreEvaluation se) {
		super(ureq, wControl);
		addLoggingResourceable(LoggingResourceable.wrap(stCourseNode));
		this.userCourseEnv = userCourseEnv;

		myContent = createVelocityContainer("run");
		myContent.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID
		
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(stCourseNode);
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		if (se != null && (hasScore || hasPassed)) {
			HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, wControl, userCourseEnv, stCourseNode);
			if (highScoreCtr.isViewHighscore()) {
				Component highScoreComponent = highScoreCtr.getInitialComponent();
				myContent.put("highScore", highScoreComponent);							
			}
		}
		// read display configuration
		ModuleConfiguration config = stCourseNode.getModuleConfiguration();
		// configure number of display rows
		int rows = config.getIntegerSafe(STCourseNodeEditController.CONFIG_KEY_COLUMNS, 1);
		myContent.contextPut("layoutType", rows);
		// the display type: toc or peekview
		String displayType = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW);

		// Build list of child nodes and peek views if necessary
		List<CourseNode> children = new ArrayList<>();

		// Build up a overview of all visible children (direct children only, no
		// grandchildren)
		String peekviewChildNodesConfig = config.getStringValue(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_CHILD_NODES, null);
		List<String> peekviewChildNodes =  (peekviewChildNodesConfig == null ? new ArrayList<>() : Arrays.asList(peekviewChildNodesConfig.split(",")));
		CourseTreeNode courseTreeNode = (CourseTreeNode)nodeAccessService.getCourseTreeModelBuilder(userCourseEnv)
				.build()
				.getNodeById(stCourseNode.getIdent());
		int chdCnt = courseTreeNode == null ? 0 : courseTreeNode.getChildCount();
		for (int i = 0; i < chdCnt; i++) {
			INode childNode = courseTreeNode.getChildAt(i);
			if (childNode instanceof CourseTreeNode) {
				CourseTreeNode childCourseTreeNode = (CourseTreeNode)childNode;
				if (childCourseTreeNode.isVisible()) {
					// Build and add child generic or specific peek view
					CourseNode child = childCourseTreeNode.getCourseNode();
					Controller childViewController = null;
					Controller childPeekViewController = null;
					boolean accessible = childCourseTreeNode.isAccessible();
					if (displayType.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW)) {
						if (peekviewChildNodes.isEmpty()) {
							// Special case: no child nodes configured. This is the case when
							// the node has been configured before it had any children. We just
							// use the first children as they appear in the list
							if (i < STCourseNodeConfiguration.MAX_PEEKVIEW_CHILD_NODES) {
								if(accessible) {
									CourseNode parent = child.getParent() instanceof CourseNode? (CourseNode)child.getParent(): null;
									child.updateModuleConfigDefaults(false, parent);
									childPeekViewController = child.createPeekViewRunController(ureq, wControl, userCourseEnv, childCourseTreeNode);
								}
							} else {
								// Stop, we already reached the max count
								break;
							}
						} else {
							// Only add configured children
							if (peekviewChildNodes.contains(child.getIdent())) {
								if(accessible) {
									CourseNode parent = child.getParent() instanceof CourseNode? (CourseNode)child.getParent(): null;
									child.updateModuleConfigDefaults(false, parent);
									childPeekViewController = child.createPeekViewRunController(ureq, wControl, userCourseEnv, childCourseTreeNode);
								}
							} else {
								// Skip this child - not configured
								continue;
							}
						}
					}
					// Add child to list
					children.add(child);
					childViewController = new PeekViewWrapperController(ureq, wControl, child, childPeekViewController, accessible);
					listenTo(childViewController); // auto-dispose controller
					myContent.put("childView_".concat(child.getIdent()), childViewController.getInitialComponent());
				}
			}
		}

		myContent.contextPut("children", children);
		myContent.contextPut("nodeFactory", CourseNodeFactory.getInstance());
		
		// push title and learning objectives, only visible on intro page
		myContent.contextPut("menuTitle", stCourseNode.getShortTitle());
		myContent.contextPut("displayTitle", stCourseNode.getLongTitle());
		if(ureq.getUserSession().getRoles().isGuestOnly() || !userCourseEnv.isParticipant()) {
			myContent.contextPut("hasScore", Boolean.FALSE);
			myContent.contextPut("hasPassed", Boolean.FALSE);
		} else {
			myContent.contextPut("hasScore", Boolean.valueOf(hasScore));
			myContent.contextPut("hasPassed", Boolean.valueOf(hasPassed));

			if(hasScore|| hasPassed) {
				CourseConfig cc = userCourseEnv.getCourseEnvironment().getCourseConfig();
				if((cc.isEfficencyStatementEnabled() || cc.isCertificateEnabled())
						&& userCourseEnv.hasEfficiencyStatementOrCertificate(false)) {
					certificationLink = LinkFactory.createButton("certification", myContent, this);
				}
			}
		}

		if (se != null) {
			Float score = se.getScore();
			Boolean passed = se.getPassed();
			if (score != null) {
				myContent.contextPut("scoreScore", AssessmentHelper.getRoundedScore(score));
			}
			if (passed != null) {
				myContent.contextPut("scorePassed", passed);
				myContent.contextPut("hasPassedValue", Boolean.TRUE);
			} else {
				myContent.contextPut("hasPassedValue", Boolean.FALSE);
			}
		}

		putInitialPanel(myContent);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(certificationLink == source) {
			RepositoryEntry re = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			String resourceUrl = "[RepositoryEntry:" + re.getKey() + "][Certification:0]";
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof OlatCmdEvent) {
			OlatCmdEvent gotoNodeEvent = (OlatCmdEvent) event;			
			String subcommand = gotoNodeEvent.getSubcommand();
			// subcommand consists of node id and path
			int slashpos = subcommand.indexOf("/");
			String nodeId = subcommand;
			String path = "";
			if (slashpos != -1) {
				nodeId = subcommand.substring(0, slashpos);
				path = subcommand.substring(slashpos);
			}
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.ST_GOTO_NODE, getClass(),
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.nodeId, nodeId, path));
			// forward to my listeners
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void doDispose() {
	// nothing to do yet
	}

}
