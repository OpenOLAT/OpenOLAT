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
package org.olat.ims.qti21.repository.handlers;

import java.io.File;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.assessment.AssessmentMode;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.ims.qti21.ui.InMemoryOutcomesListener;
import org.olat.ims.qti21.ui.QTI21DisplayController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.FileHandler;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController;
import org.olat.repository.ui.RepositoryEntryRuntimeController.RuntimeControllerCreator;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 08.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21AssessmentHandler extends FileHandler {

	@Override
	public String getSupportedType() {
		return ImsQTI21Resource.TYPE_NAME;
	}

	@Override
	public boolean isCreate() {
		return false;
	}

	@Override
	public String getCreateLabelI18nKey() {
		return "tools.add.qti21";
	}

	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description,
			Object createObject, Locale locale) {
		return null;
	}

	@Override
	public boolean isPostCreateWizardAvailable() {
		return false;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return ImsQTI21Resource.evaluate(file, filename);
	}

	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, String description,
			boolean withReferences, Locale locale, File file, String filename) {
		ImsQTI21Resource ores = new ImsQTI21Resource();
		OLATResource resource = OLATResourceManager.getInstance().createAndPersistOLATResourceInstance(ores);
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource).getBasefile();
		File zipDir = new File(fResourceFileroot, FileResourceManager.ZIPDIR);
		FileResource.copyResource(file, filename, zipDir);

		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class)
				.create(initialAuthor, null, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		DBFactory.getInstance().commit();
		return re;
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		return null;
	}
	
	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target) {
		return null;
	}

	@Override
	public boolean supportsDownload() {
		return true;
	}

	@Override
	public EditionSupport supportsEdit(OLATResourceable resource) {
		return EditionSupport.no;
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity,
			UserRequest ureq, WindowControl wControl) {
		return new RepositoryEntryRuntimeController(ureq, wControl, re, reSecurity,
				new RuntimeControllerCreator() {
					@Override
					public Controller create(UserRequest uureq, WindowControl wwControl, TooledStackedPanel toolbarPanel,
							RepositoryEntry entry, RepositoryEntrySecurity reSecurity, AssessmentMode mode) {
						InMemoryOutcomesListener listener = new InMemoryOutcomesListener();
						return new QTI21DisplayController(uureq, wwControl, listener, entry);
					}
				});
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		return null;
	}

	@Override
	public StepsMainRunController createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		return null;
	}

	@Override
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
		return null;
	}

	@Override
	public void releaseLock(LockResult lockResult) {
		//
	}

	@Override
	public boolean isLocked(OLATResourceable ores) {
		return false;
	}

	@Override
	protected String getDeletedFilePrefix() {
		return null;
	}
}