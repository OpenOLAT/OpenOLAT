/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.math.BigDecimal;

import org.olat.core.util.StringHelper;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.creditpoint.CreditPointSystem;

/**
 * 
 * Initial date: 4 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationHelper {
	
	private CertificationHelper() {
		//
	}
	
	public static final String creditPointsToString(CertificationProgram program) {
		StringBuilder sb = new StringBuilder();
		if(program.getCreditPoints() != null) {
			sb.append(program.getCreditPoints().toString()).append(" ");
		}
		CreditPointSystem system = program.getCreditPointSystem();
		if(system != null) {
			String label = system.getLabel();
			if(StringHelper.containsNonWhitespace(label)) {
				if(sb.length() > 0) sb.append(" ");
				sb.append(label);
			}
			String name = system.getName();
			if(StringHelper.containsNonWhitespace(name)) {
				if(sb.length() > 0) sb.append(" ");
				sb.append(name);
			}
		}
		return sb.toString();
	}
	
	public static final String compactCreditPointsToString(CertificationProgram program) {
		StringBuilder sb = new StringBuilder();
		if(program.getCreditPoints() != null) {
			sb.append(program.getCreditPoints().toString()).append(" ");
		}
		CreditPointSystem system = program.getCreditPointSystem();
		if(system != null) {
			if(StringHelper.containsNonWhitespace(system.getLabel())) {
				if(sb.length() > 0) sb.append(" ");
				sb.append(system.getLabel());
			} else if(StringHelper.containsNonWhitespace(system.getName())) {
				if(sb.length() > 0) sb.append(" ");
				sb.append(system.getName());
			}
		}
		return sb.toString();
	}
	
	public static final String creditPointsToString(BigDecimal amount, CreditPointSystem system) {
		StringBuilder sb = new StringBuilder();
		if(amount != null) {
			sb.append(amount.toString()).append(" ");
		}
		if(system != null) {
			String label = system.getLabel();
			if(StringHelper.containsNonWhitespace(label)) {
				if(sb.length() > 0) sb.append(" ");
				sb.append(label);
			}
		}
		return sb.toString();
	}
	
	public static final String creditPoints(BigDecimal amount) {
		return amount == null ? "" : amount.toString();
	}

}
