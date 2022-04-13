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
package org.olat.modules.wiki;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSAllItemsFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;

/**
 * Description:<br>
 * pack whole wiki with unparsed syntax files into zip for export
 * 
 * <P>
 * Initial Date:  Dec 11, 2006 <br>
 * @author guido
 */
public class WikiToZipUtils {
	
	private static final Logger log = Tracing.createLoggerFor(WikiToZipUtils.class);
	
	/**
	 * creates an html page with the mappings between the pagename and the Base64
	 * encoded filename.
	 * 
	 * @param vfsLeaves
	 * @return
	 */
	private static String createIndexPageForExport(List<VFSItem> vfsLeaves) {
		boolean hasProperties = false;
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		sb.append("</head><body><ul>");
		for (Iterator<VFSItem> iter = vfsLeaves.iterator(); iter.hasNext();) {
			VFSLeaf element = (VFSLeaf) iter.next();
			if (element.getName().endsWith(WikiManager.WIKI_PROPERTIES_SUFFIX)) {
				hasProperties = true;
				Properties p = new Properties();
				try {
					p.load(element.getInputStream());
				} catch (IOException e) {
					throw new AssertException("Wiki propterties couldn't be read! ", e);
				}
				sb.append("<li>");
				sb.append(p.getProperty(WikiManager.PAGENAME));
				sb.append(" ----> ");
				sb.append(element.getName().substring(0, element.getName().indexOf('.')));
				sb.append("</li>");
			}
		}
		sb.append("</ul></body></html>");
		if(!hasProperties) return null;
		return sb.toString();
	}
	
	/**
	 * get the whole wiki as a zip file for export, content is unparsed!
	 * @param rootContainer
	 * @return
	 */
	public static VFSLeaf getWikiAsZip(VFSContainer rootContainer){
		List<VFSItem> folders = rootContainer.getItems();
		VFSLeaf indexLeaf =(VFSLeaf)rootContainer.resolve("index.html");
		if(indexLeaf != null) indexLeaf.delete();
		List<VFSItem> filesTozip = new ArrayList<>();
		for (Iterator <VFSItem>iter = folders.iterator(); iter.hasNext();) {
			VFSItem item = iter.next();
			if (item instanceof VFSContainer) {
				VFSContainer folder = (VFSContainer) item;
				List <VFSItem>items = folder.getItems();
				String overviewPage = WikiToZipUtils.createIndexPageForExport(items);
				if(overviewPage != null){
					VFSLeaf overview = rootContainer.createChildLeaf("index.html");
					//items.add(overview); take care not to have duplicate entries in the list
					FileUtils.save(overview.getOutputStream(false), overviewPage, "utf-8");
				}
				items = folder.getItems(); //reload list, maybe there is a new index.html file
				filesTozip.addAll(items);
			}
		}
		VFSLeaf zipFile = (VFSLeaf)rootContainer.resolve("wiki.zip");
		if(rootContainer.resolve("wiki.zip") != null) zipFile.delete();
		ZipUtil.zip(filesTozip, rootContainer.createChildLeaf("wiki.zip"), VFSAllItemsFilter.ACCEPT_ALL, false);
		return (VFSLeaf)rootContainer.resolve("wiki.zip");
	}
	
	public static void wikiToZip(VFSContainer rootContainer, String currentPath, ZipOutputStream exportStream)
	throws IOException {		
		Set<String> path = new HashSet<>();
		for (VFSItem item:rootContainer.getItems(new VFSSystemItemFilter())) {
			if (item instanceof VFSContainer) {
				VFSContainer folder = (VFSContainer) item;
				List<VFSItem> items = folder.getItems();
				String overviewPage = WikiToZipUtils.createIndexPageForExport(items);
				if(overviewPage != null && !path.contains(overviewPage) && !path.contains("index.html")) {
					path.add(overviewPage);
					try {
						exportStream.putNextEntry(new ZipEntry(currentPath + "/index.html"));
						IOUtils.write(overviewPage, exportStream, StandardCharsets.UTF_8);
						exportStream.closeEntry();
					} catch (Exception e) {
						log.error("", e);
					}
				}
				for(VFSItem wikiItem:items) {
					try {
						ZipUtil.addToZip(wikiItem, currentPath, exportStream, VFSAllItemsFilter.ACCEPT_ALL, false);
					} catch (Exception e) {
						log.error("", e);
					}
				}
			}
		}
	}
}
