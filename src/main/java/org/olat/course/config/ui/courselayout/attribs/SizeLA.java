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
 * attribute: size
 * 
 * <P>
 * Initial Date:  04.02.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class SizeLA extends AbstractLayoutAttribute {

	public static final String IDENTIFIER = "size";
	
	/**
	 * 
	 */
	public SizeLA() {
		setAttributeKey("font-size");
		String[] availKeys = new String[] 	{ "xx-small", "x-small", "small", "medium", "large", "x-large", "xx-large" };
		setAvailKeys(availKeys);
		String[] relativeKeys = new String[]{ "0.3em", "xx-small", "x-small", "small", "medium", "large", "x-large", "xx-large", "3em" };
		setRelativeKeys(relativeKeys);
		String[] availValues = new String[] { "1 (xx-small)", "2 (x-small)", "3 (small)", "4 (medium)", "5 (large)", "6 (x-large)", "7 (xx-large)" };
		setAvailValues(availValues);
		String[] availCSS = new String[] 		{ "clgen_font_xxsmall", "o_xsmall", "o_small", "", "o_large", "o_xlarge", "clgen_font_xxlarge" };
		setAvailCSS(availCSS);
	}

	/**
	 * @see org.olat.course.config.ui.courselayout.attribs.AbstractLayoutAttribute#getLayoutAttributeTypeName()
	 */
	@Override
	public String getLayoutAttributeTypeName() {
		return IDENTIFIER;
	}

}
