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

package org.olat.modules.cp;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.ExportUtil;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.fileresource.FileResourceManager;

/**
 * Description: <br>
 * Provides functionality to make a IMS-Content-Packaging offline readable with
 * menu just using a browser. The menu, an ordinary
 * <ul>
 * list, is turned into a dynamic, expandable, collapsible tree structure with
 * support from www.mattkruse.com/javascript. The Menu resides in the frame
 * FRAME_NAME_MENU and the Content in FRAME_NAME_CONTENT.
 * 
 * @author alex
 */

public class CPOfflineReadableManager {
	private static CPOfflineReadableManager instance = new CPOfflineReadableManager();
	
	private static final String DOCTYPE = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">";
	private static final String IMSMANIFEST = "imsmanifest.xml";

	public static final String CPOFFLINEMENUMAT = "cp_offline_menu_mat";
	private static final String OLATICON = "olat_icon.gif";
	private static final String FAVICON = "favicon.ico";
	private static final String BRANDING = "provided by OpenOLAT";
	private static final String MKTREEJS = "mktree.js"; // mattkruseTree ->
																							// www.mattkruse.com
	private static final String MKTREECSS = "mktree.css";

	private static final String MENU_FILE = "_MENU_.html";
	private static final String FRAME_FILE = "_START_.html";
	private static final String LOGO_FILE = "_LOGO_.html";
	private static final String FRAME_NAME_MENU = "menu";
	private static final String FRAME_NAME_CONTENT = "content";
	private static final String FRAME_NAME_LOGO = "logo";

	private String rootTitle;

	private CPOfflineReadableManager() {
	// private since singleton
	}

	/**
	 * @return instance of CPOfflineReadableManager
	 */
	public static CPOfflineReadableManager getInstance() {
		return instance;
	}
	
		/**
	 * Used for migration purposes
	 * 
	 * @param unzippedDir
	 * @param targetZip
	 * @param cpOfflineMat
	 */
	public void makeCPOfflineReadable(File unzippedDir, File targetZip, File cpOfflineMat) {
		writeOfflineCP(unzippedDir);
		//assign default mat if not specified
		if(cpOfflineMat == null) cpOfflineMat = new File(WebappHelper.getContextRoot() + "/static/" + CPOFFLINEMENUMAT);
		zipOfflineReadableCP(unzippedDir, targetZip, cpOfflineMat);
	}

	/**
	 * Adds the folder CPOFFLINEMENUMAT and the two files MENU_FILE and FRAME_FILE
	 * to the _unzipped_-Folder.
	 * 
	 * @param ores
	 * @param zipName
	 */
	public void makeCPOfflineReadable(OLATResourceable ores, String zipName) {
		String repositoryHome = FolderConfig.getCanonicalRepositoryHome();
		FileResourceManager fm = FileResourceManager.getInstance();
		String relPath = fm.getUnzippedDirRel(ores);
		String resId = ores.getResourceableId().toString();

		File unzippedDir = new File(repositoryHome + "/" + relPath);
		File targetZip = new File(repositoryHome + "/" + resId + "/" + zipName);
		File cpOfflineMat = new File(WebappHelper.getContextRoot() + "/static/" + CPOFFLINEMENUMAT);

		writeOfflineCP(unzippedDir);
		zipOfflineReadableCP(unzippedDir, targetZip, cpOfflineMat);
	}

	/**
	 * writes the MENU_FILE to the _unzipped_-Folder
	 * 
	 * @param unzippedDir
	 */
	private void writeOfflineCP(File unzippedDir) {
		File mani = new File(unzippedDir, IMSMANIFEST);
		String s = createMenuAndFrame(unzippedDir, mani);

		File f = new File(unzippedDir, MENU_FILE);
		if (f.exists()) {
			FileUtils.deleteDirsAndFiles(f, false, true);
		}
		ExportUtil.writeContentToFile(MENU_FILE, s, unzippedDir, "utf-8");
	}

	/**
	 * creates menu from imsmanifest.xml
	 * 
	 * @param unzippedDir
	 * @param mani
	 * @return
	 */
	private String createMenuAndFrame(File unzippedDir, File mani) {
		LocalFileImpl vfsMani = new LocalFileImpl(mani);
		CPManifestTreeModel ctm = new CPManifestTreeModel(vfsMani);
		TreeNode root = ctm.getRootNode();
		this.rootTitle = root.getTitle();
		StringBuilder sb = new StringBuilder();
		sb.append(DOCTYPE);
		sb.append("<html>\n<head>\n");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		sb.append("<title>");
		sb.append(rootTitle);
		sb.append("</title>\n");
		sb.append("<SCRIPT SRC=\"");
		sb.append(CPOFFLINEMENUMAT);
		sb.append("/");
		sb.append(MKTREEJS);
		sb.append("\" LANGUAGE=\"JavaScript\"></SCRIPT>");
		sb.append("<LINK REL=\"stylesheet\" HREF=\"");
		sb.append(CPOFFLINEMENUMAT);
		sb.append("/");
		sb.append(MKTREECSS);
		sb.append("\"></head>\n<body>\n");
		sb.append("<div>");
		sb.append("<a href=\"#\" onclick=\"expandTree('tree1'); return false;\">Expand All</a>&nbsp;&nbsp;&nbsp;");
		sb.append("<a href=\"#\" onclick=\"collapseTree('tree1'); return false;\">Collapse All</a>&nbsp;&nbsp;&nbsp;");
		sb.append("<ul class=\"mktree\" ID=\"tree1\">");
		render(root, sb, 0);
		sb.append("</ul>");
		sb.append("</div>");
		sb.append("</body>");

		writeOfflineHTMLFrameSetFile(unzippedDir);
		writeOfflineHTMLLogoFrame(unzippedDir);

		return sb.toString();
	}

	/**
	 * @param node
	 * @param sb
	 * @param indent
	 */
	private void render(TreeNode node, StringBuilder sb, int indent) {
		// set content to first accessible child or root node if no children
		// available
		// render current node

		String nodeUri = (String) node.getUserObject();
		String title = node.getTitle();
		String altText = node.getAltText();

		sb.append("<li>\n");
		if (node.isAccessible()) {
			sb.append("<a href=\"");
			sb.append(nodeUri);
			sb.append("\" target=\"");
			sb.append(FRAME_NAME_CONTENT);
			sb.append("\" alt=\"");
			sb.append(StringEscapeUtils.escapeHtml(altText));
			sb.append("\" title=\"");
			sb.append(StringEscapeUtils.escapeHtml(altText));
			sb.append("\">");
			sb.append(title);
			sb.append("</a>\n");
		} else {
			sb.append("<span title=\"");
			sb.append(StringEscapeUtils.escapeHtml(altText));
			sb.append("\">");
			sb.append(title);
			sb.append("</span>");
		}

		// render all children
		boolean b = true;
		for (int i = 0; i < node.getChildCount(); i++) {
			if (b) {
				sb.append("<ul>\n");
			}
			TreeNode child = (TreeNode) node.getChildAt(i);
			render(child, sb, indent + 1);
			b = false;
		}
		if (!b) {
			sb.append("</ul>\n");
		}
		sb.append("</li>\n");
	}

	/**
	 * writes the FRAME_FILE to the _unzipped_-Folder
	 * 
	 * @param unzippedDir
	 * @param rootTitle
	 */
	private void writeOfflineHTMLFrameSetFile(File unzippedDir) {
		StringBuilder sb = new StringBuilder();
		sb.append(DOCTYPE);
		sb.append("<html>\n<head>\n");
		sb.append("<link rel=\"icon\" href=\"");
		sb.append(CPOFFLINEMENUMAT);
		sb.append("/");
		sb.append(FAVICON);
		sb.append("\" type=\"image/x-icon\">");
		sb.append("<LINK REL=\"stylesheet\" HREF=\"");
		sb.append(CPOFFLINEMENUMAT);
		sb.append("/");
		sb.append(MKTREECSS);
		sb.append("\">");
		sb.append("<title>");
		sb.append(rootTitle);
		sb.append("</title>\n</head>\n");
		sb.append("<frameset cols=\"250,*\" frameborder=\"0\" framespacing=\"0\" border=\"0\">");

		sb.append("<frameset rows=\"*,40\" frameborder=\"0\" framespacing=\"0\" border=\"0\">");

		sb.append("<frame src=\"");
		sb.append(MENU_FILE);
		sb.append("\" name=\"");
		sb.append(FRAME_NAME_MENU);
		sb.append("\">\n");

		sb.append("<frame src=\"");
		sb.append(LOGO_FILE);
		sb.append("\" name=\"");
		sb.append(FRAME_NAME_LOGO);
		sb.append("\">\n");

		sb.append("</frameset>");

		sb.append("<frame name=\"");
		sb.append(FRAME_NAME_CONTENT);
		sb.append("\">\n");

		sb.append("</frameset>\n</html>");

		File f = new File(unzippedDir, FRAME_FILE);
		if (f.exists()) {
			FileUtils.deleteDirsAndFiles(f, false, true);
		}
		ExportUtil.writeContentToFile(FRAME_FILE, sb.toString(), unzippedDir, "utf-8");
	}

	/**
	 * writes the FRAME_FILE to the _unzipped_-Folder
	 * 
	 * @param unzippedDir
	 * @param rootTitle
	 */
	private void writeOfflineHTMLLogoFrame(File unzippedDir) {
		StringBuilder sb = new StringBuilder();
		sb.append(DOCTYPE);
		sb.append("<html>\n<head>\n");
		sb.append("<LINK REL=\"stylesheet\" HREF=\"");
		sb.append(CPOFFLINEMENUMAT);
		sb.append("/");
		sb.append(MKTREECSS);
		sb.append("\">");
		sb.append("<title>");
		sb.append(rootTitle);
		sb.append("</title>\n</head><body>\n");
		sb.append("<div id=\"branding\">");
		sb.append("<a target=\"_blank\" href=\"http://www.openolat.org\"><img id=\"logo\" src=\"");
		sb.append(CPOFFLINEMENUMAT);
		sb.append("/");
		sb.append(OLATICON);
		sb.append("\" alt=\"OLAT_logo\">");
		sb.append(BRANDING);
		sb.append("</div>");

		sb.append("\n</body></html>");

		File f = new File(unzippedDir, LOGO_FILE);
		if (f.exists()) {
			FileUtils.deleteDirsAndFiles(f, false, true);
		}
		ExportUtil.writeContentToFile(LOGO_FILE, sb.toString(), unzippedDir, "utf-8");
	}

	/**
	 * copy the whole CPOFFLINEMENUMAT-Folder (mktree.js, mktree.css and gifs) to
	 * the _unzipped_-Folder and zip everything that is in the _unzipped_-Folder
	 * 
	 * @param unzippedDir
	 * @param targetZip
	 * @param cpOfflineMat
	 */
	private void zipOfflineReadableCP(File unzippedDir, File targetZip, File cpOfflineMat) {
		FileUtils.copyDirToDir(cpOfflineMat, unzippedDir, "copy for offline readable cp");

		if (targetZip.exists()) {
			FileUtils.deleteDirsAndFiles(targetZip, false, true);
		}

		Set allFiles = new HashSet();
		String[] cpFiles = unzippedDir.list();
		for (int i = 0; i < cpFiles.length; i++) {
			allFiles.add(cpFiles[i]);
		}
		ZipUtil.zip(allFiles, unzippedDir, targetZip, true);

	}

}