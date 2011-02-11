package org.olat.admin.sysinfo;

import org.olat.core.gui.components.form.Form;
import org.olat.core.gui.formelements.StaticHTMLTextElement;
import org.olat.core.gui.formelements.TextAreaElement;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.threadlog.RequestBasedLogLevelManager;
import org.olat.core.util.threadlog.UserBasedLogLevelManager;

/**
 * The form used by RequestLoglevelController which simply contains
 * two fields: one for the ip addresses to loglevel/logappenders,
 * the other one for the usernames to loglevel/appenders.
 * <p>
 * The format of the fields is described in RequestBasedLogLevelManager.setLogLevelAndAppender
 * and UserBasedLogLevelManager.setLogLevelAndAppender
 * <P>
 * Initial Date:  13.09.2010 <br>
 * @author Stefan
 * @see RequestBasedLogLevelManager#setLogLevelAndAppender(String)
 * @see UserBasedLogLevelManager#setLogLevelAndAppender(String)
 */
public class RequestLoglevelForm extends Form {

	private TextAreaElement remoteIps;
	private TextAreaElement usernames;

	public RequestLoglevelForm(String name, Translator translator) {
		super(name, translator);
		
		StaticHTMLTextElement ste = new StaticHTMLTextElement("requestloglevel.format.label", translator.translate("requestloglevel.format.text"), 1024);
		addFormElement("requestloglevel.format", ste);
		
		RequestBasedLogLevelManager requestBasedLogLevelManager = RequestBasedLogLevelManager.getInstance();
		if (requestBasedLogLevelManager!=null) {
			remoteIps = new TextAreaElement("requestloglevel.ips", 10, 60, null);
			String ipsAndLevels = requestBasedLogLevelManager.loadIpsAndLevels();
			if (ipsAndLevels!=null) {
				remoteIps.setValue(ipsAndLevels);
			}
			
			addFormElement("requestloglevel.ips", remoteIps);
		}

		UserBasedLogLevelManager userBasedLogLevelManager = UserBasedLogLevelManager.getInstance();
		if (userBasedLogLevelManager!=null) {
			usernames = new TextAreaElement("requestloglevel.usernames", 10, 60, null);
			String usernameAndLevels = userBasedLogLevelManager==null ? null : userBasedLogLevelManager.loadUsernameAndLevels();
			if (usernameAndLevels!=null) {
				usernames.setValue(usernameAndLevels);
			}
			
			addFormElement("requestloglevel.usernames", usernames);
		}
		
		addSubmitKey("save", "save");
	}

	/**
	 * @see org.olat.core.gui.components.form.Form#validate()
	 */
	@Override
	public boolean validate() {
		return true;
	}
	
	String[] getUsernamesAndLevels(){
		String[] retVal = usernames.getValue().split("\r\n");
		return retVal;
	}
	
	String getRawUsernames() {
		return usernames.getValue();
	}
	
	String[] getIpsAndLevels(){
		String[] retVal = remoteIps.getValue().split("\r\n");
		return retVal;
	}
	
	String getRawIpsAndLevels() {
		return remoteIps.getValue();
	}

}
