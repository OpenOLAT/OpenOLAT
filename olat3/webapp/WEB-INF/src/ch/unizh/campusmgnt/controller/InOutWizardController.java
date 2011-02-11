/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/

package ch.unizh.campusmgnt.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.GenericObjectArrayTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.WizardController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.bulk.BulkAction;
import org.olat.core.util.bulk.BulkActionModule;
import org.olat.core.util.bulk.HeaderColumnTranslator;
import org.olat.user.UserManager;

import ch.unizh.campusmgnt.ColWithBulkActionForm;
import ch.unizh.campusmgnt.KeyWithColumnForm;
import ch.unizh.campusmgnt.SeparatedValueInputForm;
/**
 * 
 * Description:<br>
 * TODO: schneider Class Description for InOutWizardController
 *
 * <P>
 * Initial Date:  19.12.2005 <br>
 * @author Alexander Schneider
 */
public class InOutWizardController extends BasicController {
	private static final String CMD_FINISHED = "finished";
	
	private HeaderColumnTranslator hcTranslator;
	private WizardController wc;
	private TableController sepValTableCtr;
	private GenericObjectArrayTableDataModel sepValModel;
	private TableController sepValFinTableCtr;
	private VelocityContainer sepValVC;
	private VelocityContainer keywithcolVC;
	private VelocityContainer colwithbulkactionVC;
	private VelocityContainer finishedVC;
	private SeparatedValueInputForm sepValInForm;
	private KeyWithColumnForm kwcForm;
	private ColWithBulkActionForm cwbForm;
	private Link backLinkKey;
	private Link backLinkCol;
	private Link backLinkFin;
	private List columnNames;
	private List olatKeys;
	private List bulkActions;
	private int steps = 4;
	private TableGuiConfiguration tableConfig;
	private int selectedColForOlatKey;
	private int selectedOlatKey;
	private String selectedValueOfOlatKey;
	private List rows;
	private int numOfValuesPerLine;
	private int numOfLines;

	public InOutWizardController(UserRequest ureq, List bulkActions, WindowControl wControl) {
		super(ureq, wControl);
		this.bulkActions = bulkActions;
		
		sepValVC = createVelocityContainer("inout");
		wc = new WizardController(ureq, wControl, steps);
		listenTo(wc);
		
		sepValInForm = new SeparatedValueInputForm(ureq, wControl);
		listenTo(sepValInForm);
		
		wc.setWizardTitle(translate("wizard.step1.title"));
		wc.setNextWizardStep(translate("wizard.step1.howto"), sepValInForm.getInitialComponent());
		sepValVC.put("wc", wc.getInitialComponent());
		JSAndCSSComponent xls_eg = new JSAndCSSComponent("xls_eg", this.getClass(), null, "xls_eg.css", true);
		sepValVC.put("xls_eg", xls_eg);
		putInitialPanel(sepValVC);
	}
	
	/**
	 * This dispatches component events...
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		
		if (source == backLinkKey){
			wc.setWizardTitle(translate("wizard.step1.title"));
			wc.setBackWizardStep(translate("wizard.step1.howto"), sepValInForm.getInitialComponent());
		// events from step 3
		// preparing step 4
		}else if (source == backLinkCol){
			wc.setWizardTitle(translate("wizard.step2.title"));
			wc.setBackWizardStep(translate("wizard.step2.howto"), keywithcolVC);
		// events from 4. step
		}else if (source == finishedVC){
			if (event.getCommand().equals(CMD_FINISHED)) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}else if (source == backLinkFin){
			wc.setWizardTitle(translate("wizard.step3.title"));
			wc.setBackWizardStep(translate("wizard.step3.howto"), colwithbulkactionVC);
		}	
	}

	/**
	 * This dispatches controller events...
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		// event from 1. step
		// preparing step 2
		if (source == sepValInForm){ 
			if (event == Event.DONE_EVENT) {
				keywithcolVC = createVelocityContainer ("keywithcol");
				backLinkKey = LinkFactory.createLinkBack(keywithcolVC, this);
				
				this.rows = new ArrayList(); // contains every input line as Object array
				rows = sepValInForm.getInputRows();
				numOfValuesPerLine = sepValInForm.getNumOfValPerLine();
				numOfLines = sepValInForm.getNumOfLines();
				
				// convert user input to an OLAT table
				columnNames = new ArrayList();
				tableConfig = new TableGuiConfiguration();
				tableConfig.setDownloadOffered(false);
				tableConfig.setSortingEnabled(false);
				tableConfig.setColumnMovingOffered(false);
				hcTranslator = new HeaderColumnTranslator(getTranslator());
				
				removeAsListenerAndDispose(sepValTableCtr);
				sepValTableCtr = new TableController(tableConfig, ureq, getWindowControl(), hcTranslator);
				listenTo(sepValTableCtr);
				
				for(int i = 0;i < numOfValuesPerLine+1; i++){ // lenght+1 since adding the delimiter above
					sepValTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ccc"+(i+1), i, null, ureq.getLocale()));
					// add every name of a column to a list deployed as pulldown to the user for matching column with olat key
					columnNames.add(translate("column", new String[]{""+(i+1)}));
				}
				sepValModel = new GenericObjectArrayTableDataModel(rows, numOfLines);
				sepValTableCtr.setTableDataModel(sepValModel);
				keywithcolVC.put("sepValTable", sepValTableCtr.getInitialComponent());
				
				PackageTranslator userTrans = new PackageTranslator("org.olat.user", ureq.getLocale());
				PackageTranslator properyHandlersTrans = new PackageTranslator("org.olat.user.propertyhandlers", ureq.getLocale());
				olatKeys = new ArrayList();
				// adding order is relevant for the "if-else if"-statement below at events from step 3
				olatKeys.add(properyHandlersTrans.translate("form.name.institutionalUserIdentifier"));
				olatKeys.add(userTrans.translate("form.username"));
				olatKeys.add(userTrans.translate("form.email"));
				
				// add olatKeys and columnsNames to the form which displays it as pulldown menus
				removeAsListenerAndDispose(kwcForm);
				kwcForm = new KeyWithColumnForm(ureq, getWindowControl(), olatKeys, columnNames);
				listenTo(kwcForm);
				keywithcolVC.put("kwcForm", kwcForm.getInitialComponent());
				
				wc.setWizardTitle(translate("wizard.step2.title"));
				wc.setNextWizardStep(translate("wizard.step2.howto"), keywithcolVC);
			}
		}
		// events from step 2
		// preparing step 3
		else if (source == kwcForm){
			if (event == Event.DONE_EVENT) { // user clicked 'next'-button !!!!!!! 
				selectedColForOlatKey = Integer.parseInt(kwcForm.getSelectedColumn());
				selectedOlatKey = Integer.parseInt(kwcForm.getSelectedOlatKey());
				selectedValueOfOlatKey = (String) olatKeys.get(selectedOlatKey);
				colwithbulkactionVC = createVelocityContainer("colwithbulkaction");
				backLinkCol = LinkFactory.createLinkBack(colwithbulkactionVC, this);
				
				removeAsListenerAndDispose(sepValTableCtr);
				sepValTableCtr = new TableController(tableConfig, ureq, getWindowControl(), hcTranslator);
				listenTo(sepValTableCtr);
				
				columnNames = null;
				columnNames = new ArrayList();
				for(int i = 0;i < numOfValuesPerLine+1; i++){ // lenght+1 since adding the delimiter above
					if (i != selectedColForOlatKey){
						sepValTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ccc"+(i+1), i, null, ureq.getLocale()));
						// add every name of a column to a list deployed as pulldown to the user for matching column with olat key
						columnNames.add(translate("column", new String[]{""+(i+1)}));
					}else{
						sepValTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("hhh" + selectedValueOfOlatKey, i, null, ureq.getLocale()));
					}
				}
				sepValTableCtr.setTableDataModel(sepValModel);
				colwithbulkactionVC.put("sepValTable", sepValTableCtr.getInitialComponent());
				
				removeAsListenerAndDispose(cwbForm);
				cwbForm = new ColWithBulkActionForm(ureq, getWindowControl(), columnNames, bulkActions);
				listenTo(cwbForm);
				colwithbulkactionVC.put("cwbForm", cwbForm.getInitialComponent());
				
				wc.setWizardTitle(translate("wizard.step3.title"));
				wc.setNextWizardStep(translate("wizard.step3.howto"), colwithbulkactionVC);
			}
		} else if (source == cwbForm){
			if (event == Event.DONE_EVENT) { // user clicked 'next'-button !!!!!!!
				List rowsFourthStep = new ArrayList(rows.size());
				for (Iterator iter = rows.iterator(); iter.hasNext();) {
					Object[] values = (Object[]) iter.next();
					rowsFourthStep.add(values.clone());
				}
				String selectedColForBulk = cwbForm.getSelectedColumn();
				int colForBulk = Integer.parseInt(selectedColForBulk);
				// the selected column for the OLAT key was not more shown in the pulldownmenu for
				// for choosing the bulkaction, but it is not removed, therefore we have to increment
				// the colForBulk in certain cases
				if (selectedColForOlatKey <= colForBulk) colForBulk++; 
				String selectedBulk = cwbForm.getSelectedBulkAction();
				int bulk = Integer.parseInt(selectedBulk);
				finishedVC = createVelocityContainer ("finished");
				backLinkFin = LinkFactory.createLinkBack(finishedVC, this);
				
				GenericObjectArrayTableDataModel sepValFinModel = new GenericObjectArrayTableDataModel(rowsFourthStep, numOfLines);
					
				UserManager um = UserManager.getInstance();
				BaseSecurity secMgr = BaseSecurityManager.getInstance();
				
				BulkActionModule bam = new BulkActionModule();
				BulkAction ba = (BulkAction) bulkActions.get(bulk);
				List identities = new ArrayList(sepValFinModel.getRowCount());
				
				// read values from the column which the user has defined as olat key (e.g. username)
				// and add them to a list.
				for(int i = 0; i < sepValFinModel.getRowCount(); i ++ ){
					String val = (String) sepValFinModel.getValueAt(i, selectedColForOlatKey);
					val = val.trim();
					Identity identity = null;

					if (selectedOlatKey == 0) { // matrikelnumber
						Map<String, String> searchValue = new HashMap<String, String>();
						searchValue.put(UserConstants.INSTITUTIONALUSERIDENTIFIER, val);
						List identitiesFoundByInst = BaseSecurityManager.getInstance().getIdentitiesByPowerSearch(null, searchValue, true, null, null, null, null, null, null, null, null);
						//FIXME:as:b error handling if there is more than one identity found by institutionalUserIdentifier
						// see also in BulkAssessmentWizardController
						if (identitiesFoundByInst.size() == 1){
							identity = (Identity) identitiesFoundByInst.get(0);
						}
					}else if (selectedOlatKey == 1){ // username
							identity = secMgr.findIdentityByName(val);
					}else if (selectedOlatKey == 2) { // email
						identity = um.findIdentityByEmail(val);
					}
					identities.add(identity);
				}
				// get results from the user chosen bulk action for every identity
				List bulkResults = bam.apply(identities, ba);
				// add the bulk results to the data model
				for(int i = 0; i < sepValFinModel.getRowCount(); i++ ){
					String result = (String) bulkResults.get(i);
					sepValFinModel.setValueAt(result,i,colForBulk);
				}
				
				tableConfig.setDownloadOffered(true);
				
				removeAsListenerAndDispose(sepValFinTableCtr);
				sepValFinTableCtr = new TableController(tableConfig, ureq, getWindowControl(), hcTranslator);
				listenTo(sepValFinTableCtr);
				
				columnNames = null;
				columnNames = new ArrayList();
				for(int i = 0;i < numOfValuesPerLine+1; i++){
					if (i == selectedColForOlatKey){
						sepValFinTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("hhh" + selectedValueOfOlatKey, i, null, ureq.getLocale()));
					}else if (i == colForBulk){
						sepValFinTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("hhh" + ba.getDisplayName(), i, null, ureq.getLocale()));
					}else{
						sepValFinTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("ccc"+(i+1), i, null, ureq.getLocale()));
					}
				}
				
				sepValFinTableCtr.setTableDataModel(sepValFinModel);
				
				finishedVC.put("sepValTable", sepValFinTableCtr.getInitialComponent());

				wc.setWizardTitle(translate("wizard.step4.title"));
				wc.setNextWizardStep(translate("wizard.step4.howto"), finishedVC);
			}
		} else if (source == wc){
			if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
		}
	}

	protected void doDispose() {
		//
	}
}
