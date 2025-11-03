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
package org.olat.modules.certificationprogram.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters.Type;

/**
 * 
 * Initial date: 3 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramRemovedMembersController extends AbstractCertificationProgramMembersController {

	protected static final String REVOKED_ID = "Revoked";
	protected static final String NOT_RENEWABLE_ID = "NotRenewable";
	
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	public CertificationProgramRemovedMembersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CertificationProgram certificationProgram, CertificationProgramSecurityCallback secCallback) {
		super(ureq, wControl, toolbarPanel, certificationProgram, secCallback);
		
		initForm(ureq);
		tableEl.setSelectedFilterTab(ureq, allTab);
		loadModel(ureq);
	}

	@Override
	protected void initColumns(FlexiTableColumnModel columnsModel) {
		//
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(CertificationStatus.NOT_RENEWABLE.name(), translate("filter.not.renewable")));
		statusValues.add(SelectionValues.entry(CertificationStatus.REVOKED.name(), translate("filter.revoked")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_STATUS, statusValues, true);
		filters.add(statusFilter);
	}

	@Override
	protected void initFiltersPresets(List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab revokedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(REVOKED_ID, translate("filter.revoked"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, CertificationStatus.REVOKED.name())));
		tabs.add(revokedTab);
		
		FlexiFiltersTab notRenewableTab = FlexiFiltersTabFactory.tabWithImplicitFilters(NOT_RENEWABLE_ID, translate("filter.not.renewable"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, CertificationStatus.NOT_RENEWABLE.name())));
		tabs.add(notRenewableTab);
	}
	
	@Override
	protected CertificationProgramMemberSearchParameters getSearchParams() {
		CertificationProgramMemberSearchParameters searchParams = new CertificationProgramMemberSearchParameters(certificationProgram);
		searchParams.setType(Type.REMOVED);
		return searchParams;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(calloutCtrl == source) {
        	cleanUp();
        } else if(toolsCtrl == source) {
        	if(event == Event.CLOSE_EVENT) {
        		calloutCtrl.deactivate();
        		cleanUp();
        	}
        }
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		calloutCtrl = null;
		toolsCtrl = null;
	}

	@Override
	protected void doOpenTools(UserRequest ureq, CertificationProgramMemberRow row, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {

		private final Link contactLink;
		
		private final CertificationProgramMemberRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CertificationProgramMemberRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tool_members");
			
			contactLink = LinkFactory.createLink("contact", "contact", getTranslator(), mainVC, this, Link.LINK);
			contactLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if(contactLink == source) {
				doOpenContact(ureq, row);
			}
		}
	}
}
