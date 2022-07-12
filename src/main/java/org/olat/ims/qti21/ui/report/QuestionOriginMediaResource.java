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
package org.olat.ims.qti21.ui.report;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.imscp.xml.manifest.ResourceType;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.manager.QuestionPoolLicenseHandler;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.references.Reference;
import org.olat.resource.references.ReferenceManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.SectionPart;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.RootNodeLookup;


/**
 * 
 * Initial date: 11 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionOriginMediaResource extends OpenXMLWorkbookResource {
	
	private static final Logger log = Tracing.createLoggerFor(QuestionOriginMediaResource.class);
	
	private boolean licenseEnabled;
	private final Translator translator;
	private final List<RepositoryEntry> testEntries;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private QuestionPoolLicenseHandler licenseHandler;
	
	public QuestionOriginMediaResource(String label, List<RepositoryEntry> testEntries, Translator translator) {
		super(label);
		CoreSpringFactory.autowireObject(this);
		this.translator = translator;
		this.testEntries = testEntries;
		licenseEnabled = licenseModule.isEnabled(licenseHandler);
	}
	
	@Override
	protected void generate(OutputStream out) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			generateHeaders(sheet);
			generateData(sheet);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	protected void generateHeaders(OpenXMLWorksheet sheet) {
		Row headerRow = sheet.newRow();
		int col = 0;
		
		// course
		headerRow.addCell(col++, translator.translate("report.course.id"));
		headerRow.addCell(col++, translator.translate("report.course.displayname"));
		headerRow.addCell(col++, translator.translate("report.course.externalref"));
		
		// test 
		headerRow.addCell(col++, translator.translate("report.test.id"));
		headerRow.addCell(col++, translator.translate("report.test.displayname"));
		headerRow.addCell(col++, translator.translate("report.test.externalref"));
		headerRow.addCell(col++, translator.translate("report.test.author"));
		
		// question
		headerRow.addCell(col++, translator.translate("report.question.title"));
		headerRow.addCell(col++, translator.translate("report.question.author"));
		headerRow.addCell(col++, translator.translate("report.question.identifier"));
		headerRow.addCell(col++, translator.translate("report.question.keywords"));
		headerRow.addCell(col++, translator.translate("report.question.taxonomy.level"));
		headerRow.addCell(col++, translator.translate("report.question.taxonomy.path"));
		headerRow.addCell(col++, translator.translate("report.question.topic"));
		headerRow.addCell(col++, translator.translate("report.question.context"));
		headerRow.addCell(col++, translator.translate("report.question.correction.time"));
		headerRow.addCell(col++, translator.translate("report.question.type"));
		if (licenseEnabled) {
			headerRow.addCell(col++, translator.translate("report.question.license"));
		}

		// master
		headerRow.addCell(col++, translator.translate("report.question.master.identifier"));
		headerRow.addCell(col++, translator.translate("report.question.master.author"));
		headerRow.addCell(col, translator.translate("report.question.master.keywords"));	
	}
	
	protected void generateData(OpenXMLWorksheet sheet) {
		List<CourseToTestHolder> courseToTestList = loadCoursesAndTests();
		dbInstance.commitAndCloseSession();
		Collections.sort(courseToTestList);
		
		for(CourseToTestHolder courseToTest:courseToTestList) {
			try {
				TestHolder testHolder = getTestHolder(courseToTest.getTestEntry());
				List<QuestionInformations> questionList = testHolder.getQuestionList();
				for(QuestionInformations question:questionList) {
					generateData(question, testHolder, courseToTest, sheet);
				}
			} catch (Exception e) {
				log.error("", e);
			} finally {
				dbInstance.commitAndCloseSession();
			}
		}	
	}
	
	protected void generateData(QuestionInformations question, TestHolder testHolder, CourseToTestHolder courseToTest, OpenXMLWorksheet sheet) {
		Row row = sheet.newRow();
		
		// course
		int col = 0;
		if(courseToTest.getCourseEntry() == null) {
			col += 3;
		} else {
			RepositoryEntry courseEntry = courseToTest.getCourseEntry();
			row.addCell(col++, courseEntry.getKey().toString());
			row.addCell(col++, courseEntry.getDisplayname());
			row.addCell(col++, courseEntry.getExternalRef());
		}
		
		// test
		RepositoryEntry testEntry = courseToTest.getTestEntry();
		row.addCell(col++, testEntry.getKey().toString());
		row.addCell(col++, testEntry.getDisplayname());
		row.addCell(col++, testEntry.getExternalRef());
		row.addCell(col++, testHolder.getOwners());
		
		// question
		row.addCell(col++, question.getTitle());
		row.addCell(col++, question.getAuthor());
		row.addCell(col++, question.getIdentifier());
		row.addCell(col++, question.getKeywords());
		row.addCell(col++, question.getTaxonomyLevel());
		row.addCell(col++, question.getTaxonomyPath());
		row.addCell(col++, question.getTopic());
		row.addCell(col++, question.getEducationalContextLevel());
		row.addCell(col++, question.getCorrectionTime());
		row.addCell(col++, question.getType());
		if(licenseEnabled) {
			row.addCell(col++, question.getLicense());
		}
		
		// master question
		row.addCell(col++, question.getMasterIdentifier());
		if(question.getMasterInformations() != null) {
			MasterInformations masterInfos = question.getMasterInformations();
			row.addCell(col++, masterInfos.getMasterAuthor());
			row.addCell(col, masterInfos.getMasterKeywords());
		}
	}

	private TestHolder getTestHolder(RepositoryEntry testEntry) {
		TestHolder testHolder = new TestHolder(testEntry);
		String owners = getOwners(testEntry);
		testHolder.setOwners(owners);
		
		List<QuestionInformations> questionList = new ArrayList<>(32);
		if(testHolder.isOk()) {
			AssessmentTest assessmentTest = testHolder.getAssessmentTest();
			List<TestPart> parts = assessmentTest.getChildAbstractParts();
			for(TestPart part:parts) {
				List<AssessmentSection> sections = part.getAssessmentSections();
				for(AssessmentSection section:sections) {
					loadQuestionMetadata(section, questionList, testHolder);
				}
			}
		}
		testHolder.setQuestionList(questionList);
		return testHolder;
	}
	
	private String getOwners(RepositoryEntry entry) {
		List<Identity> owners = repositoryService
				.getMembers(entry, RepositoryEntryRelationType.defaultGroup, GroupRoles.owner.name());
		return toString(owners);
	}
	
	private String getAuthors(QuestionItem item) {
		List<Identity> authors = qpoolService.getAuthors(item);
		return toString(authors);
	}
	
	private String toString(List<Identity> owners) {
		StringBuilder sb = new StringBuilder(32);
		if(owners != null && !owners.isEmpty()) {
			for(Identity owner:owners) {
				if(sb.length() > 0) sb.append(", ");
				
				String firstName = owner.getUser().getFirstName();
				boolean hasFirstName = StringHelper.containsNonWhitespace(firstName);
				if(hasFirstName) {
					sb.append(firstName);
				}
				
				String lastName = owner.getUser().getLastName();
				if(StringHelper.containsNonWhitespace(lastName)) {
					if(hasFirstName) sb.append(" ");
					sb.append(lastName);
				}
			}
		}
		return sb.toString();
	}
	
	private void loadQuestionMetadata(AssessmentSection section, List<QuestionInformations> questionList, TestHolder testHolder) {
		for(SectionPart part: section.getSectionParts()) {
			if(part instanceof AssessmentItemRef) {
				QuestionInformations infos = loadQuestionMetadata((AssessmentItemRef)part, testHolder);
				if(infos != null) {
					questionList.add(infos);
				}
			} else if(part instanceof AssessmentSection) {
				loadQuestionMetadata((AssessmentSection)part, questionList, testHolder);
			}
		}
	}

	private QuestionInformations loadQuestionMetadata(AssessmentItemRef itemRef, TestHolder testHolder) {
		QuestionInformations infos = null;
		AssessmentItem assessmentItem = testHolder.getAssessmentItem(itemRef);
		ManifestMetadataBuilder metadata = testHolder.getManifestMetadataBuilder(itemRef);
		if(metadata == null) {
			if(assessmentItem != null) {
				infos = new QuestionInformations(assessmentItem);
			}
		} else if(!StringHelper.containsNonWhitespace(metadata.getOpenOLATMetadataIdentifier())) {
			infos = new QuestionInformations(assessmentItem, metadata, testHolder.getOwners());
		} else {
			String identifier = metadata.getOpenOLATMetadataIdentifier();
			String masterIdentifier = metadata.getOpenOLATMetadataMasterIdentifier();
			
			List<QuestionItem> items = qpoolService.loadItemByIdentifier(identifier);
			if(items.isEmpty()) {
				infos = new QuestionInformations(assessmentItem, metadata, testHolder.getOwners());
			} else {
				QuestionItem item = items.get(0);
				String authors = getAuthors(item);
				String license = getLicense(item);
				Translator taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, translator.getLocale());
				String taxonomyLevelDisplayName = TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, item.getTaxonomyLevel());
				infos = new QuestionInformations(authors, item, license, taxonomyLevelDisplayName);
			}

			if(StringHelper.containsNonWhitespace(masterIdentifier)) {
				List<QuestionItem> masterItems = qpoolService.loadItemByIdentifier(masterIdentifier);
				if(!masterItems.isEmpty()) {
					QuestionItem masterItem = masterItems.get(0);
					String masterAuthors = getAuthors(masterItem);
					MasterInformations masterInfos = new MasterInformations(masterAuthors, masterItem);
					infos.setMasterInformations(masterInfos);
				}
			}
		}
		return infos;
	}
	
	private String getLicense(QuestionItem item) {
		License license = null;
		if(licenseEnabled) {
			license = licenseService.loadLicense(item);
		}
		
		String licenseText = null;
		if(license != null) {
			LicenseType licenseType = license.getLicenseType();
			if (licenseService.isFreetext(licenseType)) {
				licenseText = license.getFreetext();
			} else if (!licenseService.isNoLicense(licenseType)) {
				licenseText = license.getLicenseType().getName();
			}
			
			if(licenseText != null && licenseText.length() > 32000) {
				licenseText = licenseText.substring(0, 32000);
			}
		}
		return licenseText;
	}

	private List<CourseToTestHolder> loadCoursesAndTests() {
		List<CourseToTestHolder> courseToTestList = new ArrayList<>(128);
		for(RepositoryEntry testEntry:testEntries) {
			boolean hasOneReference = false;
			List<Reference> references = referenceManager.getReferencesTo(testEntry.getOlatResource());
			for(Reference reference:references) {
				OLATResource courseResource = reference.getSource();
				RepositoryEntry courseEntry = repositoryManager.lookupRepositoryEntry(courseResource, false);
				if(courseEntry != null) {
					courseToTestList.add(new CourseToTestHolder(courseEntry, testEntry));
					hasOneReference = true;
				}
			}
			
			if(!hasOneReference) {
				courseToTestList.add(new CourseToTestHolder(null, testEntry));
			}
		}
		return courseToTestList;
	}
	
	private class TestHolder {

		private final RepositoryEntry testEntry;

		private final AssessmentTest assessmentTest;
		private final ManifestBuilder manifestBuilder;
		private final ResolvedAssessmentTest resolvedAssessmentTest;
		
		private final File unzippedDirRoot;
		
		private String owners;
		private List<QuestionInformations> questionList;
		
		public TestHolder(RepositoryEntry testEntry) {
			this.testEntry = testEntry;
			
			FileResourceManager frm = FileResourceManager.getInstance();
			unzippedDirRoot = frm.unzipFileResource(testEntry.getOlatResource());
			manifestBuilder = ManifestBuilder.read(new File(unzippedDirRoot, "imsmanifest.xml"));
			resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTestNoCache(unzippedDirRoot);
			if(resolvedAssessmentTest != null) {
				assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
			} else {
				assessmentTest = null;
			}
		}
		
		public boolean isOk() {
			return testEntry != null && manifestBuilder != null && assessmentTest != null;
		}
		
		public String getOwners() {
			return owners;
		}

		public void setOwners(String owners) {
			this.owners = owners;
		}

		public List<QuestionInformations> getQuestionList() {
			return questionList;
		}

		public void setQuestionList(List<QuestionInformations> questionList) {
			this.questionList = questionList;
		}

		public AssessmentTest getAssessmentTest() {
			return assessmentTest;
		}
		
		public ManifestMetadataBuilder getManifestMetadataBuilder(AssessmentItemRef itemRef) {
			RootNodeLookup<AssessmentItem> rootNode = getItemLookup(itemRef);
			if(rootNode == null) return null;
			File itemFile = new File(rootNode.getSystemId());
			String relativePathToManifest = unzippedDirRoot.toPath().relativize(itemFile.toPath()).toString();
			ResourceType resource = manifestBuilder.getResourceTypeByHref(relativePathToManifest);
			return manifestBuilder.getMetadataBuilder(resource, true);
		}
		
		public AssessmentItem getAssessmentItem(AssessmentItemRef itemRef) {
			RootNodeLookup<AssessmentItem> rootNode = getItemLookup(itemRef);
			if(rootNode == null) return null;
			return rootNode.extractIfSuccessful();
		}
		
		public RootNodeLookup<AssessmentItem> getItemLookup(AssessmentItemRef itemRef) {
			ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
			if(resolvedAssessmentItem == null) return null;
			return resolvedAssessmentItem.getItemLookup();
		}
	}
	
	private static class QuestionInformations {
		
		private final String identifier;
		private final String title;
		private final String author;
		private final String keywords;
		private final String taxonomyPath;
		private final String taxonomyLevel;
		private final String correctionTime;
		private final String topic;
		private final String context;
		private final String type;
		private final String masterIdentifier;
		private final String license;

		private MasterInformations masterInformations;
		
		public QuestionInformations(String author, QuestionItem item, String license, String taxonomyLevelDisplayName) {
			identifier = item.getIdentifier();
			title = item.getTitle();
			this.author = author;
			keywords = item.getKeywords();
			taxonomyPath = item.getTaxonomicPath();
			taxonomyLevel = taxonomyLevelDisplayName;
			topic = item.getTopic();
			context = item.getEducationalContextLevel();
			type = item.getItemType();
			masterIdentifier = item.getMasterIdentifier();
			correctionTime = item.getCorrectionTime() == null ? "" : item.getCorrectionTime().toString();
			this.license = license;
		}
		
		public QuestionInformations(AssessmentItem assessmentItem, ManifestMetadataBuilder metadata, String testOwners) {
			identifier = metadata.getOpenOLATMetadataIdentifier();
			if(StringHelper.containsNonWhitespace(metadata.getTitle())) {
				title = metadata.getTitle();
			} else if (assessmentItem != null) {
				title = assessmentItem.getTitle();
			} else {
				title = "Unkown";
			}
			if(StringHelper.containsNonWhitespace(metadata.getOpenOLATMetadataCreator())) {
				author = metadata.getOpenOLATMetadataCreator();
			} else if(StringHelper.containsNonWhitespace(testOwners)) {
				author = testOwners;
			} else {
				author = null;
			}

			keywords = metadata.getGeneralKeywords();
			taxonomyPath = metadata.getClassificationTaxonomy();
			
			String level = null;
			if(StringHelper.containsNonWhitespace(taxonomyPath)) {
				int index = taxonomyPath.lastIndexOf('/');
				if(index >= 0) {
					level = taxonomyPath.substring(index + 1, taxonomyPath.length());
				} else {
					level = taxonomyPath;
				}
			}
			taxonomyLevel = level;
			
			topic = metadata.getOpenOLATMetadataTopic();
			context = metadata.getEducationContext();
			if(StringHelper.containsNonWhitespace(metadata.getOpenOLATMetadataQuestionType())) {
				type = metadata.getOpenOLATMetadataQuestionType();
			} else if(assessmentItem != null) {
				type = QTI21QuestionType.getType(assessmentItem).name();
			} else {
				type = QTI21QuestionType.unkown.name();
			}
			masterIdentifier = metadata.getOpenOLATMetadataMasterIdentifier();
			correctionTime = metadata.getOpenOLATMetadataCorrectionTime() == null ? "" :  metadata.getOpenOLATMetadataCorrectionTime().toString();
			
			String l = metadata.getLicense();
			if(StringHelper.containsNonWhitespace(l) && l.length() > 32000) {
				l = l.substring(0, 32000);
			}
			license = l;
		}
		
		public QuestionInformations(AssessmentItem assessmentItem) {
			identifier = null;
			title = assessmentItem.getTitle();
			author = null;
			keywords = null;
			taxonomyPath = null;
			taxonomyLevel = null;
			topic = assessmentItem.getLabel();
			context = null;
			masterIdentifier = null;
			license = null;

			QTI21QuestionType qType = QTI21QuestionType.getType(assessmentItem);
			if(qType == null) {
				type = null;
			} else {
				type = qType.name();
			}
			correctionTime = null;
		}

		public String getAuthor() {
			return author;
		}

		public String getIdentifier() {
			return identifier;
		}

		public String getTitle() {
			return title;
		}

		public String getKeywords() {
			return keywords;
		}
		
		public String getTaxonomyLevel() {
			return taxonomyLevel;
		}

		public String getTaxonomyPath() {
			return taxonomyPath;
		}

		public String getTopic() {
			return topic;
		}

		public String getEducationalContextLevel() {
			return context;
		}
		
		public String getCorrectionTime() {
			return correctionTime;
		}
		
		public String getLicense() {
			return license;
		}

		public String getType() {
			return type;
		}

		public String getMasterIdentifier() {
			return masterIdentifier;
		}

		public MasterInformations getMasterInformations() {
			return masterInformations;
		}

		public void setMasterInformations(MasterInformations masterInformations) {
			this.masterInformations = masterInformations;
		}
	}
	
	private static class MasterInformations {
		private final String masterAuthor;
		private final String masterKeywords;
		
		public MasterInformations(String masterAuthor, QuestionItem item) {
			this.masterAuthor = masterAuthor;
			this.masterKeywords = item.getKeywords();
		}
		
		public String getMasterAuthor() {
			return masterAuthor;
		}

		public String getMasterKeywords() {
			return masterKeywords;
		}
	}
	
	private static class CourseToTestHolder implements Comparable<CourseToTestHolder> {
		
		private final RepositoryEntry courseEntry;
		private final RepositoryEntry testEntry;

		public CourseToTestHolder(RepositoryEntry courseEntry, RepositoryEntry testEntry) {
			this.courseEntry = courseEntry;
			this.testEntry = testEntry;
		}

		public RepositoryEntry getCourseEntry() {
			return courseEntry;
		}

		public RepositoryEntry getTestEntry() {
			return testEntry;
		}
		
		@Override
		public int hashCode() {
			return (courseEntry == null ? 2671 : courseEntry.hashCode())
					+ (testEntry == null ? 9148 : testEntry.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof CourseToTestHolder) {
				CourseToTestHolder ctot = (CourseToTestHolder)obj;
				return ((courseEntry == null && ctot.getCourseEntry() == null) || (courseEntry != null && courseEntry.equals(ctot.getCourseEntry())))
						&& ((testEntry == null && ctot.getTestEntry() == null) || (testEntry != null && testEntry.equals(ctot.getTestEntry())));
			}
			return false;
		}

		@Override
		public int compareTo(CourseToTestHolder courseToTest) {
			int c = 0;
			if(courseEntry != null && courseToTest.getCourseEntry() != null) {
				c = courseEntry.getKey().compareTo(courseToTest.getCourseEntry().getKey());
			} else if(courseEntry != null && courseToTest.getCourseEntry() == null) {
				c = 1;
			} else if(courseEntry == null && courseToTest.getCourseEntry() != null) {
				c = -1;
			}
			
			if(c == 0) {
				c = testEntry.getKey().compareTo(courseToTest.getTestEntry().getKey());
			}
			return c;
		}
	}
}
