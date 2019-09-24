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
import org.olat.core.util.StringHelper;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class ImageRenderer extends DefaultComponentRenderer {

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
		// Use configured calculated scaled size, fallback to default size / ratio
		int width = 320;
		int height = 240;
		Size size = ic.getScaledSize();
		if(size != null) {
			width = size.getWidth();
			height = size.getHeight() + 20;//+20 because of toolbar
		}
		// Add video name with mime type ending for better browser support
		String mapperUrl = ic.getMapperUrl();
		String name = ic.getMedia().getName();
		if(name.lastIndexOf('.') > 0) {
			mapperUrl += "/" + name;
		} else {
			mapperUrl += "/video." + ic.getSuffix(ic.getMimeType());
		}
		// Add poster image if available
		String poster = null;
		if (ic.getPoster() != null) {
			poster = ic.getPosterMapperUrl() + "/" + ic.getPoster().getName();
		}
		
		// Provide own component dispatch ID and wrap in div
		String compId = "o_c" + ic.getDispatchID();
		sb.append("<div id='").append(compId).append("' class='o_video'>"); // START component
		// The inner component 
		String imgId = "mov_" + ic.getDispatchID();
		sb.append("<div id='").append(imgId).append("' name='").append(imgId).append("' style='width:");
		if(size != null) {
			sb.append(width).append("px; height:").append(height).append("px;");
		} else {
			// if no size available, scale to full width
			sb.append("100%;");

		}
		sb.append("' class='o_video_wrapper'></div>")
		  .append("<script>")
		  .append("/* <![CDATA[ */")
		  .append("BPlayer.insertPlayer('").append(Settings.createServerURI()).append(mapperUrl);
		sb.append("','").append(imgId).append("',").append(width).append(",").append(height).append(",'video'");
		if (poster != null) {
			sb.append(",null,null,null,null,null,null,'").append(poster).append("'");
		}
		sb.append(");")
		  .append("/* ]]> */")
		  .append("</script>")
		  .append("</div>"); // ENDcomponent
	}
	
	private void renderImage(StringOutput sb, ImageComponent ic) {
		// Provide own component dispatch ID and wrap in div
		String compId = "o_c" + ic.getDispatchID();
		Size scaledSize = ic.getScaledSize();
		boolean cropEnabled = ic.isCropSelectionEnabled();
		if(cropEnabled) {//wrapper for cropper.js
			sb.append("<div style='");
			if(scaledSize != null) {
				sb.append("width:").append(scaledSize.getWidth()).append("px;");
				sb.append("height:").append(scaledSize.getHeight()).append("px;");
			}
			sb.append("'>");
		}
		
		boolean divWrapper = ic.isDivImageWrapper();
		if(divWrapper) {
			sb.append("<div id='").append(compId).append("' class='o_image'>"); // START component
		}
		
		// The inner component 
		String imgId = divWrapper ? "o_img" + ic.getDispatchID() : compId;
		sb.append("<img").append(" id='").append(imgId).append("'");
		if(StringHelper.containsNonWhitespace(ic.getCssClasses())) {
			sb.append(" class=\"").append(ic.getCssClasses()).append("\"");
		}
		if (scaledSize != null) {
			sb.append(" width=\"").append(scaledSize.getWidth()).append("\"");
			sb.append(" height=\"").append(scaledSize.getHeight()).append("\"");
		}

		String mapperUrl = ic.getMapperUrl();
		String name = ic.getMedia().getName();
		if(name.lastIndexOf('.') > 0) {
			mapperUrl += "/" + name + "?" + System.nanoTime();
		} else {
			mapperUrl += "/?" + System.nanoTime();
		}
		sb.append(" src='").append(mapperUrl).append("' alt=\"");
		if(StringHelper.containsNonWhitespace(ic.getAlt())) {
			sb.append(ic.getAlt());
		} else {
			sb.append("*");
		}
		sb.append("\" />");
		
		if(cropEnabled) {
			sb.append("<input id='").append(imgId).append("_x' name='").append(imgId).append("_x' type='hidden' value='' />")
			  .append("<input id='").append(imgId).append("_y' name='").append(imgId).append("_y' type='hidden' value='' />")
			  .append("<input id='").append(imgId).append("_w' name='").append(imgId).append("_w' type='hidden' value='' />")
			  .append("<input id='").append(imgId).append("_h' name='").append(imgId).append("_h' type='hidden' value='' />");
			
			sb.append("<script>\n")
			  .append("/* <![CDATA[ */ \n")
			  .append("jQuery(function() {\n")
			  .append("  jQuery('#").append(imgId).append("').cropper({\n")
			  .append("    aspectRatio:1,\n")
			  .append("    done: function(crop) {\n")
			  .append("      jQuery('input#").append(imgId).append("_x').val(crop.x1);\n")
			  .append("    	 jQuery('input#").append(imgId).append("_y').val(crop.y1);\n")
			  .append("    	 jQuery('input#").append(imgId).append("_w').val(crop.width);\n")
			  .append("    	 jQuery('input#").append(imgId).append("_h').val(crop.height);\n")
			  .append("    }")
			  .append("  });")
			  .append("});")
		      .append("/* ]]> */\n")
		      .append("</script>");
		}
		sb.append("</div>", divWrapper); // ENDcomponent
		sb.append("</div>", cropEnabled);
	}
}