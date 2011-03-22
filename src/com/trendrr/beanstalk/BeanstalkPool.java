/**
 * 
 */
package com.trendrr.beanstalk;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author dustin
 *
 */
public class BeanstalkPool {

	protected Log log = LogFactory.getLog(BeanstalkPool.class);
	
	Set<BeanstalkClient> clients = new HashSet<BeanstalkClient>();
	int maxClients = 30;
	
	long maxUseTime = 20*60*1000; //max checkout time is 20 minutes.
	
	long maxIdleTime = 20*60*1000; //connection will be removed after no use.
	
	private String addr;
	private int port;
	private String tube = null;
	
	/**
	 * setup a new pool.  
	 * set poolSize to 0 for unlimited
	 * 
	 * @param addr
	 * @param port
	 * @param poolSize
	 * @param tube All operations for the client will work on the tube.
	 */
	public BeanstalkPool(String addr, int port, int poolSize, String tube) {
		this.addr = addr;
		this.port = port;
		this.maxClients = poolSize;
		this.tube = tube;
	}

	public BeanstalkPool(String addr, int port, int poolSize) {
		this(addr, port, poolSize, null);
	}
	
	/**
	 * returns the number of active clients in the pool
	 * @return
	 */
	public int getPoolSize() {
		return this.clients.size();
	}
	
	/**
	 * synchronized, but should be fast as the client initializtion code happens lazily. 
	 * 
	 * throws throttled exception if to many connections are currently in use.
	 * 
	 * 
	 * TODO: could do this in a more concurrent way, but i think its fine.
	 * 
	 * @return
	 */
	public synchronized BeanstalkClient getClient() throws BeanstalkException{
		Set<BeanstalkClient> toRemove = new HashSet<BeanstalkClient>();
		
		Date max = new Date(new Date().getTime() - this.maxUseTime);
		Date maxIdle = new Date(new Date().getTime() - this.maxIdleTime);
		
		BeanstalkClient returnClient = null;
		try {
			
			/*
			 * Here we iterate over all the clients and reap any that need reaping. 
			 * TODO: we could restrict this to only loop over once every minute or so.
			 * for now I don't see it being a huge problem.
			 */
			for (BeanstalkClient client : clients) {
				
				
				if (client.inUseSince != null && client.inUseSince.before(max)) {
					client.reap = true;
				}	
				if (client.lastUsed != null && client.lastUsed.before(maxIdle)) {
					client.reap = true;
				}
				if (! client.con.isOpen()) {
					client.reap = true;
				}
				
				
				if (client.reap) {
					toRemove.add(client);
				} else if (returnClient == null && client.inUseSince == null) {
					//found the useable client.
					client.inUseSince = new Date();
					client.lastUsed = new Date(); //reap old connections
					returnClient = client;
				}
			}
		} finally {
			for (BeanstalkClient c : toRemove) {
				log.info("REAPING Client: " + c);
				c.pool = null;
				this.clients.remove(c);
				c.close();
			}
		}
		if (returnClient != null)
			return returnClient;
		
		//add a new client if they are all closed.
		if (this.maxClients > 0 && this.clients.size() >= this.maxClients) {
			log.error("Too many clients in use!");	
			throw new BeanstalkException("To many clients in use");
		}
		BeanstalkClient client = new BeanstalkClient(this.addr, this.port, this.tube, this);
	
		this.clients.add(client);
		client.inUseSince = new Date();
		return client;
	}
	
	public synchronized void done(BeanstalkClient client) {
		client.inUseSince = null;
	}
}
