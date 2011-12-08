package org.olat.core.util.mail.ui;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for MailContextResolver
 * 
 * <P>
 * Initial Date:  30 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface MailContextResolver {
	
	public String getName(String businessPath, Locale locale);
	
	public void open(UserRequest ureq, WindowControl wControl, String url);

}
