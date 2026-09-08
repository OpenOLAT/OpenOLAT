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
package org.olat.upgrade;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientModule;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;
import org.olat.user.propertyhandlers.ui.UsrPropCfgManager;
import org.olat.user.propertyhandlers.ui.UsrPropCfgObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

/**
 * 
 * Initial date: 7 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_21_1_0 extends OLATUpgrade {
	
	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_21_1_0.class);

	private static final String VERSION = "OLAT_21.1.0";
	
	private static final String MIGRATE_CERTIFICATE_IN_ERROR_STATUS = "MIGRATE CERTIFICATE IN ERROR STATUS";
	private static final String DISABLE_SSRF_EXISTING_INSTANCE = "DISABLE SSRF EXISTING INSTANCE";
	private static final String ADD_CUSTOMER_NUMBER_USER_PROPERTY = "ADD CUSTOMER NUMBER USER PROPERTY";

	private static final String CUSTOMER_NUMBER_PROPERTY = "customerNumber";
	private static final String CUSTOMER_NUMBER_ADMIN_ONLY_CONTEXT = "org.olat.user.ProfileFormController";
	private static final List<String> CUSTOMER_NUMBER_CONTEXTS = List.of(
			CUSTOMER_NUMBER_ADMIN_ONLY_CONTEXT,
			"org.olat.modules.curriculum.ui.member.CurriculumElementMemberUsersController",
			"org.olat.modules.coach.reports.AbstractReportConfiguration");

	@Autowired
	private DB dbInstance;
	@Autowired
	private HttpClientModule httpClientModule;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private UsrPropCfgManager usrPropCfgManager;

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}

		boolean allOk = true;
		allOk &= migrateCertificateInErrorStatus(upgradeManager, uhd);
		allOk &= disableSSRFOnExistingInstances(upgradeManager, uhd);
		allOk &= addCustomerNumberToUserPropertyContexts(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_21_1_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_21_1_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	/**
	 * The customer number is defined in userPropertiesContext.xml, but an instance which
	 * customized the user properties in the administration overrides the usage contexts of
	 * the XML with its own persisted list. A new property is never added to such a list, so
	 * the field stays invisible. Add it to the affected usage contexts, but only when it is
	 * missing, so an instance without customization keeps its XML configuration.
	 */
	private boolean addCustomerNumberToUserPropertyContexts(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(ADD_CUSTOMER_NUMBER_USER_PROPERTY)) {
			try {
				UsrPropCfgObject cfgObject = usrPropCfgManager.getUserPropertiesConfigObject();
				UserPropertyHandler handler = cfgObject.getPropertyHandler(CUSTOMER_NUMBER_PROPERTY);
				if (handler == null) {
					log.error("User property {} not found, migration of the usage contexts skipped.", CUSTOMER_NUMBER_PROPERTY);
					allOk = false;
				} else {
					boolean changed = false;
					if (!cfgObject.isActiveHandler(handler)) {
						cfgObject.setHandlerAsActive(handler, true);
						changed = true;
					}

					for (String contextName : CUSTOMER_NUMBER_CONTEXTS) {
						UserPropertyUsageContext context = cfgObject.getUsageContexts().get(contextName);
						if (context == null) {
							continue;
						}
						if (!context.contains(handler)) {
							context.addPropertyHandler(handler);
							changed = true;
						}
						boolean adminOnly = CUSTOMER_NUMBER_ADMIN_ONLY_CONTEXT.equals(contextName);
						if (context.isForAdministrativeUserOnly(handler) != adminOnly) {
							context.setAsAdminstrativeUserOnly(handler, adminOnly);
							changed = true;
						}
					}

					if (changed) {
						usrPropCfgManager.saveUserPropertiesConfig();
						log.info(Tracing.M_AUDIT, "User property {} added to the user property usage contexts.", CUSTOMER_NUMBER_PROPERTY);
					}
				}
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(ADD_CUSTOMER_NUMBER_USER_PROPERTY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private boolean disableSSRFOnExistingInstances(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(DISABLE_SSRF_EXISTING_INSTANCE)) {
			// Only update existing instances. New instances doesn't run this updated
			// protection is enable, older instances run it, protection disabled
			if(httpClientModule.isSsrfProtectionEnabled()
					&& !isExplicitlyConfigured(HttpClientModule.HTTP_SSRF_PROTECTION_ENABLED_KEY)) {
				httpClientModule.setSsrfProtectionEnabled(false);
				log.info(Tracing.M_AUDIT, "SSRF mitigation disabled.");
			}

			uhd.setBooleanDataValue(DISABLE_SSRF_EXISTING_INSTANCE, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private boolean isExplicitlyConfigured(String key) {
		if (System.getProperty(key) != null) {
			return true;
		}
		Properties localProperties = new Properties();
		try (InputStream in = new ClassPathResource("olat.local.properties").getInputStream()) {
			localProperties.load(in);
		} catch (IOException e) {
			return false; // no local overrides at all, nothing was configured explicitly
		}
		return localProperties.containsKey(key);
	}
	
	private boolean migrateCertificateInErrorStatus(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(MIGRATE_CERTIFICATE_IN_ERROR_STATUS)) {
			try {
				log.info("Migration certificates in error status");

				int count = 0;
				List<Long> certificatesKeys = getCertificatesWithError();
				for (Long certificateKey : certificatesKeys) {
					Certificate certificate = certificatesManager.getCertificateById(certificateKey);
					if(certificate instanceof CertificateImpl certificateImpl
							&& certificate.getStatus() == CertificateStatus.error
							&& !StringHelper.containsNonWhitespace(certificateImpl.getGenerationData())) {
						certificateImpl.setStatus(CertificateStatus.failed);
						certificateImpl.setGenerationNextDate(null);
						certificateImpl.setGenerationRetries(0);
						dbInstance.getCurrentEntityManager().merge(certificateImpl);
						count++;
					}
					dbInstance.commitAndCloseSession();
				}
				dbInstance.commitAndCloseSession();
				log.info("End migration certificates in error status: {}", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(MIGRATE_CERTIFICATE_IN_ERROR_STATUS, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	private List<Long> getCertificatesWithError() {
		String query = """
				select cer.key from certificate cer
				inner join cer.olatResource as rsrc
				inner join cer.identity as ident
				where cer.statusString=:errorStatus
				order by cer.creationDate""";

		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Long.class)
				.setParameter("errorStatus", CertificateStatus.error.name())
				.getResultList();
	}
}
