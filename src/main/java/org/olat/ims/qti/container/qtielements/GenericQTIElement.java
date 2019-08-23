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

import java.util.Iterator;

import org.dom4j.Element;
import org.olat.core.util.nodes.GenericNode;
import org.olat.core.util.openxml.OpenXMLDocument;
/**
 * Initial Date:  24.11.2004
 *
 * @author Mike Stock
 */
public abstract class GenericQTIElement extends GenericNode implements QTIElement {

	private static final long serialVersionUID = 6853989371155185854L;
	private String label;
	private String qtiident;
	
	/**
	 * @param el_element
	 */
	public GenericQTIElement(Element el_element) {
		label = el_element.attributeValue("label");
		qtiident = el_element.attributeValue("ident");
		addAll(el_element);
	}
	
	private void addAll(Element el_element) {
		for (final Iterator<?> iter = el_element.elementIterator(); iter.hasNext();) {
			Element sub_element = (Element) iter.next();
			String name = sub_element.getName();
			
			if (name.equals(Presentation.xmlClass)) this.addChild(new Presentation(sub_element));
			else if (name.equals(Assessment.xmlClass)) this.addChild(new Assessment(sub_element));
			else if (name.equals(Section.xmlClass)) this.addChild(new Section(sub_element));
			else if (name.equals(Flow.xmlClass)) this.addChild(new Flow(sub_element));
			else if (name.equals(Flow_mat.xmlClass)) this.addChild(new Flow_mat(sub_element));
			else if (name.equals(Flow_label.xmlClass)) this.addChild(new Flow_label(sub_element));
			else if (name.equals(Objectives.xmlClass)) this.addChild(new Objectives(sub_element));
			else if (name.equals(Hint.xmlClass)) this.addChild(new Hint(sub_element));
			else if (name.equals(HintMaterial.xmlClass)) this.addChild(new HintMaterial(sub_element));
			else if (name.equals(Solution.xmlClass)) this.addChild(new Solution(sub_element));
			else if (name.equals(SolutionMaterial.xmlClass)) this.addChild(new SolutionMaterial(sub_element));
			else if (name.equals(AssessFeedback.xmlClass)) this.addChild(new AssessFeedback(sub_element));
			else if (name.equals(SectionFeedback.xmlClass)) this.addChild(new SectionFeedback(sub_element));
			else if (name.equals(ItemFeedback.xmlClass)) this.addChild(new ItemFeedback(sub_element));
			else if (name.equals(Material.xmlClass)) this.addChild(new Material(sub_element));
			else if (name.equals(Mattext.xmlClass)) this.addChild(new Mattext(sub_element));
			else if (name.equals(Matemtext.xmlClass)) this.addChild(new Matemtext(sub_element));
			else if (name.equals(Matimage.xmlClass)) this.addChild(new Matimage(sub_element));
			else if (name.equals(Matvideo.xmlClass)) this.addChild(new Matvideo(sub_element));
			else if (name.equals(Matbreak.xmlClass)) this.addChild(new Matbreak(sub_element));
			else if (name.equals(Matapplet.xmlClass)) this.addChild(new Matapplet(sub_element));
			else if (name.equals(Response_label.xmlClass)) this.addChild(new Response_label(sub_element));
			else if (name.equals(Response_lid.xmlClass)) this.addChild(new Response_lid(sub_element));
			else if (name.equals(Response_str.xmlClass)) this.addChild(new Response_str(sub_element));
			else if (name.equals(Response_num.xmlClass)) this.addChild(new Response_num(sub_element));
			else if (name.equals(Render_choice.xmlClass)) this.addChild(new Render_choice(sub_element));
			else if (name.equals(Render_fib.xmlClass)) this.addChild(new Render_fib(sub_element));
			else this.addChild(new Raw(sub_element)); // add raw element if unknown
		}
	}

	/**
	 * @return Label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * @return Ident
	 */
	public String getQTIIdent() {
		return qtiident;
	}

	/**
	 * Generic recursive rendering.
	 * @see org.olat.ims.qti.container.qtielements.QTIElement#render(StringBuilder, RenderInstructions)
	 */
	@Override
	public void render(StringBuilder buffer, RenderInstructions ri) {
		for (int i = 0; i < getChildCount(); i++) {
			((QTIElement)getChildAt(i)).render(buffer, ri);
		}
	}

	@Override
	public void renderOpenXML(OpenXMLDocument document, RenderInstructions ri) {
		for (int i = 0; i < getChildCount(); i++) {
			((QTIElement)getChildAt(i)).renderOpenXML(document, ri);
		}
	}
}
