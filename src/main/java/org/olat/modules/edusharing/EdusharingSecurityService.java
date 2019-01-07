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
package org.olat.modules.edusharing;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 
 * Initial date: 4 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface EdusharingSecurityService {

	public EdusharingSignature createSignature() throws EdusharingException;
	
	public byte[] sign(PrivateKey privateKey, String data) throws EdusharingException;

	public boolean verify(PublicKey publicKey, byte[] realSig, String data) throws EdusharingException;

	public String encrypt(PublicKey publicKey, String plain) throws EdusharingException;

	public String decrypt(PrivateKey privateKey, String encrypted) throws EdusharingException;

	public KeyPair generateKeys() throws EdusharingException;

	public String getPublicKey(KeyPair kp);

	public PublicKey toPublicKey(String publicKey) throws EdusharingException;

	public String getPrivateKey(KeyPair kp);

	public PrivateKey toPrivateKey(String privateKey) throws EdusharingException;

}