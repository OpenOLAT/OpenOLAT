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
package org.olat.course.nodes.form.model;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.course.nodes.form.FormParticipation;
import org.olat.course.nodes.form.FormParticipationBundle;

/**
 * 
 * Initial date: Jan 28, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FormParticipationBundleImpl implements FormParticipationBundle {
	
	private final Identity identity;
	private final FormParticipation lastParticipation;
	private final List<FormParticipation> submittedParticipations;

	public FormParticipationBundleImpl(Identity identity, FormParticipation lastParticipation,
			List<FormParticipation> submittedParticipations) {
		this.identity = identity;
		this.lastParticipation = lastParticipation;
		this.submittedParticipations = submittedParticipations;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	@Override
	public FormParticipation getLastParticipation() {
		return lastParticipation;
	}

	@Override
	public List<FormParticipation> getSubmittedParticipations() {
		return submittedParticipations;
	}

}
