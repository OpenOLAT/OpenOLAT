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
package org.olat.core.commons.services.video.dispatcher;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.olat.core.commons.services.text.TextService;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSTranscodingService;
import org.olat.core.commons.services.vfs.manager.VFSMetadataDAO;
import org.olat.core.commons.services.video.MovieService;
import org.olat.core.commons.services.video.TranscoderHelper;
import org.olat.core.commons.services.video.model.TranscoderJobType;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.manager.VideoTranscodingDAO;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Covers OO-9999 WP3 (verify the callback token in the dispatcher before any
 * other processing): a missing token, a token that does not verify, or a
 * token signed for a different job are all rejected with 403, and none of
 * the business-logic collaborators (file download, VFS/DB writes) are ever
 * reached.
 *
 * Initial date: 2026-11-02<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TranscodingDeliveryDispatcherTest {

	private static final String UUID_A = "11111111-1111-1111-1111-111111111111";
	private static final Long REFERENCE_ID_A = 111L;
	private static final Long REFERENCE_ID_B = 222L;
	private static final String VALID_TOKEN = "valid-token-for-A";

	@Mock
	private AVModule avModuleMock;
	@Mock
	private VFSMetadataDAO vfsMetadataDaoMock;
	@Mock
	private VFSTranscodingService vfsTranscodingServiceMock;
	@Mock
	private VideoManager videoManagerMock;
	@Mock
	private VideoModule videoModuleMock;
	@Mock
	private HttpClientService httpClientServiceMock;
	@Mock
	private RepositoryEntryDAO repositoryEntryDaoMock;
	@Mock
	private VideoTranscodingDAO videoTranscodingDaoMock;
	@Mock
	private MovieService movieServiceMock;
	@Mock
	private TextService textServiceMock;
	@Mock
	private VFSRepositoryService vfsRepositoryServiceMock;
	@Mock
	private TranscoderHelper transcoderHelperMock;

	private TranscodingDeliveryDispatcher dispatcher;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		dispatcher = new TranscodingDeliveryDispatcher();
		ReflectionTestUtils.setField(dispatcher, "avModule", avModuleMock);
		ReflectionTestUtils.setField(dispatcher, "vfsMetadataDao", vfsMetadataDaoMock);
		ReflectionTestUtils.setField(dispatcher, "vfsTranscodingService", vfsTranscodingServiceMock);
		ReflectionTestUtils.setField(dispatcher, "videoManager", videoManagerMock);
		ReflectionTestUtils.setField(dispatcher, "videoModule", videoModuleMock);
		ReflectionTestUtils.setField(dispatcher, "httpClientService", httpClientServiceMock);
		ReflectionTestUtils.setField(dispatcher, "repositoryEntryDao", repositoryEntryDaoMock);
		ReflectionTestUtils.setField(dispatcher, "videoTranscodingDao", videoTranscodingDaoMock);
		ReflectionTestUtils.setField(dispatcher, "movieService", movieServiceMock);
		ReflectionTestUtils.setField(dispatcher, "textService", textServiceMock);
		ReflectionTestUtils.setField(dispatcher, "vfsRepositoryService", vfsRepositoryServiceMock);
		ReflectionTestUtils.setField(dispatcher, "transcoderHelper", transcoderHelperMock);

		// The token this instance was (notionally) signed for: valid only for
		// exactly this type/uuid/referenceId triple.
		when(transcoderHelperMock.verifyCallbackToken(VALID_TOKEN, TranscoderJobType.videoConversion, UUID_A, REFERENCE_ID_A))
				.thenReturn(true);
		// Every other (token, type, uuid, referenceId) combination is left
		// unstubbed and therefore returns Mockito's default of false.
	}

	private HttpServletRequest mockRequest(String pathAfterNamespace, String body) throws IOException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn("POST");
		when(request.getContextPath()).thenReturn("");
		when(request.getPathInfo()).thenReturn("/transcoding/");
		when(request.getRequestURI()).thenReturn("/transcoding/" + pathAfterNamespace);
		when(request.getInputStream()).thenReturn(new ByteArrayServletInputStream(body.getBytes(StandardCharsets.UTF_8)));
		return request;
	}

	private void verifyNoBusinessLogicInteractions() {
		verifyNoInteractions(httpClientServiceMock, vfsMetadataDaoMock, vfsTranscodingServiceMock, videoManagerMock,
				repositoryEntryDaoMock, videoTranscodingDaoMock, movieServiceMock, textServiceMock, vfsRepositoryServiceMock);
	}

	@Test
	public void rejectsNotifyResultWithoutAToken() throws Exception {
		String body = "{\"uuid\":\"" + UUID_A + "\",\"type\":\"videoConversion\",\"referenceId\":" + REFERENCE_ID_A
				+ ",\"generated\":{\"status\":100,\"url\":\"https://service.example.org/file.mp4\"}}";
		HttpServletRequest request = mockRequest("notifyResult", body);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		dispatcher.execute(request, response);

		verify(transcoderHelperMock).verifyCallbackToken(isNull(), eq(TranscoderJobType.videoConversion), eq(UUID_A), eq(REFERENCE_ID_A));
		verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
		verifyNoBusinessLogicInteractions();
	}

	@Test
	public void rejectsNotifyResultWithAWrongToken() throws Exception {
		String body = "{\"uuid\":\"" + UUID_A + "\",\"type\":\"videoConversion\",\"referenceId\":" + REFERENCE_ID_A
				+ ",\"generated\":{\"status\":100,\"url\":\"https://service.example.org/file.mp4\"}}";
		HttpServletRequest request = mockRequest("notifyResult/wrong-token", body);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		dispatcher.execute(request, response);

		verify(transcoderHelperMock).verifyCallbackToken(eq("wrong-token"), eq(TranscoderJobType.videoConversion), eq(UUID_A), eq(REFERENCE_ID_A));
		verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
		verifyNoBusinessLogicInteractions();
	}

	@Test
	public void rejectsNotifyResultWhenTheTokenWasSignedForAnotherReferenceId() throws Exception {
		// VALID_TOKEN only verifies for REFERENCE_ID_A; replay it against a
		// callback that claims REFERENCE_ID_B.
		String body = "{\"uuid\":\"" + UUID_A + "\",\"type\":\"videoConversion\",\"referenceId\":" + REFERENCE_ID_B
				+ ",\"generated\":{\"status\":100,\"url\":\"https://service.example.org/file.mp4\"}}";
		HttpServletRequest request = mockRequest("notifyResult/" + VALID_TOKEN, body);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		dispatcher.execute(request, response);

		verify(transcoderHelperMock).verifyCallbackToken(eq(VALID_TOKEN), eq(TranscoderJobType.videoConversion), eq(UUID_A), eq(REFERENCE_ID_B));
		verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
		verifyNoBusinessLogicInteractions();
	}

	@Test
	public void acceptsNotifyResultWithAValidToken() throws Exception {
		String body = "{\"uuid\":\"" + UUID_A + "\",\"type\":\"videoConversion\",\"referenceId\":" + REFERENCE_ID_A
				+ ",\"generated\":{\"status\":100,\"url\":\"https://service.example.org/file.mp4\"}}";
		HttpServletRequest request = mockRequest("notifyResult/" + VALID_TOKEN, body);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		dispatcher.execute(request, response);

		// Reaches the business logic: proof the token gate let it through.
		verify(vfsMetadataDaoMock).loadMetadata(REFERENCE_ID_A);
		verify(response, never()).sendError(HttpServletResponse.SC_FORBIDDEN);
	}

	@Test
	public void rejectsNotifyStatusWithoutAToken() throws Exception {
		String body = "{\"uuid\":\"" + UUID_A + "\",\"type\":\"videoConversion\",\"referenceId\":" + REFERENCE_ID_A + ",\"status\":42.0}";
		HttpServletRequest request = mockRequest("notifyStatus", body);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		dispatcher.execute(request, response);

		verify(transcoderHelperMock).verifyCallbackToken(isNull(), eq(TranscoderJobType.videoConversion), eq(UUID_A), eq(REFERENCE_ID_A));
		verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
		verifyNoBusinessLogicInteractions();
	}

	@Test
	public void rejectsNotifyStatusWithAWrongToken() throws Exception {
		String body = "{\"uuid\":\"" + UUID_A + "\",\"type\":\"videoConversion\",\"referenceId\":" + REFERENCE_ID_A + ",\"status\":42.0}";
		HttpServletRequest request = mockRequest("notifyStatus/wrong-token", body);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		dispatcher.execute(request, response);

		verify(transcoderHelperMock).verifyCallbackToken(eq("wrong-token"), eq(TranscoderJobType.videoConversion), eq(UUID_A), eq(REFERENCE_ID_A));
		verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
		verifyNoBusinessLogicInteractions();
	}

	/**
	 * Minimal servlet input stream over a byte array.
	 */
	private static final class ByteArrayServletInputStream extends ServletInputStream {

		private final ByteArrayInputStream in;

		ByteArrayServletInputStream(byte[] bytes) {
			this.in = new ByteArrayInputStream(bytes);
		}

		@Override
		public int read() {
			return in.read();
		}

		@Override
		public int read(byte[] bytes, int offset, int length) {
			return in.read(bytes, offset, length);
		}

		@Override
		public boolean isFinished() {
			return in.available() == 0;
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setReadListener(ReadListener listener) {
			//
		}
	}
}
