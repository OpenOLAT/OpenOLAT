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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.TransformerException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.xml.XmpSerializer;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
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
	
	private static final Logger log = Tracing.createLoggerFor(PdfDocument.class);
	
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
	protected String printDate;
	
	public PdfDocument(Locale locale) {
		document = new PDDocument();
		printDate = Formatter.getInstance(locale).formatDate(new Date());
	}

	public void close() throws IOException {
		document.close();
	}

	public PDPage addPage() throws IOException {
		return addPage(PDRectangle.A4);
	}
	
	public PDPage addPageLandscape() throws IOException {
		return addPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
	}
	
	public PDPage addPage(PDRectangle size) throws IOException {
		if(currentContentStream != null) {
			currentContentStream.close();
		}

		PDPage page = new PDPage(size);
		document.addPage(page);
        currentPage = page;
        currentContentStream = new PDPageContentStream(document, currentPage);
        
        PDRectangle mediabox = currentPage.getMediaBox();
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
        currentContentStream.newLineAtOffset(marginLeftRight, currentY);
		showTextToStream(text, currentContentStream);
        currentContentStream.endText();
        
        float leading = lineHeightFactory * fontSize;
        currentY -= leading; 
    }

    public void addParagraph(String text, float fontSize, float paragraphWidth)
    throws IOException {
    	addParagraph(text, fontSize, false, paragraphWidth);
    }
    	
    public void addParagraph(String text, float fontSize, boolean bold, float paragraphWidth)
    throws IOException {
        float leading = lineHeightFactory * fontSize;
        
        PDFont textFont = bold ? fontBold : font;
        
        text = cleanString(text);

        List<String> lines = new ArrayList<>();
        int lastSpace = -1;
        while (text.length() > 0) {
            int spaceIndex = text.indexOf(' ', lastSpace + 1);
            if (spaceIndex < 0) {
                float size = getStringWidth(text, textFont, fontSize);
                if (size > paragraphWidth) {
                	String subString = text.substring(0, lastSpace);
                	lines.add(subString.trim());
                	text = text.substring(lastSpace).trim();
                }
                lines.add(text);
                text = "";
            } else {
                String subString = text.substring(0, spaceIndex);
                float size = getStringWidth(subString, textFont, fontSize);
                if (size > paragraphWidth) {
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
        currentContentStream.newLineAtOffset(marginLeftRight, currentY);            
        for (String line: lines) {
			showTextToStream(line, currentContentStream);
            currentContentStream.newLineAtOffset(0, -leading);
            currentY -= leading; 
        }
        currentContentStream.endText();
    }
    
    public float getStringWidth(String string, float fontSize)
    throws IOException {
    	return getStringWidth(string, font, fontSize);
    }
    
    public float getStringWidth(String string, PDFont textFont, float fontSize)
    throws IOException {
    	if(string == null || string.length() == 0) return 0.0f;
    	
    	try {
			string = cleanString(string);
			return fontSize * textFont.getStringWidth(string) / 1000;
		} catch (IllegalArgumentException e) {
			log.error("", e);
			return getStringEstimatedWidth(string.length(), textFont, fontSize);
		}
    }
    
    /**
     * This method calculated the width of a string based on its length.
     * 
     * @param length The length of the string
     * @param fontSize The font size
     * @return A width
     * @throws IOException
     */
    public float getStringEstimatedWidth(int length, PDFont textFont, float fontSize) throws IOException {
    	char[] onlyA = new char[length];
    	Arrays.fill(onlyA, 'A');
    	return fontSize * textFont.getStringWidth(String.valueOf(onlyA)) / 1000.0f;
    }
    
    public static String cleanString(String string) {
    	String text = string.replace('\n', ' ')
    			.replace('\r', ' ')
    			.replace('\t', ' ');
    	return cleanCharacters(text);
    }
    
    public static String cleanCharacters(String string) {
    	return string.replace("\u00AD", "")
    			.replace('\u00A0', ' ')
    			.replace('\u2212', '-');
    }
    
    public static void showTextToStream(String text, PDPageContentStream stream) throws IOException {
    	String cleanedText = cleanString(text);
    	try {
			stream.showText(cleanedText);
		} catch (IllegalArgumentException e) {
			log.warn("Cannot show PDF text: {}", text, e);
			stream.showText(Normalizer.normalize(cleanedText, Normalizer.Form.NFKD)
					.replaceAll("\\p{InCombiningDiacriticalMarks}+",""));
		}
    }
    
    public void drawLine(float xStart, float yStart, float xEnd, float yEnd, float lineWidth) 
    throws IOException {
		currentContentStream.setLineWidth(lineWidth);
		currentContentStream.moveTo(xStart, yStart);
		currentContentStream.lineTo(xEnd, yEnd);
		currentContentStream.stroke();
    }
    
    public void drawTextAtMovedPositionByAmount(String text, float fontSize, float textx, float texty)
    throws IOException {
    	if(!StringHelper.containsNonWhitespace(text)) return;
    	
		currentContentStream.beginText();
		currentContentStream.setFont(font, fontSize);
		currentContentStream.newLineAtOffset(textx, texty);
		showTextToStream(text, currentContentStream);
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

        XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        AdobePDFSchema pdfSchema = metadata.createAndAddAdobePDFSchema();
        pdfSchema.setProducer("OpenOLAT");

        XMPBasicSchema basicSchema = metadata.createAndAddXMPBasicSchema();
        basicSchema.setModifyDate(date);
        basicSchema.setCreateDate(date);
        basicSchema.setCreatorTool("OpenOLAT");
        basicSchema.setMetadataDate(date);

        DublinCoreSchema dcSchema = metadata.createAndAddDublinCoreSchema();
        dcSchema.setTitle(title);
        dcSchema.addCreator(author);
        dcSchema.setDescription(subject);

        PDMetadata metadataStream = new PDMetadata(document);
        
        // Create and return XMP data structure in XML format
        try(ByteArrayOutputStream xmpOutputStream = new ByteArrayOutputStream()) {
            new XmpSerializer().serialize(metadata, xmpOutputStream, true);
            metadataStream.importXMPMetadata(xmpOutputStream.toByteArray());
        } catch(IOException e) {
        	log.error("", e);
        }
        
        catalog.setMetadata(metadataStream);
    }
    
    public void addPageNumbers() throws IOException {
        float footerFontSize = 10.0f;
    	
		PDPageTree pageTree = document.getPages();
        int i = 0;
        int numOfPages = pageTree.getCount();
        for(Iterator<PDPage> pageIt=pageTree.iterator(); pageIt.hasNext(); ) {
            PDPage page = pageIt.next();
            PDRectangle pageSize = page.getMediaBox();
            
            String text = (++i) + " / " + numOfPages;
            float stringWidth = getStringWidth(text, footerFontSize);
            // calculate to center of the page
            float pageWidth = pageSize.getWidth();
            double x = (pageWidth - stringWidth) / 2.0f;
            double y = (marginTopBottom / 2.0f);
           
            // append the content to the existing stream
            PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true,true);
            contentStream.beginText();
            // set font and font size
            contentStream.setFont( font, footerFontSize );
            contentStream.setTextMatrix(Matrix.getTranslateInstance((float)x, (float)y));
            contentStream.showText(text);
            contentStream.endText();
            
            //set current date
            contentStream.beginText();
            contentStream.setFont(font, footerFontSize );
            contentStream.setTextMatrix(Matrix.getTranslateInstance(marginLeftRight, (float)y));
            contentStream.showText(printDate);
            contentStream.endText();
            
            contentStream.close();
        }
    }
    
	/**
	 * The number 6 was chosen after some trial and errors. It's a good compromise as
	 * the width of the letter is not fixed. Don't replace ... with ellipsis, it break
	 * the PDF.
	 * 
	 * @param text
	 * @param maxWidth
	 * @param fontSize
	 * @return
	 * @throws IOException
	 */
	protected String[] splitTextInTwo(String text, float maxWidth, float fontSize) throws IOException {
		float textWidth = getStringWidth(text, fontSize);
		if(maxWidth < textWidth) {

			float letterWidth = textWidth / text.length();
			int maxNumOfLetter = Math.round(maxWidth / letterWidth) - 1;
			//use space and comma as separator to gentle split the text

			int indexBefore = findBreakBefore(text, maxNumOfLetter);
			if(indexBefore < (maxNumOfLetter / 2)) {
				indexBefore = -1;//use more place
			}

			String one;
			String two;
			if(indexBefore <= 0) {
				//one word
				indexBefore = Math.min(text.length(), maxNumOfLetter - 6);
				one = text.substring(0, indexBefore) + "...";
				
				int indexAfter = findBreakAfter(text, maxNumOfLetter);
				if(indexAfter <= 0) {
					two = text.substring(indexBefore);
				} else {
					two = text.substring(indexAfter);
				}
			} else {
				one = text.substring(0, indexBefore + 1);
				two = text.substring(indexBefore + 1);
			}
			
			if(two.length() > maxNumOfLetter) {
				two = two.substring(0, maxNumOfLetter - 6) + "...";
			}
			return new String[] { one.trim(), two.trim() };
		}
		return new String[]{ text };
	}
	
	protected String[] splitTextInParts(String text, float maxWidth, float fontSize) throws IOException {
		float textWidth = getStringWidth(text, fontSize);
		if(maxWidth < textWidth) {
			
			List<String> list = new ArrayList<>();
			for( ; text.length() > 0; ) {
				
				// calculate them specifically for every line to be more precise
				textWidth = getStringWidth(text, fontSize);
				float letterWidth = textWidth / text.length();
				int maxNumOfLetter = Math.round(maxWidth / letterWidth) - 1;
				
				String line;
				if(text.length() < maxNumOfLetter) {
					line = text;
					text = "";
				} else {
					//use space and comma as separator to gentle split the text
					int indexBefore = findBreakBefore(text, maxNumOfLetter);
					if(indexBefore < (maxNumOfLetter / 2)) {
						indexBefore = -1;//use more place
					}
					
					if(indexBefore <= 0) {
						//one word
						indexBefore = Math.min(text.length(), maxNumOfLetter);
						line = text.substring(0, indexBefore);
						
						int indexAfter = findBreakAfter(text, maxNumOfLetter);
						if(indexAfter <= 0) {
							text = text.substring(indexBefore);
						} else {
							text = text.substring(indexAfter);
						}
					} else {
						line = text.substring(0, indexBefore + 1);
						text = text.substring(indexBefore + 1);
					}
				}
				list.add(line.trim());
			}
			return list.toArray(new String[list.size()]);
		}
		return new String[]{ text };
	}
	
	public static int findBreakBefore(String line, int start) {
		start = Math.min(line.length(), start);
		for (int i = start; i >= 0; --i) {
			char c = line.charAt(i);
			if (Character.isWhitespace(c) || c == '-' || c == ',') {
				return i;
			}
		}
		return -1;
	}
	
	public static int findBreakAfter(String line, int start) {
		int len = line.length();
		for (int i = start; i < len; ++i) {
			char c = line.charAt(i);
			if (Character.isWhitespace(c) || c == ',') {
				if(i + 1 < line.length()) {
					return i + 1;
				}
				return i;
			}
		}
		return -1;
	}
}
