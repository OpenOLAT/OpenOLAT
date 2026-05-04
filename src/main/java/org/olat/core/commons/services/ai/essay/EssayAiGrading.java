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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * Plain Jackson POJO holding the per-item AI-grading metadata used by the
 * formative feedback pipeline. One instance per QTI essay item, persisted
 * on disk as {@code ai-grading.json} next to the QTI item XML by
 * {@link EssayAiGradingFileStore}.
 * <p>
 * The {@code assessmentItemIdentifier} field is denormalised — populated
 * by the file store on {@code load()} from the parent directory name and
 * used only for logging / {@code AiUsageContext.usageContextId}. It is
 * marked {@link JsonIgnore} so it is never written to the JSON file (the
 * directory name is the source of truth).
 * <p>
 * {@link #contentHash} is a SHA-256 prefix of the canonical pipe-joined
 * grading-relevant fields, computed by
 * {@link EssayFormativeFeedbackService#computeContentHash(EssayAiGrading)}
 * and verified before every grading call. A mismatch refuses to grade and
 * surfaces an admin-visible error.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EssayAiGrading {

	/** Current schema version. Bump when the JSON shape changes. */
	public static final int CURRENT_VERSION = 1;

	/** Self-describing envelope fields written to {@code ai-grading.json}. */
	private String kitId;
	private int version = CURRENT_VERSION;
	private String generatedAt;

	/**
	 * Denormalised — populated by {@link EssayAiGradingFileStore#load(java.io.File)}
	 * from the parent directory name. Used only for logging and
	 * {@code AiUsageContext.usageContextId}; not a lookup key. Excluded
	 * from the on-disk JSON by {@link JsonIgnore}.
	 */
	@JsonIgnore
	private String assessmentItemIdentifier;

	private String language;
	private String referenceExcerpt;
	private String modelAnswer;
	private String keyPointsJson;
	private String rubricCriteriaJson;
	private String commonMisconceptionsJson;
	private String bloomLevel;
	private String learningObjective;
	private String gradingHints;
	private String sourceProvenanceJson;
	private Integer difficulty;
	private int tokenEstimate;
	private String contentHash;
	private String generatorSpi;
	private String generatorModel;

	public EssayAiGrading() {
		// Jackson
	}

	public String getKitId() {
		return kitId;
	}

	public void setKitId(String kitId) {
		this.kitId = kitId;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(String generatedAt) {
		this.generatedAt = generatedAt;
	}

	public String getAssessmentItemIdentifier() {
		return assessmentItemIdentifier;
	}

	public void setAssessmentItemIdentifier(String assessmentItemIdentifier) {
		this.assessmentItemIdentifier = assessmentItemIdentifier;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getReferenceExcerpt() {
		return referenceExcerpt;
	}

	public void setReferenceExcerpt(String referenceExcerpt) {
		this.referenceExcerpt = referenceExcerpt;
	}

	public String getModelAnswer() {
		return modelAnswer;
	}

	public void setModelAnswer(String modelAnswer) {
		this.modelAnswer = modelAnswer;
	}

	public String getKeyPointsJson() {
		return keyPointsJson;
	}

	public void setKeyPointsJson(String keyPointsJson) {
		this.keyPointsJson = keyPointsJson;
	}

	public String getRubricCriteriaJson() {
		return rubricCriteriaJson;
	}

	public void setRubricCriteriaJson(String rubricCriteriaJson) {
		this.rubricCriteriaJson = rubricCriteriaJson;
	}

	public String getCommonMisconceptionsJson() {
		return commonMisconceptionsJson;
	}

	public void setCommonMisconceptionsJson(String commonMisconceptionsJson) {
		this.commonMisconceptionsJson = commonMisconceptionsJson;
	}

	public String getBloomLevel() {
		return bloomLevel;
	}

	public void setBloomLevel(String bloomLevel) {
		this.bloomLevel = bloomLevel;
	}

	public String getLearningObjective() {
		return learningObjective;
	}

	public void setLearningObjective(String learningObjective) {
		this.learningObjective = learningObjective;
	}

	public String getGradingHints() {
		return gradingHints;
	}

	public void setGradingHints(String gradingHints) {
		this.gradingHints = gradingHints;
	}

	public String getSourceProvenanceJson() {
		return sourceProvenanceJson;
	}

	public void setSourceProvenanceJson(String sourceProvenanceJson) {
		this.sourceProvenanceJson = sourceProvenanceJson;
	}

	public Integer getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Integer difficulty) {
		this.difficulty = difficulty;
	}

	public int getTokenEstimate() {
		return tokenEstimate;
	}

	public void setTokenEstimate(int tokenEstimate) {
		this.tokenEstimate = tokenEstimate;
	}

	public String getContentHash() {
		return contentHash;
	}

	public void setContentHash(String contentHash) {
		this.contentHash = contentHash;
	}

	public String getGeneratorSpi() {
		return generatorSpi;
	}

	public void setGeneratorSpi(String generatorSpi) {
		this.generatorSpi = generatorSpi;
	}

	public String getGeneratorModel() {
		return generatorModel;
	}

	public void setGeneratorModel(String generatorModel) {
		this.generatorModel = generatorModel;
	}

	/**
	 * Helper accessor for the typed {@code KeyPoint} list. Returns an empty
	 * list if {@link #keyPointsJson} is null or blank.
	 */
	@JsonIgnore
	public List<EssayItemDraft.KeyPoint> getKeyPointsTyped() {
		return EssayAiGradingFileStore.parseKeyPoints(keyPointsJson);
	}

	/** Helper accessor for the typed {@code RubricCriterion} list. */
	@JsonIgnore
	public List<EssayItemDraft.RubricCriterion> getRubricCriteriaTyped() {
		return EssayAiGradingFileStore.parseRubricCriteria(rubricCriteriaJson);
	}

	/**
	 * Helper accessor for the typed misconceptions list. Returns an empty
	 * list when {@link #commonMisconceptionsJson} is null or blank.
	 */
	@JsonIgnore
	public List<String> getCommonMisconceptionsTyped() {
		return EssayAiGradingFileStore.parseMisconceptions(commonMisconceptionsJson);
	}

	/**
	 * Compute the canonical SHA-256 hex digest of the given bytes. Used by
	 * the integrity-marker injection on export and import.
	 */
	public static String sha256Hex(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(bytes);
			StringBuilder hex = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				hex.append(String.format(Locale.ROOT, "%02x", b));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable on this JVM", e);
		}
	}

	/**
	 * Defensive empty-list helper — callers can use this when initialising
	 * a fresh POJO that will later be filled in by an editor.
	 */
	@JsonIgnore
	public static List<EssayItemDraft.KeyPoint> emptyKeyPoints() {
		return new ArrayList<>();
	}
}
