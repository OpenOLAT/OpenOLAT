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
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.util.Matrix;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.pdf.PdfDocument;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 12.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckboxPDFExport extends PdfDocument implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(CheckboxPDFExport.class);
	
	private final String filename;
	private String courseTitle;
	private String courseNodeTitle;
	private String groupName;
	private String author;
	private final Translator translator;
	private int firstNameIndex;
	private int lastNameIndex;
	private int institutionalUserIdentifierIndex;
	
	public CheckboxPDFExport(String filename, Translator translator, List<UserPropertyHandler> userPropertyHandlers) {
		super(translator.getLocale());
		
		marginTopBottom = 62.0f;
		marginLeftRight = 62.0f;
		
		this.filename = filename;
		this.translator = translator;
		
		lastNameIndex = findPropertyIndex(UserConstants.LASTNAME, userPropertyHandlers);
		firstNameIndex = findPropertyIndex(UserConstants.FIRSTNAME, userPropertyHandlers);
		institutionalUserIdentifierIndex = findPropertyIndex(UserConstants.INSTITUTIONALUSERIDENTIFIER, userPropertyHandlers);
	}
	
	private int findPropertyIndex(String propertyName, List<UserPropertyHandler> userPropertyHandlers) {
		int i=0;
		int index = -1;
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			if(propertyName.equals(userPropertyHandler.getName())) {
				index = i;
			}
			i++;
		}
		return index;
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

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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
			hres.setHeader("Content-Description", StringHelper.transformDisplayNameToFileSystemName(filename));
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
    	addPage();
    	addMetadata(courseNodeTitle, courseTitle, author);

    	if(StringHelper.containsNonWhitespace(courseTitle)) {
    		addParagraph(courseTitle, 16, true, width);
    	}
    	if(StringHelper.containsNonWhitespace(courseNodeTitle)) {
    		addParagraph(courseNodeTitle, 14, true, width);
    	}
    	if(StringHelper.containsNonWhitespace(groupName)) {
    		String prefix = translator.translate("participants");
    		addParagraph(prefix + ": " + groupName, 14, true, width);
    	}
    	
    	float cellMargin = 5.0f;
    	
    	float headerMaxSize = 0.0f;
    	float fontSize = 10.0f;
    	for(Checkbox box:checkboxList.getList()) {
    		headerMaxSize = Math.max(headerMaxSize, getStringWidth(box.getTitle(), fontSize));
    	}
    	
    	String[] headers = getHeaders(checkboxList);
    	String[][] content = getRows(checkboxList, rows);
    	
    	float nameMaxSize = 0.0f;
    	for(String[] row:content) {
    		float nameWidth = getStringWidth(row[0], fontSize);
    		nameMaxSize = Math.max(nameMaxSize, nameWidth);
    	}
    	nameMaxSize = Math.min(nameMaxSize, 150f);
    	
    	int numOfRows = content.length;
    	for(int offset=0; offset<numOfRows; ) {
    		offset += drawTable(headers, content, offset, headerMaxSize, nameMaxSize, fontSize, cellMargin);
    		closePage();
        	if(offset<numOfRows) {
        		addPage();
        	}
    	}
    	
    	addPageNumbers(); 
    }
	
	private String[][] getRows(CheckboxList checkboxList, List<CheckListAssessmentRow> rows) {
		int numOfRows = rows.size();
		List<Checkbox> boxList = checkboxList.getList();
    	int numOfCheckbox = boxList.size();
    	
    	String[][] content = new String[numOfRows][];
    	for(int i=0; i<numOfRows; i++) {
    		CheckListAssessmentRow row = rows.get(i);
    		content[i] = new String[numOfCheckbox + 2];
        	content[i][0] = getName(row);
        	for(int j=0; j<numOfCheckbox; j++) {
        		Boolean[] checked = row.getChecked();
        		if(checked != null && j >= 0 && j < checked.length) {
    				Boolean check = checked[j];
    				if(check != null && check.booleanValue()) {
    					content[i][j+1] = "x";
    				}
    			}

        	}
    	}
    	
    	return content;
	}
	
	private String getName(CheckListAssessmentRow view) {
		StringBuilder sb = new StringBuilder();
		if(lastNameIndex >= 0) {
			sb.append(view.getIdentityProp(lastNameIndex));
		}
		if(firstNameIndex >= 0) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(view.getIdentityProp(firstNameIndex));
		}
		if(institutionalUserIdentifierIndex >= 0) {
			String val = view.getIdentityProp(institutionalUserIdentifierIndex);
			if(StringHelper.containsNonWhitespace(val)) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(val);
			}
		}
		return sb.toString();
	}
	
	private String[] getHeaders(CheckboxList checkboxList) {
    	int numOfCheckbox = checkboxList.getList().size();
		String[] headers = new String[numOfCheckbox + 2];
    	headers[0] = translator.translate("participants");
    	int pos = 1;
    	for(Checkbox box:checkboxList.getList()) {
    		headers[pos++] = box.getTitle();
    	}
    	headers[numOfCheckbox + 1] = translator.translate("signature");
    	return headers;
	}
    
	public int drawTable(String[] headers, String[][] content, int offset,
			float maxHeaderSize, float nameMaxSize, float fontSize, float cellMargin)
	throws IOException {
	
		float tableWidth = width;
		int cols = content[0].length;

		float headerHeight = maxHeaderSize + (2*cellMargin);
		float rowHeight = (lineHeightFactory * fontSize) + (2 * cellMargin);
		float nameMaxSizeWithMargin = nameMaxSize + (2 * cellMargin);
		
		float availableHeight = currentY - marginTopBottom - headerHeight;
		
		
		float[] rowHeights = new float[content.length];
		float usedHeight = 0.0f;
		int possibleRows = 0;
		for(int i = offset; i < content.length; i++) {
			String[] names = content[i];
			
			String name = names[0];
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
		
		float tableHeight = usedHeight + headerHeight;
		float colWidth = (tableWidth - (100 + nameMaxSizeWithMargin)) / (cols - 2.0f);

		// draw the horizontal line of the rows
		float y = currentY;
		float nexty = currentY;
		drawLine(marginLeftRight, nexty, marginLeftRight + tableWidth, nexty, 0.5f);
		nexty -= headerHeight;
		for (int i =offset; i < end; i++) {
			drawLine(marginLeftRight, nexty, marginLeftRight + tableWidth, nexty, 0.5f);
			nexty -= rowHeights[i];
		}
		drawLine(marginLeftRight, nexty, marginLeftRight + tableWidth, nexty, 0.5f);

		// draw the vertical line of the columns
		float nextx = marginLeftRight;
		drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
		nextx += nameMaxSizeWithMargin;
		for (int i=1; i<=cols-2; i++) {
			drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
			nextx += colWidth;
		}
		drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
		nextx += 100; // signature
		drawLine(nextx, y, nextx, y - tableHeight, 0.5f);

		// now add the text
		
		// draw the headers
		float textx = marginLeftRight + cellMargin;
		float texty = currentY;
		int lastColIndex = cols -1;
		for (int h=0; h<cols; h++) {
			String text = headers[h];
			if(text == null) {
				text = "";
			}
			currentContentStream.beginText();
			currentContentStream.setFont(font, fontSize);
			if (h == 0 || (h == lastColIndex)) {
				currentContentStream.newLineAtOffset(textx, texty - headerHeight + cellMargin);
				textx += nameMaxSizeWithMargin;
			} else {
				currentContentStream.setTextMatrix(Matrix.getRotateInstance(3 * (Math.PI / 2), textx + cellMargin, texty - cellMargin));
				textx += colWidth;
			}
			showTextToStream(text, currentContentStream);
			currentContentStream.endText();
		}

		currentY -= headerHeight;

		//draw the content
		textx = marginLeftRight + cellMargin;
		texty = currentY - 15;
		for (int i=offset; i<end; i++) {
			String[] rowContent = content[i];
			if(rowContent == null) continue;
			
			for (int j = 0; j < cols; j++) {
				String text = rowContent[j];
				float cellWidth = (j==0 ? nameMaxSizeWithMargin : colWidth);
				float textWidth = (j==0 ? nameMaxSize : colWidth);
				if(text != null) {
					if(rowHeights[i] > rowHeight + 1) {
						//can do 2 lines
						String[] texts = splitText(text, textWidth, fontSize);
						float lineTexty = texty;
						for(int k=0; k<2 && k<texts.length; k++) {
							String textLine = texts[k];
							currentContentStream.beginText();
							currentContentStream.setFont(font, fontSize);
							currentContentStream.newLineAtOffset(textx, lineTexty);
							showTextToStream(textLine, currentContentStream);
							currentContentStream.endText();
							lineTexty -= (lineHeightFactory * fontSize);
						}
					} else {
						currentContentStream.beginText();
						currentContentStream.setFont(font, fontSize);
						currentContentStream.newLineAtOffset(textx, texty);
						showTextToStream(text, currentContentStream);
						currentContentStream.endText();
					}
				}
				textx += cellWidth;
			}
			texty -= rowHeights[i];
			textx = marginLeftRight + cellMargin;
		}
		return rows;
	}
}