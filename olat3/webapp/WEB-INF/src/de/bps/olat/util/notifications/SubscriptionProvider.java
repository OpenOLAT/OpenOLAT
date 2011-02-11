/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2009 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.olat.util.notifications;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.notifications.ContextualSubscriptionController;
import org.olat.core.util.notifications.SubscriptionContext;

/**
 * 
 * Description:<br>
 * Managed different subscription sources.
 * 
 * <P>
 * Initial Date:  29.04.2009 <br>
 * @author bja
 */
public interface SubscriptionProvider {
	public SubscriptionContext getSubscriptionContext();
	
	public ContextualSubscriptionController getContextualSubscriptionController(UserRequest ureq, WindowControl wControl);
}
