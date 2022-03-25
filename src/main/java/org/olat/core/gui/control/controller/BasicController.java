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
* <p>
*/ 
package org.olat.core.gui.control.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.ModalController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;

/**
 * Description:<br>
 * BasicController is a controller which serves as convenient superclass for
 * controllers. it offers things like easy access to locale, identity, and
 * generation of velocitypages.<br>
 * New added methods must have visibility <code>protected</code>
 * <P>
 * Initial Date: 15.12.2006 <br>
 * 
 * @author Felix Jost, www.goodsolutions.ch
 */
public abstract class BasicController extends DefaultController {

	protected String velocity_root;
	private final Identity identity;
	private Translator translator;
	private Translator fallbackTranslator;
	private final Logger logger;

	private List<MapperKey> mapperKeys;
	private List<Controller> childControllers;

	/**
	 * easy to use controller template. Extending the BasicController allows to
	 * create velocity pages and translate keys without cumbersome creation of
	 * the corresponding objects.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	protected BasicController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null);
	}
	
	/**
	 * 
	 * brasato:::: omit - move to setTranslator()
	 * 
	 * profit from the easy of use, but have the flexibility of using a
	 * translator chain. As a main translator this packages translator is used
	 * and then fallbacked to the provided fallbacktranslator.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param fallBackTranslator
	 */
	protected BasicController(UserRequest ureq, WindowControl wControl,
			Translator fallBackTranslator) {
		super(wControl);
		setLocale(ureq.getLocale());
		identity = ureq.getIdentity();

		Class<?> cl = this.getClass();
		fallbackTranslator = fallBackTranslator;
		translator = Util.createPackageTranslator(cl, getLocale(), fallBackTranslator);
		velocity_root = Util.getPackageVelocityRoot(cl);
		logger = Tracing.createLoggerFor(cl);
	}

	@Override
	protected void doPreDispose() {
		// deregister all mappers if needed
		if (mapperKeys != null) {
			CoreSpringFactory.getImpl(MapperService.class).cleanUp(mapperKeys);
			mapperKeys.clear();
			mapperKeys = null;
		}

		// dispose child controller if needed
		if (childControllers != null) {
			for (Controller c : childControllers) {
				c.dispose();
			}
		}
	}

	/**
	 * brasato:: do some code examples
	 * 
	 * @param controller
	 * @return the same instance of the controller - used for easy and compact
	 *         code
	 * @throws AssertException
	 *             if the controller to be added is already contained.
	 */
	protected Controller listenTo(Controller controller) {
		controller.addControllerListener(this);
		if (childControllers == null) {
			childControllers = new ArrayList<>(4);
		}
		childControllers.add(controller);
		return controller;
	}

	/**
	 * Remove this from the given controller as listener and dispose the
	 * controller. This should only be used for controllers that have previously
	 * been added with the listenTo() method.
	 * 
	 * @param controller
	 *            the controller to be disposed. When controller is NULL the
	 *            method returns doing nothing, no exception will be thrown.
	 * @throws AssertException
	 *             if the controller is not contained.
	 */

	protected void removeAsListenerAndDispose(Controller controller) {
		if (controller != null) {
			if(childControllers != null) {
				childControllers.remove(controller);
			}
			controller.removeControllerListener(this);
			controller.dispose();
		}
	}
	
	protected boolean guardModalController(Controller controller) {
		return controller != null && !controller.isDisposed();
	}
	
	protected void removeModalControllers() {
		if(childControllers != null) {
			List<Controller> copyList = new ArrayList<>(childControllers);
			for(Controller controller:copyList) {
				if(controller instanceof ModalController) {
					ModalController modalController = (ModalController)controller;
					if(modalController.isCloseable()) {
						if(controller instanceof CloseableModalController) {
							((CloseableModalController)controller).deactivate();
							removeAsListenerAndDispose(controller);
						} else if(controller instanceof CloseableCalloutWindowController) {
							((CloseableCalloutWindowController)controller).deactivate();
							removeAsListenerAndDispose(controller);
						} else if(controller instanceof StepsMainRunController) {
							removeAsListenerAndDispose(controller);
						}
					}
				} else if(controller instanceof BasicController) {
					((BasicController)controller).removeModalControllers();
				}
			}
		}
	}

	/**
	 * convenience method: registers a mapper which will be automatically
	 * deregistered upon dispose of the controller
	 * 
	 * @param ureq The user request object
	 * @param m
	 *            the mapper that delivers the resources
	 * @return The mapper base URL
	 */

	protected String registerMapper(UserRequest ureq, Mapper m) {
		return registerCacheableMapper(ureq, null, m);
	}

	/**
	 * convenience method: registers a cacheable mapper which will be
	 * automatically deregistered upon dispose of the controller
	 * 
	 * @param ureq The user request object
	 * 
	 * @param cacheableMapperID
	 *            the mapper ID that is used in the url to identify this mapper.
	 *            Should be something that is derived from the context or
	 *            resource that is delivered by the mapper
	 * @param m
	 *            the mapper that delivers the resources
	 * @return The mapper base URL
	 */
	protected String registerCacheableMapper(UserRequest ureq, String cacheableMapperID, Mapper m) {
		return registerCacheableMapper(ureq, cacheableMapperID, m, -1);
	}
	
	/**
	 * Convenience method: registers a cacheable mapper which will be
	 * automatically deregistered upon dispose of the controller
	 * @param ureq
	 * @param cacheableMapperID
	 * @param m The mapper
	 * @param expirationTime -1 is the default bevahiour, else is expiration time in seconds
	 * @return
	 */
	protected String registerCacheableMapper(UserRequest ureq, String cacheableMapperID, Mapper m, int expirationTime) {
		if (mapperKeys == null) {
			mapperKeys = new ArrayList<>(2);
		}
		MapperKey mapperBaseKey;
		UserSession usess = ureq == null ? null : ureq.getUserSession();
		if (cacheableMapperID == null) {
			// use non cacheable as fallback
			mapperBaseKey = CoreSpringFactory.getImpl(MapperService.class).register(usess, m);			
		} else {
			mapperBaseKey = CoreSpringFactory.getImpl(MapperService.class).register(usess, cacheableMapperID, m, expirationTime);
		}
		// registration was successful, add to our mapper list
		mapperKeys.add(mapperBaseKey);
		return mapperBaseKey.getUrl();
	}

	/**
	 * convenience method to generate a velocitycontainer
	 * 
	 * @param page
	 *            the velocity page to use in the _content folder, e.g. "index",
	 *            or "edit". The suffix ".html" gets automatically added to the
	 *            page name e.g. "index.html".
	 */
	protected VelocityContainer createVelocityContainer(String page) {
		return createVelocityContainer(null, page);
	}
	
	/**
	 * convenience method to generate a velocitycontainer
	 * 
	 * @param id The identifier
	 * @param page
	 *            the velocity page to use in the _content folder, e.g. "index",
	 *            or "edit". The suffix ".html" gets automatically added to the
	 *            page name e.g. "index.html".
	 */
	protected VelocityContainer createVelocityContainer(String id, String page) {
		return new VelocityContainer(id, "vc_" + page, velocity_root + "/" + page
				+ ".html", translator, this);
	}

	protected StackedPanel putInitialPanel(Component initialContent) {
		if(initialContent instanceof StackedPanel) {
			super.setInitialComponent(initialContent);
			return (StackedPanel)initialContent;
		} else {
			StackedPanel p = new SimpleStackedPanel("mainBasicPanel");
			p.setContent(initialContent);
			super.setInitialComponent(p);
			return p;
		}
	}

	@Override
	protected void setInitialComponent(Component initialComponent) {
		throw new AssertException("please use method putInitialPanel!");
	}

	/**
	 * creates and activates a dialog with YES / NO Buttons. usage:<br>
	 * Do not call it from within a controllers constructor.<br>
	 * call it as last method call of a workflow.<br>
	 * <code>dialogBoxOne = createYesNoDialog(ureq, "Hello World", "Lorem ipsum dolor sit amet, sodales?", dialogBoxOne);</code>
	 * where the <code>dialogBoxOne</code> is provided as parameter to be
	 * cleaned up correctly, e.g. removed as listener and disposed. The returned
	 * DialogBoxController should be hold as instance variable for two reasons:
	 * <ul>
	 * <li>in the <code>event</code> method if then else block like
	 * <code>source == dialogBoxOne</code></li>
	 * <li>to be cleaned up correctly if the "same" dialog box is showed again</li>
	 * </ul>
	 * <p>
	 * 
	 * @param ureq
	 * @param title
	 * @param text
	 * @param dialogCtr
	 * @return
	 */
	protected DialogBoxController activateYesNoDialog(UserRequest ureq,
			String title, String text, DialogBoxController dialogCtr) {
		if (dialogCtr != null) {
			removeAsListenerAndDispose(dialogCtr);
		}
		dialogCtr = DialogBoxUIFactory.createYesNoDialog(ureq,
				getWindowControl(), title, text);
		listenTo(dialogCtr);
		dialogCtr.activate();
		return dialogCtr;
	}

	/**
	 * creates and activates a dialog with OK / CANCEL Buttons. usage:<br>
	 * Do not call it from within a controllers constructor.<br>
	 * call it as last method call of a workflow.<br>
	 * <code>dialogBoxOne = createYesNoDialog(ureq, "Hello World", "Lorem ipsum dolor sit amet, sodales?", dialogBoxOne);</code>
	 * where the <code>dialogBoxOne</code> is provided as parameter to be
	 * cleaned up correctly, e.g. removed as listener and disposed. The returned
	 * DialogBoxController should be hold as instance variable for two reasons:
	 * <ul>
	 * <li>in the <code>event</code> method if then else block like
	 * <code>source == dialogBoxOne</code></li>
	 * <li>to be cleaned up correctly if the "same" dialog box is showed again</li>
	 * </ul>
	 * 
	 * @param ureq
	 * @param title
	 * @param text
	 * @param dialogCtr
	 * @return
	 */
	protected DialogBoxController activateOkCancelDialog(UserRequest ureq,
			String title, String text, DialogBoxController dialogCtr) {
		if (dialogCtr != null) {
			removeAsListenerAndDispose(dialogCtr);
		}
		dialogCtr = DialogBoxUIFactory.createOkCancelDialog(ureq,
				getWindowControl(), title, text);
		listenTo(dialogCtr);
		dialogCtr.activate();
		return dialogCtr;
	}

	/**
	 * creates and activates a dialog with buttons provided in the
	 * <code>buttons</code> list parameter. usage:<br>
	 * Do not call it from within a controllers constructor.<br>
	 * call it as last method call of a workflow.<br>
	 * <code>dialogBoxOne = createYesNoDialog(ureq, "Hello World", "Lorem ipsum dolor sit amet, sodales?", buttonLabelList, dialogBoxOne);</code>
	 * where the <code>dialogBoxOne</code> is provided as parameter to be
	 * cleaned up correctly, e.g. removed as listener and disposed. The returned
	 * DialogBoxController should be hold as instance variable for two reasons:
	 * <ul>
	 * <li>in the <code>event</code> method if then else block like
	 * <code>source == dialogBoxOne</code></li>
	 * <li>to be cleaned up correctly if the "same" dialog box is showed again</li>
	 * </ul>
	 * 
	 * @param ureq
	 * @param title
	 * @param text
	 * @param buttonLabels
	 * @param dialogCtr
	 * @return
	 */
	protected DialogBoxController activateGenericDialog(UserRequest ureq,
			String title, String text, List<String> buttonLabels,
			DialogBoxController dialogCtr) {
		if (dialogCtr != null) {
			removeAsListenerAndDispose(dialogCtr);
		}
		dialogCtr = DialogBoxUIFactory.createGenericDialog(ureq,
				getWindowControl(), title, text, buttonLabels);
		listenTo(dialogCtr);
		dialogCtr.activate();
		return dialogCtr;
	}

	/**
	 * if you want to open a new browser window with content as defined with the
	 * ControllerCreator parameter. The new PopupBrowserWindow is created and
	 * opened. This should be the last call in a method, make this clear by
	 * return immediate after the openInNewWindow line.<br>
	 * 
	 * @param ureq
	 * @param windowContentCreator
	 *            a creator which is used to create the windows content
	 */
	protected PopupBrowserWindow openInNewBrowserWindow(UserRequest ureq,
			ControllerCreator windowContentCreator, boolean forPrint) {
		// open in new browser window
		PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice()
				.getWindowManager().createNewPopupBrowserWindowFor(ureq,
						windowContentCreator);
		pbw.setForPrint(forPrint);
		pbw.open(ureq);
		return pbw;
	}

	/**
	 * @return Returns the identity.
	 */
	protected Identity getIdentity() {
		return identity;
	}

	/**
	 * @return Returns the translator.
	 */
	protected Translator getTranslator() {
		return translator;
	}

	/**
	 * convenience method to inform the user. this will call
	 * 
	 * <pre>
	 * getWindowControl().setInfo(getTranslator().translate(key));
	 * </pre>
	 * 
	 * @param key
	 *            the key to use (in the LocalStrings_curlanguage file of your
	 *            controller)
	 */
	protected void showInfo(String key) {
		getWindowControl().setInfo(getTranslator().translate(key));
	}

	/**
	 * convenience method to inform the user. this will call
	 * 
	 * <pre>
	 * getWindowControl()
	 * 		.setInfo(getTranslator().translate(key, new String[] { arg }));
	 * </pre>
	 * 
	 * @param key
	 *            the key to use (in the LocalStrings_curlanguage file of your
	 *            controller)
	 * @param arg
	 */
	protected void showInfo(String key, String arg) {
		getWindowControl().setInfo(
				getTranslator().translate(key, new String[] { arg }));
	}
	
	/**
	 * convenience method to inform the user. this will call
	 * 
	 * <pre>
	 * getWindowControl().setInfo(getTranslator().translate(key, args));
	 * </pre>
	 * 
	 * @param key
	 *            the key to use (in the LocalStrings_curlanguage file of your
	 *            controller)
	 * @param args
	 */
	protected void showInfo(String key, String[] args) {
		getWindowControl().setInfo(getTranslator().translate(key, args));
	}

	/**
	 * convenience method to inform the user with a warning message. this will
	 * call
	 * 
	 * <pre>
	 * getWindowControl().setWarning(
	 * 		getTranslator().translate(key, new String[] { arg }));
	 * </pre>
	 * 
	 * @param key
	 *            the key to use (in the LocalStrings_curlanguage file of your
	 *            controller)
	 */
	protected void showWarning(String key) {
		getWindowControl().setWarning(getTranslator().translate(key));
	}

	/**
	 * convenience method to inform the user with a warning message. this will
	 * call
	 * 
	 * <pre>
	 * getWindowControl().setWarning(
	 * 		getTranslator().translate(key, new String[] { arg }));
	 * </pre>
	 * 
	 * @param key
	 *            the key to use (in the LocalStrings_curlanguage file of your
	 *            controller)
	 * @param arg
	 */
	protected void showWarning(String key, String arg) {
		getWindowControl().setWarning(
				getTranslator().translate(key, new String[] { arg }));
	}
	
	protected void showWarning(String key, String[] args) {
		getWindowControl().setWarning(
				getTranslator().translate(key, args));
	}

	/**
	 * convenience method to send an error msg to the user. this will call
	 * 
	 * <pre>
	 * getWindowControl().setError(getTranslator().translate(key));
	 * </pre>
	 * 
	 * @param key
	 *            the key to use (in the LocalStrings_curlanguage file of your
	 *            controller)
	 */
	protected void showError(String key) {
		getWindowControl().setError(getTranslator().translate(key));
	}

	/**
	 * convenience method to send an error msg to the user. this will call
	 * 
	 * <pre>
	 * getWindowControl().setError(
	 * 		getTranslator().translate(key, new String[] { arg }));
	 * </pre>
	 * 
	 * @param key
	 *            the key to use (in the LocalStrings_curlanguage file of your
	 *            controller)
	 * @param arg
	 */
	protected void showError(String key, String arg) {
		getWindowControl().setError(
				getTranslator().translate(key, new String[] { arg }));
	}
	
	protected void showError(String key, String[] args) {
		getWindowControl().setError(
				getTranslator().translate(key, args));
	}

	/**
	 * convenience method to translate with the built-in package translator of
	 * the controller
	 * 
	 * @param key
	 *            the key to translate
	 * @return the translated string
	 */
	protected String translate(String key) {
		return getTranslator().translate(key);
	}
	
	/**
	 * convenience method to translate with the built-in package translator of
	 * the controller
	 * 
	 * @param key The key to translate
	 * @param args Optional strings to insert into the translated string
	 * @return The translated string
	 */
	protected String translate(String key, String... args) {
		return getTranslator().translate(key, args);
	}

	/**
	 * provide your custom translator here if needed.<br>
	 * <i>Hint</i><br>
	 * try to avoid using this - and if, comment why.
	 * 
	 * @param translator
	 */
	protected void setTranslator(Translator translator) {
		this.translator = translator;
	}

	/**
	 * provide your custom velocity root here if needed<br>
	 * <i>Hint</i><br>
	 * try to avoid using this - and if, comment why.
	 * 
	 * @param velocityRoot
	 */
	protected void setVelocityRoot(String velocityRoot) {
		this.velocity_root = velocityRoot;
	}

	/**
	 * override the default base package, this is where all velocity pages and
	 * translations come from, etc. <br>
	 * <i>Hint</i><br>
	 * try to avoid using this - and if, comment why.
	 */
	protected void setBasePackage(Class<?> clazz) {
		setVelocityRoot(Util.getPackageVelocityRoot(clazz));
		if (fallbackTranslator != null) {
			setTranslator(Util.createPackageTranslator(clazz, getLocale(), fallbackTranslator));
		} else {
			setTranslator(Util.createPackageTranslator(clazz, getLocale()));			
		}
	}

	/**
	 * override the default locale.
	 * 
	 * @param locale The new locale
	 * @param setLocaleOnTranslator true: the new locale is applied to the
	 *          translator; false: the new locale is not applied to the translator
	 */
	protected void setLocale(Locale locale, boolean setLocaleOnTranslator) {
		setLocale(locale);
		if (setLocaleOnTranslator) {
			getTranslator().setLocale(locale);
		}
	}
	
	/**
	 * Returns a logger for this class to do advanced logging 
	 * <br>
	 * <i>Hint</i><br>
	 * You can also use the logDebug etc methods
	 * 
	 * @return logger
	 */
	protected Logger getLogger() {
		return logger;
	}

	/**
	 * Log an error message to the system log file. An error is when something
	 * unexpected happened that could not be resolved and as a result the
	 * current workflow has to be terminated.
	 * <p>
	 * Alternatively it is possible to throw an OLATRuntimeException if your
	 * code does not present a special alternative GUI path for the user.
	 * <p>
	 * If you can, log the error and present a useful screen to the user.
	 * 
	 * @param logMsg
	 *            Human readable log message
	 * @param cause
	 *            stack trace or NULL
	 */
	protected void logError(String logMsg, Throwable cause) {
		getLogger().error(logMsg, cause);
	}

	/**
	 * Log a warn message to the system log file. A warn message should be used
	 * when something unexpected happened but the system somehow can deal with
	 * it. e.g by using default values.
	 * 
	 * @param logMsg
	 *            Human readable log message
	 * @param cause
	 *            stack trace or NULL
	 */
	protected void logWarn(String logMsg, Throwable cause) {
		getLogger().warn(logMsg, cause);
	}

	/**
	 * Log a debug message to the system log file. A debug message is something
	 * that can be useful to get more relevant information in a deployed
	 * system. Thus, think very carefully about which information could be
	 * useful to debug a live system. Be very verbose.
	 * <p>
	 * Debug messages are not meant for debugging while development phase, use
	 * your debugger for this purpose. The only purpose of logDebug is to debug
	 * a live system.
	 * <p>
	 * To prevent many expensive string concatenation in a live system where the
	 * log level for this class is not set to debug you must use logDebug always
	 * in conjunction with isLogDebug():
	 * <code>
	 * if (isLogDebugEnabled()) {
	 *    logDebug("my relevant debugging info");
	 * }
	 * </code>
	 * 
	 * @param logMsg
	 *            Human readable log message
	 */
	protected void logDebug(String logMsg) {
		getLogger().debug(logMsg);
	}

	/**
	 * See logDebug() method
	 * @return true: debug level enabled; false: debug level not enabled
	 */
	protected boolean isLogDebugEnabled() {
		return getLogger().isDebugEnabled();
	}

	/**
	 * Log an info message to the system log file. Info messages are useful to
	 * log configuration settings or very important events to the logfile. Log
	 * only information that is really important to have in the logfile.
	 * <p>
	 * If the information is only useful to debug a problem, you might consider
	 * using logDebug instead
	 * 
	 * @param logMsg
	 *            Human readable log message
	 */
	protected void logInfo(String logMsg) {
		getLogger().info(logMsg);
	}

	/**
	 * Log an audit message to the system log file.	An audit message contains information about a user behavior: a user logged into the system, a user did a critical action. 
	 * @param logMsg
	 *            Human readable log message
	 * @param userObj
	 *            A user object with additional information or NULL
	 */
	protected void logAudit(String logMsg, Object userObj) {
		getLogger().info(Tracing.M_AUDIT, logMsg, userObj);
	}
	
	protected void logAudit(String logMsg) {
		getLogger().info(Tracing.M_AUDIT, logMsg);
	}

}
