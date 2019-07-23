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

/**
 * Initial Date:  24.11.2004
 *
 * @author Mike Stock
 */
public class Matvideo extends GenericQTIElement {

	private static final long serialVersionUID = -1557450651659211060L;

	/**
	 * Comment for <code>xmlClass</code>
	 */
	public static final String xmlClass = "matvideo";

	String videotype;
	String uri;
	int width = 300; // initialize with default values
	int height = 200; // initialize with default values
	
	/**
	 * @param el_matimage
	 */
	public Matvideo(Element el_matimage) {
		super(el_matimage);
		videotype = el_matimage.attributeValue("videotype");
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
	public void render(StringBuilder buffer, RenderInstructions ri) {
		if (videotype == null || uri == null) return;
		
		buffer.append("<object width=\"").append(width).append("\"")
			.append(" height=\"").append(height).append("\" ");
		
		if (videotype.equals("application/x-shockwave-flash")) {
			// shockwave
			buffer.append("classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,0,0\">")
				.append("<param name=\"movie\" value=\"");
			if (uri.startsWith("http://") || uri.startsWith("https://"))
				buffer.append(uri);
			else
				buffer.append((String)ri.get(RenderInstructions.KEY_STATICS_PATH)).append(uri);
			buffer.append("\"><embed type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash\" ")
				.append(" width=\"").append(width).append("\" height=\"").append(height).append("\" src=\"");
			if (uri.startsWith("http://") || uri.startsWith("https://"))
				buffer.append(uri);
			else
				buffer.append((String)ri.get(RenderInstructions.KEY_STATICS_PATH)).append(uri);
			buffer.append("\"></embed></object>");
		} else if (videotype.equals("video/quicktime")) {
			// quicktime
			buffer.append("classid=\"clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B\" codebase=\"http://www.apple.com/qtactivex/qtplugin.cab\">")
			.append("<param name=\"src\" value=\"");
			if (uri.startsWith("http://") || uri.startsWith("https://"))
				buffer.append(uri);
			else
				buffer.append((String)ri.get(RenderInstructions.KEY_STATICS_PATH)).append(uri);
			buffer.append("\"><param name=\"controller\" value=\"true\"><param name=\"autoplay\" value=\"true\">")
				.append("<embed  type=\"video/quicktime\" pluginspage=\"http://www.apple.com/quicktime/download/\" width=\"")
				.append(width).append("\" height=\"").append(height).append("\" src=\"");
			if (uri.startsWith("http://") || uri.startsWith("https://"))
				buffer.append(uri);
			else
				buffer.append((String)ri.get(RenderInstructions.KEY_STATICS_PATH)).append(uri);
			buffer.append("\" autoplay=\"true\" controller=\"true\"></embed></object>");
		} else if (videotype.equals("application/vnd.rn-realmedia")) {
			buffer.append("classid=\"clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA\">")
			.append("<param name=\"src\" value=\"");
			if (uri.startsWith("http://") || uri.startsWith("https://"))
				buffer.append(uri);
			else
				buffer.append((String)ri.get(RenderInstructions.KEY_STATICS_PATH)).append(uri);
			buffer.append("\"><param name=\"autostart\" value=\"true\">");
			buffer.append("<param name=\"controls\" value=\"imagewindow\">");
			buffer.append("<param name=\"console\" value=\"video\">");
			buffer.append("<param name=\"loop\" value=\"false\">");
			buffer.append("<embed type=\"application/vnd.rn-realmedia\"")
			.append(" width=\"").append(width).append("\" height=\"").append(height).append("\" src=\"");
			if (uri.startsWith("http://") || uri.startsWith("https://"))
				buffer.append(uri);
			else
				buffer.append((String)ri.get(RenderInstructions.KEY_STATICS_PATH)).append(uri);
			buffer.append("\" autostart=\"true\" controls=\"imagewindow\" console=\"video\" loop=\"false\"></embed></object>");
		} else if (videotype.equals("video/x-msvideo") || videotype.equals("video/mpeg")) {
			// windows media
			buffer.append("classid=\"CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95\" codebase=\"http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701\" type=\"application/x-oleobject\">")
			.append("<param name=\"fileName\" value=\"");
			if (uri.startsWith("http://") || uri.startsWith("https://"))
				buffer.append(uri);
			else
				buffer.append((String)ri.get(RenderInstructions.KEY_STATICS_PATH)).append(uri);
			buffer.append("\"><param name=\"animationatStart\" value=\"true\">");
			buffer.append("<param name=\"transparentatStart\" value=\"true\">");
			buffer.append("<param name=\"autoStart\" value=\"true\">");
			buffer.append("<param name=\"showControls\" value=\"true\">");
			buffer.append("<param name=\"loop\" value=\"false\">");
			buffer.append("<embed ")
				.append(" width=\"").append(width).append("\" height=\"").append(height).append("\" src=\"");
			if (uri.startsWith("http://") || uri.startsWith("https://"))
				buffer.append(uri);
			else
				buffer.append((String)ri.get(RenderInstructions.KEY_STATICS_PATH)).append(uri);
			buffer.append("\" name=\"bla\" displaysize=\"4\" autosize=\"-1\" bgcolor=\"darkblue\" showcontrols=\"true\" ");
			buffer.append("showtracker=\"-1\" showdisplay=\"0\" showstatusbar=\"-1\" videoborder3d=\"-1\" ");
			buffer.append("autostart=\"true\" designtimesp=\"5331\" loop=\"false\"");
			buffer.append("></embed></object>");
		} else {
			buffer.append(" />Not supported.");
		}
	}

}
