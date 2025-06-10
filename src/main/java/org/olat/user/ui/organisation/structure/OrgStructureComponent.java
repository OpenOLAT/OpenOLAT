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
package org.olat.user.ui.organisation.structure;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.util.Util;

/**
 * Initial date: Mai 07, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com,
 *         <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OrgStructureComponent extends AbstractComponent implements ControllerEventListener, Disposable {

	private static final ComponentRenderer RENDERER = new OrgStructureRenderer();

	private final WindowControl wControl;
	private OrganisationStructureCalloutController treeCtrl;
	private CloseableCalloutWindowController popupCtrl;

	private final Translator compTranslator;
	private List<Organisation> activeOrganisations;
	private boolean collapseUnrelated = false;

	public OrgStructureComponent(String name, WindowControl wControl, Locale locale) {
		super(name);
		this.wControl = wControl;
		this.compTranslator = Util.createPackageTranslator(OrgStructureComponent.class, locale);
	}

	Translator getCompTranslator() {
		return compTranslator;
	}

	public List<Organisation> getActiveOrganisations() {
		return activeOrganisations;
	}

	public void setActiveOrganisations(List<Organisation> activeOrganisations) {
		this.activeOrganisations = activeOrganisations;
		setDirty(true);
	}

	public boolean isCollapseUnrelated() {
		return collapseUnrelated;
	}

	public void setCollapseUnrelated(boolean collapseUnrelated) {
		this.collapseUnrelated = collapseUnrelated;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);

		if ("show-tree".equals(cmd)) {
			doOpenTree(ureq);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		// when the user clicks outside then dispose
		if (popupCtrl == source) {
			cleanUp();
		}
	}

	@Override
	public void dispose() {
		cleanUp();
	}

	private void cleanUp() {
		treeCtrl = cleanUp(treeCtrl);
		popupCtrl = cleanUp(popupCtrl);
	}

	private <T extends Controller> T cleanUp(T ctrl) {
		if (ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return null;
	}

	private void doOpenTree(UserRequest ureq) {
		treeCtrl = new OrganisationStructureCalloutController(ureq, wControl, activeOrganisations, collapseUnrelated);
		treeCtrl.addControllerListener(this);

		CalloutSettings settings = new CalloutSettings(true, CalloutSettings.CalloutOrientation.bottom, true, null);
		popupCtrl = new CloseableCalloutWindowController(ureq, wControl, treeCtrl.getInitialComponent(),
				getDispatchID(), null, true, null, settings);
		popupCtrl.addControllerListener(this);
		popupCtrl.activate();
	}

}
