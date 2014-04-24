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
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FileSelection;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.gui.control.generic.folder.FolderHelper;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.AbstractVirtualContainer;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSLockManager;
import org.olat.core.util.vfs.lock.LockInfo;
import org.olat.core.util.vfs.version.Versionable;
import org.olat.core.util.vfs.version.Versions;
import org.olat.user.UserManager;

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
	
	private VFSLockManager lockManager;
	private UserManager userManager;

 	private boolean bgFlag = true;
 	
	/**
	 * Default constructor.
	 */
	public ListRenderer() {
		//
	} 

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
	public void render(FolderComponent fc, StringOutput sb, URLBuilder ubu, Translator translator, boolean iframePostEnabled) {
		if(lockManager == null) {
			lockManager = CoreSpringFactory.getImpl(VFSLockManager.class);
		}
		if(userManager == null) {
			userManager = CoreSpringFactory.getImpl(UserManager.class);
		}

		List<VFSItem> children = fc.getCurrentContainerChildren();
		// folder empty?
		if (children.size() == 0) {
			sb.append("<div class=\"b_briefcase_empty\">");
			sb.append(translator.translate("NoFiles"));
			sb.append("</div>");
			return;
		}

		boolean canVersion = FolderConfig.versionsEnabled(fc.getCurrentContainer());
		
		sb.append("<table class=\"table table-bordered b_briefcase_filetable\">");
		// header
		sb.append("<thead><tr><th class=\"b_briefcase_col_name b_first_child\">");

		sb.append("<a href=\"");																																							// file name column 
		ubu.buildURI(sb, new String[] { PARAM_SORTID }, new String[] { FolderComponent.SORT_NAME }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		sb.append("\"");
		if (iframePostEnabled) { // add ajax iframe target
			StringOutput so = new StringOutput();
			ubu.appendTarget(so);
			sb.append(so.toString());
		}
		sb.append(">").append(translator.translate("header.Name")).append("</a>");
		sb.append("</th><th class=\"b_briefcase_col_size\">");
		
		sb.append("<a href=\"");																																							// file size column
		ubu.buildURI(sb, new String[] { PARAM_SORTID }, new String[] { FolderComponent.SORT_SIZE }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		sb.append("\"");
		if (iframePostEnabled) { // add ajax iframe target
			StringOutput so = new StringOutput();
			ubu.appendTarget(so);
			sb.append(so.toString());
		}
		sb.append(">").append(translator.translate("header.Size")).append("</a>");
		sb.append("</th><th class=\"b_briefcase_col_type\">");

		sb.append("<a href=\"");																																							// file type column
		ubu.buildURI(sb, new String[] { PARAM_SORTID }, new String[] { FolderComponent.SORT_TYPE }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		sb.append("\"");
		if (iframePostEnabled) { // add ajax iframe target
			StringOutput so = new StringOutput();
			ubu.appendTarget(so);
			sb.append(so.toString());
		}
		sb.append(">").append(translator.translate("header.Type")).append("</a>");
		
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
			sb.append(">").append(translator.translate("header.Version")).append("</a>");
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
		sb.append(">").append(translator.translate("header.Modified")).append("</a>")
		  .append("</th>").append("<th class=\"b_briefcase_col_info\">");

		sb.append("<a href=\"");																																							// file lock
		ubu.buildURI(sb, new String[] { PARAM_SORTID }, new String[] { FolderComponent.SORT_LOCK }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);
		sb.append("\"");
		if (iframePostEnabled) { // add ajax iframe target
			StringOutput so = new StringOutput();
			ubu.appendTarget(so);
			sb.append(so.toString());
		}
		sb.append(">").append(translator.translate("header.Status")).append("</a>");
		
		// meta data column
		sb.append("</th><th class=\"b_briefcase_col_info b_last_child\"><span>")
		  .append(translator.translate("header.Info")).append("</span></th></tr></thead>");
				
		// render directory contents
		String currentContainerPath = fc.getCurrentContainerPath();
		if (currentContainerPath.length() > 0 && currentContainerPath.charAt(0) == '/')
			currentContainerPath = currentContainerPath.substring(1);
		bgFlag = true;
		sb.append("<tbody>");
		
		for (int i = 0; i < children.size(); i++) {
			VFSItem child = children.get(i);
			appendRenderedFile(fc, child, currentContainerPath, sb, ubu, translator, iframePostEnabled, canVersion, i);
		}		
		sb.append("</tbody></table>");
	} // getRenderedDirectoryContent
	
	/**
	 * Render a single file or folder.
	 * 
	 * @param	f			The file or folder to render
	 * @param	sb		StringOutput to append generated html code
	 */
	private void appendRenderedFile(FolderComponent fc, VFSItem child, String currentContainerPath, StringOutput sb, URLBuilder ubu, Translator translator,
			boolean iframePostEnabled, boolean canContainerVersion, int pos) {
	
		// asume full access unless security callback tells us something different.
		boolean canWrite = child.getParentContainer().canWrite() == VFSConstants.YES;
		boolean canDelete = child.getParentContainer().canDelete() == VFSConstants.YES;
		boolean canMail = fc.isCanMail();
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
		if(child instanceof MetaTagged) {
			metaInfo = ((MetaTagged)child).getMetaInfo();
		}
		
		boolean lockedForUser = lockManager.isLockedForMe(child, fc.getIdentityEnvironnement().getIdentity(), fc.getIdentityEnvironnement().getRoles());
		
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

		// add checkbox for actions if user can write, delete or email this directory
		if (canWrite || canDelete || canMail) {
			sb.append("<input type=\"checkbox\" class=\"b_checkbox\" name=\"");
			sb.append(FileSelection.FORM_ID);
			sb.append("\" value=\"");
			sb.append(StringHelper.escapeHtml(name));
			sb.append("\" />");
		}		
		
		// browse link pre
		sb.append("<a id='o_sel_doc_").append(pos).append("' href=\"");
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
		sb.append("\">");
		// name
		if (isAbstract) sb.append("<i>");
		sb.append(name);
		if (isAbstract) sb.append("</i>");
		sb.append("</a>");

		//file metadata as tooltip
		if (metaInfo != null) {
			boolean hasMeta = false;
			sb.append("<div id='o_sel_doc_tooltip_").append(pos).append("' class='b_ext_tooltip_wrapper b_briefcase_meta' style='display:none;'>");
			if (StringHelper.containsNonWhitespace(metaInfo.getTitle())) {
				String title = StringHelper.escapeHtml(metaInfo.getTitle());
				sb.append("<h5>").append(Formatter.escapeDoubleQuotes(title)).append("</h5>");
				hasMeta = true;
			}
			if (StringHelper.containsNonWhitespace(metaInfo.getComment())) {
				sb.append("<div class=\"b_briefcase_comment\">");
				String comment = StringHelper.escapeHtml(metaInfo.getComment());
				sb.append(Formatter.escapeDoubleQuotes(comment));			
				sb.append("</div>");
				hasMeta = true;
			}
			boolean hasThumbnail = false;
			if(metaInfo.isThumbnailAvailable()) {
				sb.append("<div class='b_briefcase_preview' style='width:200px; height:200px; background-image:url("); 
				ubu.buildURI(sb, new String[] { PARAM_SERV_THUMBNAIL}, new String[] { "x" }, pathAndName, AJAXFlags.MODE_NORMAL);
				sb.append("); background-repeat:no-repeat; background-position:50% 50%;'>&nbsp;</div>");
				hasMeta = true;
				hasThumbnail = true;
			}

			// first try author info from metadata (creator)
			boolean hasMetaAuthor = false;
			String author = metaInfo.getCreator();
			// fallback use file author (uploader)
			if (StringHelper.containsNonWhitespace(author)) {
				hasMetaAuthor = true;
			} else {
				author = metaInfo.getAuthor();
				if(!"-".equals(author)) {
					author = UserManager.getInstance().getUserDisplayName(author);
				} else {
					author = null;
				}		
			}
			author = StringHelper.escapeHtml(author);
			if (StringHelper.containsNonWhitespace(author)) {
				sb.append("<p class=\"b_briefcase_author\">").append(Formatter.escapeDoubleQuotes(translator.translate("mf.author")));
				sb.append(": ").append(Formatter.escapeDoubleQuotes(author)).append("</p>");			
				hasMeta = true;
			}
			sb.append("</div>");
			if (hasMeta) {
				// render tooltip only when it contains something
				sb.append("<script type='text/javascript'>")
			    .append("/* <![CDATA[ */")
				  .append("jQuery(function() {")
					.append("  jQuery('#o_sel_doc_").append(pos).append("').tooltip({")
					.append("	  items: 'a', tooltipClass: 'b_briefcase_meta ")
					.append(isContainer ? "b_briefcase_folder " : "b_briefcase_file ")
					.append(hasMetaAuthor ? "b_briefcase_with_meta_author " : "b_briefcase_with_uploader_author ")
					.append(hasThumbnail ? "b_briefcase_with_thumbnail " : "b_briefcase_without_thumbnail ")
					.append("', ")
					.append("     content: function(){ return jQuery('#o_sel_doc_tooltip_").append(pos).append("').html(); }")
					.append("  });")
					.append("});")
					.append("/* ]]> */")
					.append("</script>");
			}
		}
		sb.append("</td><td>");
		
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
		boolean locked = lockManager.isLocked(child);
		if(locked) {
			LockInfo lock = lockManager.getLock(child);
			sb.append("<span class=\"b_small_icon b_briefcase_locked_file_icon\" title=\"");
			if(lock != null && lock.getLockedBy() != null) {
				String fullname = userManager.getUserDisplayName(lock.getLockedBy());
				String date = "";
				if(lock.getCreationDate() != null) {
					date = fc.getDateTimeFormat().format(lock.getCreationDate());
				}
				String msg = translator.translate("Locked", new String[]{fullname, date});
				if(lock.isWebDAVLock()) {
					msg += " (WebDAV)";
				}
				sb.append(msg);
			}
			sb.append("\">&#160;</span>");
		}
		sb.append("</td><td class=\"b_last_child\">");

		// Info link
		if (canWrite) {
			sb.append("<table class=\"b_briefcase_actions\"><tr><td>");

			// versions action
			if (canVersion) {
				// Versions link
				sb.append("<a href=\"");
				ubu.buildURI(sb, new String[] { PARAM_VERID }, new String[] { Integer.toString(pos) }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME
						: AJAXFlags.MODE_NORMAL);
				sb.append("\"");
				if (iframePostEnabled) { // add ajax iframe target
					StringOutput so = new StringOutput();
					ubu.appendTarget(so);
					sb.append(so.toString());
				}
				sb.append(" title=\"").append(StringHelper.escapeHtml(translator.translate("versions")))
						.append("\" class=\" b_small_icon b_briefcase_versions_icon\">&#160;</a>");
			} else {
				sb.append("<span class=\"b_small_icon b_briefcase_noicon\">&#160;</span>");									
			}
			sb.append("</td><td>");

			// content edit action
			String nameLowerCase = name.toLowerCase();
			boolean isLeaf= (child instanceof VFSLeaf); // OO-57 only display edit link if it's not a folder
			if (isLeaf && !lockedForUser && (nameLowerCase.endsWith(".html") || nameLowerCase.endsWith(".htm") || nameLowerCase.endsWith(".txt") || nameLowerCase.endsWith(".css") || nameLowerCase.endsWith(".csv	"))) {

				sb.append("<a href=\"");
				ubu.buildURI(sb, new String[] { PARAM_CONTENTEDITID }, new String[] { Integer.toString(pos) }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME
						: AJAXFlags.MODE_NORMAL);
				sb.append("\"");
				if (iframePostEnabled) { // add ajax iframe target
					StringOutput so = new StringOutput();
					ubu.appendTarget(so);
					sb.append(so.toString());
				}
				sb.append(" title=\"").append(StringHelper.escapeHtml(translator.translate("editor")));
				sb.append("\"><i class='o_icon o_icon_edit_file'></i></a>");
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
						sb.append(" title=\"").append(StringHelper.escapeHtml(translator.translate("eportfolio")))
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
			boolean canMetaData = canMetaInfo(child);
			if (canMetaData) {
				// Metadata edit link... also handles rename for non-OlatRelPathImpls
				sb.append("<a href='");
				ubu.buildURI(sb, new String[] { PARAM_EDTID }, new String[] { Integer.toString(pos) }, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME
						: AJAXFlags.MODE_NORMAL);
				sb.append("'");
				if (iframePostEnabled) { // add ajax iframe target
					StringOutput so = new StringOutput();
					ubu.appendTarget(so);
					sb.append(so.toString());
				}
				sb.append(" title=\"").append(StringHelper.escapeHtml(translator.translate("mf.edit")))
				  .append("\" ><i class='o_icon o_icon_edit_metadata'></i></a>");
			} else {
				sb.append("<span class='b_small_icon b_briefcase_noicon'>&#160;</span>");					
			}
			
			sb.append("</td></tr></table>");
		} else {
			sb.append("&nbsp;");
		}

		sb.append("</td></tr>");
	}
	
	private boolean canMetaInfo(VFSItem item) {
		if (item instanceof NamedContainerImpl) {
			item = ((NamedContainerImpl)item).getDelegate();
		}
		if(item instanceof VFSContainer) {
			String name = item.getName();
			if(name.equals("_sharedfolder_") || name.equals("_courseelementdata")) {
				return false;
			}			
		}
		return true;
	}
}