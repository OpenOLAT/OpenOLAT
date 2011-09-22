package org.olat.ldap;

import java.io.Serializable;

public class LDAPError implements Serializable {
	
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
	
	
	public class ErrorNode implements Serializable{
		
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
