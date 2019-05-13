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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.workbookstyle.CellStyle;

/**
 * 
 * Initial date: 21.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenXMLWorksheet {
	
	private static final Logger log = Tracing.createLoggerFor(OpenXMLWorksheet.class);
	
	static private char[] COLUMNS;
	static {
		COLUMNS = new char[26];
		for (int i = 0; i < COLUMNS.length; i++) {
			COLUMNS[i] = (char) (i + 'A');
		}
	}
	
	private String id;
	private final OpenXMLWorkbook workbook;
	private final ZipOutputStream zout;
	private XMLStreamWriter writer;
	
	private int headerRows = 0;
	private boolean opened = false;
	private final Calendar cal = Calendar.getInstance();
	
	private Row row;
	private int rowPosition = 0;
	private Map<Integer,Integer> columnsWidth = new HashMap<>();
	
	public OpenXMLWorksheet(String id, OpenXMLWorkbook workbook, ZipOutputStream zout) {
		this.id = id;
		this.zout = zout;
		this.workbook = workbook;
	}
	
	public String getId() {
		return id;
	}

	public int getHeaderRows() {
		return headerRows;
	}

	public void setHeaderRows(int headerRows) {
		this.headerRows = headerRows;
	}
	
	public void setColumnWidth(int pos, int width) {
		columnsWidth.put(pos, width);
	}
	
	public Row newRow() {
		if(!opened) {
			appendProlog();
			opened = true;
		}
		if(row != null) {
			appendRow();
			row = null;
		}
		row = new Row();
		rowPosition++;
		return row;
	}
	
	
	protected void close() {
		if(!opened) {
			appendProlog();
		}
		if(row != null) {
			appendRow();
			row = null;
		}
		appendAfterlog();
	}
	
	private void appendRow() {
		if(row == null || row.isEmpty()) return;
		
		try {
			String rowId = Integer.toString(rowPosition);
			writer.writeStartElement("row");
			writer.writeAttribute("r", rowId);

			int numOfCols = row.size();
			for(int j=0; j<numOfCols; j++) {
				Cell cell = row.getCell(j);
				if(cell != null && cell.getValue() != null) {
					writer.writeStartElement("c");
					writer.writeAttribute("r", getColumn(j).concat(rowId));
					
					CellStyle style = cell.getStyle();
					if(style != null && style.getIndex() > 0) {
						writer.writeAttribute("s", Integer.toString(style.getIndex()));
					}
					if(cell.getType() == OpenXMLCellType.sharedString) {
						writer.writeAttribute("t", "s");
					}
					
					writer.writeStartElement("v");
					if(cell.getType() == OpenXMLCellType.date) {
						cal.setTime((Date)cell.getValue());
						double val = internalGetExcelDate(cal, false);
						writer.writeCharacters(Double.toString(val));
					} else {
						writer.writeCharacters(cell.getValue().toString());
					}
					
					writer.writeEndElement();// end v
					writer.writeEndElement();// end c
				}
			}
			writer.writeEndElement();//end row
		} catch (XMLStreamException e) {
			log.error("", e);
		}
	}
	
	/*
	<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
	<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
	  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
	  xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
	  mc:Ignorable="x14ac" xmlns:x14ac="http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac">
		<dimension ref="A1:D4" />
		<sheetViews>
			<sheetView tabSelected="1" workbookViewId="0">
				<selection activeCell="E3" sqref="E3" />
				<pane topLeftCell="A2" xSplit="0" ySplit="2" activePane="bottomLeft" state="frozen" />
			</sheetView>
		</sheetViews>
		<sheetFormatPr baseColWidth="10" defaultRowHeight="15" x14ac:dyDescent="0" />
		<sheetData>
			
		</sheetData>
		<pageMargins left="0.75" right="0.75" top="1" bottom="1" header="0.5" footer="0.5" />
		<extLst>
			<ext uri="{64002731-A6B0-56B0-2670-7721B7C09600}" xmlns:mx="http://schemas.microsoft.com/office/mac/excel/2008/main">
				<mx:PLV Mode="0" OnePage="0" WScale="0" />
			</ext>
		</extLst>
	</worksheet>
*/
	private void appendProlog() {
		try {
			writer = OpenXMLUtils.createStreamWriter(zout);
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("worksheet");
			writer.writeNamespace("", "http://schemas.openxmlformats.org/spreadsheetml/2006/main");
			writer.writeNamespace("r", "http://schemas.openxmlformats.org/officeDocument/2006/relationships");
			writer.writeNamespace("mc", "http://schemas.openxmlformats.org/markup-compatibility/2006");
			writer.writeAttribute("mc:Ignorable", "x14ac");
			writer.writeNamespace("x14ac", "http://schemas.microsoft.com/office/spreadsheetml/2009/9/ac");
			
			//sheetViews
			writer.writeStartElement("sheetViews");
			writer.writeStartElement("sheetView");
			writer.writeAttribute("tabSelected", "1");
			writer.writeAttribute("workbookViewId", "0");
			
			//header rows
			if(getHeaderRows() > 0) {
				writer.writeStartElement("pane");
				writer.writeAttribute("topLeftCell", "A" + (getHeaderRows() + 1));
				writer.writeAttribute("xSplit", "0");
				writer.writeAttribute("ySplit", Integer.toString(getHeaderRows()));
				writer.writeAttribute("activePane", "bottomLeft");
				writer.writeAttribute("state", "frozen");
				writer.writeEndElement();
			}
			writer.writeStartElement("selection");
			writer.writeAttribute("activeCell", "A1");
			writer.writeAttribute("sqref", "A1");
			writer.writeEndElement();
			
			writer.writeEndElement();//end sheetView
			writer.writeEndElement();// end sheetViews
			
			//sheet format
			writer.writeStartElement("sheetFormatPr");
			writer.writeAttribute("baseColWidth", "10");
			writer.writeAttribute("defaultRowHeight", "15");
			writer.writeAttribute("x14ac:dyDescent", "0");
			writer.writeEndElement();
			
			if(columnsWidth != null && columnsWidth.size() > 0) {
				writer.writeStartElement("cols");
				for(Map.Entry<Integer, Integer> columnWidth:columnsWidth.entrySet()) {
					Integer pos = columnWidth.getKey();
					Integer width = columnWidth.getValue();
					writer.writeStartElement("col");
					writer.writeAttribute("min", pos.toString());
					writer.writeAttribute("max", pos.toString());
					writer.writeAttribute("width", width.toString());
					writer.writeAttribute("customWidth", "1");
					writer.writeEndElement();
				}
				writer.writeEndElement();
			}

			writer.writeStartElement("sheetData");
		} catch(XMLStreamException e) {
			log.error("", e);
		}
	}
	
	private void appendAfterlog() {
		try {
			writer.writeEndElement();//end sheetData
			
			//page margins
			writer.writeStartElement("pageMargins");
			writer.writeAttribute("left", "0.75");
			writer.writeAttribute("right", "0.75");
			writer.writeAttribute("top", "1");
			writer.writeAttribute("bottom", "1");
			writer.writeAttribute("header", "0.5");
			writer.writeAttribute("footer", "0.5");
			writer.writeEndElement();

			//extLst
			writer.writeStartElement("extLst");
			writer.writeStartElement("ext");
			writer.writeAttribute("uri", "{64002731-A6B0-56B0-2670-7721B7C09600}");
			writer.writeNamespace("mx", "http://schemas.microsoft.com/office/mac/excel/2008/main");
			writer.writeStartElement("mx:PLV");
			writer.writeAttribute("Mode", "0");
			writer.writeAttribute("OnePage", "0");
			writer.writeAttribute("WScale", "0");
			writer.writeEndElement();
			writer.writeEndElement();
			
			writer.writeEndElement();// end worksheet
			writer.writeEndDocument();
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			log.error("", e);
		}
	}
	
	public static final int SECONDS_PER_DAY = (24 * 60 * 60);
    public static final long DAY_MILLISECONDS = SECONDS_PER_DAY * 1000L;
	
    private static double internalGetExcelDate(Calendar date, boolean use1904windowing) {
        // Because of daylight time saving we cannot use
        //     date.getTime() - calStart.getTimeInMillis()
        // as the difference in milliseconds between 00:00 and 04:00
        // can be 3, 4 or 5 hours but Excel expects it to always
        // be 4 hours.
        // E.g. 2004-03-28 04:00 CEST - 2004-03-28 00:00 CET is 3 hours
        // and 2004-10-31 04:00 CET - 2004-10-31 00:00 CEST is 5 hours
        double fraction = (((date.get(Calendar.HOUR_OF_DAY) * 60 + date.get(Calendar.MINUTE)) * 60 + date.get(Calendar.SECOND)) * 1000 + date.get(Calendar.MILLISECOND))
        		/ ( double ) DAY_MILLISECONDS;
        Calendar calStart = dayStart(date);
        double value = fraction + absoluteDay(calStart, use1904windowing);

        if (!use1904windowing && value >= 60) {
            value++;
        } else if (use1904windowing) {
            value--;
        }

        return value;
    }
    
    private static Calendar dayStart(final Calendar cal){
        cal.get(Calendar.HOUR_OF_DAY);   // force recalculation of internal fields
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.get(Calendar.HOUR_OF_DAY);   // force recalculation of internal fields
        return cal;
    }
    
    protected static int absoluteDay(Calendar cal, boolean use1904windowing)  {
        return cal.get(Calendar.DAY_OF_YEAR) + daysInPriorYears(cal.get(Calendar.YEAR), use1904windowing);
    }

    private static int daysInPriorYears(int yr, boolean use1904windowing) {
        int yr1  = yr - 1;
        int leapDays =   yr1 / 4   // plus julian leap days in prior years
                       - yr1 / 100 // minus prior century years
                       + yr1 / 400 // plus years divisible by 400
                       - 460;      // leap days in previous 1900 years
        return 365 * (yr - (use1904windowing ? 1904 : 1900)) + leapDays;
    }
	/**
	 * Thanks to POI project and the argument c is zero based.
	 * @param c The column position, start with zero
	 * @return
	 */
	protected static final String getColumn(int c) {
		int excelColNum = c + 1;

        StringBuilder colRef = new StringBuilder(3);
        int colRemain = excelColNum;
        while(colRemain > 0) {
            int thisPart = colRemain % 26;
            if(thisPart == 0) {
            	thisPart = 26;
            }
            colRemain = (colRemain - thisPart) / 26;
            char colChar = (char)(thisPart + 64);// A is at 65
            colRef.insert(0, colChar);
        }
        return colRef.toString();
	}
    
	public class Row {
		
		private List<Cell> cells = new ArrayList<>(255);
		
		public boolean isEmpty() {
			return cells == null || cells.isEmpty();
		}

		public int size() {
			return cells.size();
		}
		
		public Cell getCell(int column) {
			if(cells.size() > column) {
				return cells.get(column);
			}
			return null;
		}
		
		public Cell addCell(int column, String value) {
			return addCell(column, value, null);
		}

		public Cell addCell(int column, String value, CellStyle style) {
			Cell cell = getOrCreateCell(column);
			cell.setStyle(style);
			cell.setType(OpenXMLCellType.sharedString);
			if(value != null) {
				int sharedIndex = workbook.getSharedStrings().add(value);
				if(sharedIndex >= 0) {
					cell.setValue(sharedIndex);
				}
			}
			return cell;
		}
		
		public Cell addCell(int column, Number value, CellStyle style) {
			Cell cell = getOrCreateCell(column);
			cell.setStyle(style);
			cell.setType(OpenXMLCellType.number);
			cell.setValue(value);
			return cell;
		}
		
		/**
		 * The accepted types are number or percent
		 * @param column The index of the column
		 * @param value	The value
		 * @param type The type, number or percent
		 * @return
		 */
		public Cell addCell(int column, Number value, CellStyle style, OpenXMLCellType type) {
			Cell cell = getOrCreateCell(column);
			cell.setStyle(style);
			cell.setType(type);
			cell.setValue(value);
			return cell;
		}
		
		/**
		 * Add a date to the cell, please choose a style with date / duration formatting.
		 * @param column
		 * @param value
		 * @param style
		 * @return
		 */
		public Cell addCell(int column, Date value, CellStyle style) {
			Cell cell = getOrCreateCell(column);
			cell.setStyle(style);
			cell.setType(OpenXMLCellType.date);
			cell.setValue(value);
			return cell;
		}
		
		private Cell getOrCreateCell(int column) {
			Cell c;
			if(cells.size() < column) {
				for(int i=cells.size(); i<column; i++) {
					cells.add(null);
				}
				c = new Cell();
				cells.add(c);
			} else if(cells.size() == column) {
				c = new Cell();
				cells.add(c);
			} else if(cells.get(column) == null) {
				c = new Cell();
				cells.set(column, c);
			} else {
				c = cells.get(column);
			}
			return c;
		}
	}
	
	public static class Cell {
		
		private Object value;
		private OpenXMLCellType type;
		private CellStyle style;
		
		public Cell() {
			//
		}
		
		public Cell(Object value, OpenXMLCellType type, CellStyle style) {
			this.value = value;
			this.type = type;
			this.style = style;
		}
		
		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}
		
		public OpenXMLCellType getType() {
			return type;
		}
		
		public void setType(OpenXMLCellType type) {
			this.type = type;
		}

		public CellStyle getStyle() {
			return style;
		}
		
		public void setStyle(CellStyle style) {
			this.style = style;
		}
	}
}
