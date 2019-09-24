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

package org.olat.course.nodes.ms;

import java.io.File;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.course.CourseModule;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.ObjectivesHelper;
import org.olat.course.nodes.PersistentAssessableCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  Jun 16, 2004
 * @author gnaegi
 */
public class MSCourseNodeRunController extends BasicController implements Activateable2 {

	private final VelocityContainer myContent;
	private DisplayOrDownloadComponent download;
	private Controller detailsCtrl;
	
	private String mapperUri;
	private final boolean showLog;
	private boolean hasScore, hasPassed, hasComment;
	private final UserCourseEnvironment userCourseEnv;
	private final boolean overrideUserResultsVisiblity;
	private final PersistentAssessableCourseNode courseNode;
	
	@Autowired
	private CourseModule courseModule;

	/**
	 * Constructor for a manual scoring course run controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param userCourseEnv The user course environment
	 * @param msCourseNode An assessable course node
	 * @param displayNodeInfo If true, the node title and learning objectives will be displayed
	 * @param showLog If true, the change log will be displayed
	 */
	public MSCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			PersistentAssessableCourseNode courseNode, boolean displayNodeInfo, boolean showLog) {
		this(ureq, wControl, userCourseEnv, courseNode, displayNodeInfo, showLog, false);
	}
	
	/**
	 * Constructor for a manual scoring course run controller
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param userCourseEnv The user course environment
	 * @param courseNode An assessable course element
	 * @param displayNodeInfo If true, the node title and learning objectives will be displayed
	 * @param showLog If true, the change log will be displayed
	 * @param overrideUserResultsVisiblity If the controller can override the user visiblity of the score evaluation
	 */
	public MSCourseNodeRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, PersistentAssessableCourseNode courseNode,
			boolean displayNodeInfo, boolean showLog, boolean overrideUserResultsVisiblity) {
		super(ureq, wControl, Util.createPackageTranslator(CourseNode.class, ureq.getLocale()));
		
		this.showLog = showLog;
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		this.overrideUserResultsVisiblity = overrideUserResultsVisiblity;
		myContent = createVelocityContainer("run");

		ModuleConfiguration config = courseNode.getModuleConfiguration();
		if (config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD,false)) {
			HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, wControl, userCourseEnv, courseNode);
			if (highScoreCtr.isViewHighscore()) {
				Component highScoreComponent = highScoreCtr.getInitialComponent();
				myContent.put("highScore", highScoreComponent);
			}
		}
				
		myContent.contextPut("displayNodeInfo", Boolean.valueOf(displayNodeInfo));
		if (displayNodeInfo) {
			// push title and learning objectives, only visible on intro page
			myContent.contextPut("menuTitle", courseNode.getShortTitle());
			myContent.contextPut("displayTitle", courseNode.getLongTitle());
			
			// Adding learning objectives
			String learningObj = courseNode.getLearningObjectives();
			if (learningObj != null) {
				Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq); 
				myContent.put("learningObjectives", learningObjectives);
				myContent.contextPut("hasObjectives", learningObj); // dummy value, just an exists operator
			}
		} 
		
		//admin setting whether to show change log or not
		myContent.contextPut("changelogconfig", courseModule.isDisplayChangeLog());

		// Push variables to velocity page
		exposeConfigToVC(ureq);		
		exposeUserDataToVC(ureq);
		putInitialPanel(myContent);
	}
	
	/**
	 * @return true if the assessed user has a score
	 */
	public boolean hasScore() {
		return hasScore;
	}
	
	/**
	 * @return true if the assessed user has passed or failed.
	 */
	public boolean hasPassed() {
		return hasPassed;
	}
	
	/**
	 * @return true if the assessed user has a comment
	 */
	public boolean hasComment() {
		return hasComment;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(type.startsWith("path")) {
			if(download != null) {
				String path = BusinessControlFactory.getInstance().getPath(entries.get(0));
				String url = mapperUri + "/" + path;
				download.triggerFileDownload(url);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if("show".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), true);
		} else if("hide".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), false);
		}
	}
	
	private void exposeConfigToVC(UserRequest ureq) {
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		myContent.contextPut("hasScoreField", courseNode.hasScoreConfigured());
		if (courseNode.hasScoreConfigured()) {
			myContent.contextPut("scoreMin", AssessmentHelper.getRoundedScore(courseNode.getMinScoreConfiguration()));
			myContent.contextPut("scoreMax", AssessmentHelper.getRoundedScore(courseNode.getMaxScoreConfiguration()));
		}
		myContent.contextPut("hasPassedField", courseNode.hasPassedConfigured());
		if (courseNode.hasPassedConfigured()) {
			myContent.contextPut("passedCutValue", AssessmentHelper.getRoundedScore(courseNode.getCutValueConfiguration()));
		}
		myContent.contextPut("hasCommentField", courseNode.hasCommentConfigured());
		String infoTextUser = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
		if(StringHelper.containsNonWhitespace(infoTextUser)) {
				myContent.contextPut(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, infoTextUser);
				myContent.contextPut("indisclaimer", isPanelOpen(ureq, "disclaimer", true));
		}
	}
	
	private void exposeUserDataToVC(UserRequest ureq) {
		AssessmentEntry assessmentEntry = courseNode.getUserAssessmentEntry(userCourseEnv);
		if(assessmentEntry == null) {
			myContent.contextPut("hasPassedValue", Boolean.FALSE);
			myContent.contextPut("passed", Boolean.FALSE);
			hasPassed = hasScore = hasComment = false;
		} else {
			String rawComment = assessmentEntry.getComment();
			hasPassed = assessmentEntry.getPassed() != null;
			hasScore = assessmentEntry.getScore() != null;
			hasComment = courseNode.hasCommentConfigured() && StringHelper.containsNonWhitespace(rawComment);
		
			boolean resultsVisible = overrideUserResultsVisiblity
					|| assessmentEntry.getUserVisibility() == null
					|| assessmentEntry.getUserVisibility().booleanValue();
			myContent.contextPut("resultsVisible", resultsVisible);
			myContent.contextPut("score", AssessmentHelper.getRoundedScore(assessmentEntry.getScore()));
			myContent.contextPut("hasPassedValue", (assessmentEntry.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
			myContent.contextPut("passed", assessmentEntry.getPassed());
			
			if(resultsVisible) {
				if(hasComment) {
					StringBuilder comment = Formatter.stripTabsAndReturns(rawComment);
					myContent.contextPut("comment", StringHelper.xssScan(comment));
					myContent.contextPut("incomment", isPanelOpen(ureq, "comment", true));
				}
				
				if(courseNode.hasIndividualAsssessmentDocuments()) {
					List<File> docs = courseNode.getIndividualAssessmentDocuments(userCourseEnv);
					mapperUri = registerCacheableMapper(ureq, null, new DocumentsMapper(docs));
					myContent.contextPut("docsMapperUri", mapperUri);
					myContent.contextPut("docs", docs);
					myContent.contextPut("inassessmentDocuments", isPanelOpen(ureq, "assessmentDocuments", true));
					if(download == null) {
						download = new DisplayOrDownloadComponent("", null);
						myContent.put("download", download);
					}
				}
				if (courseNode.hasResultsDetails()) {
					detailsCtrl = courseNode.getResultDetailsController(ureq, getWindowControl(), userCourseEnv);
					listenTo(detailsCtrl);
					myContent.put("details", detailsCtrl.getInitialComponent());
				}
			}
		}

		if(showLog) {
			UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
			String userLog = am.getUserNodeLog(courseNode, userCourseEnv.getIdentityEnvironment().getIdentity());
			myContent.contextPut("log", StringHelper.escapeHtml(userLog));
		}
	}
	
	private boolean isPanelOpen(UserRequest ureq, String panelId, boolean def) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(MSCourseNodeRunController.class, getOpenPanelId(panelId));
		return showConfig == null ? def : showConfig.booleanValue();
	}
	
	private void saveOpenPanel(UserRequest ureq, String panelId, boolean newValue) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(MSCourseNodeRunController.class, getOpenPanelId(panelId), new Boolean(newValue));
		}
		myContent.contextPut("in-" + panelId, new Boolean(newValue));
	}
	
	private String getOpenPanelId(String panelId) {
		return panelId + "::" + userCourseEnv.getCourseEnvironment().getCourseResourceableId() + "::" + courseNode.getIdent();
	}

	@Override
	protected void doDispose() {
		// do nothing here yet
	}
}
