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
package org.olat.course.nodes.cl.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.pdf.PdfDocument;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 17.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckedPDFExport extends PdfDocument implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(CheckedPDFExport.class);
	
	private final String filename;
	private String courseTitle;
	private String courseNodeTitle;
	private String author;
	private final boolean withScore;
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private int firstNameIndex;
	private int lastNameIndex;
	private int institutionalUserIdentifierIndex;
	
	private int numOfCols = 0;
	
	public CheckedPDFExport(String filename, Translator translator,
			boolean withScore, List<UserPropertyHandler> userPropertyHandlers)
			throws IOException {
		super(translator.getLocale());
		
		marginTopBottom = 62.0f;
		marginLeftRight = 62.0f;
		
		this.filename = filename;
		this.translator = translator;
		this.withScore = withScore;
		this.userPropertyHandlers = userPropertyHandlers;
		
		lastNameIndex = findPropertyIndex(UserConstants.LASTNAME, userPropertyHandlers);
		firstNameIndex = findPropertyIndex(UserConstants.FIRSTNAME, userPropertyHandlers);
		institutionalUserIdentifierIndex = findPropertyIndex(UserConstants.INSTITUTIONALUSERIDENTIFIER, userPropertyHandlers);
	}
	
	private int findPropertyIndex(String propertyName, List<UserPropertyHandler> userPropHandlers) {
		int i=0;
		int index = -1;
		for(UserPropertyHandler userPropertyHandler:userPropHandlers) {
			if(propertyName.equals(userPropertyHandler.getName())) {
				index = i;
				numOfCols++;
			}
			i++;
		}
		return index;
	}
	
	private String findHeader(String propertyName, List<UserPropertyHandler> userPropHandlers) {
		String header = null;
		for(UserPropertyHandler userPropertyHandler:userPropHandlers) {
			if(propertyName.equals(userPropertyHandler.getName())) {
				header = translator.translate(userPropertyHandler.i18nColumnDescriptorLabelKey());
			}
		}
		return header;
	}

	public String getCourseTitle() {
		return courseTitle;
	}

	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	public String getCourseNodeTitle() {
		return courseNodeTitle;
	}

	public void setCourseNodeTitle(String courseNodeTitle) {
		this.courseNodeTitle = courseNodeTitle;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
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
			hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + StringHelper.transformDisplayNameToFileSystemName(filename) + ".pdf");			
			hres.setHeader("Content-Description",StringHelper.transformDisplayNameToFileSystemName(filename));
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

	public void create(CheckboxList checkboxList, List<CheckListAssessmentRow> rows)
    throws IOException, TransformerException {
		addMetadata(courseNodeTitle, courseTitle, author);
		
		int i=0;
		for(Checkbox checkbox:checkboxList.getList()) {
			create(checkbox, i++, rows);
		}

    	addPageNumbers(); 
	}
		
	private void create(Checkbox checkbox, int checkboxIndex, List<CheckListAssessmentRow> rows)
	throws IOException {
		addPage();
		
    	if(StringHelper.containsNonWhitespace(courseTitle)) {
    		addParagraph(courseTitle, 16, true, width);
    	}
    	if(StringHelper.containsNonWhitespace(courseNodeTitle)) {
    		addParagraph(courseNodeTitle, 14, true, width);
    	}

		String text = checkbox.getTitle();
    	if(StringHelper.containsNonWhitespace(text)) {
    		if(withScore && checkbox.getPoints() != null) {
    			String[] points = new String[]{ Float.toString(checkbox.getPoints().floatValue() )};
    			text += " " + translator.translate("box.points.info", points);
    		}
    		addParagraph(text, 12f, true, width);
    	}
		
		String description = checkbox.getDescription();
    	if(StringHelper.containsNonWhitespace(description)) {
    		description = FilterFactory.getHtmlTagAndDescapingFilter().filter(description);
    		addParagraph(description, 10f, width);
    	}
    	
    	String msg = translator.translate("done.by");
    	addParagraph(msg, 10f, width);
    	
    	float cellMargin = 5f;
    	float fontSize = 10f;

    	String[] headers = getHeaders();
    	
    	List<CheckListAssessmentRow> content = getRows(checkboxIndex, rows);
    	int numOfRows = content.size();
    	if(numOfRows == 0) {
    		closePage();
    	} else {
	    	for(int offset=0; offset<numOfRows; ) {
	    		offset += drawTable(headers, content, offset, fontSize, cellMargin);
	    		closePage();
	        	if(offset<numOfRows) {
	        		addPage();
	        	}
	    	}
    	}
    }
	
	private List<CheckListAssessmentRow> getRows(int checkboxIndex, List<CheckListAssessmentRow> rows) {
		int numOfRows = rows.size();
    	
		List<CheckListAssessmentRow> filteredRows = new ArrayList<>(rows.size());
    	for(int i=0; i<numOfRows; i++) {
    		CheckListAssessmentRow row = rows.get(i);
    		Boolean[] checks = row.getChecked();
    		if(checks != null && checks.length > checkboxIndex
    				&& checks[checkboxIndex] != null && checks[checkboxIndex].booleanValue()) {
    			filteredRows.add(row);
    		}
    	}
    	
    	return filteredRows;
	}
	
	private String[] getHeaders() {
		List<String> headers = new ArrayList<>();
		if(firstNameIndex >= 0) {
			headers.add(findHeader(UserConstants.FIRSTNAME, userPropertyHandlers));
		}
		if(lastNameIndex >= 0) {
			headers.add(findHeader(UserConstants.LASTNAME, userPropertyHandlers));
		}
		if(institutionalUserIdentifierIndex >= 0) {
			headers.add(findHeader(UserConstants.INSTITUTIONALUSERIDENTIFIER, userPropertyHandlers));
		}

    	return headers.toArray(new String[headers.size()]);
	}
    
	public int drawTable(String[] headers, List<CheckListAssessmentRow> content, int offset, float fontSize, float cellMargin)
	throws IOException {
	
		float tableWidth = width;
		float rowHeight = (lineHeightFactory * fontSize) + (2 * cellMargin);
		float headerHeight = rowHeight;

		float availableHeight = currentY - marginTopBottom - headerHeight;
		float numOfAvailableRows = availableHeight / rowHeight;
		int possibleRows = Math.round(numOfAvailableRows);
		int end = Math.min(offset + possibleRows, content.size());
		int rows = end - offset;
		
		float tableHeight = (rowHeight * rows) + headerHeight;
		float colWidth = tableWidth / numOfCols;

		// draw the rows
		float y = currentY;
		float nexty = currentY;
		drawLine(marginLeftRight, nexty, marginLeftRight + tableWidth, nexty, 0.5f);
		nexty -= headerHeight;
		for (int i = 0; i <= rows; i++) {
			drawLine(marginLeftRight, nexty, marginLeftRight + tableWidth, nexty, 0.5f);
			nexty -= rowHeight;
		}

		// draw the columns
		float nextx = marginLeftRight;
		for (int i=0; i<=numOfCols; i++) {
			drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
			nextx += colWidth;
		}

		// now add the text
		
		// draw the headers
		float textx = marginLeftRight + cellMargin;
		float texty = currentY;
		for (int h=0; h<numOfCols; h++) {
			String text = headers[h];
			if(text == null) {
				text = "";
			}
			currentContentStream.beginText();
			currentContentStream.setFont(font, fontSize);
			currentContentStream.newLineAtOffset(textx, texty - headerHeight + cellMargin);
			showTextToStream(text, currentContentStream);
			currentContentStream.endText();
			textx += colWidth;
		}

		currentY -= headerHeight;

		textx = marginLeftRight + cellMargin;
		texty = currentY - 15;
		for (int i=offset; i<end; i++) {
			CheckListAssessmentRow rowContent = content.get(i);
			if(rowContent == null) continue;
			
			if(firstNameIndex >= 0) {
				String text = rowContent.getIdentityProp(firstNameIndex);
				drawTextAtMovedPositionByAmount(text, fontSize, textx, texty);
				textx += colWidth;
			}
			
			if(lastNameIndex >= 0) {
				String text = rowContent.getIdentityProp(lastNameIndex);
				drawTextAtMovedPositionByAmount(text, fontSize, textx, texty);
				textx += colWidth;
			}
			
			if(institutionalUserIdentifierIndex >= 0) {
				String text = rowContent.getIdentityProp(institutionalUserIdentifierIndex);
				drawTextAtMovedPositionByAmount(text, fontSize, textx, texty);
				textx += colWidth;
			}
			
			texty -= rowHeight;
			textx = marginLeftRight + cellMargin;
		}
		return rows;
	}
}