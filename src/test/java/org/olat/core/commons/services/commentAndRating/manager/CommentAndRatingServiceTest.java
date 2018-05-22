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
package org.olat.core.commons.services.commentAndRating.manager;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;

/**
 *
 * Initial date: 17.09.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CommentAndRatingServiceTest {

	private static final String RESOURCEABLE_TYPE_NAME = "resurcable type name";
	private static final Long RESOURCABLE_ID = 5L;
	private static final String RES_SUB_PATH = "resSubPath";
	private static final String COMMENT_TEXT = "commentText";
	private static final String UPDATED_COMMENT_TEXT = "updated comment";
	private static final String REPLY_TEXT = "my reply";
	private static Identity IGNORE_NEWS_FOR_NOBODY = null;
	private static boolean SEND_NO_EVENTS = false;

	@Mock
	private Identity identityDummy;
	@Mock
	private OLATResource resourceMock;
	@Mock
	private UserComment userCommentMock;

	@Mock
	private UserCommentsDAO userCommentsDaoMock;
	@Mock
	private NotificationsManager notificationsManagerMock;

	@InjectMocks
	private CommentAndRatingServiceImpl sut = new  CommentAndRatingServiceImpl();

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(resourceMock.getResourceableId()).thenReturn(RESOURCABLE_ID);
		when(resourceMock.getResourceableTypeName()).thenReturn(RESOURCEABLE_TYPE_NAME);

		when(userCommentMock.getResId()).thenReturn(RESOURCABLE_ID);
		when(userCommentMock.getResName()).thenReturn(RESOURCEABLE_TYPE_NAME);
	}

	@Test
	public void shouldSaveCommentWhenCommentCreated() {
		sut.createComment(identityDummy, resourceMock, RES_SUB_PATH, COMMENT_TEXT);

		verify(userCommentsDaoMock).createComment(identityDummy, resourceMock, RES_SUB_PATH, COMMENT_TEXT);
	}

	@Test
	public void shouldSaveCommentWhenCommentUpdated() {
		sut.updateComment(userCommentMock, UPDATED_COMMENT_TEXT);

		verify(userCommentsDaoMock).updateComment(userCommentMock, UPDATED_COMMENT_TEXT);
	}

	@Test
	public void shouldSaveCommentWhenReplyToComment() {
		sut.replyTo(userCommentMock, identityDummy, REPLY_TEXT);

		verify(userCommentsDaoMock).replyTo(userCommentMock, identityDummy, REPLY_TEXT);
	}

	@Test
	public void shouldMarkPublisherNewsWhenCommentCreated() {
		when(userCommentsDaoMock.createComment(identityDummy, resourceMock, RES_SUB_PATH, COMMENT_TEXT))
				.thenReturn(userCommentMock);

		sut.createComment(identityDummy, resourceMock, RES_SUB_PATH, COMMENT_TEXT);

		verify(notificationsManagerMock).markPublisherNews(
				RESOURCEABLE_TYPE_NAME,
				RESOURCABLE_ID.toString(),
				IGNORE_NEWS_FOR_NOBODY,
				SEND_NO_EVENTS);
	}

	@Test
	public void shouldMarkPublisherNewsWhenCommentUpdated() {
		when(userCommentsDaoMock.updateComment(userCommentMock, UPDATED_COMMENT_TEXT))
				.thenReturn(userCommentMock);

		sut.updateComment(userCommentMock, UPDATED_COMMENT_TEXT);

		verify(notificationsManagerMock).markPublisherNews(
				RESOURCEABLE_TYPE_NAME,
				RESOURCABLE_ID.toString(),
				IGNORE_NEWS_FOR_NOBODY,
				SEND_NO_EVENTS);
	}

	@Test
	public void shouldMarkPublisherNewsWhenRepliedToComment() {
		when(userCommentsDaoMock.replyTo(userCommentMock, identityDummy, REPLY_TEXT))
				.thenReturn(userCommentMock);

		sut.replyTo(userCommentMock, identityDummy, REPLY_TEXT);

		verify(notificationsManagerMock).markPublisherNews(
				RESOURCEABLE_TYPE_NAME,
				RESOURCABLE_ID.toString(),
				IGNORE_NEWS_FOR_NOBODY,
				SEND_NO_EVENTS);
	}

	@Test
	public void shouldNotMarkPublisherNewsWhenCommentCreationFailed() {
		when(userCommentsDaoMock.createComment(identityDummy, resourceMock, RES_SUB_PATH, COMMENT_TEXT))
				.thenReturn(null);

		sut.createComment(identityDummy, resourceMock, RES_SUB_PATH, COMMENT_TEXT);

		verify(notificationsManagerMock, never()).markPublisherNews(
				RESOURCEABLE_TYPE_NAME,
				RESOURCABLE_ID.toString(),
				IGNORE_NEWS_FOR_NOBODY,
				SEND_NO_EVENTS);
	}

	@Test
	public void shouldNotMarkPublisherNewsWhenCommentUpdateFailed() {
		when(userCommentsDaoMock.updateComment(userCommentMock, UPDATED_COMMENT_TEXT))
				.thenReturn(null);

		sut.updateComment(userCommentMock, UPDATED_COMMENT_TEXT);

		verify(notificationsManagerMock, never()).markPublisherNews(
				RESOURCEABLE_TYPE_NAME,
				RESOURCABLE_ID.toString(),
				IGNORE_NEWS_FOR_NOBODY,
				SEND_NO_EVENTS);
	}

	@Test
	public void shouldNotMarkPublisherNewsWhenReplyToCommentFailed() {
		when(userCommentsDaoMock.replyTo(userCommentMock, identityDummy, REPLY_TEXT))
				.thenReturn(null);

		sut.replyTo(userCommentMock, identityDummy, REPLY_TEXT);

		verify(notificationsManagerMock, never()).markPublisherNews(
				RESOURCEABLE_TYPE_NAME,
				RESOURCABLE_ID.toString(),
				IGNORE_NEWS_FOR_NOBODY,
				SEND_NO_EVENTS);
	}
}
