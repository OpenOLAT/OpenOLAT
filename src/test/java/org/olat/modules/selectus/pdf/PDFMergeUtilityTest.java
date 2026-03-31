/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdfwriter.compress.CompressParameters;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.junit.Test;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 24 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PDFMergeUtilityTest {
	
	private static final Logger log = Tracing.createLoggerFor(PDFMergeUtilityTest.class);
	
	@Test
	public void rawCombineDocuments() throws Exception {
		InputStream in1 = PDFMergeUtilityTest.class.getResourceAsStream("DocPosition_1.pdf");
		InputStream in2 = PDFMergeUtilityTest.class.getResourceAsStream("DocPosition_2.pdf");
		InputStream in3 = PDFMergeUtilityTest.class.getResourceAsStream("DocPosition_3.pdf");
		
		List<RandomAccessRead> sources = List.of(new RandomAccessReadBuffer(in1.readAllBytes()),
				new RandomAccessReadBuffer(in2.readAllBytes()),
				new RandomAccessReadBuffer(in3.readAllBytes()));
		mergeReads(sources);
	}
	
	public void mergeReads(final List<RandomAccessRead> sources) throws IOException {
		String title = "My title";
		String creator = "Alexander Kriegisch";
		String subject = "Subject with umlauts ÄÖÜ";

		File combinedFile = File.createTempFile("merge_pdf", ".pdf");
		log.info("Comined file: {}", combinedFile);
		try (COSStream cosStream = new COSStream();
			OutputStream mergedPDFOutputStream = new FileOutputStream(combinedFile)) {
			// If you're merging in a servlet, you can modify this example to use the outputStream only
			// as the response as shown here: http://stackoverflow.com/a/36894346/535646

			PDFMergerUtility pdfMerger = new PDFMergerUtility();
			pdfMerger.addSources(sources);
			pdfMerger.setDestinationStream(mergedPDFOutputStream);

			// PDF and XMP properties must be identical, otherwise document is not PDF/A compliant
			PDDocumentInformation pdfDocumentInfo = new PDDocumentInformation();
            
			PDMetadata xmpMetadata = createXMPMetadata(cosStream, title, creator, subject);
			pdfMerger.setDestinationDocumentInformation(pdfDocumentInfo);
			pdfMerger.setDestinationMetadata(xmpMetadata);

            log.info("Merging {} source documents into one PDF", sources.size());
            pdfMerger.mergeDocuments(IOUtils.createMemoryOnlyStreamCache(), CompressParameters.NO_COMPRESSION);
            mergedPDFOutputStream.flush();
            log.info("PDF merge successful, size = {{}} bytes", combinedFile.length());
        } catch (BadFieldValueException | TransformerException e) {
            throw new IOException("PDF merge problem", e);
        } finally {
            sources.forEach(IOUtils::closeQuietly);
        }
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
