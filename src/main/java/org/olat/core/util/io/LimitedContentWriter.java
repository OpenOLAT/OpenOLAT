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

import java.io.Writer;

/**
 * 
 * Initial date: 21.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LimitedContentWriter extends Writer {

	private final StringBuilder sb;
	private final int maxSize;

	/**
	 * @param len
	 */
	public LimitedContentWriter(int len, int maxSize) {
		sb = new StringBuilder(Math.min(len, maxSize));
		this.maxSize = maxSize;
	}
	
	protected LimitedContentWriter(int len) {
		this(len, Integer.MAX_VALUE);
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		if(accept() && len + sb.length() < maxSize) {
			sb.append(cbuf, off, len);
		}
	}
	
	@Override
	public Writer append(CharSequence seq, int start, int end) {
		if(accept()) {
			if((end - start) + sb.length() < maxSize) {
				sb.append(seq, start, end);
			} else {
				sb.append(seq, start, start + (maxSize - sb.length()));
			}
		}
		return this;
	}

	@Override
	public Writer append(CharSequence csq) {
		if(accept()) {
			if(csq.length() + sb.length() < maxSize) {
				sb.append(csq);
			} else {
				sb.append(csq, 0, maxSize - sb.length());
			}
		}
		return this;
	}

	@Override
	public Writer append(char c) {
		if(accept()) sb.append(c);
		return this;
	}
	
	public final boolean accept() {
		return sb.length() < maxSize;
	}

	@Override
	public void flush() {
		//
	}

	@Override
	public void close() {
		//
	}
	
	public int length() {
		return sb.length();
	}
	
	public char charAt(int index) {
		return sb.charAt(index);
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
}
