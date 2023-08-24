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
package org.olat.modules.openbadges.v2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesFactory;

import org.json.JSONObject;

/**
 * Initial date: 2023-05-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class Assertion {
	private static final String TYPE_VALUE = "Assertion";
	private static final String RECIPIENT_KEY = "recipient";
	private static final String BADGE_KEY = "badge";
	private static final String VERIFICATION_KEY = "verification";
	private static final String VERIFY_KEY = "verify";
	private static final String ISSUED_ON_KEY = "issuedOn";

	private static final SimpleDateFormat isoFormatMs = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	private static final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");

	/*
	 * Typical value: https://instance.openolat.org/badges/assertion/12345
	 * The URL returns a JSON of the assertion.
	 */
	private String id;
	private Identity recipient;
	private Badge badge;
	private Verification verification;
	private Date issuedOn;
	private String identifier;

	public Assertion(BadgeAssertion badgeAssertion) {
		setId(OpenBadgesFactory.createAssertionVerifyUrl(badgeAssertion.getUuid()));
		setIdentifier(badgeAssertion.getUuid());
		setRecipient(new Identity(badgeAssertion.getRecipientObject()));
		setBadge(new Badge(badgeAssertion.getBadgeClass()));
		setVerification(new Verification(badgeAssertion.getVerificationObject()));
		setIssuedOn(badgeAssertion.getIssuedOn());
	}

	public Assertion(JSONObject jsonObject) throws IllegalArgumentException {
		for (String key : jsonObject.keySet()) {
			if (Constants.ID_KEY.equals(key)) {
				if (jsonObject.get(Constants.ID_KEY) instanceof String idString) {
					setId(idString);
				} else {
					throw new IllegalArgumentException("Invalid assertion ID.");
				}
			} else if (Constants.TYPE_KEY.equals(key)) {
				if (!TYPE_VALUE.equals(jsonObject.get(Constants.TYPE_KEY))) {
					throw new IllegalArgumentException("Only type 'Assertion' supported.");
				}
			} else if (RECIPIENT_KEY.equals(key)) {
				if (jsonObject.get(RECIPIENT_KEY) instanceof JSONObject recipientJsonObject) {
					setRecipient(new Identity(recipientJsonObject));
				} else {
					throw new IllegalArgumentException("Invalid assertion recipient.");
				}
			} else if (BADGE_KEY.equals(key)) {
				if (jsonObject.get(BADGE_KEY) instanceof JSONObject badgeJsonObject) {
					setBadge(new Badge(badgeJsonObject));
				} else {
					throw new IllegalArgumentException("Invalid assertion recipient.");
				}
			} else if (VERIFICATION_KEY.equals(key) || VERIFY_KEY.equals(key)) {
				if (jsonObject.get(key) instanceof JSONObject verificationJsonObject) {
					setVerification(new Verification(verificationJsonObject));
				} else {
					throw new IllegalArgumentException("Invalid assertion verification.");
				}
			} else if (ISSUED_ON_KEY.equals(key)) {
				if (jsonObject.get(ISSUED_ON_KEY) instanceof String issuedOnString) {
					try {
						Date date = isoFormat.parse(issuedOnString);
						setIssuedOn(date);
					} catch (ParseException e) {
						throw new IllegalArgumentException("Invalid assertion issued on date. Parse error.");
					}
				} else {
					throw new IllegalArgumentException("Invalid assertion issued on date.");
				}
			}
		}
	}

	public JSONObject asJsonObject() {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put(Constants.TYPE_KEY, TYPE_VALUE);
		jsonObject.put(Constants.CONTEXT_KEY, Constants.CONTEXT_VALUE);
		jsonObject.put(Constants.ID_KEY, id);
		jsonObject.put(RECIPIENT_KEY, recipient.asJsonObject());
		jsonObject.put(BADGE_KEY, badge.asJsonObject());
		jsonObject.put(VERIFICATION_KEY, verification.asJsonObject());
		jsonObject.put(ISSUED_ON_KEY, isoFormat.format(issuedOn));

		return jsonObject;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Identity getRecipient() {
		return recipient;
	}

	public void setRecipient(Identity recipient) {
		this.recipient = recipient;
	}

	public Badge getBadge() {
		return badge;
	}

	public void setBadge(Badge badge) {
		this.badge = badge;
	}

	public Verification getVerification() {
		return verification;
	}

	public void setVerification(Verification verification) {
		this.verification = verification;
	}

	public Date getIssuedOn() {
		return issuedOn;
	}

	public void setIssuedOn(Date issuedOn) {
		this.issuedOn = issuedOn;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
}
