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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.onyx.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Validates the checksum in Onyx test summary HTML files.
 *
 */
//<OLATCE-1399>
public class SummaryCheckSumValidator {
	//	private static final Logger log = LoggerFactory.getLogger(SummaryCheckSumValidator.class);

	private static final String STRONG = "<strong>";

	/**
	 * used only for stand-alone checks see
	 * /src/main/resources/summaryCheckSumValidator/READ.ME
	 */
	public static void main(final String[] args) {
		if (args.length != 1) {
			System.err.println("Missing file-parameter!\nPlease call this program like \"java -jar SummaryCheckSumValidator summary.html\"");
			System.exit(1);
		}

		final String filename = args[0];
		final SummaryChecksumValidatorResult result = validate(filename);
		final boolean fileIsValid = result.validated;
		if (fileIsValid) {
			System.out.println("File could be validated!");
			System.out.println("Result  : " + result.result);
		} else {
			System.err.println("File could not be validated!");
			System.err.println("Reason: " + result.result);
			System.err.println("Info  : " + result.info);
		}
	}

	/**
	 * Validates the Onyx test result summary pages checksum.
	 * 
	 * @param file
	 *            The file containing the summary HTML
	 * @return SummaryChecksumValidatorResult structure. The contained validated
	 *         field contains the validation result.
	 * 
	 * @see SummaryChecksumValidatorResult
	 */
	public static SummaryChecksumValidatorResult validate(final File file) {
		FileInputStream fis = null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream((int) file.length());
		try {
			fis = new FileInputStream(file);
			final byte[] buf = new byte[102400];
			int read = 0;
			while ((read = fis.read(buf)) >= 0) {
				baos.write(buf, 0, read);
			}
		} catch (final IOException e) {
			//			log.error("Error reading file to validate: " + file.getAbsolutePath(), e);
			final SummaryChecksumValidatorResult result = new SummaryChecksumValidatorResult();
			result.result = "onyx.summary.validation.result.error.reading.file";
			result.info = file.getAbsolutePath() + " - " + e.getMessage();
			return result;

		} finally {
			try {
				if (fis != null) {
					fis.close();
					fis = null;
				}
			} catch (final Exception e) {
				// ignore
			}
		}

		try {
			baos.flush();
		} catch (final IOException e) {
			// ignore
		}

		final String html = baos.toString();

		try {
			baos.close();
		} catch (final IOException e) {
			// ignore
		}

		return internalValidate(html);
	}

	/**
	 * Validates the Onyx test result summary pages checksum.
	 * 
	 * @param filename
	 *            The filename of the file containing the summary HTML
	 * @return SummaryChecksumValidatorResult structure. The contained validated
	 *         field contains the validation result.
	 * 
	 * @see SummaryChecksumValidatorResult
	 */
	public static SummaryChecksumValidatorResult validate(final String filename) {
		final File file = new File(filename);

		if (!file.exists()) {
			final SummaryChecksumValidatorResult result = new SummaryChecksumValidatorResult();
			result.result = "onyx.summary.validation.result.file.not.found";
			result.info = filename;
			return result;
		}

		return validate(file);
	}

	private static SummaryChecksumValidatorResult internalValidate(final String html) {
		final SummaryChecksumValidatorResult result = new SummaryChecksumValidatorResult();

		// determine hashed HTML content
		final int start = html.indexOf("		<div class=\"test\">");
		if (start > 0) {
			final int end = html.indexOf("		<div class=\"hash\">", start);
			if (end > 0) {
				final String toHash = html.substring(start, end);
				//				if (log.isDebugEnabled()) {
				//					log.debug("HTML to validate: " + toHash);
				//				}

				// determine hash
				int startHash = html.indexOf(STRONG, end);
				if (startHash > 0) {
					startHash += STRONG.length();
					final int endHash = html.indexOf("</strong>", startHash);
					if (endHash > 0) {
						final String hash = html.substring(startHash, endHash);

						// compare
						final String toCompare = DigestUtils.md5Hex(toHash);
						final boolean equal = hash.equals(toCompare);

						//						if (log.isDebugEnabled()) {
						//							log.debug("Hash to validate: " + hash);
						//							log.debug("Hash to compare : " + toCompare);
						//							log.debug("Equal: " + equal);
						//						}
						if (equal) {
							result.result = "OK";
							result.validated = true;
							return result;
						}
					} else {
						result.result = "onyx.summary.validation.result.hash.end.not.found";
					}
				} else {
					result.result = "onyx.summary.validation.result.hash.start.not.found";
				}
			} else {
				result.result = "onyx.summary.validation.result.content.end.not.found";
			}
		} else {
			result.result = "onyx.summary.validation.result.content.start.not.found";
		}
		return result;
	}

	/**
	 * Summary HTML hash validator result class. Has two fields: validated
	 * (true/false) if (not) validated and result as textual validation failure
	 * reason.
	 */
	public static final class SummaryChecksumValidatorResult {
		/** Validation result */
		public boolean validated = false;
		/** Result failure text (message key) */
		public String result = "";
		/**
		 * More info if validation failed with an technical error (could not
		 * read file, ...)
		 */
		public String info = "";

		public SummaryChecksumValidatorResult() {
			validated = false;
			result = "";
			info = "";
		}
	}
}

/*
history:

$Log: SummaryCheckSumValidator.java,v $
Revision 1.2  2012-04-05 13:49:41  blaw
OLATCE-1425
* added history
* better indention
* refactored referencess for ExamPoolManagers to the abstract class
* added yesNoDialog for StartExam-function
* added more gui-warnings and / or fallback-values if student- or exam-values are not available


*/