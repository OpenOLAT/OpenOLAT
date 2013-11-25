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
import org.olat.core.util.StringHelper;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.ims.qti.editor.beecom.objects.Question;
/**
 * Initial Date:  25.11.2004
 *
 * @author Mike Stock
 */
public class Item extends GenericQTIElement {

	private static final long serialVersionUID = 3195522706482981316L;
	private String title;
	/**
	 * @param el_item
	 */
	public Item(Element el_item) {
		super(el_item);
		title = el_item.attributeValue("title");
	}

	/**
	 * @see org.olat.ims.qti.container.qtielements.QTIElement#render(StringBuilder, RenderInstructions)
	 */
	@Override
	public void render(StringBuilder buffer, RenderInstructions ri) {
		buffer.append("<div class=\"o_qti_item\">");
		if (((Boolean)ri.get(RenderInstructions.KEY_RENDER_TITLE)).booleanValue()) {
			buffer.append("<h3>").append(title).append("</h3>");
		}
		// append dummy iteminput to recognise empty statements
		buffer.append("<input type=\"hidden\" value=\"\" name=\"dummy§").append(getQTIIdent()).append("§xx§xx\" />");
		ri.put(RenderInstructions.KEY_ITEM_IDENT, getQTIIdent());
		GenericQTIElement itemObjectives = null;
		GenericQTIElement itemPresentation = null;
		for (int i = 0; i < getChildCount(); i++) {
			GenericQTIElement next = (GenericQTIElement)getChildAt(i);
			if (next instanceof Objectives) itemObjectives = next;
			else if (next instanceof Presentation) itemPresentation = next;
		}
		if (itemObjectives != null) itemObjectives.render(buffer, ri);
		if (itemPresentation != null) itemPresentation.render(buffer, ri);
		buffer.append("</div>");
	}

	@Override
	public void renderOpenXML(OpenXMLDocument document, RenderInstructions ri) {
		if (Boolean.TRUE.equals(ri.get(RenderInstructions.KEY_RENDER_TITLE))) {
			StringBuilder addText = new StringBuilder();
			String type = (String)ri.get(RenderInstructions.KEY_QUESTION_TYPE);
			String score = (String)ri.get(RenderInstructions.KEY_QUESTION_SCORE);
			if(StringHelper.containsNonWhitespace(type) || StringHelper.containsNonWhitespace(score)) {
				if(StringHelper.containsNonWhitespace(type)) {
					addText.append("(").append(type).append(")");
				}
				if(StringHelper.containsNonWhitespace(score)) {
					addText.append(" - ").append(score);
				}
			}
			document.appendHeading1(title, addText.toString());
		}

		Objectives itemObjectives = null;
		Presentation itemPresentation = null;
		for (int i=getChildCount(); i-->0; ) {
			INode next = getChildAt(i);
			if (next instanceof Objectives) {
				itemObjectives = (Objectives)next;
			} else if (next instanceof Presentation) {
				itemPresentation = (Presentation)next;
			}
		}
		
		if (itemObjectives != null) {
			itemObjectives.renderOpenXML(document, ri);
		}
		if (itemPresentation != null) {
			itemPresentation.renderOpenXML(document, ri);
		}

		Boolean renderResponse = (Boolean)ri.get(RenderInstructions.KEY_RENDER_CORRECT_RESPONSES);
		Integer type = (Integer)ri.get(RenderInstructions.KEY_QUESTION_OO_TYPE);
		if(renderResponse != null && renderResponse.booleanValue() &&
				type != null && type.intValue() == Question.TYPE_ESSAY) {

			for(int i=getChildCount(); i-->0; ) {
				QTIElement el = (QTIElement)getChildAt(i);
				if(el instanceof ItemFeedback && "Solution".equals(el.getQTIIdent())) {
					el.renderOpenXML(document, ri);
				}
			}
		}
	}
}
