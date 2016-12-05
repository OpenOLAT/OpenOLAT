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
package org.olat.ims.qti21.pool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.imscp.xml.manifest.ResourceType;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.manager.QPoolFileStorage;

import uk.ac.ed.ph.jqtiplus.node.content.xhtml.image.Img;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;

/**
 * 
 * Initial date: 05.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ExportProcessor {
	
	private static final OLog log = Tracing.createLoggerFor(QTI21ExportProcessor.class);
	
	private final Locale locale;
	private final QTI21Service qtiService;
	private final QPoolFileStorage qpoolFileStorage;

	public QTI21ExportProcessor(QTI21Service qtiService, QPoolFileStorage qpoolFileStorage, Locale locale) {
		this.locale = locale;
		this.qtiService = qtiService;
		this.qpoolFileStorage = qpoolFileStorage;
	}

	public void process(QuestionItemFull qitem, ZipOutputStream zout, Set<String> names) {
		String dir = qitem.getDirectory();
		File rootDirectory = qpoolFileStorage.getDirectory(dir);

		String rootDir = "qitem_" + qitem.getKey();
		File[] items = rootDirectory.listFiles();
		File imsmanifest = new File(rootDirectory, "imsmanifest.xml");
		ManifestBuilder manifestBuilder;
		if(imsmanifest.exists()) {
			manifestBuilder = ManifestBuilder.read(imsmanifest);
		} else {
			manifestBuilder = new ManifestBuilder();
		}
		
		File resourceFile = new File(rootDirectory, qitem.getRootFilename());
		URI assessmentItemUri = resourceFile.toURI();
		
		ResolvedAssessmentItem resolvedAssessmentItem = qtiService
				.loadAndResolveAssessmentItem(assessmentItemUri, rootDirectory);
		enrichWithMetadata(qitem, resolvedAssessmentItem, manifestBuilder);
		
		try {
			zout.putNextEntry(new ZipEntry(rootDir + "/imsmanifest.xml"));
			manifestBuilder.write(new ShieldOutputStream(zout));
			zout.closeEntry();
		} catch (Exception e) {
			log.error("", e);
		}
		
		for(File item:items) {
			if(!"imsmanifest.xml".equals(item.getName())) {
				ZipUtil.addFileToZip(rootDir + "/" + item.getName(), item, zout);
			}
		}
	}
	
	public ResolvedAssessmentItem exportToQTIEditor(QuestionItemFull fullItem, File editorContainer)
	throws IOException {
		AssessmentItemsAndResources itemAndMaterials = new AssessmentItemsAndResources();
		collectMaterials(fullItem, itemAndMaterials);
		if(itemAndMaterials.getAssessmentItems().isEmpty()) {
			return null;//nothing found
		}
		
		ResolvedAssessmentItem assessmentItem = itemAndMaterials.getAssessmentItems().get(0);
		//write materials
		for(ItemMaterial material:itemAndMaterials.getMaterials()) {
			String exportPath = material.getExportUri();
			File originalFile = material.getFile();
			File exportFile = new File(editorContainer, exportPath);
			if(!exportFile.getParentFile().exists()) {
				exportFile.getParentFile().mkdirs();
			}
			FileUtils.bcopy(originalFile, exportFile, "Copy material QTI 2.1");
		}
		return assessmentItem;
	}
	
	protected void collectMaterials(QuestionItemFull fullItem, AssessmentItemsAndResources materials) {
		String dir = fullItem.getDirectory();
		String rootFilename = fullItem.getRootFilename();
		File resourceDirectory = qpoolFileStorage.getDirectory(dir);
		File itemFile = new File(resourceDirectory, rootFilename);

		if(itemFile.exists()) {
			ResolvedAssessmentItem assessmentItem = qtiService.loadAndResolveAssessmentItem(itemFile.toURI(), resourceDirectory);
			//enrichScore(itemEl);
			//enrichWithMetadata(fullItem, itemEl);
			collectResources(assessmentItem.getRootNodeLookup().extractIfSuccessful(), itemFile, materials);
			materials.addItemEl(assessmentItem);
		}
	}
	
	protected void collectResources(AssessmentItem item, File itemFile, AssessmentItemsAndResources materials) {
		File directory = itemFile.getParentFile();

		QueryUtils.search(Img.class, item).forEach((img) -> {
			if(img.getSrc() != null) {
				String imgPath = img.getSrc().toString();
				File imgFile = new File(directory, imgPath);
				if(imgFile.exists()) {
					materials.addMaterial(new ItemMaterial(imgFile, imgPath));
				}
			}
		});

		QueryUtils.search(Object.class, item).forEach((object) -> {
			if(StringHelper.containsNonWhitespace(object.getData())) {
				String path = object.getData();
				File objectFile = new File(directory, path);
				if(objectFile.exists()) {
					materials.addMaterial(new ItemMaterial(objectFile, path));
				}
			}
		});
	}
	
	public void enrichWithMetadata(QuestionItemFull qitem, ResolvedAssessmentItem resolvedAssessmentItem, ManifestBuilder manifestBuilder) {
		ResourceType resource = manifestBuilder.getResourceTypeByHref(qitem.getRootFilename());
		if(resource == null) {
			resource = manifestBuilder.appendAssessmentItem(qitem.getRootFilename());
		}
		ManifestMetadataBuilder metadataBuilder = manifestBuilder.getMetadataBuilder(resource, true);
		enrichWithMetadata(qitem, resolvedAssessmentItem, metadataBuilder);		
	}
	
	public void assembleTest(List<QuestionItemFull> fullItems, File directory) {
		try {
			QtiSerializer qtiSerializer = qtiService.qtiSerializer();
			//imsmanifest
			ManifestBuilder manifest = ManifestBuilder.createAssessmentTestBuilder();
			
			//assessment test
			AssessmentTest assessmentTest = AssessmentTestFactory.createAssessmentTest("Assessment test from pool");
			String assessmentTestFilename = assessmentTest.getIdentifier() + ".xml";
			manifest.appendAssessmentTest(assessmentTestFilename);

			//make a section
			AssessmentSection section = assessmentTest.getTestParts().get(0).getAssessmentSections().get(0);

			//assessment items
			for(QuestionItemFull qitem:fullItems) {
				String rootFilename = qitem.getRootFilename();
				File resourceDirectory = qpoolFileStorage.getDirectory(qitem.getDirectory());
				File itemFile = new File(resourceDirectory, qitem.getRootFilename());
				String itemFilename = itemFile.getName();
				ResolvedAssessmentItem resolvedAssessmentItem = qtiService.loadAndResolveAssessmentItem(itemFile.toURI(), resourceDirectory);

				//enrichScore(itemEl);
				//collectResources(itemEl, container, materials);
				FileUtils.bcopy(itemFile, new File(directory, rootFilename), "");
				AssessmentTestFactory.appendAssessmentItem(section, itemFilename);
				manifest.appendAssessmentItem(itemFilename);
				ManifestMetadataBuilder metadata = manifest.getResourceBuilderByHref(itemFilename);
				enrichWithMetadata(qitem, resolvedAssessmentItem, metadata);
			}

			try(FileOutputStream out = new FileOutputStream(new File(directory, assessmentTestFilename))) {
				qtiSerializer.serializeJqtiObject(assessmentTest, out);	
			} catch(Exception e) {
				log.error("", e);
			}

	        manifest.write(new File(directory, "imsmanifest.xml"));
		} catch (IOException | URISyntaxException e) {
			log.error("", e);
		}
	}
	
	public void assembleTest(List<QuestionItemFull> fullItems, ZipOutputStream zout) {
		try {
			QtiSerializer qtiSerializer = qtiService.qtiSerializer();
			//imsmanifest
			ManifestBuilder manifest = ManifestBuilder.createAssessmentTestBuilder();
			
			//assessment test
			AssessmentTest assessmentTest = AssessmentTestFactory.createAssessmentTest("Assessment test from pool");
			String assessmentTestFilename = assessmentTest.getIdentifier() + ".xml";
			manifest.appendAssessmentTest(assessmentTestFilename);

			//make a section
			AssessmentSection section = assessmentTest.getTestParts().get(0).getAssessmentSections().get(0);

			//assessment items
			for(QuestionItemFull qitem:fullItems) {
				File resourceDirectory = qpoolFileStorage.getDirectory(qitem.getDirectory());
				File itemFile =  new File(resourceDirectory, qitem.getRootFilename());
				String itemFilename = itemFile.getName();

				ResolvedAssessmentItem resolvedAssessmentItem = qtiService.loadAndResolveAssessmentItem(itemFile.toURI(), resourceDirectory);

				//enrichScore(itemEl);
				//collectResources(itemEl, container, materials);

				ZipUtil.addFileToZip(itemFilename, itemFile, zout);
				AssessmentTestFactory.appendAssessmentItem(section, itemFilename);
				manifest.appendAssessmentItem(itemFilename);
				ManifestMetadataBuilder metadata = manifest.getResourceBuilderByHref(itemFilename);
				enrichWithMetadata(qitem, resolvedAssessmentItem, metadata);
			}

			zout.putNextEntry(new ZipEntry(assessmentTestFilename));
			qtiSerializer.serializeJqtiObject(assessmentTest, new ShieldOutputStream(zout));
			zout.closeEntry();

			zout.putNextEntry(new ZipEntry("imsmanifest.xml"));
			manifest.write(new ShieldOutputStream(zout));
			zout.closeEntry();
		} catch (IOException | URISyntaxException e) {
			log.error("", e);
		}
	}

	private void enrichWithMetadata(QuestionItemFull qitem, ResolvedAssessmentItem resolvedAssessmentItem, ManifestMetadataBuilder metadata) {
		String lang = qitem.getLanguage();
		if(!StringHelper.containsNonWhitespace(lang)) {
			lang = locale.getLanguage();
		}
		
		//general
		if(StringHelper.containsNonWhitespace(qitem.getTitle())) {
			metadata.setTitle(qitem.getTitle(), lang);
		}
		if(StringHelper.containsNonWhitespace(qitem.getDescription())) {
			metadata.setDescription(qitem.getDescription(), lang);
		}
		if(StringHelper.containsNonWhitespace(qitem.getKeywords())) {
			//general and classification too
			metadata.setGeneralKeywords(qitem.getKeywords(), lang);
		}
		if(StringHelper.containsNonWhitespace(qitem.getCoverage())) {
			metadata.setCoverage(qitem.getCoverage(), lang);
		}
		
		//educational
		if(qitem.getEducationalContext() != null) {
			String level = qitem.getEducationalContext().getLevel();
			metadata.setEducationalContext(level, lang);
		}
		if(qitem.getEducationalLearningTime() != null) {
			String time = qitem.getEducationalLearningTime();
			metadata.setEducationalLearningTime(time);
		}
		if(qitem.getLanguage() != null) {
			String language = qitem.getLanguage();
			metadata.setLanguage(language, lang);
		}
		
		//classification
		if(qitem.getTaxonomicPath() != null) {
			metadata.setClassificationTaxonomy(qitem.getTaxonomicPath(), lang);
		}
		
		//life-cycle
		if(StringHelper.containsNonWhitespace(qitem.getItemVersion())) {
			metadata.setLifecycleVersion(qitem.getItemVersion());
		}

		// rights
		if(qitem.getLicense() != null && StringHelper.containsNonWhitespace(qitem.getLicense().getLicenseText())) {
			metadata.setLicense(qitem.getLicense().getLicenseText());
		}
		
		//qti metadata
		if(StringHelper.containsNonWhitespace(qitem.getEditor()) || StringHelper.containsNonWhitespace(qitem.getEditorVersion())) {
			metadata.setQtiMetadataTool(qitem.getEditor(), null, qitem.getEditorVersion());
		}
		
		if(resolvedAssessmentItem != null) {
			AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
			List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
			List<String> interactionNames = new ArrayList<>(interactions.size());
			for(Interaction interaction:interactions) {
				interactionNames.add(interaction.getQtiClassName());
			}
			metadata.setQtiMetadata(interactionNames);
		}
		
		//openolat metadata
		metadata.setOpenOLATMetadataQuestionType(qitem.getItemType());
		if(qitem.getAssessmentType() != null) {//summative, formative, both
			metadata.setOpenOLATMetadataAssessmentType(qitem.getAssessmentType());
		}
		if(qitem.getDifficulty() != null) {
			metadata.setOpenOLATMetadataMasterDifficulty(qitem.getDifficulty().doubleValue());
		}
		if(qitem.getDifferentiation() != null) {
			metadata.setOpenOLATMetadataMasterDiscriminationIndex(qitem.getDifferentiation().doubleValue());
		}
		if(qitem.getNumOfAnswerAlternatives() >= 0) {
			metadata.setOpenOLATMetadataMasterDistractors(qitem.getNumOfAnswerAlternatives());
		}
		if(qitem.getStdevDifficulty() != null) {
			metadata.setOpenOLATMetadataMasterStandardDeviation(qitem.getStdevDifficulty().doubleValue());
		}
		if(qitem.getUsage() >= 0) {
			metadata.setOpenOLATMetadataUsage(qitem.getUsage());
		}
		if(StringHelper.containsNonWhitespace(qitem.getMasterIdentifier())) {
			metadata.setOpenOLATMetadataMasterIdentifier(qitem.getMasterIdentifier());
		}
	}

	private static final class AssessmentItemsAndResources {
		private final Set<String> paths = new HashSet<String>();
		private final List<ResolvedAssessmentItem> itemEls = new ArrayList<>();
		private final List<ItemMaterial> materials = new ArrayList<>();
		
		public Set<String> getPaths() {
			return paths;
		}
		
		public List<ResolvedAssessmentItem> getAssessmentItems() {
			return itemEls;
		}
		
		public void addItemEl(ResolvedAssessmentItem el) {
			itemEls.add(el);
		}
		
		public List<ItemMaterial> getMaterials() {
			return materials;
		}
		
		public void addMaterial(ItemMaterial material) {
			materials.add(material);
		}
	}
	
	private static final class ItemMaterial {
		private final File file;
		private final String exportUri;
		
		public ItemMaterial(File file, String exportUri) {
			this.file = file;
			this.exportUri = exportUri;
		}
		
		public File getFile() {
			return file;
		}
		
		public String getExportUri() {
			return exportUri;
		}
	}
}
