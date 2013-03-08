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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.manager.FileStorage;
import org.olat.modules.qpool.manager.QuestionItemDAO;
import org.olat.modules.qpool.model.QuestionItemImpl;

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
	private final String importedFilename;
	private final File importedFile;
	private final QuestionItemDAO questionItemDao;
	

	public QTIImportProcessor(Identity owner, String importedFilename, File importedFile, QuestionItemDAO questionItemDao) {
		this.owner = owner;
		this.importedFilename = importedFilename;
		this.importedFile = importedFile;
		this.questionItemDao = questionItemDao;
	}
	
	public List<QuestionItem> process() {
		List<QuestionItem> qItems = new ArrayList<QuestionItem>();
		try {
			DocInfos docInfos = getDocInfos();
			if(docInfos != null && docInfos.doc != null) {
				List<ItemInfos> itemElements = getItemList(docInfos);
				for(ItemInfos itemElement:itemElements) {
					QuestionItem qItem = processItem(itemElement);
					if(qItem != null) {
						qItems.add(qItem);
					}
					
				}
			}
				
				/*
				VFSLeaf leaf = itemDir.createChildLeaf(filename);
				OutputStream out = leaf.getOutputStream(false);
				InputStream in = null;
				try {
					in = new FileInputStream(file);
					IOUtils.copy(in, out);
				} catch (FileNotFoundException e) {
					log.error("", e);
				} catch (IOException e) {
					log.error("", e);
				} finally {
					IOUtils.closeQuietly(in);
					IOUtils.closeQuietly(out);
				}
				*/
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
			ItemInfos itemInfos = new ItemInfos(item);
			Element comment = (Element)document.selectSingleNode("/questestinterop/qticomment");
			String qtiComment = getText(comment);
			itemInfos.setComment(qtiComment);
			itemElements.add(itemInfos);
		} else if(document.matches("/questestinterop/assessment")) {
			@SuppressWarnings("unchecked")
			List<Element> items = assessment.selectNodes("//item");
			for(Element it:items) {
				itemElements.add(new ItemInfos(it));
			}
		}
		return itemElements;
	}
	
	protected QuestionItem processItem(ItemInfos itemInfos) {
		Element itemEl = itemInfos.getItemEl();
		//filename
		String filename;
		String ident = getAttributeValue(itemEl, "ident");
		if(StringHelper.containsNonWhitespace(ident)) {
			filename = StringHelper.transformDisplayNameToFileSystemName(ident) + ".xml";
		} else {
			filename = "item.xml";
		}
		String dir = FileStorage.generateDir();
		
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
		poolItem.setDescription(itemInfos.getComment());
		
		//question type: mc, sc...
		QuestionType type = null;
		//test with openolat ident 
		if (ident != null && ident.startsWith(ItemParser.ITEM_PREFIX_SCQ)) {
			type = QuestionType.SC;
		} else if(ident != null && ident.startsWith(ItemParser.ITEM_PREFIX_MCQ)) {
			type = QuestionType.MC;
		} else if(ident != null && ident.startsWith(ItemParser.ITEM_PREFIX_FIB)) {
			type = QuestionType.FIB;
		} else if(ident != null && ident.startsWith(ItemParser.ITEM_PREFIX_ESSAY)) {
			type = QuestionType.ESSAY;
		} else if(ident != null && ident.startsWith(ItemParser.ITEM_PREFIX_KPRIM)) {
			type = QuestionType.KPRIM;		
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
			poolItem.setType(type.name());
		}
		questionItemDao.persist(owner, poolItem);
		return poolItem;
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
				if(name != null && name.toLowerCase().equals(".xml")) {
					Document doc = readXml(in);
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
			in.close();
			return doc;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static class ItemInfos {
		private Element itemEl;
		private String comment;
		
		public ItemInfos() {
			//
		}
		
		public ItemInfos(Element itemEl) {
			this.itemEl = itemEl;
		}
		
		public Element getItemEl() {
			return itemEl;
		}
		
		public void setItemEl(Element itemEl) {
			this.itemEl = itemEl;
		}
		
		public String getComment() {
			return comment;
		}
		
		public void setComment(String comment) {
			this.comment = comment;
		}
	}
	
	public static class DocInfos {
		private String filename;
		private Document doc;
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