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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.cyberneko.html.parsers.SAXParser;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.olat.modules.qpool.manager.QEducationalContextDAO;
import org.olat.modules.qpool.manager.QItemTypeDAO;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class is NOT thread-safe
 * 
 * Initial date: 07.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class QTIImportProcessor {
	
	private static final OLog log = Tracing.createLoggerFor(QTIImportProcessor.class);
	
	private final Identity owner;
	private final Locale defaultLocale;
	private final String importedFilename;
	private final File importedFile;

	private final QItemTypeDAO qItemTypeDao;
	private final QPoolFileStorage qpoolFileStorage;
	private final QuestionItemDAO questionItemDao;
	private final QEducationalContextDAO qEduContextDao;
	
	public QTIImportProcessor(Identity owner, Locale defaultLocale, QuestionItemDAO questionItemDao,
			QItemTypeDAO qItemTypeDao, QEducationalContextDAO qEduContextDao, QPoolFileStorage qpoolFileStorage) {
		this(owner, defaultLocale, null, null, questionItemDao, qItemTypeDao, qEduContextDao, qpoolFileStorage);
	}

	public QTIImportProcessor(Identity owner, Locale defaultLocale, String importedFilename, File importedFile,
			QuestionItemDAO questionItemDao, QItemTypeDAO qItemTypeDao, QEducationalContextDAO qEduContextDao,
			QPoolFileStorage qpoolFileStorage) {
		this.owner = owner;
		this.defaultLocale = defaultLocale;
		this.importedFilename = importedFilename;
		this.importedFile = importedFile;
		this.qItemTypeDao = qItemTypeDao;
		this.questionItemDao = questionItemDao;
		this.qEduContextDao = qEduContextDao;
		this.qpoolFileStorage = qpoolFileStorage;
	}
	
	public List<QuestionItem> process() {
		List<QuestionItem> qItems = new ArrayList<QuestionItem>();
		try {
			DocInfos docInfos = getDocInfos();
			if(docInfos != null && docInfos.doc != null) {
				List<ItemInfos> itemInfos = getItemList(docInfos);
				for(ItemInfos itemInfo:itemInfos) {
					QuestionItemImpl qItem = processItem(docInfos, itemInfo);
					if(qItem != null) {
						processFiles(qItem, itemInfo);
						qItems.add(qItem);
					}
				}
			}
		} catch (IOException e) {
			log.error("", e);
		}
		return qItems;
	}

	protected List<ItemInfos> getItemList(DocInfos doc) {
		Document document = doc.getDocument();
		List<ItemInfos> itemElements = new ArrayList<ItemInfos>();
		Element item = (Element)document.selectSingleNode("/questestinterop/item");
		Element assessment = (Element)document.selectSingleNode("/questestinterop/assessment");
		if(item != null) {
			ItemInfos itemInfos = new ItemInfos(item, true);
			Element comment = (Element)document.selectSingleNode("/questestinterop/qticomment");
			String qtiComment = getText(comment);
			itemInfos.setComment(qtiComment);
			itemElements.add(itemInfos);
		} else if(assessment != null) {
			@SuppressWarnings("unchecked")
			List<Element> items = assessment.selectNodes("//item");
			for(Element it:items) {
				itemElements.add(new ItemInfos(it, false));
			}
		}
		return itemElements;
	}
	
	protected QuestionItemImpl processItem(DocInfos docInfos, ItemInfos itemInfos) {
		Element itemEl = itemInfos.getItemEl();
		String comment = itemInfos.getComment();
		String originalFilename = null;
		if(itemInfos.isOriginalItem()) {
			originalFilename = docInfos.filename;
		}
		return processItem(itemEl, comment, originalFilename, null, null);
	}
	
	protected QuestionItemImpl processItem(Element itemEl, String comment, String originalItemFilename, String editor, String editorVersion) {
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
			poolItem.setEditor(editorVersion);
		} else if(ooFormat) {
			poolItem.setEditor("OpenOLAT");
		}
		//if question type not found, can be overridden by the metadatas
		processItemMetadata(poolItem, itemEl);
		if(poolItem.getType() == null) {
			QItemType defType = qItemTypeDao.loadByType(QuestionType.UNKOWN.name());
			poolItem.setType(defType);
		}
		questionItemDao.persist(owner, poolItem);
		return poolItem;
	}
	
	private void processItemMetadata(QuestionItemImpl poolItem, Element itemEl) {
		@SuppressWarnings("unchecked")
		List<Element> qtiMetadataFieldList = itemEl.selectNodes("./itemmetadata/qtimetadata/qtimetadatafield");
		for(Element qtiMetadataField:qtiMetadataFieldList) {
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
	protected void processFiles(QuestionItemImpl item, ItemInfos itemInfos) {
		if(itemInfos.originalItem) {
			processItemFiles(item);
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
			OutputStream os = endFile.getOutputStream(false);;
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

		try {
			InputStream in = new FileInputStream(importedFile);
			ZipInputStream zis = new ZipInputStream(in);

			ZipEntry entry;
			try {
				while ((entry = zis.getNextEntry()) != null) {
					String name = entry.getName();
					if(materials.contains(name)) {
						
						VFSLeaf leaf = container.createChildLeaf(name);
						OutputStream out = leaf.getOutputStream(false);
						BufferedOutputStream bos = new BufferedOutputStream (out);
						FileUtils.cpio(new BufferedInputStream(zis), bos, "unzip:"+entry.getName());
						bos.flush();
						bos.close();
						out.close();
					}
				}
			} catch(Exception e) {
				log.error("", e);
			} finally {
				IOUtils.closeQuietly(zis);
				IOUtils.closeQuietly(in);
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected List<String> getMaterials(Element el) {
		List<String> materialPath = new ArrayList<String>();
		//mattext
		List<Element> mattextList = el.selectNodes(".//mattext");
		for(Element mat:mattextList) {
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
		List<Element> matList = new ArrayList<Element>();
		matList.addAll(el.selectNodes(".//matimage"));
		matList.addAll(el.selectNodes(".//mataudio"));
		matList.addAll(el.selectNodes(".//matvideo"));
		
		for(Element mat:matList) {
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
			SAXParser parser = new SAXParser();
			HTMLHandler contentHandler = new HTMLHandler(materialPath);
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(content)));
		} catch (SAXException e) {
			log.error("", e);
		} catch (IOException e) {
			log.error("", e);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private static class HTMLHandler extends DefaultHandler {
		private final List<String> materialPath;
		
		public HTMLHandler(List<String> materialPath) {
			this.materialPath = materialPath;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			String elem = localName.toLowerCase();
			if("img".equals(elem)) {
				String imgSrc = attributes.getValue("src");
				if(StringHelper.containsNonWhitespace(imgSrc)) {
					materialPath.add(imgSrc);
				}
			}
		}
	}
	
	/**
	 * Process the file of an item's package
	 * @param item
	 * @param itemInfos
	 */
	protected void processItemFiles(QuestionItemImpl item) {
	//a package with an item
		String dir = item.getDirectory();
		String rootFilename = item.getRootFilename();
		VFSContainer container = qpoolFileStorage.getContainer(dir);
		if(importedFilename.toLowerCase().endsWith(".zip")) {
			ZipUtil.unzipStrict(importedFile, container);
		} else {
			VFSLeaf endFile = container.createChildLeaf(rootFilename);
			
			OutputStream out = null;
			FileInputStream in = null;
			try {
				out = endFile.getOutputStream(false);
				in = new FileInputStream(importedFile);
				IOUtils.copy(in, out);
			} catch (IOException e) {
				log.error("", e);
			} finally {
				IOUtils.closeQuietly(out);
				IOUtils.closeQuietly(in);
			}
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
	
	protected DocInfos getDocInfos() throws IOException {
		DocInfos doc;
		if(importedFilename.toLowerCase().endsWith(".zip")) {
			doc = traverseZip(importedFile);
		} else {
			doc = traverseFile(importedFile);
		}
		return doc;
	}
	
	private DocInfos traverseFile(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		try {
			Document doc = readXml(in);
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
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
	
	private DocInfos traverseZip(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		ZipInputStream zis = new ZipInputStream(in);

		ZipEntry entry;
		try {
			while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();
				if(name != null && name.toLowerCase().endsWith(".xml")) {
					Document doc = readXml(zis);
					if(doc != null) {
						DocInfos d = new DocInfos();
						d.doc = doc;
						d.filename = name;
						return d;
					}
				}
			}
			return null;
		} catch(Exception e) {
			log.error("", e);
			return null;
		} finally {
			IOUtils.closeQuietly(zis);
			IOUtils.closeQuietly(in);
		}
	}
	
	private Document readXml(InputStream in) {
		Document doc = null;
		try {
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
	
	public static class DocInfos {
		private Document doc;
		private String filename;
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

		public String getQtiComment() {
			return qtiComment;
		}

		public void setQtiComment(String qtiComment) {
			this.qtiComment = qtiComment;
		}
	}
}