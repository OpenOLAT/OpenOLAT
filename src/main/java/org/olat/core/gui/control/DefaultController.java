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

package org.olat.core.gui.control;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ILoggingResourceable;
import org.olat.core.logging.activity.IUserActivityLogger;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.logging.activity.UserActivityLoggerImpl;
import org.olat.core.util.RunnableWithException;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.session.UserSessionManager;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public abstract class DefaultController implements Controller, ControllerEventListener {
	private static final String DEFAULTDISPOSED_PAGE = "defaultdisposed";
	private static final Logger log = Tracing.createLoggerFor(DefaultController.class);
	// for memory watch only.
	private static AtomicInteger controllerCnt = new AtomicInteger(0);
	private final Object DISPOSE_LOCK = new Object();
	
	private List<ControllerEventListener> listeners;
	private Component initialComponent;
	private boolean disposed = false;
	private StackedPanel wrapperPanel;
	private IUserActivityLogger userActivityLogger;
	
	private WindowControl newWControl;
	
	private Controller disposedMessageController;
	private Locale locale;
	
	/**
	 * for debugging / statistical information only!<br>
	 * 
	 * @return the number of controllers which are initialized but not yet disposed (disposed by state, not necessarily by the GC yet)
	 */
	public static int getControllerCount() {
		return controllerCnt.get();
	}
	
	/**
	 *
	 */
	@SuppressWarnings("unused")
	private DefaultController() {
		// prevent instantation
		
		// have to set userActivityLogger to null to comply with the fact that it's final
		userActivityLogger = null;
	}
	
	/**
	 * 
	 * @param wControl should always be provided except for basechiefcontroller etc. which provide their own impl. of a windowcontrol
	 */
	protected DefaultController(WindowControl wControl) {
		controllerCnt.incrementAndGet();
		CoreSpringFactory.autowireObject(this);
		
		// set the ThreadLocalUserActivityLogger
		this.userActivityLogger = UserActivityLoggerImpl.setupLoggerForController(wControl);
		
		// wControl may be null, e.g. for DefaultChiefController. 
		// normal controllers should provide a windowcontrol, even though they may not need it
		if (wControl != null) {
			this.newWControl = new LocalWindowControl(wControl,this);
		}
		
	}
	
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	/**
	 * do NOT use normally. use the constructor super(wControl). only used for classes which are loaded by Class.forName and need an empty contstructor
	 * @param wControl not null
	 */
	protected void setOrigWControl(WindowControl wControl) {
		if (wControl == null) throw new AssertException("can not accept a null Windowcontrol here");
		this.newWControl = new LocalWindowControl(wControl,this);
	}
	
	protected void overrideWindowControl(WindowControl wControl) {
		this.newWControl = wControl;
	}
		
	/**
	 * 
	 * @return the windowcontrol for this controller
	 */
	protected WindowControl getWindowControl() {
		if (newWControl == null) {
			throw new AssertException("no windowcontrol set!");
		}
		return newWControl;
	}
	
	protected Window getWindow() {
		if (newWControl == null) {
			return null;
		}
		return newWControl.getWindowBackOffice().getWindow();
	}

	@Override
	public WindowControl getWindowControlForDebug() {
		return getWindowControl();
	}

	@Override
	public void addControllerListener(ControllerEventListener el) {
		if (listeners == null) {
			listeners = new ArrayList<>();
		}
		if (!listeners.contains(el)) {
			listeners.add(el);
		}
	}

	@Override
	public void removeControllerListener(ControllerEventListener el) {
		if(listeners != null) {
			listeners.remove(el);
		}
	}

	@Override
	public boolean isControllerListeningTo(ControllerEventListener el) {
		return listeners != null && listeners.contains(el);
	}

	// brasato:: prio c : clean up classes using this - does not make sense really
	protected List<ControllerEventListener> getListeners() {
		return listeners;
	}

	/**
	 * fires events to registered listeners of controller events.
	 * To see all events set this class and also AbstractEventBus and Component to debug.
	 * @param event
	 * @param ores
	 */
	protected void fireEvent(UserRequest ureq, Event event) {
		if (listeners != null && listeners.size() > 0) {
			ControllerEventListener[] listenerArr = listeners.toArray(new ControllerEventListener[listeners.size()]);
			for (ControllerEventListener listener:listenerArr) {
				if (log.isDebugEnabled()) log.debug("Controller event: "+this.getClass().getName()+": fires event to: "+listener.getClass().getName());
				listener.dispatchEvent(ureq, this, event);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.Controller#dispatchEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 * Note: This method is final to make sure no subclass fiddles with this - core - framework method. As
	 *       the framework part includes setting up ThreadLocalUserActivityLogger etc
	 */
	@Override
	public final void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if (!disposed) {
			event(ureq, source, event);			
		} else {
			// COMMMENT:2008-02-28:pb: reviewed 'little hack' which is not a hack.
			// The introduced setDisposedMsgController allows the surrounding Controller
			// to set a specific controller for handling the disposed case. This 
			// specific controller always has the correct language.
			// 
			// The fall back is to present a DisposedMessage with the locale catched
			// here. Typically this is either because the programmer forgot to set
			// a specific disposed message where it is needed, or it is just a place
			// where it was not expected. And most of the times this indicates a
			// legacy or bad designed work flow.
			// 
			// :::Original comment:::
			// since this abstract controller does not know the
			// locale of the current user, we catch it here upon dispatching
			// if a controller gets disposed asynchronously before it has been
			// dispatched, we use the default locale
			// :::----------------:::
			if (locale == null) {
				locale = ureq.getLocale();
			}
			
			//show message
			if(disposedMessageController != null && wrapperPanel != null){
				wrapperPanel.setContent(disposedMessageController.getInitialComponent());
			}else if(wrapperPanel != null){
				// place disposed message
				Translator pT = Util.createPackageTranslator(DefaultController.class, locale);
				Component dispMsgVC = new VelocityContainer(DEFAULTDISPOSED_PAGE,DefaultController.class,DEFAULTDISPOSED_PAGE,pT,null);
				wrapperPanel.pushContent(dispMsgVC);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.ControllerEventListener#dispatchEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void dispatchEvent(final UserRequest ureq, final Controller source, final Event event) {
		if (!disposed) {
			getUserActivityLogger().frameworkSetBusinessPathFromWindowControl(getWindowControl());
			ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLogger(new Runnable() {

				@Override
				public void run() {
					event(ureq, source, event);
				}
				
			}, getUserActivityLogger());
		}else{
			//show message
			if(disposedMessageController != null && wrapperPanel != null){
				wrapperPanel.setContent(disposedMessageController.getInitialComponent());
			}else if(wrapperPanel != null){
				if (locale == null) {
					locale = ureq.getLocale();
				}
				// place disposed message
				Translator pT = Util.createPackageTranslator(DefaultController.class, locale);
				Component dispMsgVC = new VelocityContainer(DEFAULTDISPOSED_PAGE, DefaultController.class,DEFAULTDISPOSED_PAGE, pT, null);
				wrapperPanel.pushContent(dispMsgVC);
			}
		}
	}

	/**
	 * the only method of the interface controllereventlistener. always gets
	 * called when a controller we 'subscribed' to fires an event. we provide a
	 * default implementation here since there are many controllers which are
	 * standalone and need no subcontrollers
	 * 
	 * @param ureq
	 * @param source
	 * @param event
	 */
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq,Controller source, Event event) {
		// default impl does nothing
	}

	/**
	 * abstract event method for subclasses. the event received from the component
	 * we are listening to are always rerouted to this method here, except when
	 * the component has been disposed, in which case the events are simply
	 * ignored.
	 * @param ureq
	 * @param source
	 * @param event
	 */
	protected abstract void event(UserRequest ureq, Component source, Event event);

	/**
	 * Sets the initialComponent.
	 * 
	 * @param initialComponent The mainComponent to set
	 */
	protected void setInitialComponent(Component initialComponent) {
		if (this.initialComponent != null) throw new AssertException("can only set initialcomponent once! comp:"
				+ initialComponent.getComponentName() + ", controller: " + toString());
		// wrap a panel around the initial component which is used when this
		// controller is disposed: after having called doDispose on subclasses
		// which clean up their subcontrollers by calling dispose (and therefore
		// also (must) cleanup the gui stack to the level our initial component
		// is at), we put a generic message in the panel that the user sees this
		// message upon the next click (which will not be dispatched, since the
		// component does not exist anymore,
		// but it will just be rerendered.
		// we also take care that no event is deliverd to implementors of this
		// abstract class after this controller has been disposed
		
		if (initialComponent instanceof StackedPanel) {
			wrapperPanel = (StackedPanel) initialComponent;
		} else {
			wrapperPanel = new SimpleStackedPanel("autowrapper of controller " + this.getClass().getName());
			wrapperPanel.setContent(initialComponent);
		}
		this.initialComponent = wrapperPanel;
	}

	/**
	 * @return Component
	 */
	@Override
	public Component getInitialComponent() {
		return initialComponent;
	}
	
	/**
	 * Sets the UserRequest on this Controller's IUserActivityLogger.
	 * <p>
	 * The actual action is to set the session.
	 * @param req the UserRequest from which the session is fetched
	 */
	protected void setLoggingUserRequest(UserRequest req) {
		IUserActivityLogger logger = getUserActivityLogger();
		if (logger==null) {
			// logger is never null - guaranteed. 
			// throw this in the unlikely odd still
			throw new IllegalStateException("no logger set");
		}
		logger.frameworkSetSession(CoreSpringFactory.getImpl(UserSessionManager.class).getUserSessionIfAlreadySet(req.getHttpReq()));
	}
	
	/**
	 * Add a LoggingResourceable (e.g. a wrapped Course or a wrapped name of a CP file) to this
	 * Controller's IUserActivityLogger.
	 * <p>
	 * This method is usually called in the constructor of a Controller - in rarer cases
	 * it can be called outside constructors as well.
	 * @param loggingResourceable the loggingResourceable to be set on this Controller's 
	 * IUserActivityLogger
	 */
	public void addLoggingResourceable(ILoggingResourceable loggingResourceable) {
		IUserActivityLogger logger = getUserActivityLogger();
		if (logger==null) {
			// logger is never null - guaranteed. 
			// throw this in the unlikely odd still
			throw new IllegalStateException("no logger set");
		}
		logger.addLoggingResourceInfo(loggingResourceable);
	}
	
	/**
	 * FRAMEWORK USE ONLY!
	 * <p>
	 * Returns the UserActivityLogger of this controller or null if no logger is set yet.
	 * <p>
	 * @return UserActivityLogger of this controller or null if no logger is set
	 */
	@Override
	public IUserActivityLogger getUserActivityLogger() {
		return this.userActivityLogger;
	}

	/**
	 * Controller should override the method doDispose() instead of this one.
	 * makes sure that doDispose is only called once.
	 * 
	 * @param asynchronous if true, then this method is invoked by a different
	 *          thread than the current user-gui-thread ("mouse-click-thread").
	 *          this means if set to true, then you should inform the user by
	 *          replacing the current render subtree of your controller's
	 *          component with e.g. a velocitycontainer stating a message like
	 *          'this object has been disposed by an other process/user. please
	 *          click some other link to continue...
	 */
	@Override
	public synchronized void dispose() { //o_clusterOK by:fj
		// protect setting disposed to true by synchronized block
		synchronized (DISPOSE_LOCK) {// o_clusterok
			if (disposed) {
				return;
			} else {
				disposed = true; // disable any further event dispatching
			}
		}
		// dispose the controller now
		if (log.isDebugEnabled()) {
			log.debug("now disposing controller: " + this.toString());
		}
		
		try {
			ThreadLocalUserActivityLoggerInstaller.runWithUserActivityLoggerWithException(new RunnableWithException() {
				@Override
				public void run() throws Exception {
						doPreDispose();
						doDispose();
				}
			}, getUserActivityLogger());
		}
		catch (Exception e) {
			log.error("error while disposing controller: "+this.getClass().getName(), e);
		}
		if (log.isDebugEnabled()){
			log.debug("end of: " + this.toString());
		}
		//FIXME:pb 2008-04-16 provide a default message controller without always create one?!
		//how much memory is used here
		if(disposedMessageController!=null && wrapperPanel != null){
			wrapperPanel.setContent(disposedMessageController.getInitialComponent());
		}else if (wrapperPanel != null){
			if (locale == null) {
				//fallback to default locale
				locale = I18nModule.getDefaultLocale();
			}
			// place disposed message
			Translator pT = Util.createPackageTranslator(DefaultController.class, locale);
			Component dispMsgVC = new VelocityContainer(DEFAULTDISPOSED_PAGE,DefaultController.class,DEFAULTDISPOSED_PAGE,pT,null);
			wrapperPanel.pushContent(dispMsgVC);
		}
			
			
		controllerCnt.decrementAndGet();//count controller count down. this should event work if a disposed msg controller is created.
			
	}

	/**
	 * for classes like basiccontroller to get a hook for disposing without using doDispose()
	 *
	 */
	protected void doPreDispose() {
		// default impl does nothing
	}
	
	/**
	 * to be implemented by the concrete controllers to dispose resources, locks, subcontrollers, and so on
	 *
	 */
	protected void doDispose() {
		// default implementation does nothing
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("cListener:");
		if (listeners == null) {
			sb.append("-");
		} else {
			for (Iterator<ControllerEventListener> it_cl = listeners.iterator(); it_cl.hasNext();) {
				ControllerEventListener cev = it_cl.next();
				String ins = cev.getClass().getName(); //toString(); not cool when
				// controllers are listening to each other in a circle (should
				// never be, but then debug mode crashes because of infinite recursion.
				sb.append(ins).append(", ");
			}
		}
		return this.getClass().getName()+" [" + sb + "]";
	}

	protected WindowControl addToHistory(UserRequest ureq) {
		BusinessControlFactory.getInstance().addToHistory(ureq, getWindowControl());
		return getWindowControl();
	}
	
	protected WindowControl addToHistory(UserRequest ureq, Controller controller) {
		WindowControl wControl = null;
		if(controller instanceof DefaultController) {
			wControl = ((DefaultController)controller).getWindowControl();
		} else if(controller != null) {
			wControl = controller.getWindowControlForDebug();
		}	
		BusinessControlFactory.getInstance().addToHistory(ureq, wControl);
		return wControl;
	}
	
	protected WindowControl addToHistory(UserRequest ureq, WindowControl wControl) {
		BusinessControlFactory.getInstance().addToHistory(ureq, wControl);
		return wControl;
	}
	
	protected WindowControl addToHistory(UserRequest ureq, OLATResourceable ores, StateEntry stateEntry) {
		return addToHistory(ureq, ores, stateEntry, getWindowControl(), true);
	}
	
	protected WindowControl addToHistory(UserRequest ureq, OLATResourceable ores, StateEntry stateEntry, WindowControl wControl, boolean addToHistory) {
		return BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, stateEntry, wControl, addToHistory);
	}
	
	protected WindowControl addToHistory(UserRequest ureq, StateEntry stateEntry) {
		ContextEntry currentEntry = getWindowControl().getBusinessControl().getCurrentContextEntry();
		if(currentEntry != null) {
			currentEntry.setTransientState(stateEntry);
		}
		return addToHistory(ureq, this);
	}
	
	protected void removeHistory(UserRequest ureq) {
		BusinessControlFactory.getInstance().removeFromHistory(ureq, getWindowControl());
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.Controller#isDisposed()
	 */
	@Override
	public boolean isDisposed() {
		return disposed;
	}
	
	/**
	 * register a controller creator which is used in case the controller was 
	 * disposed and a specific message should be displayed.
	 * @param disposeMsgControllerCreator
	 */
	protected void setDisposedMsgController(Controller disposeMsgController) {
		disposedMessageController = disposeMsgController;
		controllerCnt.decrementAndGet();
	}
}