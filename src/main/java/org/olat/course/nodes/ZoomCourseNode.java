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

import org.apache.logging.log4j.Logger;
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
import org.olat.course.nodes.zoom.ZoomEditController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ui.ZoomRunController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ZoomCourseNode extends AbstractAccessableCourseNode {

    private static final Logger log = Tracing.createLoggerFor(ZoomCourseNode.class);

    private static final long serialVersionUID = 257132040249310222L;
	public static final String TYPE = "zoom";

    public ZoomCourseNode() {
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
        if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

        return StatusDescription.NOERROR;
    }

    @Override
    public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
                                                   ICourse course, UserCourseEnvironment userCourseEnv) {
        CourseNode courseNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
        RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
        String subIdent = courseNode.getIdent();
        ZoomEditController zoomEditController = new ZoomEditController(ureq, wControl, entry, subIdent);
        NodeEditController nodeEditController = new NodeEditController(ureq, wControl, stackPanel, course, courseNode,
                userCourseEnv, zoomEditController);
        nodeEditController.addControllerListener(zoomEditController);
        return nodeEditController;
    }

    @Override
    public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
        RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
        String subIdent = getIdent();
        ZoomRunController zoomRunController = new ZoomRunController(ureq, wControl,
                ZoomManager.ApplicationType.courseElement, entry, subIdent, null, userCourseEnv.isAdmin(),
                userCourseEnv.isCoach(), userCourseEnv.isParticipant());
        Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, zoomRunController, userCourseEnv, this, "o_vc_icon");
        return new NodeRunConstructionResult(ctrl);
    }

    @Override
    public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
        return new StatusDescription[0];
    }

    @Override
    public void cleanupOnDelete(ICourse course) {
        super.cleanupOnDelete(course);

        ZoomManager zoomManager = CoreSpringFactory.getImpl(ZoomManager.class);
        if (zoomManager == null) {
            log.warn("zoomManager is not available");
            return;
        }

        RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
        zoomManager.deleteConfig(entry, getIdent(), null);
    }

    @Override
    public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings, CourseEnvironmentMapper envMapper) {
        super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);

        updateZoomProfile(envMapper, sourceCourse, course, sourceCourseNode.getIdent(), getIdent());
    }

    @Override
    public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
        super.postCopy(envMapper, processType, course, sourceCourse, context);

        updateZoomProfile(envMapper, sourceCourse, course, getIdent(), getIdent());
    }

    private void updateZoomProfile(CourseEnvironmentMapper envMapper, ICourse sourceCourse, ICourse targetCourse, String sourceSubIdent, String targetSubIdent) {
        ZoomManager zoomManager = CoreSpringFactory.getImpl(ZoomManager.class);
        if (zoomManager == null) {
            log.warn("zoomManager is not available");
            return;
        }

        ZoomConfig sourceConfig = zoomManager.getConfig(sourceCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), sourceSubIdent, null);
        if (sourceConfig != null) {
            RepositoryEntry targetEntry = targetCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
            zoomManager.initializeConfig(targetEntry, targetSubIdent, null, ZoomManager.ApplicationType.courseElement, envMapper.getAuthor().getUser());
            ZoomConfig targetConfig = zoomManager.getConfig(targetEntry, targetSubIdent, null);
            if (sourceConfig.getProfile() != targetConfig.getProfile()) {
                zoomManager.recreateConfig(targetConfig, targetEntry, targetSubIdent, null, sourceConfig.getProfile());
            }
        }
    }
}
