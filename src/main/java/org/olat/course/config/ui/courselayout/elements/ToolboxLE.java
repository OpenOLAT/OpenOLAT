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
 * 
 * Description:<br>
 * element: toolbox (right panel/row2)
 * 
 * <P>
 * Initial Date: 07.02.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ToolboxLE extends AbstractLayoutElement {

	public static final String IDENTIFIER = "toolbox";

	public ToolboxLE() {
		//
	}

	public ToolboxLE(Map<String, String> config) {
		super(config);

		HashMap<String, Integer> iFrameRelativeChildren = new HashMap<>();
		setIframeRelativeChildren(iFrameRelativeChildren);

		HashMap<String, Integer> mainRelativeChildren = new HashMap<>();
		mainRelativeChildren.put("#o_main .o_toolbox a", 0);
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

	@Override
	public String getLayoutElementTypeName() {
		return IDENTIFIER;
	}

	@Override
	public AbstractLayoutElement createInstance(Map<String, String> config) {
		return new ToolboxLE(config);
	}

}
