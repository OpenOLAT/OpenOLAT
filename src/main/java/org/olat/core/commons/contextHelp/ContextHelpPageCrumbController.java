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

package org.olat.core.commons.contextHelp;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.breadcrumb.CrumbBasicController;
import org.olat.core.gui.media.ClasspathMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.resource.OresHelper;

/**
 * <h3>Description:</h3> The context help page crumb controller reads context help
 * files from the classpath and embedds them into a context help viewer.
 * <p>
 * 
 * <h3>Events thrown by this controller:</h3> none
 * <p>
 * Initial Date: 30.10.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

class ContextHelpPageCrumbController extends CrumbBasicController  {
	private VelocityContainer contextHelpWrapperVC;
	private VelocityContainer contextHelpPageVC;
	private PackageTranslator pageTranslator;
	private String bundleName = null, page = null;
	private String chelpStaticDirUrl;

	/**
	 * Constructor for a context help details page. This renders a crumb with
	 * the help page for the given bundle and page
	 * 
	 * @param ureq
	 * @param lwControl
	 * @param bundleName
	 * @param page
	 * @param pageLocale
	 */
	ContextHelpPageCrumbController(UserRequest ureq, WindowControl lwControl, String bundleName, String page, Locale pageLocale) {
		super(ureq, lwControl);
		setLocale(pageLocale, true);

		this.bundleName = bundleName;
		this.page = page;
		contextHelpWrapperVC = createVelocityContainer("contexthelpwrapper");		
		// Initialize the page mapper - must be done before calling createHelpPage()
		initPageResourceMapper(ureq);	
		pageTranslator = new PackageTranslator(bundleName, getLocale());
		contextHelpPageVC = createHelpPage(bundleName, page);								
		
		// Add page infos and rating
		addPageInfos();
		addPageRating(ureq);

		if (contextHelpPageVC == null) {
			contextHelpWrapperVC.contextPut("page", ureq.getNonParsedUri());			
		}
		
		putInitialPanel(contextHelpWrapperVC);
	}

	/**
	 * Internal helper to initialize the mapper that provides context help
	 * resources. The mapper tries it first with the language of the user, then
	 * with the default language and last but not least with the fallback
	 * language.
	 */
	private void initPageResourceMapper(UserRequest ureq) {
		// Add page resources mapper
		Mapper pageResourceMapper = new Mapper() {
			public MediaResource handle(String relPath, HttpServletRequest request) {
				ClasspathMediaResource mr = null;
				// relPath: myimage.png
				int suffixPos = relPath.lastIndexOf(".");
				if (suffixPos > 0) {
					String mediaName = relPath.substring(0, suffixPos);
					//fxdiff FXOLAT-185:fix loading of files in jar
					if(mediaName.startsWith("/")) {
						mediaName = mediaName.substring(1, mediaName.length());
					}
					String postfix = relPath.substring(suffixPos);
					// 1) try it with current language
					String fileName = mediaName + "_" + getLocale().toString() + postfix;
					mr = new ClasspathMediaResource(Package.getPackage(bundleName), ContextHelpModule.CHELP_STATIC_DIR + fileName);
					// 2) try it with default language
					if (!mr.resourceExists()) {
						fileName = mediaName + "_" + I18nModule.getDefaultLocale().toString() + postfix;
						mr = new ClasspathMediaResource(Package.getPackage(bundleName), ContextHelpModule.CHELP_STATIC_DIR + fileName);						
					}
					// 3) try it with fallback language
					if (!mr.resourceExists()) {
						fileName = mediaName + "_" + I18nModule.getFallbackLocale().toString() + postfix;
						mr = new ClasspathMediaResource(Package.getPackage(bundleName), ContextHelpModule.CHELP_STATIC_DIR + fileName);						
					}
				}
				// If not even a fallback image is found, serve a not-found resource
				if (!mr.resourceExists()) {
					return new NotFoundMediaResource(relPath);
				}
				return mr;
			}
		};
		// we use user scope mappers because global mappers can only be instantiated once
		chelpStaticDirUrl = registerMapper(ureq, pageResourceMapper);
	}

	/**
	 * Inernal helper to add some information about the currently loaded page.
	 */
	private void addPageInfos() {
		if (contextHelpPageVC == null) {
			contextHelpWrapperVC.contextPut("pageFound", Boolean.FALSE);
		} else {
			contextHelpWrapperVC.contextPut("pageFound", Boolean.TRUE);
			contextHelpWrapperVC.put("contextHelpPage", contextHelpPageVC);			
			contextHelpWrapperVC.contextPut("title", getCrumbLinkText());
		}
	}

	/**
	 * Internal helper to add the page rating elements. If the user is not
	 * authenticated or the feature is disabled in the configuration, no such
	 * elements are added.
	 * 
	 * @param ureq
	 */
	private void addPageRating(UserRequest ureq) {
		if ( ! ContextHelpModule.isContextRatingEnabled() 
			|| ! ureq.getUserSession().isAuthenticated()
			|| contextHelpPageVC == null ) return;
		
			
		Roles roles = ureq.getUserSession().getRoles();
		OLATResourceable helpOres = OresHelper.createOLATResourceableType("contexthelp");
		String key = pageTranslator.toString() + ":" + bundleName + ":" + page;
		CommentAndRatingSecurityCallback secCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), roles.isOLATAdmin(), roles.isGuestOnly());
		UserCommentsAndRatingsController commentsAndRatingCtr = new UserCommentsAndRatingsController(ureq, getWindowControl(), helpOres, key, secCallback, true, true, true);
		listenTo(commentsAndRatingCtr);
		contextHelpWrapperVC.put("commentsAndRatingCtr", commentsAndRatingCtr.getInitialComponent());
	}

	/**
	 * Helper to create the velocity page and initialize the page translator
	 * 
	 * @param bundleName
	 * @param pageName
	 * @return
	 */
	private VelocityContainer createHelpPage(String bundleName, String pageName) {
		if (bundleName == null || pageName == null) return null;
		String pagePath = bundleName.replace('.', '/') + ContextHelpModule.CHELP_DIR + pageName;
		if (getClass().getResource("/" + pagePath) == null) {
			logWarn("Context help page does not exist on path::" + pagePath, null);
			return null;
		}
		VelocityContainer container =  new VelocityContainer("contextHelpPageVC", pagePath, pageTranslator, this);					
		// add media directory url to context for resource delivery
		container.contextPut("chelpStaticDirUrl", chelpStaticDirUrl);
		return container;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == contextHelpPageVC) {
			// Must be a link to another help page, add to crumb path. 
			String subPage = event.getCommand();
			String bundleParam = ureq.getParameter(VelocityRenderDecorator.PARAM_CHELP_BUNDLE);
			if (bundleParam == null) bundleParam = bundleName;
			// Add new crumb controller now. Old one is disposed automatically
			ContextHelpPageCrumbController pageCrumController = new ContextHelpPageCrumbController(ureq, getWindowControl(), bundleParam, subPage, getLocale());
			activateAndListenToChildCrumbController(pageCrumController);			
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	protected void doDispose() {
		// controllers and mapper autodisposed by basic controller
	}

	/**
	 * @see org.olat.core.gui.control.generic.breadcrumb.CrumbBasicController#getCrumbLinkHooverText()
	 */
	public String getCrumbLinkHooverText() {
		return translate("contexthelp.crump.hover.prefix") + ": " + getCrumbLinkText();
	}

	/**
	 * @see org.olat.core.gui.control.generic.breadcrumb.CrumbBasicController#getCrumbLinkText()
	 */
	public String getCrumbLinkText() {
		return pageTranslator.translate("chelp." + page.split("\\.")[0] + ".title");
	}

	/**
	 * Change the locale for this view
	 * @param locale
	 * @param ureq
	 */
	public void setLocale(Locale locale, UserRequest ureq) {
		// Update wrapper container translator and for subsequent requests
		setLocale(locale, true);
		// Update page translator
		pageTranslator.setLocale(locale);
		// Re-translate page
		addPageInfos();
		addPageRating(ureq);
		// Update next crumb in chain
		ContextHelpPageCrumbController child = (ContextHelpPageCrumbController) getChildCrumbController();
		if (child != null) {
			child.setLocale(locale, ureq);
		}
	}
	
}
