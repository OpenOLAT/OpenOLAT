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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * Initial date: 2022-11-07<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class VideoSubtitlesHelper {

	private static final Logger log = Tracing.createLoggerFor(VideoSubtitlesHelper.class);

	private static final String VTT_MARKER = "WEBVTT";

	private static final String timeCodeRegex = "^(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3}) --> (\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})$";
	private static final Pattern timeCodePattern = Pattern.compile(timeCodeRegex);

	public static boolean isVtt(VFSLeaf vfsLeaf) {
		if (vfsLeaf == null) {
			return false;
		}

		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(vfsLeaf.getInputStream()))) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				if (StringHelper.containsNonWhitespace(line)) {
					return line.equals(VTT_MARKER);
				}
			}
		} catch (Exception e) {
			log.error("Unable to load VFSLeaf  " + vfsLeaf.getName(), e);
		}

		return false;
	}

	enum SrtState {
		waitingForSequenceNumber,
		waitingForTimeCode,
		aggregatingCaptionText
	}

	public static void convertSrtToVtt(InputStream inputStream, OutputStream outputStream) throws Exception {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			int sequenceNumber;
			int checkSequenceNumber = 1;
			Pair<Long, Long> timeCode;

			SrtState state = SrtState.waitingForSequenceNumber;
			outputStream.write(VTT_MARKER.getBytes(StandardCharsets.UTF_8));
			outputStream.write('\n');
			outputStream.write('\n');

			while ((line = bufferedReader.readLine()) != null) {
				switch (state) {
					case waitingForSequenceNumber:
						if (StringHelper.containsNonWhitespace(line)) {
							sequenceNumber = Integer.parseInt(line);
							if (sequenceNumber != checkSequenceNumber) {
								throw new IllegalStateException("Sequence number error in SRT file");
							}
							checkSequenceNumber++;
							state = SrtState.waitingForTimeCode;
						}
						break;
					case waitingForTimeCode:
						if (!StringHelper.containsNonWhitespace(line)) {
							log.error("Time code not found");
							throw new IllegalStateException("Time code line empty");
						}
						state = SrtState.aggregatingCaptionText;
						timeCode = parseSrtTimeCode(line);
						String vttTimeCode = createVttTimeCode(timeCode);
						outputStream.write(vttTimeCode.getBytes(StandardCharsets.UTF_8));
						outputStream.write('\n');
						break;
					case aggregatingCaptionText:
						if (StringHelper.containsNonWhitespace(line)) {
							outputStream.write(line.getBytes(StandardCharsets.UTF_8));
						} else {
							state = SrtState.waitingForSequenceNumber;
						}
						outputStream.write('\n');
				}
			}
		} catch (IOException e) {
			log.error("Exception while reading SRT input stream", e);
			throw e;
		}
	}

	public static void convertSrtToVtt(VFSLeaf vfsLeaf, VFSContainer targetContainer, String fileName) throws Exception {
		if (vfsLeaf == null) {
			throw new IOException("Input missing");
		}

		VFSLeaf vttVfsLeaf = targetContainer.createChildLeaf(fileName);
		convertSrtToVtt(vfsLeaf.getInputStream(), vttVfsLeaf.getOutputStream(false));
	}

	public static Pair<Long, Long> parseSrtTimeCode(String timeCodeString) {
		if (!StringHelper.containsNonWhitespace(timeCodeString)) {
			throw new IllegalArgumentException("timeCodeString is empty or null");
		}
		Matcher matcher = timeCodePattern.matcher(timeCodeString);
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Time code format not recognized: " + timeCodeString);
		}

		if (matcher.groupCount() != 8) {
			throw new IllegalArgumentException("Time code format does not match requirements: " + timeCodeString);
		}

		try {
			int fromHours = Integer.parseInt(matcher.group(1));
			int fromMinutes = Integer.parseInt(matcher.group(2));
			int fromSeconds = Integer.parseInt(matcher.group(3));
			int fromMilliseconds = Integer.parseInt(matcher.group(4));
			int toHours = Integer.parseInt(matcher.group(5));
			int toMinutes = Integer.parseInt(matcher.group(6));
			int toSeconds = Integer.parseInt(matcher.group(7));
			int toMilliseconds = Integer.parseInt(matcher.group(8));

			long from = fromMilliseconds + 1000L * (fromSeconds + 60L * (fromMinutes + 60L * fromHours));
			long to = toMilliseconds + 1000L * (toSeconds + 60L * (toMinutes + 60L * toHours));

			return new ImmutablePair<>(from, to);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Time code cannot be parsed: " + timeCodeString);
		}
	}

	public static String createVttTimeCode(Pair<Long, Long> fromToTimes) {
		StringBuilder sb = new StringBuilder();

		long from = fromToTimes.getLeft();
		long fromMilliseconds = from % 1000;
		from /= 1000;
		long fromSeconds = from % 60;
		from /= 60;
		long fromMinutes = from % 60;
		from /= 60;
		long fromHours = from;

		long to = fromToTimes.getRight();
		long toMilliseconds = to % 1000;
		to /= 1000;
		long toSeconds = to % 60;
		to /= 60;
		long toMinutes = to % 60;
		to /= 60;
		long toHours = to;

		sb.append(String.format("%02d:%02d:%02d.%03d", fromHours, fromMinutes, fromSeconds, fromMilliseconds));
		sb.append(" --> ");
		sb.append(String.format("%02d:%02d:%02d.%03d", toHours, toMinutes, toSeconds, toMilliseconds));

		return sb.toString();
	}
}
