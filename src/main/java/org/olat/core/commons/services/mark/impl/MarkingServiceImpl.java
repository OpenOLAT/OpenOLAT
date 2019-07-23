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

package org.olat.core.commons.services.mark.impl;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.commons.services.mark.MarkResourceStat;
import org.olat.core.commons.services.mark.MarkingService;
import org.olat.core.commons.services.mark.impl.ui.MarkController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Initial Date:  9 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Service
public class MarkingServiceImpl implements MarkingService {
	
	@Autowired
	private MarkManager markManager;

	@Override
	public MarkManager getMarkManager() {
		return markManager;
	}
	

	@Override
	public Controller getMarkController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, String subPath, String businessPath) {
		return new MarkController(ureq, wControl, ores, subPath, businessPath);
	}

	@Override
	public Controller getMarkController(UserRequest ureq, WindowControl wControl, Mark mark) {
		return new MarkController(ureq, wControl, mark.getOLATResourceable(), mark.getResSubPath(), mark.getBusinessPath());
	}
	
	@Override
	public Controller getMarkController(UserRequest ureq, WindowControl wControl, Mark mark, MarkResourceStat stat, OLATResourceable ores, String subPath, String businessPath) {
		return new MarkController(ureq, wControl, mark, stat, ores, subPath, businessPath);
	}
}
