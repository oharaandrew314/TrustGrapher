package cu.repsystestbed.simulation;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import cu.repsystestbed.exceptions.SimulationException;
import cu.repsystestbed.simulation.messages.Message;


public class Engine
{
	static Logger logger = Logger.getLogger(Engine.class.getName());
	
	private Hashtable<Integer, Agent> registeredAgents;
	private Hashtable<Integer, Message> messages;
	
	public Engine()
	{
		this.registeredAgents = new Hashtable<Integer, Agent>();
		this.messages = new Hashtable<Integer, Message>();
	}
	
	protected void registerAgent(Agent agent) throws SimulationException
	{
		if(agent==null)
		{
			String error = "Cannot register a null agent.";
			logger.error(error);
			throw new SimulationException(error);
		}
		if(this.registeredAgents.containsKey(agent.id))
		{
			String error = "Agent " + agent.id + " already added.";
			logger.error(error);
			throw new SimulationException(error);
		}
		this.registeredAgents.put(agent.id, agent);
	}
	
	protected void send(Message msg) throws SimulationException
	{
		if(msg==null)
		{
			String error = "Cannot send a null message.";
			logger.error(error);
			throw new SimulationException(error);
		}
		//add the message to message queue
		synchronized(this.messages)
		{
			this.messages.put(msg.getMessageId(), msg);
		}
		//ready to process the message
		notifyAll();
	}
	
	protected void processMessage(Message msg) throws SimulationException
	{
		Agent receiver = this.registeredAgents.get(msg.getReceiver().id);
		//always store a copy of the msg
		synchronized(this.messages)
		{
			receiver.processMessage(new Message(msg));	
			deleteMsg(msg);
		}

	}
	
	/**
	 * synchronized. must be called only by processMessage() method
	 * @param msg
	 */
	private void deleteMsg(Message msg)
	{
		//remove the copy
		this.messages.remove(msg.getMessageId());
	}
	
	public void startSimulation()
	{
		EngineThread engineThread = new EngineThread();
		engineThread.start();
	}
	
	private class EngineThread extends Thread
	{
		public void run()
		{
			if(messages.size()==0)
			{
				String error = "No messages.";
				logger.error(error);
				return;
			}
			if(registeredAgents.size()==0)
			{
				String error = "No agents. Register agents first.";
				logger.error(error);
				return;
			}
			
			for(;;)
			{
				if(messages.isEmpty())
				{
					try
					{
						logger.info("Waiting for messages");
						wait();
					} catch (InterruptedException e)
					{
						logger.error(e.getMessage());
						return;
					}
				}
				//wake up and process the message
				ArrayList<Message> tempMsgs =  (ArrayList<Message>) messages.values();
				for(Message msg : tempMsgs)
				{
					try
					{
						processMessage(msg);
					} catch (SimulationException e)
					{
						logger.error(e.getMessage());
						return;
					}
				}
			}	
			
		
			
		}

		
	}
	
		
	

}
