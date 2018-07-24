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
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.LogDelegator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.TreeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.run.userview.VisibleTreeFilter;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
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
public class FeedMediaDispatcher extends LogDelegator implements Dispatcher {

	private static final String PODCAST_URI_PREFIX = FeedManager.KIND_PODCAST;
	private static final String BLOG_URI_PREFIX = FeedManager.KIND_BLOG;
	public static final String TOKEN_PROVIDER = "feed";

	public static Hashtable<String, String> resourceTypes, uriPrefixes;
	static {
		// Mapping: uri prefix --> resource type
		resourceTypes = new Hashtable<String, String>();
		resourceTypes.put(PODCAST_URI_PREFIX, PodcastFileResource.TYPE_NAME);
		resourceTypes.put(BLOG_URI_PREFIX, BlogFileResource.TYPE_NAME);

		// Mapping: resource type --> uri prefix
		uriPrefixes = new Hashtable<String, String>();
		uriPrefixes.put(PodcastFileResource.TYPE_NAME, PODCAST_URI_PREFIX);
		uriPrefixes.put(BlogFileResource.TYPE_NAME, BLOG_URI_PREFIX);
	}

	private static final OLog log = Tracing.createLoggerFor(FeedMediaDispatcher.class);

	/**
	 * @see org.olat.core.dispatcher.Dispatcher#execute(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		String uriPrefix = DispatcherModule.getLegacyUriPrefix(request);
		String requestedPath = getPath(request, uriPrefix);

		Path path = null;
		try {
			// Assume the URL was correct.
			// At first, look up path in cache. Even before extracting any parameters

			path = new Path(requestedPath);
			// See brasatoconfigpart.xml. The uriPrefix is like '/olat/podcast/' or
			// '/company/blog/'. Get the podcast or blog string.
			// remove the last slash if it exists
			int lastIndex = uriPrefix.length() - 1;
			if (uriPrefix.lastIndexOf("/") == lastIndex) {
				uriPrefix = uriPrefix.substring(0, lastIndex);
			}
			int lastSlashPos = uriPrefix.lastIndexOf("/");
			String feedUriPrefix = uriPrefix.substring(lastSlashPos + 1);
			// String feedUriPrefix = uriPrefix.replaceAll("olat|/", "");
			OLATResourceable feed = null;

			if (path.isCachedAndAccessible()) {
				// Serve request
				path.compile();
				feed = OLATResourceManager.getInstance().findResourceable(path.getFeedId(), resourceTypes.get(feedUriPrefix));
				deliverFile(request, response, feed, path);
			} else {
				path.compile();
				feed = OLATResourceManager.getInstance().findResourceable(path.getFeedId(), resourceTypes.get(feedUriPrefix));
				if (hasAccess(feed, path)) {
					// Only cache when accessible
					path.cache(feed, true);
					deliverFile(request, response, feed, path);
				} else {
					// Deny access
					log.info("Access was denied. Path::" + path);
					DispatcherModule.sendForbidden(request.getRequestURI(), response);
				}
			}
		} catch (InvalidPathException e) {
			logWarn("The requested path is invalid. path::" + path, e);
			DispatcherModule.sendBadRequest(request.getRequestURI(), response);
		} catch (Throwable t) {
			logWarn("Nothing was delivered. Path::" + path, t);
			DispatcherModule.sendNotFound(request.getRequestURI(), response);
		}
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
		DBFactory.getInstance().intermediateCommit();

		// Create the resource to be delivered
		MediaResource resource = null;
		FeedManager manager = FeedManager.getInstance();

		if (path.isFeedType()) {
			// Only create feed if modified. Send not modified response else.
			Identity identity = getIdentity(path.getIdentityKey());
			long sinceModifiedMillis = request.getDateHeader("If-Modified-Since");
			
			Feed feedLight = manager.loadFeed(feed);
			long lastModifiedMillis = -1;
			if (feedLight != null) {
				lastModifiedMillis = feedLight.getLastModified().getTime();
			}
			if (sinceModifiedMillis >= (lastModifiedMillis / 1000L) * 1000L) {
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
				resource = manager.createFeedFile(feed, identity, path.getCourseId(), path.getNodeId());
			}
		} else if (path.isItemType()) {
			resource = manager.createItemMediaFile(feed, path.getItemId(), path.getItemFileName());
		} else if (path.isIconType()) {
			Size thumbnailSize = null;
			String thumbnail = request.getParameter("thumbnail");
			if(StringHelper.containsNonWhitespace(thumbnail)) {
				thumbnailSize = Size.parseString(thumbnail);
			}
			VFSLeaf resourceFile = manager.createFeedMediaFile(feed, path.getIconFileName(), thumbnailSize);
			if(resourceFile != null) {
				resource = new VFSMediaResource(resourceFile);
			}
		}
		// Eventually deliver the requested resource
		ServletUtil.serveResource(request, response, resource);
	}

	/**
	 * Get the identity from the key.
	 * 
	 * @param idKey
	 * @return the Identity
	 */
	private Identity getIdentity(Long idKey) {
		Identity identity = null;
		if (idKey != null) {
			identity = BaseSecurityManager.getInstance().loadIdentityByKey(idKey);
		}
		return identity;
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
	private boolean hasAccess(OLATResourceable feed, Path path) {
		boolean hasAccess = false;
		Identity identity = getIdentity(path.getIdentityKey());

		if (path.isCourseType()) {
			// A course node is being requested
			OLATResourceable oresCourse = OLATResourceManager.getInstance()
					.findResourceable(path.getCourseId(), CourseModule.getCourseTypeName());
			ICourse course = CourseFactory.loadCourse(oresCourse);
			CourseNode node = course.getEditorTreeModel().getCourseNode(path.getNodeId());
			// Check access
			hasAccess = hasAccess(identity, path.getToken(), course, node);
		} else {
			// A learning resource is being requested
			hasAccess = hasAccess(identity, path.getToken(), feed);
		}
		return hasAccess;
	}

	/**
	 * Verifies the access of an identity to a course node.
	 * 
	 * @param identity
	 * @param token
	 * @param course
	 * @param node
	 * @return True if the identity has access to the node in the given course.
	 *         False otherwise.
	 */
	private boolean hasAccess(Identity identity, String token, ICourse course, CourseNode node) {
		boolean hasAccess = false;
		final RepositoryManager resMgr = RepositoryManager.getInstance();
		final RepositoryEntry repoEntry = resMgr.lookupRepositoryEntry(course, false);
		if (allowsGuestAccess(repoEntry)) {
			hasAccess = true;
		} else {
			IdentityEnvironment ienv = new IdentityEnvironment();
			ienv.setIdentity(identity);
			Roles roles = BaseSecurityManager.getInstance().getRoles(identity);
			ienv.setRoles(roles);
			UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
			// Build an evaluation tree
			TreeEvaluation treeEval = new TreeEvaluation();
			NodeEvaluation nodeEval = node.eval(userCourseEnv.getConditionInterpreter(), treeEval, new VisibleTreeFilter());
			if (nodeEval.isVisible() && validAuthentication(identity, token)) {
				hasAccess = true;
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
	private boolean hasAccess(Identity identity, String token, OLATResourceable feed) {
		boolean hasAccess = false;
		RepositoryManager resMgr = RepositoryManager.getInstance();
		RepositoryEntry repoEntry = resMgr.lookupRepositoryEntry(feed, false);
		if (allowsGuestAccess(repoEntry)) {
			hasAccess = true;
		} else if (identity != null) {
			if (repoEntry != null){
				final Roles roles = BaseSecurityManager.getInstance().getRoles(identity);
				final boolean isAllowedToLaunch = resMgr.isAllowedToLaunch(identity, roles, repoEntry);
				if (isAllowedToLaunch && validAuthentication(identity, token)) {
					hasAccess = true;
				}
			} else {
				// no repository entry -> could be a feed without a repository-entry (ePortfolio-Blog-feed)
				EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
				if (ePFMgr.checkFeedAccess(feed, identity)){
					return validAuthentication(identity, token);
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
		boolean valid = false;
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		Authentication authentication = secMgr.findAuthenticationByAuthusername(identity.getKey().toString(), TOKEN_PROVIDER);
		if (authentication != null && authentication.getCredential().equals(token)) {
			valid = true;
		}
		return valid;
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

	/**
	 * Redirect to Path.getFeedBaseUri()
	 * 
	 * @param feed
	 * @param identityKey
	 * @return The feed base uri for the given user (identity)
	 */
	public static String getFeedBaseUri(Feed feed, Identity identity, Long courseId, String nodeId) {
		return Path.getFeedBaseUri(feed, identity, courseId, nodeId);
	}
}
