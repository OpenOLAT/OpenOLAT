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
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.jamwiki.utils.Utilities;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSLeafFilter;
import org.olat.modules.wiki.gui.components.wikiToHtml.FilterUtil;
import org.olat.modules.wiki.versioning.ChangeInfo;
import org.olat.modules.wiki.versioning.DifferenceService;
import org.olat.user.UserManager;

/**
 * Description:<br>
 * Abstract model of a single Wiki where a wikiWord is unique and used as a key to get it's content.
 * The pages are hold in an map where the key is an base64 (excluding the "/" of the base64 transformation) id of the wiki name. The base64
 * key is in the wikiManager used for persisting an single page to an fileSystem. Normal viewing operations
 * are performed by the map where all pages are accessible, writing actions are handled by the wikiManager.
 * <P>
 * Initial Date: May 7, 2006 <br>
 * 
 * @author guido schnider
 */
public class Wiki implements WikiContainer, Serializable {
	private static final long serialVersionUID = -244524942476374366L;
	public static final String CSS_CLASS_WIKI_ICON = "o_wiki_icon";

	// synchronized map of all pages of a wiki as the whole model (wiki object) gets cached this object itself is save for cluster mode
	private Map<String, WikiPage> wikiPages;//o_clusterOK by gs 
	private DifferenceService diffService;
	private VFSContainer versionsContainer, pageContainer, mediaContainer;
	protected static final String NEW_PAGE = "O_new_page";
	private String IMAGE_NAMESPACE = "Image:";
	private String MEDIA_NAMESPACE = "Media:";
	private static final Logger log = Tracing.createLoggerFor(Wiki.class);
	
	protected Wiki(VFSContainer wikiRootContainer) {
		if(wikiRootContainer == null) throw new AssertException("null values are not allowed for the wiki constructor!");
		wikiPages = Collections.synchronizedMap(new HashMap<String, WikiPage>());
		this.diffService = WikiManager.getInstance().getDiffService();
		versionsContainer = VFSManager.getOrCreateContainer(wikiRootContainer, WikiManager.VERSION_FOLDER_NAME);
		pageContainer = VFSManager.getOrCreateContainer(wikiRootContainer, WikiManager.WIKI_RESOURCE_FOLDER_NAME);
		mediaContainer = VFSManager.getOrCreateContainer(wikiRootContainer, WikiContainer.MEDIA_FOLDER_NAME);
	}

	/**
	 * Return a wiki page but normally without content yet (performance issues)
	 * but with all other attributes. To get the page content call getPage(pageId,
	 * true)
	 * 
	 * @param pageId use either the page name or the pageID
	 * @see org.olat.modules.wiki.WikiPage.getPageId() as key;
	 * @return the wikiPage or null if not found
	 */
	public WikiPage getPage(String pageId) {
		WikiPage page = getPageById(pageId);
		// try also the pageName, may be someone tried to access with the name
		// instead of the page id
		if (page == null) {
			page = getPageById(generatePageId(pageId));
		}
		if (page == null) {
			page = getPageById(generatePageId(FilterUtil.normalizeWikiLink(pageId)));
		}
		if (page == null) {
			page = new WikiPage(WikiPage.WIKI_ERROR);
			page.setContent("wiki.error.page.not.found");
		}
		return page;
	}
	
	/**
	 * The method check the specified pageId with the
	 * id in the map and if it doesn't found a page, check
	 * all pages for an alternative ids.
	 * 
	 * @param pageId
	 * @return
	 */
	private final WikiPage getPageById(String pageId) {
		WikiPage page = wikiPages.get(pageId);
		if(page == null) {
			for(WikiPage wikiPage:wikiPages.values()) {
				if(wikiPage.matchIds(pageId)) {
					page = wikiPage;
					break;
				}
			}
		}
		return page;
	}

	/**
	 * @param pageId
	 * @param loadContent loads the page content (article) lazy (performance
	 *          issues) when set true
	 * @return if loadContent == true the WikiPage with the article otherwise the
	 *         WikiPage with all other attributes
	 */
	public WikiPage getPage(String pageId, boolean loadContent) {
		WikiPage page = getPage(pageId);
		// if not empty content is already loaded
		if (!page.getContent().equals("")) {
			return page;
		}
		if (loadContent) {
			VFSItem item = pageContainer.resolve(page.getPageId() + "." + WikiManager.WIKI_FILE_SUFFIX);
			if(item instanceof VFSLeaf) {
				try(InputStream in = ((VFSLeaf)item).getInputStream()) {
					page.setContent(FileUtils.load(in, "utf-8"));
				} catch(Exception e) {
					log.error("Cannot load wiki page: " + item, e);
				}
			}
		}
		return page;
	}

	public void addPage(WikiPage page) {
		String pageId = page.getPageId();
		if (!wikiPages.containsKey(pageId)) {
			wikiPages.put(pageId, page);
		}
	}

	/**
	 * @param pageId, or filename for media and image files
	 * @see WikiPage.generateId(name) as pages are stored by pageId
	 * @return
	 */
	@Override
	public boolean pageExists(String pageId) {
		if( log.isDebugEnabled() ) {
			boolean exists = wikiPages.containsKey(pageId);
			log.debug("\n\nChecking for existence of page with id in this wiki: "+ pageId +" located in: "+pageContainer);
			log.debug("Does page exists?: "+ exists);
			if(exists) log.debug("Page has spoken name: "+ getPage(pageId).getPageName());
		}
		boolean isImage = pageId.startsWith(IMAGE_NAMESPACE);
		boolean isMedia = pageId.startsWith(MEDIA_NAMESPACE);
		if ( isImage || isMedia ) {
			if (isImage) {
				return mediaFileExists(pageId.substring(IMAGE_NAMESPACE.length(), pageId.length()));
			}
			if (isMedia) {
				return mediaFileExists(pageId.substring(MEDIA_NAMESPACE.length(), pageId.length()));
			}
		}

		boolean exists = wikiPages.containsKey(pageId);
		if(!exists) {
			for(WikiPage wikiPage:wikiPages.values()) {
				if(wikiPage.matchIds(pageId)) {
					exists = true;
					break;
				}
			}	
		}
		return exists;
	}

	protected void removePage(WikiPage page) {
		String name = page.getPageName();
		if (name.equals(WikiPage.WIKI_INDEX_PAGE) || name.equals(WikiPage.WIKI_MENU_PAGE)) return;
		wikiPages.remove(page.getPageId());
	}

	protected int getNumberOfPages() {
		return wikiPages.size();
	}

	protected List<ChangeInfo> getDiff(WikiPage page, int version1, int version2) {
		WikiPage v1 = loadVersion(page, version1);
		WikiPage v2 = loadVersion(page, version2);
		
		if (log.isDebugEnabled()) {
			log.debug("comparing wiki page versions: " + version1 + " <--> " + version2);
			log.debug("version 1:\n" + v1.toString());
			log.debug("version 2:\n" + v2.toString());
		}
		
		return diffService.diff(v1.getContent(), v2.getContent());
	}

	protected List<WikiPage> getHistory(WikiPage page) {
		List<WikiPage> versions = new ArrayList<>();
		List<VFSItem> leafs = versionsContainer.getItems(new VFSLeafFilter());
		if (leafs.size() > 0) {
			for (Iterator<VFSItem> iter = leafs.iterator(); iter.hasNext();) {
				VFSLeaf leaf = (VFSLeaf)iter.next();
				String filename = leaf.getName();
				if (filename.indexOf(WikiManager.WIKI_PROPERTIES_SUFFIX) != -1 && filename.startsWith(page.getPageId())) {
					versions.add(assignPropertiesToPage(leaf));
				}
			}
			// add also the current version but only if saved once 
		}
		if (page.getModificationTime() > 0) versions.add(page);
		return versions;
	}

	protected WikiPage loadVersion(WikiPage page, int version) {
		// if version matches recent version the current pags is requested
		if (version == page.getVersion() && !page.getContent().equals("")) return page;
		if (version == 0) return new WikiPage("dummy");
		if (page.getPageName().equals("dummy")) return page;
		WikiPage pageVersion = null;
		VFSLeaf versionLeaf = (VFSLeaf) versionsContainer.resolve(page.getPageId() + "." + WikiManager.WIKI_PROPERTIES_SUFFIX + "-" + version);
		pageVersion = assignPropertiesToPage(versionLeaf);
		VFSLeaf contentLeaf = (VFSLeaf) versionsContainer.resolve(page.getPageId() + "." + WikiManager.WIKI_FILE_SUFFIX + "-" + version);
		if (contentLeaf != null) {
			pageVersion.setContent(FileUtils.load(contentLeaf.getInputStream(), "utf-8"));
		} else {
			pageVersion.setContent("");
		}
		return pageVersion;

	}

	public static WikiPage assignPropertiesToPage(VFSLeaf leaf) {
		Properties p = new Properties();
		if (leaf != null) {
		try(InputStream is =leaf.getInputStream()) {
			p.load(is);
		} catch (IOException e) {
			throw new OLATRuntimeException("Wiki page couldn't be read! Pagename:"+leaf.getName(), e);
		}
		String pageName = p.getProperty(WikiManager.PAGENAME);
		if(pageName == null){
			log.warn("wiki properties page is persent but without content. Name:"+leaf.getName());
			return null;
		}
		
		String initialPageName = p.getProperty(WikiManager.INITIAL_PAGENAME);
		WikiPage page = new WikiPage(pageName, initialPageName);
		String oldPageNames = p.getProperty(WikiManager.OLD_PAGENAME);
		if(StringHelper.containsNonWhitespace(oldPageNames)) {
			String[] names =  oldPageNames.split("[,]");
			if(names.length > 0) {
				page.setOldPageNames(Arrays.asList(names));
			}
		}
		page.setCreationTime(p.getProperty(WikiManager.C_TIME));
		page.setVersion(p.getProperty(WikiManager.VERSION));
		page.setForumKey(p.getProperty(WikiManager.FORUM_KEY));
		page.setInitalAuthor(p.getProperty(WikiManager.INITIAL_AUTHOR));
		page.setModificationTime(p.getProperty(WikiManager.M_TIME));
		page.setModifyAuthor(p.getProperty(WikiManager.MODIFY_AUTHOR));
		page.setViewCount(p.getProperty(WikiManager.VIEW_COUNT));
		page.setUpdateComment(p.getProperty(WikiManager.UPDATE_COMMENT));
		return page;
		} else {
			return new WikiPage("dummy");
		}
	}

	protected List<VFSItem> getMediaFileListWithMetadata() {
		return mediaContainer.getItems();
	}
	
	protected List<VFSItem> getMediaFileList() {
		List<VFSItem> allFiles = mediaContainer.getItems();
		List<VFSItem> mediaFilesOnly = new ArrayList<>();
		for (Iterator<VFSItem> iter = allFiles.iterator(); iter.hasNext();) {
			VFSItem element = iter.next();
			if(!element.getName().endsWith(WikiMainController.METADATA_SUFFIX)) { 
				mediaFilesOnly.add(element);
			}
		}
		return mediaFilesOnly;
	}

	public String getAllPageNamesSorted() {
		List<WikiPage> pages = new ArrayList<>(wikiPages.values());
		Collections.sort(pages, WikiPageSort.PAGENAME_ORDER);
		StringBuilder sb = new StringBuilder();
		for (WikiPage page:pages) {
			if (!page.getPageName().startsWith("O_")) {
				sb.append("* ");
				if(log.isDebugEnabled()) sb.append(page.getPageId()).append("  -->  ");
				sb.append("[[")
				  .append(page.getPageName())
				  .append("]]\n");
			}
		}
		return sb.toString();
	}

	protected String getRecentChanges(Locale locale) {
		if(locale == null) throw new AssertException("param was null which is not allowed");
		final int MAX_RESULTS = 5;
		List<WikiPage> pages = new ArrayList<>(wikiPages.values());
		Collections.sort(pages, WikiPageSort.MODTIME_ORDER);
		StringBuilder sb = new StringBuilder(512);
		int counter = 0;
		
		Formatter f = Formatter.getInstance(locale);
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		
		for (Iterator<WikiPage> iter = pages.iterator(); iter.hasNext();) {
			if (counter > MAX_RESULTS) break;
			WikiPage page = iter.next();
			if (!page.getPageName().startsWith("O_") && !page.getPageName().startsWith(WikiPage.WIKI_MENU_PAGE)) {
				sb.append("* [[");
				sb.append(page.getPageName());
				sb.append("]] ");
				sb.append(f.formatDateAndTime(new Date(page.getModificationTime())));
				sb.append(" Author: ");
				long author = page.getModifyAuthor();
				if (author != 0) {
					String authorFullname = userManager.getUserDisplayName(author);
					if(StringHelper.containsNonWhitespace(authorFullname)) {
						sb.append(" Author: ").append(authorFullname);
					} else {
						sb.append("???");
					}
				}
				sb.append("\n");
				counter++;
			}
		}
		return sb.toString();
	}

	protected WikiPage findPage(String name) {
		WikiPage page = null;
		if (pageExists(WikiManager.generatePageId(FilterUtil.normalizeWikiLink(name)))) return getPage(name, true);
		page = new WikiPage(NEW_PAGE);
		page.setContent("[[" + name + "]]");
		return page;
	}

	/**
	 * @see org.olat.core.commons.modules.wiki.WikiContainer#generatePageId(java.lang.String)
	 */
	@Override
	public String generatePageId(String pageName) {
		if(log.isDebugEnabled()) log.debug("Generating page id from page name: "+pageName +" to id: "+WikiManager.generatePageId(pageName));
		return WikiManager.generatePageId(pageName);
	}

	/**
	 * @return a List of all pages in a wiki ordered by date
	 */
	protected List<WikiPage> getPagesByDate() {
		ArrayList<WikiPage> pages = new ArrayList<>(wikiPages.values());
		Collections.sort(pages, WikiPageSort.MODTIME_ORDER);
		return pages;
	}

	/**
	 * @return a List containing all pages names of the wiki sorted alphabetically
	 */
	protected List<String> getListOfAllPageNames() {
		ArrayList<WikiPage> pages = new ArrayList<>(wikiPages.values());
		ArrayList<String> pageNames = new ArrayList<>(pages.size());
		Collections.sort(pages, WikiPageSort.PAGENAME_ORDER);
		for (Iterator<WikiPage> iter = pages.iterator(); iter.hasNext();) {
			WikiPage page = iter.next();
			if (!page.getPageName().startsWith("O_")) {
				pageNames.add(page.getPageName());
			}
		}
		return pageNames;
	}

	/**
	 * 
	 * @return a List of all pages in a wiki
	 */
		public List<WikiPage> getAllPagesWithContent() {
			return getAllPagesWithContent(false);
		}
		
		public List<WikiPage> getAllPagesWithContent(boolean includeSpecialPages) {
			ArrayList<WikiPage> pages = new ArrayList<>();
			for (Iterator<String> keyes = wikiPages.keySet().iterator(); keyes.hasNext();) {
				String pageId = keyes.next();
				WikiPage wikiPage = getPage(pageId);
				// check if the page is a content page
				if (includeSpecialPages) {
					if (wikiPage.getContent().equals("") ) {
						// wikiPage has empty content => try to load content
						if (!wikiPage.getPageName().startsWith("O_")) {
							wikiPage = getPage(pageId, true);
						}
					}
					pages.add(wikiPage);
				} else {
					if (!wikiPage.getPageName().startsWith("O_")) {
						if (wikiPage.getContent().equals("") ) {
							// wikiPage has empty content => try to load content
							wikiPage = getPage(pageId, true);
						}
					pages.add(wikiPage);
					}
				}
			}
			return pages;
		}

	/**
	 * FIXME:gs increase performance
	 * @param imageName
	 * @return
	 */
	public boolean mediaFileExists(String imageName) {
		List<VFSItem> mediaFiles = getMediaFileList();
		if (mediaFiles.size() == 0) return false;
		for (Iterator<VFSItem> iter = mediaFiles.iterator(); iter.hasNext();) {
			VFSLeaf leaf = (VFSLeaf) iter.next();
			if (leaf.getName().equals(Utilities.encodeForURL(imageName))) return true;
		}
		return false;
	}

}
