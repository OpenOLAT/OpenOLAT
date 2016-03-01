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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
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
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.PathUtils.YesMatcher;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.assessment.AssessmentMode;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.ZippedDirectoryMediaResource;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.OnyxToQtiWorksHandler;
import org.olat.ims.qti21.pool.QTI21QPoolServiceProvider;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.InMemoryOutcomesListener;
import org.olat.ims.qti21.ui.QTI21RuntimeController;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.qpool.model.QItemList;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.FileHandler;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController.RuntimeControllerCreator;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;

/**
 * 
 * Initial date: 08.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21AssessmentTestHandler extends FileHandler {
	
	private static final OLog log = Tracing.createLoggerFor(QTI21AssessmentTestHandler.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private QTI21QPoolServiceProvider qpoolServiceProvider;
	
	@Autowired
	private AssessmentEntryDAO assessmentEntryDAO;
	@Autowired
	private AssessmentTestSessionDAO assessmentTestSessionDao;

	@Override
	public String getSupportedType() {
		return ImsQTI21Resource.TYPE_NAME;
	}

	@Override
	public boolean isCreate() {
		return true;
	}

	@Override
	public String getCreateLabelI18nKey() {
		return "tools.add.qti21";
	}

	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description, Object createObject, Locale locale) {
		ImsQTI21Resource ores = new ImsQTI21Resource();
		
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(ores);
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		dbInstance.commit();
		
		File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), FileResourceManager.ZIPDIR);
		if(!repositoryDir.exists()) {
			repositoryDir.mkdirs();
		}
		if(createObject instanceof QItemList) {
			QItemList itemToImport = (QItemList)createObject;
			qpoolServiceProvider.exportToEditorPackage(repositoryDir, itemToImport.getItems(), locale);
		} else if(createObject instanceof QTIEditorPackage) {
			QTIEditorPackage testToConvert = (QTIEditorPackage)createObject;
			qpoolServiceProvider.convertFromEditorPackage(testToConvert, repositoryDir, locale);
		} else {
			createMinimalAssessmentTest(displayname, repositoryDir);
		}
		return re;
	}
	
	public void createMinimalAssessmentTest(String displayName, File directory) {
        ManifestBuilder manifestBuilder = ManifestBuilder.createAssessmentTestBuilder();

		//single choice
		File itemFile = new File(directory, IdentifierGenerator.newAsString(QTI21QuestionType.sc.getPrefix()) + ".xml");
		AssessmentItem assessmentItem = AssessmentItemFactory.createSingleChoice();
		QtiSerializer qtiSerializer = qtiService.qtiSerializer();
		manifestBuilder.appendAssessmentItem(itemFile.getName());	
		
		//test
        File testFile = new File(directory, IdentifierGenerator.newAssessmentTestFilename());
		AssessmentTest assessmentTest = AssessmentTestFactory.createAssessmentTest(displayName);
		manifestBuilder.appendAssessmentTest(testFile.getName());
        
        // item -> test
        try {
			AssessmentSection section = assessmentTest.getTestParts().get(0).getAssessmentSections().get(0);
			AssessmentTestFactory.appendAssessmentItem(section, itemFile.getName());
		} catch (URISyntaxException e) {
			log.error("", e);
		}
        
        try(FileOutputStream out = new FileOutputStream(itemFile)) {
			qtiSerializer.serializeJqtiObject(assessmentItem, out);	
		} catch(Exception e) {
			log.error("", e);
		}
        
		try(FileOutputStream out = new FileOutputStream(testFile)) {
			qtiSerializer.serializeJqtiObject(assessmentTest, out);	
		} catch(Exception e) {
			log.error("", e);
		}

        manifestBuilder.write(new File(directory, "imsmanifest.xml"));
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
		copyResource(file, filename, zipDir);

		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class)
				.create(initialAuthor, null, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		DBFactory.getInstance().commit();
		return re;
	}
	
	private boolean copyResource(File file, String filename, File targetDirectory) {
		try {
			Path path = FileResource.getResource(file, filename);
			if(path == null) {
				return false;
			}
			
			Path destDir = targetDirectory.toPath();
			Files.walkFileTree(path, new CopyAndConvertVisitor(path, destDir, new YesMatcher()));
			return true;
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
	}
	
	private static class CopyAndConvertVisitor extends SimpleFileVisitor<Path> {

		private final Path source;
		private final Path destDir;
		private final PathMatcher filter;
		
		public CopyAndConvertVisitor(Path source, Path destDir, PathMatcher filter) {
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
	        	String filename = file.getFileName().toString();
	        	if(filename != null && filename.endsWith("xml")) {
	        		convertXmlFile(file, destFile);
	        	} else {
	        		Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
	        	}
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
		
		/**
		 * Convert the XML files
		 * @param inputFile
		 * @param outputFile
		 */
		private void convertXmlFile(Path inputFile, Path outputFile) {
			try(InputStream in = Files.newInputStream(inputFile);
					Writer out = Files.newBufferedWriter(outputFile, Charset.forName("UTF-8"))) {
				XMLOutputFactory xof = XMLOutputFactory.newInstance();
		        XMLStreamWriter xtw = xof.createXMLStreamWriter(out);
		
				SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
				OnyxToQtiWorksHandler myHandler = new OnyxToQtiWorksHandler(xtw);
				saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", myHandler);
				saxParser.parse(in, myHandler);
			} catch(Exception e) {
				log.error("", e);
			}
		}
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		File unzippedDir = FileResourceManager.getInstance().unzipFileResource(res);
		String displayName = CoreSpringFactory.getImpl(RepositoryManager.class)
				.lookupDisplayNameByOLATResourceableId(res.getResourceableId());
		return new ZippedDirectoryMediaResource(displayName, unzippedDir);
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
	public EditionSupport supportsEdit(OLATResourceable resource) {
		return EditionSupport.yes;
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity,
			UserRequest ureq, WindowControl wControl) {
		return new QTI21RuntimeController(ureq, wControl, re, reSecurity,
				new RuntimeControllerCreator() {
					@Override
					public Controller create(UserRequest uureq, WindowControl wwControl, TooledStackedPanel toolbarPanel,
							RepositoryEntry entry, RepositoryEntrySecurity repoSecurity, AssessmentMode mode) {
						InMemoryOutcomesListener listener = new InMemoryOutcomesListener();
						return new AssessmentTestDisplayController(uureq, wwControl, listener, entry, entry, null);
					}
				});
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		AssessmentTestComposerController editorCtrl = new AssessmentTestComposerController(ureq, wControl, toolbar, re);
		return editorCtrl;
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
	public boolean readyToDelete(RepositoryEntry entry, Identity identity, Roles roles, Locale locale, ErrorList errors) {
		return super.readyToDelete(entry, identity, roles, locale, errors);
	}

	@Override
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res) {
		boolean clean = super.cleanupOnDelete(entry, res);
		assessmentTestSessionDao.deleteUserTestSessions(entry);
		assessmentEntryDAO.deleteEntryForReferenceEntry(entry);
		return clean;
	}

	@Override
	protected String getDeletedFilePrefix() {
		return null;
	}
}