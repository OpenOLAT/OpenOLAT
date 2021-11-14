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

package org.olat.core.commons.services.mark.impl.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkResourceStat;
import org.olat.core.commons.services.mark.MarkingService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Description:<br>
 * A controller to mark a resource and show the status of the flag.
 * 
 * <P>
 * Initial Date:  9 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class MarkController extends FormBasicController {

	private final MarkingService markingService;
	private FormLink markLink;
	
	private final OLATResourceable ores;
	private final String subPath;
	private final String businessPath;
	
	private Mark mark;
	private MarkResourceStat stat;
	
	private boolean marked;
	
	public MarkController(UserRequest ureq, WindowControl wControl, Mark mark) {
		super(ureq, wControl);
		if(mark == null) throw new NullPointerException("The mark cannot be null with this constructor");
		
		this.marked = true;
		this.mark = mark;
		this.ores = mark.getOLATResourceable();
		this.subPath = mark.getResSubPath();
		this.businessPath = mark.getBusinessPath();
		markingService = (MarkingService) CoreSpringFactory.getBean(MarkingService.class);
		
		initForm(ureq);
	}
	
	public MarkController(UserRequest ureq, WindowControl wControl, Mark mark, MarkResourceStat stat, OLATResourceable ores, String subPath, String businessPath) {
		super(ureq, wControl);
		
		this.marked = (mark != null);
		this.mark = mark;
		this.stat = stat;
		this.ores = ores;
		this.subPath = subPath;
		this.businessPath = businessPath;
		markingService = (MarkingService) CoreSpringFactory.getBean(MarkingService.class);
		
		initForm(ureq);
	}
	
	public MarkController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, String subPath, String businessPath) {
		super(ureq, wControl);
		
		this.ores = ores;
		this.subPath = subPath;
		this.businessPath = businessPath;
		markingService = (MarkingService) CoreSpringFactory.getBean(MarkingService.class);
		marked = markingService.getMarkManager().isMarked(ores, ureq.getIdentity(), subPath);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		markLink = uifactory.addFormLink("mark", " ", " ", formLayout, Link.NONTRANSLATED + Link.LINK_CUSTOM_CSS);
		markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setEnabled(!ureq.getUserSession().getRoles().isGuestOnly());

		String tooltip;
		if(stat == null) {
			tooltip = getTranslator().translate("mark.no_stat", null);
		} else {
			Integer countI = stat.getCount();
			if (mark!=null && mark.getCreator().equalsByPersistableKey(getIdentity())){
				if (countI == 1) {
					tooltip = getTranslator().translate("mark.stat.self.only");
				} else {
					tooltip = getTranslator().translate("mark.stat.self", new String[]{ new Integer(countI-1).toString() });
				}
			} else {
				tooltip = getTranslator().translate("mark.stat", new String[]{ countI.toString() });
			}
		}
		
		markLink.getComponent().setTooltip(tooltip);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//do nothing
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == markLink) {
			Identity identity = ureq.getIdentity();
			if(marked) {
				if(mark == null) {
					markingService.getMarkManager().removeMark(ores, ureq.getIdentity(), subPath);
					fireEvent(ureq, new UnmarkedEvent(ores, subPath));
				} else {
					markingService.getMarkManager().removeMark(mark);
					fireEvent(ureq, new UnmarkedEvent(mark.getOLATResourceable(), mark.getResSubPath()));
					mark = null;
				}
			} else {
				mark = markingService.getMarkManager().setMark(ores, identity, subPath, businessPath);
				fireEvent(ureq, new MarkedEvent(mark));
			}
			marked = !marked;
			markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		}
	}
}
