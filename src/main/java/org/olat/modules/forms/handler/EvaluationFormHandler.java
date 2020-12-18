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
package org.olat.modules.forms.handler;

import static org.olat.modules.forms.handler.EvaluationFormResource.FORM_XML_FILE;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ZippedDirectoryMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.PathUtils;
import org.olat.core.util.PathUtils.YesMatcher;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormReadyToDelete;
import org.olat.modules.forms.EvaluationFormsModule;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.FormXStream;
import org.olat.modules.forms.ui.EvaluationFormEditorController;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.forms.ui.EvaluationFormRuntimeController;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.references.ReferenceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EvaluationFormHandler implements RepositoryHandler {
	
	private static final Logger log = Tracing.createLoggerFor(EvaluationFormHandler.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private EvaluationFormsModule formsModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OLATResourceManager olatResourceManager;
	@Autowired
	private EvaluationFormManager evaluationFormManager;

	@Override
	public boolean supportCreate(Identity identity, Roles roles) {
		return formsModule.isEnabled();
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "new.form";
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description,
			Object createObject, Organisation organisation, Locale locale) {
		EvaluationFormResource ores = new EvaluationFormResource();
		OLATResource resource = olatResourceManager.findOrPersistResourceable(ores);
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description, resource,
				RepositoryEntryStatusEnum.preparation, organisation);
		dbInstance.commit();
		
		File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), FileResourceManager.ZIPDIR);
		if(!repositoryDir.exists()) {
			repositoryDir.mkdirs();
		}

		Form form = new Form();
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		XStreamHelper.writeObject(FormXStream.getXStream(), formFile, form);
		return re;
	}
	
	@Override
	public boolean supportImport() {
		return true;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return EvaluationFormResource.evaluate(file, filename);
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
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, String description,
			boolean withReferences, Organisation organisation, Locale locale, File file, String filename) {
		
		EvaluationFormResource ores = new EvaluationFormResource();
		OLATResource resource = olatResourceManager.createAndPersistOLATResourceInstance(ores);
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource).getBasefile();
		File zipDir = new File(fResourceFileroot, FileResourceManager.ZIPDIR);
		copyResource(file, filename, zipDir);

		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description, resource,
				RepositoryEntryStatusEnum.preparation, organisation);
		dbInstance.commit();
		return re;
	}
	
	private boolean copyResource(File file, String filename, File targetDirectory) {
		try {
			Path path = FileResource.getResource(file, filename);
			if(path == null) {
				return false;
			}
			
			Path destDir = targetDirectory.toPath();
			Files.walkFileTree(path, new CopyVisitor(path, destDir, new YesMatcher()));
			PathUtils.closeSubsequentFS(path);
			return true;
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname,
			String description, Organisation organisation, Locale locale, String url) {
		//
		return null;
	}
	
	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target) {
		File sourceRootFile = FileResourceManager.getInstance().getFileResourceRootImpl(source.getOlatResource()).getBasefile();
		File targetRootDir = FileResourceManager.getInstance().getFileResourceRootImpl(target.getOlatResource()).getBasefile();
		File sourceDir = new File(sourceRootFile, FileResourceManager.ZIPDIR);
		File targetDir = new File(targetRootDir, FileResourceManager.ZIPDIR);
		FileUtils.copyDirContentsToDir(sourceDir, targetDir, false, "Copy");
		return target;
	}

	@Override
	public boolean supportsDownload() {
		return true;
	}

	@Override
	public EditionSupport supportsEdit(OLATResourceable resource, Identity identity, Roles roles) {
		return EditionSupport.yes;
	}
	
	@Override
	public boolean supportsAssessmentDetails() {
		return false;
	}
	
	@Override
	public VFSContainer getMediaContainer(RepositoryEntry repoEntry) {
		return FileResourceManager.getInstance()
				.getFileResourceMedia(repoEntry.getOlatResource());
	}

	@Override
	public boolean readyToDelete(RepositoryEntry entry, Identity identity, Roles roles, Locale locale, ErrorList errors) {
		String referencesSummary = referenceManager.getReferencesToSummary(entry.getOlatResource(), locale);
		if (referencesSummary != null) {
			Translator translator = Util.createPackageTranslator(RepositoryManager.class, locale);
			errors.setError(translator.translate("details.delete.error.references",
					new String[] { referencesSummary, entry.getDisplayname() }));
			return false;
		}
		
		boolean delete = true;
		Map<String,EvaluationFormReadyToDelete> deleteDelegates = CoreSpringFactory.getBeansOfType(EvaluationFormReadyToDelete.class);
		for(EvaluationFormReadyToDelete delegate:deleteDelegates.values()) {
			delete &= delegate.readyToDelete(entry, locale, errors);
		}
		return delete;
	}

	@Override
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
		FileResourceManager.getInstance().deleteFileResource(res);
		return true;
	}

	/**
	 * Transform the map in a XML file and zip it (Repository export want a zip)
	 * @see org.olat.repository.handlers.RepositoryHandler#getAsMediaResource(org.olat.core.id.OLATResourceable)
	 */
	@Override
	public MediaResource getAsMediaResource(OLATResourceable res) {
		File unzippedDir = FileResourceManager.getInstance().unzipFileResource(res);
		String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(res.getResourceableId());
		return new ZippedDirectoryMediaResource(displayName, unzippedDir);
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl control, TooledStackedPanel toolbar) {
		File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), FileResourceManager.ZIPDIR);
		File formFile = new File(repositoryDir, FORM_XML_FILE);
		DataStorage storage = evaluationFormManager.loadStorage(re);
		
		//if in use -> edition is restricted
		boolean restrictedEdit = evaluationFormManager.isEvaluationFormActivelyUsed(re);
		boolean restrictedEditWeight = evaluationFormManager.isEvaluationFormWeightActivelyUsed(re);
		if(restrictedEdit) {
			Translator translator = Util.createPackageTranslator(EvaluationFormRuntimeController.class, ureq.getLocale());
			toolbar.setMessage(translator.translate("evaluation.form.in.use"));
			toolbar.setMessageCssClass("o_warning");
		}
		return new EvaluationFormEditorController(ureq, control, formFile, storage, restrictedEdit, restrictedEditWeight);
	}
	
	public File getFormFile(RepositoryEntry re) {
		File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), FileResourceManager.ZIPDIR);
		return new File(repositoryDir, FORM_XML_FILE);
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		return new EvaluationFormRuntimeController(ureq, wControl, re, reSecurity,
			(uureq, wwControl, toolbarPanel, entry, security, assessmentMode) -> {
					File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), FileResourceManager.ZIPDIR);
					File formFile = new File(repositoryDir, FORM_XML_FILE);
					DataStorage storage = evaluationFormManager.loadStorage(re);
					return new EvaluationFormExecutionController(uureq, wwControl, formFile, storage);
			});
	}

	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}

	@Override
	public String getSupportedType() {
		return EvaluationFormResource.TYPE_NAME;
	}

	@Override
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
		return CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, identity, "subkey", null);
	}

	@Override
	public void releaseLock(LockResult lockResult) {
		if(lockResult!=null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockResult);
		}
	}

	@Override
	public boolean isLocked(OLATResourceable ores) {
		return CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(ores, "subkey");
	}

	private static class CopyVisitor extends SimpleFileVisitor<Path> {

		private final Path source;
		private final Path destDir;
		private final PathMatcher filter;
		
		public CopyVisitor(Path source, Path destDir, PathMatcher filter) {
			this.source = source;
			this.destDir = destDir;
			this.filter = filter;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
	    throws IOException {
			Path relativeFile = source.relativize(file);
	        final Path destFile = Paths.get(destDir.toString(), relativeFile.toString());
	        if(filter.matches(file)) {
	        		Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
	        }
	        return FileVisitResult.CONTINUE;
		}
	 
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		throws IOException {
			Path relativeDir = source.relativize(dir);
	        final Path dirToCreate = Paths.get(destDir.toString(), relativeDir.toString());
	        if(Files.notExists(dirToCreate)){
	        		Files.createDirectory(dirToCreate);
	        }
	        return FileVisitResult.CONTINUE;
		}
	}
}
