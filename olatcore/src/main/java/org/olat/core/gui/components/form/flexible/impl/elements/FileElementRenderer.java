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
* Copyright (c) frentix GmbH<br>
* http://www.frentix.com<br>
* <p>
*/ 

package org.olat.core.gui.components.form.flexible.impl.elements;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.defaults.dispatcher.StaticMediaDispatcher;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;

/**
 * <h3>Description:</h3> The file element renderer displays a file upload
 * element for a single file. To make it more beauty, the standard file chooser
 * button form the browser is hidden and a fake button is displayed instead.
 * <p>
 * If the file element has already a file preset or submitted in a previous
 * request, the file name of this file is displayed.
 * <p>
 * The read only view displays only the file name
 * <p>
 * Initial Date: 08.12.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class FileElementRenderer implements ComponentRenderer {

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	public void render(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		// Use translator with flexi form elements fallback
		Translator trans = Util.createPackageTranslator(FileElementRenderer.class, translator.getLocale(), translator);
		//
		FileElementComponent fileComp = (FileElementComponent) source;
		FileElementImpl fileElem = fileComp.getFileElementImpl();
		String id = fileComp.getFormDispatchId();
		// Calculate current file name: either from already uploaded file or
		// from initial file or empty
		String fileName = fileElem.getUploadFileName();
		if (fileName == null) {
			// try fallback: default file
			File initialFile = fileElem.getInitialFile();
			if (initialFile != null) {
				fileName = initialFile.getName();
			} else {
				fileName = "";
			}
		}

		// Read-write view
		if (source.isEnabled()) {
			sb.append("<div class='b_fileinput'>");	
			// input.Browse is the real filebrowser, but set to be transparent. 
			// the div.b_fileinput_fakechooser is layered below the input.Browse and represents the visual GUI. 
			// Since input.Browse is layered above div.b_fileinput_fakechooser, all click events to go input.Browse
			// See http://www.quirksmode.org/dom/inputfile.html
			sb.append("<input type='file' name=\"");
	 		sb.append(id); // name for form labeling
	 		sb.append("\" id=\"");
	 		sb.append(id); // id to make dirty button work
	 		sb.append("\" class='b_fileinput_realchooser' ");
	 		// Add on* event handlers
	 		StringBuilder eventHandlers = FormJSHelper.getRawJSFor(fileElem.getRootForm(), id, fileElem.getAction());
	 		int onChangePos = eventHandlers.indexOf("onchange=");
	 		if (onChangePos != -1) {
	 			// add file upload change handler
	 			sb.append(eventHandlers.substring(0, onChangePos + 10));
	 			sb.append("b_handleFileUploadFormChange(this, this.form.fake_").append(id).append(", this.form.upload);");
	 			sb.append(eventHandlers.substring(onChangePos + 10, eventHandlers.length()));
	 		} else {
	 			sb.append(eventHandlers);
	 			sb.append(" onchange=\"b_handleFileUploadFormChange(this, this.form.fake_").append(id).append(", this.form.upload)\"");
	 		}
	 		// Add mime type restriction
	 		Set<String> mimeTypes = fileElem.getMimeTypeLimitations();
	 		if (mimeTypes.size() > 0 ) {
	 			sb.append(" accept=\"");
	 			Iterator iterator = mimeTypes.iterator();
	 			while (iterator.hasNext()) {
					String type = (String) iterator.next();
					sb.append(type);
					if (iterator.hasNext()) sb.append(",");
				}
	 			sb.append("\"");
	 		}
	 		// Add pseudo focus marker on fake file chooser button
	 		sb.append(" onfocus=\"this.form.fake_").append(id).append(".nextSibling.style.border = '1px dotted black';\"");
	 		sb.append(" onblur=\"this.form.fake_").append(id).append(".nextSibling.style.border = '0';\"");
	 		// Add select text (hover)
	 		sb.append(" title=\"").append(StringEscapeUtils.escapeHtml(trans.translate("file.element.select"))).append("\"/>");	
			sb.append("<div class='b_fileinput_fakechooser'>");	
			// Add the visible but fake input field and a styled faked file chooser button
			sb.append("<input name='fake_").append(id).append("' value=\"").append(StringEscapeUtils.escapeHtml(fileName)).append("\"/>");	
			sb.append("<a href='#' class='b_with_small_icon_left b_fileinput_icon'><span>").append(trans.translate("file.element.select")).append("</span></a>");	
			// Add Max upload size
			if (fileElem.getMaxUploadSizeKB() != FileElement.UPLOAD_UNLIMITED) {
				String maxUpload = Formatter.roundToString((fileElem.getMaxUploadSizeKB()+0f) / 1024, 1);
				sb.append("<span class='b_fileinput_maxsize'>(").append(trans.translate("file.element.select.maxsize", new String[]{maxUpload})).append(")</span>");	
			}
			sb.append("</div></div>");	
			
			// Add IE fix to deal with SSL and server timeouts
			// See http://bugs.olat.org/jira/browse/OLAT-1299
			sb.append("<!--[if lte IE 7]>");
			sb.append("<iframe height='1px' style='visibility:hidden' src='");
			StaticMediaDispatcher.renderStaticURI(sb, "workaround.html");
			sb.append("'></iframe>");
			sb.append("<![endif]-->");
				
			// Add set dirty form on change
			sb.append(FormJSHelper.getJSStartWithVarDeclaration(fileComp.getFormDispatchId()));
			/* deactivated due OLAT-3094 and OLAT-3040
			if(te.hasFocus()){
				sb.append(FormJSHelper.getFocusFor(teC.getFormDispatchId()));
			}
			 */
			sb.append(FormJSHelper.getSetFlexiFormDirty(fileElem.getRootForm(), fileComp.getFormDispatchId()));
			sb.append(FormJSHelper.getJSEnd());
			
		} else {
			//
			// Read only view
			sb.append("<span id=\"");
			sb.append(id);
			sb.append("\" ");
			sb.append(FormJSHelper.getRawJSFor(fileElem.getRootForm(), id, fileElem.getAction()));
			sb.append(" >");
			sb.append("<input disabled=\"disabled\" class=\"b_form_element_disabled\" size=\"");
			sb.append("\" value=\"");
			sb.append(StringEscapeUtils.escapeHtml(fileName));
			sb.append("\" ");
			sb.append("\" />");		
			sb.append("</span>");
		}

	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.ComponentRenderer#renderBodyOnLoadJSFunctionCall(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.RenderingState)
	 */
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer,
			StringOutput sb, Component source, RenderingState rstate) {
		// nothing to do
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.ComponentRenderer#renderHeaderIncludes(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderingState)
	 */
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb,
			Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		// nothing to do
	}

}
