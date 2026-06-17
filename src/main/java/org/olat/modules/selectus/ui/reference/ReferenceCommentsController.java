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
package org.olat.modules.selectus.ui.reference;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;

import org.olat.modules.selectus.model.ReferenceComment;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 27 oct. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceCommentsController extends FormBasicController {
	
	private final List<ReferenceComment> comments;
	
	public ReferenceCommentsController(UserRequest ureq, WindowControl wControl, List<ReferenceComment> comments) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.comments = comments;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int counter = 0;
		Formatter formatter = Formatter.getInstance(getLocale());
		if(comments.size() > 1) {
			Collections.sort(comments, new CommentComparator());
		}
		for(ReferenceComment comment:comments) {
			StringBuilder text = Formatter.escWithBR(comment.getComment());
			uifactory.addStaticTextElement("comment_" + (++counter), "reference.comment", text.toString(), formLayout);
			String date = formatter.formatDateLong(comment.getCreationDate());
			uifactory.addStaticTextElement("date_" + (++counter), "reference.comment.date", date, formLayout);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private static class CommentComparator implements Comparator<ReferenceComment> {

		@Override
		public int compare(ReferenceComment o1, ReferenceComment o2) {
			if(o1 == null || o2 == null) {
				return compareNulls(o1, o2);
			}
			
			Date c1 = o1.getCreationDate();
			Date c2 = o2.getCreationDate();
			return c2.compareTo(c1);
		}
		
		private int compareNulls(Object o1, Object o2) {
			if(o1 == null && o2 == null) {
				return 0;
			}
			return o1 == null ? -1 : 1;
		}
	}
}
