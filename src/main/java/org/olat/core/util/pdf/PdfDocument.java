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
package org.olat.core.util.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.TransformerException;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

/**
 * The metric is dpi: 72 dpi.<br/>
 * 1 inch -> 72 points<br/>
 * 1cm -> 28.3 points<br/>
 * 
 * Initial date: 12.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PdfDocument {
	
	protected PDFont font = PDType1Font.HELVETICA;
	protected PDFont fontBold = PDType1Font.HELVETICA_BOLD;
	protected float marginTopBottom = 72.0f;
	protected float marginLeftRight = 72.0f;
	protected float lineHeightFactory = 1.5f;
	protected float width;
	
	protected PDDocument document;
	protected PDPage currentPage;
	protected PDPageContentStream currentContentStream;
	
	protected float currentY;
	private String printDate;
	
	public PdfDocument(Locale locale) throws IOException {
		document = new PDDocument();
		printDate = Formatter.getInstance(locale).formatDate(new Date());
	}

	public void close() throws IOException {
		document.close();
	}

	public PDPage addPage() throws IOException {
		if(currentContentStream != null) {
			currentContentStream.close();
		}

		PDPage page = new PDPage(PDPage.PAGE_SIZE_A4);
        document.addPage(page);
        currentPage = page;
        currentContentStream = new PDPageContentStream(document, currentPage);
        
        PDRectangle mediabox = currentPage.findMediaBox();
        width = mediabox.getWidth() - 2 * marginLeftRight;
        currentY = mediabox.getUpperRightY() - marginTopBottom;
        return page;
	}
	
	public void closePage() throws IOException {
		if(currentContentStream != null) {
			currentContentStream.close();
		}
	}
	
    public void addText(String text, float fontSize)
    throws IOException {
        currentContentStream.beginText();
        currentContentStream.setFont(font, fontSize);
        currentContentStream.moveTextPositionByAmount(marginLeftRight, currentY);
        currentContentStream.drawString(text);
        currentContentStream.endText();
        
        float leading = lineHeightFactory * fontSize;
        currentY -= leading; 
    }

    public void addParagraph(String text, float fontSize, float width)
    throws IOException {
    	addParagraph(text, fontSize, false, width);
    }
    	
    public void addParagraph(String text, float fontSize, boolean bold, float width)
    throws IOException {
        float leading = lineHeightFactory * fontSize;
        
        PDFont textFont = bold ? fontBold : font;

        List<String> lines = new ArrayList<String>();
        int lastSpace = -1;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0) {
                lines.add(text);
                text = "";
            } else {
                String subString = text.substring(0, spaceIndex);
                float size = fontSize * textFont.getStringWidth(subString) / 1000;
                if (size > width) {
                    if (lastSpace < 0) // So we have a word longer than the line... draw it anyways
                        lastSpace = spaceIndex;
                    subString = text.substring(0, lastSpace);
                    lines.add(subString);
                    text = text.substring(lastSpace).trim();
                    lastSpace = -1;
                } else {
                    lastSpace = spaceIndex;
                }
            }
        }

        currentContentStream.beginText();
        currentContentStream.setFont(textFont, fontSize);
        currentContentStream.moveTextPositionByAmount(marginLeftRight, currentY);            
        for (String line: lines) {
        	currentContentStream.drawString(line);
            currentContentStream.moveTextPositionByAmount(0, -leading);
            currentY -= leading; 
        }
        currentContentStream.endText();
    }
    
    public float getStringWidth(String string, float fontSize)
    throws IOException {
    	return fontSize * font.getStringWidth(string) / 1000;
    }
    
    public void drawLine(float xStart, float yStart, float xEnd, float yEnd, float width) 
    throws IOException {
		currentContentStream.setLineWidth(width);
		currentContentStream.drawLine(xStart, yStart, xEnd, yEnd);
    }
    
    public void drawTextAtMovedPositionByAmount(String text, float fontSize, float textx, float texty)
    throws IOException {
    	if(!StringHelper.containsNonWhitespace(text)) return;
    	
    	currentContentStream.beginText();
		currentContentStream.setFont(font, fontSize);
		currentContentStream.moveTextPositionByAmount(textx, texty);
		currentContentStream.drawString(text);
		currentContentStream.endText();
    }
    
    public void addMetadata(String title, String subject, String author)
    throws IOException, TransformerException {
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDDocumentInformation info = document.getDocumentInformation();
        Calendar date = Calendar.getInstance();

        info.setAuthor(author);
        info.setCreator(author);
        info.setCreationDate(date);
        info.setModificationDate(date);
        info.setTitle(title);
        info.setSubject(subject);

        XMPMetadata metadata = new XMPMetadata();
        XMPSchemaPDF pdfSchema = metadata.addPDFSchema();
        pdfSchema.setProducer("OpenOLAT");

        XMPSchemaBasic basicSchema = metadata.addBasicSchema();
        basicSchema.setModifyDate(date);
        basicSchema.setCreateDate(date);
        basicSchema.setCreatorTool("OpenOLAT");
        basicSchema.setMetadataDate(date);

        XMPSchemaDublinCore dcSchema = metadata.addDublinCoreSchema();
        dcSchema.setTitle(title);
        dcSchema.addCreator(author);
        dcSchema.setDescription(subject);

        PDMetadata metadataStream = new PDMetadata(document);
        metadataStream.importXMPMetadata(metadata);
        catalog.setMetadata(metadataStream);
    }
    
    public void addPageNumbers() throws IOException {
        float footerFontSize = 10.0f;
    	
    	@SuppressWarnings("unchecked")
		List<PDPage> allPages = document.getDocumentCatalog().getAllPages();
        int numOfPages = allPages.size();
        for( int i=0; i<allPages.size(); i++ ) {
            PDPage page = allPages.get( i );
            PDRectangle pageSize = page.findMediaBox();
            
            String text = (i+1) + " / " + numOfPages;
            float stringWidth = getStringWidth(text, footerFontSize);
            // calculate to center of the page
            float pageWidth = pageSize.getWidth();
            double x = (pageWidth - stringWidth) / 2.0f;
            double y = (marginTopBottom / 2.0f);
           
            // append the content to the existing stream
            PDPageContentStream contentStream = new PDPageContentStream(document, page, true, true,true);
            contentStream.beginText();
            // set font and font size
            contentStream.setFont( font, footerFontSize );
            contentStream.setTextTranslation(x, y);
            contentStream.drawString(text);
            contentStream.endText();
            
            //set current date
            contentStream.beginText();
            contentStream.setFont(font, footerFontSize );
            contentStream.setTextTranslation(marginLeftRight, y);
            contentStream.drawString(printDate);
            contentStream.endText();
            
            contentStream.close();
        }
    }
}
