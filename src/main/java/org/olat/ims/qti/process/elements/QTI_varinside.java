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

package org.olat.ims.qti.process.elements;

import org.dom4j.Element;
import org.olat.ims.qti.container.ItemContext;

/**
 * 
 */
public class QTI_varinside implements BooleanEvaluable {

	/**
	 * var inside
	 * qti ims 1.2.1
	 * <!ELEMENT varinside (#PCDATA)>
	 * <!ATTLIST varinside  areatype     (Ellipse | Rectangle | Bounded )  #REQUIRED
	 *                 %I_RespIdent;
	 *                %I_Index; >
	 * mit 
	 * <!ENTITY % I_RespIdent " respident CDATA  #REQUIRED">
	 * <!ENTITY % I_Index " index CDATA  #IMPLIED">
	 * 
	 *  Response type:    X-Y co-ordinates (XY)	Single	={{identifier, xcoord, ycoord}}, {duration}
	 * 
	 * e.g. <varinside respident = "IHS01" areatype = "Rectangle">50,200,250,350</varinside>
	 */
	public boolean eval(Element boolElement, ItemContext userContext, EvalContext ect) {
		throw new RuntimeException ("varinside not supported yet");
		//return false;
		//Variables inputVars = userContext.getCurrentInput();
		/*String respident = boolElement.attributeValue("respident");
		String areaType = boolElement.attributeValue("areatype");
		String shouldRegion = boolElement.getText(); // the answer is tested against content of elem.
		//String userVal = inputVars.getStringVariable(respident);
		/*
		 * variables = pool, one variable consist varitems which have values like 1, "bla", 1;2;5 and such
		 * 
		 
		int x = inputVars.getXofXY(respident);
		int y = inputVars.getYofXY(respident);
		int x0 = ParseHelper.parseMulti(shouldRegion).get(1);
		int x1 = ParseHelper.parseMulti(shouldRegion).get(1);
		int y0 = ParseHelper.parseMulti(shouldRegion).get(1);
		int y1 = ParseHelper.parseMulti(shouldRegion).get(1);
		
		boolean ok = false; //
		return ok;
		*/
	}
	
	

}
