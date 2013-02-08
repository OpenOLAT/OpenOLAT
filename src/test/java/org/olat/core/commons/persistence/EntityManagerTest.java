package org.olat.core.commons.persistence;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.Test;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryShortImpl;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * this is 
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EntityManagerTest extends OlatTestCase {
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private EntityManagerFactory emf;
	
	

	@Test
	public void test2() {
		
		EntityManager em1 = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		System.out.println("current1: " + em1);
		List<RepositoryEntryShortImpl> res = repositoryManager.loadRepositoryEntryShortsByResource(Collections.singletonList(27l), "CourseModule");
		
		EntityManager em2 = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		System.out.println("current2: " + em2);
		
		
		EntityTransaction trx = em2.getTransaction();
		System.out.println("Active: " + trx.isActive());
		
		trx.commit();
		
		EntityManagerFactoryUtils.closeEntityManager(em2);
		
		EntityManager em3 = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		System.out.println("current3: " + em3);
		
		
		List<TransactionSynchronization> syncs = TransactionSynchronizationManager.getSynchronizations();
		TransactionSynchronizationManager.clear();
		Map<Object,Object> map = TransactionSynchronizationManager.getResourceMap();
		EntityManager em4 = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
		System.out.println("current4: " + em4);
		
	}
	

	public void test3() {
		//lookupRepositoryEntry(Long key, boolean strict) 
		
		new TestThread(27l, repositoryManager).start();

		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class TestThread extends Thread {
		private Long reKey;
		private RepositoryManager repositoryManager;
		
		public TestThread(Long reKey, RepositoryManager repositoryManager) {
			this.reKey = reKey;
			this.repositoryManager = repositoryManager;
		}
		
		public void run() {
			
			EntityManager em1 = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
			System.out.println("em1: " + em1);
			RepositoryEntry re = repositoryManager.lookupRepositoryEntry(27l, false);
			EntityManager em2 = EntityManagerFactoryUtils.getTransactionalEntityManager(emf);
			System.out.println("em2: " + em2);
			
			System.out.println(re);
			
			
		}
		
	}

}
