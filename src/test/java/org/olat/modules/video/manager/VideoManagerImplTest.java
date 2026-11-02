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
package org.olat.modules.video.manager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.video.model.TranscoderJobStatus;
import org.olat.core.commons.services.video.model.TranscoderJobType;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.modules.video.VideoTranscoding;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Covers OO-9999 WP4 H3 for the video *status* callback path
 * ({@code handleVideoTranscodingJobStatus}), the sibling of the already-covered
 * H2 (conversion status) and H3 (video result) gates: a status update must be
 * rejected for a row that is no longer pending, so a replayed {@code notifyStatus}
 * callback cannot revert an already finished transcoding back to "in progress".
 *
 * Initial date: 2026-11-02<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VideoManagerImplTest {

	private static final Long RESOURCE_KEY = 4321L;
	private static final String UUID_A = "11111111111111111111111111111111";

	@Mock
	private VideoTranscodingDAO videoTranscodingDaoMock;
	@Mock
	private DB dbInstanceMock;
	@Mock
	private CoordinatorManager coordinatorManagerMock;
	@Mock
	private Coordinator coordinatorMock;
	@Mock
	private EventBus eventBusMock;

	private VideoManagerImpl videoManager;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		when(coordinatorManagerMock.getCoordinator()).thenReturn(coordinatorMock);
		when(coordinatorMock.getEventBus()).thenReturn(eventBusMock);

		videoManager = new VideoManagerImpl();
		ReflectionTestUtils.setField(videoManager, "videoTranscodingDao", videoTranscodingDaoMock);
		ReflectionTestUtils.setField(videoManager, "dbInstance", dbInstanceMock);
	}

	@Test
	public void ignoresAStatusUpdateForARowThatIsAlreadyDone() {
		VideoTranscoding doneTranscoding = mock(VideoTranscoding.class);
		when(doneTranscoding.getStatus()).thenReturn(VideoTranscoding.TRANSCODING_STATUS_DONE);
		when(videoTranscodingDaoMock.getVideoTranscodings(RESOURCE_KEY, UUID_A)).thenReturn(List.of(doneTranscoding));

		handleVideoTranscodingJobStatus(statusOf(42f));

		verify(doneTranscoding, never()).setStatus(anyInt());
		verify(videoTranscodingDaoMock, never()).updateTranscoding(any());
	}

	@Test
	public void appliesAStatusUpdateForARowThatIsStillPending() {
		VideoTranscoding pendingTranscoding = mock(VideoTranscoding.class);
		when(pendingTranscoding.getStatus()).thenReturn(10);
		when(videoTranscodingDaoMock.getVideoTranscodings(RESOURCE_KEY, UUID_A)).thenReturn(List.of(pendingTranscoding));

		handleVideoTranscodingJobStatus(statusOf(42f));

		verify(pendingTranscoding).setStatus(42);
		verify(videoTranscodingDaoMock).updateTranscoding(pendingTranscoding);
	}

	@Test
	public void updatesOnlyThePendingRowsWhenSomeAreAlreadyDone() {
		VideoTranscoding doneTranscoding = mock(VideoTranscoding.class);
		when(doneTranscoding.getStatus()).thenReturn(VideoTranscoding.TRANSCODING_STATUS_DONE);
		VideoTranscoding pendingTranscoding = mock(VideoTranscoding.class);
		when(pendingTranscoding.getStatus()).thenReturn(10);
		when(videoTranscodingDaoMock.getVideoTranscodings(RESOURCE_KEY, UUID_A))
				.thenReturn(List.of(doneTranscoding, pendingTranscoding));

		handleVideoTranscodingJobStatus(statusOf(55f));

		verify(doneTranscoding, never()).setStatus(anyInt());
		verify(videoTranscodingDaoMock, never()).updateTranscoding(doneTranscoding);
		verify(pendingTranscoding).setStatus(55);
		verify(videoTranscodingDaoMock).updateTranscoding(pendingTranscoding);
	}

	private static TranscoderJobStatus statusOf(float percentage) {
		TranscoderJobStatus status = new TranscoderJobStatus();
		status.setType(TranscoderJobType.videoTranscoding);
		status.setUuid(UUID_A);
		status.setReferenceId(RESOURCE_KEY);
		status.setStatus(percentage);
		return status;
	}

	/**
	 * {@code handleVideoTranscodingJobStatus} ends with a call to the static
	 * {@link CoordinatorManager#getInstance()}, which is only ever set by Spring
	 * at context startup. Outside a Spring context it is null, so it is mocked
	 * statically here for the duration of the call (same pattern already used in
	 * {@code RoomManagementServiceTest}).
	 */
	private void handleVideoTranscodingJobStatus(TranscoderJobStatus status) {
		try (MockedStatic<CoordinatorManager> coordinatorManagerStatic = mockStatic(CoordinatorManager.class)) {
			coordinatorManagerStatic.when(CoordinatorManager::getInstance).thenReturn(coordinatorManagerMock);
			videoManager.handleVideoTranscodingJobStatus(status);
		}
	}
}
