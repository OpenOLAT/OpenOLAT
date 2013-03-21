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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.manager.FileStorage;

/**
 * 
 * Initial date: 11.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTIExportProcessor {
	
	private static final OLog log = Tracing.createLoggerFor(QTIExportProcessor.class);

	private final FileStorage qpoolFileStorage;
	
	public QTIExportProcessor(FileStorage qpoolFileStorage) {
		this.qpoolFileStorage = qpoolFileStorage;
	}
	
	public void process(QuestionItemFull fullItem, ZipOutputStream zout) {
		String dir = fullItem.getDirectory();
		VFSContainer container = qpoolFileStorage.getContainer(dir);
		ZipUtil.addToZip(container, "", zout);
	}
	
	public void assembleTest(List<QuestionItemFull> fullItems) {
		Element sectionEl = createSectionBasedAssessment("Assessment");
		for(QuestionItemFull fullItem:fullItems) {
			String dir = fullItem.getDirectory();
			String rootFilename = fullItem.getRootFilename();
			VFSContainer container = qpoolFileStorage.getContainer(dir);
			VFSItem rootItem = container.resolve(rootFilename);
			List<String> path = new ArrayList<String>();
			collectResource(container, "", path);
			System.out.println(path);
			
			if(rootItem instanceof VFSLeaf) {
				VFSLeaf rootLeaf = (VFSLeaf)rootItem;
				Element el = readItemXml(rootLeaf);
				Element cloneEl = (Element)el.clone();
				enrichScore(cloneEl);
				enrichWithMetadata(fullItem, cloneEl);
				sectionEl.add(cloneEl);
			}
		}
		
		writeDocument(sectionEl);
	}
	
	private void collectResource(VFSContainer container, String currentPath, List<String> path) {
		List<VFSItem> items = container.getItems();
		for(VFSItem item:items) {
			String itemPath = currentPath + "/" + item.getName();
			if(item instanceof VFSLeaf) {
				path.add(itemPath);
			} else if(item instanceof VFSContainer) {
				collectResource((VFSContainer)item, itemPath, path);
			}
		}
	}
	
	private void writeDocument(Element el) {	
		try {
			Document doc = el.getDocument();
			OutputStream os = new FileOutputStream(new File("/HotCoffee/test.xml"));
			XMLWriter xw = new XMLWriter(os, new OutputFormat("  ", true));
			xw.write(doc);
			xw.close();
			os.close();
		} catch (IOException e) {
			log.error("", e);
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
	private void enrichScore(Element item) {
		@SuppressWarnings("unchecked")
		List<Element> sv = item.selectNodes("./resprocessing/outcomes/decvar[@varname='SCORE']");
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
		String path = fullItem.getTaxonomicPath();
		System.out.println(qtimetadata + " " + path);
	}
	
	private void addMetadataField(String label, String entry, Element qtimetadata) {
		Element qtimetadatafield = qtimetadata.addElement("qtimetadatafield");
		qtimetadatafield.addElement("fieldlabel").setText(label);
		qtimetadatafield.addElement("fieldentry").setText(entry);
	}
	
	
	/*
	 * 
	 * <itemmetadata>
					<qtimetadata>
						<qtimetadatafield>
							<fieldlabel>qmd_levelofdifficulty</fieldlabel>
							<fieldentry>basic</fieldentry>
						</qtimetadatafield>
						<qtimetadatafield>
							<fieldlabel>qmd_topic</fieldlabel>
							<fieldentry>qtiv1p2test</fieldentry>
						</qtimetadatafield>
					</qtimetadata>
				</itemmetadata>

				<qtimetadata>
            <vocabulary uri="imsqtiv1p2_metadata.txt" vocab_type="text/plain"/>
            <qtimetadatafield>
               <fieldlabel>qmd_weighting</fieldlabel>
               <fieldentry>2</fieldentry>
            </qtimetadatafield>
            ...
         </qtimetadata>

         
         
         http://qtimigration.googlecode.com/svn-history/r29/trunk/pyslet/unittests/data_imsqtiv1p2p1/input/


<qtimetadatafield>
                    <fieldlabel>name</fieldlabel>
                    <fieldentry>Metadata New-Style</fieldentry>
                </qtimetadatafield>
                <qtimetadatafield>
                    <fieldlabel>marks</fieldlabel>
                    <fieldentry>50.0</fieldentry>
                </qtimetadatafield>
                <qtimetadatafield>
                    <fieldlabel>syllabusarea</fieldlabel>
                    <fieldentry>Migration</fieldentry>
                </qtimetadatafield>
                <qtimetadatafield>
                    <fieldlabel>author</fieldlabel>
                    <fieldentry>Steve Author</fieldentry>
                </qtimetadatafield>
                <qtimetadatafield>
                    <fieldlabel>creator</fieldlabel>
                    <fieldentry>Steve Creator</fieldentry>
                </qtimetadatafield>
                <qtimetadatafield>
                    <fieldlabel>owner</fieldlabel>
                    <fieldentry>Steve Owner</fieldentry>
                </qtimetadatafield>
                <qtimetadatafield>
                    <fieldlabel>item type</fieldlabel>
                    <fieldentry>MCQ</fieldentry>
                </qtimetadatafield>
                <qtimetadatafield>
                    <fieldlabel>status</fieldlabel>
                    <fieldentry>Experimental</fieldentry>
                </qtimetadatafield>
                <qtimetadatafield>
                    <fieldlabel>qmd_levelofdifficulty</fieldlabel>
                    <fieldentry>Professional Development</fieldentry>
                </qtimetadatafield>
                <qtimetadatafield>
                    <fieldlabel>qmd_toolvendor</fieldlabel>
                    <fieldentry>Steve Lay</fieldentry>
                </qtimetadatafield>
                <qtimetadatafield>
                    <fieldlabel>description</fieldlabel>
                    <fieldentry>General Description Extension</fieldentry>
                </qtimetadatafield>
                
                
                <itemmetadata>
            <qmd_itemtype>MCQ</qmd_itemtype>
            <qmd_levelofdifficulty>Professional Development</qmd_levelofdifficulty>
            <qmd_maximumscore>50.0</qmd_maximumscore>
            <qmd_status>Experimental</qmd_status>
            <qmd_toolvendor>Steve Lay</qmd_toolvendor>
            <qmd_topic>Migration</qmd_topic>
        </itemmetadata>
	 */
}
