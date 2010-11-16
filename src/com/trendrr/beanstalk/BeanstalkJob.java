/**
 * 
 */
package com.trendrr.beanstalk;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author dustin
 *
 */
public class BeanstalkJob {

	protected Log log = LogFactory.getLog(BeanstalkJob.class);
	
	private byte[] data;
	private long id;
	BeanstalkClient client = null;
	
	public BeanstalkClient getClient() {
		return this.client;
	}
	public void setClient(BeanstalkClient client) {
		this.client = client;
	}
	public byte[] getData() {
		return this.data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public long getId() {
		return this.id;
	}
	public void setId(long id) {
		this.id = id;
	}

}
