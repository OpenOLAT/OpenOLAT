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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
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
import org.olat.core.util.httpclient.ProtectionProfile;
import org.olat.modules.audiovideorecording.AVModule;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoModule;
import org.olat.modules.video.VideoTranscoding;
import org.olat.modules.video.manager.VideoTranscodingDAO;
import org.olat.modules.video.model.VideoTranscodingImpl;
import org.olat.modules.video.model.VideoTranscodingMode;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.resource.OLATResource;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Covers OO-9999 WP3 (verify the callback token in the dispatcher before any
 * other processing): a missing token, a token that does not verify, or a
 * token signed for a different job are all rejected with 403, and none of
 * the business-logic collaborators (file download, VFS/DB writes) are ever
 * reached.
 * <p>
 * Also covers the unit-testable rows of WP4 (hardening, independent of the
 * token): H4 (the download must come from the configured service host), H7
 * (a download that exceeds the size limit is stopped and cleaned up) and H8
 * (a failed download never touches an existing target file). These call the
 * private {@code download(...)} method directly, with a stubbed Apache
 * HttpClient response, rather than going through the full result-handling
 * business logic.
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
	private File tempDir;

	@Before
	public void setUp() throws IOException {
		MockitoAnnotations.openMocks(this);
		tempDir = Files.createTempDirectory("transcoding-dispatcher-test").toFile();
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

		// OO-9999, WP4 H1: these tests exercise the token gate (WP3), so the feature
		// gate added for videoConversion by H1 must be open, not the thing under test here.
		when(avModuleMock.isVideoRecordingEnabled()).thenReturn(true);
		when(avModuleMock.getVideoConversionMode()).thenReturn(VideoTranscodingMode.service);
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
	public void ignoresAVideoTranscodingResultForARowInErrorStatus() throws Exception {
		// gnaegi, code review OO-9999: a valid token must not resurrect a row that
		// has already failed terminally (ERROR, INEFFICIENT or TIMEOUT).
		String token = "valid-token-for-transcoding";
		Long videoResourceKey = 999L;
		when(transcoderHelperMock.verifyCallbackToken(token, TranscoderJobType.videoTranscoding, UUID_A, REFERENCE_ID_A))
				.thenReturn(true);
		when(videoModuleMock.isTranscodingEnabled()).thenReturn(true);
		when(videoModuleMock.getVideoTranscodingMode()).thenReturn(VideoTranscodingMode.service);

		RepositoryEntry entryMock = Mockito.mock(RepositoryEntry.class);
		OLATResource videoResourceMock = Mockito.mock(OLATResource.class);
		when(repositoryEntryDaoMock.loadByResourceKey(REFERENCE_ID_A)).thenReturn(entryMock);
		when(entryMock.getOlatResource()).thenReturn(videoResourceMock);
		when(videoResourceMock.getKey()).thenReturn(videoResourceKey);

		VideoTranscodingImpl erroredRow = new VideoTranscodingImpl();
		erroredRow.setResolution(720);
		erroredRow.setStatus(VideoTranscoding.TRANSCODING_STATUS_ERROR);
		when(videoTranscodingDaoMock.getVideoTranscodings(videoResourceKey, UUID_A))
				.thenReturn(java.util.List.of(erroredRow));

		String body = "{\"uuid\":\"" + UUID_A + "\",\"type\":\"videoTranscoding\",\"referenceId\":" + REFERENCE_ID_A
				+ ",\"generated\":{\"status\":100,\"url\":\"https://service.example.org/720file.mp4\",\"resolution\":720}}";
		HttpServletRequest request = mockRequest("notifyResult/" + token, body);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		dispatcher.execute(request, response);

		// The token gate let it through (proof: the pending check was reached)...
		verify(videoTranscodingDaoMock).getVideoTranscodings(videoResourceKey, UUID_A);
		// ...but since no row for this uuid is pending, nothing downstream ever runs:
		// no download, no status change, no file write, and no error response either.
		verifyNoInteractions(videoManagerMock, movieServiceMock, textServiceMock, vfsRepositoryServiceMock, httpClientServiceMock);
		verify(response, never()).sendError(org.mockito.ArgumentMatchers.anyInt());
		assertEquals("Row must stay in its terminal ERROR status, not revert to in-progress",
				VideoTranscoding.TRANSCODING_STATUS_ERROR, erroredRow.getStatus());
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

	@Test
	public void rejectsADownloadFromAnUnconfiguredHostH4() throws Exception {
		when(avModuleMock.getVideoConversionServiceUrl()).thenReturn("https://service.example.org/api");
		File targetFile = new File(tempDir, "target.bin");

		assertThrows(IOException.class, () -> invokeDownload(TranscoderJobType.videoConversion,
				"https://evil.example.org/payload.bin", targetFile, 1_000_000L));

		verifyNoInteractions(httpClientServiceMock);
		assertFalse("A rejected download must not create the target file", targetFile.exists());
		assertEquals("No temporary file must be left behind", 0, tempDir.listFiles().length);
	}

	@Test
	public void stopsADownloadThatExceedsTheSizeLimitH7() throws Exception {
		when(avModuleMock.getVideoConversionServiceUrl()).thenReturn("https://service.example.org");
		stubHttpGet(HttpServletResponse.SC_OK, new byte[2000]);
		File targetFile = new File(tempDir, "target.bin");

		assertThrows(IOException.class, () -> invokeDownload(TranscoderJobType.videoConversion,
				"https://service.example.org/payload.bin", targetFile, 1000L));

		assertFalse("An oversized download must not produce a target file", targetFile.exists());
		assertEquals("The partial download must be deleted, not left behind", 0, tempDir.listFiles().length);
	}

	@Test
	public void leavesTheExistingTargetFileUnchangedOnAFailedDownloadH8() throws Exception {
		when(avModuleMock.getVideoConversionServiceUrl()).thenReturn("https://service.example.org");
		File targetFile = new File(tempDir, "target.bin");
		byte[] originalContent = "original content, must survive a failed download".getBytes(StandardCharsets.UTF_8);
		Files.write(targetFile.toPath(), originalContent);
		// Fails mid-stream, after already having delivered some bytes, to prove this is not
		// merely "nothing was written yet" but an in-progress download that gets aborted.
		stubHttpGet(HttpServletResponse.SC_OK, () -> new InputStream() {
			private final byte[] prefix = "partial-data-before-the-connection-drops".getBytes(StandardCharsets.UTF_8);
			private int position = 0;

			@Override
			public int read() throws IOException {
				if (position < prefix.length) {
					return prefix[position++];
				}
				throw new IOException("Simulated network failure mid-download");
			}
		});

		assertThrows(IOException.class, () -> invokeDownload(TranscoderJobType.videoConversion,
				"https://service.example.org/payload.bin", targetFile, 1_000_000L));

		assertArrayEquals("The existing target file must be left exactly as it was", originalContent,
				Files.readAllBytes(targetFile.toPath()));
		File[] filesAfter = tempDir.listFiles();
		assertEquals("No temporary file must be left behind", 1, filesAfter.length);
		assertEquals("target.bin", filesAfter[0].getName());
	}

	/**
	 * Invokes the private {@code download(TranscoderJobType, String, File, long)}, unwrapping the
	 * {@link IOException} that {@link ReflectionTestUtils} would otherwise hide inside an
	 * {@link UndeclaredThrowableException}.
	 */
	private void invokeDownload(TranscoderJobType type, String url, File targetFile, long maxSizeBytes) throws IOException {
		try {
			ReflectionTestUtils.invokeMethod(dispatcher, "download", type, url, targetFile, maxSizeBytes);
		} catch (UndeclaredThrowableException e) {
			if (e.getCause() instanceof IOException ioException) {
				throw ioException;
			}
			throw e;
		}
	}

	private void stubHttpGet(int statusCode, byte[] content) {
		stubHttpGet(statusCode, () -> new ByteArrayInputStream(content));
	}

	private void stubHttpGet(int statusCode, java.util.function.Supplier<InputStream> contentSupplier) {
		CloseableHttpClient httpClientMock = Mockito.mock(CloseableHttpClient.class);
		CloseableHttpResponse httpResponseMock = Mockito.mock(CloseableHttpResponse.class);
		StatusLine statusLineMock = Mockito.mock(StatusLine.class);
		HttpEntity httpEntityMock = Mockito.mock(HttpEntity.class);

		try {
			when(httpClientServiceMock.createHttpClient(any(ProtectionProfile.class))).thenReturn(httpClientMock);
			when(httpClientMock.execute(any(HttpGet.class))).thenReturn(httpResponseMock);
			when(httpResponseMock.getStatusLine()).thenReturn(statusLineMock);
			when(statusLineMock.getStatusCode()).thenReturn(statusCode);
			when(httpResponseMock.getEntity()).thenReturn(httpEntityMock);
			when(httpEntityMock.getContent()).thenAnswer(invocation -> contentSupplier.get());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
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
