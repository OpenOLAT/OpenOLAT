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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.modules.webFeed.manager.FeedManager;

/**
 * The Path class.
 * <p>
 * Collects all parameters provided in the URL.
 * <p>
 * Implements caching of validated paths.
 * <p>
 * For examples see constructor documentation.
 * <P>
 * Initial Date: Apr 9, 2009 <br>
 * 
 * @author gwassmann
 */
public class Path {

	// Instance variables
	private int type;
	private Long courseId, feedId, identityKey;
	private String originalPath, itemId, nodeId, iconFileName, itemFileName, token;

	// Path types
	private static final int FEED = 1;
	private static final int FEED_MEDIA = 2;
	private static final int ITEM_MEDIA = 3;

	private static final int AUTHENTICATED = 10;
	private static final int AUTHENTICATED_FEED = FEED + AUTHENTICATED;
	private static final int AUTHENTICATED_FEED_MEDIA = FEED_MEDIA + AUTHENTICATED;
	private static final int AUTHENTICATED_ITEM_MEDIA = ITEM_MEDIA + AUTHENTICATED;

	private static final int COURSE = 20;
	private static final int COURSE_FEED = FEED + COURSE;
	private static final int COURSE_FEED_MEDIA = FEED_MEDIA + COURSE;
	private static final int COURSE_ITEM_MEDIA = ITEM_MEDIA + COURSE;

	private static final int AUTHENTICATED_COURSE_FEED = FEED + COURSE + AUTHENTICATED;
	private static final int AUTHENTICATED_COURSE_ICON = FEED_MEDIA + COURSE + AUTHENTICATED;
	private static final int AUTHENTICATED_COURSE_ITEM = ITEM_MEDIA + COURSE + AUTHENTICATED;

	// Patterns
	private static final String NUMBER = "(\\d+)";
	private static final String WORD = "(\\w+)";
	private static final String FILE_NAME = "([^/]+\\.\\w{3,4})";
	private static final String SLASH = "/";
	private static final String BASE_PATH_DELIMITER = "/_/";
	public static final String COURSE_NODE_INDICATOR = "coursenode";
	public static final String MEDIA_DIR = "media";	

	private static final String AUTHENTICATION = NUMBER + SLASH + WORD + SLASH;
	private static final String COURSE_PREFIX = COURSE_NODE_INDICATOR + SLASH;
	private static final String COURSE_NODE = NUMBER + SLASH + WORD + SLASH;
	private static final String FEED_ID = NUMBER + BASE_PATH_DELIMITER;
	private static final String FEED_PATH = FEED_ID + FeedManager.RSS_FEED_NAME;
	private static final String FEED_MEDIA_PATH = FEED_ID + MEDIA_DIR + SLASH + FILE_NAME;
	private static final String ITEM_MEDIA_PATH = FEED_ID + WORD + SLASH + MEDIA_DIR + SLASH + FILE_NAME;

	private static final String COURSE_PATH = COURSE_PREFIX + COURSE_NODE;
	private static final String AUTH_COURSE_PATH = COURSE_PREFIX + AUTHENTICATION + COURSE_NODE;

	// The final path patterns
	private static final Pattern feedPattern = Pattern.compile(FEED_PATH);
	private static final Pattern feedMediaPattern = Pattern.compile(FEED_MEDIA_PATH);
	private static final Pattern itemMediaPattern = Pattern.compile(ITEM_MEDIA_PATH);
	private static final Pattern authFeedPattern = Pattern.compile(AUTHENTICATION + FEED_PATH);
	private static final Pattern authFeedMediaPattern = Pattern.compile(AUTHENTICATION + FEED_MEDIA_PATH);
	private static final Pattern authItemMediaPattern = Pattern.compile(AUTHENTICATION + ITEM_MEDIA_PATH);
	private static final Pattern courseFeedPattern = Pattern.compile(COURSE_PATH + FEED_PATH);
	private static final Pattern courseFeedMediaPattern = Pattern.compile(COURSE_PATH + FEED_MEDIA_PATH);
	private static final Pattern courseItemMediaPattern = Pattern.compile(COURSE_PATH + ITEM_MEDIA_PATH);
	private static final Pattern authCourseFeedPattern = Pattern.compile(AUTH_COURSE_PATH + FEED_PATH);
	private static final Pattern authCourseFeedMediaPattern = Pattern.compile(AUTH_COURSE_PATH + FEED_MEDIA_PATH);
	private static final Pattern authCourseItemMediaPattern = Pattern.compile(AUTH_COURSE_PATH + ITEM_MEDIA_PATH);

	private static List<Pattern> patterns = new ArrayList<>();
	static {
		patterns.add(feedPattern);
		patterns.add(feedMediaPattern);
		patterns.add(itemMediaPattern);
		patterns.add(authFeedPattern);
		patterns.add(authFeedMediaPattern);
		patterns.add(authItemMediaPattern);
		patterns.add(courseFeedPattern);
		patterns.add(courseFeedMediaPattern);
		patterns.add(courseItemMediaPattern);
		patterns.add(authCourseFeedPattern);
		patterns.add(authCourseFeedMediaPattern);
		patterns.add(authCourseItemMediaPattern);
	}

	/**
	 * Make a new path object and try to match a valid path pattern. The path
	 * argument should look something like this:
	 * <ol>
	 * <li>{feedId}/_/feed.rss
	 * <li>{feedId}/_/icon.jpg
	 * <li>{feedId}/_/{itemId}/ruby.mp3
	 * <li>{identityKey}/{token}/{feedId}/_/feed.rss
	 * <li>{identityKey}/{token}/{feedId}/_/icon.jpg
	 * <li>{identityKey}/{token}/{feedId}/_/{itemId}/video.mp4
	 * <li>coursenode/{courseId}/{nodeId}/{feedId}/_/feed.rss
	 * <li>coursenode/{courseId}/{nodeId}/{feedId}/_/icon.jpg
	 * <li>coursenode/{courseId}/{nodeId}/{feedId}/_/{itemId}/{feedId}/week52.mp3
	 * <li>
	 * coursenode/{identityKey}/{token}/{courseId}/{nodeId}/{feedId}/_/feed.rss
	 * <li>
	 * coursenode/{identityKey}/{token}/{courseId}/{nodeId}/{feedId}/_/icon.jpg
	 * <li>
	 * coursenode/{identityKey}/{token}/{courseId}/{nodeId}/{feedId}/_/{itemId}/
	 * week52.mp3
	 * </ol>
	 * 
	 * @param path The requested path
	 */
	Path(String path) {
		this.originalPath = path;
	}

	/**
	 * Try to match the path to the patterns and extract the parameters.
	 */
	public void compile() throws InvalidPathException {
		Matcher match = null;
		for (Pattern pattern : patterns) {
			match = pattern.matcher(originalPath);
			if (match.matches()) {
				getParameters(match);
				break;
			}
		}
		if (!match.matches()) { throw new InvalidPathException(); }
	}

	/**
	 * Read all parameters from the matched path.
	 * 
	 * @param match
	 */
	private void getParameters(Matcher match) {
		Pattern pattern = match.pattern();
		if (feedPattern.equals(pattern)) {
			// {feedId}/feed.rss
			type = FEED;
			feedId = Long.parseLong(match.group(1));
			// The feed file name is constant, hence not needed
		} else if (feedMediaPattern.equals(pattern)) {
			// {feedId}/icon.jpg
			type = FEED_MEDIA;
			feedId = Long.parseLong(match.group(1));
			iconFileName = match.group(2);
		} else if (itemMediaPattern.equals(pattern)) {
			// {feedId}/{itemId}/ruby.mp3
			type = ITEM_MEDIA;
			feedId = Long.parseLong(match.group(1));
			setItemId(match.group(2));
			itemFileName = match.group(3);

		} else if (authFeedPattern.equals(pattern)) {
			// {identityKey}/{token}/{feedId}/feed.rss
			type = AUTHENTICATED_FEED;
			identityKey = Long.parseLong(match.group(1));
			token = match.group(2);
			feedId = Long.parseLong(match.group(3));
		} else if (authFeedMediaPattern.equals(pattern)) {
			// {identityKey}/{token}/{feedId}/icon.jpg
			type = AUTHENTICATED_FEED_MEDIA;
			identityKey = Long.parseLong(match.group(1));
			token = match.group(2);
			feedId = Long.parseLong(match.group(3));
			iconFileName = match.group(4);
		} else if (authItemMediaPattern.equals(pattern)) {
			// {identityKey}/{token}/{feedId}/{itemId}/ruby.mp3
			type = AUTHENTICATED_ITEM_MEDIA;
			identityKey = Long.parseLong(match.group(1));
			token = match.group(2);
			feedId = Long.parseLong(match.group(3));
			setItemId(match.group(4));
			itemFileName = match.group(5);

		} else if (courseFeedPattern.equals(pattern)) {
			// coursenode/{courseId}/{nodeId}/{feedId}/feed.rss
			type = COURSE_FEED;
			courseId = Long.parseLong(match.group(1));
			nodeId = match.group(2);
			feedId = Long.parseLong(match.group(3));
		} else if (courseFeedMediaPattern.equals(pattern)) {
			// coursenode/{courseId}/{nodeId}/{feedId}/icon.jpg
			type = COURSE_FEED_MEDIA;
			courseId = Long.parseLong(match.group(1));
			nodeId = match.group(2);
			feedId = Long.parseLong(match.group(3));
			iconFileName = match.group(4);
		} else if (courseItemMediaPattern.equals(pattern)) {
			// coursenode/{courseId}/{nodeId}/{feedId}/{itemId}/ruby.mp3
			type = COURSE_ITEM_MEDIA;
			courseId = Long.parseLong(match.group(1));
			nodeId = match.group(2);
			feedId = Long.parseLong(match.group(3));
			setItemId(match.group(4));
			itemFileName = match.group(5);

		} else if (authCourseFeedPattern.equals(pattern)) {
			// coursenode/{identityKey}/{token}/{courseId}/{nodeId}/{feedId}/feed.rss
			type = AUTHENTICATED_COURSE_FEED;
			identityKey = Long.parseLong(match.group(1));
			token = match.group(2);
			courseId = Long.parseLong(match.group(3));
			nodeId = match.group(4);
			feedId = Long.parseLong(match.group(5));
		} else if (authCourseFeedMediaPattern.equals(pattern)) {
			// coursenode/{identityKey}/{token}/{courseId}/{nodeId}/{feedId}/icon.jpg
			type = AUTHENTICATED_COURSE_ICON;
			identityKey = Long.parseLong(match.group(1));
			token = match.group(2);
			courseId = Long.parseLong(match.group(3));
			nodeId = match.group(4);
			feedId = Long.parseLong(match.group(5));
			iconFileName = match.group(6);
		} else if (authCourseItemMediaPattern.equals(pattern)) {
			// coursenode/{identityKey}/{token}/{courseId}/{nodeId}/{feedId}/{itemId}/ruby.mp3
			type = AUTHENTICATED_COURSE_ITEM;
			identityKey = Long.parseLong(match.group(1));
			token = match.group(2);
			courseId = Long.parseLong(match.group(3));
			nodeId = match.group(4);
			feedId = Long.parseLong(match.group(5));
			setItemId(match.group(6));
			itemFileName = match.group(7);
		}
	}

	/**
	 * @param feedId The feedId to set.
	 */
	public void setFeedId(Long feedId) {
		this.feedId = feedId;
	}

	/**
	 * @param courseId The courseId to set.
	 */
	public void setCourseId(Long courseId) {
		this.courseId = courseId;
	}

	/**
	 * @return Returns the courseId.
	 */
	public Long getCourseId() {
		return courseId;
	}

	/**
	 * @return The feedId.
	 */
	public Long getFeedId() {
		return feedId;
	}

	/**
	 * @param itemId The itemId to set.
	 */
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	/**
	 * @return Returns the itemId.
	 */
	public String getItemId() {
		return itemId;
	}

	/**
	 * @param nodeId The nodeId to set.
	 */
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return Returns the nodeId.
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * @param iconFileName The iconFileName to set.
	 */
	public void setIconFileName(String iconFileName) {
		this.iconFileName = iconFileName;
	}

	/**
	 * @return Returns the iconFileName.
	 */
	public String getIconFileName() {
		return iconFileName;
	}

	/**
	 * @param itemFileName The itemFileName to set.
	 */
	public void setItemFileName(String itemFileName) {
		this.itemFileName = itemFileName;
	}

	/**
	 * @return Returns the itemFileName.
	 */
	public String getItemFileName() {
		return itemFileName;
	}

	/**
	 * @param identityKey The identityKey to set.
	 */
	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	/**
	 * @return Returns the identityKey.
	 */
	public Long getIdentityKey() {
		return identityKey;
	}

	/**
	 * @param token The token to set.
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return Returns the token.
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return true if the path is an RSS feed file.
	 */
	public boolean isFeedType() {
		return type % 10 == FEED;
	}

	/**
	 * @return true if the path is the feed icon.
	 */
	public boolean isIconType() {
		return type % 10 == FEED_MEDIA;
	}

	/**
	 * @return true if the path is the item media file.
	 */
	public boolean isItemType() {
		return type % 10 == ITEM_MEDIA;
	}

	/**
	 * @return true if the path is a course type
	 */
	public boolean isCourseType() {
		return type > COURSE;
	}

	@Override
	public String toString() {
		return originalPath;
	}
	
}
