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

package org.olat.ims.qti.container.qtielements;

import org.dom4j.Element;
import org.olat.core.logging.AssertException;
import org.olat.core.util.openxml.OpenXMLDocument;

/**
 * Initial Date:  24.11.2004
 * @author Mike Stock
 */

public class Flow_label extends GenericQTIElement {

	private static final long serialVersionUID = -8374148188767094396L;
	/**
	 * Comment for <code>FLOW_BLOCK</code>
	 */
	public static final int FLOW_BLOCK = 0;
	/**
	 * Comment for <code>FLOW_LIST</code>
	 */
	public static final int FLOW_LIST = 1;
	
	/**
	 * Comment for <code>xmlClass</code>
	 */
	public static final String xmlClass = "flow_label";
	private int flowClass = FLOW_BLOCK;
	
	/**
	 * @param el_flow
	 */
	public Flow_label(Element el_flow) {
		super(el_flow);
		
		String sFlow = el_flow.attributeValue("class");
		if (sFlow != null) {
			if (sFlow.equals("Block")) { flowClass = FLOW_BLOCK; }
			else if (sFlow.equals("List")) { flowClass = FLOW_LIST; }
			else throw new AssertException("Invalid value for attribute class.");
		}
	}
	
	/**
	 * @return flowClass
	 */
	public int getFlowClass() {
		return flowClass;
	}
	
	/**
	 * @param flowClass
	 */
	public void setFlowClass(int flowClass) {
		this.flowClass = flowClass;
	}

	/**
	 * @see org.olat.ims.qti.container.qtielements.QTIElement#render(StringBuilder, RenderInstructions)
	 */
	@Override
	public void render(StringBuilder buffer, RenderInstructions ri) {
		ri.put(RenderInstructions.KEY_FLOW_LABEL, new Integer(flowClass));
		super.render(buffer, ri);
	}

	@Override
	public void renderOpenXML(OpenXMLDocument document, RenderInstructions ri) {
		ri.put(RenderInstructions.KEY_FLOW_LABEL, new Integer(flowClass));
		super.renderOpenXML(document, ri);
	}
}
