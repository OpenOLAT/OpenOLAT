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
package org.olat.core.commons.controllers.resume;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.admin.landingpages.LandingPagesModule;
import org.olat.admin.landingpages.model.Rules;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.WizardInfoController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryManager;
import org.olat.core.id.context.HistoryModule;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.login.AfterLoginInterceptionManager;
import org.olat.login.LoginInterceptorConfiguration;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResumeSessionController extends BasicController {
	
	private static final String PROPERTY_CAT = "afterLogin";
	
	private Panel currentPanel;
	private VelocityContainer mainVC;
	private Interceptor currentInterceptor;
	
	private ResumeController resumeCtrl;
	private CloseableModalController cmc;
	private WizardInfoController wizardCtrl;
	
	private final List<Property> preferencesList;
	private final List<Interceptor> interceptors;
	
	private final Redirect redirect;

	@Autowired
	private PropertyManager pm;
	@Autowired
	private LandingPagesModule lpModule;
	@Autowired
	private HistoryModule historyModule;
	@Autowired
	private HistoryManager historyManager;
	@Autowired
	private AfterLoginInterceptionManager loginInterceptorsManager;
	
	public ResumeSessionController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(AfterLoginInterceptionManager.class, getLocale(), getTranslator()));
		
		List<LoginInterceptorConfiguration> configurations = loginInterceptorsManager.getInterceptorsConfiguration();
		preferencesList = pm.listProperties(ureq.getIdentity(), null, null, null, PROPERTY_CAT, null);
		
		interceptors = new ArrayList<>();
		for(LoginInterceptorConfiguration configuration:configurations) {
			if(configuration.getCreator() == null) continue;
			
			Controller interceptorCtrl = configuration.getCreator().createController(ureq, wControl);

			//check redo timeout / state
			if(interceptorCtrl != null && isInterceptionNeeded(interceptorCtrl, configuration)) {
				
				String i18nKey = configuration.getI18nIntroKey();
				boolean forceUser = configuration.isForceUser();

				// check if interception criteria is needed
				if(interceptorCtrl instanceof SupportsAfterLoginInterceptor) {
					SupportsAfterLoginInterceptor loginInterceptor = (SupportsAfterLoginInterceptor)interceptorCtrl;
	
					if(loginInterceptor.isUserInteractionRequired(ureq)) {
						interceptors.add(new Interceptor(interceptorCtrl, i18nKey, forceUser));
					} else {
						interceptorCtrl.dispose();
					}
				} else {
					interceptors.add(new Interceptor(interceptorCtrl, i18nKey, forceUser));
				}
			}
		}
		
		//can add disclaimer?
		
		redirect = isResumeInteractionRequired(ureq);
		if(redirect.isInterceptionRequired()) {
			//add an ad hoc interceptor
			resumeCtrl = new ResumeController(ureq, getWindowControl(), redirect);
			listenTo(resumeCtrl);
			interceptors.add(new Interceptor(resumeCtrl, "org.olat.core.commons.controllers.resume:resume", false));
		}
		
		if(interceptors.isEmpty()) {
			if(StringHelper.containsNonWhitespace(redirect.getRedirectUrl())) {
				redirect(ureq, redirect.getRedirectUrl());
			}
			terminateInterception(ureq);
		} else {
			mainVC = createVelocityContainer("resume_session");
			currentPanel = new Panel("resumePanel");
			mainVC.put("actualPanel", currentPanel);
			
			if(interceptors.size() > 1) {
				wizardCtrl = new WizardInfoController(ureq, interceptors.size());
				listenTo(wizardCtrl);
				mainVC.put("wizard", wizardCtrl.getInitialComponent());
				mainVC.contextPut("ctrlCount", interceptors.size());
			}

			pushNextInterceptor(ureq);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), mainVC, true, translate("runonce.title"), false);	
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	/**
	 * Return true if a redirect happen
	 * @return
	 */
	public boolean redirect() {
		return redirect != null && !redirect.isInterceptionRequired()
				&& StringHelper.containsNonWhitespace(redirect.getRedirectUrl());
	}
	
	public boolean userInteractionNeeded() {
		return !interceptors.isEmpty();
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}	

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc && event == CloseableModalController.CLOSE_MODAL_EVENT) {
			// show warning if this is a task, where user is forced to do it
			if(currentInterceptor != null && currentInterceptor.isForceUserKey()) {
				showWarning("runonce.forced");
				cmc.activate();
			}
		} else if(resumeCtrl == source) {
			String cmd = event.getCommand();
			if("landing".equals(cmd)) {
				launch(ureq, redirect.getLandingPage());
			} else if("no".equals(cmd)) {
				//nothing to do
			} else if(StringHelper.containsNonWhitespace(redirect.getRedirectUrl())) {
				String bc = redirect.getFormattedRedirectUrl();
				launch(ureq, bc);
			}
			terminateInterception(ureq);
		} else if (currentInterceptor != null && currentInterceptor.getController() == source) {
			if(event == Event.DONE_EVENT) {
				saveOrUpdatePropertyForController(source.getClass().getName());
				pushNextInterceptor(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				if(!currentInterceptor.isForceUserKey()) {
					pushNextInterceptor(ureq);
				}
			}
		}
		super.event(ureq, source, event);
	}

	private boolean isInterceptionNeeded(Controller controller, LoginInterceptorConfiguration interceptor) {
		String ctrlName = controller.getClass().getName();
		// check if the time between to appearance is ago
		if (interceptor.getRedoTimeout() != null) {
			// redo-timeout not yet over, so don't do again
			Long redoTimeout = interceptor.getRedoTimeout();
			if (((Calendar.getInstance().getTimeInMillis() / 1000) - redoTimeout) < getLastRunTimeForController(ctrlName)) {
				controller.dispose();
				return false;
			}
		// check if run already for non-recurring entries
		} else if (getRunStateForController(ctrlName)) {
			controller.dispose();
			return false;
		}
		return true;
	}
	
	private Long getLastRunTimeForController(String ctrlName) {
		for (Property prop : preferencesList) {
			if (prop.getName().equals(ctrlName)) {
				return Long.valueOf(prop.getLastModified().getTime() / 1000);
			}
		}
		return Long.valueOf(0l);
	}

	private boolean getRunStateForController(String ctrlName) {
		for (Property prop : preferencesList) {
			if (prop.getName().equals(ctrlName)) { 
				return Boolean.parseBoolean(prop.getStringValue());
			}
		}
		return false;
	}
	
	private void saveOrUpdatePropertyForController(String ctrlName) {
		for (Property prop : preferencesList) {
			if (prop.getName().equals(ctrlName)) {
				prop.setStringValue(Boolean.TRUE.toString());
				pm.updateProperty(prop);
				return;
			}
		}
		Property prop = pm.createPropertyInstance(getIdentity(), null, null, PROPERTY_CAT, ctrlName, null, null, "true", null);
		pm.saveProperty(prop);
	}
	
	private void pushNextInterceptor(UserRequest ureq) {
		int currentCtrlIndex = -1;
		if(currentInterceptor != null) {
			currentCtrlIndex = interceptors.indexOf(currentInterceptor);
			removeAsListenerAndDispose(currentInterceptor.getController());
		}
		
		if ((currentCtrlIndex + 1) < interceptors.size()) {
			int nextCtrlIndex = currentCtrlIndex + 1;
			if (interceptors.get(nextCtrlIndex) == null) {
				return;
			}
			
			if(wizardCtrl != null) {
				wizardCtrl.setCurStep(nextCtrlIndex + 1);
			}

			currentInterceptor = interceptors.get(nextCtrlIndex);

			Controller currentCtrl = currentInterceptor.getController();
			if (currentCtrl != null) {
				listenTo(currentCtrl);
				if (StringHelper.containsNonWhitespace(currentInterceptor.getI18nKey())) {
					String[] introComb = currentInterceptor.getI18nKey().split(":");
					mainVC.contextPut("introPkg", introComb[0]);
					mainVC.contextPut("introKey", introComb[1]);
				} else {
					mainVC.contextRemove("introPkg");
					mainVC.contextRemove("introKey");
				}
				currentPanel.setContent(currentCtrl.getInitialComponent());
			}
			
		} else {
			cmc.deactivate();
			if(StringHelper.containsNonWhitespace(redirect.getRedirectUrl())) {
				String bc = redirect.getFormattedRedirectUrl();
				launch(ureq, bc);
				redirect(ureq, redirect.getRedirectUrl());
			}
			terminateInterception(ureq);
		}
	}
	
	private void terminateInterception(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private Redirect isResumeInteractionRequired(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();

		Redirect option;
		if(isREST(ureq)) {
			String url = getRESTRedirectURL(ureq);
			option = new Redirect(url);
		} else if(!historyModule.isResumeEnabled()) {
			String url = toUrl(getLandingBC(ureq));
			option = new Redirect(url);
		} else if(usess.getRoles().isGuestOnly()) {
			String url = toUrl(getLandingBC(ureq));
			option = new Redirect(url);
		} else {
			Preferences prefs =  usess.getGuiPreferences();
			String resumePrefs = (String)prefs.get(WindowManager.class, "resume-prefs");
			if(!StringHelper.containsNonWhitespace(resumePrefs)) {
				resumePrefs = historyModule.getResumeDefaultSetting();
			}

			if("none".equals(resumePrefs)) {
				String url = toUrl(getLandingBC(ureq));
				option = new Redirect(url);
			} else if ("auto".equals(resumePrefs)) {
				HistoryPoint historyEntry = historyManager.readHistoryPoint(ureq.getIdentity());
				if(historyEntry != null && StringHelper.containsNonWhitespace(historyEntry.getBusinessPath())) {
					List<ContextEntry> cloneCes = BusinessControlFactory.getInstance().cloneContextEntries(historyEntry.getEntries());
					String bc = BusinessControlFactory.getInstance().getAsRestPart(cloneCes, true);
					option = new Redirect(bc);
				} else {
					String url = toUrl(getLandingBC(ureq));
					option = new Redirect(url);
				}
			} else if ("ondemand".equals(resumePrefs)) {
				HistoryPoint historyEntry = historyManager.readHistoryPoint(ureq.getIdentity());
				if(historyEntry != null && StringHelper.containsNonWhitespace(historyEntry.getBusinessPath())) {
					List<ContextEntry> cloneCes = BusinessControlFactory.getInstance().cloneContextEntries(historyEntry.getEntries());
					String url = BusinessControlFactory.getInstance().getAsRestPart(cloneCes, true);
					String landingPage = getLandingBC(ureq);
					option = new Redirect(url, landingPage);
				} else {
					String url = toUrl(getLandingBC(ureq));
					option = new Redirect(url);
				}
			} else {
				String url = toUrl(getLandingBC(ureq));
				option = new Redirect(url);
			}
		}
		return option;
	}
	
	private boolean isREST(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		if(usess.getEntry("AuthDispatcher:businessPath") != null) return true;
		return false;
	}
	
	private String getRESTRedirectURL(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		String url = (String)usess.getEntry("AuthDispatcher:businessPath");
		List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(url);
		return BusinessControlFactory.getInstance().getAsRestPart(ces, true);
	}
	
	private String getLandingBC(UserRequest ureq) {
		Preferences prefs =  ureq.getUserSession().getGuiPreferences();
		String landingPage = (String)prefs.get(WindowManager.class, "landing-page");
		if(StringHelper.containsNonWhitespace(landingPage)) {
			String path = Rules.cleanUpLandingPath(landingPage);
			if(StringHelper.containsNonWhitespace(path)) {
				landingPage = BusinessControlFactory.getInstance().formatFromURI(path);
			}
		}
		if(!StringHelper.containsNonWhitespace(landingPage)) {
			landingPage = lpModule.getRules().match(ureq.getUserSession());
		}
		return landingPage;
	}
	
	private String toUrl(String businessPath) {
		String url = businessPath;
		if(StringHelper.containsNonWhitespace(url)) {
			if(url.startsWith("[")) {
				List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(url);
				url = BusinessControlFactory.getInstance().getAsRestPart(ces, true);
			}
			if(url.startsWith("/")) {
				url = url.substring(1, url.length());
			}
		}
		return url;
	}
	
	private void launch(UserRequest ureq, String businessPath) {
		if(StringHelper.containsNonWhitespace(businessPath)) {
			try {
				//make the resume secure. If something fail, don't generate a red screen
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} catch (Exception e) {
				logError("Error while resuming", e);
			}
		}
	}
	
	private void redirect(UserRequest ureq, String url) {
		if(StringHelper.containsNonWhitespace(url)) {
			try {
				ureq.getUserSession().putEntry("redirect-bc", url);
			} catch (Exception e) {
				logError("Error while resuming", e);
			}
		}
	}
	
	public static class Redirect {
		
		private final String redirectUrl;
		private final String landingPagePath;
		private final boolean interception;
		
		public Redirect(String redirectUrl) {
			this.redirectUrl = redirectUrl;
			landingPagePath = null;
			interception = false;
		}
		
		public Redirect(String redirectUrl, String landingPagePath) {
			this.redirectUrl = redirectUrl;
			this.landingPagePath = landingPagePath;
			interception = true;
		}
		
		public boolean isInterceptionRequired() {
			return interception;
		}

		public String getRedirectUrl() {
			return redirectUrl;
		}
		
		/**
		 * 
		 * @return A business path formatted like [xy:0]
		 */
		public String getFormattedRedirectUrl() {
			String bc = redirectUrl;
			if(bc.indexOf(']') < 0) {
				bc = BusinessControlFactory.getInstance().formatFromURI(bc);
			}
			return bc;
		}

		public String getLandingPage() {
			return landingPagePath;
		}
	}
	
	private static class Interceptor {
		
		private final String i18nKey;
		private final boolean forceUserKey;
		private final Controller controller;
		
		public Interceptor(Controller controller, String i18nKey, boolean forceUserKey) {
			this.controller = controller;
			this.i18nKey = i18nKey;
			this.forceUserKey = forceUserKey;
		}

		public String getI18nKey() {
			return i18nKey;
		}

		public boolean isForceUserKey() {
			return forceUserKey;
		}

		public Controller getController() {
			return controller;
		}
	}
}
