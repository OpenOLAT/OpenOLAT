/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.admin.user.imp;

import static org.olat.login.ui.LoginUIFactory.formatDescriptionAsList;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.ExcelMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.ldap.LDAPLoginModule;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.shibboleth.ShibbolethModule;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.olat.user.propertyhandlers.DatePropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * First step in user import wizard.
 * 
 * <P>
 * Initial Date: 30.01.2008 <br>
 * 
 * @author rhaag
 */
class ImportStep00 extends BasicStep {

	private static final String usageIdentifyer = UserImportController.class.getCanonicalName();
	private boolean canCreateOLATPassword;
	private Mapper excelMapper;
	private TextAreaElement textAreaElement;

	public ImportStep00(UserRequest ureq, boolean canCreateOLATPassword) {
		super(ureq);
		this.canCreateOLATPassword = canCreateOLATPassword;
		setI18nTitleAndDescr("step0.description", "step0.short.descr");
		setNextStep(new ImportStep01(ureq, canCreateOLATPassword, false));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new ImportStepForm00(ureq, windowControl, form, stepsRunContext);
	}

	private final class ImportStepForm00 extends StepFormBasicController {

		private List<Identity> idents;
		private List<UpdateIdentity> updateIdents;
		private List<TransientIdentity> newIdents;
		private List<UserPropertyHandler> userPropertyHandlers;
		
		private final SyntaxValidator passwordSyntaxValidator;
		private final SyntaxValidator usernameSyntaxValidator;

		@Autowired
		private UserManager um;
		@Autowired
		private UserModule userModule;
		@Autowired
		private I18nModule i18nModule;
		@Autowired
		private I18nManager i18nManager;
		@Autowired
		private OLATAuthManager olatAuthManager;
		@Autowired
		private BaseSecurity securityManager;
		@Autowired
		private LDAPLoginModule ldapModule;
		@Autowired
		private ShibbolethModule shibbolethModule;

		public ImportStepForm00(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			flc.setTranslator(getTranslator());
			usernameSyntaxValidator = olatAuthManager.createUsernameSytaxValidator();
			passwordSyntaxValidator = olatAuthManager.createPasswordSytaxValidator();
			initForm(ureq);
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void formOK(UserRequest ureq) {
			String inp = textAreaElement.getValue();
			addToRunContext("inp", inp);
			addToRunContext("idents", idents);
			addToRunContext("newIdents", newIdents);
			addToRunContext("updateIdents", updateIdents);
			addToRunContext("validImport", Boolean.TRUE);
			boolean newUsers = false;
			if (!newIdents.isEmpty()) {
				setNextStep(new ImportStep01(ureq, canCreateOLATPassword, true));
				newUsers = true;
			}
			addToRunContext("newUsers", newUsers);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}

		@Override
		protected boolean validateFormLogic(UserRequest ureq) {
			Object validatedInp = getFromRunContext("inp");
			String inp = textAreaElement.getValue();
			if(validatedInp != null && validatedInp.equals(inp)) {
				//already validated
				return true;
			}

			String defaultlang = i18nModule.getDefaultLocale().toString();
			List<String> importedEmails = new ArrayList<>();
			List<String> importedInstitutionalEmails = new ArrayList<>();

			idents = new ArrayList<>();
			newIdents = new ArrayList<>();
			updateIdents = new ArrayList<>();
			
			// Note: some values are fix and required: login, pwd and lang, those
			// can not be configured in the config file
			// because they are not user properties.
			// all fields out of
			// src/serviceconfig/org/olat/_spring/olat_userconfig.xml -> Key:
			// org.olat.admin.user.imp.UserImportController
			// are required and have to be submitted in the right order
			// - pwd can be enabled / disabled by configuration
			Collection<String> languages = i18nModule.getEnabledLanguageKeys();
			String[] lines = inp.split("\r?\n");
			List<ErrorLine> errors = new ArrayList<>();
			for (int i = 0; i < lines.length; i++) {
				if(i % 25 == 0) {
					DBFactory.getInstance().commitAndCloseSession();
				}
				
				String line = lines[i];
				if (line.equals("")) continue;
				
				String delimiter = "\t";
				// use comma as fallback delimiter, e.g. for better testing
				if (line.indexOf(delimiter) == -1) {
					delimiter = ",";
				}
				
				String[] parts = line.split(delimiter);
				String login;
				String pwd = null;
				String lang;

				int columnId = 0;

				// login row
				if (parts.length > columnId) {
					login = parts[columnId].trim();
				} else {
					errors.add(errorLine(i, "error.columncount", new String[] { String.valueOf(i + 1) }));
					continue;
				}
				columnId++;

				// pwd row
				if (canCreateOLATPassword) {
					if (parts.length > columnId) {
						String trimmedPwd = parts[columnId].trim();
						// treat all white-space-only passwords as non-passwords.
						// the user generation code below will then generate no
						// authentication token for this user.
						if (StringHelper.containsNonWhitespace(trimmedPwd)) {
							pwd = trimmedPwd;
						}
					} else {
						errors.add(errorLine(i, "error.columncount", new String[] { String.valueOf(i + 1) }));
						continue;
					}
				}
				columnId++;

				// optional language fields
				if (parts.length > columnId) {
					lang = parts[columnId].trim();
					if (lang.equals("")) {
						lang = defaultlang;
					} else if (!languages.contains(lang)) {
						errors.add(errorLine(i, "error.lang", new String[] { String.valueOf(i + 1), lang }));
						continue;
					}
				} else {
					lang = defaultlang;
				}
				columnId++;
				
				Date expirationDate = expirationDate(parts, columnId);
				Identity ident = findByLogin(login, pwd);
				if (ident != null) {
					// update existing accounts, add info message
					
					UpdateIdentity uIdentity = new UpdateIdentity(ident, pwd, lang, expirationDate);
					boolean error = updateUserProperties(uIdentity, ident.getUser(), parts, i, columnId, importedEmails, importedInstitutionalEmails, errors);
					error |= !validatePassword(pwd, uIdentity, i, errors);
					if(error) {
						continue;
					}

					idents.add(uIdentity);
					updateIdents.add(uIdentity);
				} else {
					// no identity/user yet, create
					// check that no user with same login name is already in list
					boolean error = false;
					for (TransientIdentity singleUser:newIdents) {
						if (singleUser.getName().equalsIgnoreCase(login)) {
							errors.add(errorLine(i, "error.login.douplicate", new String[] { String.valueOf(i + 1), login }));
							error = true;
							break;
						}
					}

					TransientIdentity uIdentity = new TransientIdentity();
					// insert fix fields: login, pwd, lang from above
					uIdentity.setName(login);
					uIdentity.setPassword(pwd);
					uIdentity.setLanguage(lang);
					uIdentity.setExpirationDate(expirationDate);
					
					error |= updateUserProperties(uIdentity, null, parts, i, columnId, importedEmails, importedInstitutionalEmails, errors);
					error |= !validateUsername(login, uIdentity, i, errors);
					error |= !validatePassword(pwd, uIdentity, i, errors);
					if(error) {
						continue;
					}
					
					idents.add(uIdentity);
					newIdents.add(uIdentity);
				}
			}
			
			if(!errors.isEmpty()) {
				List<Integer> errorLines = errors.stream().map(ErrorLine::getLine).collect(Collectors.toList());
				textAreaElement.setErrors(errorLines);
				String errorsMsg = errors.stream().map(ErrorLine::getErrorMsg).collect(Collectors.joining("<br>"));
				textAreaElement.setErrorKey("noTransOnlyParam", new String[] { errorsMsg });
			}
			return errors.isEmpty();
		}
		
		private ErrorLine errorLine(int line, String i18nErrorKey, String[] args) {
			return new ErrorLine(line, translate(i18nErrorKey, args));
		}
		
		private Date expirationDate(String[] parts, int columnId) {
			Date expirationDate = null;
			if(parts.length > columnId + userPropertyHandlers.size()) {
				try {
					String expirationPart = parts[columnId + userPropertyHandlers.size()];
					if(StringHelper.containsNonWhitespace(expirationPart)) {
						expirationDate = parseDate(expirationPart, getLocale());
						if(expirationDate == null) {
							for(String languageKey:i18nModule.getEnabledLanguageKeys()) {
								Locale locale = i18nManager.getLocaleOrDefault(languageKey);
								expirationDate = parseDate(expirationPart, locale);
								if(expirationDate != null) {
									break;
								}
							}
						}
					}
				} catch (Exception e) {
					logError("", e);
				}
			}
			
			if(expirationDate != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(expirationDate);
				// Excel will cut 2021 to 21
				if(cal.get(Calendar.YEAR) < 100) {
					cal.add(Calendar.YEAR, 2000);
					expirationDate = cal.getTime();
				}
			}
			return expirationDate;
		}
		
		private Date parseDate(String val, Locale locale) {
			try {
				return Formatter.getInstance(locale).parseDate(val);
			} catch (ParseException e) {
				getLogger().error("Cannot parse date {} with locale {}", val, locale);
				return null;
			}
		}
		
		private Identity findByLogin(String login, String pwd) {
			Identity identity;
			if(pwd != null && pwd.startsWith(UserImportController.LDAP_MARKER) && ldapModule.isLDAPEnabled()) {
				String ldapLogin = pwd.substring(UserImportController.LDAP_MARKER.length());
				Authentication authentication = securityManager.findAuthenticationByAuthusername(ldapLogin,
						LDAPAuthenticationController.PROVIDER_LDAP, BaseSecurity.DEFAULT_ISSUER);
				identity = authentication == null ? null : authentication.getIdentity();
			} else if(pwd != null && pwd.startsWith(UserImportController.SHIBBOLETH_MARKER) && shibbolethModule.isEnableShibbolethLogins()) {
				String shibbolethLogin = pwd.substring(UserImportController.SHIBBOLETH_MARKER.length());
				Authentication authentication = securityManager.findAuthenticationByAuthusername(shibbolethLogin,
						ShibbolethDispatcher.PROVIDER_SHIB, BaseSecurity.DEFAULT_ISSUER);
				identity = authentication == null ? null : authentication.getIdentity();
			} else if(StringHelper.containsNonWhitespace(pwd)) {
				Authentication authentication = securityManager.findAuthenticationByAuthusername(login,
						"OLAT", BaseSecurity.DEFAULT_ISSUER);
				identity = authentication == null ? null : authentication.getIdentity();
			} else {
				identity = securityManager.findIdentityByUsernames(login);
			}
			return identity;
		}
		
		private boolean updateUserProperties(Identity ud, User originalUser, String[] parts, int i, int columnId,
				List<String> importedEmails, List<String> importedInstitutionalEmails, List<ErrorLine> errors) {
			
			boolean importDataError = false;
			for (int j = 0; j < userPropertyHandlers.size(); j++) {
				UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(j);
				String thisKey = userPropertyHandler.getName();
				String thisValue = "";
				// last columns may be empty if not mandatory
				if (parts.length <= columnId) {
					thisValue = "";
				} else {
					thisValue = parts[columnId].trim();
				}
				boolean isMandatoryField = um.isMandatoryUserProperty(usageIdentifyer, userPropertyHandler);
				if (thisKey.equals(UserConstants.EMAIL) && !userModule.isEmailMandatory()) {
					isMandatoryField = false;
				}
				if (isMandatoryField && !StringHelper.containsNonWhitespace(thisValue)) {
					String label = "";
					if(userPropertyHandler.i18nFormElementLabelKey() != null) {
						label = translate(userPropertyHandler.i18nFormElementLabelKey());
					}
					errors.add(errorLine(i, "error.mandatory", new String[] { String.valueOf(i + 1), label }));
					importDataError = true;
					break;
				}
				// used for call-back value depending on PropertyHandler
				ValidationError validationError = new ValidationError();
				// Only validate value when not empty. In case of mandatory fields the previous check makes sure the
				// user has a non-empty value. 
				if (StringHelper.containsNonWhitespace(thisValue) && !userPropertyHandler.isValidValue(null, thisValue, validationError, getLocale())) {
					String error = "unkown";
					String label = "";
					if(userPropertyHandler.i18nFormElementLabelKey() != null) {
						label = translate(userPropertyHandler.i18nFormElementLabelKey());
					}
					if(validationError.getErrorKey() != null) {
						error = translate(validationError.getErrorKey(), validationError.getArgs());
					}
					errors.add(errorLine(i, "error.lengthorformat", new String[] { String.valueOf(i + 1), label, error}));
					importDataError = true;
					break;
				}
				// check that no user with same (institutional) e-mail is already in OLAT
				if ( (thisKey.equals(UserConstants.INSTITUTIONALEMAIL) && !thisValue.isEmpty()) || thisKey.equals(UserConstants.EMAIL)) {
					if (!um.isEmailAllowed(thisValue, originalUser)) {
						errors.add(errorLine(i, "error.email.exists", new String[] { String.valueOf(i + 1), thisValue }));
						importDataError = true;
						break;
					}
				}
				// check that no user with same email is already in list
				if(userModule.isEmailUnique() && StringHelper.containsNonWhitespace(thisValue)) {
					if (thisKey.equals(UserConstants.EMAIL)) {
						// check that no user with same email is already in list
						int mailPos = importedEmails.indexOf(thisValue);
						if(mailPos < 0) {
							mailPos = importedInstitutionalEmails.indexOf(thisValue);
							if(mailPos >= 0 && thisValue.equals(ud.getUser().getProperty(UserConstants.INSTITUTIONALEMAIL, getLocale()))) {
								mailPos = -1;
							}
						}
						boolean duplicate = mailPos > -1;
						if (duplicate) {
							mailPos++;
							errors.add(errorLine(i, "error.email.douplicate", new String[] { String.valueOf(i + 1), thisValue, Integer.toString(mailPos) }));
							importDataError = true;
							break;
						} else {
							importedEmails.add(thisValue);
						}
					} else if (thisKey.equals(UserConstants.INSTITUTIONALEMAIL)) {
						// check that no user with same email is already in list
						int mailPos = importedInstitutionalEmails.indexOf(thisValue);
						if(mailPos < 0) {
							mailPos = importedEmails.indexOf(thisValue);
							if(mailPos >= 0 && thisValue.equals(ud.getUser().getProperty(UserConstants.EMAIL, getLocale()))) {
								mailPos = -1;
							}
						}
						boolean duplicate = mailPos > -1;
						if (duplicate) {
							mailPos++;
							errors.add(errorLine(i, "error.email.douplicate", new String[] { String.valueOf(i + 1), thisValue, Integer.toString(mailPos) }));
							importDataError = true;
							break;
						} else {
							importedInstitutionalEmails.add(thisValue);
						}
					}
				}
				
				// convert to date internal format
				if(userPropertyHandler instanceof DatePropertyHandler) {
					DatePropertyHandler dateHandler = (DatePropertyHandler)userPropertyHandler;
					thisValue = encodeDateProperty(thisValue, dateHandler);
				}

				ud.getUser().setProperty(thisKey, thisValue);
				columnId++;
			}
			return importDataError;
		}
		
		private String encodeDateProperty(String thisValue, DatePropertyHandler handler) {
			Date date = parseDate(thisValue, getLocale());
			if(date == null) {
				for(String languageKey:i18nModule.getEnabledLanguageKeys()) {
					Locale locale = i18nManager.getLocaleOrDefault(languageKey);
					date = parseDate(thisValue, locale);
					if(date != null) {
						break;
					}
				}
			}
			return date == null ? null : handler.encode(date);
		}
		
		private boolean validatePassword(String password, Identity userIdentity, int i, List<ErrorLine> errors) {
			if (StringHelper.containsNonWhitespace(password)
					&& !password.startsWith(UserImportController.SHIBBOLETH_MARKER)
					&& !password.startsWith(UserImportController.LDAP_MARKER)) {
				ValidationResult validationResult = passwordSyntaxValidator.validate(password, userIdentity);
				if (!validationResult.isValid()) {
					String descriptions = formatDescriptionAsList(validationResult.getInvalidDescriptions(), getLocale());
					errors.add(errorLine(i, "error.pwd", new String[] { String.valueOf(i + 1), descriptions }));
					return false;
				}
			}
			return true;
		}
		
		private boolean validateUsername(String username, Identity userIdentity, int i, List<ErrorLine> errors) {
			if (StringHelper.containsNonWhitespace(username)) {
				ValidationResult validationResult = usernameSyntaxValidator.validate(username, userIdentity);
				if (!validationResult.isValid()) {
					String descriptions = validationResult.getInvalidDescriptions().get(0).getText(getLocale());
					errors.add(errorLine(i, "error.login", new String[] { String.valueOf(i + 1), descriptions }));
					return false;
				}
			}
			return true;
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("title");
			formLayout.setElementCssClass("o_sel_import_users_data");

			FormLayoutContainer textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), velocity_root + "/step0.html");
			formLayout.add(textContainer);
			textContainer.contextPut("canCreateOLATPassword", canCreateOLATPassword);

			userPropertyHandlers = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, true);
			excelMapper = createMapper(ureq);
			String mapperURI = registerMapper(ureq, excelMapper);
			textContainer.contextPut("mapperURI", mapperURI);

			// get mandatory user-properties and set as text
			setTranslator(UserManager.getInstance().getPropertyHandlerTranslator(getTranslator()));
			UserPropertyHandler userPropertyHandler;
			String mandatoryProperties = "";
			for (int i = 0; i < userPropertyHandlers.size(); i++) {
				userPropertyHandler = userPropertyHandlers.get(i);
				String mandatoryChar = "";
				if (um.isMandatoryUserProperty(usageIdentifyer, userPropertyHandler)) {
					mandatoryChar = " *";
				}
				mandatoryProperties += ", " + translate(userPropertyHandler.i18nColumnDescriptorLabelKey()) + mandatoryChar;
			}
			textContainer.contextPut("mandatoryProperties", mandatoryProperties);
			
			List<String> passwordRules = passwordSyntaxValidator.getAllDescriptions().stream()
					.map(d -> d.getText(getLocale()))
					.collect(Collectors.toList());
			textContainer.contextPut("passwordRules", passwordRules);
			
			textAreaElement = uifactory.addTextAreaElement("importform", "form.importdata", -1, 10, 100, false, false, true, "", formLayout);
			textAreaElement.setMandatory(true);
			textAreaElement.setLineNumbersEnbaled(true);
			textAreaElement.setStripedBackgroundEnabled(true);
			textAreaElement.setFixedFontWidth(true);
			textAreaElement.setNotEmptyCheck("error.emptyform");
		}

		/**
		 * creates a Mapper which generates an import example file for Excel
		 * 
		 * @return
		 */
		private Mapper createMapper(UserRequest ureq) {
			final String charset = um.getUserCharset(ureq.getIdentity());
			return (relPath, request) -> {
				setTranslator(um.getPropertyHandlerTranslator(getTranslator()));
				StringBuilder headerLine = new StringBuilder(1024);
				headerLine.append(translate("table.user.login")).append(" *");
				StringBuilder dataLine = new StringBuilder();
				dataLine.append("demo");
				if (canCreateOLATPassword) {
					headerLine.append("\t").append(translate("table.user.pwd"));
					dataLine.append("\t").append("olat4you");
				}
				headerLine.append("\t").append(translate("table.user.lang"));
				dataLine.append("\t").append(i18nModule.getLocaleKey(getLocale()));
				for (int i = 0; i < userPropertyHandlers.size(); i++) {
					UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
					headerLine.append("\t").append(translate(userPropertyHandler.i18nColumnDescriptorLabelKey()));
					if (um.isMandatoryUserProperty(usageIdentifyer, userPropertyHandler)) {
						headerLine.append(" *");
					}
					dataLine.append("\t").append(translate("import.example.".concat(userPropertyHandler.getName())));
				}
				
				headerLine.append("\t").append(translate("table.user.expiration"));
				String dateExample = Formatter.getInstance(getLocale()).formatDate(ureq.getRequestTimestamp());
				dataLine.append("\t").append(dateExample);
				
				String writeToFile = headerLine
						.append("\n").append(dataLine)
						.toString();
				ExcelMediaResource emr = new ExcelMediaResource(writeToFile, charset);
				emr.setFilename("UserImportExample");
				return emr;
			};
		}
		
		private class ErrorLine {
			private final int line;
			private final String errorMsg;
			
			public ErrorLine(int line, String errorMsg) {
				this.line = line;
				this.errorMsg = errorMsg;
			}

			public int getLine() {
				return line;
			}

			public String getErrorMsg() {
				return errorMsg;
			}
		}
	}
}