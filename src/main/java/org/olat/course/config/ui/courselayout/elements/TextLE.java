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
package org.olat.course.config.ui.courselayout.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.olat.course.config.ui.courselayout.attribs.AbstractLayoutAttribute;
import org.olat.course.config.ui.courselayout.attribs.BackgroundColorLA;
import org.olat.course.config.ui.courselayout.attribs.ColorLA;
import org.olat.course.config.ui.courselayout.attribs.FontLA;
import org.olat.course.config.ui.courselayout.attribs.PreviewLA;
import org.olat.course.config.ui.courselayout.attribs.SizeLA;

/**
 * Description:<br>
 * element: text with children
 * 
 * <P>
 * Initial Date: 03.02.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class TextLE extends AbstractLayoutElement {

	public static final String IDENTIFIER = "text";

	public TextLE() {
		//
	}

	public TextLE(Map<String, String> config) {
		super(config);

		HashMap<String, Integer> iFrameRelativeChildren = new HashMap<>();
		iFrameRelativeChildren.put("body", 0);
		iFrameRelativeChildren.put("table", 0);
		iFrameRelativeChildren.put("ol, ul, li", 0);
		setIframeRelativeChildren(iFrameRelativeChildren);

		HashMap<String, Integer> mainRelativeChildren = new HashMap<>();
		mainRelativeChildren.put("#o_main", 0);
		iFrameRelativeChildren.put("#o_main table", 0);
		iFrameRelativeChildren.put("#o_main ol, #o_main ul, #o_main li", 0);
		setMainRelativeChildren(mainRelativeChildren);

		ArrayList<AbstractLayoutAttribute> avAttribs = new ArrayList<>();
		avAttribs.add(new FontLA());
		avAttribs.add(new SizeLA());
		avAttribs.add(new ColorLA());
		avAttribs.add(new BackgroundColorLA());
		avAttribs.add(new PreviewLA());

		setAvailableAttributes(avAttribs);
		initAttributeConfig();
	}

	public TextLE createInstance(Map<String, String> config) {
		return new TextLE(config);
	}

	@Override
	public String getLayoutElementTypeName() {
		return IDENTIFIER;
	}

}
