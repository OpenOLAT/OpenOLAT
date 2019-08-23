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

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.video.VideoEditController;
import org.olat.course.nodes.video.VideoPeekviewController;
import org.olat.course.nodes.video.VideoRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;

/**
 * Coursenode to display a videoresource
 * 
 * @author Dirk Furrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = -3808867902051897291L;
	private static final String TYPE = "video";

	public VideoCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		VideoEditController childTabCntrllr = new VideoEditController(this, ureq, wControl, course, euce);
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, euce, childTabCntrllr);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return VideoEditController.getVideoReference(getModuleConfiguration(), false);
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(
			UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne,
			String nodecmd) {
		updateModuleConfigDefaults(false);
		VideoRunController cprunC = new VideoRunController(getModuleConfiguration(), wControl, ureq, userCourseEnv, this);
		return cprunC.createNodeRunConstructionResult(ureq);
	}

	@Override
	public StatusDescription isConfigValid(){
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		StatusDescription sd = StatusDescription.NOERROR;
		boolean isValid = VideoEditController.isModuleConfigValid(getModuleConfiguration());
		if (!isValid) {
			// FIXME: refine statusdescriptions
			String shortKey = "no.video.chosen";
			String longKey = "error.noreference.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(VideoEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(VideoEditController.PANE_TAB_VIDEOCONFIG);
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		// only here we know which translator to take for translating condition
		// error messages
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}

	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		RepositoryEntry re = VideoEditController.getVideoReference(getModuleConfiguration(), false);
		if (re == null) return;
		File fExportDirectory = new File(exportDirectory, getIdent());
		fExportDirectory.mkdirs();
		RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fExportDirectory);
		reie.exportDoExport();
	}

	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importDirectory, getIdent());
		if(withReferences && rie.anyExportedPropertiesAvailable()) {
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(VideoFileResource.TYPE_NAME);
			RepositoryEntry re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
					rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
			VideoEditController.setVideoReference(re, getModuleConfiguration());
		} else {
			VideoEditController.removeVideoReference(getModuleConfiguration());
		}
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		return new VideoPeekviewController(ureq, wControl,
				getReferencedRepositoryEntry().getOlatResource(),
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey(),
				getIdent());
	}
}
