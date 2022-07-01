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
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
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
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.imscp.xml.manifest.ResourceType;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;

/**
 * 
 * Initial date: 05.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ExportProcessor {
	
	private static final Logger log = Tracing.createLoggerFor(QTI21ExportProcessor.class);
	
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
		
		try(OutputStream out = new ShieldOutputStream(zout)) {
			zout.putNextEntry(new ZipEntry(rootDir + "/imsmanifest.xml"));
			manifestBuilder.write(out);
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
	
	public ResolvedAssessmentItem exportToQTIEditor(QuestionItemFull fullItem, File questionContainer)
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
			File exportFile = new File(questionContainer, exportPath);
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
			if(assessmentItem != null) {
				//enrichScore(itemEl);
				//enrichWithMetadata(fullItem, itemEl);
				ImportExportHelper.getMaterials(assessmentItem, itemFile, materials);
				materials.addItemEl(resolvedAssessmentItem);
			}
		}
	}
	

	
	public void enrichWithMetadata(QuestionItemFull qitem, ResolvedAssessmentItem resolvedAssessmentItem, ManifestBuilder manifestBuilder) {
		ResourceType resource = manifestBuilder.getResourceTypeByHref(qitem.getRootFilename());
		if(resource == null) {
			resource = manifestBuilder.appendAssessmentItem(qitem.getRootFilename());
		}
		ManifestMetadataBuilder metadataBuilder = manifestBuilder.getMetadataBuilder(resource, true);
		metadataBuilder.appendMetadataFrom(qitem, resolvedAssessmentItem, locale);	
	}
	
	public void assembleTest(String title, List<QuestionItemFull> fullItems, boolean groupByTaxonomyLevel, File directory) {
		try {
			QtiSerializer qtiSerializer = qtiService.qtiSerializer();
			//imsmanifest
			ManifestBuilder manifest = ManifestBuilder.createAssessmentTestBuilder();
			
			//assessment test
			DoubleAdder atomicMaxScore = new DoubleAdder();
			if(!StringHelper.containsNonWhitespace(title)) {
				title = "Assessment test from pool";
			}
			
			Translator translator = Util.createPackageTranslator(AssessmentTestComposerController.class, locale);
			String sectionTitle = translator.translate("new.section");
			
			AssessmentTest assessmentTest = AssessmentTestFactory.createAssessmentTest(title, sectionTitle);
			String assessmentTestFilename = assessmentTest.getIdentifier() + ".xml";
			manifest.appendAssessmentTest(assessmentTestFilename);

			//make a section
			final TestPart testPart = assessmentTest.getTestParts().get(0);
			AssessmentSection defaultSection = testPart.getAssessmentSections().get(0);
			Map<String,AssessmentSection> sectionByTitles = new HashMap<>();
			
			Translator taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, translator.getLocale());

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
				
				String taxonomyLevelDisplayName = TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, qitem.getTaxonomyLevel());
				AssessmentSection section = defaultSection;
				if(groupByTaxonomyLevel && StringHelper.containsNonWhitespace(taxonomyLevelDisplayName)) {
					section = sectionByTitles.computeIfAbsent(taxonomyLevelDisplayName, level
							-> AssessmentTestFactory.appendAssessmentSection(level, testPart));
				}

				AssessmentTestFactory.appendAssessmentItem(section, newItemFilename);
				manifest.appendAssessmentItem(newItemFilename);
				ManifestMetadataBuilder metadata = manifest.getResourceBuilderByHref(newItemFilename);
				metadata.appendMetadataFrom(qitem, resolvedAssessmentItem, locale);
				
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
					File exportFile = new File(containerDir, exportPath);
					if(!exportFile.getParentFile().exists()) {
						exportFile.getParentFile().mkdirs();
					}
					FileUtils.bcopy(originalFile, exportFile, "Copy material QTI 2.1");
				}
			}
			
			if(defaultSection.getSectionParts().isEmpty()) {
				testPart.getChildAbstractParts().remove(defaultSection);
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
				metadata.appendMetadataFrom(qitem, resolvedAssessmentItem, locale);
				
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
			serializeAssessmentTest(assessmentTest, zout);
			zout.closeEntry();

			zout.putNextEntry(new ZipEntry("imsmanifest.xml"));
			writeManifest(manifest, zout);
			zout.closeEntry();
		} catch (IOException | URISyntaxException e) {
			log.error("", e);
		}
	}
	
	private void writeManifest(ManifestBuilder manifest, ZipOutputStream zout) {
		try(OutputStream out = new ShieldOutputStream(zout)) {
			manifest.write(out);
		} catch(IOException e) {
			log.error("Cannot write manifest", e);
		}
	}
	
	private void serializeAssessmentTest(AssessmentTest assessmentTest, ZipOutputStream zout) {
		try(OutputStream out = new ShieldOutputStream(zout)) {
			QtiSerializer qtiSerializer = qtiService.qtiSerializer();
			qtiSerializer.serializeJqtiObject(assessmentTest, out);
		} catch(IOException e) {
			log.error("Cannot write manifest", e);
		}
	}
}
