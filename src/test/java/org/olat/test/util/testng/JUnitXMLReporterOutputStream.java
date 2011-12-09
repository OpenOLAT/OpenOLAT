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
*/
package org.olat.test.util.testng;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class JUnitXMLReporterOutputStream extends OutputStream {

	private final JUnitXMLReporter reporter_;
	
	final int type_; // 1 == stdout, 2 == stderr

	public JUnitXMLReporterOutputStream(JUnitXMLReporter reporter, int type) {
		reporter_ = reporter;
		type_ = type;
        if(type != 1 && type != 2)
            throw new IllegalArgumentException("index has to be 1 or 2");
	}

	@Override
	public void write(int b) throws IOException {
		try{
	        Class clazz=JUnitXMLReporter.local.get();
	        if(clazz != null) {
	            Tuple<ByteArrayOutputStream,ByteArrayOutputStream> tuple=reporter_.outputs.get(clazz);
	            if(tuple != null) {
	                ByteArrayOutputStream sb=type_ == 1? tuple.getVal1() : tuple.getVal2();
	                sb.write(b);
	                return;
	            }
	        }
        	if (type_==1) {
        		reporter_.unsolicitedOut.write(b);
        	} else {
        		reporter_.unsolicitedErr.write(b);
        	}
		} finally {
			if (type_==1) {
				reporter_.old_stdout.write(b);
			} else {
				reporter_.old_stderr.write(b);
			}
		}
    }

}
