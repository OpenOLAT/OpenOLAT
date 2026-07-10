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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * Lightweight on-disk companion that records the AI generator provenance for
 * a QTI item. Lives next to the QTI item XML as {@code ai-source.json} and is
 * read by the question-pool import path to derive the pool's
 * {@code editor / editorVersion} metadata without having to overload the QTI
 * {@code toolName} attribute.
 * <p>
 * For essay items the same information is already present in
 * {@link EssayAiGrading} ({@code generatorSpi}, {@code generatorModel}) so
 * essay items only carry {@code ai-grading.json} — no need to also write this
 * file. For multiple-choice items {@code ai-grading.json} does not apply, so
 * {@code ai-source.json} is the canonical place for the provenance.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiSourceCompanion {

	private String spi;
	private String model;
	private String generatedAt;
	private boolean unsupervisedGenerated;
	

	public AiSourceCompanion() {
		// Jackson
	}

	public AiSourceCompanion(String spi, String model, String generatedAt, boolean unsupervisedGenerated) {
		this.spi = spi;
		this.model = model;
		this.generatedAt = generatedAt;
		this.unsupervisedGenerated = unsupervisedGenerated;
	}

	public String getSpi() {
		return spi;
	}

	public void setSpi(String spi) {
		this.spi = spi;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getGeneratedAt() {
		return generatedAt;
	}

	public void setGeneratedAt(String generatedAt) {
		this.generatedAt = generatedAt;
	}

	public boolean isUnsupervisedGenerated() {
		return unsupervisedGenerated;
	}

	public void setUnsupervisedGenerated(boolean unsupervisedGenerated) {
		this.unsupervisedGenerated = unsupervisedGenerated;
	}
}
