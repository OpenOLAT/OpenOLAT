//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;


/**
 * 
 * Description:<br>
 * Form for standard options of the virtual classroom course node.
 * 
 * <P>
 * Initial Date:  18.01.2011 <br>
 * @author skoeber
 */
public class VCEditForm extends FormBasicController {
	
	// GUI
  private FormLayoutContainer editVC;
  private FormSubmit submit;
  private List<TextElement> vcTitleInputList;
  private List<TextElement> vcDescriptionInputList;
  private List<DateChooser> vcCalenderbeginInputList;
  private List<TextElement> vcDurationInputList;
  private List<FormLink> vcDelButtonList;
  private List<FormLink> vcAddButtonList;
  private SingleSelection vcTemplate;
  private MultipleSelectionElement multiSelectOptions;
  private static String OPTION_DATES = "vc.access.dates";
  
  // data
  private DefaultVCConfiguration config;
  private Map<String,String> templates = new HashMap<String, String>();
  private List<MeetingDate> dateList = new ArrayList<MeetingDate>();

  private int counter = 0;

	public VCEditForm(UserRequest ureq, WindowControl wControl, Map<String, String> templates, DefaultVCConfiguration config) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		this.config = config;
		this.templates.putAll(templates);
		
		// read existing dates from config
		if(config.getMeetingDates() != null) dateList.addAll(config.getMeetingDates());

    this.vcTitleInputList = new ArrayList<TextElement>(dateList.size());
    this.vcDescriptionInputList = new ArrayList<TextElement>(dateList.size());
    this.vcCalenderbeginInputList = new ArrayList<DateChooser>(dateList.size());
    this.vcDurationInputList = new ArrayList<TextElement>(dateList.size());
    this.vcAddButtonList = new ArrayList<FormLink>(dateList.size());
    this.vcDelButtonList = new ArrayList<FormLink>(dateList.size());

    initForm(this.flc, this, ureq);
	}

  @Override
  protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
    editVC = FormLayoutContainer.createCustomFormLayout("titleLayout", getTranslator(), velocity_root + "/editForm.html");
    formLayout.add(editVC);

    // template chooser
    String[] keys = new String[templates.size() + 1];
    keys[0] = DefaultVCConfiguration.DEFAULT_TEMPLATE;
    String[] values = new String[templates.size() + 1];
    values[0] = "";
    int index = 1;
    for(String key:templates.keySet()) {
      keys[index] = key;
      values[index] = templates.get(key);
      index++;
    }
    boolean hasTemplates = templates.size() > 0;
    if(hasTemplates) {
      vcTemplate = uifactory.addDropdownSingleselect("vc.template.choose", "vc.template.choose.label", editVC, keys, values, null);
      String templateKey = config.getTemplateKey();
      vcTemplate.select(templateKey == null ? DefaultVCConfiguration.DEFAULT_TEMPLATE : templateKey, true);
    }
    editVC.contextPut("hasTemplates", hasTemplates);
    
    // meeting options
    boolean useDates = !dateList.isEmpty() | config.isUseMeetingDates();
    String[] accessKeys = new String[] {OPTION_DATES};
  	String[] accessVals = new String[] {translate(OPTION_DATES)};
  	multiSelectOptions = uifactory.addCheckboxesVertical("vc.options", "vc.options.label", editVC, accessKeys, accessVals, 1);
  	multiSelectOptions.select(OPTION_DATES, useDates);
  	multiSelectOptions.addActionListener(FormEvent.ONCHANGE);

    // create gui elements for all meetings
    editVC.contextPut("useDates", useDates);
    if(useDates) addDates();

    editVC.contextPut("dateList", dateList);
    editVC.contextPut("vcTitleInputList", vcTitleInputList);
    editVC.contextPut("vcDescriptionInputList", vcDescriptionInputList);
    editVC.contextPut("vcCalenderbeginInputList", vcCalenderbeginInputList);
    editVC.contextPut("vcDurationInputList", vcDurationInputList);
    editVC.contextPut("vcAddButtonList", vcAddButtonList);
    editVC.contextPut("vcDelButtonList", vcDelButtonList);

    submit = new FormSubmit("subm", "submit");

    formLayout.add(submit);
  }
  
  private void addDates() {
  	if (dateList.isEmpty()) {
  		MeetingDate meetingData = new MeetingDate();
  		meetingData.setBegin(new Date());
  		meetingData.setEnd(new Date(meetingData.getBegin().getTime() + 1000*60*60));
  		dateList.add(meetingData);
  	}
  	for (int i = 0; i < dateList.size(); i++) {
  		MeetingDate date = dateList.get(i);
  		addRow(i, date);
  	}
  }
  
  private void removeDates() {
  	for (int i = 0; i < dateList.size(); i++) {
  		removeRow(i);
  	}
  }

  @Override
  protected void doDispose() {
    // nothing to dispose
  }

  @Override
  protected void formOK(UserRequest ureq) {
    // read data from form elements
    for (int i = 0; i < dateList.size(); i++) {
      MeetingDate date = dateList.get(i);
      String dateValue = vcTitleInputList.get(i).getValue();
      date.setTitle(dateValue);

      StringTokenizer strTok = new StringTokenizer(vcDurationInputList.get(i).getValue(), ":", false);
      long dur = 1000 * 60 * 60 * Long.parseLong(strTok.nextToken()) + 1000 * 60 * Long.parseLong(strTok.nextToken());

      date.setBegin(vcCalenderbeginInputList.get(i).getDate());
      date.setEnd(new Date(date.getBegin().getTime() + dur));
      date.setDescription(vcDescriptionInputList.get(i).getValue());
    }
    boolean useDates = multiSelectOptions.getSelectedKeys().contains(OPTION_DATES);
    config.setUseMeetingDates(useDates);
    if(useDates) config.setMeetingDatas(dateList);
    if(!templates.isEmpty() && vcTemplate.isOneSelected()) config.setTemplateKey(vcTemplate.getSelectedKey());
    fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
  }

  @Override
  protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
    if (source.getComponent() instanceof Link) {
      if (vcAddButtonList.contains(source)) {
        Long row = new Long(vcAddButtonList.indexOf(source));
        int index = row.intValue() + 1;
        MeetingDate meetingData = new MeetingDate();
        meetingData.setBegin(new Date());
        meetingData.setEnd(new Date(meetingData.getBegin().getTime() + 1000*60*60));
        dateList.add(index, meetingData);
        addRow(index, meetingData);
      } else if (vcDelButtonList.contains(source)) {
        // special case: only one line existent
        if (dateList.size() == 1) {
          // clear this line
          vcTitleInputList.get(0).setValue("");
          vcDescriptionInputList.get(0).setValue("");
          vcCalenderbeginInputList.get(0).setDate(new Date());
          vcDurationInputList.get(0).setValue("01:00");
        } else {
          int row = vcDelButtonList.indexOf(source);
          removeRow(row);
        }
      }
    } else if(source == multiSelectOptions) {
    	boolean useDates = multiSelectOptions.getSelectedKeys().contains(OPTION_DATES);
    	if(useDates) addDates();
    	else removeDates();
    	editVC.contextRemove("useDates");
    	editVC.contextPut("useDates", useDates);
    	editVC.setDirty(true);
    }
    super.formInnerEvent(ureq, source, event);
  }

  private void addRow(int index, final MeetingDate date) {
    // title
    TextElement vcTitle = uifactory.addTextElement("title" + counter, null, -1, date.getTitle(), editVC);
    vcTitle.setDisplaySize(30);
    vcTitle.setMandatory(true);
    vcTitle.setNotEmptyCheck("vc.table.title.empty");
    vcTitleInputList.add(index, vcTitle);

    // description
    TextElement vcDescription = uifactory.addTextElement("description" + counter, null, -1, date.getDescription(), editVC);
    vcDescription.setDisplaySize(20);
    vcDescription.setNotEmptyCheck("vc.table.description.empty");
    vcDescription.setMandatory(true);
    vcDescriptionInputList.add(index, vcDescription);

    // begin
    DateChooser vcScheduleDate = uifactory.addDateChooser("begin" + counter, "vc.table.begin", null, editVC);
    vcScheduleDate.setNotEmptyCheck("vc.table.begin.empty");
    vcScheduleDate.setValidDateCheck("vc.table.begin.error");
    vcScheduleDate.setMandatory(true);
    vcScheduleDate.setDisplaySize(20);
    vcScheduleDate.setDateChooserTimeEnabled(true);
    vcScheduleDate.setDate(date.getBegin());
    vcCalenderbeginInputList.add(index, vcScheduleDate);

    // add date duration
    SimpleDateFormat sdDuration = new SimpleDateFormat("HH:mm");
    TimeZone tz = TimeZone.getTimeZone("Etc/GMT+0");
    sdDuration.setTimeZone(tz);

    TextElement vcDuration = uifactory.addTextElement("duration" + counter, "vc.table.duration", 5, String.valueOf(0), editVC);
    vcDuration.setDisplaySize(5);
    vcDuration.setValue(sdDuration.format(new Date(date.getEnd().getTime() - date.getBegin().getTime())));
    vcDuration.setRegexMatchCheck("\\d{1,2}:\\d\\d", "form.error.format");
    vcDuration.setExampleKey("vc.table.duration.example", null);
    vcDuration.setNotEmptyCheck("vc.table.duration.empty");
    vcDuration.setErrorKey("vc.table.duration.error", null);
    vcDuration.setMandatory(true);
    vcDuration.showExample(true);
    vcDuration.showError(false);
    this.vcDurationInputList.add(index, vcDuration);

    // add row button
    FormLink addButton = new FormLinkImpl("add" + counter, "add" + counter, "vc.table.add", Link.BUTTON_SMALL);
    editVC.add(addButton);
    vcAddButtonList.add(index, addButton);

    // remove row button
    FormLink delButton = new FormLinkImpl("delete" + counter, "delete" + counter, "vc.table.delete", Link.BUTTON_SMALL);
    editVC.add(delButton);
    vcDelButtonList.add(index, delButton);

    // increase the counter to enable unique component names
    counter++;
  }

  private void removeRow(int row) {
    // remove date from model list
    if(dateList.get(row) != null) dateList.remove(row);

    editVC.remove(vcTitleInputList.remove(row));
    editVC.remove(vcDescriptionInputList.remove(row));
    editVC.remove(vcDurationInputList.remove(row));
    editVC.remove(vcCalenderbeginInputList.remove(row));
    editVC.remove(vcAddButtonList.remove(row));
    editVC.remove(vcDelButtonList.remove(row));

    // decrease the counter for unique component names
    counter--;
  }

}
//</OLATCE-103>