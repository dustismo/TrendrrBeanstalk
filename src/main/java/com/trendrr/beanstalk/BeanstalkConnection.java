/**
 * 
 */
package com.trendrr.beanstalk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Wraps the beanstalk connection.  
 * 
 * 
 * @author dustin
 *
 */
public class BeanstalkConnection {

	protected Log log = LogFactory.getLog(BeanstalkConnection.class);
	
	private SocketChannel channel;
	private ByteArrayOutputStream outbuf = new ByteArrayOutputStream();
	
	public void connect(String addr, int port) throws BeanstalkException {
		try {
			this.channel = SocketChannel.open();
			this.channel.connect(new InetSocketAddress(addr, port));
			this.channel.finishConnect();
		} catch (Exception x) {
			throw new BeanstalkException(x);
		}
	}
	
	public void close() {	
		try {
			outbuf.close();
		} catch (Exception x) {
			log.debug("Caught", x);
		}
		
		try {
			channel.close();
		} catch (Exception x) {
			log.debug("Caught", x);
		}
	}

	public boolean isOpen() {
		return channel != null && channel.isOpen();
	}
	
	public void write(String str) throws BeanstalkDisconnectedException, BeanstalkException{
		try {
			ByteBuffer buf = ByteBuffer.wrap(str.getBytes());
			while(buf.hasRemaining()) {
				channel.write(buf);
			}
		} catch (Exception x) {
			this.throwException(x);
		}
	}
	
	private void throwException(Exception x) throws BeanstalkDisconnectedException, BeanstalkException{
		if (x instanceof NotYetConnectedException) {
			throw new BeanstalkDisconnectedException(x);
		}
		if (x instanceof IOException) {
			throw new BeanstalkDisconnectedException(x);
		}

		
		throw new BeanstalkException(x);
	}
	
	public void write(byte[] bytes) throws BeanstalkDisconnectedException, BeanstalkException{
		try {
			ByteBuffer buf = ByteBuffer.wrap(bytes);
			while(buf.hasRemaining()) {
				channel.write(buf);
			}
		} catch (Exception x) {
			this.throwException(x);
		}
	}
	
	/**
	 * returns the control response.  ends with \r\n
	 * @return
	 */
	public String readControlResponse() throws BeanstalkDisconnectedException, BeanstalkException{
		//clear the old out buffer
		String response = null;
		int count = 0;
		while (response == null) {
			count++;
			
			outbuf = new ByteArrayOutputStream();
			ByteBuffer buf = ByteBuffer.allocate(4096);
			if (count > 10000) {
				throw new BeanstalkException("OH Snap, nothing to read from the buffer for 100 seconds!");
			}
			
			try {
				if (channel.read(buf) == 0) {
					log.warn("Nothing in the buffer, sleeping for 100 millis, will try again");
					try {
						Thread.sleep(100);
					} catch (Exception x) {
						log.error("CuaghT", x);
					}
					continue;
				}
			} catch (Exception x) {
				this.throwException(x);
			}
			
			byte[] bytes = buf.array();
					
			ByteArrayOutputStream stringBuf = new ByteArrayOutputStream();
			byte lastByte = ' ';
	
			for (int i=0 ; i < buf.position(); i++) {
				byte curByte = bytes[i];
	
				if (lastByte == '\r' && curByte == '\n' && response == null) {
					response =  new String(stringBuf.toByteArray()).trim();
					if (response.isEmpty()) {
						log.warn("Errant line end found, possibly from the previous request. skipping");
						response = null;
					}
					continue;
				}
				
				if (response == null) {
					stringBuf.write(curByte);
				} else {
					outbuf.write(curByte);
				}
				lastByte = curByte;
			}
	//		log.info("OUTBUF: " + outbuf.size() + " :" + new String(outbuf.toByteArray()));
		}
		return response;
	}
	
	public byte[] readBytes(int numBytes) throws BeanstalkDisconnectedException, BeanstalkException{
		
		byte[] bytes = new byte[numBytes];

		byte[] array = this.outbuf.toByteArray();
		this.outbuf = new ByteArrayOutputStream();
		
		int bytesWritten = 0;
		//first read any bytes that are already in the outbuffer.
		for (int i=0; i < array.length; i++) {
			if (bytesWritten < bytes.length) {
				bytes[i] = array[i];
				bytesWritten++;
			} else {
				this.outbuf.write(array[i]);
			}
			
		}
//		log.info("GOT 468: " + bytesWritten + " " + bytes.length);
		if (bytesWritten >= bytes.length) {
			return bytes;
		}
		
		int numRead = 1;
		while(numRead >0) {
			//then read the bytes waiting in the channel.
			ByteBuffer buf = ByteBuffer.allocate(4096);
			try {
				numRead = channel.read(buf);
			} catch (Exception x) {
				this.throwException(x);
			}
			
			byte[] read = buf.array();
			for (int i=0 ; i<numRead; i++) {
				if (bytesWritten < bytes.length) {
					bytes[bytesWritten] = read[i];
				} else {
					this.outbuf.write(read[i]);
				}
				bytesWritten++;
			}
			if (bytesWritten >= bytes.length) {
				log.debug("468 GOT : " + bytesWritten + " " + bytes.length);
				return bytes;
			}
		}
//		log.info("GOT : " + bytesWritten + " " + bytes.length);
		return bytes;
	}
	
	@Override
	public void finalize() {
		this.close();//just for safety.  
	}
}
