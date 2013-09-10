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
 *
 * @author Mike Stock
 */
public class Matapplet extends GenericQTIElement {

	private static final long serialVersionUID = 2801306422876223480L;
	/**
	 * Comment for <code>xmlClass</code>
	 */
	public static final String xmlClass = "matapplet";
	private static final String PARA = "§";

	String uri;
	int width = 300; // initialize with default values
	int height = 200; // initialize with default values
	
	/**
	 * @param el_matimage
	 */
	public Matapplet(Element el_matimage) {
		super(el_matimage);
		uri = el_matimage.attributeValue("uri");
		String sWidth = el_matimage.attributeValue("width");
		if (sWidth != null && sWidth.length() > 0) {
			try { width = Integer.parseInt(sWidth); }
			catch (NumberFormatException nfe) {
				throw new AssertException("Non-integer value for width.");
			}
		}
		
		String sHeight = el_matimage.attributeValue("height");
		if (sHeight != null && sHeight.length() > 0) {
			try { height = Integer.parseInt(sHeight); }
			catch (NumberFormatException nfe) {
				throw new AssertException("Non-integer value for height.");
			}
		}
	}

	/**
	 * @see org.olat.ims.qti.container.qtielements.QTIElement#render(StringBuilder, RenderInstructions)
	 */
	@Override
	public void render(StringBuilder buffer, RenderInstructions ri) {
		buffer.append("<object width=\"").append(width).append("\"")
			.append(" height=\"").append(height).append("\" ");
		
		String strippedURI = uri;
		String staticsPath = (String)ri.get(RenderInstructions.KEY_STATICS_PATH);
		int paramStopChar = uri.indexOf('?');
		if (paramStopChar != -1) strippedURI = uri.substring(0, paramStopChar);

		if (strippedURI.toLowerCase().endsWith(".swf")) {
			// render shockwave with special parameters in order for shockwave to be able to post the results
			// back to the qti engin. The flash will get a parameter called "url" where it is supposed to
			// post its results.
			buffer.append("classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0\">")
				.append("<param name=\"movie\" value=\"");
			if (uri.startsWith("http://") || uri.startsWith("https://"))
				buffer.append(uri);
			else
				buffer.append(staticsPath).append(uri);
			// append response parameters, check for flash parameters and wether to append to the end of those parameters
			if (uri.indexOf('?') != -1) buffer.append('&');
			else buffer.append('?');
			buffer.append("oResponseURL=").append(ri.get(RenderInstructions.KEY_APPLET_SUBMIT_URI)).append("&oResponseParam=qti")
			.append(PARA).append(ri.get(RenderInstructions.KEY_ITEM_IDENT))
			.append(PARA).append(ri.get(RenderInstructions.KEY_RESPONSE_IDENT))
			.append(PARA).append("flash")
			.append("&oStaticsURL=" + staticsPath);
			buffer.append("\"><embed type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash\"")
				.append(" width=\"").append(width).append("\" height=\"").append(height).append("\" src=\"");
			if (uri.startsWith("http://") || uri.startsWith("https://"))
				buffer.append(uri);
			else
				buffer.append(staticsPath).append(uri);
			// append response parameters, check for flash parameters and wether to append to the end of those parameters
			if (uri.indexOf('?') != -1) buffer.append('&');
			else buffer.append('?');
			buffer.append("oResponseURL=").append(ri.get(RenderInstructions.KEY_APPLET_SUBMIT_URI)).append("&oResponseParam=qti")
			.append(PARA).append(ri.get(RenderInstructions.KEY_ITEM_IDENT))
			.append(PARA).append(ri.get(RenderInstructions.KEY_RESPONSE_IDENT))
			.append(PARA).append("flash")
			.append("&oStaticsURL=" + staticsPath);
			buffer.append("\"></embed></object>");
		} else {
			buffer.append(" />Not supported.");
		}
	}

	@Override
	public void renderOpenXML(OpenXMLDocument document, RenderInstructions ri) {
		document.appendText("Applet not supported", true);
	}
}