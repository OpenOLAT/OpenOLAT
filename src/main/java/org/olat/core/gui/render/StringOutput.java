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
* <p>
*/ 

package org.olat.core.gui.render;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;

/**
 * @author Felix Jost
 */
public class StringOutput extends Writer {

	private final StringBuilder sb;

	/**
	 * @param len
	 */
	public StringOutput(int len) {
		sb = new StringBuilder(len);
	}

	/**
	 * 
	 */
	public StringOutput() {
		sb = new StringBuilder();
	}
	
	public char getLastChar() {
		return sb.length() > 0 ? sb.charAt(sb.length() - 1) : 0;
	}

	/**
	 * @param val
	 * @return Itself
	 */
	public StringOutput append(String val) {
		sb.append(val);
		return this;
	}
	
	public StringOutput append(String format, Object...args) {
		return appendFmt(format, args);
	}
	
	/**
	 * 
	 * @param val The value to append
	 * @param append If true append happens, if false not
	 * @return Itself
	 */
	public StringOutput append(String val, boolean append) {
		if(append) {
			sb.append(val);
		}
		return this;
	}
	
	public StringOutput append(String valTrue, String valFalse, boolean val) {
		if(val) {
			sb.append(valTrue);
		} else {
			sb.append(valFalse);
		}
		return this;
	}
	
	public StringOutput ifCond(boolean cond) {
		if (cond) return this;
		return new StringOutput();
	}
	
	public StringOutput appendFmt(String format, Object...params) {
		if(params.length == 0) {
			sb.append(format);
		} else {
			String v = String.format(format, params);
			sb.append(v);
		}
		return this;
	}
	
	/**
	 * @param val
	 * @return Itself
	 */
	public StringOutput append(boolean val) {
		sb.append(val);
		return this;
	}

	/**
	 * @param i
	 * @return Itself
	 */
	public StringOutput append(int i) {
		sb.append(i);
		return this;
	}
	
	public StringOutput append(double d) {
		sb.append(Double.toString(d));
		return this;
	}

	/**
	 * @param stringOutput
	 * @return Itself
	 */
	public StringOutput append(StringOutput stringOutput) {
		sb.append(stringOutput.sb);
		return this;
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		sb.append(cbuf, off, len);
	}

	/**
	 * @param sMin
	 * @return Itself
	 */
	public StringOutput append(long val) {
		sb.append(val);
		return this;
	}

	/**
	 * @param buffer
	 * @return Itself
	 */
	public StringOutput append(StringBuilder buffer) {
		sb.append(buffer);
		return this;
	}
	
	/**
	 * @param buffer
	 * @return Itself
	 */
	public StringOutput appendScanned(String str) {
		sb.append(new OWASPAntiSamyXSSFilter().filter(str));
		return this;
	}
	
	/**
	 * @param buffer
	 * @return Itself
	 */
	public StringOutput appendHtmlEscaped(String str) {
		StringHelper.escapeHtml(this, str);
		return this;
	}
	
	public StringOutput appendHtmlAttributeEscaped(String str) {
		sb.append(StringHelper.escapeForHtmlAttribute(str));
		return this;
	}
	
	public StringOutput insert(int offset, String str) {
		sb.insert(offset, str);
		return this;
	}
	
	/**
	 * Generate the following html code: onclick="call" onkeyup="if(event.which == 13 || event.keyCode){ call }"
	 * @param call The JavaScript method to envelop
	 * @return The generated html attributes
	 */
	public StringOutput onClickKeyEnter(String call) {
		FormJSHelper.onClickKeyEnter(sb, call);
		return this;
	}
	
	public void ensureCapacity(int minimumCapacity) {
		sb.ensureCapacity(minimumCapacity);
	}
	
	public int capacity() {
		return sb.capacity();
	}
	
	public void setLength(int newLength) {
		sb.setLength(newLength);
	}

	/**
	 * @return The length of the string output
	 */
	public int length() {
		return sb.length();
	}
	
	public boolean contains(String str) {
		return sb.indexOf(str) >= 0;
	}
	
	public int indexOf(String str) {
		return sb.indexOf(str);
	}
	
	public StringBuilder getBuffer() {
		return sb;
	}
	
	public Reader getReader() {
		return new StringOutputReader();
	}
	
	@Override
	public void flush() {
		//
	}

	@Override
	public void close() throws IOException {
		//
	}

	@Override
	public String toString() {
		return sb.toString();
	}
	
	private class StringOutputReader extends Reader {
		
		private int length;
		private int next = 0;
		private int mark = 0;
		
		/**
		 * Creates a new string reader.
		 *
		 * @param s  String providing the character stream.
		 */
		public StringOutputReader() {
			this.length = sb.length();
		}

		/**
		 * Reads a single character.
		 *
		 * @return     The character read, or -1 if the end of the stream has been
		 *             reached
		 *
		 * @exception  IOException  If an I/O error occurs
		 */
		@Override
		public int read() throws IOException {
			synchronized (lock) {
			    if (next >= length)
			    	return -1;
			    
			    char[] dst = new char[1];
			    sb.getChars(next++, next, dst, 0);
			    return dst[0];
			}
		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			synchronized (lock) {
		    if ((off < 0) || (off > cbuf.length) || (len < 0) ||
		        ((off + len) > cbuf.length) || ((off + len) < 0)) {
		    	throw new IndexOutOfBoundsException();
		    } else if (len == 0) {
		    	return 0;
		    }
			    if (next >= length) return -1;
			    
			    int n = Math.min(length - next, len);
			    sb.getChars(next, next + n, cbuf, off);
			    next += n;
			    return n;
			}
		}

		/**
		 * Skips the specified number of characters in the stream. Returns
		 * the number of characters that were skipped.
		 *
		 * <p>The <code>ns</code> parameter may be negative, even though the
		 * <code>skip</code> method of the {@link Reader} superclass throws
		 * an exception in this case. Negative values of <code>ns</code> cause the
		 * stream to skip backwards. Negative return values indicate a skip
		 * backwards. It is not possible to skip backwards past the beginning of
		 * the string.
		 *
		 * <p>If the entire string has been read or skipped, then this method has
		 * no effect and always returns 0.
		 *
		 * @exception  IOException  If an I/O error occurs
		 */
		@Override
		public long skip(long ns) throws IOException {
			synchronized (lock) {
				if (next >= length)
					return 0;
				// Bound skip by beginning and end of the source
				long n = Math.min(length - next, ns);
				n = Math.max(-next, n);
				next += n;
				return n;
			}
		}

		@Override
		public boolean ready() throws IOException {
			synchronized (lock) {
				return true;
			}
		}

		@Override
		public boolean markSupported() {
			return true;
		}

		@Override
		public void mark(int readAheadLimit) throws IOException {
			if (readAheadLimit < 0) {
				throw new IllegalArgumentException("Read-ahead limit < 0");
			}
			synchronized (lock) {
				mark = next;
			}
		}

		@Override
		public void reset() throws IOException {
			synchronized (lock) {
				next = mark;
			}
		}

    	@Override
	    public void close() throws IOException {
	    	//
	    }
	}
}