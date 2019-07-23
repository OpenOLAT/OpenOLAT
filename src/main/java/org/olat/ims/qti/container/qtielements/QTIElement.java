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

import org.olat.core.util.openxml.OpenXMLDocument;

/**
 * Initial Date:  25.11.2004
 *
 * @author Mike Stock
 */
public interface QTIElement {

	/**
	 * @return The label or null if not defined.
	 */
	public String getLabel();
	
	/**
	 * @return The ident or null if not defined.
	 */
	public String getQTIIdent();
	
	/**
	 * Render to HTML
	 * 
	 * @param buffer
	 * @param ri
	 */
	public void render(StringBuilder buffer, RenderInstructions ri);
	
	/**
	 * Render to OpenXML
	 * 
	 * @param document The document to rendered within
	 * @param parentElement The parent element 
	 * @param ri
	 */
	public void renderOpenXML(OpenXMLDocument document, RenderInstructions ri);
}
