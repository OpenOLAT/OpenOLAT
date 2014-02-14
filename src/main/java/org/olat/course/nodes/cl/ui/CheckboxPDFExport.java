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

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
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
	
	private static final OLog log = Tracing.createLoggerFor(CheckboxPDFExport.class);
	
	private final String filename;
	private String title;
	private String subject;
	private String objectives;
	private String author;
	private final Translator translator;
	private int firstNameIndex;
	private int lastNameIndex;
	
	public CheckboxPDFExport(String filename, Translator translator, List<UserPropertyHandler> userPropertyHandlers)
			throws IOException {
		super();
		
		marginTopBottom = 62.0f;
		marginLeftRight = 62.0f;
		
		this.filename = filename;
		this.translator = translator;
		
		int i=0;
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			if(UserConstants.LASTNAME.equals(userPropertyHandler.getName())) {
				lastNameIndex = i;
			}
			i++;
		}
		
		int j=0;
		for(UserPropertyHandler userPropertyHandler:userPropertyHandlers) {
			if(UserConstants.FIRSTNAME.equals(userPropertyHandler.getName())) {
				firstNameIndex = j;
			}
			j++;
		}
	}
	
    public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getObjectives() {
		return objectives;
	}

	public void setObjectives(String objectives) {
		this.objectives = objectives;
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
			hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + StringHelper.urlEncodeUTF8(filename));			
			hres.setHeader("Content-Description",StringHelper.urlEncodeUTF8(filename));
			document.save(hres.getOutputStream());
		} catch (COSVisitorException | IOException e) {
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

	public void create(CheckboxList checkboxList, CheckListAssessmentDataModel dataModel)
    throws IOException, COSVisitorException, TransformerException {
    	addPage();
    	addMetadata(title, subject, author);
    	if(StringHelper.containsNonWhitespace(objectives)) {
    		addParagraph(objectives, 10, width);
    	}
    	
    	float cellMargin = 5.0f;
    	
    	float headerMaxSize = 0.0f;
    	float fontSize = 10.0f;
    	for(Checkbox box:checkboxList.getList()) {
    		headerMaxSize = Math.max(headerMaxSize, getStringWidth(box.getTitle(), fontSize));
    	}
    	
    	String[] headers = getHeaders(checkboxList);
    	String[][] content = getRows(checkboxList, dataModel);
    	
    	float nameMaxSize = 0.0f;
    	for(String[] row:content) {
    		nameMaxSize = Math.max(nameMaxSize, getStringWidth(row[0], fontSize));
    	}
    	
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
	
	private String[][] getRows(CheckboxList checkboxList, CheckListAssessmentDataModel dataModel) {
		List<CheckListAssessmentRow> rows = dataModel.getBackedUpRows();
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
		sb.append(view.getIdentityProp(lastNameIndex))
		  .append(", ")
		  .append(view.getIdentityProp(firstNameIndex));
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
    
	public int drawTable(String[] headers, String[][] content, int offset, float maxHeaderSize, float nameMaxSize, float fontSize, float cellMargin)
	throws IOException {
	
		float tableWidth = width;
		int cols = content[0].length;

		float headerHeight = maxHeaderSize + (2*cellMargin);
		float rowHeight = (lineHeightFactory * fontSize) + (2 * cellMargin);
		nameMaxSize += (2 * cellMargin);
		
		float availableHeight = currentY - marginTopBottom - headerHeight;
		float numOfAvailableRows = availableHeight / rowHeight;
		int possibleRows = Math.round(numOfAvailableRows);
		int end = Math.min(offset + possibleRows, content.length);
		int rows = end - offset;
		
		float tableHeight = (rowHeight * rows) + headerHeight;
		float colWidth = (tableWidth - (100 + nameMaxSize)) / (float) (cols - 2.0f);

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
		drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
		nextx += nameMaxSize;
		for (int i=1; i<=cols-2; i++) {
			drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
			nextx += colWidth;
		}
		drawLine(nextx, y, nextx, y - tableHeight, 0.5f);
		nextx += 100;
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
				currentContentStream.moveTextPositionByAmount(textx, texty - headerHeight + cellMargin);
				currentContentStream.drawString(text);
				textx += nameMaxSize;
			} else {
				currentContentStream.setTextRotation(3 * (Math.PI / 2), textx + cellMargin, texty - cellMargin);
				currentContentStream.drawString(text);
				textx += colWidth;
			}
			currentContentStream.endText();
		}

		currentY -= headerHeight;

		textx = marginLeftRight + cellMargin;
		texty = currentY - 15;
		for (int i=offset; i<end; i++) {
			String[] rowContent = content[i];
			if(rowContent == null) continue;
			
			for (int j = 0; j < cols; j++) {
				String text = rowContent[j];
				if(text != null) {
					currentContentStream.beginText();
					currentContentStream.setFont(font, fontSize);
					currentContentStream.moveTextPositionByAmount(textx, texty);
					currentContentStream.drawString(text);
					currentContentStream.endText();
				}
				textx += (j==0 ? 100 : colWidth);
			}
			texty -= rowHeight;
			textx = marginLeftRight + cellMargin;
		}
		return rows;
	}
}