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
package org.olat.repository.handlers;

import java.io.File;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseModule;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExportLinkEnum;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.ui.author.CreateCourseFromTemplateContext;
import org.olat.repository.ui.author.CreateCourseFromTemplateStep01;

/**
 * Initial date: 2025-11-04<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseTemplateHandler extends CourseHandler {
	
	@Override
	public boolean hasCreateWizard() {
		return true;
	}

	@Override
	public StepsMainRunController startCreateWizard(UserRequest ureq, WindowControl windowControl, Translator translator) {
		CreateCourseFromTemplateContext context = new CreateCourseFromTemplateContext();
		Step start = new CreateCourseFromTemplateStep01(ureq, context);
		StepRunnerCallback finish = (UserRequest ur, WindowControl wc, StepsRunContext rc) -> StepsMainRunController.DONE_MODIFIED;
		return new StepsMainRunController(ureq, windowControl, start, finish, null, 
				translator.translate("new.course.from.template"), "");
	}

	@Override
	public String getSupportedType() {
		return CourseModule.getCourseTypeName() + "Template";
	}

	@Override
	public String getCreateLabelI18nKey() {
		return "new.course.from.template";
	}

	@Override
	public boolean supportImport() {
		return false;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return ResourceEvaluation.notValid();
	}

	@Override
	public boolean supportImportUrl() {
		return false;
	}

	@Override
	public ResourceEvaluation acceptImport(String url) {
		return ResourceEvaluation.notValid();
	}

	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, 
										  String description, RepositoryEntryImportExportLinkEnum withLinkedReferences, 
										  Organisation organisation, Locale locale, File file, String filename) {
		return null;
	}

	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, 
										  String description, Organisation organisation, Locale locale, String url) {
		return null;
	}

	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target) {
		return null;
	}

	@Override
	public boolean supportsDownload() {
		return false;
	}

	@Override
	public EditionSupport supportsEdit(OLATResourceable resource, Identity identity, Roles roles) {
		return EditionSupport.no;
	}

	@Override
	public boolean supportsAssessmentDetails() {
		return false;
	}

	@Override
	public VFSContainer getMediaContainer(RepositoryEntry repoEntry) {
		return null;
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		return null;
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		return null;
	}

	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}
}
