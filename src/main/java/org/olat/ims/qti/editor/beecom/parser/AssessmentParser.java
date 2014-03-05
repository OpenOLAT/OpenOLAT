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
import org.olat.ims.qti.editor.beecom.objects.Assessment;
import org.olat.ims.qti.editor.beecom.objects.Control;
import org.olat.ims.qti.editor.beecom.objects.Metadata;
import org.olat.ims.qti.editor.beecom.objects.OutcomesProcessing;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;
import org.olat.ims.qti.editor.beecom.objects.SelectionOrdering;

/**
 * @author rkulow
 *
 */
public class AssessmentParser implements IParser {
	
	private ParserManager parserManager = new ParserManager();
	
	/**
	 * @see org.olat.ims.qti.editor.beecom.IParser#parse(org.dom4j.Element)
	 */
	public Object parse(Element element) {
		//assert element.getName().equalsIgnoreCase("assessment");
		Assessment assessment = new Assessment();
		
		// attributes 
		
		Attribute attr = element.attribute("ident");
		if(attr!=null) assessment.setIdent(attr.getValue());
		attr = element.attribute("title");
		if(attr!=null) assessment.setTitle(attr.getValue());
		
		// elements

		// DURATION
		QTIObject duration =
		(QTIObject) parserManager.parse(element.element("duration"));
		assessment.setDuration(duration);
		
		//ASSESSMENTCONTROL
		List assessmentcontrolsXML = element.elements("assessmentcontrol");
		List assessmentcontrols = new ArrayList();
		for(Iterator i= assessmentcontrolsXML.iterator();i.hasNext();) {
			assessmentcontrols.add(parserManager.parse((Element)i.next()));
			assessment.setInheritControls(true);
		}
		if (assessmentcontrols.size() == 0) {
			assessmentcontrols.add(new Control());
			assessment.setInheritControls(false);
		}
		assessment.setAssessmentcontrols(assessmentcontrols);

		// OUTCOMES PROCESSING
		OutcomesProcessing outcomesProcessing = (OutcomesProcessing)parserManager.parse(element.element("outcomes_processing"));
		if (outcomesProcessing != null)
			assessment.setOutcomes_processing(outcomesProcessing);
		
		// SELECTION ORDERING
		SelectionOrdering selectionOrdering = (SelectionOrdering)parserManager.parse(element.element("selection_ordering"));
		if (selectionOrdering != null){
			assessment.setSelection_ordering(selectionOrdering);
		} else {
			assessment.setSelection_ordering(new SelectionOrdering());
		}
		
		//SECTIONS
		List sectionsXML = element.elements("section");
		List sections = new ArrayList();
		for(Iterator i = sectionsXML.iterator();i.hasNext();) {
			sections.add(parserManager.parse((Element)i.next()));	
		}
		assessment.setSections(sections);
		
		//ITEMS
		List itemsXML = element.elements("item");
		List items = new ArrayList();
		for(Iterator i = itemsXML.iterator();i.hasNext();) {
			items.add(parserManager.parse((Element)i.next()));	
		}
		assessment.setItems(items);
		
		//OBJECTIVES
		Element mattext = (Element)element.selectSingleNode("./objectives/material/mattext");
		if (mattext != null)
			assessment.setObjectives(mattext.getTextTrim());
		
		// METADATA
		Metadata metadata = (Metadata)parserManager.parse(element.element("qtimetadata"));
		if (metadata != null)
			assessment.setMetadata(metadata);
		
		
		return assessment;
	}
	
	

}
