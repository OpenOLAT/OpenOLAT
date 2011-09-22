package org.olat.modules.fo;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Description:<br>
 * MultiUserEvent fired at close/open or hide/show forum thread, 
 * or add/delete thread.
 * 
 * <P>
 * Initial Date:  09.07.2009 <br>
 * @author Lavinia Dumitrescu
 */
public class ForumChangedEvent extends MultiUserEvent {

	public ForumChangedEvent(String command) {
		super(command);		
	}	
}
