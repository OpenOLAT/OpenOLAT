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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.render.velocity.VelocityModule;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
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

	private static final Logger log = Tracing.createLoggerFor(CPOfflineReadableManager.class);
	
	private static final String DIRNAME_CPOFFLINEMENUMAT = "cp_offline_menu_mat";
	private static final String FILENAME_START = "_START_.html";
	private static final String FILENAME_IMSMANIFEST = "imsmanifest.xml";
	private static final String FRAME_NAME_CONTENT = "content";

	private String rootTitle;

	private VelocityEngine velocityEngine;

	private CPOfflineReadableManager() {
		// private since singleton

		// init velocity engine
		Properties p = new Properties();
		try {
			velocityEngine = new VelocityEngine();
			p.setProperty(RuntimeConstants.RESOURCE_MANAGER_CACHE_CLASS, "org.olat.core.gui.render.velocity.InfinispanResourceCache");
			p.setProperty(RuntimeConstants.INPUT_ENCODING, VelocityModule.getInputEncoding());
			p.setProperty("classpath.resource.loader.cache", Settings.isDebuging() ? "false" : "true");
			velocityEngine.init(p);
		} catch (Exception e) {
			throw new RuntimeException("config error " + p);
		}
	}

	/**
	 * @return instance of CPOfflineReadableManager
	 */
	public static CPOfflineReadableManager getInstance() {
		return instance;
	}

	/**
	 * "exports" the the given CP (specified by its containing _unzipped_ directory) to a
	 * zipFile.<br />
	 * The resulting zip contains a "offline-readable" version of the CP.
	 * including style-sheets, menu-Tree and OpenOLAT branding
	 * 
	 * @param ores
	 *            the containing directory
	 * @param targetZip
	 *            the resulting zip-filename
	 */
	public void makeCPOfflineReadable(File unzippedDir, File targetZip) {
		try {
			writeOfflineCPStartHTMLFile(unzippedDir);
			File cpOfflineMat = new File(WebappHelper.getContextRealPath("/static/" + DIRNAME_CPOFFLINEMENUMAT));
			zipOfflineReadableCP(unzippedDir, targetZip, cpOfflineMat);
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public void makeCPOfflineReadable(String manifest, String indexSrc, ZipOutputStream exportStream) {
		try {
			//start page
			String startPage = getOfflineCPStartHTMLFile(manifest, indexSrc);
			exportStream.putNextEntry(new ZipEntry("_START_.html"));
			IOUtils.write(startPage, exportStream, "UTF-8");
			exportStream.closeEntry();
			
			File cpOfflineMat = new File(WebappHelper.getContextRealPath("/static/"), DIRNAME_CPOFFLINEMENUMAT);
			for(File content:cpOfflineMat.listFiles()) {
				exportStream.putNextEntry(new ZipEntry(DIRNAME_CPOFFLINEMENUMAT + "/" + content.getName()));
				InputStream in = new FileInputStream(content);
				FileUtils.cpio(in, exportStream, "");
				exportStream.closeEntry();
				in.close();
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}

	/**
	 * "exports" the the given CP (specified by its OLATResourceable) to a
	 * zipFile.<br />
	 * The resulting zip contains a "offline-readable" version of the CP.
	 * including style-sheets, menu-Tree and OpenOLAT branding
	 * 
	 * @param ores
	 *            the OLATResourceable (expected to be a CP)
	 * @param zipName
	 *            the resulting zip-filename
	 */
	public void makeCPOfflineReadable(OLATResourceable ores, String zipName) {
		try {
			String repositoryHome = FolderConfig.getCanonicalRepositoryHome();
			FileResourceManager fm = FileResourceManager.getInstance();
			String relPath = fm.getUnzippedDirRel(ores);
			String resId = ores.getResourceableId().toString();

			File unzippedDir = new File(repositoryHome + "/" + relPath);
			File targetZip = new File(repositoryHome + "/" + resId + "/" + zipName);
			File cpOfflineMat = new File(WebappHelper.getContextRealPath("/static/" + DIRNAME_CPOFFLINEMENUMAT));

			writeOfflineCPStartHTMLFile(unzippedDir);
			zipOfflineReadableCP(unzippedDir, targetZip, cpOfflineMat);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	/**
	 * generates a html-file (_START_.html) that presents the given cp-content
	 * (specified by its "_unzipped_"-dir). The resulting file is suitable for
	 * offline reading of the cp.
	 * 
	 * 
	 * @param unzippedDir
	 *            the directory that contains the unzipped CP
	 */
	private void writeOfflineCPStartHTMLFile(File unzippedDir) throws IOException {

		/* first, we do the menu-tree */
		File mani = new File(unzippedDir, FILENAME_IMSMANIFEST);
		LocalFileImpl vfsMani = new LocalFileImpl(mani);
		CPManifestTreeModel ctm = new CPManifestTreeModel(vfsMani, "");
		TreeNode root = ctm.getRootNode();
		// let's take the rootnode title as  page title
		this.rootTitle = root.getTitle(); 

		StringBuilder menuTreeSB = new StringBuilder();
		renderMenuTreeNodeRecursively(root, menuTreeSB, 0);
		
		// now put values to velocityContext
		VelocityContext ctx = new VelocityContext();
		ctx.put("menutree", menuTreeSB.toString());
		ctx.put("rootTitle", this.rootTitle);
		ctx.put("cpoff",DIRNAME_CPOFFLINEMENUMAT);
		
		StringWriter sw = new StringWriter();
		try {
			String template = FileUtils.load(CPOfflineReadableManager.class.getResourceAsStream("_content/cpofflinereadable.html"), "utf-8");
			boolean evalResult = velocityEngine.evaluate(ctx, sw, "cpexport", template);
			if (!evalResult)
				log.error("Could not evaluate velocity template for CP Export");
		} catch (Exception e) {
			log.error("Error while evaluating velovity template for CP Export",e);
		}
		
		File f = new File(unzippedDir, FILENAME_START);
		if (f.exists()) {
			FileUtils.deleteDirsAndFiles(f, false, true);
		}
		ExportUtil.writeContentToFile(FILENAME_START, sw.toString(), unzippedDir, "utf-8");
	}
	
	public String getOfflineCPStartHTMLFile(String manifest, String indexSrc)
	throws IOException {

		CPManifestTreeModel ctm = new CPManifestTreeModel(manifest, "");
		TreeNode root = ctm.getRootNode();
		// let's take the rootnode title as  page title

		StringBuilder menuTreeSB = new StringBuilder();
		renderMenuTreeNodeRecursively(root, menuTreeSB, 0);
		
		// now put values to velocityContext
		VelocityContext ctx = new VelocityContext();
		ctx.put("menutree", menuTreeSB.toString());
		ctx.put("rootTitle", root.getTitle());
		ctx.put("cpoff",DIRNAME_CPOFFLINEMENUMAT);
		ctx.put("index", indexSrc);
		
		StringWriter sw = new StringWriter();
		try {
			String template = FileUtils.load(CPOfflineReadableManager.class.getResourceAsStream("_content/cpofflinereadable.html"), "utf-8");
			boolean evalResult = velocityEngine.evaluate(ctx, sw, "cpexport", template);
			if (!evalResult)
				log.error("Could not evaluate velocity template for CP Export");
		} catch (Exception e) {
			log.error("Error while evaluating velovity template for CP Export",e);
		}
		return sw.toString();
	}


	/**
	 * @param node
	 * @param sb
	 * @param indent
	 */
	private void renderMenuTreeNodeRecursively(TreeNode node, StringBuilder sb, int level) {
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
			renderMenuTreeNodeRecursively(child, sb, level + 1);
			b = false;
		}
		if (!b) {
			sb.append("</ul>\n");
		}
		sb.append("</li>\n");
	}

	/**
	 * copy the whole CPOFFLINEMENUMAT-Folder (mktree.js, mktree.css and gifs)
	 * to the _unzipped_-Folder and zip everything that is in the
	 * _unzipped_-Folder
	 * 
	 * @param unzippedDir
	 * @param targetZip
	 * @param cpOfflineMat
	 */
	private void zipOfflineReadableCP(File unzippedDir, File targetZip, File cpOfflineMat) {
		// copy the offlineMat to unzippedDir
		FileUtils.copyDirToDir(cpOfflineMat, unzippedDir, "copy for offline readable cp");
		
		if (targetZip.exists()) {
			FileUtils.deleteDirsAndFiles(targetZip, false, true);
		} else {
			// ZipUtil.zip expects the target-file's parent directory to exist!
			targetZip.getParentFile().mkdirs();
		}

		Set<String> allFilesInUnzippedDir = new HashSet<String>();
		String[] cpFiles = unzippedDir.list();
		for (int i = 0; i < cpFiles.length; i++) {
			allFilesInUnzippedDir.add(cpFiles[i]);
		}
		boolean zipResult = ZipUtil.zip(allFilesInUnzippedDir, unzippedDir, targetZip, true);

		if(!targetZip.exists()){
			log.warn("targetZip does not exists after zipping. zip-result is: "+ zipResult);
		}
	}

}