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

import org.dom4j.Element;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
import org.olat.ims.qti.editor.beecom.objects.QTIDocument;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;

/**
 * @author rkulow
 *
 */
public class QTIDocumentParser implements IParser {
	private ParserManager parserManager = new ParserManager();
	
	public Object parse(Element element) {
		//assert element.getName().equalsIgnoreCase("questestinterop");
		QTIDocument qtiDocument= new QTIDocument();
		
		Object tmp = null;
		Element qticomment = element.element("qticomment");
		qtiDocument.setQticomment((QTIObject)parserManager.parse(qticomment));
		
		Element objectbank = element.element("objectbank");
		tmp = parserManager.parse(objectbank);
		if(tmp!=null) {
			qtiDocument.setObjectbank((QTIObject)tmp);
		}
		Element assesment = element.element("assessment");
		tmp = parserManager.parse(assesment);	
		if(tmp!=null) {
			qtiDocument.setAssessment((Assessment)tmp);
		}
		return qtiDocument;		
	}

}
