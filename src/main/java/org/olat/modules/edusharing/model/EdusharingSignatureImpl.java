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
package org.olat.modules.edusharing.model;

import org.olat.modules.edusharing.EdusharingSignature;

/**
 * 
 * Initial date: 5 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingSignatureImpl implements EdusharingSignature {
	
	private final String appId;
	private final String timeStamp;
	private final String signed;
	private final String signature;
	
	public EdusharingSignatureImpl(String appId, String timeStamp, String signed, String signature) {
		this.appId = appId;
		this.timeStamp = timeStamp;
		this.signed = signed;
		this.signature = signature;
	}

	@Override
	public String getAppId() {
		return appId;
	}

	@Override
	public String getTimeStamp() {
		return timeStamp;
	}

	@Override
	public String getSigned() {
		return signed;
	}

	@Override
	public String getSignature() {
		return signature;
	}

}
