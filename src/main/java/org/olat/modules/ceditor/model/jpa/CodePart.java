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
package org.olat.modules.ceditor.model.jpa;

import java.io.Serial;

import org.olat.modules.ceditor.model.CodeElement;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

/**
 * Initial date: 2023-12-11<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Entity(name="cecodepart")
public class CodePart extends AbstractPart implements CodeElement {

	@Serial
	private static final long serialVersionUID = -878376036122396975L;

	@Override
	@Transient
	public String getType() {
		return "code";
	}

	@Override
	public CodePart copy() {
		CodePart part = new CodePart();
		copy(part);
		return part;
	}
}
