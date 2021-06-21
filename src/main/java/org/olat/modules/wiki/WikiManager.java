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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.PathUtils;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFilter;
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
public class WikiManager {
	
	private static final Logger log = Tracing.createLoggerFor(WikiManager.class);

	public static final String VIEW_COUNT = "view.count";
	public static final String MODIFY_AUTHOR = "modify.author";
	public static final String M_TIME = "mTime";
	public static final String INITIAL_AUTHOR = "initial.author";
	public static final String INITIAL_PAGENAME = "initial.pagename";
	public static final String FORUM_KEY = "forum.key";
	public static final String VERSION = "version";
	public static final String C_TIME = "cTime";
	public static final String PAGENAME = "pagename";
	public static final String OLD_PAGENAME = "old.pagenames";
	private static WikiManager instance;
	public static final String WIKI_RESOURCE_FOLDER_NAME = "wiki";
	public static final String VERSION_FOLDER_NAME = "versions";
	public static final String WIKI_FILE_SUFFIX = "wp";
	public static final String WIKI_DOT_FILE_SUFFIX = "." + WIKI_FILE_SUFFIX;
	public static final String WIKI_PROPERTIES_SUFFIX = "properties";
	public static final String WIKI_DOT_PROPERTIES_SUFFIX = "." + WIKI_PROPERTIES_SUFFIX;
	public static final String UPDATE_COMMENT = "update.comment";
	
  //o_clusterNOK cache : 08.04.08/cg Not tested in cluster-mode 
	CacheWrapper<String,Wiki> wikiCache;
	
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
	
	public WikiMainController createWikiMainController(UserRequest ureq, WindowControl wControl, OLATResourceable ores,
			WikiSecurityCallback securityCallback, WikiAssessmentProvider assessmentProvider, String initialPageName) {
		return new WikiMainController(ureq, wControl, ores, securityCallback, assessmentProvider, initialPageName);
	}

	/**
	 * @param createdBy 
	 * @return the new created resource
	 */
	
	
	public WikiResource createWiki(Identity createdBy) {
		WikiResource resource = new WikiResource();
		createFolders(resource, createdBy);
		OLATResourceManager rm = getResourceManager();
		OLATResource ores = rm.createOLATResourceInstance(resource);
		rm.saveOLATResource(ores);
		return resource;
	}

	public void setCoordinator(CoordinatorManager coord){
			coordinator = coord;
	}
	
	public boolean importWiki(File file, String filename, File targetDirectory) {
		try {
			Path path = FileResource.getResource(file, filename);
			if(path == null) {
				return false;
			}
			
			Path destDir = targetDirectory.toPath();
			Files.walkFileTree(path, new ImportVisitor(destDir));
			PathUtils.closeSubsequentFS(path);
			return true;
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
	}
	
	/**
	 * Copy the wiki pages, media but ignores the versions.
	 * 
	 */
	public boolean copyWiki(File sourceDirectory, File targetDirectory) {
		try {
			Path path = sourceDirectory.toPath();
			Path destDir = targetDirectory.toPath();
			Files.walkFileTree(path, new CopyVisitor(path, destDir));
			return true;
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
	}
	
	/**
	 * Reset the same properties as in the method resetCopiedPage
	 * of WikiPage.
	 * 
	 * @param file
	 * @param destFile
	 */
	private static final void resetAndCopyProperties(Path file, Path destFile) {
		Properties props = new Properties();
		try (InputStream inStream = Files.newInputStream(file);
				OutputStream outStream = Files.newOutputStream(destFile)) {
			props.load(inStream);
			props.setProperty(VERSION, "0");
			props.setProperty(FORUM_KEY, "0");
			props.setProperty(MODIFY_AUTHOR, "0");
			props.setProperty(UPDATE_COMMENT, "0");
			props.setProperty(VIEW_COUNT, "0");
			props.setProperty(M_TIME, "0");
			props.store(outStream, "");
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * Copy the content of a resource of type wiki where the files
	 * are saved in different directories: wiki and media. The versions
	 * are ignored.
	 * 
	 * Initial date: 02.05.2014<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	public static class CopyVisitor extends SimpleFileVisitor<Path> {
		
		private final Path sourceDir;
		private final Path destDir;
		
		public CopyVisitor(Path sourceDir, Path destDir) throws IOException {
			this.destDir = destDir;
			this.sourceDir = sourceDir;
			Path wikiDir = destDir.resolve(WIKI_RESOURCE_FOLDER_NAME);
			Files.createDirectories(wikiDir);
			Path mediaDir = destDir.resolve(WikiContainer.MEDIA_FOLDER_NAME);
			Files.createDirectories(mediaDir);
			Path versionDir = destDir.resolve(VERSION_FOLDER_NAME);
			Files.createDirectories(versionDir);
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
	    throws IOException {

			Path relFile = sourceDir.relativize(file);
			
	        String filename = file.getFileName().toString();
	        if(filename.endsWith(WikiManager.WIKI_PROPERTIES_SUFFIX)) {
	        		final Path destFile = Paths.get(destDir.toString(), relFile.toString());
	        		resetAndCopyProperties(file, destFile);
	        } else if (filename.endsWith(WIKI_FILE_SUFFIX)) {
	        	final Path destFile = Paths.get(destDir.toString(), relFile.toString());
				Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
			} else if (!filename.contains(WIKI_FILE_SUFFIX + "-")
					&& !filename.contains(WIKI_PROPERTIES_SUFFIX + "-")) {
				final Path destFile = Paths.get(destDir.toString(), relFile.toString());
				Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
			}
	        return FileVisitResult.CONTINUE;
		}
	 
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		throws IOException {
			dir = sourceDir.relativize(dir);
			
	        final Path dirToCreate = Paths.get(destDir.toString(), dir.toString());
	        if(Files.notExists(dirToCreate)){
	        	Files.createDirectory(dirToCreate);
	        }
	        return FileVisitResult.CONTINUE;
		}
	}
	
	
	/**
	 * Dispatch the content in the wiki and media folders from an import
	 * where all the files are flatted at the root of the archive.
	 * 
	 * Initial date: 02.05.2014<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	public static class ImportVisitor extends SimpleFileVisitor<Path> {
		
		private final Path destDir;
		private final Path wikiDir;
		private final Path mediaDir;
		
		public ImportVisitor(Path destDir) throws IOException {
			this.destDir = destDir;
			wikiDir = destDir.resolve(WIKI_RESOURCE_FOLDER_NAME);
			Files.createDirectories(wikiDir);
			mediaDir = destDir.resolve(WikiContainer.MEDIA_FOLDER_NAME);
			Files.createDirectories(mediaDir);
			Path versionDir = destDir.resolve(VERSION_FOLDER_NAME);
			Files.createDirectories(versionDir);
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
	    throws IOException {
			String filename = file.getFileName().toString();
			Path normalizedPath = file.normalize();
			if(!normalizedPath.startsWith(destDir)) {
				throw new IOException("Invalid ZIP");
			}
			
	        if(filename.endsWith(WikiManager.WIKI_PROPERTIES_SUFFIX)) {
	        	String f = convertAlternativeFilename(file.toString());
	        	final Path destFile = Paths.get(wikiDir.toString(), f);
	        	resetAndCopyProperties(file, destFile);
	        } else if (filename.endsWith(WIKI_FILE_SUFFIX)) {
	        	String f = convertAlternativeFilename(file.toString());
	        	final Path destFile = Paths.get(wikiDir.toString(), f);
	        	Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
			} else if (!filename.contains(WIKI_FILE_SUFFIX + "-")
					&& !filename.contains(WIKI_PROPERTIES_SUFFIX + "-")) {
				final Path destFile = Paths.get(mediaDir.toString(), file.toString());
				Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
			}
	        return FileVisitResult.CONTINUE;
		}
	 
		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		throws IOException {
			final Path dirToCreate = Paths.get(destDir.toString(), dir.toString());
	        if(Files.notExists(dirToCreate)){
	        		Files.createDirectory(dirToCreate);
	        }
	        return FileVisitResult.CONTINUE;
		}
	}
	
	
/**
 * API change stop
 */
	
	void createFolders(OLATResourceable ores, Identity savedBy) {
		long start = 0;
		if (log.isDebugEnabled()) {
			start = System.currentTimeMillis();
		}
		VFSContainer rootContainer = getWikiRootContainer(ores);
		VFSContainer unzippedDir = (VFSContainer) rootContainer.resolve(FileResourceManager.ZIPDIR);
		if (unzippedDir == null) { // check for _unzipped_ dir from imported wiki's
			if (rootContainer.createChildContainer(WIKI_RESOURCE_FOLDER_NAME) == null) throwError(ores);
			if (rootContainer.createChildContainer(WikiContainer.MEDIA_FOLDER_NAME) == null) throwError(ores);
			if (rootContainer.createChildContainer(VERSION_FOLDER_NAME) == null) throwError(ores);
		} else { // _unzipped_ dir found: move elements to wiki folder and delete
			// unzipped dir and zip files
			List<VFSItem> files = unzippedDir.getItems();
			VFSContainer wikiCtn = rootContainer.createChildContainer(WIKI_RESOURCE_FOLDER_NAME);
			VFSContainer mediaCtn = rootContainer.createChildContainer(WikiContainer.MEDIA_FOLDER_NAME);
			if (rootContainer.createChildContainer(VERSION_FOLDER_NAME) == null) throwError(ores);
			if (wikiCtn == null) throwError(ores);
			// copy files to wiki and media folder
			for (Iterator<VFSItem> iter = files.iterator(); iter.hasNext();) {
				VFSLeaf leaf = ((VFSLeaf) iter.next());
				if (leaf.getName().endsWith(WikiManager.WIKI_FILE_SUFFIX) || leaf.getName().endsWith(WikiManager.WIKI_PROPERTIES_SUFFIX)) {
					wikiCtn.copyFrom(leaf, savedBy);
				} else {
					if (leaf.getName().contains(WikiManager.WIKI_FILE_SUFFIX+"-") || leaf.getName().contains(WikiManager.WIKI_PROPERTIES_SUFFIX+"-")) {
						leaf.delete(); // delete version history
					} else {
						mediaCtn.copyFrom(leaf, savedBy);
					}
				}
			}
			unzippedDir.delete();
			List<VFSItem> zipFiles = rootContainer.getItems(new VFSItemSuffixFilter(new String[] { "zip" }));
			// delete all zips
			for (Iterator<VFSItem> iter = zipFiles.iterator(); iter.hasNext();) {
				VFSLeaf element = (VFSLeaf) iter.next();
				element.delete();
			}
			//reset forum key and author references keys back to default as users and forums may not exist
			List<VFSItem> propertyLeafs = wikiCtn.getItems(new PropertiesFilter());
			for (Iterator<VFSItem> iter = propertyLeafs.iterator(); iter.hasNext();) {
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
		if (log.isDebugEnabled()) {
			long end = System.currentTimeMillis();
			log.debug("creating folders and move files and updating properties to default values took: (milliseconds)"+(end-start));
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
			wikiCache =  coordinator.getCoordinator().getCacher().getCache(WikiManager.class.getSimpleName(), "wiki");
		}
		return wikiCache.computeIfAbsent(wikiKey, key -> {
			long start = 0;
			// wiki not in cache load form filesystem
			if (log.isDebugEnabled()) {
				log.debug("wiki not in cache. Loading wiki from filesystem. Ores: " + ores.getResourceableId());
				start = System.currentTimeMillis();
			}

			VFSContainer folder = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
			// wiki folder structure does not yet exists, but resource does. Create
			// wiki in group context
			if (folder == null) {
				createFolders(ores, null);
				folder = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
			}
			
			// folders should be present, create the wiki
			Wiki wiki = new Wiki(getWikiRootContainer(ores));
			// filter for xyz.properties files
			List<VFSItem> wikiLeaves = folder.getItems(new PropertiesFilter());
			for (Iterator<VFSItem> iter = wikiLeaves.iterator(); iter.hasNext();) {
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
				String idOutOfFileName = propertiesFile.getName().substring(0, propertiesFile.getName().indexOf('.'));
				if (!page.matchIds(idOutOfFileName)) {
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
				saveWikiPage(ores, indexPage, false, wiki, false, null);
				saveWikiPage(ores, menuPage, false, wiki, false, null);
			}
			
			// add pages internally used for displaying dynamic data, they are not persisted
			WikiPage recentChangesPage = new WikiPage(WikiPage.WIKI_RECENT_CHANGES_PAGE);
			WikiPage a2zPage = new WikiPage(WikiPage.WIKI_A2Z_PAGE);
			wiki.addPage(recentChangesPage);
			wiki.addPage(a2zPage);

			if (log.isDebugEnabled()) {
				long stop = System.currentTimeMillis();
				log.debug("loading of wiki from filessystem took (ms) " + (stop - start));
			}
			return wiki;
		});
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
	 * @param savedBy
	 */
	public void saveWikiPage(OLATResourceable ores, WikiPage page, boolean incrementVersion, Wiki wiki, boolean cache, Identity savedBy) {
		//cluster_OK by guido
		VFSContainer versionsContainer = getWikiContainer(ores, VERSION_FOLDER_NAME);
		VFSContainer wikiContentContainer = getWikiContainer(ores, WIKI_RESOURCE_FOLDER_NAME);
		// rename existing content file to version x and copy it to the version
		// container
		VFSItem item = wikiContentContainer.resolve(page.getPageId() + "." + WIKI_FILE_SUFFIX);
		if (item != null && incrementVersion) {
			if (page.getVersion() > 0) {
				versionsContainer.copyFrom(item, savedBy);
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
			if (page.getVersion() > 0) {
				versionsContainer.copyFrom(item, savedBy);
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
		try(OutputStream os = leaf.getOutputStream(false)) {
			p.store(os, "wiki page meta properties");
		} catch (IOException e) {
			throw new OLATRuntimeException(WikiManager.class, "failed to save wiki page properties for page with id: " + page.getPageId() + " and olatresource: " + ores.getResourceableId(), e);
		}
		page.setViewCount(0); //reset view count of the page
		
		//update cache to inform all nodes about the change
		if (cache && wikiCache != null) {
			wikiCache.update(OresHelper.createStringRepresenting(ores), wiki);
		}
		
		if(ThreadLocalUserActivityLogger.getLoggedIdentity() != null) {
			// do logging only for real user
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_UPDATE, getClass());
		}
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
		List<VFSItem> leafs = versionsContainer.getItems(new VFSLeafFilter());
		if (leafs.size() > 0) {
			for (Iterator<VFSItem> iter = leafs.iterator(); iter.hasNext();) {
				VFSLeaf leaf = (VFSLeaf) iter.next();
				String filename = leaf.getName();
				if (filename.startsWith(page.getPageId())) {
					leaf.delete();
				}
			}
		}
		log.info(Tracing.M_AUDIT, "Deleted wiki page with name: " + page.getPageName() + " from resourcable id: "+ ores.getResourceableId());
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
	
	public static String convertAlternativeFilename(String id) {
		String convertedId = id;
		if(id != null) {
			if(id.endsWith(WIKI_DOT_FILE_SUFFIX)) {
				convertedId = convertAlternativeFilename(id, WIKI_DOT_FILE_SUFFIX);
			} else if(id.endsWith(WIKI_DOT_PROPERTIES_SUFFIX)) {
				convertedId = convertAlternativeFilename(id, WIKI_DOT_PROPERTIES_SUFFIX);
			}
		}
		return convertedId;
	}
	
	private static String convertAlternativeFilename(String id, String suffix) {
		char[] idChars = id.toCharArray();
		int indexLast = idChars.length - suffix.length() - 1;
		if(idChars[indexLast] == '_') {
			idChars[indexLast] =  '=';
			if(idChars[indexLast - 1] == '_') {
				idChars[indexLast - 1] =  '=';
			}
			return new String(idChars);
		}
		return id;
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
	public LocalFolderImpl getWikiRootContainer(OLATResourceable ores) {
		// Check if Resource is a BusinessGroup, because BusinessGroup-wiki's are stored at a different place
		if(log.isDebugEnabled()){
			log.debug("calculating wiki root container with ores id: "+ores.getResourceableId()+" and resourcable type name: "+ores.getResourceableTypeName());
		}
		if (isGroupContextWiki(ores)) {
			// Group Wiki
			return VFSManager.olatRootContainer(getGroupWikiRelPath(ores), null);
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
	public LocalFolderImpl getMediaFolder(OLATResourceable ores) {
		// Check if Resource is a BusinessGroup, because BusinessGroup-wiki's are stored at a different place
		if (isGroupContextWiki(ores)) {
			// Group Wiki
			return VFSManager.olatRootContainer(getGroupWikiRelPath(ores) + "/" + WikiContainer.MEDIA_FOLDER_NAME, null);
		} else {
			// Repository Wiki	
			return VFSManager.olatRootContainer("/repository/" + ores.getResourceableId() + "/" + WikiContainer.MEDIA_FOLDER_NAME,	null);
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
	
	private static class PropertiesFilter implements VFSItemFilter {
		@Override
		public boolean accept(VFSItem vfsItem) {
			return !vfsItem.isHidden() && vfsItem.getName().toLowerCase()
					.endsWith(WIKI_DOT_PROPERTIES_SUFFIX);
		}
	}
}