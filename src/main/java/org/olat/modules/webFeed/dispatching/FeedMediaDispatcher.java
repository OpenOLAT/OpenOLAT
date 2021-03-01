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
package org.olat.modules.webFeed.dispatching;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.feed.blog.BlogToolController;
import org.olat.course.run.userview.AccessibleFilter;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.resource.OLATResourceManager;

/**
 * Dispatch any media files belonging to a podcast which an identity is
 * authorized to access. The media file can belong to a learning resource or a
 * course node.
 * <p>
 * Examples: see Path constructor
 * <p>
 * Initial Date: Mar 10, 2009 <br>
 * 
 * @author gwassmann
 */
public class FeedMediaDispatcher implements Dispatcher, GenericEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(FeedMediaDispatcher.class);

	public static final String TOKEN_PROVIDER = "feed";
	
	private DB dbInstance;
	private FeedManager feedManager;
	private BaseSecurity securityManager;
	private RepositoryManager repositoryManager;
	private OLATResourceManager resourceManager;
	private CacheWrapper<FeedPathKey,Boolean> validatedUriCache;

	/**
	 * [used by Spring]
	 * @param feedManager
	 */
	public void setFeedManager(FeedManager feedManager) {
		this.feedManager = feedManager;
	}
	
	/**
	 * [used by Spring]
	 * @param coordinator
	 */
	public void setCoordinatorManager(CoordinatorManager coordinator) {
		validatedUriCache = coordinator.getCoordinator().getCacher().getCache(Path.class.getSimpleName(), "feed");
		coordinator.getCoordinator().getEventBus().registerFor(this, null, RepositoryService.REPOSITORY_EVENT_ORES);	
	}
	
	/**
	 * [used by Spring]
	 * @param securityManager
	 */
	public void setSecurityManager(BaseSecurity securityManager) {
		this.securityManager = securityManager;
	}
	
	/**
	 * [used by Spring]
	 * @param repositoryManager
	 */
	public void setRepositoryManager(RepositoryManager repositoryManager) {
		this.repositoryManager = repositoryManager;
	}
	
	/**
	 * [used by Spring]
	 * @param dbInstance
	 */
	public void setDbInstance(DB dbInstance) {
		this.dbInstance = dbInstance;
	}

	/**
	 * [used by Spring]
	 * @param resourceManager
	 */
	public void setResourceManager(OLATResourceManager resourceManager) {
		this.resourceManager = resourceManager;
	}

	@Override
	public void event(Event event) {
		if(event instanceof EntryChangedEvent) {
			EntryChangedEvent ece = (EntryChangedEvent)event;
			if(ece.getChange() == Change.modifiedAccess || ece.getChange() == Change.modifiedAtPublish) {
				Long entryKey = ece.getRepositoryEntryKey();
				discardCache(entryKey);
			}
		}
	}
	
	private void discardCache(Long entryKey) {
		List<FeedPathKey> keys = validatedUriCache.getKeys();
		if(!keys.isEmpty()) {	
			RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(entryKey);
			if(entry != null) {
				Long resourceId = entry.getOlatResource().getResourceableId();
				for(FeedPathKey key:keys) {
					if(resourceId.equals(key.getResourceId())) {
						try {
							validatedUriCache.remove(key);
						} catch (Exception e) {
							log.info("Cannot remove this key: {}", key);
						}
					}
				}
			}
		}
	}

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		String requestedPath = getPath(request, uriPrefix);

		UserRequest ureq = null;
		try{
			//upon creation URL is checked for 
			ureq = new UserRequestImpl(uriPrefix, request, response);
		} catch(NumberFormatException nfe) {
			//
		}
		
		if(requestedPath == null || requestedPath.length() == 0) {
			DispatcherModule.sendBadRequest(request.getRequestURI(), response);
			return;
		}

		Path path = null;
		try {
			// Assume the URL was correct.
			// At first, look up path in cache. Even before extracting any parameters
			path = new Path(requestedPath);
			path.compile();
			
			// See brasatoconfigpart.xml. The uriPrefix is like '/olat/podcast/' or
			// '/company/blog/'. Get the podcast or blog string.
			// remove the last slash if it exists
			int lastIndex = uriPrefix.length() - 1;
			if (uriPrefix.lastIndexOf('/') == lastIndex) {
				uriPrefix = uriPrefix.substring(0, lastIndex);
			}
			int lastSlashPos = uriPrefix.lastIndexOf('/');
			String feedUriPrefix = uriPrefix.substring(lastSlashPos + 1);
			OLATResourceable feed = resourceManager.findResourceable(path.getFeedId(), getResourceType(feedUriPrefix));
			if(isAccessible(ureq, path, feed)) {
				deliverFile(request, response, feed, path);
			} else {
				log.info("Access was denied. Path::{}", path);
				DispatcherModule.sendForbidden(request.getRequestURI(), response);
			}
		} catch (InvalidPathException e) {
			log.warn("The requested path is invalid. path::{}", path, e);
			DispatcherModule.sendBadRequest(request.getRequestURI(), response);
		} catch (Exception e) {
			log.warn("Nothing was delivered. Path::{}", path, e);
			DispatcherModule.sendNotFound(request.getRequestURI(), response);
		}
	}
	
	public static final String getURIPrefix(String type) {
		if(PodcastFileResource.TYPE_NAME.equals(type)) {
			return FeedManager.KIND_PODCAST;
		} else if(BlogFileResource.TYPE_NAME.equals(type)) {
			return FeedManager.KIND_BLOG;
		}
		return null;
	}
	
	private static final String getResourceType(String feedUriPrefix) {
		if(FeedManager.KIND_PODCAST.equals(feedUriPrefix)) {
			return PodcastFileResource.TYPE_NAME;
		} else if(FeedManager.KIND_BLOG.equals(feedUriPrefix)) {
			return BlogFileResource.TYPE_NAME;
		}
		return null;
	}
	
	private boolean isAccessible(UserRequest ureq, Path path, OLATResourceable feed) {
		Boolean accessible = null;
	
		Long ressourceId;
		if(path.getCourseId() == null) {
			ressourceId = path.getFeedId();
		} else {
			ressourceId = path.getCourseId();
		}
		if(path.getIdentityKey() == null && ureq != null && ureq.getIdentity() != null) {
			path.setIdentityKey(ureq.getIdentity().getKey());
		}
		
		FeedPathKey key = new FeedPathKey(path.getIdentityKey(), ressourceId, path.getNodeId());
		if (Settings.isDebuging()) {
			boolean hasAccess = hasAccess(ureq, feed, path);
			accessible = Boolean.valueOf(hasAccess);
		} else {
			accessible = validatedUriCache.computeIfAbsent(key, k -> {
				boolean hasAccess = hasAccess(ureq, feed, path);
				return Boolean.valueOf(hasAccess);
			});
		}

		return accessible != null && accessible.booleanValue();
	}

	/**
	 * Dispatch and deliver the requested file given in the path.
	 * 
	 * @param request
	 * @param response
	 * @param feed
	 * @param path
	 */
	private void deliverFile(HttpServletRequest request, HttpServletResponse response, OLATResourceable feed, Path path) {
		// OLAT-5243 related: deliverFile can last arbitrary long, which can cause the open db connection to timeout and cause errors,
		// hence we need to do an intermediateCommit here
		dbInstance.intermediateCommit();

		// Create the resource to be delivered
		MediaResource resource = null;

		if (path.isFeedType()) {
			// Only create feed if modified. Send not modified response else.
			Identity identity = null;
			Roles roles = null;
			if(path.getIdentityKey() != null) {
				identity = securityManager.loadIdentityByKey(path.getIdentityKey());
				roles = securityManager.getRoles(identity);
			} else {
				roles = Roles.guestRoles();
			}
			long sinceModifiedMillis = request.getDateHeader("If-Modified-Since");
			
			Feed feedLight = feedManager.loadFeed(feed);
			long lastModifiedMillis = -1;
			if (feedLight != null) {
				lastModifiedMillis = feedLight.getLastModified().getTime();
			}
			boolean sendLastModified = Settings.isDebuging()
					? false
					: sinceModifiedMillis >= (lastModifiedMillis / 1000L) * 1000L;
			if (sendLastModified) {
				// Send not modified response
				response.setDateHeader("last-modified", lastModifiedMillis);
				try {
					response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
					return;
				} catch (IOException e) {
					// Send not modified failed
					log.error("Send not modified failed", e);
					return;
				}
			} else {
				resource = feedManager.createFeedFile(feed, identity, roles, path.getCourseId(), path.getNodeId());
			}
		} else if (path.isItemType()) {
			resource = feedManager.createItemMediaFile(feed, path.getItemId(), path.getItemFileName());
		} else if (path.isIconType()) {
			Size thumbnailSize = null;
			String thumbnail = request.getParameter("thumbnail");
			if(StringHelper.containsNonWhitespace(thumbnail)) {
				thumbnailSize = Size.parseString(thumbnail);
			}
			VFSLeaf resourceFile = feedManager.createFeedMediaFile(feed, path.getIconFileName(), thumbnailSize);
			if(resourceFile != null) {
				resource = new VFSMediaResource(resourceFile);
			}
		}
		// Eventually deliver the requested resource
		ServletUtil.serveResource(request, response, resource);
	}

	/**
	 * Remove some prefixes from the request path.
	 * 
	 * @param request
	 * @param prefix
	 * @return The path of the request
	 */
	private String getPath(HttpServletRequest request, String prefix) {
		String path = request.getPathInfo();
		// remove servlet context path (/olat) from uri prefix (/olat/podcast)
		prefix = prefix.substring(WebappHelper.getServletContextPath().length());
		// remove prefix (/podcast) from path
		path = path.substring(prefix.length());
		return path;
	}

	/**
	 * The global access verification method.
	 * 
	 * @param feed
	 * @param path
	 * @return true if the path may be dispatched.
	 */
	private boolean hasAccess(UserRequest ureq, OLATResourceable feed, Path path) {
		boolean hasAccess = false;
		if (path.isCourseType()) {
			// A course node is being requested
			ICourse course = CourseFactory.loadCourse(path.getCourseId());
			if(course != null) {
				hasAccess = hasAccess(ureq, path, course, path.getNodeId(), feed);
			}
		} else {
			// A learning resource is being requested
			hasAccess = hasAccess(ureq, path, feed);
		}
		return hasAccess;
	}

	/**
	 * Verifies the access of an identity to a course node.
	 * 
	 * @param identity
	 * @param token
	 * @param course
	 * @param pathNodeId
	 * @return True if the identity has access to the node in the given course.
	 *         False otherwise.
	 */
	private boolean hasAccess(UserRequest ureq, Path path, ICourse course, String pathNodeId, OLATResourceable feed) {
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		if (allowsGuestAccess(entry)) {
			return true;
		}
		
		Roles roles = null;
		Identity identity = null;
		RepositoryEntrySecurity reSecurity = null;
		if(ureq != null && ureq.getIdentity() != null) {
			identity = ureq.getIdentity();
			roles = ureq.getUserSession().getRoles();
			reSecurity = repositoryManager.isAllowed(ureq, entry);
		} else if(path.getIdentityKey() != null) {
			identity = securityManager.loadIdentityByKey(path.getIdentityKey());
			if(validAuthentication(identity, path.getToken())) {
				roles = securityManager.getRoles(identity);
				reSecurity = repositoryManager.isAllowed(identity, roles, entry);
			}
		}
		
		boolean hasAccess = false;
		if(identity != null && roles != null && reSecurity != null) {
			if(BlogToolController.SUBSCRIPTION_SUBIDENTIFIER.equals(pathNodeId)) {
				hasAccess = reSecurity.canLaunch();
			} else {
				IdentityEnvironment ienv = new IdentityEnvironment(identity, roles);
				ienv.setAttributes(new HashMap<>());
				UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment(), null, null, null, null,
						reSecurity.isCourseCoach() || reSecurity.isGroupCoach(), reSecurity.isEntryAdmin(), reSecurity.isCourseParticipant() || reSecurity.isGroupParticipant(),
						false);
				// Build an evaluation tree
				NodeAccessService nodeAccessService = CoreSpringFactory.getImpl(NodeAccessService.class);
				TreeNode treeNode = nodeAccessService.getCourseTreeModelBuilder(userCourseEnv)
						.withFilter(AccessibleFilter.create())
						.build()
						.getNodeById(pathNodeId);
				if (treeNode != null && treeNode.isAccessible()) {
					hasAccess = true;
				} else {
					log.info("Course element not found or access denied. Path::{}", path);
				}
			}
		}
		
		if(!hasAccess) {
			//allow if the feed resource itself allow guest access
			entry = repositoryManager.lookupRepositoryEntry(feed, false);
			if (allowsGuestAccess(entry)) {
				return true;
			}
		}
		return hasAccess;
	}

	/**
	 * Verifiy if the identity has access to the feed.
	 * 
	 * @param identity
	 * @param token
	 * @param feed
	 * @return true if the identity has access.
	 */
	private boolean hasAccess(UserRequest ureq, Path path, OLATResourceable feed) {
		RepositoryEntry entry = repositoryManager.lookupRepositoryEntry(feed, false);
		if (allowsGuestAccess(entry)) {
			return true;
		}
		
		Identity identity = null;
		RepositoryEntrySecurity reSecurity = null;
		if(ureq != null && ureq.getIdentity() != null) {
			identity = ureq.getIdentity();
			if(entry != null) {
				Roles roles = ureq.getUserSession().getRoles();
				reSecurity = repositoryManager.isAllowed(identity, roles, entry);
			}
		} else if(path.getIdentityKey() != null) {
			identity = securityManager.loadIdentityByKey(path.getIdentityKey());
			if(validAuthentication(identity, path.getToken())) {
				Roles roles = securityManager.getRoles(identity);
				reSecurity = repositoryManager.isAllowed(identity, roles, entry);
			}
		}
		
		boolean hasAccess = false;
		if (identity != null) {
			if (entry != null){
				if (reSecurity != null && reSecurity.canLaunch()) {
					hasAccess = true;
				}
			}
		}
		return hasAccess;
	}

	/**
	 * Authenticates the identity by token
	 * 
	 * @param identity
	 * @param token
	 * @return True if authentication is valid
	 */
	private boolean validAuthentication(Identity identity, String token) {
		if(identity == null || token == null) return false;
		
		Authentication authentication = securityManager.findAuthenticationByAuthusername(identity.getKey().toString(), TOKEN_PROVIDER, BaseSecurity.DEFAULT_ISSUER);
		return authentication != null && authentication.getCredential().equals(token);
	}

	/**
	 * @param feed
	 * @return true if the feed allows guest access.
	 */
	private boolean allowsGuestAccess(final RepositoryEntry repoEntry) {
		boolean guestsAllowed = false;
		if (repoEntry != null && repoEntry.isGuests()) {
			guestsAllowed = true;
		}
		return guestsAllowed;
	}
}
