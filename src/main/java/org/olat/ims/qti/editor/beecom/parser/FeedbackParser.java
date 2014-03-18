/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.editor.beecom.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.olat.ims.qti.editor.beecom.objects.Feedback;

/**
 * @author rkulow
 *
 */
public class FeedbackParser implements IParser {

	private ParserManager parserManager = new ParserManager();
	
	/**
	 * @see org.olat.ims.qti.editor.beecom.objects.IParser#parse(org.dom4j.Element)
	 */
	public Object parse(Element element) {
		//assert element.getName().equalsIgnoreCase("sectionfeedback") 
		//			|| element.getName().equalsIgnoreCase("itemfeedback")
		//			|| element.getName().equalsIgnoreCase("assessmentfeedback");

		List materialsXML = element.selectNodes(".//material");
		if (materialsXML.size() == 0) return null;

		Feedback feedback = new Feedback();
		// attributes 
		Attribute tmp = element.attribute("ident");
		if(tmp!=null) feedback.setIdent(tmp.getValue());
		tmp = element.attribute("title");
		if(tmp!=null) feedback.setTitle(tmp.getValue());
		tmp = element.attribute("view");
		if(tmp!=null) feedback.setView(tmp.getValue());
		
		// get type
		if (element.element("solution") != null) return null;
		else if (element.element("hint") != null) return null;
		
		// parse Material
		//MATERIAL
		List<Object> materials = new ArrayList<>();
		for(Iterator i = materialsXML.iterator();i.hasNext();) {
			materials.add(parserManager.parse((Element)i.next()));	
		}
		feedback.setMaterials(materials);
		return feedback;
	}

}
