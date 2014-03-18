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
package org.olat.ldap;

import java.io.Serializable;

public class LDAPError implements Serializable {

	private static final long serialVersionUID = 3195738602137330173L;
	private ErrorNode head, end;
	private int size;
	
	public LDAPError(){
		head = new ErrorNode(null, null, end);
		end = new ErrorNode(null, head, null);
		size=0;
	}
	
	public boolean isEmpty(){
		return (size == 0) ? true : false;
	}
	
	public int size(){
		return this.size;
	}
	
	public void insert(String error){
		ErrorNode newError = new ErrorNode(error, end.getPrev(), end);
		end.getPrev().setNext(newError);
		end.setPrev(newError);
		size++;
	}
	
	public String get(){
		if(isEmpty()!=true){
			ErrorNode errorNode = end.getPrev();
			String error = errorNode.getError();
			errorNode.getPrev().setNext(end);
			end.setPrev(errorNode.getPrev());
			size--;
			return error;
		}
		else return null;
	}
	
	
	public static class ErrorNode implements Serializable{

		private static final long serialVersionUID = 233510588950547484L;
		private String error;
		private ErrorNode next, prev;
		
		public ErrorNode(){
			this(null, null, null);
		}
		
		public ErrorNode(String error){
			this(error, null, null);
		}
		
		public ErrorNode(String error, ErrorNode prev, ErrorNode next){
			this.error = error;
			this.next = next;
			this.prev = prev;
		}
		
		public void setError(String error) {
			this.error = error;
		}

		public void setNext(ErrorNode next) {
			this.next = next;
		}

		public void setPrev(ErrorNode prev) {
			this.prev = prev;
		}
		
		public ErrorNode getNext(){
			return this.next;
		}
		
		public ErrorNode getPrev(){
			return this.prev;
		}
		
		public String getError(){
			return this.error;
		}
	}

}
