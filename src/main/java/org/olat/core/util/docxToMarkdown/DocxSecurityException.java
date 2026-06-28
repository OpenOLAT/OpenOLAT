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
package org.olat.core.util.docxToMarkdown;

import java.io.IOException;

/**
 * Signals that a DOCX archive was rejected for a security reason during
 * extraction. Carries a {@link Reason} so the service layer can map the
 * failure to the matching i18n message key instead of a generic read error.
 * <p>
 * Extends {@link IOException} so callers that only catch {@code IOException}
 * still treat it as a read failure, while security-aware callers can branch
 * on {@link #getReason()}.
 *
 * @author frentix GmbH, https://www.frentix.com
 */
class DocxSecurityException extends IOException {

	private static final long serialVersionUID = 1L;

	/**
	 * The concrete security violation that caused the rejection. Each reason
	 * maps to a dedicated i18n key in the service layer.
	 */
	enum Reason {
		/** A VBA macro project (vbaProject.bin) was found. */
		MACRO_DETECTED,
		/** A ZIP entry name attempted path traversal (zip slip). */
		ZIP_SLIP,
		/** The archive exceeded the entry-count / size limits (zip bomb). */
		ZIP_BOMB,
		/** The document is encrypted / password-protected. */
		ENCRYPTED
	}

	private final transient Reason reason;

	DocxSecurityException(Reason reason, String message) {
		super(message);
		this.reason = reason;
	}

	Reason getReason() {
		return reason;
	}
}
