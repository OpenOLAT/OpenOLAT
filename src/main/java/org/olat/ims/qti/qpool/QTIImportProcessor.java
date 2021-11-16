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
package org.olat.ims.qti.qpool;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.PathUtils;
import org.olat.core.util.PathUtils.CopyVisitor;
import org.olat.core.util.PathUtils.YesMatcher;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.qti.questionimport.ItemAndMetadata;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.InputSource;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * This class is NOT thread-safe
 * 
 * Initial date: 07.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class QTIImportProcessor {
	
	private static final Logger log = Tracing.createLoggerFor(QTIImportProcessor.class);
	
	
	private final Identity owner;
	private final Locale defaultLocale;
	private final String importedFilename;
	private final File importedFile;

	@Autowired
	private DB dbInstance;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QPoolFileStorage qpoolFileStorage;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QEducationalContextDAO qEduContextDao;
	
	public QTIImportProcessor(Identity owner, Locale defaultLocale) {
		this(owner, defaultLocale, null, null);
	}

	public QTIImportProcessor(Identity owner, Locale defaultLocale, String importedFilename, File importedFile) {
		this.owner = owner;
		this.defaultLocale = defaultLocale;
		this.importedFilename = importedFilename;
		this.importedFile = importedFile;

		CoreSpringFactory.autowireObject(this);
	}
	
	public List<QuestionItem> process() {
		List<QuestionItem> qItems = new ArrayList<>();
		try {
			List<DocInfos> docInfoList = getDocInfos();
			if(docInfoList != null) {
				for(DocInfos docInfos:docInfoList) {
					List<QuestionItem> processdItems = process(docInfos);
					qItems.addAll(processdItems);
					dbInstance.commit();
				}

				for(DocInfos docInfos:docInfoList) {
					FileUtils.closeSafely(docInfos);
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
		return qItems;
	}
	
	private List<QuestionItem> process(DocInfos docInfos) {
		List<QuestionItem> qItems = new ArrayList<>();
		if(docInfos.doc != null) {
			List<ItemInfos> itemInfos = getItemList(docInfos);
			for(ItemInfos itemInfo:itemInfos) {
				QuestionItemImpl qItem = processItem(docInfos, itemInfo, null);
				if(qItem != null) {
					processFiles(qItem, itemInfo, docInfos);
					qItem = questionItemDao.merge(qItem);
					qItems.add(qItem);
				}
			}
		}
		return qItems;
	}

	protected List<ItemInfos> getItemList(DocInfos doc) {
		Document document = doc.getDocument();
		List<ItemInfos> itemElements = new ArrayList<>();
		Element item = (Element)document.selectSingleNode("/questestinterop/item");
		Element assessment = (Element)document.selectSingleNode("/questestinterop/assessment");
		if(item != null) {
			ItemInfos itemInfos = new ItemInfos(item, true);
			Element comment = (Element)document.selectSingleNode("/questestinterop/qticomment");
			String qtiComment = getText(comment);
			itemInfos.setComment(qtiComment);
			itemElements.add(itemInfos);
		} else if(assessment != null) {
			List<Node> items = assessment.selectNodes("//item");
			for(Node it:items) {
				itemElements.add(new ItemInfos((Element)it, false));
			}
		}
		return itemElements;
	}
	
	protected QuestionItemImpl processItem(DocInfos docInfos, ItemInfos itemInfos, ItemAndMetadata metadata) {
		Element itemEl = itemInfos.getItemEl();
		String comment = itemInfos.getComment();
		String originalFilename = null;
		if(itemInfos.isOriginalItem()) {
			originalFilename = docInfos.filename;
		}
		return processItem(itemEl, comment, originalFilename, null, null, docInfos, metadata);
	}
	
	protected QuestionItemImpl processItem(Element itemEl, String comment, String originalItemFilename,
			String editor, String editorVersion, DocInfos docInfos, ItemAndMetadata metadata) {
		//filename
		String filename;
		String ident = getAttributeValue(itemEl, "ident");
		if(originalItemFilename != null) {
			filename = originalItemFilename;
		} else if(StringHelper.containsNonWhitespace(ident)) {
			filename = StringHelper.transformDisplayNameToFileSystemName(ident) + ".xml";
		} else {
			filename = "item.xml";
		}
		String dir = qpoolFileStorage.generateDir();
		
		//title
		String title = getAttributeValue(itemEl, "title");
		if(!StringHelper.containsNonWhitespace(title)) {
			title = ident;
		}
		if(!StringHelper.containsNonWhitespace(title)) {
			title = importedFilename;
		}

		QuestionItemImpl poolItem = questionItemDao.create(title, QTIConstants.QTI_12_FORMAT, dir, filename);
		//description
		poolItem.setDescription(comment);
		//language from default
		poolItem.setLanguage(defaultLocale.getLanguage());
		//question type first
		boolean ooFormat = processItemQuestionType(poolItem, ident, itemEl);
		if(StringHelper.containsNonWhitespace(editor)) {
			poolItem.setEditor(editor);
			poolItem.setEditorVersion(editorVersion);
		} else if(ooFormat) {
			poolItem.setEditor("OpenOLAT");
		}
		//if question type not found, can be overridden by the metadatas
		processItemMetadata(poolItem, itemEl);
		if(poolItem.getType() == null) {
			QItemType defType = qItemTypeDao.loadByType(QuestionType.UNKOWN.name());
			poolItem.setType(defType);
		}
		if(docInfos != null) {
			processSidecarMetadata(poolItem, docInfos);
		}
		if(metadata != null) {
			processItemMetadata(poolItem, metadata);
		}
		questionItemDao.persist(owner, poolItem);
		if(metadata != null) {
			createLicense(poolItem, metadata);
		}
		return poolItem;
	}
	
	private void processItemMetadata(QuestionItemImpl poolItem, ItemAndMetadata metadata) {
		//non heuristic set of question type
		int questionType = metadata.getQuestionType();
		if(questionType >= 0) {
			String typeStr;
			switch(questionType) {
				case Question.TYPE_MC: typeStr = QuestionType.MC.name(); break;
				case Question.TYPE_SC: typeStr = QuestionType.SC.name(); break;
				case Question.TYPE_FIB: typeStr = QuestionType.FIB.name(); break;
				case Question.TYPE_ESSAY: typeStr = QuestionType.ESSAY.name(); break;
				default: typeStr = null;
			}
			if(typeStr != null) {
				QItemType type = qItemTypeDao.loadByType(typeStr);
				if(type != null) {
					poolItem.setType(type);
				}
			}
		}
		
		String coverage = metadata.getCoverage();
		if(StringHelper.containsNonWhitespace(coverage)) {
			poolItem.setCoverage(coverage);
		}
		
		String language = metadata.getLanguage();
		if(StringHelper.containsNonWhitespace(language)) {
			poolItem.setLanguage(language);
		}
		
		String keywords = metadata.getKeywords();
		if(StringHelper.containsNonWhitespace(keywords)) {
			poolItem.setKeywords(keywords);
		}
		
		String taxonomyPath = metadata.getTaxonomyPath();
		if(StringHelper.containsNonWhitespace(taxonomyPath)) {
			QTIMetadataConverter converter = new QTIMetadataConverter(qItemTypeDao, qEduContextDao, qpoolService);
			TaxonomyLevel taxonomyLevel = converter.toTaxonomy(taxonomyPath);
			poolItem.setTaxonomyLevel(taxonomyLevel);
		}
		
		String level = metadata.getLevel();
		if(StringHelper.containsNonWhitespace(level)) {
			QTIMetadataConverter converter = new QTIMetadataConverter(qItemTypeDao, qEduContextDao, qpoolService);
			QEducationalContext educationalContext = converter.toEducationalContext(level);
			poolItem.setEducationalContext(educationalContext);
		}
		
		String time = metadata.getTypicalLearningTime();
		if(StringHelper.containsNonWhitespace(time)) {
			poolItem.setEducationalLearningTime(time);
		}
		
		String editor = metadata.getEditor();
		if(StringHelper.containsNonWhitespace(editor)) {
			poolItem.setEditor(editor);
		}
		
		String editorVersion = metadata.getEditorVersion();
		if(StringHelper.containsNonWhitespace(editorVersion)) {
			poolItem.setEditorVersion(editorVersion);
		}
		
		int numOfAnswerAlternatives = metadata.getNumOfAnswerAlternatives();
		if(numOfAnswerAlternatives > 0) {
			poolItem.setNumOfAnswerAlternatives(numOfAnswerAlternatives);
		}
		
		poolItem.setDifficulty(metadata.getDifficulty());
		poolItem.setDifferentiation(metadata.getDifferentiation());
		poolItem.setStdevDifficulty(metadata.getStdevDifficulty());
	}

	private void createLicense(QuestionItemImpl poolItem, ItemAndMetadata metadata) {
		String license = metadata.getLicense();
		QTIMetadataConverter converter = new QTIMetadataConverter(qItemTypeDao, qEduContextDao, qpoolService);
		converter.createLicense(poolItem, license);
	}
	
	private void processItemMetadata(QuestionItemImpl poolItem, Element itemEl) {
		List<Node> qtiMetadataFieldList = itemEl.selectNodes("./itemmetadata/qtimetadata/qtimetadatafield");
		for(Node qtiMetadataField:qtiMetadataFieldList) {
			Element labelEl = (Element)qtiMetadataField.selectSingleNode("./fieldlabel");
			Element entryEl = (Element)qtiMetadataField.selectSingleNode("./fieldentry");
			if(labelEl != null && entryEl != null) {
				processMetadataField(poolItem, labelEl, entryEl);
			}
		}
	}
	
	/**
	 * <ul>
	 *  <li>qmd_computerscored</li>
	 *  <li>qmd_feedbackpermitted</li>
	 *  <li>qmd_hintspermitted</li>
	 *  <li>qmd_itemtype -> (check is made on the content of the item)</li>
	 *  <li>qmd_levelofdifficulty -> educational context</li>
	 *  <li>qmd_maximumscore</li>
	 *  <li>qmd_renderingtype</li>
	 *  <li>qmd_responsetype</li>
	 *  <li>qmd_scoringpermitted</li>
	 *  <li>qmd_solutionspermitted</li>
	 *  <li>qmd_status</li>
	 *  <li>qmd_timedependence</li>
	 *  <li>qmd_timelimit</li>
	 *  <li>qmd_toolvendor -> editor</li>
	 *  <li>qmd_topic</li>
	 *  <li>qmd_material</li>
	 *  <li>qmd_typeofsolution</li>
	 *  <li>qmd_weighting</li>
	 * </ul> 
	 * @param poolItem
	 * @param labelEl
	 * @param entryEl
	 */
	private void processMetadataField(QuestionItemImpl poolItem, Element labelEl, Element entryEl) {
		String label = labelEl.getText();
		String entry = entryEl.getText();
		
		if(QTIConstants.META_LEVEL_OF_DIFFICULTY.equals(label)) {
			if(StringHelper.containsNonWhitespace(entry)) {
				QEducationalContext context = qEduContextDao.loadByLevel(entry);
				if(context == null) {
					context = qEduContextDao.create(entry, true);
				}
				poolItem.setEducationalContext(context);
			}
		} else if(QTIConstants.META_ITEM_TYPE.equals(label)) {
			if(poolItem.getType() == null &&  StringHelper.containsNonWhitespace(entry)) {
				//some heuristic
				String typeStr = entry;
				if(typeStr.equalsIgnoreCase("MCQ") || typeStr.equalsIgnoreCase("Multiple choice")) {
					typeStr = QuestionType.MC.name();
				} else if(typeStr.equalsIgnoreCase("SCQ") || typeStr.equalsIgnoreCase("Single choice")) {
					typeStr = QuestionType.SC.name();
				} else if(typeStr.equalsIgnoreCase("fill-in") || typeStr.equals("Fill-in-the-Blank")
						|| typeStr.equalsIgnoreCase("Fill-in-Blank") || typeStr.equalsIgnoreCase("Fill In the Blank")) {
					typeStr = QuestionType.FIB.name();
				} else if(typeStr.equalsIgnoreCase("Essay")) {
					typeStr = QuestionType.ESSAY.name();
				}
				
				QItemType type = qItemTypeDao.loadByType(entry);
				if(type == null) {
					type = qItemTypeDao.create(entry, true);
				}
				poolItem.setType(type);
			}
		} else if(QTIConstants.META_TOOLVENDOR.equals(label)) {
			poolItem.setEditor(entry);
		}
	}
	
	/**
	 * Save the item element in a <questestinterop> cartridge if needed
	 * @param item
	 * @param itemEl
	 */
	protected void processFiles(QuestionItemImpl item, ItemInfos itemInfos, DocInfos docInfos) {
		if(itemInfos.originalItem) {
			processItemFiles(item, docInfos);
		} else {
			//an assessment package
			processAssessmentFiles(item, itemInfos);
		}
	}
	
	protected void processAssessmentFiles(QuestionItemImpl item, ItemInfos itemInfos) {
		//a package with an item
		String dir = item.getDirectory();
		String rootFilename = item.getRootFilename();
		VFSContainer container = qpoolFileStorage.getContainer(dir);
		VFSLeaf endFile = container.createChildLeaf(rootFilename);
		
		//embed in <questestinterop>
		DocumentFactory df = DocumentFactory.getInstance();
		Document itemDoc = df.createDocument();
		Element questestinteropEl = df.createElement(QTIDocument.DOCUMENT_ROOT);
		itemDoc.setRootElement(questestinteropEl);
		Element deepClone = (Element)itemInfos.getItemEl().clone();
		questestinteropEl.add(deepClone);
		
		//write
		try {
			OutputStream os = endFile.getOutputStream(false);
			XMLWriter xw = new XMLWriter(os, new OutputFormat("  ", true));
			xw.write(itemDoc.getRootElement());
			xw.close();
			os.close();
		} catch (IOException e) {
			log.error("", e);
		}
		
		//there perhaps some other materials
		if(importedFilename.toLowerCase().endsWith(".zip")) {
			processAssessmentMaterials(deepClone, container);
		}
	}
	
	private void processAssessmentMaterials(Element itemEl, VFSContainer container) {
		List<String> materials = getMaterials(itemEl);

		try(InputStream in = new FileInputStream(importedFile);
				ZipInputStream zis = new ZipInputStream(in)) {

			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();
				if(materials.contains(name)) {
					unzipMaterial(zis, container, name);
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	private void unzipMaterial(ZipInputStream zis, VFSContainer container, String name) {
		VFSLeaf leaf = container.createChildLeaf(name);
		try(OutputStream out = leaf.getOutputStream(false);
				OutputStream bos = new BufferedOutputStream(out);) {
			FileUtils.cpio(zis, bos, "unzip:" + name);
			bos.flush();
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	protected List<String> getMaterials(Element el) {
		List<String> materialPath = new ArrayList<>();
		//mattext
		List<Node> mattextList = el.selectNodes(".//mattext");
		for(Node matNode:mattextList) {
			Element mat = (Element)matNode;
			Attribute texttypeAttr = mat.attribute("texttype");
			if(texttypeAttr != null) {
				String texttype = texttypeAttr.getValue();
				if("text/html".equals(texttype)) {
					String content = mat.getStringValue();
					findMaterialInMatText(content, materialPath);
				}
			}
		}
		//matimage uri
		List<Node> matList = new ArrayList<>();
		matList.addAll(el.selectNodes(".//matimage"));
		matList.addAll(el.selectNodes(".//mataudio"));
		matList.addAll(el.selectNodes(".//matvideo"));
		
		for(Node matNode:matList) {
			Element mat = (Element)matNode;
			Attribute uriAttr = mat.attribute("uri");
			String uri = uriAttr.getValue();
			materialPath.add(uri);
		}
		return materialPath;
	}
	
	/**
	 * Parse the content and collect the images source
	 * @param content
	 * @param materialPath
	 */
	protected void findMaterialInMatText(String content, List<String> materialPath) {
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			QTI12HtmlHandler contentHandler = new QTI12HtmlHandler(materialPath);
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(content)));
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * Process the file of an item's package
	 * @param item
	 * @param itemInfos
	 */
	protected void processItemFiles(QuestionItemImpl item, DocInfos docInfos) {
	//a package with an item
		String dir = item.getDirectory();
		String rootFilename = item.getRootFilename();
		VFSContainer container = qpoolFileStorage.getContainer(dir);
		
		if(docInfos != null && docInfos.getRoot() != null) {
			try {
				Path destDir = ((LocalImpl)container).getBasefile().toPath();
				//unzip to container
				Path path = docInfos.getRoot();
				Files.walkFileTree(path, new CopyVisitor(path, destDir, new YesMatcher()));
			} catch (IOException e) {
				log.error("", e);
			}
		} else if(importedFilename.toLowerCase().endsWith(".zip")) {
			ZipUtil.unzipStrict(importedFile, container);
		} else {
			VFSLeaf endFile = container.createChildLeaf(rootFilename);
			try(OutputStream out = endFile.getOutputStream(false);
					FileInputStream in = new FileInputStream(importedFile)) {
				IOUtils.copy(in, out);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}
	
	private boolean processSidecarMetadata(QuestionItemImpl item, DocInfos docInfos) {
		try {
			Path path = docInfos.root;
			if(path != null && path.getFileName() != null) {
				Path metadata = path.resolve(path.getFileName().toString() + "_metadata.xml");
				Document document = readSidecarMetadata(metadata);
				if(document != null) {
			        Element rootElement = document.getRootElement();
			        QTIMetadataConverter enricher = new QTIMetadataConverter(rootElement, qItemTypeDao, qEduContextDao, qpoolService);
			        enricher.toQuestion(item);
				}
			}
	        return true;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	private Document readSidecarMetadata(Path metadata) {
		try(InputStream metadataIn = Files.newInputStream(metadata)) {
			SAXReader reader = SAXReader.createDefault();
	        return reader.read(metadataIn);
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private boolean processItemQuestionType(QuestionItemImpl poolItem, String ident, Element itemEl) {
		boolean openolatFormat = false;
		
		//question type: mc, sc...
		QuestionType type = null;
		//test with openolat ident 
		if (ident != null && ident.startsWith(ItemParser.ITEM_PREFIX_SCQ)) {
			type = QuestionType.SC;
			openolatFormat = true;
		} else if(ident != null && ident.startsWith(ItemParser.ITEM_PREFIX_MCQ)) {
			type = QuestionType.MC;
			openolatFormat = true;
		} else if(ident != null && ident.startsWith(ItemParser.ITEM_PREFIX_FIB)) {
			type = QuestionType.FIB;
			openolatFormat = true;
		} else if(ident != null && ident.startsWith(ItemParser.ITEM_PREFIX_ESSAY)) {
			type = QuestionType.ESSAY;
			openolatFormat = true;
		} else if(ident != null && ident.startsWith(ItemParser.ITEM_PREFIX_KPRIM)) {
			type = QuestionType.KPRIM;	
			openolatFormat = true;
		} else if(itemEl.selectNodes("//render_choice").size() == 1) {
			Element lidEl = (Element)itemEl.selectSingleNode("//response_lid");
			String rcardinality = getAttributeValue(lidEl, "rcardinality");
			if("Single".equals(rcardinality)) {
				type = QuestionType.SC;
			} else if("Multiple".equals(rcardinality)) {
				type = QuestionType.MC;
			}
		} else if(itemEl.selectNodes("//render_fib").size() == 1) {
			type = QuestionType.FIB;
		}
		if(type != null) {
			QItemType itemType = qItemTypeDao.loadByType(type.name());
			poolItem.setType(itemType);
		}
		
		return openolatFormat;
	}
	
	private String getAttributeValue(Element el, String attrName) {
		if(el == null) return null;
		Attribute attr = el.attribute(attrName);
		return (attr == null) ? null : attr.getStringValue();
	}
	
	private String getText(Element el) {
		if(el == null) return null;
		return el.getText();
	}
	
	protected List<DocInfos> getDocInfos() throws IOException {
		List<DocInfos> doc;
		if(importedFilename.toLowerCase().endsWith(".zip")) {
			doc = traverseZip_nio(importedFile);
		} else {
			doc = Collections.singletonList(traverseFile(importedFile));
		}
		return doc;
	}
	
	private DocInfos traverseFile(File file) {
		try {
			Document doc = readXml(file.toPath());
			if(doc != null) {
				DocInfos d = new DocInfos();
				d.doc = doc;
				d.filename = file.getName();
				return d;
			}
			return null;
		} catch(Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private List<DocInfos> traverseZip_nio(File file) throws IOException {
		List<DocInfos> docInfos = new ArrayList<>();
		
		Path fPath = FileSystems.newFileSystem(file.toPath(), (ClassLoader)null).getPath("/");
		if(fPath != null) {
			DocInfosVisitor visitor = new DocInfosVisitor();
		    Files.walkFileTree(fPath, visitor);
		    
		    List<Path> xmlFiles = visitor.getXmlFiles();
		    for(Path xmlFile:xmlFiles) {
		    	Document doc = readXml(xmlFile);
				if(doc != null) {
					DocInfos d = new DocInfos();
					d.setDocument(doc);
					d.setRoot(xmlFile.getParent());
					d.setFilename(xmlFile.getFileName().toString());
					docInfos.add(d);
				}
		    	
		    }
		}
		
		
		return docInfos;
	}
	
	public static class DocInfosVisitor extends SimpleFileVisitor<Path> {
		
		private final List<Path> xmlFiles = new ArrayList<>();
		
		public List<Path> getXmlFiles() {
			return xmlFiles;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		throws IOException {
			String name = file.getFileName().toString();
			if(name != null && name.toLowerCase().endsWith(".xml")) {
				xmlFiles.add(file);
			}
	        return FileVisitResult.CONTINUE;
		}
	}
	
	private Document readXml(Path xmlFile) {
		Document doc = null;
		try(InputStream in = Files.newInputStream(xmlFile)) {
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			doc = xmlParser.parse(in, false);
			return doc;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static class ItemInfos {
		private String comment;
		private final Element itemEl;
		private final boolean originalItem;

		public ItemInfos(Element itemEl, boolean originalItem) {
			this.itemEl = itemEl;
			this.originalItem = originalItem;
		}
		
		public Element getItemEl() {
			return itemEl;
		}
		
		public boolean isOriginalItem() {
			return originalItem;
		}

		public String getComment() {
			return comment;
		}
		
		public void setComment(String comment) {
			this.comment = comment;
		}
	}
	
	public static class DocInfos implements Closeable {
		private Document doc;
		private String filename;
		private Path root;
		private Path metadata;
		private String qtiComment;
		
		public String getFilename() {
			return filename;
		}
		
		public void setFilename(String filename) {
			this.filename = filename;
		}

		public Document getDocument() {
			return doc;
		}
		
		public void setDocument(Document doc) {
			this.doc = doc;
		}

		public Path getMetadata() {
			return metadata;
		}

		public void setMetadata(Path metadata) {
			this.metadata = metadata;
		}

		public Path getRoot() {
			return root;
		}

		public void setRoot(Path root) {
			this.root = root;
		}

		public String getQtiComment() {
			return qtiComment;
		}

		public void setQtiComment(String qtiComment) {
			this.qtiComment = qtiComment;
		}

		@Override
		public void close() throws IOException {
			PathUtils.closeSubsequentFS(root);
		}
	}
}