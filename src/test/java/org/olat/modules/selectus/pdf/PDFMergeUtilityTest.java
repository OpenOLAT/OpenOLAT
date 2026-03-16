/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.pdf;

/**
 * 
 * Initial date: 24 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PDFMergeUtilityTest {
	
	/*
	public void rawCombineDocuments() throws Exception {
		InputStream in1 = PDFMergeUtilityTest.class.getResourceAsStream("DocPosition_1.pdf");
		InputStream in2 = PDFMergeUtilityTest.class.getResourceAsStream("DocPosition_2.pdf");
		InputStream in3 = PDFMergeUtilityTest.class.getResourceAsStream("DocPosition_3.pdf");

		PDDocument destination = new PDDocument(MemoryUsageSetting.setupMainMemoryOnly());
		PDDocumentOutline outline = new PDDocumentOutline();
		destination.getDocumentCatalog().setDocumentOutline(outline);
		
        PDFCloneUtility cloner = new PDFCloneUtility(destination);
        append(destination, outline, "Doc 1", in1, cloner);
        append(destination, outline, "Document", in2, cloner);
        append(destination, outline, "Med. crasher", in3, cloner);

		File combinedFile = new File("/HotCoffee/test.pdf");
		if(combinedFile.exists()) {
			combinedFile.delete();
			combinedFile = new File("/HotCoffee/test.pdf");
		}
		try(OutputStream out = new FileOutputStream(combinedFile)) {
			destination.save(out);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
        
	private void append(PDDocument destination, PDDocumentOutline outline, String name, InputStream source, PDFCloneUtility cloner) {
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
				
				if(!outlined) {
					 PDOutlineItem pagesOutline = new PDOutlineItem();
			         pagesOutline.setTitle(name);
			         pagesOutline.setDestination(newPage);
			         outline.addLast(pagesOutline);
			         outlined = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
}
