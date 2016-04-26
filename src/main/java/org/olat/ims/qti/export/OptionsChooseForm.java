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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.export;

import java.util.Collection;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;



/**
 * Initial Date: June 07, 2006 <br>
 * 
 * @author Alexander Schneider
 */
public class OptionsChooseForm extends StepFormBasicController {
  
  private static final String SCQ_ITEMCOLS = "scqitemcols";
  private static final String SCQ_POSCOL = "scqposcol";
  private static final String SCQ_POINTCOL = "scqpointcol";
  private static final String SCQ_TIMECOLS = "scqtimecols";
  
  private static final String MCQ_ITEMCOLS = "mcqitemcols";
  private static final String MCQ_POSCOL = "mcqposcol";
  private static final String MCQ_POINTCOL = "mcqpointcol";
  private static final String MCQ_TIMECOLS = "mcqtimecols";
  
  private static final String KPRIM_ITEMCOLS = "kprimitemcols";
  private static final String KPRIM_POINTCOL = "kprimpointcol";
  private static final String KPRIM_TIMECOLS = "kprimtimecols";
  
  private static final String FIB_ITEMCOLS = "fibitemcols";
  private static final String FIB_POINTCOL = "fibpointcol";
  private static final String FIB_TIMECOLS = "fibtimecols";
  
  private static final String ESSAY_ITEMCOLS = "essayitemcols";
  private static final String ESSAY_TIMECOLS = "essaytimecols";
  
  private boolean hasSCQ = false;
  private boolean hasMCQ = false;
  private boolean hasKRIM = false;
  private boolean hasFIB = false;
  private boolean hasEssay = false;
  
  private Map<Class<?>, QTIExportItemFormatConfig> mapWithConfigs;
  
  private MultipleSelectionElement scq, mcq, kprim, fib, essay;
  private String[] scqKeys, mcqKeys, kprimKeys, fibKeys, essayKeys;
  private String[] scqVals, mcqVals, kprimVals, fibVals, essayVals;
  
  
  
	public OptionsChooseForm(UserRequest ureq, WindowControl wControl,
			Map<Class<?>, QTIExportItemFormatConfig> mapWithConfigs,
			StepsRunContext runContext, Form form) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		
		this.mapWithConfigs = mapWithConfigs;

		hasSCQ   = mapWithConfigs.get(QTIExportSCQItemFormatConfig.class)   != null;
		hasMCQ   = mapWithConfigs.get(QTIExportMCQItemFormatConfig.class)   != null;
		hasKRIM  = mapWithConfigs.get(QTIExportKPRIMItemFormatConfig.class) != null;
		hasFIB   = mapWithConfigs.get(QTIExportFIBItemFormatConfig.class)   != null;
		hasEssay = mapWithConfigs.get(QTIExportEssayItemFormatConfig.class) != null;
		
		scqKeys = new String[]{SCQ_ITEMCOLS, SCQ_POSCOL, SCQ_POINTCOL, SCQ_TIMECOLS};
		scqVals = new String[] {
				translate("form.itemcols"),
				translate("form.poscol"),
				translate("form.pointcol"),
				translate("form.timecols")
		};
		
		mcqKeys = new String[]{MCQ_ITEMCOLS, MCQ_POSCOL, MCQ_POINTCOL, MCQ_TIMECOLS};
		mcqVals = new String[] {
				translate("form.itemcols"),
				translate("form.poscol"),
				translate("form.pointcol"),
				translate("form.timecols")
		};
		
		kprimKeys = new String[]{KPRIM_ITEMCOLS, KPRIM_POINTCOL, KPRIM_TIMECOLS};
		kprimVals = new String[] {
				translate("form.itemcols"),
				translate("form.pointcol"),
				translate("form.timecols")
		};
		
		fibKeys = new String[]{FIB_ITEMCOLS, FIB_POINTCOL, FIB_TIMECOLS};
		fibVals = new String[] {
				translate("form.itemcols"),
				translate("form.pointcol"),
				translate("form.timecols")
		};
		
		essayKeys = new String[]{ESSAY_ITEMCOLS, ESSAY_TIMECOLS};
		essayVals = new String[] {
				translate("form.itemcols"),
				translate("form.timecols")
		};
		
		initForm (ureq);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		
		// Single Choice
		if(hasSCQ){
			QTIExportSCQItemFormatConfig c = (QTIExportSCQItemFormatConfig) mapWithConfigs.get(QTIExportSCQItemFormatConfig.class);
			Collection<String> s = scq.getSelectedKeys();
			c.setResponseCols(s.contains(SCQ_ITEMCOLS));
			c.setPositionsOfResponsesCol(s.contains(SCQ_POSCOL));
			c.setPointCol(s.contains(SCQ_POINTCOL));
			c.setTimeCols(s.contains(SCQ_TIMECOLS));
		}
		
		// Multiple Choice
		if(hasMCQ){
			QTIExportMCQItemFormatConfig c = (QTIExportMCQItemFormatConfig) mapWithConfigs.get(QTIExportMCQItemFormatConfig.class);
			Collection<String> s = mcq.getSelectedKeys();
			c.setResponseCols(s.contains(MCQ_ITEMCOLS));
			c.setPositionsOfResponsesCol(s.contains(MCQ_POSCOL));
			c.setPointCol(s.contains(MCQ_POINTCOL));
			c.setTimeCols(s.contains(MCQ_TIMECOLS));
		}
		
		// KPRIM
		if(hasKRIM){
			QTIExportKPRIMItemFormatConfig c = (QTIExportKPRIMItemFormatConfig) mapWithConfigs.get(QTIExportKPRIMItemFormatConfig.class);
			Collection<String> s = kprim.getSelectedKeys();
			c.setResponseCols(s.contains(KPRIM_ITEMCOLS));
			c.setPointCol(s.contains(KPRIM_POINTCOL));
			c.setTimeCols(s.contains(KPRIM_TIMECOLS));
		}
		
		// Fill in the Blank
		if(hasFIB){
			QTIExportFIBItemFormatConfig c = (QTIExportFIBItemFormatConfig) mapWithConfigs.get(QTIExportFIBItemFormatConfig.class);
			Collection<String> s = fib.getSelectedKeys();
			c.setResponseCols(s.contains(FIB_ITEMCOLS));
			c.setPointCol(s.contains(FIB_POINTCOL));
			c.setTimeCols(s.contains(FIB_TIMECOLS));
		}
		
		// Essay
		if(hasEssay){
			QTIExportEssayItemFormatConfig c = (QTIExportEssayItemFormatConfig) mapWithConfigs.get(QTIExportEssayItemFormatConfig.class);
			Collection<String> s = essay.getSelectedKeys();
			c.setResponseCols(s.contains(ESSAY_ITEMCOLS));
			c.setTimeCols(s.contains(ESSAY_TIMECOLS));
		}
		
		return true;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		scq = uifactory.addCheckboxesVertical("scq", "form.scqtitle", formLayout, scqKeys, scqVals, 1);
		if(hasSCQ){
			QTIExportItemFormatConfig c = mapWithConfigs.get(QTIExportSCQItemFormatConfig.class);
			scq.select(SCQ_ITEMCOLS, c.hasResponseCols());
			scq.select(SCQ_POSCOL,   c.hasPositionsOfResponsesCol());
			scq.select(SCQ_POINTCOL, c.hasPointCol());
			scq.select(SCQ_TIMECOLS, c.hasTimeCols());
		} else {
			scq.setVisible(false);
		}

		mcq = uifactory.addCheckboxesVertical("mcq", "form.mcqtitle", formLayout, mcqKeys, mcqVals, 1);
		if(hasMCQ){
			QTIExportItemFormatConfig c = mapWithConfigs.get(QTIExportMCQItemFormatConfig.class);
			mcq.select(MCQ_ITEMCOLS, c.hasResponseCols());
			mcq.select(MCQ_POSCOL,   c.hasPositionsOfResponsesCol());
			mcq.select(MCQ_POINTCOL, c.hasPointCol());
			mcq.select(MCQ_TIMECOLS, c.hasTimeCols());
		} else {
			mcq.setVisible(false);
		}
		
		kprim = uifactory.addCheckboxesVertical("kprim", "form.kprimtitle", formLayout, kprimKeys, kprimVals, 1);
		if(hasKRIM){
			QTIExportItemFormatConfig c = mapWithConfigs.get(QTIExportKPRIMItemFormatConfig.class);
			kprim.select(KPRIM_ITEMCOLS, c.hasResponseCols());
			kprim.select(KPRIM_POINTCOL, c.hasPointCol());
			kprim.select(KPRIM_TIMECOLS, c.hasTimeCols());
		} else {
			kprim.setVisible(false);
		}
		
		fib = uifactory.addCheckboxesVertical("fib", "form.fibtitle", formLayout, fibKeys, fibVals, 1);
		if(hasFIB){
			QTIExportItemFormatConfig c = mapWithConfigs.get(QTIExportFIBItemFormatConfig.class);
			fib.select(FIB_ITEMCOLS, c.hasResponseCols());
			fib.select(FIB_POINTCOL, c.hasPointCol());
			fib.select(FIB_TIMECOLS, c.hasTimeCols());
		} else {
			fib.setVisible(false);
		}
		
		essay = uifactory.addCheckboxesVertical("essay", "form.essaytitle", formLayout, essayKeys, essayVals, 1);
		if(hasEssay){
			QTIExportItemFormatConfig c = mapWithConfigs.get(QTIExportEssayItemFormatConfig.class);
			essay.select(FIB_ITEMCOLS, c.hasResponseCols());
			essay.select(FIB_TIMECOLS, c.hasTimeCols());
		} else {
			essay.setVisible(false);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
}
