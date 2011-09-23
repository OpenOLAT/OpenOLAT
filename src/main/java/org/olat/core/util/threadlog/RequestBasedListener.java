package org.olat.core.util.threadlog;

import org.olat.core.gui.control.Event;
import org.olat.core.util.event.GenericEventListener;

/**
 * Listener passed on to the PersistentProperties for the
 * RequestBasedLogLevelManager.
 * <p>
 * The reason for having this as a separate class is simply
 * the fact that OlatResourceable names are capped in terms
 * of length
 * <P>
 * Initial Date:  13.09.2010 <br>
 * @author Stefan
 */
public class RequestBasedListener implements GenericEventListener {

	/** the RequestBasedLogLevelManager to which this listener is associated **/ 
	private RequestBasedLogLevelManager manager;
	
	/**
	 * Sets the RequestBasedLogLevelManager to which this listener is associated.
	 * <p>
	 * Used by spring.
	 * @param manager the RequestBasedLogLevelManager to which this listener is associated.
	 */
	public void setManager(RequestBasedLogLevelManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void event(@SuppressWarnings("unused") Event event) {
		if (manager!=null) manager.init();
	}

}
