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
package org.olat.course.config.ui.courselayout.attribs;

/**
 * Description:<br>
 * attribute: font 
 * IMPORTANT: needs corresponding css-classes in olat.css to get
 * a valid preview in the dropdown (not supported by every browser)!
 * 
 * <P>
 * Initial Date: 03.02.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class FontLA extends AbstractLayoutAttribute {

	public static final String IDENTIFIER = "font";

	public FontLA() {
		setAttributeKey("font-family");
		String[] availKeys = new String[] { "arial,helvetica,sans-serif", "arial black,avant garde", "comic sans ms,sans-serif",
				"courier new,courier", "georgia,serif", "impact,chicago", "lucida console,monaco,monospace",
				"palatino linotype,book antiqua,palatino,serif", "times new roman,times", "verdana,geneva,sans-serif", "wingdings,zapf dingbats" };
		setAvailKeys(availKeys);
		String[] availValues = new String[] { "Arial normal", "Arial black", "Comic Sans MS", "Courier", "Georgia", "Impact", "Lucida Console",
				"Palatino", "Times", "Verdana", "Wingdings" };
		setAvailValues(availValues);
		String[] availCSS = new String[] { "clgen_font_arial", "clgen_font_arial_black", "clgen_font_comic", "clgen_font_courier",
				"clgen_font_georgia", "clgen_font_impact", "clgen_font_lucida", "clgen_font_palatino", "clgen_font_times", "clgen_font_verdana", "" };
		setAvailCSS(availCSS);
	}

	@Override
	public String getLayoutAttributeTypeName() {
		return IDENTIFIER;
	}

}
