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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.Tracing;
import org.olat.test.OlatTestCase;

/**
 * Initial date: 2022-11-07<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VideoSubtitlesHelperTest extends OlatTestCase {

	private static final Logger log = Tracing.createLoggerFor(VideoSubtitlesHelperTest.class);

	@Test
	public void srtTimeCodeTest() {
		Pair<Long, Long> allZeros = VideoSubtitlesHelper.parseSrtTimeCode("00:00:00,000 --> 00:00:00,000");
		Assert.assertEquals(new ImmutablePair<>(0L, 0L), allZeros);

		Pair<Long, Long> minuteAfterHour = VideoSubtitlesHelper.parseSrtTimeCode("01:00:00,000 --> 01:01:00,000");
		Assert.assertEquals(new ImmutablePair<>(3600000L, 3660000L), minuteAfterHour);

		Pair<Long, Long> arbitratyTimeCode = VideoSubtitlesHelper.parseSrtTimeCode("01:23:45,678 --> 01:34:56,789");
		Assert.assertEquals(new ImmutablePair<>(5025678L, 5696789L), arbitratyTimeCode);
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullSrtTimeCodeTest() {
		VideoSubtitlesHelper.parseSrtTimeCode(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void badFormatSrtTimeCodeTest() {
		VideoSubtitlesHelper.parseSrtTimeCode("01:00:00 --> 01:01:01");
	}

	@Test(expected = IllegalArgumentException.class)
	public void nonParsableNumbersSrtTimeCodeTest() {
		VideoSubtitlesHelper.parseSrtTimeCode("01:00:00,abc --> 01:01:01,000");
	}

	@Test
	public void createVttTimeCodeTest() {
		Pair<Long, Long> arbitraryTimePair = new ImmutablePair<>(5025678L, 5696789L);
		String vttTimeCode = VideoSubtitlesHelper.createVttTimeCode(arbitraryTimePair);
		Assert.assertEquals("01:23:45.678 --> 01:34:56.789", vttTimeCode);
	}

	@Test
	public void simpleConvertSrtToVttTest() throws Exception {
		String srtContents = "1\n" +
				"00:00:10,260 --> 00:00:15,980\n" +
				"First line in SRT format \uD83D\uDE00.\n" +
				"Second line in SRT format \u2665.\n" +
				"\n" +
				"2\n" +
				"00:00:16,380 --> 00:00:20,779\n" +
				"A second subtitle\n";

		ByteArrayInputStream bais = new ByteArrayInputStream(srtContents.getBytes(StandardCharsets.UTF_8));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		VideoSubtitlesHelper.convertSrtToVtt(bais, baos);
		String vttContents = baos.toString(StandardCharsets.UTF_8);
		String expectedVttContents = "WEBVTT\n" +
				"\n" +
				"00:00:10.260 --> 00:00:15.980\n" +
				"First line in SRT format \uD83D\uDE00.\n" +
				"Second line in SRT format \u2665.\n" +
				"\n" +
				"00:00:16.380 --> 00:00:20.779\n" +
				"A second subtitle\n";

		Assert.assertEquals(expectedVttContents, vttContents);
	}

	@Test
	public void isVttTest() {
		String simpleVttContents = "WEBVTT\n" +
				"\n" +
				"00:00:00.000 --> 00:00:04.000\n" +
				"Simple VTT";

		String notVtt = "1\n" +
				"00:00:10,260 --> 00:00:15,980\n" +
				"Subtitle text\n";

		String vttWithTitle = "WEBVTT - Spanish language subtitle track\n" +
				"\n" +
				"00:00:00.000 --> 00:00:04.000\n" +
				"VTT simple";

		Assert.assertTrue(VideoSubtitlesHelper.isVtt(new ByteArrayInputStream(simpleVttContents.getBytes(StandardCharsets.UTF_8))));
		Assert.assertFalse(VideoSubtitlesHelper.isVtt(new ByteArrayInputStream(notVtt.getBytes(StandardCharsets.UTF_8))));
		Assert.assertTrue(VideoSubtitlesHelper.isVtt(new ByteArrayInputStream(vttWithTitle.getBytes(StandardCharsets.UTF_8))));
	}
}