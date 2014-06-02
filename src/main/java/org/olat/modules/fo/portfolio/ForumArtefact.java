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
package org.olat.modules.fo.portfolio;

import org.olat.core.util.resource.OresHelper;
import org.olat.modules.fo.Forum;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

public class ForumArtefact extends AbstractArtefact {

	private static final long serialVersionUID = -1315885067800017656L;
	public static final String FORUM_ARTEFACT_TYPE = OresHelper.calculateTypeName(Forum.class);

	@Override
	public String getIcon() {
		return "o_fo_icon";
	}

	@Override
	public String getResourceableTypeName() {
		return FORUM_ARTEFACT_TYPE;
	}

}
