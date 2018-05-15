// <OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.control.generic.tabbable.TabbableDefaultController;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.AbstractAccessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.StatusDescriptionHelper;
import org.olat.course.nodes.TitledWrapperHelper;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

import de.bps.course.nodes.vc.NoProviderController;
import de.bps.course.nodes.vc.VCConfiguration;
import de.bps.course.nodes.vc.VCEditController;
import de.bps.course.nodes.vc.VCRunController;
import de.bps.course.nodes.vc.provider.VCProvider;
import de.bps.course.nodes.vc.provider.VCProviderFactory;

/**
 * Description:<br>
 * date list course node.
 * 
 * <P>
 * Initial Date: 19.07.2010 <br>
 * 
 * @author Jens Lindner (jlindne4@hs-mittweida.de)
 * @author skoeber
 */
public class VCCourseNode extends AbstractAccessableCourseNode {

	private static final String TYPE = "vc";

	// configuration
	public static final String CONF_VC_CONFIGURATION = "vc_configuration";
	public final static String CONF_PROVIDER_ID = "vc_provider_id";

	public VCCourseNode() {
		super(TYPE);
	}

	/**
	 * To support different virtual classroom implementations it's necessary to
	 * check whether the persisted configuration suits to the actual virtual
	 * classroom implementation or not. If not a new one will be created and
	 * persisted.
	 * 
	 * @param provider
	 * @return the persisted configuration or a fresh one
	 */
	private VCConfiguration handleConfig(final VCProvider provider) {
		getModuleConfiguration().setStringValue(CONF_PROVIDER_ID, provider.getProviderId());
		VCConfiguration config = (VCConfiguration) getModuleConfiguration().get(CONF_VC_CONFIGURATION);
		if (config == null || config.getProviderId() == null || !config.getProviderId().equals(provider.getProviderId())) {
			config = provider.createNewConfiguration();
		}
		getModuleConfiguration().set(CONF_VC_CONFIGURATION, config);
		return config;
	}

	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		// no update to default config necessary
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,
			UserCourseEnvironment userCourseEnv) {
		updateModuleConfigDefaults(false);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		// load and check configuration
		String providerId = getModuleConfiguration().getStringValue(CONF_PROVIDER_ID);
		VCProvider provider = providerId == null ? VCProviderFactory.createDefaultProvider() : VCProviderFactory.createProvider(providerId);
		
		TabbableDefaultController childTabCntrllr;
		if(provider != null) {
			VCConfiguration config = handleConfig(provider);
			// create room if configured to do it immediately
			if(config.isCreateMeetingImmediately()) {
				Long key = course.getResourceableId();
				// here, the config is empty in any case, thus there are no start and end dates
				provider.createClassroom(key + "_" + this.getIdent(), this.getShortName(), this.getLongTitle(), null, null, config);
			}
			// create edit controller
			childTabCntrllr = new VCEditController(ureq, wControl, this, course, userCourseEnv, provider, config);
		} else {
			//empty panel
			childTabCntrllr = new NoProviderController(ureq, wControl);
		}
		
		NodeEditController nodeEditCtr = new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode,
				userCourseEnv, childTabCntrllr);
		nodeEditCtr.addControllerListener(childTabCntrllr);
		return nodeEditCtr;
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		updateModuleConfigDefaults(false);
		// check if user is moderator of the virtual classroom
		Roles roles = ureq.getUserSession().getRoles();
		boolean moderator = roles.isOLATAdmin();
		Long key = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		if (!moderator) {
			if(roles.isLearnResourceManager() || roles.isAuthor() || roles.isOLATAdmin()) {
				RepositoryManager rm = RepositoryManager.getInstance();
				RepositoryEntry re = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				if (re != null) {
					moderator = rm.isOwnerOfRepositoryEntry(ureq.getIdentity(), re);
					if(!moderator) {
						moderator = rm.isLearnResourceManagerFor(ureq.getUserSession().getRoles(), re);
					}
				}
			}
		}
		// load configuration
		final String providerId = getModuleConfiguration().getStringValue(CONF_PROVIDER_ID);
		VCProvider provider = providerId == null ? VCProviderFactory.createDefaultProvider() : VCProviderFactory.createProvider(providerId);
		VCConfiguration config = handleConfig(provider);
		// create run controller
		Controller runCtr = new VCRunController(ureq, wControl, key + "_" + getIdent(), getShortName(), getLongTitle(), config, provider,
				moderator, userCourseEnv.isCourseReadOnly());
		Controller controller = TitledWrapperHelper.getWrapper(ureq, wControl, runCtr, this, "o_vc_icon");
		return new NodeRunConstructionResult(controller);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne) {
		return null;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}

	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }
		StatusDescription status = StatusDescription.NOERROR;
		
		// load configuration
		final String providerId = getModuleConfiguration().getStringValue(CONF_PROVIDER_ID);
		VCProvider provider = providerId == null ? VCProviderFactory.createDefaultProvider() : VCProviderFactory.createProvider(providerId);
		boolean invalid = provider == null || !handleConfig(provider).isConfigValid();
		if (invalid) {
			String[] params = new String[] { this.getShortTitle() };
			String shortKey = "error.config.short";
			String longKey = "error.config.long";
			String translationPackage = VCEditController.class.getPackage().getName();
			status = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, translationPackage);
			status.setDescriptionForUnit(getIdent());
			status.setActivateableViewIdentifier(VCEditController.PANE_TAB_VCCONFIG);
		}
		
		return status;
	}

	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		// load configuration
		final String providerId = getModuleConfiguration().getStringValue(CONF_PROVIDER_ID);
		VCProvider provider = providerId == null ? VCProviderFactory.createDefaultProvider() : VCProviderFactory.createProvider(providerId);
		VCConfiguration config = handleConfig(provider);
		// remove meeting
		provider.removeClassroom(course.getResourceableId() + "_" + this.getIdent(), config);
	}

}
// </OLATCE-103>