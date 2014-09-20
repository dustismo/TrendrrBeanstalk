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
public class BeanstalkException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5427161060543990905L;
	protected Log log = LogFactory.getLog(BeanstalkException.class);
	
	public BeanstalkException () {
		this(null, null);
	}
	
	public BeanstalkException(String message) {
		this(message, null);
	}
	
	public BeanstalkException(String message, Exception cause) {
		super(message, cause);
	}
	
	public BeanstalkException(Exception cause) {
		this(null, cause);
	}
}
