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

package org.olat.core.commons.modules.bc.components;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.modules.bc.commands.FolderCommandFactory;
import org.olat.core.commons.services.vfs.VFSVersionModule;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;

/**
 * @author Felix Jost
 */
public class FolderComponentRenderer extends DefaultComponentRenderer {

	private final ListRenderer listRenderer;
	private final CrumbRenderer crumbRenderer;

	private VFSVersionModule vfsVersionModule;

	/**
	 * Constructor for TableRenderer. Singleton and must be reentrant
	 * There must be an empty contructor for the Class.forName() call
	 */
	public FolderComponentRenderer() {
		super();
		listRenderer = new ListRenderer();
		crumbRenderer = new CrumbRenderer();
	}

	@Override
	public void renderComponent(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		if(vfsVersionModule == null) {
			vfsVersionModule = CoreSpringFactory.getImpl(VFSVersionModule.class);
		}
		
		FolderComponent fc = (FolderComponent) source;
		// is called for the current inline html
		int renderType = 0;
		if (args != null && args.length > 0) {
			if (args[0].equals("list")) renderType = 0;
			if (args[0].equals("crumb")) renderType = 1;
		}
		// get ajax flag for link rendering
		boolean iframePostEnabled = renderer.getGlobalSettings().getAjaxFlags().isIframePostEnabled();
		
		if (renderType == 1) {
			crumbRenderer.render(fc, target, ubu, iframePostEnabled);
		} else {
			renderList(target, fc, ubu, translator, iframePostEnabled);
		}
	}

	private void renderList(StringOutput target, FolderComponent fc, URLBuilder ubu, Translator translator, boolean iframePostEnabled) {
		
		VFSContainer currentContainer = fc.getCurrentContainer();
		boolean canWrite = currentContainer.canWrite() == VFSConstants.YES;
		boolean canCreateFolder = true;
		if(currentContainer.getLocalSecurityCallback() != null && !currentContainer.getLocalSecurityCallback().canCreateFolder()) {
			canCreateFolder = false;
		}
		
		boolean canDelete = false;
		boolean canVersion = vfsVersionModule.isEnabled() && fc.getCurrentContainer().canVersion() == VFSConstants.YES;
		boolean canMail = fc.isCanMail();
		
		List<VFSItem> children = fc.getCurrentContainerChildren();
		for (VFSItem child:children) {
			if (child.canDelete() == VFSConstants.YES) {
				canDelete = true;
				break;
			}
		}

		String formName = "folder" + CodeHelper.getRAMUniqueID();
		target.append("<form  method=\"post\" id=\"").append(formName).append("\" action=\"");
		ubu.buildURI(target, new String[] { VelocityContainer.COMMAND_ID }, new String[] {FolderRunController.FORM_ACTION }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		target.append("\" ");
		if (iframePostEnabled) { // add ajax iframe target
			target.append(" onsubmit=\"o_XHRSubmit('").append(formName).append("');\">");
			target.append("<input id=\"o_mai_").append(formName).append("\" type=\"hidden\" name=\"multi_action_identifier\" value=\"\"").append(" />");
		} else {
			target.append(" onsubmit=\"if ( b_briefcase_isChecked('").append(formName)
			  .append("', '").append(Formatter.escapeSingleAndDoubleQuotes(StringHelper.escapeHtml(translator.translate("alert")).toString())) 
			  .append("')) { if(o_info.linkbusy) return false; else o_beforeserver(); return true; } else {return false; }\">");
		}

		target.append("<div class=\"o_bc_createactions clearfix\"><ul class='nav navbar-nav navbar-right'>");
		if (canWrite) {
			// add folder actions: upload file, create new folder, create new file

			if(canVersion) {
			// deleted files
				target.append("<li><a class=\"o_bc_deletedfiles\"");
				ubu.buildHrefAndOnclick(target, null, iframePostEnabled, false, false, new NameValuePair(VelocityContainer.COMMAND_ID, "dfiles"))
				   .append(" role='button'><i class='o_icon o_icon_recycle o_icon-fw'></i> ")
				   .append(translator.translate("dfiles"))
				   .append("</a></li>");
			}
			
			if(canWrite) {
				if(fc.getExternContainerForCopy() != null && (fc.getExternContainerForCopy().getLocalSecurityCallback() == null ||
						fc.getExternContainerForCopy().getLocalSecurityCallback().canCopy())) {
					//option copy file
					target.append("<li><a class=\"o_bc_copy\" ");
					ubu.buildHrefAndOnclick(target, null, iframePostEnabled, false, false, new NameValuePair(VelocityContainer.COMMAND_ID, "copyfile" ))
					   .append(" role='button'><i class='o_icon o_icon_copy o_icon-fw'></i> ")
					   .append(translator.translate("copyfile"))
					   .append("</a></li>");
				}
				
				// option upload	
				target.append("<li><a class='o_bc_upload' ");
				ubu.buildHrefAndOnclick(target, null, iframePostEnabled, false, false, new NameValuePair(VelocityContainer.COMMAND_ID, "ul" ))
				   .append(" role='button'><i class='o_icon o_icon_upload o_icon-fw'></i> ")
				   .append(translator.translate("ul"))
				   .append("</a></li>");
	
				if(canCreateFolder) {
					// option new folder
					target.append("<li><a class=\"b_bc_newfolder\" ");
					ubu.buildHrefAndOnclick(target, null, iframePostEnabled, false, false, new NameValuePair(VelocityContainer.COMMAND_ID, "cf" ))
					   .append(" role='button'><i class='o_icon o_icon_new_folder o_icon-fw'></i> ")
					   .append(translator.translate("cf"))
					   .append("</a></li>");
				}
	
				// option new file
				target.append("<li><a class=\"b_bc_newfile\" ");
				ubu.buildHrefAndOnclick(target, null, iframePostEnabled, false, false, new NameValuePair(VelocityContainer.COMMAND_ID, "cfile" ))
				   .append(" role='button'><i class='o_icon o_icon_new_document o_icon-fw'></i> ")
				   .append(translator.translate("cfile"))
				   .append("</a></li>");
			}
		}
		
		//placeholder for the search
		target.append("</ul></div>"); // END o_bc_createactions
		
		// add current file bread crumb path
		crumbRenderer.render(fc, target, ubu, iframePostEnabled);			

		// add file listing for current folder
		target.append("<div class='o_table_wrapper'>");
		listRenderer.render(fc, target, ubu, translator, iframePostEnabled, formName);

		if (!children.isEmpty()) {
			target.append("<div class='o_button_group'>");
			
			if(canMail) {
				target.append("<button type=\"button\" class='btn btn-default' onclick=\"o_TableMultiActionEvent('").append(formName).append("','")
				      .append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_MAIL)
				      .append("');\"><span>").append(StringHelper.escapeHtml(translator.translate("send"))).append("</span></button>");
			}
			
			target.append(" <button type=\"button\" class='btn btn-default' onclick=\"o_TableMultiActionEvent('").append(formName).append("','")
			      .append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_DOWNLOAD_ZIP)
			      .append("');\"><span>").append(StringHelper.escapeHtml(translator.translate("download"))).append("</span></button>");
			
			if (canDelete) {
				// delete
				target.append(" <button type=\"button\" class='btn btn-default' onclick=\"o_TableMultiActionEvent('").append(formName).append("','")
				      .append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_DEL)
				      .append("');\"><span>").append(StringHelper.escapeHtml(translator.translate("del"))).append("</span></button>");
			}

			if (canWrite) {
				// move
				target.append(" <button type=\"button\" class='btn btn-default' onclick=\"o_TableMultiActionEvent('").append(formName).append("','")
				      .append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_MOVE)
			          .append("');\"><span>").append(StringHelper.escapeHtml(translator.translate("move"))).append("</span></button>");
				// copy
				target.append(" <button type=\"button\" class='btn btn-default' onclick=\"o_TableMultiActionEvent('").append(formName).append("','")
				      .append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_COPY)
			         .append("');\"><span>").append(StringHelper.escapeHtml(translator.translate("copy"))).append("</span></button>");
				// zip
				target.append(" <button type=\"button\" class='btn btn-default' onclick=\"o_TableMultiActionEvent('").append(formName).append("','")
				      .append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_ZIP)
			          .append("');\"><span>").append(StringHelper.escapeHtml(translator.translate("zip"))).append("</span></button>");
				//unzip
				target.append(" <button type=\"button\" class='btn btn-default' onclick=\"o_TableMultiActionEvent('").append(formName).append("','")
				      .append(FolderRunController.ACTION_PRE).append(FolderCommandFactory.COMMAND_UNZIP)
			         .append("');\"><span>").append(StringHelper.escapeHtml(translator.translate("unzip"))).append("</span></button>");		
			}
			target.append("</div>"); // END o_button_group
		}
		target.append("</div></form>"); // END o_table_wrapper
	}
}
