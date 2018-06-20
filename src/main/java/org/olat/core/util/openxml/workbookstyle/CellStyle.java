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
package org.olat.core.util.openxml.workbookstyle;

/**
 * 
 * Initial date: 21.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CellStyle extends IndexedObject {

	private String id = "0";

	private String numFmtId = "0";
	private Font font;
	private Fill fill;
	private Border border;
	private String applyBorder;
	private String applyNumberFormat;
	private Alignment alignment;
	private String applyAlignment;

	public CellStyle(int index) {
		super(index);
	}
	
	public CellStyle(int index, String numFmtId, Font font, Fill fill, Border border, String applyBorder, String applyNumberFormat, Alignment alignment, String applyAlignment) {
		super(index);
		this.numFmtId = numFmtId;
		this.font = font;
		this.fill = fill;
		this.border = border;
		this.applyBorder = applyBorder;
		this.applyNumberFormat = applyNumberFormat;
		this.applyAlignment = applyAlignment;
		this.alignment = alignment;
	}
	
	public Font getFont() {
		return font;
	}
	
	public void setFont(Font font) {
		this.font = font;
	}
	
	public Fill getFill() {
		return fill;
	}
	
	public void setFill(Fill fill) {
		this.fill = fill;
	}
	
	public Border getBorder() {
		return border;
	}
	
	public void setBorder(Border border) {
		this.border = border;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNumFmtId() {
		return numFmtId;
	}

	public void setNumFmtId(String numFmtId) {
		this.numFmtId = numFmtId;
	}

	public String getApplyBorder() {
		return applyBorder;
	}

	public void setApplyBorder(String applyBorder) {
		this.applyBorder = applyBorder;
	}

	public String getApplyNumberFormat() {
		return applyNumberFormat;
	}

	public void setApplyNumberFormat(String applyNumberFormat) {
		this.applyNumberFormat = applyNumberFormat;
	}

	public String getApplyAlignment() {
		return applyAlignment;
	}

	public void setApplyAlignment(String applyAlignment) {
		this.applyAlignment = applyAlignment;
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}
}
