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
* <p>
*/ 

package org.olat.core.gui.components.image;

import org.olat.core.commons.services.image.Size;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class ImageRenderer extends DefaultComponentRenderer {

	/**
	 * @see org.olat.core.gui.render.ui.ComponentRenderer#render(org.olat.core.gui.render.Renderer,
	 *      org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator,
	 *      org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		ImageComponent ic = (ImageComponent) source;
		if(ic.getMedia() != null) {
			String mimeType = ic.getMimeType();
			if(mimeType != null && mimeType.startsWith("image/")) {
				renderImage(sb, ic);
			} else if(mimeType != null && mimeType.startsWith("video/")) {
				renderMovie(sb, ic);
			}
		}
	}
	
	private void renderMovie(StringOutput sb, ImageComponent ic) {
		String imgId = "mov_" + ic.getDispatchID();
		int width = 320;
		int height = 240;
		Size size = ic.getRealSize();
		if(size != null) {
			width = size.getWidth();
			height = size.getHeight() + 20;//+20 because of toolbar
		}
		String mapperUrl = ic.getMapperUrl();
		String name = ic.getMedia().getName();
		if(name.lastIndexOf('.') > 0) {
			mapperUrl += "/" + name;
		} else {
			mapperUrl += "/video." + ic.getSuffix(ic.getMimeType());
		}
		
		sb.append("<div id='").append(imgId).append("' name='").append(imgId).append("'></div>")
		  .append("<script type='text/javascript'>")
		  .append("/* <![CDATA[ */")
		  .append("BPlayer.insertPlayer('").append(Settings.createServerURI()).append(mapperUrl);
		sb.append("','").append(imgId).append("',").append(width).append(",").append(height).append(",'video');")
		  .append("/* ]]> */")
		  .append("</script>");
	}
	
	private void renderImage(StringOutput sb, ImageComponent ic) {
		String imgId = "img_" + ic.getDispatchID();
		sb.append("<img").append(" id='").append(imgId).append("'");
		Size size = ic.getScaledSize();
		if (size != null) {
			sb.append(" width=\"").append(size.getWidth()).append("\"");
			sb.append(" height=\"").append(size.getHeight()).append("\"");
		}

		String mapperUrl = ic.getMapperUrl();
		String name = ic.getMedia().getName();
		if(name.lastIndexOf('.') > 0) {
			mapperUrl += "/" + name;
		} else {
			mapperUrl += "/?" + System.nanoTime();
		}
		sb.append(" src='").append(mapperUrl).append("' class='img-responsive'/>");
		
		if(ic.isCropSelectionEnabled()) {
			sb.append("<input id='").append(imgId).append("_x' name='").append(imgId).append("_x' type='hidden' value='' />")
			  .append("<input id='").append(imgId).append("_y' name='").append(imgId).append("_y' type='hidden' value='' />")
			  .append("<input id='").append(imgId).append("_w' name='").append(imgId).append("_w' type='hidden' value='' />")
			  .append("<input id='").append(imgId).append("_h' name='").append(imgId).append("_h' type='hidden' value='' />");
			
			sb.append("<script type='text/javascript'>\n")
			  .append("/* <![CDATA[ */ \n")
			  .append("jQuery(function() {\n")
			  .append("  jQuery('#").append(imgId).append("').imageCrop({\n")
			  .append("    displayPreview:false,\n")
			  .append("    displaySize:true,\n")
			  .append("    overlayOpacity:0.25,\n")
			  .append("    aspectRatio:1,\n")
			  .append("    onSelect: function(crop) {\n")
			  .append("      jQuery('input#").append(imgId).append("_x').val(crop.selectionX);\n")
			  .append("    	 jQuery('input#").append(imgId).append("_y').val(crop.selectionY);\n")
			  .append("    	 jQuery('input#").append(imgId).append("_w').val(crop.selectionWidth);\n")
			  .append("    	 jQuery('input#").append(imgId).append("_h').val(crop.selectionHeight);\n")
			  .append("    }")
			  .append("  });")
			  .append("});")
		      .append("/* ]]> */\n")
		      .append("</script>");
		}
	}
}