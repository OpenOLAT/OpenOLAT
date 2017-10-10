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
package org.olat.core.util.mail;

import java.io.File;

import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * 
 * 
 * <P>
 * Initial Date:  24 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http.//www.frentix.com
 */
@Service("mailModule")
public class MailModule extends AbstractSpringModule {
	
	private static final String INTERN_MAIL_SYSTEM = "internSystem";
	private static final String SHOW_OUTBOX_RECIPIENT_NAMES = "showRecipientNames";
	private static final String SHOW_OUTBOX_MAIL_ADDRESSES = "showMailAddresses";
	private static final String SHOW_INBOX_RECIPIENT_NAMES = "showInboxRecipientNames";
	private static final String SHOW_INBOX_MAIL_ADDRESSES = "showInboxMailAddresses";
	private static final String RECEIVE_REAL_MAIL_USER_DEFAULT_SETTING = "receiveRealMailUserDefaultSetting";
	
	@Value("${mail.intern:false}")
	private boolean internSystem;
	@Value("${mail.showOutboxRecipientNames:true}")
	private boolean showOutboxRecipientNames;
	@Value("${mail.showOutboxMailAddresses:false}")
	private boolean showOutboxMailAddresses;
	@Value("${mail.showInboxRecipientNames:true}")
	private boolean showInboxRecipientNames;
	@Value("${mail.showInboxMailAddresses:false}")
	private boolean showInboxMailAddresses;
	@Value("${mail.receiveRealMailUserDefaultSetting:true}")
	private boolean receiveRealMailUserDefaultSetting;
	
	private int maxSizeOfAttachments = 5;
	
	private static final String ATTACHMENT_DEFAULT = "/mail";
	private String attachmentsRoot = ATTACHMENT_DEFAULT;
	
	private final FolderModule folderModule;
	
	@Autowired
	public MailModule(CoordinatorManager coordinatorManager, FolderModule folderModule) {
		super(coordinatorManager);
		this.folderModule = folderModule;
	}

	@Override
	public void init() {
		String internSystemValue = getStringPropertyValue(INTERN_MAIL_SYSTEM, true);
		if(StringHelper.containsNonWhitespace(internSystemValue)) {
			internSystem = "true".equalsIgnoreCase(internSystemValue);
		}
		
		String receiveRealMailUserDefaultSettingValue = getStringPropertyValue(RECEIVE_REAL_MAIL_USER_DEFAULT_SETTING, true);
		if(StringHelper.containsNonWhitespace(receiveRealMailUserDefaultSettingValue)) {
			receiveRealMailUserDefaultSetting = "true".equalsIgnoreCase(receiveRealMailUserDefaultSettingValue);
		}

		String showOutboxRecipientNamesValue = getStringPropertyValue(SHOW_OUTBOX_RECIPIENT_NAMES, true);
		if(StringHelper.containsNonWhitespace(showOutboxRecipientNamesValue)) {
			showOutboxRecipientNames = "true".equalsIgnoreCase(showOutboxRecipientNamesValue);
		}

		String showOutboxMailAddressesValue = getStringPropertyValue(SHOW_OUTBOX_MAIL_ADDRESSES, true);
		if(StringHelper.containsNonWhitespace(showOutboxMailAddressesValue)) {
			showOutboxMailAddresses = "true".equalsIgnoreCase(showOutboxMailAddressesValue);
		}
		
		String showInboxRecipientNamesValue = getStringPropertyValue(SHOW_INBOX_RECIPIENT_NAMES, true);
		if(StringHelper.containsNonWhitespace(showInboxRecipientNamesValue)) {
			showInboxRecipientNames = "true".equalsIgnoreCase(showInboxRecipientNamesValue);
		}

		String showInboxMailAddressesValue = getStringPropertyValue(SHOW_INBOX_MAIL_ADDRESSES, true);
		if(StringHelper.containsNonWhitespace(showInboxMailAddressesValue)) {
			showInboxMailAddresses = "true".equalsIgnoreCase(showInboxMailAddressesValue);
		}
	}

	@Override
	protected void initFromChangedProperties() {
		init();
	}

	/**
	 * Used the intern mail system.
	 * @return
	 */
	public boolean isInternSystem() {
		return internSystem;
	}
	
	/**
	 * @param internSystem
	 */
	public void setInterSystem(boolean internSystem) {
		this.internSystem = internSystem;
		String internSystemStr = internSystem ? "true" : "false";
		setStringProperty(INTERN_MAIL_SYSTEM, internSystemStr, true);
	}


	public boolean isShowOutboxRecipientNames() {
		return showOutboxRecipientNames;
	}

	public void setShowOutboxRecipientNames(boolean showOutboxRecipientNames) {
		this.showOutboxRecipientNames = showOutboxRecipientNames;
		String showOutboxRecipientNamesStr = showOutboxRecipientNames ? "true" : "false";
		setStringProperty(SHOW_OUTBOX_RECIPIENT_NAMES, showOutboxRecipientNamesStr, true);
	}

	public boolean isShowOutboxMailAddresses() {
		return showOutboxMailAddresses;
	}

	public void setShowOutboxMailAddresses(boolean showOutboxMailAddresses) {
		this.showOutboxMailAddresses = showOutboxMailAddresses;
		String showOutboxMailAddressesStr = showOutboxMailAddresses ? "true" : "false";
		setStringProperty(SHOW_OUTBOX_MAIL_ADDRESSES, showOutboxMailAddressesStr, true);
	}

	public boolean isShowInboxRecipientNames() {
		return showInboxRecipientNames;
	}
	
	public void setShowInboxRecipientNames(boolean showInboxRecipientsNames) {
		this.showInboxRecipientNames = showInboxRecipientsNames;
		String showInboxRecipientNamesStr = showInboxRecipientsNames ? "true" : "false";
		setStringProperty(SHOW_INBOX_RECIPIENT_NAMES, showInboxRecipientNamesStr, true);
	}
	
	public boolean isShowInboxMailAddresses() {
		return showInboxMailAddresses;
	}

	public void setShowInboxMailAddresses(boolean showInboxMailAddresses) {
		this.showInboxMailAddresses = showInboxMailAddresses;
		String showInboxMailAddressesStr = showInboxMailAddresses ? "true" : "false";
		setStringProperty(SHOW_INBOX_MAIL_ADDRESSES, showInboxMailAddressesStr, true);
	}

	/**
	 * Users can receive real e-mail too. This setting is the default for
	 * users. They can change it in Preferences Panel.
	 * @return
	 */
	public boolean isReceiveRealMailUserDefaultSetting() {
		return receiveRealMailUserDefaultSetting;
	}
	
	public void setReceiveRealMailUserDefaultSetting(boolean realMail) {
		String realMailStr = realMail ? "true" : "false";
		setStringProperty(RECEIVE_REAL_MAIL_USER_DEFAULT_SETTING, realMailStr, true);
	}

	/**
	 * Check if the mail host is configured
	 * @return
	 */
	public boolean isMailHostEnabled() {
		String mailhost = WebappHelper.getMailConfig("mailhost");
		return (StringHelper.containsNonWhitespace(mailhost) && !mailhost.equalsIgnoreCase("disabled"));
	}
	
	/**
	 * @return the maximum size allowed for attachements in MB (default 5MB)
	 */
	public int getMaxSizeForAttachement() {
		String maxSizeStr = WebappHelper.getMailConfig("mailAttachmentMaxSize");
		if(StringHelper.containsNonWhitespace(maxSizeStr)) {
			maxSizeOfAttachments = Integer.parseInt(maxSizeStr);
		}
		return maxSizeOfAttachments;
	}
	
	public VFSContainer getRootForAttachments() {
		String root = folderModule.getCanonicalRoot() + attachmentsRoot;
		File rootFile = new File(root);
		if(!rootFile.exists()) {
			rootFile.mkdirs();
		}
		VFSContainer rootContainer = new LocalFolderImpl(rootFile);
		return rootContainer;
	}

	/**
	 * @return the configured mail host. Can be null, indicating that the system
	 *         should not send any mail at all
	 */
	public String getMailhost() {
		return WebappHelper.getMailConfig("mailhost");
	}

}