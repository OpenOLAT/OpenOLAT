/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.util;

import java.util.logging.Level;

/**
 * Initial Date:  04.12.2006 <br>
 * @author patrickb
 */
public class ValidationStatusImpl implements ValidationStatus {

	private Level level = ValidationStatus.NOERROR;
	public static final ValidationStatus NOERROR = new ValidationStatusImpl();
	
	private ValidationStatusImpl() {
		//
	}
	
	public ValidationStatusImpl(Level level){
		this.level = level;
	}

	@Override
	public boolean isError() {
		return ValidationStatus.ERROR.equals(level);
	}

	@Override
	public boolean isInfo() {
		return ValidationStatus.INFO.equals(level);
	}

	@Override
	public boolean isWarning() {
		return ValidationStatus.WARNING.equals(level);
	}

	@Override
	public Level getLevel() {
		return level;
	}
}
