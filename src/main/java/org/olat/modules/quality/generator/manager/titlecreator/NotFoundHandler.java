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
package org.olat.modules.quality.generator.manager.titlecreator;

import java.util.Collections;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.olat.modules.quality.generator.TitleCreatorHandler;

/**
 * 
 * Initial date: 27.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NotFoundHandler implements TitleCreatorHandler {

	@Override
	public boolean canHandle(Class<?> clazz) {
		return true;
	}

	@Override
	public void mergeContext(VelocityContext context, Object object) {
		// nothing to do
	}

	@Override
	public List<String> getIdentifiers() {
		return Collections.emptyList();
	}

}
