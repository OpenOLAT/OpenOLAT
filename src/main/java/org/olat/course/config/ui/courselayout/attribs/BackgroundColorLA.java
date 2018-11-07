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
 * 
 * Description:<br>
 * attribute: background-color
 * needs some dummy classes in olat.css
 * colored-dropdowns are not supported by all browsers!
 * 
 * <P>
 * Initial Date:  08.02.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class BackgroundColorLA extends ColorLA {

	private static final String BG_IDENTIFIER = "backgroundColor";
	
	public BackgroundColorLA() {
		super();
		setAttributeKey("background-color");

	}

	@Override
	public String getLayoutAttributeTypeName() {
		return BG_IDENTIFIER;
	}	

}
