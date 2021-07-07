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

import java.io.File;

import org.olat.core.gui.components.form.flexible.impl.MultipartFileInfos;

/**
 * 
 * @author srosse, stephane.rosse@cyberiacafe.ch, http://www.frentix.com
 *
 */
public abstract class ResponseInput {
	
	public static class StringInput extends ResponseInput {
		
	    private final String[] responseData;
	    
	    public StringInput(final String[] responseData) {
	        this.responseData = responseData;

	    }
	    
	    public String[] getResponseData() {
	    	return responseData;
	    }
	}
	
	public static class Base64Input extends ResponseInput {
		
		private final byte[] responseData;
		private final byte[] responseCompanionData;
		private final String contentType;
		
		public Base64Input(String contentType, byte[] responseData, byte[] responseCompanionData) {
			this.contentType = contentType;
			this.responseData = responseData;
			this.responseCompanionData = responseCompanionData;
		}
		
		public String getContentType() {
			return contentType;
		}
		
		public byte[] getResponseData() {
			return responseData;
		}
		
		public byte[] getResponseCompanionData() {
			return responseCompanionData;
		}
	}
	
	public static class FileInput extends ResponseInput {
		
		private final MultipartFileInfos multipartFileInfos;
		
		public FileInput(MultipartFileInfos multipartFileInfos) {
			this.multipartFileInfos = multipartFileInfos;
		}
		
		public MultipartFileInfos getMultipartFileInfos() {
			return multipartFileInfos;
		}
		
		public boolean isEmpty() {
			return multipartFileInfos.isEmpty();
		}

		public File getFile() {
			return multipartFileInfos.getFile();
		}

		public String getFileName() {
			return multipartFileInfos.getFileName();
		}

		public String getContentType() {
			return multipartFileInfos.getContentType();
		}	
	}
}
