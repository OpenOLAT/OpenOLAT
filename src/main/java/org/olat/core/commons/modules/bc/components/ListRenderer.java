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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.commons.modules.bc.FileSelection;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.MetaInfoFactory;
import org.olat.core.commons.modules.bc.meta.MetaInfoHelper;
import org.olat.core.gui.control.generic.folder.FolderHelper;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.AbstractVirtualContainer;
import org.olat.core.util.vfs.OlatRelPathImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.version.Versionable;
import org.olat.core.util.vfs.version.Versions;

/**
 * Initial Date:  Feb 12, 2004
 *
 * @author Mike Stock
 */
public class ListRenderer {

	/** Edit parameter identifier. */
	public static final String PARAM_EDTID = "fcedt";
	/** Edit parameter identifier. */
 	public static final String PARAM_CONTENTEDITID = "contentedit";
 	/** Serve resource identifier */
 	public static final String PARAM_SERV = "serv";
	/** Sort parameter identifier. */
	public static final String PARAM_SORTID = "fcsrt";
	/** View version parameter identifier. */
	public static final String PARAM_VERID = "fcver";
	/** Add to ePortfolio parameter identifier. */
	public static final String PARAM_EPORT = "epadd";
	/** View thumbnail */
	public static final String PARAM_SERV_THUMBNAIL = "servthumb";
	
	/** dummy file types */
	private static final String TYPE_FILE = "file";

 	private boolean bgFlag = true;
 	
	/**
	 * Default constructor.
	 */
	public ListRenderer() { super(); } 

	/**
	 * Render contents of directory to a html table.
	 * 
	 * @param dir
	 * @param secCallback
	 * @param ubu
	 * @param translator
	 * @param iframePostEnabled
	 * @return Render results.
	 */
	public String render(FolderComponent fc, URLBuilder ubu, Translator translator, boolean iframePostEnabled) {
		StringOutput sb = new StringOutput();		

		List<VFSItem> children = fc.getCurrentContainerChildren();
		// folder empty?
		if (children.size() == 0) {
			sb.append("<div class=\"b_briefcase_empty\">");
			sb.append(translator.translate("NoFiles"));
			sb.append("</div>");
			return sb.toString();
		}

		boolean canVersion = FolderConfig.versionsEnabled(fc.getCurrentContainer());
		
		sb.append("<table class=\"b_briefcase_filetable\">");
		// header
		sb.append("<thead><tr><th class=\"b_briefcase_col_name b_first_child\">");
		
		// TODO:laeb: set css class depending on sorting state like following and add sort arrow pics as css background image
//		String cssClass;
//		if (FolderComponent.sortCol.equals(FolderComponent.SORT_NAME)) {
//			if (FolderComponent.sortAsc) cssClass = "o_cfc_col_srt_asc";
//			else												 cssClass = "o_cfc_col_srt_desc";
//		} else												 cssClass = "o_cfc_col_unsorted";
		
		sb.append("<a href=\"");																																							// file name column 
		ubu.buildURI(sb, new String[] { PARAM_SORTID }, new String[] { FolderComponent.SORT_NAME }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		sb.append("\"");
		if (iframePostEnabled) { // add ajax iframe target
			StringOutput so = new StringOutput();
			ubu.appendTarget(so);
			sb.append(so.toString());
		}
		sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("header.Name"))).append("\">")
			.append(translator.translate("header.Name")).append("</a>");
		sb.append("</th><th class=\"b_briefcase_col_size\">");
		
		sb.append("<a href=\"");																																							// file size column
		ubu.buildURI(sb, new String[] { PARAM_SORTID }, new String[] { FolderComponent.SORT_SIZE }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		sb.append("\"");
		if (iframePostEnabled) { // add ajax iframe target
			StringOutput so = new StringOutput();
			ubu.appendTarget(so);
			sb.append(so.toString());
		}
		sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("header.Size"))).append("\">")
			.append(translator.translate("header.Size")).append("</a>");

		sb.append("</th><th class=\"b_briefcase_col_type\">");

		sb.append("<a href=\"");																																							// file type column
		ubu.buildURI(sb, new String[] { PARAM_SORTID }, new String[] { FolderComponent.SORT_TYPE }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		sb.append("\"");
		if (iframePostEnabled) { // add ajax iframe target
			StringOutput so = new StringOutput();
			ubu.appendTarget(so);
			sb.append(so.toString());
		}
		sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("header.Type"))).append("\">")
			.append(translator.translate("header.Type")).append("</a>");
		
		if(canVersion) {
			sb.append("</th><th class=\"b_briefcase_col_rev\">")
				.append("<a href=\"");																																							// file size column
			ubu.buildURI(sb, new String[] { PARAM_SORTID }, new String[] { FolderComponent.SORT_REV }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
			sb.append("\"");
			if (iframePostEnabled) { // add ajax iframe target
				StringOutput so = new StringOutput();
				ubu.appendTarget(so);
				sb.append(so.toString());
			}
			sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("header.Version"))).append("\">")
				.append(translator.translate("header.Version")).append("</a>");
		}
		
		sb.append("</th><th class=\"b_briefcase_col_date\">");

		sb.append("<a href=\"");																																							// file modification date column
		ubu.buildURI(sb, new String[] { PARAM_SORTID }, new String[] { FolderComponent.SORT_DATE }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		sb.append("\"");
		if (iframePostEnabled) { // add ajax iframe target
			StringOutput so = new StringOutput();
			ubu.appendTarget(so);
			sb.append(so.toString());
		}
		sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("header.Modified"))).append("\">")
			.append(translator.translate("header.Modified")).append("</a>");
		sb.append("</th>");
		sb.append("<th class=\"b_briefcase_col_info\">");
		
		
		
		sb.append("<a href=\"");																																							// file lock
		ubu.buildURI(sb, new String[] { PARAM_SORTID }, new String[] { FolderComponent.SORT_LOCK }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		sb.append("\"");
		if (iframePostEnabled) { // add ajax iframe target
			StringOutput so = new StringOutput();
			ubu.appendTarget(so);
			sb.append(so.toString());
		}
		sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("header.Status"))).append("\">")
			.append(translator.translate("header.Status")).append("</a>");
		
		// meta data column
		sb.append("</th><th class=\"b_briefcase_col_info b_last_child\"><span");
		sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("header.Info"))).append("\">");
		sb.append(translator.translate("header.Info"));
		sb.append("</span></th></tr></thead>");
				
		// render directory contents
		String currentContainerPath = fc.getCurrentContainerPath();
		if (currentContainerPath.length() > 0 && currentContainerPath.charAt(0) == '/')
			currentContainerPath = currentContainerPath.substring(1);
		bgFlag = true;
		sb.append("<tbody>");
		
		Map<Long,Identity> identityMap = new HashMap<Long,Identity>();
		for (int i = 0; i < children.size(); i++) {
			VFSItem child = children.get(i);
			appendRenderedFile(fc, child, currentContainerPath, sb, ubu, translator, iframePostEnabled, canVersion, identityMap, i);
		}		
		sb.append("</tbody></table>");
		return sb.toString();
	} // getRenderedDirectoryContent
	
	/**
	 * Render a single file or folder.
	 * 
	 * @param	f			The file or folder to render
	 * @param	sb		StringOutput to append generated html code
	 */
	private void appendRenderedFile(FolderComponent fc, VFSItem child, String currentContainerPath, StringOutput sb, URLBuilder ubu, Translator translator,
			boolean iframePostEnabled, boolean canContainerVersion, Map<Long,Identity> identityMap, int pos) {
	
		// asume full access unless security callback tells us something different.
		boolean canWrite = child.getParentContainer().canWrite() == VFSConstants.YES;
		boolean canDelete = child.getParentContainer().canDelete() == VFSConstants.YES;
		boolean isAbstract = (child instanceof AbstractVirtualContainer);

		Versions versions = null;
		if(canContainerVersion && child instanceof Versionable) {
			Versionable versionable = (Versionable)child;
			if(versionable.getVersions().isVersioned()) {
				versions = versionable.getVersions();
			}
		}
		boolean canVersion = versions != null && !versions.getRevisions().isEmpty();
		
		boolean canAddToEPortfolio = FolderConfig.isEPortfolioAddEnabled();
		
		VFSLeaf leaf = null;
		if (child instanceof VFSLeaf) {
			leaf = (VFSLeaf)child;
		}
		boolean isContainer = (leaf == null); // if not a leaf, it must be a container...
		
		MetaInfo metaInfo = null;
		if(child instanceof OlatRelPathImpl) {
			metaInfo = MetaInfoFactory.createMetaInfoFor((OlatRelPathImpl)child);
		}
		boolean lockedForUser = MetaInfoHelper.isLocked(metaInfo, fc.getIdentityEnvironnement());
		
		String name = child.getName();
		String pathAndName = currentContainerPath;
		if (pathAndName.length() > 0 && !pathAndName.endsWith("/"))
			pathAndName = pathAndName + "/";
		pathAndName = pathAndName + name;
		String type = FolderHelper.extractFileType(child.getName(), translator.getLocale());
				
		// tr begin, set alternating bgcolor
		sb.append("<tr");
		bgFlag = !bgFlag;
		if (bgFlag) { sb.append(" class=\"b_table_odd\""); }
		sb.append("><td class=\"b_first_child\">");

		// add checkbox for actions if user can write or delete to this directory
		if (canWrite || canDelete) {
			sb.append("<input type=\"checkbox\" class=\"b_checkbox\" name=\"");
			sb.append(FileSelection.FORM_ID);
			sb.append("\" value=\"");
			sb.append(StringEscapeUtils.escapeHtml(name));
			sb.append("\" />");
		}		
		
		// browse link pre
		sb.append("<a href=\"");
		if (isContainer) { // for directories... normal module URIs
			ubu.buildURI(sb, null, null, pathAndName, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
			sb.append("\"");
			if (iframePostEnabled) { // add ajax iframe target
				StringOutput so = new StringOutput();
				ubu.appendTarget(so);
				sb.append(so.toString());
			}
		} else { // for files, add PARAM_SERV command
			ubu.buildURI(sb, new String[] { PARAM_SERV }, new String[] { "x" }, pathAndName, AJAXFlags.MODE_NORMAL);
			sb.append("\" target=\"_blank\"");
		}
		// icon css
		sb.append(" class=\"b_with_small_icon_left ");
		if (isContainer) sb.append(CSSHelper.CSS_CLASS_FILETYPE_FOLDER);
		else sb.append(CSSHelper.createFiletypeIconCssClassFor(name));
		sb.append("\"");
		// file metadata
		if (metaInfo != null) {
			sb.append(" ext:qtip=\"<div class='b_ext_tooltip_wrapper b_briefcase_meta'>");
			if (StringHelper.containsNonWhitespace(metaInfo.getTitle())) {				
				sb.append("<h5>").append(Formatter.escapeDoubleQuotes(metaInfo.getTitle())).append("</h5>");		
			}
			if (StringHelper.containsNonWhitespace(metaInfo.getComment())) {
				sb.append(Formatter.escapeDoubleQuotes(metaInfo.getComment()));			
			}
			if(metaInfo.isThumbnailAvailable()) {
				sb.append("<div class='b_briefcase_preview' style='width:200px; height:200px; background-image:url("); 
				ubu.buildURI(sb, new String[] { PARAM_SERV_THUMBNAIL}, new String[] { "x" }, pathAndName, AJAXFlags.MODE_NORMAL);
				sb.append("); background-repeat:no-repeat; background-position:50% 50%;'>&nbsp;</div>");
			}

			String author = metaInfo.getAuthor();
			if (StringHelper.containsNonWhitespace(author)) {
				sb.append("<p>").append(Formatter.escapeDoubleQuotes(translator.translate("mf.author")));
				sb.append(": ").append(Formatter.escapeDoubleQuotes(author)).append("</p>");			
			}
			sb.append("</div>\"");
		}
		sb.append(">");
		// name
		if (isAbstract) sb.append("<i>");
		sb.append(name);
		if (isAbstract) sb.append("</i>");
		sb.append("</a></td><td>");
		
		// filesize
		if (!isContainer) {
			// append filesize
			sb.append(StringHelper.formatMemory(leaf.getSize()));
		}
		sb.append("</td><td>");

		// type
		if (isContainer) {
			sb.append(translator.translate("Directory"));
		}
		else {
			if (type.equals(TYPE_FILE)) {
				sb.append(translator.translate("UnknownFile"));
			}
			else {
				sb.append(type.toUpperCase());
				sb.append(" ").append(translator.translate(TYPE_FILE));
			}
		}
		sb.append("</td><td>");
		
		if(canContainerVersion) {
			if (canVersion)
				sb.append(versions.getRevisionNr());
			sb.append("</td><td>");
		}
		
		// last modified
		long lastModified = child.getLastModified();
		if (lastModified != VFSConstants.UNDEFINED)
			sb.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, translator.getLocale()).format(new Date(lastModified)));
		else
			sb.append("-");
		sb.append("</td><td>");
		
		//locked
		if(metaInfo != null) {
			if(metaInfo.isLocked()) {
				Identity lockedBy = identityMap.get(metaInfo.getLockedBy());
				if(lockedBy == null) {
					lockedBy = metaInfo.getLockedByIdentity();
					if(lockedBy != null) {
						identityMap.put(lockedBy.getKey(), lockedBy);
					}
				}
				
				sb.append("<span class=\"b_small_icon b_briefcase_locked_file_icon\" ext:qtip=\"");
				if(lockedBy != null) {
					String firstName = lockedBy.getUser().getProperty(UserConstants.FIRSTNAME, translator.getLocale());
					String lastName = lockedBy.getUser().getProperty(UserConstants.LASTNAME, translator.getLocale());
					String date = "";
					if(metaInfo.getLockedDate() != null) {
						date = fc.getDateTimeFormat().format(metaInfo.getLockedDate());
					}
					sb.append(translator.translate("Locked", new String[]{firstName, lastName, date}));
				}
				sb.append("\">&#160;</span>");
			}
		}
		sb.append("</td><td class=\"b_last_child\">");

		// Info link
		if (canWrite) {
			sb.append("<table class=\"b_briefcase_actions\"><tr><td>");

			// versions action
			if (canVersion) {
				// Versions link
				if (lockedForUser) {
					sb.append("<span ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("versions")))
							.append("\" class=\" b_small_icon b_briefcase_versions_dis_icon\">&#160;</span>");
				} else {
					sb.append("<a href=\"");
					ubu.buildURI(sb, new String[] { PARAM_VERID }, new String[] { Integer.toString(pos) }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME
							: AJAXFlags.MODE_NORMAL);
					sb.append("\"");
					if (iframePostEnabled) { // add ajax iframe target
						StringOutput so = new StringOutput();
						ubu.appendTarget(so);
						sb.append(so.toString());
					}
					sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("versions")))
							.append("\" class=\" b_small_icon b_briefcase_versions_icon\">&#160;</a>");
				}
			} else {
				sb.append("<span class=\"b_small_icon b_briefcase_noicon\">&#160;</span>");									
			}
			sb.append("</td><td>");

			// content edit action
			String nameLowerCase = name.toLowerCase();
			if (!lockedForUser && (nameLowerCase.endsWith(".html") || nameLowerCase.endsWith(".htm") || nameLowerCase.endsWith(".txt") || nameLowerCase.endsWith(".css"))) {

				sb.append("<a href=\"");
				ubu.buildURI(sb, new String[] { PARAM_CONTENTEDITID }, new String[] { Integer.toString(pos) }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME
						: AJAXFlags.MODE_NORMAL);
				sb.append("\"");
				if (iframePostEnabled) { // add ajax iframe target
					StringOutput so = new StringOutput();
					ubu.appendTarget(so);
					sb.append(so.toString());
				}
				sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("editor")));
				sb.append("\" class=\"b_small_icon b_briefcase_edit_file_icon\">&#160;</a>");
			} else {
				sb.append("<span class=\"b_small_icon b_briefcase_noicon\">&#160;</span>");	
			}
			sb.append("</td><td>");
			
			// eportfolio collect action
			// get a link for adding a file to ePortfolio, if file-owner is the current user
			if (canAddToEPortfolio && !isContainer) {
				if (metaInfo != null) {
					Identity author = metaInfo.getAuthorIdentity();
					if (author != null && fc.getIdentityEnvironnement().getIdentity().getKey().equals(author.getKey())) {
						sb.append("<a href=\"");
						ubu.buildURI(sb, new String[] { PARAM_EPORT }, new String[] { Integer.toString(pos) }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME
								: AJAXFlags.MODE_NORMAL);
						sb.append("\"");
						if (iframePostEnabled) { // add ajax iframe target
							StringOutput so = new StringOutput();
							ubu.appendTarget(so);
							sb.append(so.toString());
						}
						sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("eportfolio")))
								.append("\" class=\" b_small_icon b_eportfolio_add\">&#160;</a>");
					} else {
						sb.append("<span class=\"b_small_icon b_briefcase_noicon\">&#160;</span>");					
					}
				}
			} else {
				sb.append("<span class=\"b_small_icon b_briefcase_noicon\">&#160;</span>");									
			}
			sb.append("</td><td>");

			// meta edit action (rename etc)
			boolean canMetaData = MetaInfoHelper.canMetaInfo(child);
			if (canMetaData) {
				if (lockedForUser) {
					// Metadata link disabled...
					sb.append("<span ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("edit")))
							.append("\" class=\" b_small_icon b_briefcase_edit_meta_dis_icon\">&#160;</span>");
				} else {
					// Metadata edit link... also handles rename for non-OlatRelPathImpls
					sb.append("<a href=\"");
					ubu.buildURI(sb, new String[] { PARAM_EDTID }, new String[] { Integer.toString(pos) }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME
							: AJAXFlags.MODE_NORMAL);
					sb.append("\"");
					if (iframePostEnabled) { // add ajax iframe target
						StringOutput so = new StringOutput();
						ubu.appendTarget(so);
						sb.append(so.toString());
					}
					sb.append(" ext:qtip=\"").append(StringEscapeUtils.escapeHtml(translator.translate("mf.edit")))
							.append("\" class=\" b_small_icon b_briefcase_edit_meta_icon\">&#160;</a>");
				}
			} else {
				sb.append("<span class=\"b_small_icon b_briefcase_noicon\">&#160;</span>");					
			}
			
			sb.append("</td></tr></table>");
		} else {
			sb.append("&nbsp;");
		}

		sb.append("</td></tr>");
	}
}