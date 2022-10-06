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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.model.LicenseImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.PathUtils;
import org.olat.core.util.PathUtils.YesMatcher;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.LockResult;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.InMemoryOutcomeListener;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.pool.QTI21QPoolServiceProvider;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.QTI21AssessmentDetailsController;
import org.olat.ims.qti21.ui.QTI21OverrideOptions;
import org.olat.ims.qti21.ui.QTI21RuntimeController;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.model.QItemList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.FileHandler;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;

/**
 * 
 * Initial date: 08.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21AssessmentTestHandler extends FileHandler {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21AssessmentTestHandler.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Module qtiModule;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private QTI21QPoolServiceProvider qpoolServiceProvider;
	
	@Autowired
	private AssessmentTestSessionDAO assessmentTestSessionDao;

	@Override
	public String getSupportedType() {
		return ImsQTI21Resource.TYPE_NAME;
	}

	@Override
	public boolean supportCreate(Identity identity, Roles roles) {
		return true;
	}

	@Override
	public String getCreateLabelI18nKey() {
		return "new.test";
	}

	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description,
			Object createObject, Organisation organisation, Locale locale) {
		ImsQTI21Resource ores = new ImsQTI21Resource();
		
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(ores);
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		dbInstance.commit();

		File repositoryDir = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), FileResourceManager.ZIPDIR);
		if(!repositoryDir.exists()) {
			repositoryDir.mkdirs();
		}
		if(createObject instanceof QItemList) {
			QItemList itemToImport = (QItemList)createObject;
			qpoolServiceProvider.exportToEditorPackage(displayname, repositoryDir,
					itemToImport.getItems(), itemToImport.isGroupByTaxonomyLevel(), locale);
		} else {
			createMinimalAssessmentTest(displayname, repositoryDir, locale);
		}
		return re;
	}
	
	public void createMinimalAssessmentTest(String displayName, File directory, Locale locale) {
        ManifestBuilder manifestBuilder = ManifestBuilder.createAssessmentTestBuilder();

		Translator translator = Util.createPackageTranslator(AssessmentTestComposerController.class, locale);

		//single choice
		File itemFile = new File(directory, IdentifierGenerator.newAsString(QTI21QuestionType.sc.getPrefix()) + ".xml");
		AssessmentItem assessmentItem = AssessmentItemFactory.createSingleChoice(translator.translate("new.sc"), translator.translate("new.answer"));
		QtiSerializer qtiSerializer = qtiService.qtiSerializer();
		manifestBuilder.appendAssessmentItem(itemFile.getName());	
		
		//test
        File testFile = new File(directory, IdentifierGenerator.newAssessmentTestFilename());
		AssessmentTest assessmentTest = AssessmentTestFactory.createAssessmentTest(displayName, translator.translate("new.section"));
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
	public boolean supportImport() {
		return true;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return ImsQTI21Resource.evaluate(file, filename);
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
		ImsQTI21Resource ores = new ImsQTI21Resource();
		OLATResource resource = OLATResourceManager.getInstance().createAndPersistOLATResourceInstance(ores);
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRoot(resource);
		File zipDir = new File(fResourceFileroot, FileResourceManager.ZIPDIR);
		copyResource(file, filename, zipDir);
		
		File optionsFile = new File(zipDir, QTI21Service.PACKAGE_CONFIG_FILE_NAME);
		if(optionsFile.exists()) {
			try {// move the options to the root directory
				File target = new File(fResourceFileroot, QTI21Service.PACKAGE_CONFIG_FILE_NAME);
				Files.move(optionsFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				log.error("", e);
			}
		} 

		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class).create(initialAuthor, null, "", displayname, description,
					resource, RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		return re;
	}
	
	private boolean copyResource(File file, String filename, File targetDirectory) {
		try {
			String fallbackEncoding = qtiModule.getImportEncodingFallback();
			Path path = FileResource.getResource(file, filename, fallbackEncoding);
			if(path == null) {
				return false;
			}
			
			Path destDir = targetDirectory.toPath();
			QTI21IMSManifestExplorerVisitor visitor = new QTI21IMSManifestExplorerVisitor();
			Files.walkFileTree(path, visitor);
			Files.walkFileTree(path, new CopyAndConvertVisitor(path, destDir, visitor.getInfos(), new YesMatcher()));
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
		return null;
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res) {
		return new QTI21AssessmentTestMediaResource(res);
	}
	
	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target) {
		File sourceRootFile = FileResourceManager.getInstance().getFileResourceRootImpl(source.getOlatResource()).getBasefile();
		File targetRootDir = FileResourceManager.getInstance().getFileResourceRootImpl(target.getOlatResource()).getBasefile();
		File sourceDir = new File(sourceRootFile, FileResourceManager.ZIPDIR);
		File targetDir = new File(targetRootDir, FileResourceManager.ZIPDIR);
		FileUtils.copyDirContentsToDir(sourceDir, targetDir, false, "Copy");
		File sourceOptionsFile = new File(sourceRootFile, QTI21Service.PACKAGE_CONFIG_FILE_NAME);
		if(sourceOptionsFile.exists()) {
			FileUtils.copyFileToDir(sourceOptionsFile, targetRootDir, "Copy QTI 2.1 Options");
		}
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
		return true;
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity,
			UserRequest ureq, WindowControl wControl) {
		return new QTI21RuntimeController(ureq, wControl, re, reSecurity, (uureq, wwControl, toolbarPanel, entry, repoSecurity, mode) -> {
			
			QTI21DeliveryOptions deliveryOptions = qtiService.getDeliveryOptions(entry);
			QTI21OverrideOptions overrideOptions = QTI21OverrideOptions.nothingOverriden();
			if(!deliveryOptions.isAllowAnonym() && uureq.getUserSession().getRoles().isGuestOnly()) {
				Translator translator = Util.createPackageTranslator(QTI21RuntimeController.class, uureq.getLocale());
				Controller contentCtr = MessageUIFactory.createInfoMessage(uureq, wwControl,
						translator.translate("anonym.not.allowed.title"),
						translator.translate("anonym.not.allowed.descr"));
				return new LayoutMain3ColsController(uureq, wwControl, contentCtr);
			}
			boolean authorMode = reSecurity.isEntryAdmin();
			if(authorMode) {
				return new AssessmentTestDisplayController(uureq, wwControl, new InMemoryOutcomeListener(), entry, entry, null,
						deliveryOptions, overrideOptions, false, authorMode, true);
			}
			CoreSpringFactory.getImpl(UserCourseInformationsManager.class)
				.updateUserCourseInformations(entry.getOlatResource(), uureq.getIdentity());
			return new AssessmentTestDisplayController(uureq, wwControl, null, entry, entry, null,
					deliveryOptions, overrideOptions, false, authorMode, false);
		});
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		return new AssessmentTestComposerController(ureq, wControl, toolbar, re);
	}

	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbar, Identity assessedIdentity) {
		return new QTI21AssessmentDetailsController(ureq, wControl, toolbar, re, assessedIdentity);
	}

	@Override
	public List<License> getElementsLicenses(RepositoryEntry entry) {
		List<License> licenses = new ArrayList<>();
		
		try {
			FileResourceManager frm = FileResourceManager.getInstance();
			File unzippedDirRoot = frm.unzipFileResource(entry.getOlatResource());
			ManifestBuilder manifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
			ResolvedAssessmentTest resolvedObject = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, true);
			AssessmentTest assessmentTest = resolvedObject.getRootNodeLookup().extractIfSuccessful();
			List<String> metadataLicenses = new ArrayList<>();
			List<String> metadataIdentifiers = new ArrayList<>();
			for(TestPart part:assessmentTest.getTestParts()) {
				collectElementsLicensesRecursive(part, metadataLicenses, metadataIdentifiers, manifestBuilder);
			}
			
			List<QuestionItemShort> items = qpoolService.loadItemsByIdentifier(metadataIdentifiers);
			if(!items.isEmpty()) {
				licenses.addAll(licenseService.loadLicenses(items));
			}
			if(!metadataLicenses.isEmpty()) {
				for(String metadataLicense:metadataLicenses) {
					License license = new LicenseImpl();
					license.setFreetext(metadataLicense);
					licenses.add(license);
				}
			}
		} catch(Exception e) {
			log.error("", e);
		}

		return licenses;
	}
	
	private void collectElementsLicensesRecursive(AbstractPart part, List<String> licenses, List<String> metadataIdentifiers, ManifestBuilder manifestBuilder) {
		if(part instanceof AssessmentItemRef) {
			AssessmentItemRef itemRef = (AssessmentItemRef)part;
			
			ManifestMetadataBuilder metadata = manifestBuilder.getResourceBuilderByHref(itemRef.getHref().toString());
			if(metadata != null) {
				if(metadata.getLom(false) != null) {
					String license = metadata.getLicense();
					if(StringHelper.containsNonWhitespace(license)) {
						licenses.add(license);
					}
				}
				
				String metadataIdentifier = metadata.getOpenOLATMetadataIdentifier();
				if(!StringHelper.containsNonWhitespace(metadataIdentifier)) {
					metadataIdentifier = metadata.getOpenOLATMetadataMasterIdentifier();
				}
				if(StringHelper.containsNonWhitespace(metadataIdentifier)) {
					metadataIdentifiers.add(metadataIdentifier);
				}
			}
		}

		List<? extends AbstractPart> childParts = part.getChildAbstractParts();
		for(AbstractPart childPart:childParts) {
			collectElementsLicensesRecursive(childPart, licenses, metadataIdentifiers, manifestBuilder);
		}
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
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res) {
		boolean clean = super.cleanupOnDelete(entry, res);
		assessmentTestSessionDao.deleteAllUserTestSessionsByTest(entry);
		return clean;
	}

	@Override
	protected String getDeletedFilePrefix() {
		return null;
	}
}