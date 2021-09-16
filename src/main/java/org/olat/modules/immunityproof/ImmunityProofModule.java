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
package org.olat.modules.immunityproof;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: 07.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Service
public class ImmunityProofModule extends AbstractSpringModule implements ConfigOnOff {
	
	public static final String USER_PROPERTY_HANDLER = 					ImmunityProofModule.class.getCanonicalName();
	public static final String IMMUNITY_PROOF_COMMISSIONER_ROLE =		"Immunity_Commissioner";
	public static final String REMINDER_MAIL_TRANSLATION_KEY = 			"immunity.proof.reminder.mail.body";
	public static final String PROOF_DELETED_TRANSLATION_KEY = 			"immunity.proof.deleted.mail.body";
	public static final String COMMISSIONER_ADDED_TRANSLATION_KEY = 	"immunity.proof.commissioner.added.mail.body";
	public static final String COMMISSIONER_REMOVED_TRANSLATION_KEY =	"immunity.proof.commissioner.removed.mail.body";
	
	private static final String PROP_ENABLED = 							"immunity.proof.enabled";
	
	private static final String PROP_VALIDITY_VACCINATION = 			"immunity.proof.validity.vaccination";
	private static final String PROP_VALIDITY_RECOVERY = 				"immunity.proof.validity.recovery";
	private static final String PROP_VALIDITY_TEST_PCR = 				"immunity.proof.validity.test.pcr";
	private static final String PROP_VALIDITY_TEST_ANTIGEN = 			"immunity.proof.validity.test.antigen";
	
	private static final String PROP_VALID_VACCINES = 					"immunity.proof.vaccines";
	
	private static final String PROP_REMINDER_BEFORE_EXPIRATION  =		"immunity.proof.reminder.before.expiration";
	
	private static final String PROP_COMMISSIONERS_GROUP_KEY = 			"immunity.proof.commissioners.group.key";
	
	private static final String PROP_QR_ENTRANCE_TEXT = 				"immunity.proof.qr.entrance.text";
	
	@Value("${immunity.proof.enabled}")
	private boolean enabled;
	
	@Value("${immunity.proof.validity.vaccination}")
	private int validityVaccination;
	@Value("${immunity.proof.validity.recovery}")
	private int validityRecovery;
	@Value("${immunity.proof.validity.test.pcr}")
	private int validityPCR;
	@Value("${immunity.proof.validity.test.antigen}")
	private int validityAntigen;
	
	@Value("${immunity.proof.vaccines}")
	private String validVaccines;
	
	@Value("${immunity.proof.reminder.before.expiration}")
	private int reminderPeriod;
	
	@Value("${immunity.proof.qr.entrance.text:}")
    private String qrEntranceText;
    @Value("${immunity.proof.qr.entrance.text:}")
    private String qrEntranceTextBackup;
	
	@Autowired
	public ImmunityProofModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		initProperties();		
	}
	
	@Override
	protected void initFromChangedProperties() {
		initProperties();
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	private void initProperties() {
		enabled = getBooleanPropertyValue(PROP_ENABLED) || enabled;
		
		validityVaccination = getIntPropertyValue(PROP_VALIDITY_VACCINATION, validityVaccination);
		validityRecovery = getIntPropertyValue(PROP_VALIDITY_RECOVERY, validityRecovery);
		validityPCR = getIntPropertyValue(PROP_VALIDITY_TEST_PCR, validityPCR);
		validityAntigen = getIntPropertyValue(PROP_VALIDITY_TEST_ANTIGEN, validityAntigen);
		
		validVaccines = getStringPropertyValue(PROP_VALID_VACCINES, validVaccines);
		
		reminderPeriod = getIntPropertyValue(PROP_REMINDER_BEFORE_EXPIRATION, reminderPeriod);
		
		qrEntranceText = getStringPropertyValue(PROP_QR_ENTRANCE_TEXT, qrEntranceText);
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setBooleanProperty(PROP_ENABLED, enabled, true);
	}
	
	public int getValidityVaccination() {
		return validityVaccination;
	}
	
	public void setValidityVaccination(int validityVaccination) {
		this.validityVaccination = validityVaccination;
		setIntProperty(PROP_VALIDITY_VACCINATION, validityVaccination, true);
	}
	
	public int getValidityRecovery() {
		return validityRecovery;
	}
	
	public void setValidityRecovery(int validityRecovery) {
		this.validityRecovery = validityRecovery;
		setIntProperty(PROP_VALIDITY_RECOVERY, validityRecovery, true);
	}
	
	public int getValidityPCR() {
		return validityPCR;
	}
	
	public void setValidityPCR(int validityPCR) {
		this.validityPCR = validityPCR;
		setIntProperty(PROP_VALIDITY_TEST_PCR, validityPCR, true);
	}
	
	public int getValidityAntigen() {
		return validityAntigen;
	}  
	
	public void setValidityAntigen(int validityAntigen) {
		this.validityAntigen = validityAntigen;
		setIntProperty(PROP_VALIDITY_TEST_ANTIGEN, validityAntigen, true);
	}
	
	public String getValidVaccines() {
		if (validVaccines != null) {
			return validVaccines.replace(",", ", ");
		}
		
		return validVaccines;
	}
	
	public List<String> getValidVaccinesList() {
		if (!StringHelper.containsNoneOfCoDouSemi(validVaccines)) {
			return new ArrayList<>();
		}
		
		return Arrays.asList(validVaccines.split(","));
	}
	
	public void setValidVaccines(String validVaccines) {
		if (!StringHelper.containsNonWhitespace(validVaccines)) {
			return;
		}
		
		validVaccines = validVaccines.replace(" ", "");
		
		this.validVaccines = validVaccines;
		setStringProperty(PROP_VALID_VACCINES, validVaccines, true);
	}
	
	public int getReminderPeriod() {
		return reminderPeriod;
	}
	
	public void setReminderPeriod(int reminderPeriod) {
		this.reminderPeriod = reminderPeriod;
		setIntProperty(PROP_REMINDER_BEFORE_EXPIRATION, reminderPeriod, true);
	}	
	
	public Long getCommissionersGroupKey() {
		return getLongProperty(PROP_COMMISSIONERS_GROUP_KEY);
	}
	
	public void setCommissionersGroupKey(Long groupKey) {
		if (groupKey == null) {
			removeProperty(PROP_COMMISSIONERS_GROUP_KEY, true);
		} else {
			setLongProperty(PROP_COMMISSIONERS_GROUP_KEY, groupKey, true);
		}
	}
	
	public String getQrEntranceText() {
		return qrEntranceText;
	}
	
	public void setQrEntranceText(String qrEntranceText) {
		if (qrEntranceText == null) {
            this.qrEntranceText = qrEntranceTextBackup;
        } else {
            this.qrEntranceText = qrEntranceText;
        }
        setStringProperty(PROP_QR_ENTRANCE_TEXT, qrEntranceText, true);
	}
	
	public int getValidity(ImmunityProofType type) {
		switch (type) {
		case vaccination:
			return getValidityVaccination();
		case recovery:
			return getValidityRecovery();
		case pcrTest:
			return getValidityPCR();
		case antigenTest:
			return getValidityAntigen();
		case medicalCertificate:
			return 0;
		}
		
		return 0;
	}
	
	public enum ImmunityProofType {
		vaccination, 
		recovery, 
		pcrTest,
		antigenTest,
		medicalCertificate
	}
	
	public enum ImmunityProofLevel {
		none,
		claimed,
		validated
	}
}
