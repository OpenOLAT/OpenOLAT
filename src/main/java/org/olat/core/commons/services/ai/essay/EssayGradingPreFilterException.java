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

/**
 *
 * Wraps a pre-filter {@link RejectionReason} into an exception for
 * rare call paths that need to short-circuit via throw rather than
 * return. The normal grading flow returns a {@code REJECTED}
 * {@link FormativeFeedback} instead.
 *
 * Initial date: 2026-04-20<br>
 *
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class EssayGradingPreFilterException extends AiEssayGradingException {

	private static final long serialVersionUID = 1L;

	private final RejectionReason reason;

	public EssayGradingPreFilterException(RejectionReason reason) {
		super(reason == null ? "pre-filter rejection" : reason.messageKey());
		this.reason = reason;
	}

	public RejectionReason getReason() {
		return reason;
	}
}
