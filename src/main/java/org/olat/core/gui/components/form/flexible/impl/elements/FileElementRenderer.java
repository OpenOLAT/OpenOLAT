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
import org.olat.core.gui.util.CSSHelper;
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

	protected Translator getTranslator(Translator fallbackTranslator) {
		return Util.createPackageTranslator(
				FileElementRenderer.class, fallbackTranslator.getLocale(), fallbackTranslator);
	}

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
		boolean hasFile = true;
		String fileName = fileElem.getUploadFileName();
		if (fileName == null) {
			// try fallback: default file
			File initialFile = fileElem.getInitialFile();
			if (initialFile != null) {
				fileName = initialFile.getName();
			} else {
				fileName = "";
				hasFile = false;
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
			

			// Show available file and delete button
			boolean showReplaceButton = hasFile && fileElem.isReplaceButton() && (fileElem.getInitialFile() != null || fileElem.getUploadFile() != null);
			if (hasFile) {
				sb.append("<div class='o_filemeta'>").append(CSSHelper.getIcon(CSSHelper.createFiletypeIconCssClassFor(fileName)));
				sb.appendHtmlEscaped(fileName);
				sb.append("<span class='o_filesize text-muted'>(").append(Formatter.formatBytes(fileElem.getUploadSize())).append(")</span>");
				
				if (showReplaceButton) {
					renderFileInput(sb, trans, fileElem, id, fileName, showReplaceButton);
				}
				boolean showDeleteButton = fileElem.isDeleteEnabled()
						&& (fileElem.getInitialFile() != null || fileElem.getUploadFile() != null);
				if(showDeleteButton) {
					sb.append("<a class='btn btn-xs btn-default o_filedelete' href=\"javascript:")
					.append(FormJSHelper.getXHRFnCallFor(fileElem.getRootForm(), fileComp.getFormDispatchId(), 1, false, false, new NameValuePair("delete", "delete")))
					.append(";\" onclick=\"\" ")
					.append(" title=\"").appendHtmlEscaped(trans.translate("file.element.delete")).append("\" ><i class='o_icon o_icon_delete_item'> </i> ").append(trans.translate("delete")).append("</a>");
				}
				sb.append("</div>");
			}
			
			if (!showReplaceButton) {
				renderFileInput(sb, trans, fileElem, id, fileName, showReplaceButton);
			}
			
			// Add example text and  max upload size
			if(fileElem.getExampleText() != null) {
				sb.append("<div class='help-block'>")
				  .append(fileElem.getExampleText());
				if (fileElem.getMaxUploadSizeKB() != FileElement.UPLOAD_UNLIMITED) {
					String maxUpload = Formatter.formatBytes(fileElem.getMaxUploadSizeKB() * 1000);
					sb.append(" (")
					  .append(trans.translate("file.element.select.maxsize", maxUpload))
					  .append(")");
				}
				sb.append("</div>");	
			} else if (fileElem.getMaxUploadSizeKB() != FileElement.UPLOAD_UNLIMITED) {
				String maxUpload = Formatter.formatBytes(fileElem.getMaxUploadSizeKB() * 1000);
				sb.append("<div class='help-block o_maxsize'>(")
				.append(trans.translate("file.element.select.maxsize", maxUpload))
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
			  .appendHtmlEscaped(fileName).append("\" ")
			  .append("\" />")
			  .append("</span>");
		}
	}

	private void renderFileInput(StringOutput sb, Translator trans, FileElementImpl fileElem, String id,
			String fileName, boolean showReplaceButton) {
		sb.append("<div class='o_fileinput")
		  .append(" o_sel_file_uploaded", fileElem.getUploadFile() != null)
		  .append(" o_area panel-placeholder", fileElem.isArea() && !showReplaceButton)
		  .append(" o_preview", !fileElem.isButtonsEnabled())
		  .append("'");
		if(fileElem.isArea()) {
			sb.append(" ondragover=\"jQuery(this).addClass('o_dnd_over')\" ondragleave=\"jQuery(this).removeClass('o_dnd_over')\"");				
		}
		sb.append(">");
		
		// input.Browse is the real filebrowser, but set to be transparent. 
		// the div.o_fakechooser is layered below the input.Browse and represents the visual GUI. 
		// Since input.Browse is layered above div.o_fakechooser, all click events to go input.Browse
		// See http://www.quirksmode.org/dom/inputfile.html
		if (fileElem.isButtonsEnabled()) {
			sb.append("<input type='file' name=\"");
			 sb.append(id); // name for form labeling
			 sb.append("\" id=\"");
			 sb.append(id); // id to make dirty button work
			 if (fileElem.getMaxUploadSizeKB() != FileElement.UPLOAD_UNLIMITED) {
				 sb.append("\" data-max-size=\"").append(fileElem.getMaxUploadSizeKB() * 1024l);
			 }
			 sb.append("\" class='form-control o_realchooser' tabindex='0' ");
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
			 sb.append(" onblur=\"try { this.form.fake_").append(id).append(".nextSibling.style.border = '0'; } catch(e) {}\"");
			 // Add select text (hover)
			 sb.append(" title=\"").appendHtmlEscaped(trans.translate("file.element.select")).append("\" />");
		}
		
		if(showReplaceButton) {
			sb.append("<div class='o_file_replace btn btn-xs btn-default ' aria-hidden='true'>");
			sb.append("<i class='o_icon o_icon_upload'> </i> ").append(trans.translate("replace"));
			sb.append("</div>");
		} else if(fileElem.isArea()) {
			sb.append("<div class='o_dnd' aria-hidden='true'>");
			sb.append("<div class='o_dnd_icon'><i class='o_icon o_icon o_icon_upload'></i></div>");
			sb.append("<div class='o_dnd_info'>").append(trans.translate("file.element.dnd.info")).append("</div>");
			sb.append("<div class='o_dnd_select'><button class='btn btn-xs btn-default' tabindex='-1'><span>");
			sb.append(trans.translate("file.element.dnd.select")).append("</span></button></div>");
			sb.append("</div>");
			
		} else {
			// Add the visible but fake input field and a styled faked file chooser button
			sb.append("<div class='o_fakechooser input-group' aria-hidden='true'>");
			sb.append("<input class='form-control' name='fake_").append(id).append("' value=\"").appendHtmlEscaped(fileName)
			  .append("\" placeholder=\"").appendHtmlEscaped(trans.translate("file.element.select")).append("\" tabindex='-1' />");  
			sb.append("<span class='input-group-addon'><a href='javascript:;' tabindex='-1'><i class='o_icon o_icon_upload'> </i></a></span>");
			sb.append("</div>");				
		}
		
		sb.append("</div>"); // End o_fileinput
	}
}
