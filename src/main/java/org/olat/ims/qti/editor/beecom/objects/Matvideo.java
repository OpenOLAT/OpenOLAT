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

package org.olat.ims.qti.editor.beecom.objects;

import org.dom4j.Element;
import org.olat.core.util.CodeHelper;

/**
 * @author rkulow
 *
 */
public class Matvideo implements QTIObject, MatElement {
	
	public static final String VIDEO_TYPE_QUICKTIME = "video/quicktime";
	public static final String VIDEO_TYPE_SHOKWAVE = "application/x-shockwave-flash";
	public static final String VIDEO_TYPE_REAL = "application/vnd.rn-realmedia";
	public static final String VIDEO_TYPE_WMV = "video/x-msvideo";
	public static final String VIDEO_TYPE_MPEG = "video/mpeg";
	
	private String id = null;
	private String label = null;
	private String URI = null;
	private String videotype = null;
	private String width = null;
	private String height = null;
	private boolean externalURI = false;
	
	public Matvideo(String uri) {
		id = "" + CodeHelper.getRAMUniqueID();
		setURI(uri);
	}
	
	/**
	 * @see org.olat.ims.qti.editor.beecom.objects.QTIObject#addToElement(org.dom4j.Element)
	 */
	public void addToElement(Element root) {
		if (URI != null) {
			Element matvideo = root.addElement("matvideo");
			matvideo.addAttribute("uri", URI);
			matvideo.addAttribute("videotype", videotype);
			matvideo.addAttribute("width", width);
			matvideo.addAttribute("height", height);
		}
	}

	/**
	 * @see org.olat.ims.qti.editor.beecom.objects.MatElement#renderAsHtml(java.lang.String)
	 */
	public String renderAsHtml(String mediaBaseURL) {
		if (getURI() == null)	{
			return "[ VIDEO: no video selected ]";
		}	else {
			StringBuilder buffer = new StringBuilder();
			String uri = mediaBaseURL + "/" + getURI();
			
			buffer.append("<object ");
			if (width != null) buffer.append(" width=\"").append(width).append("\" ");
			if (height != null) buffer.append(" height=\"").append(height).append("\" ");
			
			if (videotype.equals("application/x-shockwave-flash")) {
				// shockwave
				buffer.append("classid=\"clsid:D27CDB6E-AE6D-11cf-96B8-444553540000\" codebase=\"http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0\">");
				buffer.append("<param name=\"movie\" value=\"");
				buffer.append(uri);
				buffer.append("\" />");
				if (width != null) buffer.append("<param name=\"movie\" value=\"").append(width).append("\" />");
				if (height != null) buffer.append("<param name=\"movie\" value=\"").append(height).append("\" />");
				buffer.append("<embed type=\"application/x-shockwave-flash\" pluginspage=\"http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash\" ");
				buffer.append("src=\"");
				buffer.append(uri);
				buffer.append("\"");				
				
			} else if (videotype.equals("video/quicktime")) {
				// quicktime
				buffer.append("classid=\"clsid:02BF25D5-8C17-4B23-BC80-D3488ABDDC6B\" codebase=\"http://www.apple.com/qtactivex/qtplugin.cab\">");
				buffer.append("<param name=\"src\" value=\"");
				buffer.append(uri);
				buffer.append("\" />");
				if (width != null) buffer.append("<param name=\"movie\" value=\"").append(width).append("\" />");
				if (height != null) buffer.append("<param name=\"movie\" value=\"").append(height).append("\" />");
				buffer.append("<param name=\"controller\" value=\"true\"><param name=\"autoplay\" value=\"true\" />");
				buffer.append("<embed type=\"video/quicktime\" pluginspage=\"http://www.apple.com/quicktime/download/\"");
				buffer.append(" src=\"");
				buffer.append(uri);
				buffer.append("\" autoplay=\"true\" controller=\"true\"");
				
			} else if (videotype.equals("application/vnd.rn-realmedia")) {
				buffer.append("classid=\"clsid:CFCDAA03-8BE4-11cf-B84B-0020AFBBCCFA\" >")
					.append("<param name=\"src\" value=\"");
				buffer.append(uri);
				buffer.append("\" />");
				if (width != null) buffer.append("<param name=\"movie\" value=\"").append(width).append("\" />");
				if (height != null) buffer.append("<param name=\"movie\" value=\"").append(height).append("\" />");
				buffer.append("<param name=\"autostart\" value=\"true\" />");
				buffer.append("<param name=\"controls\" value=\"imagewindow\" />");
				buffer.append("<param name=\"console\" value=\"video\" />");
				buffer.append("<param name=\"loop\" value=\"false\" />");
				buffer.append("<embed type=\"application/vnd.rn-realmedia\" ").append("src=\"");
				buffer.append(uri);
				buffer.append("\" autostart=\"true\" controls=\"imagewindow\" console=\"video\" loop=\"false\"");
			
			} else if (videotype.equals("video/x-msvideo") || videotype.equals("video/mpeg")) {
				// windows media
				buffer.append("classid=\"CLSID:22d6f312-b0f6-11d0-94ab-0080c74c7e95\" codebase=\"http://activex.microsoft.com/activex/controls/mplayer/en/nsmp2inf.cab#Version=5,1,52,701\" type=\"application/x-oleobject\">")
				.append("<param name=\"fileName\" value=\"");
				buffer.append(uri);
				buffer.append("\" />");
				if (width != null) buffer.append("<param name=\"movie\" value=\"").append(width).append("\" />");
				if (height != null) buffer.append("<param name=\"movie\" value=\"").append(height).append("\" />");
				buffer.append("<param name=\"animationatStart\" value=\"true\" />");
				buffer.append("<param name=\"transparentatStart\" value=\"true\" />");
				buffer.append("<param name=\"autoStart\" value=\"true\" />");
				buffer.append("<param name=\"showControls\" value=\"true\" />");
				buffer.append("<param name=\"loop\" value=\"false\" />");
				buffer.append("<embed type=\"application/x-mplayer2\" pluginspage=\"http://microsoft.com/windows/mediaplayer/en/download/\" ")
					.append("src=\"");
				buffer.append(uri);
				buffer.append("\" displaysize=\"4\" autosize=\"-1\" bgcolor=\"darkblue\" showcontrols=\"true\" ");
				buffer.append("showtracker=\"-1\" showdisplay=\"0\" showstatusbar=\"-1\" videoborder3d=\"-1\" ");
				buffer.append("autostart=\"true\" designtimesp=\"5331\" loop=\"false\"");

			} else {
				buffer.append(" />Not supported.");
				return buffer.toString();
			}
			if (width != null) buffer.append(" width=\"").append(width).append("\"");
			if (height != null) buffer.append(" height=\"").append(height).append("\"");
			buffer.append("></embed></object>");

			return buffer.toString();
		}
	}

	/**
	 * @see org.olat.ims.qti.editor.beecom.objects.MatElement#renderAsText()
	 */
	public String renderAsText() {
		if (getURI() == null)	{
			return "[ VIDEO: no video selected ]";
		}	else {
			return "[ VIDEO: "+getURI()+" ]";
		}
	}
	
	/**
	 * @return
	 */
	public String getURI() {
		return URI;
	}

	/**
	 * @param string
	 */
	public void setURI(String string) {
		if (string == null) return;
		URI = string;
		externalURI = (URI.indexOf("://") == -1) ? false : true;
		
		// extract videotype
		String suffix = URI.toLowerCase();
		if (suffix.lastIndexOf('.') == -1) return;
		suffix = suffix.substring(suffix.lastIndexOf('.') + 1, suffix.length());
		if (suffix.equals("mov")) setVideotype(VIDEO_TYPE_QUICKTIME);
		if (suffix.equals("qt")) setVideotype(VIDEO_TYPE_QUICKTIME);
		if (suffix.equals("rm")) setVideotype(VIDEO_TYPE_REAL);
		if (suffix.equals("swf")) setVideotype(VIDEO_TYPE_SHOKWAVE);
		if (suffix.equals("fla")) setVideotype(VIDEO_TYPE_SHOKWAVE);
		if (suffix.equals("flv")) setVideotype(VIDEO_TYPE_SHOKWAVE);
		if (suffix.equals("wma")) setVideotype(VIDEO_TYPE_WMV);
		if (suffix.equals("wmv")) setVideotype(VIDEO_TYPE_WMV);
		if (suffix.equals("avi")) setVideotype(VIDEO_TYPE_WMV);
		if (suffix.equals("mpeg")) setVideotype(VIDEO_TYPE_MPEG);
		if (suffix.equals("mpg")) setVideotype(VIDEO_TYPE_MPEG);
		if (suffix.equals("mp3")) setVideotype(VIDEO_TYPE_MPEG);
	}

	/**
	 * @return
	 */
	public boolean isExternalURI() {
		return externalURI;
	}

	/**
	 * @return
	 */
	public String getHeight() {
		return height;
	}

	/**
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return
	 */
	public String getVideotype() {
		return videotype;
	}

	/**
	 * @return
	 */
	public String getWidth() {
		return width;
	}

	/**
	 * @param b
	 */
	public void setExternalURI(boolean b) {
		externalURI = b;
	}

	/**
	 * @param string
	 */
	public void setHeight(String string) {
		height = string;
	}

	/**
	 * @param string
	 */
	public void setLabel(String string) {
		label = string;
	}

	/**
	 * @param string
	 */
	public void setVideotype(String string) {
		videotype = string;
	}

	/**
	 * @param string
	 */
	public void setWidth(String string) {
		width = string;
	}

	/**
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param string
	 */
	public void setId(String string) {
		id = string;
	}

}
