package com.example.elasticsearch.exception;

/**
 * The Class MessageProcessingException.
 */
public class MessageProcessingException  extends Exception{
	
	/**
	 *MessageProcessingException.java of MessageProcessingException.java 
	 */
	private static final long serialVersionUID = -5996393571895084587L;

	/** Exeption message. */
	private final String message;
	
	
	/**
	 * Instantiates a new message processing exception.
	 *
	 * @param message the message
	 */
	public MessageProcessingException(final String message) {
		super(message);
		this.message = message;
	}


	
	/**
	 * To Stirng print the error message.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "MessageProcessingException [message=" + message + "]";
	}
	

}

