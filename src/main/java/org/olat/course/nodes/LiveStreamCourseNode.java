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
package org.olat.course.nodes;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.cal.CalSecurityCallback;
import org.olat.course.nodes.cal.CalSecurityCallbackFactory;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.nodes.livestream.LiveStreamModule;
import org.olat.course.nodes.livestream.LiveStreamSecurityCallback;
import org.olat.course.nodes.livestream.LiveStreamSecurityCallbackFactory;
import org.olat.course.nodes.livestream.ui.LiveStreamEditController;
import org.olat.course.nodes.livestream.ui.LiveStreamPeekviewController;
import org.olat.course.nodes.livestream.ui.LiveStreamRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 22 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = 1948699450468358698L;
	
	public static final String TYPE = "livestream";
	
	public static final int CURRENT_VERSION = 1;
	public static final String CONFIG_BUFFER_BEFORE_MIN = "bufferBeforeMin";
	public static final String CONFIG_BUFFER_AFTER_MIN = "bufferBeforeAfter";
	public static final String CONFIG_COACH_CAN_EDIT = "coachCanEdit";
	public static final String CONFIG_PLAYER_PROFILE = "playerProfile";

	public LiveStreamCourseNode() {
		this(null);
	}

	public LiveStreamCourseNode(INode parent) {
		super(TYPE, parent);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		LiveStreamEditController editCtrl = new LiveStreamEditController(ureq, wControl, getModuleConfiguration());
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, editCtrl);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller runCtrl;
		if (userCourseEnv.isCourseReadOnly()) {
			Translator trans = Util.createPackageTranslator(Card2BrainCourseNode.class, ureq.getLocale());
			String title = trans.translate("freezenoaccess.title");
			String message = trans.translate("freezenoaccess.message");
			runCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			CalSecurityCallback calSecCallback = CalSecurityCallbackFactory.createCourseCalendarCallback(userCourseEnv);
			CourseCalendars calendars = CourseCalendars.createCourseCalendarsWrapper(ureq, wControl, userCourseEnv,
					calSecCallback);
			LiveStreamSecurityCallback secCallback = LiveStreamSecurityCallbackFactory
					.createSecurityCallback(userCourseEnv, this.getModuleConfiguration());
			runCtrl = new LiveStreamRunController(ureq, wControl, this, userCourseEnv, secCallback, calendars);
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runCtrl, this, "o_livestream_icon");
		return new NodeRunConstructionResult(ctrl);
	}
	
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		CalSecurityCallback secCallback = CalSecurityCallbackFactory.createCourseCalendarCallback(userCourseEnv);
		CourseCalendars calendars = CourseCalendars.createCourseCalendarsWrapper(ureq, wControl, userCourseEnv,
				secCallback);
		return new LiveStreamPeekviewController(ureq, wControl, getIdent(), this.getModuleConfiguration(), calendars);
	}

	@SuppressWarnings("deprecation")
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}
	
	@Override
	public StatusDescription isConfigValid() {
		return  StatusDescription.NOERROR;
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			LiveStreamModule liveStreamModule = CoreSpringFactory.getImpl(LiveStreamModule.class);
			config.setIntValue(CONFIG_BUFFER_BEFORE_MIN, liveStreamModule.getBufferBeforeMin());
			config.setIntValue(CONFIG_BUFFER_AFTER_MIN, liveStreamModule.getBufferAfterMin());
			config.setBooleanEntry(CONFIG_COACH_CAN_EDIT, liveStreamModule.isEditCoach());
			// CONFIG_PLAYER_PROFILE has no default value, because previously the multi
			// stream option has to be enabled and the default value has to be selected.
		}
		config.setConfigurationVersion(CURRENT_VERSION);
	}
	
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

}
