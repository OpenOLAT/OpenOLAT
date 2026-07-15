/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.matching;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.gui.control.Event;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Configuration module for the taxonomy embedding-based matching feature.
 * Enabled/disabled in the AI Module admin UI.
 *
 * Initial date: 2026-06-05<br>
 * @author uhensler, https://www.frentix.com
 */
@Service
public class TaxonomyMatchingModule extends AbstractSpringModule {

	private static final String PROP_ENABLED = "taxonomy.matching.enabled";
	private static final String PROP_PGVECTOR_ENABLED = "taxonomy.matching.pgvector.enabled";
	private static final String PROP_SPI = "taxonomy.matching.spi";
	private static final String PROP_MODEL = "taxonomy.matching.model";
	private static final String PROP_MIN_SCORE = "taxonomy.matching.min.score";
	private static final String PROP_LOCAL_MODEL_DIR = "taxonomy.matching.local.model.dir";
	private static final String PROP_QUERY_PREFIX = "taxonomy.matching.query.prefix";
	private static final String PROP_PASSAGE_PREFIX = "taxonomy.matching.passage.prefix";
	private static final String PROP_VECTOR_KEY = "taxonomy.matching.vector.key";
	private static final String PROP_VECTOR_DIM = "taxonomy.matching.vector.dim";

	private static final double DEFAULT_MIN_SCORE = 0.65;

	@Value("${taxonomy.matching.enabled:false}")
	private boolean enabled;
	@Value("${taxonomy.matching.pgvector.enabled:true}")
	private boolean pgVectorEnabled;
	@Value("${taxonomy.matching.spi:}")
	private String spiId;
	@Value("${taxonomy.matching.model:}")
	private String model;
	@Value("${taxonomy.matching.min.score:0.65}")
	private double minScore;
	@Value("${taxonomy.matching.local.model.dir:}")
	private String localModelDir;
	@Value("${taxonomy.matching.query.prefix:}")
	private String queryPrefixOverride;
	@Value("${taxonomy.matching.passage.prefix:}")
	private String passagePrefixOverride;

	private volatile boolean pgVectorActive;

	private String vectorKey;
	private int vectorDim;

	public TaxonomyMatchingModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.registerFor(this, null, OresHelper.createOLATResourceableType("TaxonomyMatching"));
	}

	@Override
	public void event(Event event) {
		if (event instanceof TaxonomyMatchingReindexEvent) {
			setPgVectorActive(true);
		}
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	private void updateProperties() {
		String enabledStr = getStringPropertyValue(PROP_ENABLED, Boolean.toString(enabled));
		enabled = "true".equalsIgnoreCase(enabledStr);

		String pgVectorEnabledStr = getStringPropertyValue(PROP_PGVECTOR_ENABLED, Boolean.toString(pgVectorEnabled));
		pgVectorEnabled = "true".equalsIgnoreCase(pgVectorEnabledStr);

		spiId = getStringPropertyValue(PROP_SPI, spiId);
		model = getStringPropertyValue(PROP_MODEL, model);

		String minScoreStr = getStringPropertyValue(PROP_MIN_SCORE, Double.toString(minScore));
		try {
			minScore = Double.parseDouble(minScoreStr);
		} catch (NumberFormatException e) {
			minScore = DEFAULT_MIN_SCORE;
		}

		localModelDir = getStringPropertyValue(PROP_LOCAL_MODEL_DIR, localModelDir);
		queryPrefixOverride = getStringPropertyValue(PROP_QUERY_PREFIX, queryPrefixOverride);
		passagePrefixOverride = getStringPropertyValue(PROP_PASSAGE_PREFIX, passagePrefixOverride);

		vectorKey = getStringPropertyValue(PROP_VECTOR_KEY, vectorKey);
		String vectorDimStr = getStringPropertyValue(PROP_VECTOR_DIM, null);
		if (vectorDimStr != null) {
			try {
				vectorDim = Integer.parseInt(vectorDimStr);
			} catch (NumberFormatException e) {
				vectorDim = 0;
			}
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(PROP_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isPgVectorEnabled() {
		return pgVectorEnabled;
	}

	public String getSpiId() {
		return spiId;
	}

	public void setSpiId(String spiId) {
		this.spiId = spiId;
		pgVectorActive = false;
		setStringProperty(PROP_SPI, StringHelper.containsNonWhitespace(spiId) ? spiId : "", true);
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
		pgVectorActive = false;
		setStringProperty(PROP_MODEL, StringHelper.containsNonWhitespace(model) ? model : "", true);
	}

	public double getMinScore() {
		return minScore;
	}

	public void setMinScore(double minScore) {
		this.minScore = minScore;
		setStringProperty(PROP_MIN_SCORE, Double.toString(minScore), true);
	}

	public String getLocalModelDir() {
		return localModelDir;
	}

	public void setLocalModelDir(String localModelDir) {
		this.localModelDir = localModelDir;
		setStringProperty(PROP_LOCAL_MODEL_DIR, StringHelper.containsNonWhitespace(localModelDir) ? localModelDir : "", true);
	}

	public String getQueryPrefix() {
		return resolvePrefix(queryPrefixOverride, true);
	}

	public String getPassagePrefix() {
		return resolvePrefix(passagePrefixOverride, false);
	}

	private String resolvePrefix(String override, boolean query) {
		if (StringHelper.containsNonWhitespace(override)) {
			return override;
		}
		if (!StringHelper.containsNonWhitespace(model)) {
			return "";
		}
		String lower = model.toLowerCase();
		if (lower.contains("e5")) {
			return query ? "query: " : "passage: ";
		}
		if (lower.contains("nomic")) {
			return query ? "search_query: " : "search_document: ";
		}
		if (lower.contains("qwen")) {
			// Qwen3-Embedding: instruction on query side only; passages are embedded as plain text
			// Has a vary big impact on the score!
			return query ? "Instruct: Given a topic, retrieve the most relevant taxonomy level\nQuery: " : "";
		}
		// bge-m3 and OpenAI models do not use any prefix
		return "";
	}

	public boolean isPgVectorActive() {
		return pgVectorActive;
	}

	public void setPgVectorActive(boolean pgVectorActive) {
		this.pgVectorActive = pgVectorActive;
	}

	public String getVectorKey() {
		return vectorKey;
	}

	public int getVectorDim() {
		return vectorDim;
	}

	public void setVectorKeyAndDim(String key, int dim) {
		this.vectorKey = key;
		this.vectorDim = dim;
		setStringProperty(PROP_VECTOR_KEY, key != null ? key : "", true);
		setStringProperty(PROP_VECTOR_DIM, Integer.toString(dim), true);
	}

	public void clearVectorKeyAndDim() {
		this.vectorKey = null;
		this.vectorDim = 0;
		setStringProperty(PROP_VECTOR_KEY, "", true);
		setStringProperty(PROP_VECTOR_DIM, "", true);
	}
}
