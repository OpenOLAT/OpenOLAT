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
package org.olat.modules.immunityproof.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.i18n.I18nManager;
import org.olat.modules.immunityproof.ImmunityProofContext;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ImmunityProofModule.ImmunityProofType;

public class ImmunityProofCertificateChecker extends Thread {

	private static final Logger log = Tracing.createLoggerFor(ImmunityProofCertificateChecker.class);

	private volatile Process process;
	private volatile ImmunityProofContext context;

	private final List<String> cmd;
	private final CountDownLatch doneSignal;

	private ImmunityProofModule immunityProofModule; // Autowiring is not working here

	public ImmunityProofCertificateChecker(ImmunityProofModule immunityProofModule, ImmunityProofContext context,
			List<String> cmd, CountDownLatch doneSignal) {
		this.immunityProofModule = immunityProofModule;
		this.cmd = cmd;
		this.context = context;
		this.doneSignal = doneSignal;
	}

	public void destroyProcess() {
		if (process != null) {
			process.destroy();
			process = null;
		}
	}

	@Override
	public void run() {
		try {
			if (log.isDebugEnabled()) {
				log.debug(cmd.toString());
			}

			ProcessBuilder builder = new ProcessBuilder(cmd);
			process = builder.start();
			executeProcess(context, process);
			doneSignal.countDown();
		} catch (IOException e) {
			log.error("Could not spawn convert sub process", e);
			destroyProcess();
		}
	}

	private final void executeProcess(ImmunityProofContext context, Process proc) {

		StringBuilder errorBuilder = new StringBuilder();
		StringBuilder outputBuilder = new StringBuilder();
		String line;

		StringBuilder errorLogBuilder = new StringBuilder();
		StringBuilder outputLogBuilder = new StringBuilder();
		context.setOutput(outputLogBuilder);
		context.setErrors(errorLogBuilder);

		InputStream stderr = proc.getErrorStream();
		InputStreamReader iserr = new InputStreamReader(stderr);
		BufferedReader berr = new BufferedReader(iserr);
		line = null;
		try {
			while ((line = berr.readLine()) != null) {
				errorBuilder.append(line);
				errorLogBuilder.append(line).append("<br>");
			}
		} catch (IOException e) {
			//
		}

		InputStream stdout = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdout);
		BufferedReader br = new BufferedReader(isr);
		line = null;
		try {
			while ((line = br.readLine()) != null) {
				outputBuilder.append(line);
				outputLogBuilder.append(line).append("<br>");
			}
		} catch (IOException e) {
			//
		}

		if (log.isDebugEnabled()) {
			log.debug("Error: {}", errorBuilder.toString());
			log.debug("Output: {}", outputBuilder.toString());
		}
		
		String output = outputBuilder.toString();
		String error = errorBuilder.toString();
		
		String validKeyUsage = "Valid Key Usage: True";
		String signatureValid = "Signature Valid: True";
		String payload = "Payload";
		
		// Clear context
		context.setCertificateBelongsToUser(false);
		context.setCertificateFound(false);
		context.setCertificateValid(false);
		context.setProofFrom(null);
		context.setProofUntil(null);
		context.setProofType(null);
		
		try {
			int exitValue = proc.waitFor();
			boolean signaturAndKeyAreValid = output.contains(validKeyUsage) && output.contains(signatureValid);

			if (exitValue != 0 || !signaturAndKeyAreValid) {
				context.setCertificateFound(false);
				log.debug(output);
				log.error(error);
				return;
			}

			String[] outputArray = output.split(payload);
			int firstJsonBracket = outputArray[1].indexOf("{");
			int lastJsonBracket = outputArray[1].lastIndexOf("}") + 1;
			String jsonPayloadString = outputArray[1].substring(firstJsonBracket, lastJsonBracket);

			JSONObject jsonPayload = new JSONObject(jsonPayloadString);

			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			List<SimpleDateFormat> timeFormats = new ArrayList<>();
			timeFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"));
			timeFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss[+-]hh"));
			timeFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss[+-]hhmm"));
			timeFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss[+-]hh:mm"));

			Date birthdate = null;
			String firstName = "";
			String lastName = "";

			try {
				birthdate = dateFormat.parse(jsonPayload.getString("dob"));
				context.setBirthDate(birthdate);
			} catch (Exception e) {
			}

			try {
				firstName = jsonPayload.getJSONObject("nam").getString("gn");
				context.setFirstName(firstName);
			} catch (Exception e) {
			}

			try {
				lastName = jsonPayload.getJSONObject("nam").getString("fn");
				context.setLastName(lastName);
			} catch (Exception e) {
			}

			if (jsonPayload.has("v")) {
				// Vaccination
				JSONObject vaccination = jsonPayload.getJSONArray("v").getJSONObject(0);
				// Check for COVID 19
				if (vaccination.getInt("tg") != 840539006) {
					return;
				}

				// Check for amount of doses
				if (vaccination.getInt("dn") < vaccination.getInt("sd")) {
					return;
				}

				// Check date of vaccination
				Date vaccinationDate = dateFormat.parse(vaccination.getString("dt"));
				if (vaccinationDate.after(new Date())) {
					return;
				}

				context.setProofFrom(vaccinationDate);
				context.setProofType(ImmunityProofType.vaccination);

			} else if (jsonPayload.has("t")) {
				// Test
				JSONObject test = jsonPayload.getJSONArray("t").getJSONObject(0);
				// Check for COVID 19
				if (test.getInt("tg") != 840539006) {
					return;
				}

				// Sample collection date
				Date testDate = parseTime(timeFormats, test.getString("sc"));
				if (testDate.after(new Date())) {
					return;
				}
				
				// Save test date
				context.setProofFrom(testDate);
				
				// Check PCR test
				if (test.getString("tt").equals("LP6464-4")) {
					context.setProofType(ImmunityProofType.pcrTest);
				}
				// Check rapid test
				else if (test.getString("tt").equals("LP217198-3")) {
					context.setProofType(ImmunityProofType.antigenTest);
				}
				
			} else if (jsonPayload.has("r")) {
				// Recovery
				JSONObject recovery = jsonPayload.getJSONArray("r").getJSONObject(0);
				// Check for COVID 19
				if (recovery.getInt("tg") != 840539006) {
					return;
				}

				// Recovery Dates
				Date validFrom = dateFormat.parse(recovery.getString("df"));
				Date validUntil = dateFormat.parse(recovery.getString("du"));
				
				context.setProofFrom(validFrom);
				context.setProofUntil(validUntil);
				context.setProofType(ImmunityProofType.recovery);
			}

			context.setCertificateFound(true);
			context.setCertificateValid(true);

			// Check if it belongs to user
			if (context.getIdentity() != null) {
				boolean certificateBelongsToUser = true;
				
				// Check birthdate
				int birthdateAccordance = immunityProofModule.getAccordanceBirthdate();
				if (birthdateAccordance == 100) {
					Locale userLocale = I18nManager.getInstance()
							.getLocaleOrDefault(context.getIdentity().getUser().getPreferences().getLanguage());

					String birthdateString = context.getIdentity().getUser().getProperty(UserConstants.BIRTHDAY,
							userLocale);
					String birthDateToCheck = Formatter.getInstance(userLocale).formatDate(birthdate);

					certificateBelongsToUser &= birthdateString.equals(birthDateToCheck);
				}

				// Check strings
				LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

				// Check first name
				int firstNameAccordance = immunityProofModule.getAccordanceFirstName();
				boolean firstNameCorrect = false;

				if (firstNameAccordance > 0) {
					String firstNameToCheck = context.getIdentity().getUser().getFirstName();

					if (firstName.contains(firstNameToCheck) || firstNameToCheck.contains(firstName)) {
						firstNameCorrect = true;
					} else {
						int firstNameDistance = levenshteinDistance.apply(firstName, firstNameToCheck);

						if (firstNameDistance > 0) {
							float firstNameCoverage = 1 - ((float) firstNameDistance / firstName.length());
							float firstNameToCheckCoverage = 1
									- ((float) firstNameDistance / firstNameToCheck.length());
							float firstNameRequiredAccordance = (float) firstNameAccordance / 100;

							firstNameCorrect = firstNameCoverage >= firstNameRequiredAccordance
									|| firstNameToCheckCoverage >= firstNameRequiredAccordance;
						}
					}
					
					if (!firstNameCorrect) {
						// This is an edge case, repeat everything with lower cased names
						
						firstName = firstName.toLowerCase();
						firstNameToCheck = firstNameToCheck.toLowerCase();
						
						if (firstName.contains(firstNameToCheck) || firstNameToCheck.contains(firstName)) {
							firstNameCorrect = true;
						} else {
							int firstNameDistance = levenshteinDistance.apply(firstName, firstNameToCheck);

							if (firstNameDistance > 0) {
								float firstNameCoverage = 1 - ((float) firstNameDistance / firstName.length());
								float firstNameToCheckCoverage = 1
										- ((float) firstNameDistance / firstNameToCheck.length());
								float firstNameRequiredAccordance = (float) firstNameAccordance / 100;

								firstNameCorrect = firstNameCoverage >= firstNameRequiredAccordance
										|| firstNameToCheckCoverage >= firstNameRequiredAccordance;
							}
						}
					}
				} else {
					firstNameCorrect = true;
				}

				certificateBelongsToUser &= firstNameCorrect;

				// Check last name
				int lastNameAccordance = immunityProofModule.getAccordanceLastName();
				boolean lastNameCorrect = false;
				
				if (lastNameAccordance > 0) {
					String lastNameToCheck = context.getIdentity().getUser().getLastName();

					if (lastName.contains(lastNameToCheck) || lastNameToCheck.contains(lastName)) {
						lastNameCorrect = true;
					} else {
						int lastNameDistance = levenshteinDistance.apply(lastName, lastNameToCheck);

						if (lastNameDistance > 0) {
							float lastNameCoverage = 1 - ((float) lastNameDistance / lastName.length());
							float lastNameToCheckCoverage = 1 - ((float) lastNameDistance / lastNameToCheck.length());
							float lastNameRequiredAccordance = (float) lastNameAccordance / 100;

							lastNameCorrect = lastNameCoverage >= lastNameRequiredAccordance
									|| lastNameToCheckCoverage >= lastNameRequiredAccordance;
						}
					}
					
					if (!lastNameCorrect) {
						// This is an edge case, repeat everything with lower cased names
						
						lastName = lastName.toLowerCase();
						lastNameToCheck = lastNameToCheck.toLowerCase();
						
						if (lastName.contains(lastNameToCheck) || lastNameToCheck.contains(lastName)) {
							lastNameCorrect = true;
						} else {
							int lastNameDistance = levenshteinDistance.apply(lastName, lastNameToCheck);

							if (lastNameDistance > 0) {
								float lastNameCoverage = 1 - ((float) lastNameDistance / lastName.length());
								float lastNameToCheckCoverage = 1 - ((float) lastNameDistance / lastNameToCheck.length());
								float lastNameRequiredAccordance = (float) lastNameAccordance / 100;

								lastNameCorrect = lastNameCoverage >= lastNameRequiredAccordance
										|| lastNameToCheckCoverage >= lastNameRequiredAccordance;
							}
						}
					}
				} else {
					lastNameCorrect = true;
				}

				certificateBelongsToUser &= lastNameCorrect;

				context.setCertificateBelongsToUser(certificateBelongsToUser);
			}


		} catch (Exception e) {
			// Go on
		}

		return;
	}

	private Date parseTime(List<SimpleDateFormat> formats, String timeString) {
		for (SimpleDateFormat pattern : formats) {
			try {
				return pattern.parse(timeString);

			} catch (ParseException pe) {
			}
		}

		return null;
	}

	public ImmunityProofContext getContext() {
		return context;
	}
}
