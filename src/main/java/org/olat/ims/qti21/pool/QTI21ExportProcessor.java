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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentTestBuilder;
import org.olat.ims.qti21.model.xml.AssessmentTestFactory;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.pool.ImportExportHelper.AssessmentItemsAndResources;
import org.olat.ims.qti21.pool.ImportExportHelper.ItemMaterial;
import org.olat.imscp.xml.manifest.ResourceType;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.manager.QPoolFileStorage;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;

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

	public void process(QuestionItemFull qitem, ZipOutputStream zout) {
		String dir = qitem.getDirectory();
		File rootDirectory = qpoolFileStorage.getDirectory(dir);

		String rootDir = "qitem_" + qitem.getKey();
		
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
				.loadAndResolveAssessmentItemForCopy(assessmentItemUri, rootDirectory);
		enrichWithMetadata(qitem, resolvedAssessmentItem, manifestBuilder);
		
		try {
			zout.putNextEntry(new ZipEntry(rootDir + "/imsmanifest.xml"));
			manifestBuilder.write(new ShieldOutputStream(zout));
			zout.closeEntry();
		} catch (Exception e) {
			log.error("", e);
		}

		try {
			Files.walkFileTree(rootDirectory.toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String filename = file.getFileName().toString();
					if(!"imsmanifest.xml".equals(filename) && !filename.startsWith(".")) {
						String relPath = rootDirectory.toPath().relativize(file).toString();
						ZipUtil.addFileToZip(rootDir + "/" + relPath, file, zout);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			log.error("", e);
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
			ResolvedAssessmentItem resolvedAssessmentItem = qtiService.loadAndResolveAssessmentItemForCopy(itemFile.toURI(), resourceDirectory);
			AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
			//enrichScore(itemEl);
			//enrichWithMetadata(fullItem, itemEl);
			ImportExportHelper.getMaterials(assessmentItem, itemFile, materials);
			materials.addItemEl(resolvedAssessmentItem);
		}
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
			DoubleAdder atomicMaxScore = new DoubleAdder();
			AssessmentTest assessmentTest = AssessmentTestFactory.createAssessmentTest("Assessment test from pool", "Section");
			String assessmentTestFilename = assessmentTest.getIdentifier() + ".xml";
			manifest.appendAssessmentTest(assessmentTestFilename);

			//make a section
			AssessmentSection section = assessmentTest.getTestParts().get(0).getAssessmentSections().get(0);

			//assessment items
			for(QuestionItemFull qitem:fullItems) {
				File resourceDirectory = qpoolFileStorage.getDirectory(qitem.getDirectory());
				File itemFile = new File(resourceDirectory, qitem.getRootFilename());
				ResolvedAssessmentItem resolvedAssessmentItem = qtiService.loadAndResolveAssessmentItemForCopy(itemFile.toURI(), resourceDirectory);
				AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
				assessmentItem.setIdentifier(QTI21QuestionType.generateNewIdentifier(assessmentItem.getIdentifier()));
				
				//save the item in its own container
				String container = qitem.getKey().toString();
				File containerDir = new File(directory, container);
				containerDir.mkdirs();
				File newItemFile = new File(containerDir, assessmentItem.getIdentifier() + ".xml");
				String newItemFilename = container  + "/" + newItemFile.getName();
				qtiService.persistAssessmentObject(newItemFile, assessmentItem);

				AssessmentTestFactory.appendAssessmentItem(section, newItemFilename);
				manifest.appendAssessmentItem(newItemFilename);
				ManifestMetadataBuilder metadata = manifest.getResourceBuilderByHref(newItemFilename);
				enrichWithMetadata(qitem, resolvedAssessmentItem, metadata);
				
				Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
				if(maxScore != null) {
					atomicMaxScore.add(maxScore.doubleValue());
				}
				
				//write materials
				AssessmentItemsAndResources materials = new AssessmentItemsAndResources();
				ImportExportHelper.getMaterials(assessmentItem, itemFile, materials);
				for(ItemMaterial material:materials.getMaterials()) {
					String exportPath = material.getExportUri();
					File originalFile = material.getFile();
					File exportFile = new File(container, exportPath);
					if(!exportFile.getParentFile().exists()) {
						exportFile.getParentFile().mkdirs();
					}
					FileUtils.bcopy(originalFile, exportFile, "Copy material QTI 2.1");
				}
			}
			
			AssessmentTestBuilder assessmentTestBuilder = new AssessmentTestBuilder(assessmentTest);
			double sumMaxScore = atomicMaxScore.sum();
			if(sumMaxScore > 0.0d) {
				assessmentTestBuilder.setMaxScore(sumMaxScore);
			}
			assessmentTest = assessmentTestBuilder.build();

			try(FileOutputStream out = new FileOutputStream(new File(directory, assessmentTestFilename))) {
				qtiSerializer.serializeJqtiObject(assessmentTest, out);	
			} catch(Exception e) {
				log.error("", e);
			}

	        manifest.write(new File(directory, "imsmanifest.xml"));
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	public void assembleTest(List<QuestionItemFull> fullItems, ZipOutputStream zout) {
		try {
			QtiSerializer qtiSerializer = qtiService.qtiSerializer();
			//imsmanifest
			ManifestBuilder manifest = ManifestBuilder.createAssessmentTestBuilder();
			
			//assessment test
			AssessmentTest assessmentTest = AssessmentTestFactory.createAssessmentTest("Assessment test from pool", "Section");
			String assessmentTestFilename = assessmentTest.getIdentifier() + ".xml";
			manifest.appendAssessmentTest(assessmentTestFilename);

			//make a section
			AssessmentSection section = assessmentTest.getTestParts().get(0).getAssessmentSections().get(0);

			//assessment items
			for(QuestionItemFull qitem:fullItems) {
				File resourceDirectory = qpoolFileStorage.getDirectory(qitem.getDirectory());
				File itemFile =  new File(resourceDirectory, qitem.getRootFilename());
				String itemFilename = itemFile.getName();
				String container = qitem.getKey().toString();
				String containedFilename = container + "/" + itemFilename;

				ResolvedAssessmentItem resolvedAssessmentItem = qtiService.loadAndResolveAssessmentItemForCopy(itemFile.toURI(), resourceDirectory);
	
				ZipUtil.addFileToZip(containedFilename, itemFile, zout);
				AssessmentTestFactory.appendAssessmentItem(section, containedFilename);
				manifest.appendAssessmentItem(containedFilename);
				ManifestMetadataBuilder metadata = manifest.getResourceBuilderByHref(containedFilename);
				enrichWithMetadata(qitem, resolvedAssessmentItem, metadata);
				
				//write materials
				try {
					Files.walkFileTree(resourceDirectory.toPath(), new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
							String filename = file.getFileName().toString();
							if(!"imsmanifest.xml".equals(filename) && !filename.startsWith(".") && !itemFilename.equals(filename)) {
								String relPath = resourceDirectory.toPath().relativize(file).toString();
								ZipUtil.addFileToZip(container + "/" + relPath, file, zout);
							}
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					log.error("", e);
				}
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
}
