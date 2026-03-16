/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.pdf;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.multipdf.PDFCloneUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
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
	private final PDFCloneUtility cloner;
	
	public PDFMergeUtility() {
		destination = null;//TODO selectus new PDDocument(MemoryUsageSetting.setupMainMemoryOnly());
		outline = new PDDocumentOutline();
		destination.getDocumentCatalog().setDocumentOutline(outline);
		cloner = null;//TODO selectus new PDFCloneUtility(destination);
	}
    
    public void addSummary(String name, InputStream summaryStream) {
    	if(summaryStream != null) {
    		append(name, summaryStream);
    	}
    }
    
    public boolean addDocument(String name, InputStream summaryStream, InputStream document) {
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
    	destination.save(out);
    }
    
	@Override
	public void close() throws IOException {
		destination.close();
	}

	private boolean append(String name, InputStream source) {
		//TODO selectus
		/*
		try (PDDocument sourceDoc = PDDocument.load(source, MemoryUsageSetting.setupMainMemoryOnly())) {
			boolean outlined = false;
			for (PDPage page : sourceDoc.getPages()) {
				PDPage newPage = new PDPage((COSDictionary) cloner.cloneForNewDocument(page.getCOSObject()));
				newPage.setCropBox(page.getCropBox());
				newPage.setMediaBox(page.getMediaBox());
				newPage.setRotation(page.getRotation());
				PDResources resources = page.getResources();
				if (resources != null) {
					newPage.setResources(new PDResources((COSDictionary) cloner.cloneForNewDocument(resources)));
				} else {
					newPage.setResources(new PDResources());
				}
				destination.addPage(newPage);
				
				if(!outlined && StringHelper.containsNonWhitespace(name)) {
					 PDOutlineItem pagesOutline = new PDOutlineItem();
			         pagesOutline.setTitle(name);
			         pagesOutline.setDestination(newPage);
			         outline.addLast(pagesOutline);
			         outlined = true;
				}
			}
			return true;
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
		*/
		return false;
	}
}
