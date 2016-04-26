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
public class Font extends IndexedObject {
	
	private String szVal = "12";
	private String colorTheme = "1";
	private String nameVal = "Calibri";
	private String familyVal = "2";
	private String schemeVal = "minor";
	private FontStyle fontStyle = FontStyle.none;
	
	public enum FontStyle {
		none,
		bold
	}

	public Font(int index) {
		super(index);
	}
	
	public Font(int index, String szVal, String colorTheme, String nameVal, String familyVal, String schemeVal, FontStyle fontStyle) {
		super(index);
		this.szVal = szVal;
		this.colorTheme = colorTheme;
		this.nameVal = nameVal;
		this.familyVal = familyVal;
		this.schemeVal = schemeVal;
		this.fontStyle = fontStyle;
	}

	public String getSzVal() {
		return szVal;
	}

	public void setSzVal(String szVal) {
		this.szVal = szVal;
	}

	public String getColorTheme() {
		return colorTheme;
	}

	public void setColorTheme(String colorTheme) {
		this.colorTheme = colorTheme;
	}

	public String getNameVal() {
		return nameVal;
	}

	public void setNameVal(String nameVal) {
		this.nameVal = nameVal;
	}

	public String getFamilyVal() {
		return familyVal;
	}

	public void setFamilyVal(String familyVal) {
		this.familyVal = familyVal;
	}

	public String getSchemeVal() {
		return schemeVal;
	}

	public void setSchemeVal(String schemeVal) {
		this.schemeVal = schemeVal;
	}

	public FontStyle getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(FontStyle fontStyle) {
		this.fontStyle = fontStyle;
	}
}
