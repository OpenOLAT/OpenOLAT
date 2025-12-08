/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.catalog;

import org.olat.modules.catalog.model.CatalogEntrySecurityCallbackImpl;

/**
 * 
 * Initial date: Oct 22, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface CatalogEntrySecurityCallback {
	
	static CatalogEntrySecurityCallback ACCESSIBLE_CALLBACK
			= new CatalogEntrySecurityCallbackImpl(true, true, null, false, false, false);
	static CatalogEntrySecurityCallback NOT_YET_PUPLISHED
			= new CatalogEntrySecurityCallbackImpl(true, false, OpenDisabledReason.notYetPublished, false, false, false);
	static CatalogEntrySecurityCallback NO_CONTENT_YET
			= new CatalogEntrySecurityCallbackImpl(true, false, OpenDisabledReason.noContentYet, false, false, false);
	static CatalogEntrySecurityCallback CONFIRMATION_OUTSTANDING
			= new CatalogEntrySecurityCallbackImpl(true, false, OpenDisabledReason.confirmationOutstanding, false, false, false);
	static CatalogEntrySecurityCallback FULLY_BOOKED_CALLBACK
			= new CatalogEntrySecurityCallbackImpl(false, false, null, true, false, false);
	static CatalogEntrySecurityCallback AUTO_BOOKING_CALLBACK
			= new CatalogEntrySecurityCallbackImpl(false, false, null, true, true, true);
	static CatalogEntrySecurityCallback BOOKING_CALLBACK
			= new CatalogEntrySecurityCallbackImpl(false, false, null, true, true, false);
	static CatalogEntrySecurityCallback ERROR_CALLBACK
			= new CatalogEntrySecurityCallbackImpl(false, false, null, false, false, false);
		
	enum OpenDisabledReason { notYetPublished, noContentYet, confirmationOutstanding }
	
	boolean isOpenAvailable();
	
	boolean isOpenEnabled();
	
	OpenDisabledReason getOpenDisabledReason();
	
	boolean isBookAvailable();
	
	boolean isBookEnabled();
	
	boolean isAutoBooking();

}
