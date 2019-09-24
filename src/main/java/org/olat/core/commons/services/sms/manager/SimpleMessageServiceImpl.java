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
package org.olat.core.commons.services.sms.manager;


import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.sms.MessageLog;
import org.olat.core.commons.services.sms.MessagesSPI;
import org.olat.core.commons.services.sms.SimpleMessageException;
import org.olat.core.commons.services.sms.SimpleMessageModule;
import org.olat.core.commons.services.sms.SimpleMessageService;
import org.olat.core.commons.services.sms.model.MessageStatistics;
import org.olat.core.commons.services.sms.ui.AbstractSMSConfigurationController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class SimpleMessageServiceImpl implements SimpleMessageService {
	
	private static final Logger log = Tracing.createLoggerFor(SimpleMessageServiceImpl.class);
	private static final Random rnd = new Random();
	
	@Autowired
	private MessageLogDAO messageLogDao;
	
	@Autowired
	private SimpleMessageModule messageModule;
	@Autowired
	private List<MessagesSPI> messagesSpiList;

	@Override
	public List<MessagesSPI> getMessagesSpiList() {
		return messagesSpiList;
	}

	@Override
	public String generateToken() {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<6; i++) {
			int n = Math.round(rnd.nextFloat() * 9.0f);
			if(n < 0) {
				n = 0;
			} else if(n > 9) {
				n = 9;
			}
			sb.append(n);
		}
		return sb.toString();
	}

	@Override
	public boolean validate(String number) {
		if(!StringHelper.containsNonWhitespace(number)) return false;
		number = number.replace("+", "").replace(" ", "");
		if(StringHelper.isLong(number)) {
			try {
				Long phone = Long.valueOf(number);
				return phone > 0;
			} catch (NumberFormatException e) {
				//
			}
		}
		return false;
	}

	@Override
	public void sendMessage(String text, Identity recipient) throws SimpleMessageException {
		String telNumber = recipient.getUser().getSmsTelMobile();
		sendMessage(text, telNumber, recipient);
	}

	@Override
	public void sendMessage(String text, String telNumber, Identity recipient) throws SimpleMessageException {
		MessagesSPI spi = getMessagesSpi();
		MessageLog mLog = messageLogDao.create(recipient, spi.getId());
		boolean allOk = spi.send(mLog.getMessageUuid(), text, telNumber);
		mLog.setServerResponse(Boolean.toString(allOk));
		messageLogDao.save(mLog);
		log.info(Tracing.M_AUDIT, "SMS send: " + allOk + " to " + recipient + " with number: " + telNumber);
	}

	@Override
	public List<MessageStatistics> getStatisticsPerMonth() {
		MessagesSPI selectedSpi = getMessagesSpi();
		return messageLogDao.getStatisticsPerMonth(selectedSpi.getId());
	}

	@Override
	public MessagesSPI getMessagesSpi(String serviceId) {
		MessagesSPI spi = null;
		if("devnull".equals(serviceId)) {
			spi = new DevNullProvider();
		} else if(messagesSpiList != null) {
			for(MessagesSPI mSpi:messagesSpiList) {
				if(mSpi.getId().equals(serviceId)) {
					spi = mSpi;
				}
			}
		}
		return spi;
	}

	@Override
	public MessagesSPI getMessagesSpi() {
		if(Settings.isDebuging()) return new DevNullProvider();

		if(messageModule.isEnabled() && !messagesSpiList.isEmpty()) {
			String providerId = messageModule.getProviderId();
			if(StringHelper.containsNonWhitespace(providerId) && !"devnull".equals(providerId)) {
				for(MessagesSPI spi:messagesSpiList) {
					if(providerId.equalsIgnoreCase(spi.getId())) {
						return spi;
					}
				}
			}
		}
		return new DevNullProvider();
	}
	
	private static class DevNullProvider implements MessagesSPI {
		
		@Override
		public String getId() {
			return "devnull";
		}

		@Override
		public String getName() {
			return "/dev/null";
		}

		@Override
		public boolean isValid() {
			return true;
		}

		@Override
		public AbstractSMSConfigurationController getConfigurationController(UserRequest ureq, WindowControl wControl, Form form) {
			return null;
		}

		@Override
		public boolean send(String messageId, String text, String recipient) {
			log.info("Send: " + text);
			return true;
		}
	}
}