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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
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
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.olat.shibboleth.ShibbolethModule;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;

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
	private TextElement textAreaElement;

	public ImportStep00(UserRequest ureq, boolean canCreateOLATPassword) {
		super(ureq);
		this.canCreateOLATPassword = canCreateOLATPassword;
		setI18nTitleAndDescr("step0.description", "step0.short.descr");
		setNextStep(new ImportStep01(ureq, canCreateOLATPassword, false));
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.Step#getInitialPrevNextFinishConfig()
	 */
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, false);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.Step#getStepController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.core.gui.control.generic.wizard.StepsRunContext,
	 *      org.olat.core.gui.components.form.flexible.impl.Form)
	 */
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		StepFormController stepI = new ImportStepForm00(ureq, windowControl, form, stepsRunContext);
		return stepI;
	}


	private final class ImportStepForm00 extends StepFormBasicController {

		private List<Identity> idents;
		private List<UpdateIdentity> updateIdents;
		private List<TransientIdentity> newIdents;
		private List<UserPropertyHandler> userPropertyHandlers;

		@Autowired
		private UserManager um;
		@Autowired
		private BaseSecurity securityManager;
		@Autowired
		private ShibbolethModule shibbolethModule;

		public ImportStepForm00(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
			super(ureq, control, rootForm, runContext, LAYOUT_VERTICAL, null);
			flc.setTranslator(getTranslator());
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
			if (newIdents.size() != 0) {
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

			String defaultlang = I18nModule.getDefaultLocale().toString();
			List<String> importedEmails = new ArrayList<String>();

			boolean importDataError = false;

			idents = new ArrayList<Identity>();
			newIdents = new ArrayList<TransientIdentity>();
			updateIdents = new ArrayList<UpdateIdentity>();
			//check also emails in change-workflow, see OLAT-5723
			Set<String> tempEmailsInUse = getTemporaryEmailInUse();
			
			// Note: some values are fix and required: login, pwd and lang, those
			// can not be configured in the config file
			// because they are not user properties.
			// all fields out of
			// src/serviceconfig/org/olat/_spring/olat_userconfig.xml -> Key:
			// org.olat.admin.user.imp.UserImportController
			// are required and have to be submitted in the right order
			// - pwd can be enabled / disabled by configuration
			Collection<String> languages = I18nModule.getEnabledLanguageKeys();
			String[] lines = inp.split("\r?\n");
			for (int i = 0; i < lines.length; i++) {
				if(i % 25 == 0) {
					DBFactory.getInstance().commitAndCloseSession();
				}
				
				
				String line = lines[i];
				if (line.equals("")) continue;
				
				String delimiter = "\t";
				// use comma as fallback delimiter, e.g. for better testing
				if (line.indexOf(delimiter) == -1) delimiter = ",";
				String[] parts = line.split(delimiter);
				String login, pwd, lang;

				int columnId = 0;

				// login row
				if (parts.length > columnId) {
					login = parts[columnId].trim();
					if (!UserManager.getInstance().syntaxCheckOlatLogin(login)) {
						textAreaElement.setErrorKey("error.login", new String[] { String.valueOf(i + 1), login });
						importDataError = true;
						break;
					}
				} else {
					textAreaElement.setErrorKey("error.columncount", new String[] { String.valueOf(i + 1) });
					importDataError = true;
					break;
				}
				columnId++;

				// pwd row
				if (canCreateOLATPassword) {
					if (parts.length > columnId) {
						pwd = parts[columnId].trim();
						if (StringHelper.containsNonWhitespace(pwd)) {
							if(pwd.startsWith(UserImportController.SHIBBOLETH_MARKER) && shibbolethModule.isEnableShibbolethLogins()) {
								String authusername = pwd.substring(UserImportController.SHIBBOLETH_MARKER.length());
								Authentication auth = securityManager.findAuthenticationByAuthusername(authusername, ShibbolethDispatcher.PROVIDER_SHIB);
								if(auth != null) {
									String authLogin = auth.getIdentity().getName();
									if(!login.equals(authLogin)) {
										textAreaElement.setErrorKey("error.shibbolet.name.inuse", new String[] { String.valueOf(i + 1), authusername });
										importDataError = true;
										break;
									}
								}
							} else if (!UserManager.getInstance().syntaxCheckOlatPassword(pwd)) {
								textAreaElement.setErrorKey("error.pwd", new String[] { String.valueOf(i + 1), pwd });
								importDataError = true;
								break;
							}
						} else {
							// treat all white-space-only passwords as non-passwords.
							// the user generation code below will then generate no
							// authentication token for this user
							pwd = null;
						}
					} else {
						textAreaElement.setErrorKey("error.columncount", new String[] { String.valueOf(i + 1) });
						importDataError = true;
						break;
					}
				} else {
					pwd = null;
				}
				columnId++;

				// optional language fields
				if (parts.length > columnId) {
					lang = parts[columnId].trim();
					if (lang.equals("")) {
						lang = defaultlang;
					} else if (!languages.contains(lang)) {
						textAreaElement.setErrorKey("error.lang", new String[] { String.valueOf(i + 1), lang });
						importDataError = true;
						break;
					}
				} else {
					lang = defaultlang;
				}
				columnId++;

				Identity ident = BaseSecurityManager.getInstance().findIdentityByName(login);
				if (ident != null) {
					// update existing accounts, add info message
					
					UpdateIdentity uIdentity = new UpdateIdentity(ident, pwd, lang);
					idents.add(uIdentity);
					updateIdents.add(uIdentity);
					
					importDataError = updateUserProperties(uIdentity, parts, i, columnId, tempEmailsInUse, importedEmails);
					if(importDataError) break;
				} else {
					// no identity/user yet, create
					// check that no user with same login name is already in list
					for (Iterator<TransientIdentity> it_news = newIdents.iterator(); it_news.hasNext();) {
						TransientIdentity singleUser = it_news.next();
						if (singleUser.getName().equalsIgnoreCase(login)) {
							textAreaElement.setErrorKey("error.login.douplicate", new String[] { String.valueOf(i + 1), login });
							importDataError = true;
							break;
						}
					}

					TransientIdentity ud = new TransientIdentity();
					// insert fix fields: login, pwd, lang from above
					ud.setName(login);
					ud.setPassword(pwd);
					ud.setLanguage(lang);
					importDataError = updateUserProperties(ud, parts, i, columnId, tempEmailsInUse, importedEmails);
					if(importDataError) break;
					
					idents.add(ud);
					newIdents.add(ud);
				}
			}

			return !importDataError;
		}
		
		private Set<String> getTemporaryEmailInUse() {
			Set<String> tempEmailsInUse = new HashSet<String>();
			RegistrationManager rm = CoreSpringFactory.getImpl(RegistrationManager.class);
			List<TemporaryKey> tk = rm.loadTemporaryKeyByAction(RegistrationManager.EMAIL_CHANGE);
			if (tk != null) {
				for (TemporaryKey temporaryKey : tk) {
					XStream xml = new XStream();
					@SuppressWarnings("unchecked")
					Map<String, String> mails = (Map<String, String>) xml.fromXML(temporaryKey.getEmailAddress());
					for(Map.Entry<String, String> mailEntry:mails.entrySet()) {
						tempEmailsInUse.add(mailEntry.getKey());
						tempEmailsInUse.add(mailEntry.getValue());
					}
				}
			}
			return tempEmailsInUse;
		}
		
		private boolean updateUserProperties(Identity ud, String[] parts, int i, int columnId,
				Set<String> tempEmailsInUse, List<String> importedEmails) {
			
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
				if (isMandatoryField && !StringHelper.containsNonWhitespace(thisValue)) {
					String label = "";
					if(userPropertyHandler.i18nFormElementLabelKey() != null) {
						label = translate(userPropertyHandler.i18nFormElementLabelKey());
					}
					textAreaElement.setErrorKey("error.mandatory", new String[] { String.valueOf(i + 1), label });
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
					textAreaElement.setErrorKey("error.lengthorformat", new String[] { String.valueOf(i + 1), label, error});
					importDataError = true;
					break;
				}
				// check that no user with same (institutional) e-mail is already in OLAT
				if ( (thisKey.equals(UserConstants.INSTITUTIONALEMAIL) || thisKey.equals(UserConstants.EMAIL)) && !thisValue.isEmpty() ) {
					// check that no user with same email is already in OLAT
					Identity identity = UserManager.getInstance().findIdentityByEmail(thisValue);
					if (identity != null && !ud.equals(identity)) {
						textAreaElement.setErrorKey("error.email.exists", new String[] { String.valueOf(i + 1), thisValue });
						importDataError = true;
						break;
					}
				}
				// check that no user with same email is already in list
				if (thisKey.equals(UserConstants.EMAIL)) {
					// check that no user with same email is already in list
					Integer mailPos = importedEmails.indexOf(thisValue);
					boolean duplicate = mailPos != -1;
					if (!duplicate) {
						duplicate |= tempEmailsInUse.contains(thisValue);
					}

					if (duplicate) {
						mailPos++;
						textAreaElement.setErrorKey("error.email.douplicate",
								new String[] { String.valueOf(i + 1), thisValue, mailPos.toString() });
						importDataError = true;
						break;
					} else {
						importedEmails.add(thisValue);
					}
				}
				ud.getUser().setProperty(thisKey, thisValue);
				columnId++;
			}
			return importDataError;
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("title");

			FormLayoutContainer textContainer = FormLayoutContainer.createCustomFormLayout("index", getTranslator(), this.velocity_root + "/step0.html");
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

			textAreaElement = uifactory.addTextAreaElement("importform", "form.importdata", -1, 10, 100, false, "", formLayout);
			textAreaElement.setMandatory(true);
			textAreaElement.setNotEmptyCheck("error.emptyform");
		}

		/**
		 * creates a Mapper which generates an import example file for Excel
		 * 
		 * @return
		 */
		private Mapper createMapper(UserRequest ureq) {
			final String charset = UserManager.getInstance().getUserCharset(ureq.getIdentity());
			Mapper m = new Mapper() {
				@SuppressWarnings({"synthetic-access" })
				public MediaResource handle(String relPath, HttpServletRequest request) {
					setTranslator(UserManager.getInstance().getPropertyHandlerTranslator(getTranslator()));
					String headerLine = translate("table.user.login") + " *";
					String dataLine = "demo";
					if (canCreateOLATPassword) {
						headerLine += "\t" + translate("table.user.pwd");
						dataLine += "\t" + "olat4you";
					}
					headerLine += "\t" + translate("table.user.lang");
					dataLine += "\t" + I18nManager.getInstance().getLocaleKey(getLocale());
					UserPropertyHandler userPropertyHandler;
					for (int i = 0; i < userPropertyHandlers.size(); i++) {
						userPropertyHandler = userPropertyHandlers.get(i);
						String mandatoryChar = "";
						if (um.isMandatoryUserProperty(usageIdentifyer, userPropertyHandler)) {
							mandatoryChar = " *";
						}
						headerLine += "\t" + translate(userPropertyHandler.i18nColumnDescriptorLabelKey()) + mandatoryChar;
						dataLine += "\t" + translate("import.example." + userPropertyHandler.getName());
					}
					String writeToFile = headerLine + "\n" + dataLine;
					
					ExcelMediaResource emr = new ExcelMediaResource(writeToFile, charset);
					emr.setFilename("UserImportExample");
					return emr;
				}
			};
			return m;
		}

	}
}