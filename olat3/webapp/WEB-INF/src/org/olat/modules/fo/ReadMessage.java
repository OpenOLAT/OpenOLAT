package org.olat.modules.fo;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

/**
 * 
 * Description:<br>
 * TODO: Lavinia Dumitrescu Class Description for ReadMessage
 * 
 * <P>
 * Initial Date:  14.03.2008 <br>
 * @author Lavinia Dumitrescu
 */
public interface ReadMessage extends CreateInfo, Persistable {

	public abstract Identity getIdentity();
	public abstract Forum getForum();	
	public abstract Message getMessage();
	
}
