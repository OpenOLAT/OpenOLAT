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
package org.olat.core.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * It's a wrapper for a ZIP input stream which MUST not be closed
 * at the end of the processing of an entry, but only at the end
 * of the whole ZIP processing.
 * 
 * <P>
 * Initial Date:  5 nov. 2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix
 */
public class ShieldInputStream extends InputStream {
	private final InputStream delegate;
	
	public ShieldInputStream(InputStream delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public int read() throws IOException {
		return delegate.read();
	}

	@Override
	public int available() throws IOException {
		return delegate.available();
	}

	@Override
	public void close() {
		// do nothing
	}

	@Override
	public synchronized void mark(int readlimit) {
		delegate.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return delegate.markSupported();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return delegate.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return delegate.read(b);
	}

	@Override
	public synchronized void reset() throws IOException {
		delegate.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return delegate.skip(n);
	}
}
