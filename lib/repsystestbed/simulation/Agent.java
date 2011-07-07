package cu.repsystestbed.simulation;

import org.apache.log4j.Logger;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.exceptions.SimulationException;
import cu.repsystestbed.simulation.messages.*;


public class Agent 
{
	static int agentCounter=0;
	static Logger logger = Logger.getLogger(Agent.class.getName());
	
	public final int id;
	private ReputationAlgorithm repAlg;
	private TrustAlgorithm trustAlg;
	private Engine simulationEngine;
	
	public Agent()
	{
		id=agentCounter;
		agentCounter++;
	}
	
	public Agent(int id)
	{
		this.id = id;
	}
	
	public String toString()
	{
		return "Agent " + id + " ";
	}
	
	@Override
	public boolean equals(Object o)
	{
		Agent otherAgent = (Agent) o;
		if(this.id==otherAgent.id) return true;
		else return false;
	}
	
	@Override
	public int hashCode()
	{
		return this.id;
	}

	public void setReputationAlg(ReputationAlgorithm repAlg)
	{
		this.repAlg = repAlg;
	}

	public ReputationAlgorithm getReputationAlg()
	{
		return repAlg;
	}

	public void setTrustAlg(TrustAlgorithm trustAlg)
	{
		this.trustAlg = trustAlg;
	}

	public TrustAlgorithm getTrustAlg()
	{
		return trustAlg;
	}
	
	public void processMessage(Message msg) throws SimulationException
	{
		if(msg instanceof RequestFeedbackMsg)
		{
			
		}else if(msg instanceof ResponseFeedbackMsg)
		{
			
		}else if(msg instanceof RequestReputationMsg)
		{
			
		}else if(msg instanceof ResponseReputationMsg)
		{
			
		}else
		{
			String error = "Unknown message received.";
			logger.error(error);
			throw new SimulationException(error);
		}
		 
	}
	
	public void register(Engine simulationEngine) throws SimulationException
	{
		if(simulationEngine==null)
		{
			String error = "Simulation engine is null.";
			logger.error(error);
			throw new SimulationException(error);
		}
		this.simulationEngine = simulationEngine;
		this.simulationEngine.registerAgent(this);
	}
}
