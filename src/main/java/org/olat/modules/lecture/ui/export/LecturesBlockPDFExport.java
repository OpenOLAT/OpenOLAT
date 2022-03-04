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
package org.olat.modules.lecture.ui.export;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.pdf.PdfDocument;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 22 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesBlockPDFExport extends PdfDocument implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(LecturesBlockPDFExport.class);
	
	private String teacher;
	private int numOfLectures;
	private final Translator translator;
	private final LectureBlock lectureBlock;
	private final RepositoryEntry entry;
	private final boolean authorizedAbsenceEnabled;
	
	public LecturesBlockPDFExport(LectureBlock lectureBlock, boolean authorizedAbsenceEnabled, Translator translator)
			throws IOException {
		super(translator.getLocale());
		
		marginTopBottom = 62.0f;
		marginLeftRight = 62.0f;
		entry = lectureBlock.getEntry();
		this.translator = translator;
		this.lectureBlock = lectureBlock;
		this.authorizedAbsenceEnabled = authorizedAbsenceEnabled;
		
		numOfLectures = lectureBlock.getCalculatedLecturesNumber();
	}

	public String getTeacher() {
		return teacher;
	}

	public void setTeacher(String teacher) {
		this.teacher = teacher;
	}

	@Override
	public long getCacheControlDuration() {
		return 0;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/pdf";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			Formatter formatter = Formatter.getInstance(translator.getLocale());
			String filename = lectureBlock.getTitle()
					+ "_" + formatter.formatDate(lectureBlock.getStartDate())
					+ "_" + formatter.formatTimeShort(lectureBlock.getStartDate())
					+ "-" + formatter.formatTimeShort(lectureBlock.getEndDate())
					+ ".pdf";
			hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(filename));			
			hres.setHeader("Content-Description",StringHelper.urlEncodeUTF8(filename));
			document.save(hres.getOutputStream());
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		try {
			close();
		} catch (IOException e) {
			log.error("", e);
		}
	}

	public void create(List<Identity> rows, List<LectureBlockRollCall> rollCalls, List<AbsenceNotice> notices)
    throws IOException, TransformerException {
		addPageLandscape();
		String lectureBlockTitle = lectureBlock.getTitle();
		String resourceTitle = entry.getDisplayname();
		addMetadata(lectureBlockTitle, resourceTitle, teacher);
	
		String title = resourceTitle + " - " + lectureBlockTitle;
		title = translator.translate("attendance.list.title", title);
		addParagraph(title, 16, true, width);
	
		Formatter formatter = Formatter.getInstance(translator.getLocale());
		String dates = translator.translate("pdf.table.dates",
			formatter.formatDate(lectureBlock.getStartDate()),
			formatter.formatTimeShort(lectureBlock.getStartDate()),
			formatter.formatTimeShort(lectureBlock.getEndDate()));
	
		addParagraph(dates, 12, true, width);
	  	
		float cellMargin = 5.0f;
		float fontSize = 10.0f;
	    	
		Row[] content = getRows(rows, rollCalls, notices);
	    	
		int numOfRows = content.length;
		for(int offset=0; offset<numOfRows; ) {
			offset += drawTable(content, offset, fontSize, cellMargin);
			closePage();
			if(offset<numOfRows) {
				addPageLandscape();
			}
		}
		
		addPageNumbers(); 
	}
	
	@Override
    public void addPageNumbers() throws IOException {
        float footerFontSize = 10.0f;
    	
		PDPageTree pageTree = document.getPages();
        int numOfPages = pageTree.getCount();
        int i = 0;
        for(Iterator<PDPage> pageIt=pageTree.iterator(); pageIt.hasNext(); ) {
            PDPage page = pageIt.next();
            PDRectangle pageSize = page.getMediaBox();
            
            String text = (++i) + " / " + numOfPages;
            float stringWidth = getStringWidth(text, footerFontSize);
            // calculate to center of the page
            float pageWidth = pageSize.getWidth();
            float x = (pageWidth - stringWidth) / 2.0f;
            float y = (marginTopBottom / 2.0f);
           
            // append the content to the existing stream
            PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true,true);
            
            // set warning
            contentStream.beginText();
            contentStream.setFont(font, footerFontSize );
            contentStream.setTextMatrix(Matrix.getTranslateInstance(marginLeftRight, y + 14));
            contentStream.showText(translator.translate("rollcall.coach.hint.generic"));
            contentStream.endText();
            
            contentStream.beginText();
            // set font and font size
            contentStream.setFont(font, footerFontSize );
            contentStream.setTextMatrix(Matrix.getTranslateInstance(x, y));
            contentStream.showText(text);
            contentStream.endText();
            
            //set current date
            contentStream.beginText();
            contentStream.setFont(font, footerFontSize );
            contentStream.setTextMatrix(Matrix.getTranslateInstance(marginLeftRight, y));
            contentStream.showText(printDate);
            contentStream.endText();
            
            contentStream.close();
        }
    }
		
	private Row[] getRows(List<Identity> rows, List<LectureBlockRollCall> rollCalls, List<AbsenceNotice> notices) {
		int numOfRows = rows.size();
		Map<Identity,LectureBlockRollCall> rollCallMap = new HashMap<>();
		for(LectureBlockRollCall rollCall:rollCalls) {
			rollCallMap.put(rollCall.getIdentity(), rollCall);
		}
	
		Row[] content = new Row[numOfRows];
		for(int i=0; i<numOfRows; i++) {
			Identity row = rows.get(i);
			String fullname = getName(row);
		
			String comment = null;
			boolean authorised = false;
			boolean[] absences = new boolean[numOfLectures];
			Arrays.fill(absences, false);
			
			AbsenceNotice notice = notices.stream()
					.filter(n -> n.getIdentity().equals(row))
					.findFirst().orElse(null);
		
			LectureBlockRollCall rollCall = rollCallMap.get(rows.get(i));
			if(rollCall != null) {
				if(rollCall.getLecturesAbsentList() != null) {
		    		List<Integer> absenceList = rollCall.getLecturesAbsentList();
		    		for(int j=0; j<numOfLectures; j++) {
		    			absences[j] = absenceList.contains(Integer.valueOf(j)) || notice != null;
		    		}
		    	}
		    	authorised = (rollCall.getAbsenceAuthorized() != null && rollCall.getAbsenceAuthorized().booleanValue())
		    			|| (notice != null && notice.getAbsenceAuthorized() != null && notice.getAbsenceAuthorized().booleanValue());
		    	if(StringHelper.containsNonWhitespace(rollCall.getComment())) {
		    		comment = rollCall.getComment();
		    	}
			} else if(notice != null) {
		    	for(int j=0; j<numOfLectures; j++) {
		    		absences[j] = true;
		    	}
		    	authorised = notice.getAbsenceAuthorized() != null && notice.getAbsenceAuthorized().booleanValue();	
			}
			content[i] = new Row(fullname, absences, authorised, comment);
		}
		
		return content;
	}
	
	private String getName(Identity identity) {
		StringBuilder sb = new StringBuilder();
		User user = identity.getUser();
		if(StringHelper.containsNonWhitespace(user.getFirstName())) {
			
			sb.append(user.getFirstName());
		}
		if(StringHelper.containsNonWhitespace(user.getLastName())) {
			if(sb.length() > 0) sb.append(" ");
			sb.append(user.getLastName());
		}

		String institutionalIdentifier = user.getProperty(UserConstants.INSTITUTIONALUSERIDENTIFIER, translator.getLocale());
		if(StringHelper.containsNonWhitespace(institutionalIdentifier)) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(institutionalIdentifier);
		}
		return sb.toString();
	}
    
	public int drawTable(Row[] content, int offset, float fontSize, float cellMargin)
	throws IOException {
	
		float tableWidth = width;
		float rowHeight = (lineHeightFactory * fontSize) + (2 * cellMargin);
		
		float allColWidth = 29f;
		float authorisedColWidth = authorizedAbsenceEnabled ? 29f : 0f;
		float lectureColWidth = 15f;
		float lecturesColWidth =  numOfLectures * lectureColWidth + cellMargin;

		float nameMaxSizeWithMargin = (tableWidth - lecturesColWidth - allColWidth - authorisedColWidth) / 2.0f;
		if(nameMaxSizeWithMargin < 140.0f) {
			nameMaxSizeWithMargin = 140.0f;
		}
		float commentColWidth = tableWidth - lecturesColWidth - allColWidth - authorisedColWidth - nameMaxSizeWithMargin;
		float nameMaxSize = nameMaxSizeWithMargin - (2 * cellMargin);
		
		float availableHeight = currentY - marginTopBottom - rowHeight;
		
		float[] rowHeights = new float[content.length];
		float usedHeight = 0.0f;
		int possibleRows = 0;
		for(int i = offset; i < content.length; i++) {
			String name = content[i].getName();
			float nameWidth = getStringWidth(name, fontSize);
			float nameHeight;
			if(nameWidth > nameMaxSize) {
				nameHeight = rowHeight + (lineHeightFactory * fontSize);
			} else {
				nameHeight = rowHeight;
			}

			if((usedHeight + nameHeight) > availableHeight) {
				break;
			}
			usedHeight += nameHeight;
			rowHeights[i] = nameHeight;
			possibleRows++;
		}
		
		int end = Math.min(offset + possibleRows, content.length);
		int rows = end - offset;
		
		float tableHeight = usedHeight + rowHeight;
	
		// draw the horizontal line of the rows
		float y = currentY;
		float nexty = currentY;
		drawLine(marginLeftRight, nexty, marginLeftRight + tableWidth, nexty, 0.5f);
		nexty -= rowHeight;
		for (int i =offset; i < end; i++) {
			drawLine(marginLeftRight, nexty, marginLeftRight + tableWidth, nexty, 0.5f);
			nexty -= rowHeights[i];
		}
		drawLine(marginLeftRight, nexty, marginLeftRight + tableWidth, nexty, 0.5f);

		// draw the vertical line of the columns
		float nextx = marginLeftRight;
		drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
		nextx += nameMaxSizeWithMargin;
		drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
		nextx += lecturesColWidth;
		drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
		nextx += allColWidth;
		drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
		if(authorizedAbsenceEnabled) {
			nextx += authorisedColWidth;
			drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
		}
		nextx += commentColWidth;
		drawLine(nextx, y, nextx, y - tableHeight, 0.5f);

		// now add the text
		// draw the headers
		final float textx = marginLeftRight + cellMargin;
		float texty = currentY;
		{
			float headerY = texty - rowHeight + (2 * cellMargin);
			float headerX = textx;
			
			currentContentStream.beginText();
			currentContentStream.setFont(fontBold, fontSize);
			currentContentStream.newLineAtOffset(headerX, headerY);
			currentContentStream.showText(translator.translate("pdf.table.header.participants"));
			currentContentStream.endText();
			
			headerX += nameMaxSizeWithMargin;
			for(int i=0; i<numOfLectures; i++) {
				currentContentStream.beginText();
				currentContentStream.setFont(fontBold, fontSize);
				currentContentStream.newLineAtOffset(headerX, headerY);
				currentContentStream.showText(Integer.toString(i+1));
				currentContentStream.endText();
				headerX += lectureColWidth;
			}
			
			headerX += cellMargin;
			currentContentStream.beginText();
			currentContentStream.setFont(fontBold, fontSize);
			currentContentStream.newLineAtOffset(headerX, headerY);
			currentContentStream.showText(translator.translate("pdf.table.header.all"));
			currentContentStream.endText();
			headerX += allColWidth;
			
			if(authorizedAbsenceEnabled) {
				currentContentStream.beginText();
				currentContentStream.setFont(fontBold, fontSize);
				currentContentStream.newLineAtOffset(headerX, headerY);
				currentContentStream.showText(translator.translate("pdf.table.header.authorised"));
				currentContentStream.endText();
				headerX += authorisedColWidth;	
			}

			currentContentStream.beginText();
			currentContentStream.setFont(fontBold, fontSize);
			currentContentStream.newLineAtOffset(headerX, headerY);
			currentContentStream.showText(translator.translate("pdf.table.header.comment"));
			currentContentStream.endText();
		}

		currentY -= rowHeight;

		//draw the content
		texty = currentY - 15;
		for (int i=offset; i<end; i++) {
			String text = cleanString(content[i].getName());
			if(text == null) continue;
			
			if(rowHeights[i] > rowHeight + 1) {
				//can do 2 lines
				String[] texts = splitTextInTwo(text, nameMaxSize, fontSize);
				float lineTexty = texty;
				for(int k=0; k<2 && k<texts.length; k++) {
					String textLine = texts[k];
					currentContentStream.beginText();
					currentContentStream.setFont(font, fontSize);
					currentContentStream.newLineAtOffset(textx, lineTexty);
					currentContentStream.showText(textLine);
					currentContentStream.endText();
					lineTexty -= (lineHeightFactory * fontSize);
				}
			} else {
				currentContentStream.beginText();
				currentContentStream.setFont(font, fontSize);
				currentContentStream.newLineAtOffset(textx, texty);
				currentContentStream.showText(text);
				currentContentStream.endText();
			}
			
			float offetSetYTop = 7f;
			float offetSetYBottom = 2f;
			float boxWidth = 9.0f;
			
			//absences check box
			boolean all = true;
			boolean[] absences = content[i].getAbsences();
			float boxx = textx + nameMaxSizeWithMargin;
			for (int j=0; j<absences.length; j++) {
				drawLine(boxx, texty + offetSetYTop, boxx, texty - offetSetYBottom, 0.5f);
				drawLine(boxx, texty - offetSetYBottom, boxx + boxWidth, texty - offetSetYBottom, 0.5f);
				drawLine(boxx, texty + offetSetYTop, boxx + boxWidth, texty + offetSetYTop, 0.5f);
				drawLine(boxx + boxWidth, texty + offetSetYTop, boxx + boxWidth, texty - offetSetYBottom, 0.5f);
				
				if(absences[j]) {
					currentContentStream.beginText();
					currentContentStream.setFont(font, fontSize);
					currentContentStream.newLineAtOffset(boxx + 2f, texty);
					currentContentStream.showText("x");
					currentContentStream.endText();
				}
				all &= absences[j];
				boxx += 15f;
			}

			{// all check box
				boxx += cellMargin;
				float startBoxx = boxx + ((allColWidth - boxWidth - (2 * cellMargin)) / 2);
				drawLine(startBoxx, texty + offetSetYTop, startBoxx, texty - offetSetYBottom, 0.5f);
				drawLine(startBoxx, texty - offetSetYBottom, startBoxx + boxWidth, texty - offetSetYBottom, 0.5f);
				drawLine(startBoxx, texty + offetSetYTop, startBoxx + boxWidth, texty + offetSetYTop, 0.5f);
				drawLine(startBoxx + boxWidth, texty + offetSetYTop, startBoxx + boxWidth, texty - offetSetYBottom, 0.5f);
			
				if(all) {
					currentContentStream.beginText();
					currentContentStream.setFont(font, fontSize);
					currentContentStream.newLineAtOffset(startBoxx + 2f, texty);
					currentContentStream.showText("x");
					currentContentStream.endText();
				}
				boxx += allColWidth;
			}
			
			if(authorizedAbsenceEnabled) {// authorized
				float startBoxx = boxx + ((authorisedColWidth - boxWidth - (2 * cellMargin)) / 2);
				drawLine(startBoxx, texty + offetSetYTop, startBoxx, texty - offetSetYBottom, 0.5f);
				drawLine(startBoxx, texty - offetSetYBottom, startBoxx + boxWidth, texty - offetSetYBottom, 0.5f);
				drawLine(startBoxx, texty + offetSetYTop, startBoxx + boxWidth, texty + offetSetYTop, 0.5f);
				drawLine(startBoxx + boxWidth, texty + offetSetYTop, startBoxx + boxWidth, texty - offetSetYBottom, 0.5f);
			
				if(content[i].isAuthorised()) {
					currentContentStream.beginText();
					currentContentStream.setFont(font, fontSize);
					currentContentStream.newLineAtOffset(startBoxx + 2f, texty);
					currentContentStream.showText("x");
					currentContentStream.endText();
				}
				boxx += authorisedColWidth;
			}
			
			{//comment
				String comment = content[i].getComment();
				if(comment != null) {
					float commentWidth = getStringWidth(comment, fontSize);
					float commentCellWidth = commentColWidth - (2 * cellMargin);
					if(commentWidth > commentCellWidth) {
						commentWidth = getStringWidth(comment, fontSize - 2);
						if(commentWidth > commentCellWidth) {
							//cut
							float numOfChars = comment.length() * (commentColWidth / commentWidth);
							comment = comment.substring(0, Math.round(numOfChars) - 4) + "...";
						}
						currentContentStream.beginText();
						currentContentStream.setFont(font, fontSize - 2);
						currentContentStream.newLineAtOffset(boxx + 2f, texty);
						showTextToStream(comment, currentContentStream);
						currentContentStream.endText();
					} else {
						currentContentStream.beginText();
						currentContentStream.setFont(font, fontSize);
						currentContentStream.newLineAtOffset(boxx + 2f, texty);
						showTextToStream(comment, currentContentStream);
						currentContentStream.endText();
					}
				}
			}
			
			texty -= rowHeights[i];
		}
		return rows;
	}
	
	private static class Row {
		
		private final String name;
		private final boolean[] absences;
		private final boolean authorised;
		private final String comment;
		
		public Row(String name, boolean[] absences, boolean authorised, String comment) {
			this.name = name;
			this.absences = absences;
			this.authorised = authorised;
			this.comment = comment;
		}

		public String getName() {
			return name;
		}

		public boolean[] getAbsences() {
			return absences;
		}

		public boolean isAuthorised() {
			return authorised;
		}
		
		public String getComment() {
			return comment;
		}
	}
}