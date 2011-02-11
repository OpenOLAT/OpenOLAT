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
* <p>
*/ 

package org.olat.ims.qti.container.qtielements;

import org.dom4j.Element;

/**
 * Initial Date:  24.11.2004
 * @author Mike Stock
 */
public class Material extends GenericQTIElement {

	/**
	 * Comment for <code>xmlClass</code>
	 */
	public static final String xmlClass = "material";

	/**
	 * @param el_material
	 */
	public Material(Element el_material) {
		super(el_material);
	}

	/**
	 * @see org.olat.ims.qti.container.qtielements.QTIElement#render(StringBuilder, RenderInstructions)
	 */
	public void render(StringBuilder buffer, RenderInstructions ri) {
		super.render(buffer, ri);
		int iFlowMatClass = RenderInstructions.RENDER_FLOW_BLOCK;
		Integer flowMatClass = (Integer)ri.get(RenderInstructions.KEY_FLOW_MAT);
		if (flowMatClass != null) iFlowMatClass = flowMatClass.intValue();
		
		if (iFlowMatClass == RenderInstructions.RENDER_FLOW_LIST)
			buffer.append("<br />");
	}

}
