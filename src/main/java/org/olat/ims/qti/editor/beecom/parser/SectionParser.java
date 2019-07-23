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

import org.dom4j.Element;
import org.olat.ims.qti.editor.beecom.objects.Control;
import org.olat.ims.qti.editor.beecom.objects.Duration;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;
import org.olat.ims.qti.editor.beecom.objects.Section;
import org.olat.ims.qti.editor.beecom.objects.SelectionOrdering;

/**
 * @author rkulow
 *
 */
public class SectionParser implements IParser {
		private ParserManager parserManager = new ParserManager();
		
		public Object parse(Element element) {
			//assert element.getName().equalsIgnoreCase("questestinterop");
			Section section = new Section();
		 
			// attributes 
			section.setIdent(element.attribute("ident").getValue());
			section.setTitle(element.attribute("title").getValue());
		
			// elements

			// DURATION
			Duration duration = (Duration) parserManager.parse(element.element("duration"));
			section.setDuration(duration);
			
			List sectioncontrolsXML = element.elements("sectioncontrol");
			List<Object> sectioncontrols = new ArrayList<>();
			for(Iterator i= sectioncontrolsXML.iterator();i.hasNext();) {
				sectioncontrols.add(parserManager.parse((Element)i.next()));
			}
			if (sectioncontrols.size() == 0) {
				sectioncontrols.add(new Control());
			}
			section.setSectioncontrols(sectioncontrols);
		
			// SELECTION ORDERING
			SelectionOrdering selectionOrdering = (SelectionOrdering)parserManager.parse(element.element("selection_ordering"));
			if (selectionOrdering != null){
				section.setSelection_ordering(selectionOrdering);
			} else {
				section.setSelection_ordering(new SelectionOrdering());
			}

			//SECTIONS
			List sectionsXML = element.elements("section");
			List<Object> sections = new ArrayList<>();
			for(Iterator i = sectionsXML.iterator();i.hasNext();) {
				sections.add(parserManager.parse((Element)i.next()));	
			}
			section.setSections(sections);

		
			//ITEMS
			List itemsXML = element.elements("item");
			List<Object> items = new ArrayList<>();
			for(Iterator i = itemsXML.iterator();i.hasNext();) {
				items.add(parserManager.parse((Element)i.next()));	
			}
			section.setItems(items);
			
			//OBJECTIVES
			Element mattext = (Element)element.selectSingleNode("./objectives/material/mattext");
			if (mattext != null)
				section.setObjectives(mattext.getTextTrim());
			
			//FEEDBACKS
			List feedbacksXML = element.elements("sectionfeedback");
			List<QTIObject> feedbacks = new ArrayList<>();
			for(Iterator i = feedbacksXML.iterator();i.hasNext();) {
				QTIObject tmp = (QTIObject)parserManager.parse((Element)i.next());
				feedbacks.add(tmp);
			}
			section.setSectionfeedbacks(feedbacks);
			 
			QTIObject outcomes_processing = (QTIObject)parserManager.parse(element.element("outcomes_processing"));
			section.setOutcomes_processing(outcomes_processing);
			
		return section;
	}


}
