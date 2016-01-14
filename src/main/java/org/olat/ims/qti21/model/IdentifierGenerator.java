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
package org.olat.ims.qti21.model;

import java.util.UUID;

import org.olat.core.util.CodeHelper;

import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * Generate an identifier, max length 32 characters (to be conform with QTI 2.1)
 * with an oo prefix.
 * 
 * 
 * Initial date: 03.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentifierGenerator {
	
	public static final String newAsString() {
		return "oo".concat(UUID.randomUUID().toString().replace("-", "").substring(2));
	}
	
	public static final String newAsString(String prefix) {
		return prefix.concat(UUID.randomUUID().toString().replace("-", "").substring(prefix.length()));
	}
	
	public static final Identifier newAsIdentifier(String prefix) {
		return Identifier.parseString(newAsString(prefix));
	}
	
	public static final Identifier newNumberAsIdentifier(String prefix) {
		long number = CodeHelper.getForeverUniqueID();
		return Identifier.parseString(prefix + Long.toString(number));
	}
	
	public static final Identifier newAsIdentifier() {
		return Identifier.parseString(newAsString());
	}
	
	public static final String newAssessmentTestFilename() {
		return "test" + UUID.randomUUID() + ".xml";
	}
	
	public static final String newAssessmentItemFilename() {
		return "item" + UUID.randomUUID() + ".xml";
	}

}
