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
package org.olat.modules.project.model;

import java.util.Date;

import org.olat.core.commons.services.tag.model.TagInfoImpl;
import org.olat.modules.project.ProjTagInfo;

/**
 * 
 * Initial date: 14 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjTagInfoImpl extends TagInfoImpl implements ProjTagInfo {

	private final boolean selected;

	public ProjTagInfoImpl(Long key, Date creationDate, String displayName, Long count, Long artefactSelected) {
		super(key, creationDate, displayName, count);
		this.selected = artefactSelected > 0;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

}
