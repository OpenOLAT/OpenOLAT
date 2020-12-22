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
package org.olat.modules.bigbluebutton.ui;

import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.bigbluebutton.BigBlueButtonDispatcher;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingLayoutEnum;
import org.olat.modules.bigbluebutton.BigBlueButtonMeetingTemplate;

/**
 * 
 * Initial date: 14 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonUIHelper {
	
	public static void updateTemplateInformations(SingleSelection templateEl, TextElement externalLinkEl,
			SingleSelection publishingEl, SingleSelection recordEl, List<BigBlueButtonMeetingTemplate> templates) {
		templateEl.setExampleKey(null, null);
		if(templateEl.isOneSelected()) {
			BigBlueButtonMeetingTemplate template = getSelectedTemplate(templateEl, templates);
			if(template != null && template.getMaxParticipants() != null) {
				Integer maxConcurrentInt = template.getMaxConcurrentMeetings();
				String maxConcurrent = (maxConcurrentInt == null ? " ∞" : maxConcurrentInt.toString());
				String[] args = new String[] { template.getMaxParticipants().toString(), maxConcurrent};
				if(template.getWebcamsOnlyForModerator() != null && template.getWebcamsOnlyForModerator().booleanValue()) {
					templateEl.setExampleKey("template.explain.max.participants.with.webcams.mod", args);
				} else {
					templateEl.setExampleKey("template.explain.max.participants", args);
				}
			}
			if (externalLinkEl != null) {
				boolean visible = template != null && template.isExternalUsersAllowed();
				externalLinkEl.setVisible(visible);
				if(visible && !StringHelper.containsNonWhitespace(externalLinkEl.getValue())) {
					externalLinkEl.setValue(Long.toString(CodeHelper.getForeverUniqueID()));
				}
			}
			
			if(recordEl != null) {
				boolean recordVisible = template != null && template.getRecord() != null && template.getRecord().booleanValue();
				boolean wasVisible = recordEl.isVisible();
				recordEl.setVisible(recordVisible);
				if(!recordEl.isOneSelected() || (recordVisible && !wasVisible)) {
					recordEl.select("yes", true);	
				}
				
				if(publishingEl != null) {
					publishingEl.setVisible(recordVisible && "yes".equals(recordEl.getSelectedKey()));
				}
			}
		} else {
			if (externalLinkEl != null) {
				externalLinkEl.setVisible(false);
			}
			if(recordEl != null) {
				recordEl.setVisible(false);
			}
			if(publishingEl != null) {
				publishingEl.setVisible(false);
			}
		}
	}
	
	public static BigBlueButtonMeetingTemplate getSelectedTemplate(SingleSelection templateEl, List<BigBlueButtonMeetingTemplate> templates) {
		if (!templateEl.isOneSelected()) return null;
		
		String selectedTemplateId = templateEl.getSelectedKey();
		return templates.stream()
					.filter(tpl -> selectedTemplateId.equals(tpl.getKey().toString()))
					.findFirst()
					.orElse(null);
	}
	
	public static boolean isWebcamLayoutAvailable(BigBlueButtonMeetingTemplate template) {
		if(template == null) {
			return true;
		}
		return template.getWebcamsOnlyForModerator() == null || !template.getWebcamsOnlyForModerator().booleanValue();
	}
	
	public static void updateLayoutSelection(SingleSelection layoutEl, Translator translator, boolean webcamAvailable) {
		if(webcamAvailable && layoutEl.getKeys().length == 1) {
			KeyValues layoutKeyValues = new KeyValues();
			layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.standard.name(), translator.translate("layout.standard")));
			layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.webcam.name(), translator.translate("layout.webcam")));
			layoutEl.setKeysAndValues(layoutKeyValues.keys(), layoutKeyValues.values(), null);
		} else if(!webcamAvailable && layoutEl.getKeys().length > 1) {
			layoutEl.select(BigBlueButtonMeetingLayoutEnum.standard.name(), true);
			
			KeyValues layoutKeyValues = new KeyValues();
			layoutKeyValues.add(KeyValues.entry(BigBlueButtonMeetingLayoutEnum.standard.name(), translator.translate("layout.standard")));
			layoutEl.setKeysAndValues(layoutKeyValues.keys(), layoutKeyValues.values(), null);
		}
		
		layoutEl.setVisible(layoutEl.getKeys().length > 1);
	}

	public static boolean validateReadableIdentifier(TextElement externalLinkEl, BigBlueButtonMeeting meeting) {
		boolean allOk = true;
		
		externalLinkEl.clearError();
		if(externalLinkEl.isVisible()) {
			String identifier = externalLinkEl.getValue();
			if (StringHelper.containsNonWhitespace(externalLinkEl.getValue())) {
				if(identifier.length() > 64) {
					externalLinkEl.setErrorKey("form.error.toolong", new String[] { "64" });
					allOk &= false;
				} else if(getBigBlueButtonManager().isIdentifierInUse(identifier, meeting)) {
					externalLinkEl.setErrorKey("error.identifier.in.use", null);
					allOk &= false;
				} else {
					try {
						URI uri = new URI(BigBlueButtonDispatcher.getMeetingUrl(identifier));
						uri.normalize();
					} catch(Exception e) {
						externalLinkEl.setErrorKey("error.identifier.url.not.valid", new String[] { e.getMessage() });
						allOk &= false;
					}
				}
				externalLinkEl.setExampleKey("noTransOnlyParam", new String[] {BigBlueButtonDispatcher.getMeetingUrl(identifier)});			
			} else {
				externalLinkEl.setExampleKey(null, null);
			}
		}

		return allOk;
	}
	
	public static boolean validateTime(TextElement el, long maxValue) {
		boolean allOk = true;
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue())) {
			if(!StringHelper.isLong(el.getValue())) {
				el.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			} else if(Long.parseLong(el.getValue()) > maxValue) {
				el.setErrorKey("error.too.long.time", new String[] { Long.toString(maxValue) });
				allOk &= false;
			}
		}
		return allOk;
	}
	
	public static boolean validateDuration(DateChooser startDateEl, TextElement leadTimeEl, DateChooser endDateEl,
			TextElement followupTimeEl, BigBlueButtonMeetingTemplate template) {
		boolean allOk = true;
		
		Date start = startDateEl.getDate();
		Date end = endDateEl.getDate();
		long leadTime = getLongOrZero(leadTimeEl);
		long followupTime = getLongOrZero(followupTimeEl);
		if (!validateDuration(start, end, leadTime, followupTime, template)) {
				endDateEl.setErrorKey("error.duration", new String[] { template.getMaxDuration().toString() });
				allOk &= false;
		}
		return allOk;
	}
	
	public static boolean validateDuration(DateChooser startEndDateEl, TextElement leadTimeEl,
			TextElement followupTimeEl, BigBlueButtonMeetingTemplate template) {
		boolean allOk = true;
		
		Date start = startEndDateEl.getDate();
		Date end = startEndDateEl.getSecondDate();
		long leadTime = getLongOrZero(leadTimeEl);
		long followupTime = getLongOrZero(followupTimeEl);
		if (!validateDuration(start, end, leadTime, followupTime, template)) {
			startEndDateEl.setErrorKey("error.duration", new String[] { template.getMaxDuration().toString() });
				allOk &= false;
		}
		return allOk;
	}

	public static boolean validateDuration(Date start, Date end, long leadTime, long followupTime,
			BigBlueButtonMeetingTemplate template) {
		if(template != null && template.getMaxDuration() != null && start != null && end != null) {
			// all calculation in milli-seconds
			long realStart = start.getTime() - (60 * 1000 * leadTime);
			long realEnd = end.getTime() + (60 * 1000 * followupTime);
			long duration = realEnd - realStart;
			long maxDuration  = (60 * 1000 * template.getMaxDuration());
			return maxDuration >= duration;
		}
		return true;
	}
	
	public static boolean validateSlot(DateChooser startDateEl, TextElement leadTimeEl, DateChooser endDateEl,
			TextElement followupTimeEl, BigBlueButtonMeeting meeting, BigBlueButtonMeetingTemplate template) {
		boolean allOk = true;
		
		Date start = startDateEl.getDate();
		Date end = endDateEl.getDate();
		long leadTime = getLongOrZero(leadTimeEl);
		long followupTime = getLongOrZero(followupTimeEl);
		boolean slotFree = validateSlot(meeting, template, start, end, leadTime, followupTime);
		if(!slotFree) {
			startDateEl.setErrorKey("server.overloaded", null);
			allOk &= false;
		}
		
		return allOk;
	}

	public static boolean validateSlot(BigBlueButtonMeeting meeting, BigBlueButtonMeetingTemplate template, Date start,
			Date end, long leadTime, long followupTime) {
		return getBigBlueButtonManager().isSlotAvailable(meeting, template, start, leadTime, end, followupTime);
	}
	
	public static boolean validatePermanentSlot(SingleSelection templateEl, BigBlueButtonMeeting meeting, BigBlueButtonMeetingTemplate template) {
		boolean allOk = true;
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 12);
		Date endDate = cal.getTime();
		
		boolean slotFree = getBigBlueButtonManager().isSlotAvailable(meeting, template,
				new Date(), 0, endDate, 0);
		if(!slotFree) {
			templateEl.setErrorKey("server.overloaded", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	public static long getLongOrZero(TextElement textElement) {
		long followupTime = 0;
		if(textElement.isVisible() && StringHelper.isLong(textElement.getValue())) {
			followupTime = Long.valueOf(textElement.getValue());
		}
		return followupTime;
	}
	
	public static boolean isRecord(BigBlueButtonMeetingTemplate template) {
		return template != null && template.getRecord() != null && template.getRecord().booleanValue();
	}
	
	public static boolean isRecord(BigBlueButtonMeeting meeting) {
		boolean record = meeting != null && meeting.getTemplate() != null
				&& meeting.getTemplate().getRecord() != null
				&& meeting.getTemplate().getRecord().booleanValue();
		// override only if template enables recording
		if(record && meeting.getRecord() != null) {
			record = meeting.getRecord().booleanValue();
		}
		return record;
	}
	
	public static final String getServerNameFromUrl(String url) {
		if(url == null) return null;
		
		int index = url.indexOf("://");
		if(index >= 0) {
			url = url.substring(index + 3);
		}
		int slashIndex = url.indexOf('/');
		if(slashIndex >= 0) {
			url = url.substring(0, slashIndex);
		}
		return url;
	}
	
	private static BigBlueButtonManager getBigBlueButtonManager() {
		return CoreSpringFactory.getImpl(BigBlueButtonManager.class);
	}
	
}
