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
package org.olat.core.util.openxml;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.openxml.workbookstyle.Alignment;
import org.olat.core.util.openxml.workbookstyle.Border;
import org.olat.core.util.openxml.workbookstyle.CellStyle;
import org.olat.core.util.openxml.workbookstyle.Fill;
import org.olat.core.util.openxml.workbookstyle.Font;
import org.olat.core.util.openxml.workbookstyle.Font.FontStyle;

/**
 * 
 * Initial date: 21.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenXMLWorkbookStyles {

	public static final String PERCENT_FORMAT = "10";
	public static final String DATE_FORMAT = "14";
	public static final String TIME_FORMAT = "21";
	public static final String DATE_TIME_FORMAT = "22";

	private List<Font> fonts = new ArrayList<>();
	private List<Fill> fills = new ArrayList<>();
	private List<Border> borders = new ArrayList<>();
	
	private List<CellStyle> cellXfs = new ArrayList<>();
	
	private final Font boldFont;
	private final Font standardFont;
	
	private final Fill noneFile;
	private final Fill gray125Fill;
	private final Fill correctFill;
	private final Fill lightGrayFill;
	
	private final Border noBorder;
	private final Border borderTop;
	private final Border borderRight;
	
	/**
	 * Definition of a standard style which is at index 0
	 */
	private final CellStyle standardStyle;//at place zero
	private final CellStyle borderRightStyle;
	private final CellStyle dateStyle;
	private final CellStyle durationStyle;
	private final CellStyle dateTimeStyle;
	private final CellStyle headerStyle;
	private final CellStyle correctStyle;
	private final CellStyle percentStyle;
	private final CellStyle lightGrayStyle;
	private final CellStyle topAlignStyle;
	private final CellStyle bottomAlignStyle;

	public OpenXMLWorkbookStyles() {
		standardFont = new Font(fonts.size(), "12", "1", "Calibri", "2", "minor", FontStyle.none);
		fonts.add(standardFont);
		boldFont = new Font(fonts.size(), "12", "1", "Calibri", "2", "minor", FontStyle.bold);
		fonts.add(boldFont);
		
		noneFile = new Fill(fills.size(), "none");
		fills.add(noneFile);
		gray125Fill = new Fill(fills.size(), "gray125");
		fills.add(gray125Fill);
		lightGrayFill = new Fill(fills.size(), "solid", "EFEFEFEF", "64");
		fills.add(lightGrayFill);
		correctFill = new Fill(fills.size(), "solid", "FFC3FFC0", "64");
		fills.add(correctFill);
		
		noBorder = new Border(borders.size());
		borders.add(noBorder);
		borderRight = new Border(borders.size(), "thin", null, null, null);
		borders.add(borderRight);
		borderTop = new Border(borders.size(), "thin", null, null, null);
		borders.add(borderTop);
		
		standardStyle = new CellStyle(cellXfs.size(), "0", standardFont, noneFile, noBorder, null, null, null, null);
		cellXfs.add(standardStyle);
		borderRightStyle = new CellStyle(cellXfs.size(), "0", standardFont, noneFile, borderRight, null, null, null, null);
		cellXfs.add(borderRightStyle);
		dateStyle = new CellStyle(cellXfs.size(), DATE_FORMAT, standardFont, noneFile, noBorder, null, "1", null, null);
		cellXfs.add(dateStyle);
		dateTimeStyle = new CellStyle(cellXfs.size(), DATE_TIME_FORMAT, standardFont, noneFile, noBorder, null, "1", null, null);
		cellXfs.add(dateTimeStyle);
		durationStyle = new CellStyle(cellXfs.size(), TIME_FORMAT, standardFont, noneFile, borderRight, null, "1", null, null);
		cellXfs.add(durationStyle);
		headerStyle = new CellStyle(cellXfs.size(), "0", boldFont, noneFile, noBorder, null, null, null, null);
		cellXfs.add(headerStyle);
		correctStyle = new CellStyle(cellXfs.size(), "0", boldFont, correctFill, noBorder, null, null, null, null);
		cellXfs.add(correctStyle);
		percentStyle = new CellStyle(cellXfs.size(), PERCENT_FORMAT, standardFont, noneFile, borderRight, null, "1", null, null);
		cellXfs.add(percentStyle);
		lightGrayStyle = new CellStyle(cellXfs.size(), "0", standardFont, lightGrayFill, borderRight, null, null, null, null);
		cellXfs.add(lightGrayStyle);
		Alignment topAlign = new Alignment(Alignment.TOP, "1");
		topAlignStyle = new CellStyle(cellXfs.size(), "0", standardFont, noneFile, noBorder, null, null, topAlign, "1");
		cellXfs.add(topAlignStyle);
		Alignment bottomAlign = new Alignment(Alignment.BOTTOM, "1");
		bottomAlignStyle = new CellStyle(cellXfs.size(), "0", standardFont, noneFile, noBorder, null, null, bottomAlign, "1");
		cellXfs.add(bottomAlignStyle);
	}
	
	public CellStyle getBorderRightStyle() {
		return borderRightStyle;
	}
	
	/**
	 * Standard date format
	 * @return
	 */
	public CellStyle getDateStyle() {
		return dateStyle;
	}
	
	public CellStyle getDateTimeStyle() {
		return dateTimeStyle;
	}
	
	public CellStyle getDurationStyle() {
		return durationStyle;
	}
	
	public CellStyle getHeaderStyle() {
		return headerStyle;
	}
	
	public CellStyle getCorrectStyle() {
		return correctStyle;
	}
	
	public CellStyle getPercentStyle() {
		return percentStyle;
	}
	
	public CellStyle getLightGrayStyle() {
		return lightGrayStyle;
	}
	
	public CellStyle getTopAlignStyle() {
		return topAlignStyle;
	}

	public CellStyle getBottomAlignStyle() {
		return bottomAlignStyle;
	}

	public List<Font> getFonts() {
		return fonts;
	}
	
	public List<Fill> getFills() {
		return fills;
	}
	
	public List<Border> getBorders() {
		return borders;
	}
	
	public List<CellStyle> getCellXfs() {
		return cellXfs;
	}
}
