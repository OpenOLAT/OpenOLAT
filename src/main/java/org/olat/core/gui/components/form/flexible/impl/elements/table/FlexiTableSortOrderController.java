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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * Initial date: 2024-10-01<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class FlexiTableSortOrderController extends BasicController {

	private final VelocityContainer mainVC;

	private final FlexiTableElementImpl el;

	protected FlexiTableSortOrderController(UserRequest ureq, WindowControl wControl, FlexiTableElementImpl el) {
		super(ureq, wControl);
		this.el = el;

		mainVC = createVelocityContainer("sort_orders");
		List<Row> rows = el.getSorts().stream().map(this::toRow).toList();
		mainVC.contextPut("rows", rows);
		putInitialPanel(mainVC);
	}

	private Row toRow(FlexiTableSort sort) {
		String linkId = sort.getSortKey().getKey();
		Link link = LinkFactory.createCustomLink(linkId, "select", sort.getLabel(),
				Link.LINK | Link.NONTRANSLATED, mainVC, this);
		boolean spacer = FlexiTableSort.SPACER.equals(sort);
		if (sort.isSelected()) {
			if (sort.getSortKey().isAsc()) {
				link.setIconLeftCSS("o_icon o_icon_sort_desc o_icon-fw");
			} else {
				link.setIconLeftCSS("o_icon o_icon_sort_asc o_icon-fw");
			}
		}
		link.setUserObject(sort);
		return new Row(link, spacer);
	}

	public record Row(Link button, boolean spacer) {}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link && link.getUserObject() instanceof FlexiTableSort sort) {
			fireEvent(ureq, new SortOrderEvent(sort));
		}
	}

	protected static class SortOrderEvent extends Event {

		private final FlexiTableSort sort;

		public SortOrderEvent(FlexiTableSort sort) {
			super("sort-order");
			this.sort = sort;
		}

		public FlexiTableSort getSort() {
			return sort;
		}
	}
}
