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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.FileUtils;
import org.olat.core.util.cache.n.CacheWrapper;
import org.olat.core.util.controller.OLATResourceableListeningWrapperController;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSItemSuffixFilter;
import org.olat.core.util.vfs.filters.VFSLeafFilter;
import org.olat.course.CourseModule;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.WikiResource;
import org.olat.group.BusinessGroup;
import org.olat.modules.wiki.versioning.DifferenceService;
import org.olat.modules.wiki.versioning.diff.CookbookDifferenceService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * Description:<br>
 * This class handles several wiki's by storing them in a cache and also creates a new wikis.
 * It handles also the file operation to persist the data in the wiki pages which are stored on the file
 * system.
 * <P>
 * Initial Date: May 5, 2006 <br>
 * 
 * @author guido
 */
public class WikiManager extends BasicManager {

	public static final String VIEW_COUNT = "view.count";
	public static final String MODIFY_AUTHOR = "modify.author";
	public static final String M_TIME = "mTime";
	public static final String INITIAL_AUTHOR = "initial.author";
	public static final String FORUM_KEY = "forum.key";
	public static final String VERSION = "version";
	public static final String C_TIME = "cTime";
	public static final String PAGENAME = "pagename";
	private static WikiManager instance;
	public static final String WIKI_RESOURCE_FOLDER_NAME = "wiki";
	public static final String VERSION_FOLDER_NAME = "versions";
	public static final String WIKI_FILE_SUFFIX = "wp";
	public static final String WIKI_PROPERTIES_SUFFIX = "properties";
	public static final String UPDATE_COMMENT = "update.comment";
	
  //o_clusterNOK cache : 08.04.08/cg Not tested in cluster-mode 
	CacheWrapper wikiCache;
	
	OLATResourceManager resourceManager;
	FileResourceManager fileResourceManager;
	CoordinatorManager coordinator;
	
	/**
	 * spring only
	 */
	private WikiManager() {
		instance = this;
	}
	
	/**
	 * return singleton
	 */
	public static WikiManager getInstance() {
		return instance;
	}
	
	
	// ---- begin controller factory -----
	/** @param ureq
	 * @param wControl
	 * @param ores either an OlatResourcable of an repository entry or of an BusinessGroup
	 * @param securityCallback a callback to evaluate the permissions
	 * @param initialPageName opens the wiki with an certain page, default is the index page if null is passed
	 * @param courseContext - a course context or null if used outside a course
	 * @param courseNodeContext - a courseNode context or null if used outside a course
	 */
	public WikiMainController createWikiMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, WikiSecurityCallback securityCallback, String initialPageName) {
		return new WikiMainController(ureq, wControl, ores, securityCallback, initialPageName);
	}
	
	/**
	 * brasato:::: to discuss, are two methods needed at all? probably not, unless there are cases to launch this controller without an ores known.
	 * such as contentPackacking which requires only a fileroot (but this file directory depends on a ores in the end)
	 * 
	 * create a wikiMaincontroller which disposes itself when the associated olatresourceable is disposed.
	 */
	public Controller createWikiMainControllerDisposeOnOres(UserRequest ureq, WindowControl wControl, OLATResourceable ores, WikiSecurityCallback securityCallback, String initialPageName) {
		Controller controller = new WikiMainController(ureq, wControl, ores, securityCallback, initialPageName);
		//fxdiff BAKS-7 Resume function
		OLATResourceableListeningWrapperController dwc = new OLATResourceableListeningWrapperController(ureq, wControl, ores, controller, null, ureq.getIdentity());
		return dwc;
	}

	// ---- end controller factory -----

	/**
	 * @return the new created resource
	 */
	
	
	public FileResource createWiki() {
		FileResource resource = new WikiResource();
		createFolders(resource);
		OLATResourceManager rm = getResourceManager();
		OLATResource ores = rm.createOLATResourceInstance(resource);
		rm.saveOLATResource(ores);
		return resource;
	}

	public void setCoordinator(CoordinatorManager coord){
			coordinator = coord;
	}
	
	
/**
 * API change stop
 */
	
	void createFolders(OLATResourceable ores) {
		long start = 0;
		if (isLogDebugEnabled()) {
			start = System.currentTimeMillis();
		}
		VFSContainer rootContainer = getWikiRootContainer(ores);
		VFSContainer unzippedDir = (VFSContainer) rootContainer.resolve(getFileResourceManager().ZIPDIR);
		if (unzippedDir == null) { // check for _unzipped_ dir from imported wiki's
			if (rootContainer.createChildContainer(WIKI_RESOURCE_FOLDER_NAME) == null) throwError(ores);
			if (rootContainer.createChildContainer(WikiContainer.MEDIA_FOLDER_NAME) == null) throwError(ores);
			if (rootContainer.createChildContainer(VERSION_FOLDER_NAME) == null) throwError(ores);
		} else { // _unzipped_ dir found: move elements to wiki folder and delete
			// unzipped dir and zip files
			List files = unzippedDir.getItems();
			VFSContainer wikiCtn = rootContainer.createChildContainer(WIKI_RESOURCE_FOLDER_NAME);
			VFSContainer mediaCtn = rootContainer.createChildContainer(WikiContainer.MEDIA_FOLDER_NAME);
			if (rootContainer.createChildContainer(VERSION_FOLDER_NAME) == null) throwError(ores);
			if (wikiCtn == null) throwError(ores);
			// copy files to wiki and media folder
			for (Iterator iter = files.iterator(); iter.hasNext();) {
				VFSLeaf leaf = ((VFSLeaf) iter.next());
				if (leaf.getName().endsWith(WikiManager.WIKI_FILE_SUFFIX) || leaf.getName().endsWith(WikiManager.WIKI_PROPERTIES_SUFFIX)) {
					wikiCtn.copyFrom(leaf);
				} else {
					if (leaf.getName().contains(WikiManager.WIKI_FILE_SUFFIX+"-") || leaf.getName().contains(WikiManager.WIKI_PROPERTIES_SUFFIX+"-")) {
						leaf.delete(); // delete version history
					} else
					mediaCtn.copyFrom(leaf);
				}
			}
			unzippedDir.delete();
			List zipFiles = rootContainer.getItems(new VFSItemSuffixFilter(new String[] { "zip" }));
			// delete all zips
			for (Iterator iter = zipFiles.iterator(); iter.hasNext();) {
				VFSLeaf element = (VFSLeaf) iter.next();
				element.delete();
			}
			//reset forum key and author references keys back to default as users and forums may not exist
			List propertyLeafs = wikiCtn.getItems(new VFSItemSuffixFilter(new String[] { WikiManager.WIKI_PROPERTIES_SUFFIX }));
			for (Iterator iter = propertyLeafs.iterator(); iter.hasNext();) {
				VFSLeaf element = (VFSLeaf) iter.next();
				WikiPage page = Wiki.assignPropertiesToPage(element);
				page.setForumKey(0);
				page.setInitalAuthor(0);
				page.setModifyAuthor(0);
				page.setModificationTime(0);
				page.setViewCount(0);
				page.setVersion("0");
				page.setCreationTime(System.currentTimeMillis());
				saveWikiPageProperties(ores, page);
			}
		}
		if (isLogDebugEnabled()) {
			long end = System.currentTimeMillis();
			logDebug("creating folders and move files and updating properties to default values took: (milliseconds)"+(end-start), null);
		}
	}

	private void throwError(OLATResourceable ores) {
		throw new OLATRuntimeException(this.getClass(), "Unable to create wiki folder structure for resource: " + ores.getResourceableId(),
				null);
	}


		/**
		 * @param ores
		 * @return a wiki loaded from cache or the fileSystem
		 */
	public Wiki getOrLoadWiki(final OLATResourceable ores) {
		final String wikiKey = OresHelper.createStringRepresenting(ores);
		//cluster_OK by guido
		if (wikiCache == null) {
				
			  coordinator.getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
				public void  execute() {
					if (wikiCache == null) {
						wikiCache =  coordinator.getCoordinator().getCacher().getOrCreateCache(this.getClass(), "wiki");
					}
				}
			});
		}
		Wiki wiki = (Wiki) wikiCache.get(wikiKey);
		if (wiki != null) {
			logDebug("loading wiki from cache. Ores: " + ores.getResourceableId());
			return wiki;
		}
		// No wiki in cache => load it form file-system
			coordinator.getCoordinator().getSyncer().doInSync(ores, new SyncerExecutor() {
			public void execute() {

				long start = 0;
				// wiki not in cache load form filesystem
				if (isLogDebugEnabled()) {
					logDebug("wiki not in cache. Loading wiki from filesystem. Ores: " + ores.getResourceableId());
					start = System.currentTimeMillis();
				}

				Wiki wiki = null;
				VFSContainer folder = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
				// wiki folder structure does not yet exists, but resource does. Create
				// wiki in group context
				if (folder == null) {
					// createWikiforExistingResource(ores);
					createFolders(ores);
					folder = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
				}
				// folders should be present, create the wiki
				wiki = new Wiki(getWikiRootContainer(ores));
				// filter for xyz.properties files
				List wikiLeaves = folder.getItems(new VFSItemSuffixFilter(new String[] { WikiManager.WIKI_PROPERTIES_SUFFIX }));
				for (Iterator iter = wikiLeaves.iterator(); iter.hasNext();) {
					VFSLeaf propertiesFile = (VFSLeaf) iter.next();
					WikiPage page = Wiki.assignPropertiesToPage(propertiesFile);
					if (page == null) {
						// broken pages get automatically cleaned from filesystem
						String contentFileToBeDeleted = (propertiesFile.getName().substring(0,
								propertiesFile.getName().length() - WikiManager.WIKI_PROPERTIES_SUFFIX.length()) + WikiManager.WIKI_FILE_SUFFIX);
						folder.resolve(contentFileToBeDeleted).delete();
						propertiesFile.delete();
						continue;
					}
					// index and menu page are loaded by default
					if (page.getPageName().equals(WikiPage.WIKI_INDEX_PAGE) || page.getPageName().equals(WikiPage.WIKI_MENU_PAGE)) {
						VFSLeaf leaf = (VFSLeaf) folder.resolve(page.getPageId() + "." + WikiManager.WIKI_FILE_SUFFIX);
						page.setContent(FileUtils.load(leaf.getInputStream(), "utf-8"));
					}

					// due to a bug we have to rename some pages that start with an non
					// ASCII lowercase letter
					String idOutOfFileName = propertiesFile.getName().substring(0, propertiesFile.getName().indexOf("."));
					if (!page.getPageId().equals(idOutOfFileName)) {
						// rename corrupt prop file
						propertiesFile.rename(page.getPageId() + "." + WikiManager.WIKI_PROPERTIES_SUFFIX);
						// load content and delete corrupt content file
						VFSLeaf contentFile = (VFSLeaf) folder.resolve(idOutOfFileName + "." + WikiManager.WIKI_FILE_SUFFIX);
						contentFile.rename(page.getPageId() + "." + WikiManager.WIKI_FILE_SUFFIX);
					}

					wiki.addPage(page);
				}
				// if index and menu page not present create the first page and save it
				if (wiki.getNumberOfPages() == 0) {
					WikiPage indexPage = new WikiPage(WikiPage.WIKI_INDEX_PAGE);
					WikiPage menuPage = new WikiPage(WikiPage.WIKI_MENU_PAGE);
					indexPage.setCreationTime(System.currentTimeMillis());
					wiki.addPage(indexPage);
					menuPage.setCreationTime(System.currentTimeMillis());
					menuPage.setContent("* [[Index]]\n* [[Index|Your link]]");
					wiki.addPage(menuPage);
					saveWikiPage(ores, indexPage, false, wiki);
					saveWikiPage(ores, menuPage, false, wiki);
				}
				// add pages internally used for displaying dynamic data, they are not
				// persisted
				WikiPage recentChangesPage = new WikiPage(WikiPage.WIKI_RECENT_CHANGES_PAGE);
				WikiPage a2zPage = new WikiPage(WikiPage.WIKI_A2Z_PAGE);
				wiki.addPage(recentChangesPage);
				wiki.addPage(a2zPage);

				// wikiCache.put(OresHelper.createStringRepresenting(ores), wiki);
				if (isLogDebugEnabled()) {
					long stop = System.currentTimeMillis();
					logDebug("loading of wiki from filessystem took (ms) " + (stop - start));
				}
				wikiCache.put(wikiKey, wiki);
			}
		});
		//at this point there will be something in the cache
		return (Wiki) wikiCache.get(wikiKey);

	}
		

	public DifferenceService getDiffService() {
		return new CookbookDifferenceService();
	}


	/**
	 * persists a wiki page on the filesystem. It moves the recent page and the
	 * metadata to the versions folder with the version on the tail and saves new
	 * page with metadata to the wiki folder. Does not need to be synchronized as
	 * editing is locked on page level by the
	 * 
	 * @see WikiMainController
	 * @param ores
	 * @param page
	 */
	public void saveWikiPage(OLATResourceable ores, WikiPage page, boolean incrementVersion, Wiki wiki) {
		//cluster_OK by guido
		VFSContainer versionsContainer = getWikiContainer(ores, VERSION_FOLDER_NAME);
		VFSContainer wikiContentContainer = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
		// rename existing content file to version x and copy it to the version
		// container
		VFSItem item = wikiContentContainer.resolve(page.getPageId() + "." + WIKI_FILE_SUFFIX);
		if (item != null && incrementVersion) {
			if (page.getVersion() > 0) {
				versionsContainer.copyFrom(item);
				VFSItem copiedItem = versionsContainer.resolve(page.getPageId() + "." + WIKI_FILE_SUFFIX);
				String fileName = page.getPageId() + "." + WIKI_FILE_SUFFIX + "-" + page.getVersion();
				copiedItem.rename(fileName);
			}
			item.delete();
		}
		// rename existing meta file to version x and copy it to the version
		// container
		item = wikiContentContainer.resolve(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
		if (item != null && incrementVersion) {
			// TODO renaming and coping does not work. Bug?? felix fragen
			if (page.getVersion() > 0) {
				versionsContainer.copyFrom(item);
				VFSItem copiedItem = versionsContainer.resolve(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
				String fileName = page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX + "-" + page.getVersion();
				copiedItem.rename(fileName);
			}
			item.delete();
		}
		// store recent content file
		VFSLeaf leaf = wikiContentContainer.createChildLeaf(page.getPageId() + "." + WIKI_FILE_SUFFIX);
		if(leaf == null) throw new AssertException("Tried to save wiki page with id ("+page.getPageId()+") and Olatresource: "+ores.getResourceableId()+" but page already existed!");
		FileUtils.save(leaf.getOutputStream(false), page.getContent(), "utf-8");

		// store recent properties file
		leaf = wikiContentContainer.createChildLeaf(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
		if (leaf == null) throw new AssertException("could not create file for wiki page "+page.getPageId()+", ores: "+ores.getResourceableTypeName()+":"+ores.getResourceableId()+", wikicontainer:"+wikiContentContainer);
		if (incrementVersion) page.incrementVersion();
		// update modification time
		if (!page.getContent().equals("")) page.setModificationTime(System.currentTimeMillis());
		Properties p = getPageProperties(page);
		try {
			OutputStream os = leaf.getOutputStream(false);
			p.store(os, "wiki page meta properties");
			os.close();
			// if (incrementVersion) page.incrementVersion();
		} catch (IOException e) {
			throw new OLATRuntimeException(WikiManager.class, "failed to save wiki page properties for page with id: " + page.getPageId() + " and olatresource: " + ores.getResourceableId(), e);
		}
		page.setViewCount(0); //reset view count of the page
		
		//update cache to inform all nodes about the change
		if (wikiCache!=null) {
			wikiCache.update(OresHelper.createStringRepresenting(ores), wiki);
		}
		// do logging
		ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_UPDATE, getClass());
	}

	/**
	 * delete a page completely by removing from the file system
	 * 
	 * @param ores
	 * @param page
	 */
	protected void deleteWikiPage(OLATResourceable ores, WikiPage page) {
		String name = page.getPageName();
		// do not delete default pages
		if (name.equals(WikiPage.WIKI_INDEX_PAGE) || name.equals(WikiPage.WIKI_MENU_PAGE)) return;
		VFSContainer wikiContentContainer = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
		VFSContainer versionsContainer = getWikiContainer(ores, VERSION_FOLDER_NAME);
		//delete content and property file
		VFSItem item = wikiContentContainer.resolve(page.getPageId() + "." + WIKI_FILE_SUFFIX);
		if (item != null) item.delete();
		item = wikiContentContainer.resolve(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
		if (item != null) item.delete();
		
		//delete all version files of the page
		List leafs = versionsContainer.getItems(new VFSLeafFilter());
		if (leafs.size() > 0) {
			for (Iterator iter = leafs.iterator(); iter.hasNext();) {
				VFSLeaf leaf = (VFSLeaf) iter.next();
				String filename = leaf.getName();
				if (filename.startsWith(page.getPageId())) {
					leaf.delete();
				}
			}
		}
		logAudit("Deleted wiki page with name: " + page.getPageName() + " from resourcable id: "+ ores.getResourceableId());
		if (wikiCache!=null) {
			wikiCache.update(OresHelper.createStringRepresenting(ores), getOrLoadWiki(ores));
		}
	}
	
	/**
	 * delete a whole wiki from the cache and the filesystem
	 * @param ores
	 */
	protected void deleteWiki(OLATResourceable ores) {
		if (wikiCache!=null) {
			wikiCache.remove(OresHelper.createStringRepresenting(ores));
		}
		getResourceManager().deleteOLATResourceable(ores);
	}

	/**
	 * @param ores
	 * @param page
	 */
	public void updateWikiPageProperties(OLATResourceable ores, WikiPage page) {
		saveWikiPageProperties(ores, page);
		if (wikiCache!=null) {
			wikiCache.update(OresHelper.createStringRepresenting(ores), getOrLoadWiki(ores));
		}
	}


	private void saveWikiPageProperties(OLATResourceable ores, WikiPage page) {
		VFSContainer wikiContentContainer = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
		VFSLeaf leaf = (VFSLeaf) wikiContentContainer.resolve(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
		if (leaf == null) leaf = wikiContentContainer.createChildLeaf(page.getPageId() + "." + WIKI_PROPERTIES_SUFFIX);
		Properties p = getPageProperties(page);
		try {
			p.store(leaf.getOutputStream(false), "wiki page meta properties");
		} catch (IOException e) {
			throw new OLATRuntimeException(WikiManager.class, "failed to save wiki page properties for page with id: " + page.getPageId() +" and olatresource: " + ores.getResourceableId(), e);
		}
	}

	/**
	 * @param page
	 * @return the fields of the page object as properties
	 */
	private Properties getPageProperties(WikiPage page) {
		Properties p = new Properties();
		p.setProperty(PAGENAME, page.getPageName());
		p.setProperty(VERSION, String.valueOf(page.getVersion()));
		p.setProperty(FORUM_KEY, String.valueOf(page.getForumKey()));
		p.setProperty(INITIAL_AUTHOR, String.valueOf(page.getInitalAuthor()));
		p.setProperty(MODIFY_AUTHOR, String.valueOf(page.getModifyAuthor()));
		p.setProperty(C_TIME, String.valueOf(page.getCreationTime()));
		p.setProperty(VIEW_COUNT, String.valueOf(page.getViewCount()));
		p.setProperty(M_TIME, String.valueOf(page.getModificationTime()));
		p.setProperty(UPDATE_COMMENT, page.getUpdateComment());
		return p;
	}

	/**
	 * @param pageName
	 * @return
	 */
	public static String generatePageId(String pageName) {
		try {
			String encoded = new String(Base64.encodeBase64(pageName.getBytes("utf-8")), "us-ascii");
			encoded = encoded.replace('/', '_'); //base64 can contain "/" so we have to replace them
			return encoded;
		} catch (UnsupportedEncodingException e) {
			throw new OLATRuntimeException(WikiManager.class, "Encoding UTF-8 not supported by your platform!", e);
		}
	}

	/**
	 * @param ores
	 * @param folderName
	 * @return the Vfs container or null if not found
	 */
	public VFSContainer getWikiContainer(OLATResourceable ores, String folderName) {
		VFSContainer wikiRootContainer = getWikiRootContainer(ores);
		return (VFSContainer) wikiRootContainer.resolve(folderName);
	}
	
	/**
	 * Returns the root-container for certain OLAT-resourceable.
	 * @param ores
	 * @return
	 */
	public VFSContainer getWikiRootContainer(OLATResourceable ores) {
		// Check if Resource is a BusinessGroup, because BusinessGroup-wiki's are stored at a different place
		if(isLogDebugEnabled()){
			logDebug("calculating wiki root container with ores id: "+ores.getResourceableId()+" and resourcable type name: "+ores.getResourceableTypeName(), null);
		}
		if (isGroupContextWiki(ores)) {
			// Group Wiki
			return new OlatRootFolderImpl(getGroupWikiRelPath(ores), null);
		} else {
			// Repository Wiki
			return getFileResourceManager().getFileResourceRootImpl(ores);
		}
	}
	
	/**
	 * Get Wiki-File-Path for certain BusinessGroup.
	 * @param businessGroup
	 * @return
	 */
	private String getGroupWikiRelPath(OLATResourceable ores) {
		return "/cts/wikis/" + ores.getResourceableTypeName() + "/" + ores.getResourceableId();
	}

	/**
	 * Return Media folder for uploading files.
	 * @param ores 
	 * @return
	 */
	public OlatRootFolderImpl getMediaFolder(OLATResourceable ores) {
		// Check if Resource is a BusinessGroup, because BusinessGroup-wiki's are stored at a different place
		if (isGroupContextWiki(ores)) {
			// Group Wiki
			return new OlatRootFolderImpl(getGroupWikiRelPath(ores) + "/" + WikiContainer.MEDIA_FOLDER_NAME, null);
		} else {
			// Repository Wiki	
		  return new OlatRootFolderImpl("/repository/" + ores.getResourceableId() + "/" + WikiContainer.MEDIA_FOLDER_NAME,	null);
		}
	}
	
	/**
	 * @return false if repository wiki or true if group only wiki
	 */
	/**
	 * @return false if repository wiki or true if group only wiki
	 */
	protected boolean isGroupContextWiki(OLATResourceable ores) {
		return ores.getResourceableTypeName().equals(OresHelper.calculateTypeName(BusinessGroup.class));
	}

	/**
	 * wiki subscription context for wikis in the course.
	 * @param cenv
	 * @param wikiCourseNode
	 * @return
	 */
	public static SubscriptionContext createTechnicalSubscriptionContextForCourse(CourseEnvironment cenv, WikiCourseNode wikiCourseNode) {
		return new SubscriptionContext(CourseModule.getCourseTypeName(), cenv.getCourseResourceableId(), wikiCourseNode.getIdent());
	}

	public void setResourceManager(OLATResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	public OLATResourceManager getResourceManager() {
		return resourceManager;
	}

	public void setFileResourceManager(FileResourceManager fileResourceManager) {
		this.fileResourceManager = fileResourceManager;
	}

	public FileResourceManager getFileResourceManager() {
		return fileResourceManager;
	}
	
}