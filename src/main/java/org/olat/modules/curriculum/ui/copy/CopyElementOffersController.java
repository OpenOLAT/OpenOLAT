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
package org.olat.modules.curriculum.ui.copy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.DateChooserOrientation;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Organisation;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyOfferSetting;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.copy.CopyElementOffersDataModel.CopyOfferCols;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CopyElementOffersController extends StepFormBasicController {
	
	private FlexiTableElement tableEl;
	private CopyElementOffersDataModel tableModel;
	
	private int counter = 0;
	private final CopyElementContext context;
	
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private OrganisationModule organisationModule;
	
	public CopyElementOffersController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			CopyElementContext context) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "offers");
		setTranslator(Util.createPackageTranslator(CurriculumComposerController.class, getLocale(),
				Util.createPackageTranslator(AccessConfigurationController.class, getLocale(), getTranslator())));
		this.context = context;

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyOfferCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyOfferCols.label));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyOfferCols.publishedIn,
				new PublishedInFlexiCellRenderer(getTranslator())));
		if(organisationModule.isEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyOfferCols.organisations));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyOfferCols.availableIn));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyOfferCols.price));
		
		tableModel = new CopyElementOffersDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, true, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
	}
	
	private void loadModel() {
		List<OfferAndAccessCopy> offersAndInfos = context.getOffersAndAccessInfos();
		List<Offer> offers = offersAndInfos.stream()
				.map(OfferAndAccessCopy::getOffer)
				.toList();
		Map<Long,List<Organisation>> offerKeyToOrganisations = acService.getOfferKeyToOrganisations(offers);
		List<CopyOfferRow> rows = offersAndInfos.stream()
				.map(oai -> forgeRow(oai, offerKeyToOrganisations.get(oai.getOffer().getKey())))
				.toList();
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CopyOfferRow forgeRow(OfferAndAccessCopy offerAndInfos, List<Organisation> organisations) {
		OfferAccess link = offerAndInfos.getOfferAccess();
		AccessMethodHandler handler = acModule.getAccessMethodHandler(link.getMethod().getType());
		String type = handler.getMethodName(getLocale());
		
		CopyOfferRow row = new CopyOfferRow(offerAndInfos, organisations, type);
		
		Date validFrom = link.getValidFrom();
		Date validTo = link.getValidTo();
		if(validFrom != null || validTo != null) {
			DateChooser validFromToEl = uifactory.addDateChooser("valid_" + (++counter), null, validFrom, flc);
			validFromToEl.setFormLayout("tablecell");
			validFromToEl.setSecondDate(true);
			validFromToEl.setSecondDate(validTo);
			validFromToEl.setSeparator("to.separator");
			validFromToEl.setOrientation(DateChooserOrientation.top);
			row.setValidFromToEl(validFromToEl);
		}
		
		return row;
	}
	
	@Override
	protected void formNext(UserRequest ureq) {
		List<CopyOfferRow> rows = tableModel.getObjects();
		List<CopyOfferSetting> settings = new ArrayList<>();
		for(CopyOfferRow row:rows) {
			DateChooser dateChooser = row.getValidFromToEl();
			OfferAndAccessCopy offerAndInfos = row.getOfferAndInfos();
			if(dateChooser == null) {
				offerAndInfos.setValidFrom(null);
				offerAndInfos.setValidTo(null);
			} else {
				offerAndInfos.setValidFrom(dateChooser.getDate());
				offerAndInfos.setValidTo(dateChooser.getSecondDate());
			}
			settings.add(new CopyOfferSetting(row.getOffer(), offerAndInfos.getValidFrom(), offerAndInfos.getValidTo()));
		}
		context.setOfferSettings(settings);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
