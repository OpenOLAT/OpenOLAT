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
package org.olat.ims.qti21.ui.editor.metadata;

import org.olat.core.id.Roles;
import org.olat.modules.qpool.QPoolSecurityCallback;

/**
 * 
 * Initial date: 8 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemEditorQPoolSecurityCallback implements QPoolSecurityCallback {

	@Override
	public void setRoles(Roles roles) {
		//
	}

	@Override
	public boolean canUseCollections() {
		return false;
	}

	@Override
	public boolean canUsePools() {
		return false;
	}

	@Override
	public boolean canUseGroups() {
		return false;
	}

	@Override
	public boolean canUseReviewProcess() {
		return false;
	}

	@Override
	public boolean canUseTaxonomy() {
		return true;
	}

	@Override
	public boolean canUseEducationalContext() {
		return true;
	}

	@Override
	public boolean canNewQuestions() {
		return false;
	}

	@Override
	public boolean canEditQuestions() {
		return false;
	}

	@Override
	public boolean canShareQuestions() {
		return false;
	}

	@Override
	public boolean canCreateTest() {
		return false;
	}

	@Override
	public boolean canEditAllQuestions() {
		return false;
	}

	@Override
	public boolean canConfigReviewProcess() {
		return false;
	}

	@Override
	public boolean canConfigTaxonomies() {
		return false;
	}

	@Override
	public boolean canConfigPools() {
		return false;
	}

	@Override
	public boolean canConfigItemTypes() {
		return false;
	}

	@Override
	public boolean canConfigEducationalContext() {
		return false;
	}
}