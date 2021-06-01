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
package org.olat.ims.qti21.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.MultipartFileInfos;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.ui.ResponseInput.Base64Input;
import org.olat.ims.qti21.ui.ResponseInput.FileInput;
import org.olat.ims.qti21.ui.ResponseInput.StringInput;

import uk.ac.ed.ph.jqtiplus.exception.QtiParseException;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 24.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractQtiWorksController extends FormBasicController {
	
	public static final String PNG_BASE64_PREFIX = "data:image/png;base64,";
		
	public AbstractQtiWorksController(UserRequest ureq, WindowControl wControl, String pageName) {
		super(ureq, wControl, pageName);
	}

	@Override
	protected void doDispose() {
		//
	}

	protected String extractComment() {
        if (mainForm.getRequestParameter("qtiworks_comment_presented") == null) {
            /* No comment box given to candidate */
            return null;
        }
        String comment = mainForm.getRequestParameter("qtiworks_comment");
        return StringHelper.containsNonWhitespace(comment) ? comment : null;
    }
	
	protected abstract Identifier getResponseIdentifierFromUniqueId(String uniqueId);
	
	protected void processResponse(UserRequest ureq, FormItem source) {
		Map<Identifier, ResponseInput> stringResponseMap = extractStringResponseData();
		Map<Identifier, ResponseInput> fileResponseMap;
		if(mainForm.isMultipartEnabled()) {
			fileResponseMap = extractFileResponseData();
		} else {
			fileResponseMap = Collections.emptyMap();
		}
		
		// Used for hints
		if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if(cmd != null && cmd.startsWith("qtiworks_response_")) {
				String responseIdentifierString = cmd.substring("qtiworks_response_".length());
				String presentedFlag = "qtiworks_presented_".concat(responseIdentifierString);
				if(mainForm.getRequestParameterSet().contains(presentedFlag)) {
					Identifier responseIdentifier;
					try {
						responseIdentifier = getResponseIdentifierFromUniqueId(responseIdentifierString);
						//Identifier.parseString(responseIdentifierString);
					} catch (final QtiParseException e) {
						throw new RuntimeException("Bad response identifier encoded in parameter " + cmd, e);
					}
					
					String[] responseValues;
					if(button.getUserObject() instanceof EndAttemptInteraction) {
						responseValues = new String[]{ ((EndAttemptInteraction)button.getUserObject()).getTitle() };
					} else {
						responseValues = new String[]{ "submit" };
					}
			        StringInput stringResponseData = new StringInput(responseValues);
					stringResponseMap.put(responseIdentifier, stringResponseData);
				}
			}
		}
		
		String candidateComment = extractComment();
		fireResponse(ureq, source, stringResponseMap, fileResponseMap, candidateComment);
	}
	
	protected void processPartialTemporaryResponse(UserRequest ureq) {
		Map<Identifier, ResponseInput> stringResponseMap = extractStringResponseData();

		String cmd = ureq.getParameter("tmpResponse");
		String responseIdentifierString = cmd.substring("qtiworks_response_".length());
		String presentedFlag = "qtiworks_presented_".concat(responseIdentifierString);
		if(mainForm.getRequestParameterSet().contains(presentedFlag)) {
			Identifier responseIdentifier;
			try {
				responseIdentifier = getResponseIdentifierFromUniqueId(responseIdentifierString);
				//Identifier.parseString(responseIdentifierString);
			} catch (final QtiParseException e) {
				throw new RuntimeException("Bad response identifier encoded in parameter " + cmd, e);
			}
			
			String[] responseValues = new String[]{ "submit" };
	        StringInput stringResponseData = new StringInput(responseValues);
			stringResponseMap.put(responseIdentifier, stringResponseData);
		}
		
		firePartialTemporaryResponse(ureq, stringResponseMap);
	}
	
	protected void processFullTemporaryResponse(UserRequest ureq) {
		Map<Identifier, ResponseInput> stringResponseMap = extractStringResponseData();
		Map<Identifier, ResponseInput> fileResponseMap;
		if(mainForm.isMultipartEnabled()) {
			fileResponseMap = extractFileResponseData();
		} else {
			fileResponseMap = Collections.emptyMap();
		}

		String candidateComment = extractComment();
		fireFullTemporaryResponse(ureq, stringResponseMap, fileResponseMap, candidateComment);
	}
	
	protected abstract void firePartialTemporaryResponse(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap);
	
	protected abstract void fireFullTemporaryResponse(UserRequest ureq,
			Map<Identifier, ResponseInput> stringResponseMap, Map<Identifier, ResponseInput> fileResponseMap,
			String comment);
	
	protected abstract void fireResponse(UserRequest ureq, FormItem source,
			Map<Identifier, ResponseInput> stringResponseMap, Map<Identifier, ResponseInput> fileResponseMap,
			String comment);
		
	protected Map<Identifier, ResponseInput> extractStringResponseData() {
        final Map<Identifier, ResponseInput> responseMap = new HashMap<>();

        final Set<String> parameterNames = mainForm.getRequestParameterSet();
        for (final String name : parameterNames) {
            if (name.startsWith("qtiworks_presented_")) {
                final String responseIdentifierString = name.substring("qtiworks_presented_".length());
                final Identifier responseIdentifier;
                try {
                	responseIdentifier = getResponseIdentifierFromUniqueId(responseIdentifierString);
                   // responseIdentifier = Identifier.parseString(responseIdentifierString);
                }
                catch (final QtiParseException e) {
                    //throw new BadResponseWebPayloadException("Bad response identifier encoded in parameter  " + name, e);
                	throw new RuntimeException("Bad response identifier encoded in parameter  " + name, e);
                }
                
                String[] responseBase64Values = mainForm.getRequestParameterValues("qtiworks_response_64_" + responseIdentifierString);
				if(responseBase64Values != null && responseBase64Values.length == 1) {
					//only used from drawing interaction as image/png
					String responseData = responseBase64Values[0];
					if(responseData.startsWith(PNG_BASE64_PREFIX)) {
	                		byte[] file = Base64.decodeBase64(responseData.substring(PNG_BASE64_PREFIX.length(), responseData.length()));
	                		final Base64Input stringResponseData = new Base64Input("image/png", file);
	                		responseMap.put(responseIdentifier, stringResponseData);
					}
				} else {
					final String[] responseValues = mainForm.getRequestParameterValues("qtiworks_response_" + responseIdentifierString);
					if(responseValues != null && responseValues.length > 0) {
						for(int i=responseValues.length; i-->0; ) {
							responseValues[i] = FilterFactory.getXMLValidCharacterFilter().filter(responseValues[i]);
						}
					}
                		final StringInput stringResponseData = new StringInput(responseValues);
                		responseMap.put(responseIdentifier, stringResponseData);
                }
            }
        }
        return responseMap;
    }
	
	protected Map<Identifier, ResponseInput> extractFileResponseData() {
		Map<Identifier, ResponseInput> fileResponseMap = new HashMap<>();

		Set<String> parameterNames = new HashSet<>(mainForm.getRequestMultipartFilesSet());
		parameterNames.addAll(mainForm.getRequestParameterSet());
		for (String name : parameterNames) {
			if (name.startsWith("qtiworks_uploadpresented_")) {
				String responseIdentifierString = name.substring("qtiworks_uploadpresented_".length());
				Identifier responseIdentifier;
				try {
					responseIdentifier = getResponseIdentifierFromUniqueId(responseIdentifierString);
					//responseIdentifier = Identifier.parseString(responseIdentifierString);
				} catch (final QtiParseException e) {
					throw new RuntimeException("Bad response identifier encoded in parameter " + name, e);
				}

				String multipartName = "qtiworks_uploadresponse_" + responseIdentifierString;
				MultipartFileInfos multipartFile = mainForm.getRequestMultipartFileInfos(multipartName);
				if (multipartFile == null) {
					throw new RuntimeException("Expected to find multipart file with name " + multipartName);
				}
				fileResponseMap.put(responseIdentifier, new FileInput(multipartFile));
			}
		}
		return fileResponseMap;
	}
}
