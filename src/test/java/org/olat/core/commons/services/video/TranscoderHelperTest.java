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
package org.olat.core.commons.services.video;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.services.video.model.TranscoderJobType;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Covers OO-9999 WP2 (sign the transcoder callback URLs): the HMAC token is
 * deterministic for a given (secret, type, uuid, referenceId) and changes
 * whenever any one of them changes.
 *
 * Initial date: 2026-11-02<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TranscoderHelperTest {

	private static final TranscoderJobType TYPE_A = TranscoderJobType.videoConversion;
	private static final TranscoderJobType TYPE_B = TranscoderJobType.videoTranscoding;
	private static final String UUID_A = "11111111-1111-1111-1111-111111111111";
	private static final String UUID_B = "22222222-2222-2222-2222-222222222222";
	private static final Long REFERENCE_ID_A = 123456L;
	private static final Long REFERENCE_ID_B = 654321L;
	private static final String SECRET_A = "secret-A-01234567890123456789012345";
	private static final String SECRET_B = "secret-B-98765432109876543210987654";

	private TranscoderHelper transcoderHelper;

	@Before
	public void setUp() {
		transcoderHelper = spy(new TranscoderHelper());
		doReturn(SECRET_A).when(transcoderHelper).getCallbackSecret();
	}

	private String createToken(TranscoderJobType type, String uuid, Long referenceId) {
		return ReflectionTestUtils.invokeMethod(transcoderHelper, "createCallbackToken", type, uuid, referenceId);
	}

	@Test
	public void isDeterministicForTheSameJobData() {
		String token1 = createToken(TYPE_A, UUID_A, REFERENCE_ID_A);
		String token2 = createToken(TYPE_A, UUID_A, REFERENCE_ID_A);

		assertEquals(token1, token2);
	}

	@Test
	public void verifiesTheTokenItSignedForTheSameJobData() {
		String token = createToken(TYPE_A, UUID_A, REFERENCE_ID_A);

		assertTrue(transcoderHelper.verifyCallbackToken(token, TYPE_A, UUID_A, REFERENCE_ID_A));
	}

	@Test
	public void rejectsTheTokenWhenTheUuidDiffers() {
		String token = createToken(TYPE_A, UUID_A, REFERENCE_ID_A);

		assertFalse(transcoderHelper.verifyCallbackToken(token, TYPE_A, UUID_B, REFERENCE_ID_A));
	}

	@Test
	public void rejectsTheTokenWhenTheTypeDiffers() {
		String token = createToken(TYPE_A, UUID_A, REFERENCE_ID_A);

		assertFalse(transcoderHelper.verifyCallbackToken(token, TYPE_B, UUID_A, REFERENCE_ID_A));
	}

	@Test
	public void rejectsTheTokenWhenTheReferenceIdDiffers() {
		String token = createToken(TYPE_A, UUID_A, REFERENCE_ID_A);

		assertFalse(transcoderHelper.verifyCallbackToken(token, TYPE_A, UUID_A, REFERENCE_ID_B));
	}

	@Test
	public void rejectsTheTokenWhenTheSecretDiffers() {
		String token = createToken(TYPE_A, UUID_A, REFERENCE_ID_A);
		doReturn(SECRET_B).when(transcoderHelper).getCallbackSecret();

		assertFalse(transcoderHelper.verifyCallbackToken(token, TYPE_A, UUID_A, REFERENCE_ID_A));
	}

	@Test
	public void rejectsAMissingOrMalformedToken() {
		assertFalse(transcoderHelper.verifyCallbackToken(null, TYPE_A, UUID_A, REFERENCE_ID_A));
		assertFalse(transcoderHelper.verifyCallbackToken("", TYPE_A, UUID_A, REFERENCE_ID_A));
		assertFalse(transcoderHelper.verifyCallbackToken("   ", TYPE_A, UUID_A, REFERENCE_ID_A));
		assertFalse(transcoderHelper.verifyCallbackToken("not valid base64url!!", TYPE_A, UUID_A, REFERENCE_ID_A));
	}

	@Test
	public void rejectsWhenAClaimFromTheBodyIsMissing() {
		String token = createToken(TYPE_A, UUID_A, REFERENCE_ID_A);

		assertFalse(transcoderHelper.verifyCallbackToken(token, null, UUID_A, REFERENCE_ID_A));
		assertFalse(transcoderHelper.verifyCallbackToken(token, TYPE_A, null, REFERENCE_ID_A));
		assertFalse(transcoderHelper.verifyCallbackToken(token, TYPE_A, UUID_A, null));
	}

	@Test
	public void tokenIsSafeAsAUrlPathSegment() {
		String token = createToken(TYPE_A, UUID_A, REFERENCE_ID_A);

		assertFalse(token.contains("+"));
		assertFalse(token.contains("/"));
		assertFalse(token.contains("="));
	}
}
