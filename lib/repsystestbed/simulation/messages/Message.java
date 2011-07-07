package cu.repsystestbed.simulation.messages;

import org.apache.log4j.Logger;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.exceptions.SimulationException;
import cu.repsystestbed.simulation.Agent;

public class Message
{
	static int messageIdCounter = 0;
	static Logger logger = Logger.getLogger(Message.class.getName());
	
	private final int messageId;
	private Agent sender;
	private Agent receiver;
	
	public Message(Agent sender, Agent receiver) throws SimulationException
	{
		if(sender==null || receiver==null)
		{
			String msg = "Sender or receiver is null.";
			logger.error(msg);
			throw new SimulationException(msg);
		}
		if(sender.equals(receiver))
		{
			String msg = "Cannot send a message to self.";
			logger.error(msg);
			throw new SimulationException(msg);
		}
		
		messageId = this.messageIdCounter++;
		this.sender = sender;
		this.receiver = receiver;
	}
	
	public Message(Message otherMsg)
	{
		//no need for checks as otherMsg must have gone through checks
		this.messageId = otherMsg.messageId;
		this.sender = otherMsg.sender;
		this.receiver = otherMsg.receiver;
	}

	public int getMessageId()
	{
		return messageId;
	}

	public void setSender(Agent sender)
	{
		this.sender = sender;
	}

	public Agent getSender()
	{
		return sender;
	}

	public void setReceiver(Agent receiver)
	{
		this.receiver = receiver;
	}

	public Agent getReceiver()
	{
		return receiver;
	}
	

}
