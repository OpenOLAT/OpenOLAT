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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.CDATA;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.manager.QPoolFileStorage;
import org.xml.sax.InputSource;

import nu.validator.htmlparser.common.XmlViolationPolicy;
import nu.validator.htmlparser.sax.HtmlParser;

/**
 * 
 * Initial date: 11.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIExportProcessor {
	
	private static final Logger log = Tracing.createLoggerFor(QTIExportProcessor.class);

	private final QPoolFileStorage qpoolFileStorage;
	
	public QTIExportProcessor(QPoolFileStorage qpoolFileStorage) {
		this.qpoolFileStorage = qpoolFileStorage;
	}
	
	public void process(QuestionItemFull fullItem, ZipOutputStream zout, Set<String> names) {
		String dir = fullItem.getDirectory();
		VFSContainer container = qpoolFileStorage.getContainer(dir);

		String rootDir = "qitem_" + fullItem.getKey();
		List<VFSItem> items = container.getItems();
		addMetadata(fullItem, rootDir, zout);
		for(VFSItem item:items) {
			ZipUtil.addToZip(item, rootDir, zout, new VFSSystemItemFilter(), false);
		}
	}
	
	private void addMetadata(QuestionItemFull fullItem, String dir, ZipOutputStream zout) {
		try {
			Document document = DocumentHelper.createDocument();
			Element qtimetadata = document.addElement("qtimetadata");
			QTIMetadataConverter enricher = new QTIMetadataConverter(qtimetadata);
			enricher.toXml(fullItem);
			zout.putNextEntry(new ZipEntry(dir + "/" + "qitem_" + fullItem.getKey() + "_metadata.xml"));
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter(zout, format);
			writer.write(document);
		} catch (IOException e) {
			log.error("",  e);
		}
	}
	
	/**
	 * <li>List all items
	 * <li>Rewrite path
	 * <li>Assemble qti.xml
	 * <li>Write files at new path
	 * @param fullItems
	 * @param zout
	 */
	public void assembleTest(List<QuestionItemFull> fullItems, ZipOutputStream zout) {
		ItemsAndMaterials itemAndMaterials = new ItemsAndMaterials();
		for(QuestionItemFull fullItem:fullItems) {
			collectMaterials(fullItem, itemAndMaterials);
		}
		
		try {
			byte[] buffer = new byte[FileUtils.BSIZE];
			
			//write qti.xml
			Element sectionEl = createSectionBasedAssessment("Assessment");
			for(Element itemEl:itemAndMaterials.getItemEls()) {
				//generate new ident per item
				String ident = getAttributeValue(itemEl, "ident");
				String exportIdent = QTIEditHelper.generateNewIdent(ident);
				itemEl.addAttribute("ident", exportIdent);
				sectionEl.add(itemEl);
			}
			zout.putNextEntry(new ZipEntry("qti.xml"));
			XMLWriter xw = new XMLWriter(zout, new OutputFormat("  ", true));
			xw.write(sectionEl.getDocument());
			zout.closeEntry();
			
			//write materials
			for(ItemMaterial material:itemAndMaterials.getMaterials()) {
				String exportPath = material.getExportUri();
				zout.putNextEntry(new ZipEntry(exportPath));
				InputStream in = material.getLeaf().getInputStream();
				int c;
				while ((c = in.read(buffer, 0, buffer.length)) != -1) {
					zout.write(buffer, 0, c);
				}
				IOUtils.closeQuietly(in);
				zout.closeEntry();
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public Element exportToQTIEditor(QuestionItemFull fullItem, VFSContainer editorContainer) {
		ItemsAndMaterials itemAndMaterials = new ItemsAndMaterials();
		collectMaterials(fullItem, itemAndMaterials);
		if(itemAndMaterials.getItemEls().isEmpty()) {
			return null;//nothing found
		}
		
		Element itemEl = itemAndMaterials.getItemEls().get(0);
		//write materials
		for(ItemMaterial material:itemAndMaterials.getMaterials()) {
			String exportPath = material.getExportUri();
			VFSLeaf leaf = editorContainer.createChildLeaf(exportPath);
			VFSManager.copyContent(material.getLeaf(), leaf, false, null);
		}
		return itemEl;
	}
	
	protected void collectMaterials(QuestionItemFull fullItem, ItemsAndMaterials materials) {
		String dir = fullItem.getDirectory();
		String rootFilename = fullItem.getRootFilename();
		VFSContainer container = qpoolFileStorage.getContainer(dir);
		VFSItem rootItem = container.resolve(rootFilename);

		if(rootItem instanceof VFSLeaf) {
			VFSLeaf rootLeaf = (VFSLeaf)rootItem;
			Element el = (Element)readItemXml(rootLeaf).clone();
			Element itemEl = (Element)el.clone();
			//enrichScore(itemEl);
			enrichWithMetadata(fullItem, itemEl);
			collectResources(itemEl, container, materials);
			materials.addItemEl(itemEl);
		}
	}
	
	private String getAttributeValue(Element el, String attrName) {
		if(el == null) return null;
		Attribute attr = el.attribute(attrName);
		return (attr == null) ? null : attr.getStringValue();
	}

	private void collectResources(Element el, VFSContainer container, ItemsAndMaterials materials) {
		collectResourcesInMatText(el, container, materials);
		collectResourcesInMatMedias(el, container, materials);
	}
	
	/**
	 * Collect the file and rewrite the 
	 * @param el
	 * @param container
	 * @param materials
	 * @param paths
	 */
	private void collectResourcesInMatText(Element el, VFSContainer container, ItemsAndMaterials materials) {
		//mattext
		List<Node> mattextList = el.selectNodes(".//mattext");
		for(Node matNode:mattextList) {
			Element mat = (Element)matNode;
			Attribute texttypeAttr = mat.attribute("texttype");
			if(texttypeAttr != null && "text/html".equals(texttypeAttr.getValue())) {
				List<Node> childElList = new ArrayList<>(mat.content());
				for(Node childEl:childElList) {
					mat.remove(childEl);
				}

				for(Node childEl:childElList) {
					if(Node.CDATA_SECTION_NODE == childEl.getNodeType()) {
						CDATA data = (CDATA)childEl;
						boolean changed = false;
						String text = data.getText();
						List<String> materialPaths = findMaterialInMatText(text);
						for(String materialPath:materialPaths) {
							VFSItem matVfsItem = container.resolve(materialPath);
							if(matVfsItem instanceof VFSLeaf) {
								String exportUri = generateExportPath(materials.getPaths(), matVfsItem);
								materials.addMaterial(new ItemMaterial((VFSLeaf)matVfsItem, exportUri));
								text = text.replaceAll(materialPath, exportUri);
								changed = true;
							}
						}
						if(changed) {
							mat.addCDATA(text);
						} else {
							mat.add(childEl);
						}
					} else {
						mat.add(childEl);
					}
				}
			}
		}
	}
	
	private void collectResourcesInMatMedias(Element el, VFSContainer container, ItemsAndMaterials materials) {
		//matimage uri
		List<Node> matList = new ArrayList<>();
		matList.addAll(el.selectNodes(".//matimage"));
		matList.addAll(el.selectNodes(".//mataudio"));
		matList.addAll(el.selectNodes(".//matvideo"));
		
		for(Node matNode:matList) {
			Element mat = (Element)matNode;
			Attribute uriAttr = mat.attribute("uri");
			String uri = uriAttr.getValue();
			
			VFSItem matVfsItem = container.resolve(uri);
			if(matVfsItem instanceof VFSLeaf) {
				String exportUri = generateExportPath(materials.getPaths(), matVfsItem);
				ItemMaterial iMat = new ItemMaterial((VFSLeaf)matVfsItem, exportUri);
				materials.addMaterial(iMat);
				mat.addAttribute("uri", exportUri);
			}
		}
	}
	
	private String generateExportPath(Set<String> paths, VFSItem leaf) {
		String filename = leaf.getName();
		for(int count=0; paths.contains(filename) && count < 999 ; ) {
			filename = FileUtils.appendNumberAtTheEndOfFilename(filename, count++);
		}
		paths.add(filename);
		return "media/" + filename;
	}
	
	/**
	 * Parse the content and collect the images source
	 * @param content
	 * @param materialPath
	 */
	private List<String> findMaterialInMatText(String content) {
		try {
			HtmlParser parser = new HtmlParser(XmlViolationPolicy.ALTER_INFOSET);
			QTI12HtmlHandler contentHandler = new QTI12HtmlHandler();
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(content)));
			return contentHandler.getMaterialPath();
		} catch (Exception e) {
			log.error("", e);
			return Collections.emptyList();
		}
	}
	
	private Element createSectionBasedAssessment(String title) {
		DocumentFactory df = DocumentFactory.getInstance();
		Document doc = df.createDocument();
		doc.addDocType(QTIConstants.XML_DOCUMENT_ROOT, null, QTIConstants.XML_DOCUMENT_DTD);
		
		/*
		<questestinterop>
  		<assessment ident="frentix_9_87230240084930" title="SR Test">
		 */
		Element questestinterop = doc.addElement(QTIConstants.XML_DOCUMENT_ROOT);
		Element assessment = questestinterop.addElement("assessment");
		assessment.addAttribute("ident", CodeHelper.getGlobalForeverUniqueID());
		assessment.addAttribute("title", title);
		//metadata
		/*
		<qtimetadata>
      	<qtimetadatafield>
        <fieldlabel>qmd_assessmenttype</fieldlabel>
        <fieldentry>Assessment</fieldentry>
      </qtimetadatafield>
    </qtimetadata>
		*/
		Element qtimetadata = assessment.addElement("qtimetadata");
		addMetadataField("qmd_assessmenttype", "Assessment", qtimetadata);
		//section
		/*
		<section ident="frentix_9_87230240084931" title="Section">
    	<selection_ordering>
      	<selection/>
      	<order order_type="Sequential"/>
    	</selection_ordering>
    */
		Element section = assessment.addElement("section");
		section.addAttribute("ident", CodeHelper.getGlobalForeverUniqueID());
		section.addAttribute("title", "Section");
		Element selectionOrdering = section.addElement("selection_ordering");
		selectionOrdering.addElement("selection");
		Element order = selectionOrdering.addElement("order");
		order.addAttribute("order_type", "Sequential");
		return section;
	}
	
	private void addMetadataField(String label, String entry, Element qtimetadata) {
		if(entry != null) {
			Element qtimetadatafield = qtimetadata.addElement("qtimetadatafield");
			qtimetadatafield.addElement("fieldlabel").setText(label);
			qtimetadatafield.addElement("fieldentry").setText(entry);
		}
	}
	
	private Element readItemXml(VFSLeaf leaf) {
		Document doc = null;
		try {
			InputStream is = leaf.getInputStream();
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			doc = xmlParser.parse(is, false);
			
			Element item = (Element)doc.selectSingleNode("questestinterop/item");
			is.close();
			return item;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * OpenOLAT QTI 1.2 runtime need it:
	 	 <resprocessing>
    	<outcomes>
      	<decvar varname="SCORE" vartype="Decimal" defaultval="0" minvalue="0.0" maxvalue="1.0" cutvalue="1.0"/>
    	</outcomes>
	 * @param fullItem
	 * @param item
	 */
	protected void enrichScoreDontUseIt(Element item) {
		List<Node> sv = item.selectNodes("./resprocessing/outcomes/decvar[@varname='SCORE']");
		// the QTIv1.2 system relies on the SCORE variable of items
		if (sv.isEmpty()) {
			//create resprocessing if needed
			Element resprocessing;
			if(item.selectNodes("./resprocessing").isEmpty()) {
				resprocessing = item.addElement("resprocessing");
			} else {
				resprocessing = (Element)item.selectNodes("./resprocessing").get(0);
			}

			//create outcomes if needed
			Element outcomes;
			if(resprocessing.selectNodes("./outcomes").isEmpty()) {
				outcomes = resprocessing.addElement("outcomes");
			} else {
				outcomes = (Element)resprocessing.selectNodes("./outcomes").get(0);
			}
			
			//create decvar if needed
			Element decvar = outcomes.addElement("decvar");
			decvar.addAttribute("varname", "SCORE");
			decvar.addAttribute("vartype", "Decimal");
			decvar.addAttribute("defaultval", "0");
			decvar.addAttribute("minvalue", "0.0");
			decvar.addAttribute("maxvalue", "1.0");
			decvar.addAttribute("cutvalue", "1.0");
		}
	}
	
	private void enrichWithMetadata(QuestionItemFull fullItem, Element item) {
		Element qtimetadata = (Element)item.selectSingleNode("./itemmetadata/qtimetadata");
		if(qtimetadata != null) {
			QTIMetadataConverter enricher = new QTIMetadataConverter(qtimetadata);
			enricher.toXml(fullItem);
		}
	}
	
	private static final class ItemsAndMaterials {
		private final Set<String> paths = new HashSet<>();
		private final List<Element> itemEls = new ArrayList<>();
		private final List<ItemMaterial> materials = new ArrayList<>();
		
		public Set<String> getPaths() {
			return paths;
		}
		
		public List<Element> getItemEls() {
			return itemEls;
		}
		
		public void addItemEl(Element el) {
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
		private final VFSLeaf leaf;
		private final String exportUri;
		
		public ItemMaterial(VFSLeaf leaf, String exportUri) {
			this.leaf = leaf;
			this.exportUri = exportUri;
		}
		
		public VFSLeaf getLeaf() {
			return leaf;
		}
		
		public String getExportUri() {
			return exportUri;
		}
	}
}
