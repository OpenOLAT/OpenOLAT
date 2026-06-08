/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.pdf;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 24 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PDFMergeUtility implements Closeable {
	
	private static final Logger log = Tracing.createLoggerFor(PDFMergeUtility.class);
	
	private final PDDocument destination;
	private final PDDocumentOutline outline;
	private final PDFMergerUtility merger;
	
	public PDFMergeUtility() {
		destination = new PDDocument();
		outline = new PDDocumentOutline();
		destination.getDocumentCatalog().setDocumentOutline(outline);
		merger = new PDFMergerUtility();
	}
    
    public void addSummary(String name, InputStream summaryStream)
    throws IOException {
    	if(summaryStream != null) {
    		merger.addSource(new RandomAccessReadBuffer(summaryStream.readAllBytes()));
    	}
    }
    
    public boolean addDocument(String name, InputStream summaryStream, InputStream document) throws IOException {
    	boolean allOk = true;
    	if(summaryStream != null) {
    		allOk &= append(name, summaryStream);
    	}
    	if(document != null) {
    		allOk &= append(null, document);
    	}
    	return allOk;
    }
    
    public void merge(OutputStream out) throws IOException {
    	try (COSStream cosStream = new COSStream()) {
    		// If you're merging in a servlet, you can modify this example to use the outputStream only
    		// as the response as shown here: http://stackoverflow.com/a/36894346/535646
    		merger.setDestinationStream(out);

    		// PDF and XMP properties must be identical, otherwise document is not PDF/A compliant
    		PDDocumentInformation pdfDocumentInfo = new PDDocumentInformation();
            
    		PDMetadata xmpMetadata = createXMPMetadata(cosStream, "Test", "Test", "Application");
    		merger.setDestinationDocumentInformation(pdfDocumentInfo);
    		merger.setDestinationMetadata(xmpMetadata);

            log.info("Merging {} source documents into one PDF", "unkown");
            merger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache(), CompressParameters.NO_COMPRESSION);
            log.info("PDF merge successful, size = {{}} bytes");
        } catch (BadFieldValueException | TransformerException e) {
			log.error("", e);
		}
    }
    
	@Override
	public void close() throws IOException {
		destination.close();
	}

	private boolean append(String name, InputStream source) throws IOException {
    	merger.addSource(new RandomAccessReadBuffer(source.readAllBytes()));
		return true;
	}
	
    private PDMetadata createXMPMetadata(COSStream cosStream, String title, String creator, String subject)
            throws BadFieldValueException, TransformerException, IOException {
        log.info("Setting XMP metadata (title, author, subject) for merged PDF");
        XMPMetadata xmpMetadata = XMPMetadata.createXMPMetadata();

        // PDF/A-1b properties
        PDFAIdentificationSchema pdfaSchema = xmpMetadata.createAndAddPDFAIdentificationSchema();
        pdfaSchema.setPart(1);
        pdfaSchema.setConformance("B");

        // Dublin Core properties
        DublinCoreSchema dublinCoreSchema = xmpMetadata.createAndAddDublinCoreSchema();
        dublinCoreSchema.setTitle(title);
        dublinCoreSchema.addCreator(creator);
        dublinCoreSchema.setDescription(subject);

        // XMP Basic properties
        XMPBasicSchema basicSchema = xmpMetadata.createAndAddXMPBasicSchema();
        Calendar creationDate = Calendar.getInstance();
        basicSchema.setCreateDate(creationDate);
        basicSchema.setModifyDate(creationDate);
        basicSchema.setMetadataDate(creationDate);
        basicSchema.setCreatorTool(creator);

        // Create and return XMP data structure in XML format
        try (OutputStream cosXMPStream = cosStream.createOutputStream())
        {
            new XmpSerializer().serialize(xmpMetadata, cosXMPStream, true);
            cosStream.setName(COSName.TYPE, "Metadata" );
            cosStream.setName(COSName.SUBTYPE, "XML" );
            return new PDMetadata(cosStream);
        }
    }
}
