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
package org.olat.modules.wiki;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSAllItemsFilter;
import org.olat.modules.cp.CPOfflineReadableManager;

/**
 * 
 * Initial date: 08.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WikiToCPResource implements MediaResource {
	
	private static final Logger log = Tracing.createLoggerFor(WikiToCPResource.class);
	
	private static final String encoding = "UTF-8";
	
	private final Identity identity;
	private final Translator translator;
	private final OLATResourceable ores;
	
	public WikiToCPResource(OLATResourceable ores, Identity identity, Translator translator) {
		this.identity = identity;
		this.translator = translator;
		this.ores = ores;
	}
	
	@Override
	public long getCacheControlDuration() {
		return ServletUtil.CACHE_NO_CACHE;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return "application/zip";
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			hres.setCharacterEncoding(encoding);
		} catch (Exception e) {
			log.error("", e);
		}
		
		Wiki wiki = WikiManager.getInstance().getOrLoadWiki(ores);
		String label = "Wiki.zip";
		String urlEncodedLabel = StringHelper.urlEncodeUTF8(label);
		hres.setHeader("Content-Disposition","attachment; filename*=UTF-8''" + urlEncodedLabel);			
		hres.setHeader("Content-Description", urlEncodedLabel);
		
		try(ZipOutputStream zout = new ZipOutputStream(hres.getOutputStream())) {
			zout.setLevel(9);
			wikiToCP(wiki, zout);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public void release() {
		//
	}
	
	private void wikiToCP(Wiki wiki, ZipOutputStream zout)
	throws IOException {
		WikiToCPExport export = new WikiToCPExport(ores, translator);
		
		// create the ims manifest
		String manifest = export.createIMSManifest(wiki, identity);
		zout.putNextEntry(new ZipEntry("imsmanifest.xml"));
		IOUtils.write(manifest, zout, StandardCharsets.UTF_8);
		zout.closeEntry();

		VFSContainer mediaContainer = WikiManager.getInstance().getMediaFolder(ores);
		List<VFSItem> images = mediaContainer.getItems();
		for (VFSItem image:images) {
			ZipUtil.addToZip(image, "", zout, VFSAllItemsFilter.ACCEPT_ALL, false);
		}

		// create the javascript mapping file
		String jsContent = export.createJsMappingContent(wiki);
		zout.putNextEntry(new ZipEntry("mapping.js"));
		IOUtils.write(jsContent, zout, StandardCharsets.UTF_8);
		zout.closeEntry();
		
		
		List<WikiPage> pages = wiki.getAllPagesWithContent(true);
		for (WikiPage page: pages) {
			String htmlPage = export.wikiPageToHtml(page);
			zout.putNextEntry(new ZipEntry(page.getPageId() + ".html"));
			IOUtils.write(htmlPage, zout, StandardCharsets.UTF_8);
			zout.closeEntry();
		}
		
		WikiPage index = wiki.getPage(WikiPage.WIKI_INDEX_PAGE, true);
		String indexSrc = index.getPageId() + ".html";
		CPOfflineReadableManager.getInstance().makeCPOfflineReadable(manifest, indexSrc, zout);
	}
}
