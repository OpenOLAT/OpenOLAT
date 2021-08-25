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
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.card2brain.Card2BrainEditController;
import org.olat.course.nodes.card2brain.Card2BrainPeekViewController;
import org.olat.course.nodes.card2brain.Card2BrainRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.card2brain.Card2BrainModule;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10.04.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Card2BrainCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = -7338962050297071079L;
	
	public static final String TYPE = "card2brain";
	
	public static final String CONFIG_FLASHCARD_ALIAS = "flashcardAlias";
	public static final String CONFIG_ENABLE_PRIVATE_LOGIN = "enablePrivateLogin";
	public static final String CONFIG_PRIVATE_KEY = "privateKey";
	public static final String CONFIG_PRIVATE_SECRET = "privateSecret";
	
	public Card2BrainCourseNode() {
		super(TYPE);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		Card2BrainEditController childTabCntrllr = new Card2BrainEditController(ureq, wControl, getModuleConfiguration());
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller runCtrl;
		if(userCourseEnv.isCourseReadOnly()) {
			Translator trans = Util.createPackageTranslator(Card2BrainCourseNode.class, ureq.getLocale());
		    String title = trans.translate("freezenoaccess.title");
		    String message = trans.translate("freezenoaccess.message");
		    runCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else if (userCourseEnv.getIdentityEnvironment().getRoles().isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(Card2BrainCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
		    runCtrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			runCtrl = new Card2BrainRunController(ureq, wControl, this.getModuleConfiguration());
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runCtrl, userCourseEnv, this, "o_card2brain_icon");
		return new NodeRunConstructionResult(ctrl);
	}
		
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, nodeSecCallback, null).getRunController();
	}
	
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		return new Card2BrainPeekViewController(ureq, wControl, 
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey(), 
				getIdent(),
				this.getModuleConfiguration().getStringValue(Card2BrainCourseNode.CONFIG_FLASHCARD_ALIAS));
	}

	@SuppressWarnings("deprecation")
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public StatusDescription isConfigValid() {
		if(oneClickStatusCache!=null) {
			return oneClickStatusCache[0];
		}
		
		StatusDescription sd =  StatusDescription.NOERROR;
		Card2BrainModule card2BrainModule = CoreSpringFactory.getImpl(Card2BrainModule.class);
		boolean isEnterpriseLogin = !getModuleConfiguration().getBooleanSafe(CONFIG_ENABLE_PRIVATE_LOGIN);
		if (!card2BrainModule.isEnterpriseLoginEnabled() && !card2BrainModule.isPrivateLoginEnabled()) {
			// both logins are deactivated
			String shortKey = "edit.warning.bothLoginDisabled.short";
			String longKey = "edit.warning.bothLoginDisabled";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(Card2BrainEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(Card2BrainEditController.PANE_TAB_VCCONFIG);
		} else if (isEnterpriseLogin && !card2BrainModule.isEnterpriseLoginEnabled()) {
			// enterprise login is not enabled anymore
			String shortKey = "edit.warning.enterpriseLoginDisabled.short";
			String longKey = "edit.warning.enterpriseLoginDisabled";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(Card2BrainEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(Card2BrainEditController.PANE_TAB_VCCONFIG);
		} else if (!isEnterpriseLogin && !card2BrainModule.isPrivateLoginEnabled()) {
			// private login is not enabled anymore
			String shortKey = "edit.warning.privateLoginDisabled.short";
			String longKey = "edit.warning.privateLoginDisabled";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(Card2BrainEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(Card2BrainEditController.PANE_TAB_VCCONFIG);
		}
		return sd;
	}
	
	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

}
