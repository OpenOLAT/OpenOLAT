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
package org.olat.modules.library;

import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.library.search.LibraryContextEntryControllerCreator;
import org.olat.modules.library.site.LibrarySite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * <h3>Description:</h3>
 * load library as a module, with its own config
 * 
 * Initial Date:  09.08.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
@Service("libraryModule")
public class LibraryModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String CONTACTS_TO_NOTIFY_AFTER_UPLOAD = "notify.afterupload";
	private static final String CONTACTS_TO_NOTIFY_AFTER_FREEING = "notify.afterfreeing";
	private static final String LIBRARY_ENTRY_KEY = "library.repository.entry.key";
	private static final String LIBRARY_ENABLE = "site.library.enabled";
	
	private boolean autoSubscribe = false;
	
	@Value("${site.library.enabled}")
	private boolean enabled;
	@Value("${library.repository.entry.key}")
	private String libraryEntryKey;
	@Value("${library.notify.afterupload}")
	private String emailContactsToNotifyAfterUpload;
	@Value("${library.notify.afterfreeing}")
	private String emailContactsToNotifyAfterFreeing;
	
	@Autowired
	public LibraryModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager, "com.frentix.olat.library.LibraryManager");
	}

	@Override
	public void init() {
		// Add controller factory extension point to launch library
		NewControllerFactory.getInstance().addContextEntryControllerCreator(LibrarySite.class.getSimpleName(),
				new LibraryContextEntryControllerCreator());
		
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		String enabledObj = getStringPropertyValue(LIBRARY_ENABLE, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String libraryEntryObj = getStringPropertyValue(LIBRARY_ENTRY_KEY, true);
		if(StringHelper.containsNonWhitespace(libraryEntryObj)) {
			libraryEntryKey = libraryEntryObj;
		}
		
		String emailContactsToNotifyAfterUploadObj = getStringPropertyValue(CONTACTS_TO_NOTIFY_AFTER_UPLOAD, true);
		if(StringHelper.containsNonWhitespace(emailContactsToNotifyAfterUploadObj)) {
			emailContactsToNotifyAfterUpload = emailContactsToNotifyAfterUploadObj;
		}
		
		String emailContactsToNotifyAfterFreeingObj = getStringPropertyValue(CONTACTS_TO_NOTIFY_AFTER_FREEING, true);
		if(StringHelper.containsNonWhitespace(emailContactsToNotifyAfterFreeingObj)) {
			emailContactsToNotifyAfterFreeing = emailContactsToNotifyAfterFreeingObj;
		}
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enable) {
		this.enabled = enable;
		setStringProperty(LIBRARY_ENABLE, enable ? "true" : "false", true);
	}
	
	public boolean isAutoSubscribe() {
		return autoSubscribe;
	}

	public String getEmailContactsToNotifyAfterUpload() {
		return emailContactsToNotifyAfterUpload;
	}
	
	public void setEmailContactsToNotifyAfterUpload(String mail) {
		emailContactsToNotifyAfterUpload = mail;
		setStringProperty(CONTACTS_TO_NOTIFY_AFTER_UPLOAD, mail, true);
	}
	
	public String getEmailContactsToNotifyAfterFreeing() {
		return emailContactsToNotifyAfterFreeing;
	}
	
	public void setEmailContactsToNotifyAfterFreeing(String email) {
		emailContactsToNotifyAfterFreeing = email;
		setStringProperty(CONTACTS_TO_NOTIFY_AFTER_FREEING, email, true);
	}
	
	public String getLibraryEntryKey() {
		return libraryEntryKey;
	}
	
	public void setLibraryEntryKey(String key) {
		this.libraryEntryKey = key;
		setStringProperty(LIBRARY_ENTRY_KEY, libraryEntryKey, true);
	}
}
