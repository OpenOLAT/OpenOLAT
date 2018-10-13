/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */ 

package org.olat.core.gui.components.form.flexible.impl.elements;

import java.io.File;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.image.ImageFormItem;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
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

public class FileElementRenderer extends DefaultComponentRenderer {

	public Translator getTranslator(Translator fallbackTranslator) {
		return Util.createPackageTranslator(
				FileElementRenderer.class, fallbackTranslator.getLocale(), fallbackTranslator);
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.ComponentRenderer#render(org.olat.core.gui.render.Renderer, org.olat.core.gui.render.StringOutput, org.olat.core.gui.components.Component, org.olat.core.gui.render.URLBuilder, org.olat.core.gui.translator.Translator, org.olat.core.gui.render.RenderResult, java.lang.String[])
	 */
	@Override
	public void render(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		// Use translator with flexi form elements fallback
		Translator trans = getTranslator(translator);
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
		if (fileComp.isEnabled()) {
			ImageFormItem previewCmp = fileElem.getPreviewFormItem();
			if(previewCmp != null && previewCmp.isEnabled() && previewCmp.isVisible()) {
				sb.append("<div class='o_filepreview'>");	
				renderer.render(previewCmp.getComponent(), sb, args);
				sb.append("</div>");
			}
			
			boolean showDeleteButton = fileElem.isDeleteEnabled()
					&& (fileElem.getInitialFile() != null || fileElem.getUploadFile() != null);
			
			sb.append("<div class='o_fileinput'>");	
			// input.Browse is the real filebrowser, but set to be transparent. 
			// the div.o_fakechooser is layered below the input.Browse and represents the visual GUI. 
			// Since input.Browse is layered above div.o_fakechooser, all click events to go input.Browse
			// See http://www.quirksmode.org/dom/inputfile.html
			if (fileElem.isButtonsEnabled()) {
				sb.append("<input type='file' name=\"");
				 sb.append(id); // name for form labeling
				 sb.append("\" id=\"");
				 sb.append(id); // id to make dirty button work
				 sb.append("\" class='form-control o_realchooser ").append(" o_chooser_with_delete", showDeleteButton).append("' ");
				 // Add on* event handlers
				 StringBuilder eventHandlers = FormJSHelper.getRawJSFor(fileElem.getRootForm(), id, fileElem.getAction());
				 int onChangePos = eventHandlers.indexOf("onchange=");
				 if (onChangePos != -1) {
					 // add file upload change handler
					 sb.append(eventHandlers.substring(0, onChangePos + 10))
					   .append("b_handleFileUploadFormChange(this, this.form.fake_").append(id).append(", this.form.upload);")
					   .append(eventHandlers.substring(onChangePos + 10, eventHandlers.length()));
				 } else {
					 sb.append(eventHandlers)
					   .append(" onchange=\"b_handleFileUploadFormChange(this, this.form.fake_").append(id).append(", this.form.upload)\"");
				 }
				 // Add pseudo focus marker on fake file chooser button
				 sb.append(" onfocus=\"this.form.fake_").append(id).append(".nextSibling.style.border = '1px dotted black';\"")
				   .append(" onblur=\"this.form.fake_").append(id).append(".nextSibling.style.border = '0';\"");
				 // Add select text (hover)
				 sb.append(" title=\"").append(StringEscapeUtils.escapeHtml(trans.translate("file.element.select"))).append("\" />");
			}
			// Add the visible but fake input field and a styled faked file chooser button
			sb.append("<div class='o_fakechooser input-group'>");
			sb.append("<input class='form-control' name='fake_").append(id).append("' value=\"").append(StringEscapeUtils.escapeHtml(fileName))
			  .append("\" placeholder=\"").append(StringEscapeUtils.escapeHtml(trans.translate("file.element.select"))).append("\" />");  
			sb.append("<span class='input-group-addon'><a href='javascript:;'><i class='o_icon o_icon_upload'> </i></a></span>");
			if(showDeleteButton) {
				sb.append("<a class='input-group-addon' href=\"javascript:")
				  .append(FormJSHelper.getXHRFnCallFor(fileElem.getRootForm(), fileComp.getFormDispatchId(), 1, false, false, new NameValuePair("delete", "delete")))
				  .append(";\" onclick=\"\" ")
				  .append(" title=\"").append(StringEscapeUtils.escapeHtml(trans.translate("file.element.delete"))).append("\" ><i class='o_icon o_icon_delete'> </i></a>");
			}
			sb.append("</div></div>");
			// Add example text and  max upload size
			if(fileElem.getExampleText() != null) {
				sb.append("<div class='help-block'>")
				  .append(fileElem.getExampleText());
				if (fileElem.getMaxUploadSizeKB() != FileElement.UPLOAD_UNLIMITED) {
					String maxUpload = Formatter.formatBytes(fileElem.getMaxUploadSizeKB() * 1000);
					sb.append(" (")
					  .append(trans.translate("file.element.select.maxsize", new String[]{maxUpload}))
					  .append(")");
				}
				sb.append("</div>");	
			} else if (fileElem.getMaxUploadSizeKB() != FileElement.UPLOAD_UNLIMITED) {
				String maxUpload = Formatter.formatBytes(fileElem.getMaxUploadSizeKB() * 1000);
				sb.append("<div class='help-block o_maxsize'>(")
				.append(trans.translate("file.element.select.maxsize", new String[]{maxUpload}))
				.append(")</div>");	
			}
			
			// Add set dirty form on change
			FormJSHelper.appendFlexiFormDirty(sb, fileElem.getRootForm(), fileComp.getFormDispatchId());
		} else {
			// Read only view
			sb.append("<span id=\"").append(id).append("\" ")
			  .append(FormJSHelper.getRawJSFor(fileElem.getRootForm(), id, fileElem.getAction()))
			  .append(" >")
			  .append("<input type='text' disabled=\"disabled\" class=\"form-control o_disabled\" size=\"")
			  .append("\" value=\"")
			  .append(StringEscapeUtils.escapeHtml(fileName)).append("\" ")
			  .append("\" />")
			  .append("</span>");
		}
	}
}
