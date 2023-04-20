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

import java.io.Serial;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.jupyterhub.JupyterHubEditController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.jupyterhub.JupyterDeployment;
import org.olat.modules.jupyterhub.JupyterManager;
import org.olat.modules.jupyterhub.ui.JupyterHubRunController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

import org.apache.logging.log4j.Logger;

/**
 * Initial date: 2023-04-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class JupyterHubCourseNode extends AbstractAccessableCourseNode {

	@Serial
	private static final long serialVersionUID = -1122572597593821075L;

	public static final String TYPE = "jupyterHub";
	public static final String CLIENT_ID = "clientId";
	public static final String IMAGE = "image";
	public static final String SUPPRESS_DATA_TRANSMISSION_AGREEMENT = "suppressDataTransmissionAgreement";

	private static final Logger log = Tracing.createLoggerFor(JupyterHubCourseNode.class);

	public JupyterHubCourseNode() {
		super(TYPE);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}

		return StatusDescription.NOERROR;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl,
												   BreadcrumbPanel stackPanel, ICourse course,
												   UserCourseEnvironment userCourseEnv) {
		CourseNode courseNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		String subIdent = courseNode.getIdent();
		JupyterHubEditController jupyterHubEditController = new JupyterHubEditController(ureq, wControl, entry, subIdent, getModuleConfiguration());
		NodeEditController nodeEditController = new NodeEditController(ureq, wControl, stackPanel, course, courseNode,
				userCourseEnv, jupyterHubEditController);
		nodeEditController.addControllerListener(jupyterHubEditController);
		return nodeEditController;
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
																	 UserCourseEnvironment userCourseEnv,
																	 CourseNodeSecurityCallback nodeSecCallback,
																	 String nodecmd, VisibilityFilter visibilityFilter) {
		String clientId = getModuleConfiguration().getStringValue(CLIENT_ID);
		String image = getModuleConfiguration().getStringValue(IMAGE);
		Boolean suppressDataTransmissionAgreement = getModuleConfiguration().getBooleanEntry(SUPPRESS_DATA_TRANSMISSION_AGREEMENT);
		RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		String subIdent = getIdent();
		JupyterHubRunController jupyterHubRunController = new JupyterHubRunController(ureq, wControl, entry, subIdent,
				clientId, image, suppressDataTransmissionAgreement, userCourseEnv, this);
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, jupyterHubRunController, userCourseEnv,
				this, "o_jupyter_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		return new StatusDescription[0];
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);

		JupyterManager jupyterManager = CoreSpringFactory.getImpl(JupyterManager.class);
		if (jupyterManager == null) {
			log.warn("jupyterManager is not available");
			return;
		}

		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		jupyterManager.deleteJupyterHub(entry, getIdent());
	}

	@Override
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse,
									  ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		updateJupyterHubDeployment(sourceCourse, course, sourceCourseNode.getIdent(), getIdent());
	}

	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course,
						 ICourse sourceCourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCourse, context);

		updateJupyterHubDeployment(sourceCourse, course, getIdent(), getIdent());
	}

	private void updateJupyterHubDeployment(ICourse sourceCourse,
											ICourse targetCourse, String sourceSubIdent, String targetSubIdent) {
		JupyterManager jupyterManager = CoreSpringFactory.getImpl(JupyterManager.class);
		if (jupyterManager == null) {
			log.warn("jupyterManager is not available");
			return;
		}

		JupyterDeployment sourceJupyterDeployment = jupyterManager.getJupyterDeployment(
				sourceCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry(),
				sourceSubIdent
		);
		if (sourceJupyterDeployment != null) {
			RepositoryEntry targetEntry = targetCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			String clientId = getModuleConfiguration().getStringValue(CLIENT_ID);
			String image = getModuleConfiguration().getStringValue(IMAGE);
			Boolean suppressDataTransmissionAgreement = getModuleConfiguration().getBooleanEntry(SUPPRESS_DATA_TRANSMISSION_AGREEMENT);
			jupyterManager.initializeJupyterHubDeployment(targetEntry, targetSubIdent, clientId, image, suppressDataTransmissionAgreement);
			JupyterDeployment targetJupyterDeployment = jupyterManager.getJupyterDeployment(targetEntry, targetSubIdent);
			if (sourceJupyterDeployment.getJupyterHub() != targetJupyterDeployment.getJupyterHub()) {
				jupyterManager.recreateJupyterHubDeployment(targetJupyterDeployment, targetEntry, targetSubIdent,
						sourceJupyterDeployment.getJupyterHub());
			}
		}
	}
}
