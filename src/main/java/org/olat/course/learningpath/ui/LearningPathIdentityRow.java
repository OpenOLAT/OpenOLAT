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
package org.olat.course.learningpath.ui;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer.CompletionPassed;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 2 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathIdentityRow extends UserPropertiesRow implements CompletionPassed {

	private final Double completion;
	private final Boolean passed;
	private final BigDecimal score;

	public LearningPathIdentityRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale,
			Double completion, Boolean passed, BigDecimal score) {
		super(identity, userPropertyHandlers, locale);
		this.completion = completion;
		this.passed = passed;
		this.score = score;
	}

	@Override
	public Double getCompletion() {
		return completion;
	}

	@Override
	public Boolean getPassed() {
		return passed;
	}

	public BigDecimal getScore() {
		return score;
	}

}
