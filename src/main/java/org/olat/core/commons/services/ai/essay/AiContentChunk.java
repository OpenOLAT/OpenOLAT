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

import java.util.List;

/**
 *
 * Single heading + token-bounded slice of source material produced by the
 * content chunker. The full chunker implementation lives in the content
 * pipeline (Phase B) in package
 * {@code org.olat.core.commons.services.ai.content}; this record is placed
 * alongside the essay SPI so the SPI contract can reference it without a
 * cross-package import cycle.
 * <p>
 * Fields:
 * <ul>
 *   <li>{@code id} — stable chunk identifier (used in provenance).</li>
 *   <li>{@code text} — the chunk content (Markdown).</li>
 *   <li>{@code headingPath} — heading trail from the document root down
 *       to the chunk, top-most first.</li>
 *   <li>{@code tokenEstimate} — rough token count used by the prompt
 *       assembler for budget checks.</li>
 *   <li>{@code oversize} — true if the chunk exceeds the tier budget and
 *       should be either re-chunked or truncated.</li>
 * </ul>
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public record AiContentChunk(
		String id,
		String text,
		List<String> headingPath,
		int tokenEstimate,
		boolean oversize) {

}
