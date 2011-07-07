package cu.repsystestbed.simulation.messages;

import cu.repsystestbed.exceptions.SimulationException;
import cu.repsystestbed.simulation.Agent;

public class RequestFeedbackMsg extends Message
{

	private Agent assessee;
	public RequestFeedbackMsg(Agent sender, Agent receiver, Agent assessee) throws SimulationException
	{
		super(sender, receiver);
		if(assessee==null)
		{
			String msg = "Assessee is null.";
			logger.error(msg);
			throw new SimulationException(msg);
		}
		this.setAssessee(assessee);
	}
	public void setAssessee(Agent assessee)
	{
		this.assessee = assessee;
	}
	public Agent getAssessee()
	{
		return assessee;
	}
	
	

}
