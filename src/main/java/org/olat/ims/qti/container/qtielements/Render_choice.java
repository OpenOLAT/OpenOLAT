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

import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.AbstractAttribute;
import org.olat.core.logging.AssertException;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;

/**
 * Initial Date: 25.11.2004
 * 
 * @author Mike Stock
 */
public class Render_choice extends GenericQTIElement {

	/**
	 * Comment for <code>xmlClass</code>
	 */
	public static final String xmlClass = "render_choice";

	private boolean shuffle = false;
	private boolean kprim = false;
	private int minnumber = -1;
	private int maxnumber = -1;

	/**
	 * @param el_element
	 */
	public Render_choice(Element el_element) {
		super(el_element);

		// fetch shuffle
		String sShuffle = el_element.attributeValue("shuffle");
		if (sShuffle != null) {
			if (sShuffle.equals("Yes")) shuffle = true;
			else if (sShuffle.equals("No")) shuffle = false;
			else throw new AssertException("Attribute shuffle has invalid value.");
		}

		// get min/max number
		String sInteger = el_element.attributeValue("minnumber");
		if (sInteger != null) {
			try {
				minnumber = Integer.parseInt(sInteger);
			} catch (NumberFormatException nfe) {
				throw new AssertException("Attribute minnumber has invalid value.");
			}
		}
		sInteger = el_element.attributeValue("maxnumber");
		if (sInteger != null) {
			try {
				maxnumber = Integer.parseInt(sInteger);
			} catch (NumberFormatException nfe) {
				throw new AssertException("Attribute maxnumber has invalid value.");
			}
		}
		// find Kprim questions
		List ident = el_element.selectNodes("../../../@ident");
		if (ident != null && ident.size() == 1) {
			try {
				if (((AbstractAttribute) ident.get(0)).getValue().startsWith(ItemParser.ITEM_PREFIX_KPRIM)) kprim = true;
			} catch (Throwable t) {
				// not a kprim
			}
		}
	}

	/**
	 * @return maxnumber
	 */
	public int getMaxnumber() {
		return maxnumber;
	}

	/**
	 * @return minnumber
	 */
	public int getMinnumber() {
		return minnumber;
	}

	/**
	 * @return shuffle
	 */
	public boolean isShuffle() {
		return shuffle;
	}

	/**
	 * @see org.olat.ims.qti.container.qtielements.QTIElement#render(StringBuilder,
	 *      RenderInstructions)
	 */
	public void render(StringBuilder buffer, RenderInstructions ri) {

		if (kprim) {
			ri.put(RenderInstructions.KEY_RENDER_CLASS, "kprim");
			buffer.append("<table class=\"o_qti_item_kprim\" cellpadding=\"2\" cellspacing=\"0\">");
			buffer.append("<tr><th align=\"center\">+</th><th align=\"center\">-</th><th></th></tr>");

		} else {
			
			if (ri.containsKey(RenderInstructions.KEY_RENDER_AUTOENUM_LIST)) {
				ri.put(RenderInstructions.KEY_RENDER_AUTOENUM_IDX, "0");
			} 
			
			ri.put(RenderInstructions.KEY_RENDER_CLASS, "choice");
			buffer.append("<div class=\"o_qti_item_choice\">");
		}
		/*
		 * // Display min/max values if (minnumber != -1 || maxnumber != -1) {
		 * Translator translator = new PackageTranslator("org.olat.ims.qti",
		 * (Locale)ri.get(RenderInstructions.KEY_LOCALE)); buffer.append("<p class=\"o_qti_minmax\">");
		 * if (minnumber != -1) { buffer.append("<i>").append(translator.translate("render.item.minanswers")).append("&nbsp;")
		 * .append(minnumber).append("</i><br />"); } if (maxnumber != -1) {
		 * buffer.append("<i>").append(translator.translate("render.item.maxanswers")).append("&nbsp;")
		 * .append(maxnumber).append("</i>"); } buffer.append("</p>"); }
		 */
		super.render(buffer, ri);
		if (kprim) {
			buffer.append("</table>");
		} else {
			buffer.append("</div>");
		}
	}
}
