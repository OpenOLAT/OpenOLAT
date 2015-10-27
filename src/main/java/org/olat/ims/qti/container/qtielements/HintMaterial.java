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

import java.util.Locale;

import org.dom4j.Element;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.ims.qti.QTIModule;
/**
 * Initial Date:  24.11.2004
 *
 * @author Mike Stock
 */
public class HintMaterial extends GenericQTIElement {

	private static final long serialVersionUID = 8946607035475045734L;
	/**
	 * Comment for <code>xmlClass</code>
	 */
	public static final String xmlClass = "hintmaterial";

	/**
	 * @param el_material
	 */
	public HintMaterial(Element el_material) {
		super(el_material);
	}

	/**
	 * @see org.olat.ims.qti.container.qtielements.GenericQTIElement#render(java.lang.StringBuilder, org.olat.ims.qti.container.qtielements.RenderInstructions)
	 */
	@Override
	public void render(StringBuilder buffer, RenderInstructions ri) {
		buffer.append("<div id='o_qti_hints' class='o_togglebox_wrapper'>")
		      .append("<a href='#' data-toggle='collapse' data-target='#o_qti_hints_inner' onclick='jQuery(this).toggleClass('o_in')'>")
		      .append("<i class='o_icon o_icon-fw o_icon_help'></i> ")
		      .append(getHintTitle(ri))
		      .append("</a><div id=\"o_qti_hints_inner\" class=\"collapse\"><div class=\"o_important o_togglebox_content clearfix\">");
		super.render(buffer, ri);
		buffer.append("</div></div></div>");
	}

	@Override
	public void renderOpenXML(OpenXMLDocument document, RenderInstructions ri) {
		document.appendHeading2(getHintTitle(ri), null);
		super.renderOpenXML(document, ri);
	}
	
	private String getHintTitle(RenderInstructions ri) {
		Locale locale = (Locale)ri.get(RenderInstructions.KEY_LOCALE);
		Translator translator = Util.createPackageTranslator(QTIModule.class, locale);
		return translator.translate("render.hint");
	}
}