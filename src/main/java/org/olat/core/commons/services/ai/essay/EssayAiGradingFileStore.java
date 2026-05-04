/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.ai.essay;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *
 * On-disk store for the {@link EssayAiGrading} POJO. The file is named
 * {@code ai-grading.json} and lives in the same directory as the QTI essay
 * item XML it grades. The directory name is the {@code assessmentItemIdentifier}
 * — the field is denormalised onto the POJO at {@link #load(File)} time
 * but never written to JSON (see {@code @JsonIgnore} on the POJO).
 * <p>
 * Stable canonical serialisation:
 * <ul>
 *   <li>map keys are sorted (ORDER_MAP_ENTRIES_BY_KEYS),</li>
 *   <li>dates as ISO-8601 strings, never timestamps,</li>
 *   <li>unknown JSON properties are tolerated on read (forward compat).</li>
 * </ul>
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class EssayAiGradingFileStore {

	private static final Logger log = Tracing.createLoggerFor(EssayAiGradingFileStore.class);

	/** Canonical filename written next to the QTI essay item XML. */
	public static final String FILENAME = "ai-grading.json";

	private static final ObjectMapper MAPPER = createMapper();

	/**
	 * Load the grading POJO from the {@code ai-grading.json} file inside
	 * the given question directory. The {@code assessmentItemIdentifier} is
	 * populated from the directory name. Returns {@code null} when the file
	 * does not exist (manual essay item, no AI grading metadata yet).
	 *
	 * @param questionDir the parent directory of the QTI item XML; must not
	 *                    be {@code null}
	 * @return the loaded grading or {@code null} when no file is present
	 */
	public EssayAiGrading load(File questionDir) {
		if (questionDir == null) {
			return null;
		}
		File file = new File(questionDir, FILENAME);
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		try {
			byte[] bytes = Files.readAllBytes(file.toPath());
			EssayAiGrading grading = MAPPER.readValue(bytes, EssayAiGrading.class);
			if (grading != null) {
				grading.setAssessmentItemIdentifier(questionDir.getName());
			}
			return grading;
		} catch (IOException e) {
			log.warn("Failed to read {} from {}: {}", FILENAME, questionDir, e.getMessage());
			return null;
		}
	}

	/**
	 * Persist the grading POJO to {@code ai-grading.json} inside the given
	 * question directory. Ensures stable {@code kitId}, {@code generatedAt}
	 * and {@code version} envelope fields are populated. The
	 * {@code assessmentItemIdentifier} field is intentionally not written
	 * to disk.
	 *
	 * @return the canonical bytes that were written, suitable for SHA-256
	 *         hashing by the marker-injection pipeline; never {@code null}
	 *         unless the write failed (then a warning is logged and an
	 *         empty array is returned)
	 */
	public byte[] save(File questionDir, EssayAiGrading grading) {
		if (questionDir == null || grading == null) {
			return new byte[0];
		}
		if (!questionDir.exists() && !questionDir.mkdirs()) {
			log.warn("Cannot create question directory {}", questionDir);
			return new byte[0];
		}
		// Stamp envelope fields if missing.
		if (grading.getKitId() == null || grading.getKitId().isBlank()) {
			grading.setKitId(UUID.randomUUID().toString());
		}
		if (grading.getGeneratedAt() == null || grading.getGeneratedAt().isBlank()) {
			grading.setGeneratedAt(Instant.now().toString());
		}
		if (grading.getVersion() <= 0) {
			grading.setVersion(EssayAiGrading.CURRENT_VERSION);
		}
		try {
			byte[] bytes = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(grading);
			File file = new File(questionDir, FILENAME);
			Files.write(file.toPath(), bytes);
			return bytes;
		} catch (IOException e) {
			log.warn("Failed to write {} to {}: {}", FILENAME, questionDir, e.getMessage());
			return new byte[0];
		}
	}

	/** Whether the store has a persisted file for the given question dir. */
	public boolean exists(File questionDir) {
		return questionDir != null && new File(questionDir, FILENAME).isFile();
	}

	/**
	 * Direct file accessor used by callers that need to byte-copy or hash
	 * the on-disk file (e.g. QTI export marker injection).
	 */
	public File fileFor(File questionDir) {
		return questionDir == null ? null : new File(questionDir, FILENAME);
	}

	/** Read raw canonical bytes for hashing (export marker injection). */
	public byte[] readBytes(File questionDir) throws IOException {
		File file = fileFor(questionDir);
		if (file == null || !file.exists()) {
			return new byte[0];
		}
		return Files.readAllBytes(file.toPath());
	}

	/** Serialise the in-memory POJO to the canonical JSON bytes used by the file store. */
	public byte[] toCanonicalJsonBytes(EssayAiGrading grading) {
		if (grading == null) {
			return new byte[0];
		}
		try {
			return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsBytes(grading);
		} catch (IOException e) {
			log.warn("Failed to serialise EssayAiGrading: {}", e.getMessage());
			return new byte[0];
		}
	}

	/** Parse JSON bytes into an {@link EssayAiGrading} POJO; never throws. */
	public EssayAiGrading fromCanonicalJsonBytes(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		try {
			return MAPPER.readValue(bytes, EssayAiGrading.class);
		} catch (IOException e) {
			log.warn("Failed to parse ai-grading bytes: {}", e.getMessage());
			return null;
		}
	}

	// ------------------------------------------------------------------ Static helpers

	static List<EssayItemDraft.KeyPoint> parseKeyPoints(String json) {
		return parseList(json, new TypeReference<List<EssayItemDraft.KeyPoint>>() {});
	}

	static List<EssayItemDraft.RubricCriterion> parseRubricCriteria(String json) {
		return parseList(json, new TypeReference<List<EssayItemDraft.RubricCriterion>>() {});
	}

	static List<String> parseMisconceptions(String json) {
		return parseList(json, new TypeReference<List<String>>() {});
	}

	private static <T> List<T> parseList(String json, TypeReference<List<T>> typeRef) {
		if (json == null || json.isBlank()) {
			return new ArrayList<>();
		}
		try {
			List<T> list = MAPPER.readValue(json, typeRef);
			return list == null ? new ArrayList<>() : list;
		} catch (IOException e) {
			return new ArrayList<>();
		}
	}

	private static ObjectMapper createMapper() {
		ObjectMapper m = new ObjectMapper();
		m.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
		m.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return m;
	}
}
