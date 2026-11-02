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
package org.olat.modules.video;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Covers OO-9999 WP1 (the secret that signs the transcoder callback URLs):
 * it is generated once and reused afterwards, instead of changing on every
 * reload.
 * <p>
 * These tests call the private {@code initCallbackSecret()} directly rather
 * than the full {@code init()} / {@code initFromChangedProperties()}, which
 * also registers the video repository handler and the video site in
 * process-wide static registries that must not be touched repeatedly from a
 * unit test.
 *
 * Initial date: 2026-11-02<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VideoModuleTest {

	@Mock
	private CoordinatorManager coordinatorManagerMock;
	@Mock
	private Coordinator coordinatorMock;
	@Mock
	private EventBus eventBusMock;

	@Before
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		when(coordinatorManagerMock.getCoordinator()).thenReturn(coordinatorMock);
		when(coordinatorMock.getEventBus()).thenReturn(eventBusMock);
	}

	/**
	 * A VideoModule whose persisted properties read from / write to
	 * {@code userDataDir}, with only the property store initialized (not the
	 * rest of {@code init()}).
	 */
	private VideoModule newModule(File userDataDir) {
		VideoModule videoModule = new VideoModule(coordinatorManagerMock);
		Object persistedProperties = ReflectionTestUtils.getField(videoModule, "moduleConfigProperties");
		ReflectionTestUtils.invokeMethod(persistedProperties, "setUserDataDirectory", userDataDir.getAbsolutePath());
		ReflectionTestUtils.invokeMethod(persistedProperties, "init");
		return videoModule;
	}

	@Test
	public void generatesACallbackSecretOnFirstUse() throws IOException {
		VideoModule videoModule = newModule(Files.createTempDirectory("video-module-test").toFile());

		ReflectionTestUtils.invokeMethod(videoModule, "initCallbackSecret");

		String secret = videoModule.getCallbackSecret();
		assertNotNull(secret);
		// 32 random bytes, Base64url without padding -> 43 characters, no '+', '/' or '='
		assertEquals(43, secret.length());
		assertFalse(secret.contains("+"));
		assertFalse(secret.contains("/"));
		assertFalse(secret.contains("="));
	}

	@Test
	public void keepsTheSameSecretWhenCalledAgainOnTheSameInstance() throws IOException {
		VideoModule videoModule = newModule(Files.createTempDirectory("video-module-test").toFile());

		ReflectionTestUtils.invokeMethod(videoModule, "initCallbackSecret");
		String firstSecret = videoModule.getCallbackSecret();

		// What initFromChangedProperties() does on a PersistedPropertiesChangedEvent:
		// call initCallbackSecret() again after the properties were reloaded.
		ReflectionTestUtils.invokeMethod(videoModule, "initCallbackSecret");

		assertEquals(firstSecret, videoModule.getCallbackSecret());
	}

	@Test
	public void keepsTheSameSecretAcrossARestart() throws IOException {
		File userDataDir = Files.createTempDirectory("video-module-test").toFile();
		VideoModule firstStart = newModule(userDataDir);
		ReflectionTestUtils.invokeMethod(firstStart, "initCallbackSecret");
		String secretAfterFirstStart = firstStart.getCallbackSecret();

		// A second instance reading the same persisted property file, as after an
		// OpenOlat restart, or on another cluster node once it reloads the file.
		VideoModule secondStart = newModule(userDataDir);
		ReflectionTestUtils.invokeMethod(secondStart, "initCallbackSecret");

		assertEquals(secretAfterFirstStart, secondStart.getCallbackSecret());
	}
}
