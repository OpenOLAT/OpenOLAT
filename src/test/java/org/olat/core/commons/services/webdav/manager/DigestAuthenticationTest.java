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
package org.olat.core.commons.services.webdav.manager;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 28.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DigestAuthenticationTest {
	
	@Test
	public void parseDigestAuthentication() {
		String request = "Digest username=\"kanu\",realm=\"OLAT WebDAV Access\",nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\",uri=\"/webdav\",cnonce=\"195d0d15b31ee8f0a7f243b9bfcd881d\",nc=00000001,response=\"671742b00fae8d4c8ceb6a5bcf2b36fa\",qop=\"auth\"";
		DigestAuthentication auth = DigestAuthentication.parse(request);
		Assert.assertEquals("kanu", auth.getUsername());
		Assert.assertEquals("OLAT WebDAV Access", auth.getRealm());
		Assert.assertEquals("dcd98b7102dd2f0e8b11d0f600bfb0c093", auth.getNonce());
		Assert.assertEquals("/webdav", auth.getUri());
		Assert.assertEquals("195d0d15b31ee8f0a7f243b9bfcd881d", auth.getCnonce());
		Assert.assertEquals("00000001", auth.getNc());
		Assert.assertEquals("671742b00fae8d4c8ceb6a5bcf2b36fa", auth.getResponse());
		Assert.assertEquals("auth", auth.getQop());
	}
	
	@Test
	public void parseDigestAuthentication_withSpecialCharacters() throws IOException {
		String request = "Digest username=\"kanu\",realm=\"OLAT WebDAV Access\",nonce=\"f1794336f940449a91c0214d39a45538\",uri=\"/olat/webdav/groupfolders/Ajax%20Group/Test%201/Grobplanung,%20rollende%20Planung%20Formular.xls\",cnonce=\"5128af45c016b7590f136fcf1152ebaf\",nc=00000001,response=\"2c56db720b5bb34f1887d4b8f8a41f51\",qop=\"auth\"";
		
		DigestAuthentication auth = DigestAuthentication.parse(request);
		Assert.assertEquals("kanu", auth.getUsername());
		Assert.assertEquals("OLAT WebDAV Access", auth.getRealm());
		Assert.assertEquals("f1794336f940449a91c0214d39a45538", auth.getNonce());
		Assert.assertEquals("/olat/webdav/groupfolders/Ajax%20Group/Test%201/Grobplanung,%20rollende%20Planung%20Formular.xls", auth.getUri());
		Assert.assertEquals("5128af45c016b7590f136fcf1152ebaf", auth.getCnonce());
		Assert.assertEquals("00000001", auth.getNc());
		Assert.assertEquals("2c56db720b5bb34f1887d4b8f8a41f51", auth.getResponse());
		Assert.assertEquals("auth", auth.getQop());
	}

}
