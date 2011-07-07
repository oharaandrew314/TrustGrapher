/**
 * 
 */
package cu.repsystestbed.simulation.messages;

import cu.repsystestbed.exceptions.SimulationException;
import cu.repsystestbed.simulation.Agent;

/**
 * @author partheinstein
 *
 */
public class RequestReputationMsg extends Message
{

	private Agent trustee;
	
	public RequestReputationMsg(Agent sender, Agent receiver, Agent trustee) throws SimulationException
	{
		super(sender, receiver);
		setTrustee(trustee);
	
	}

	private void setTrustee(Agent trustee)
	{
		this.trustee = trustee;
	}

	public Agent getTrustee()
	{
		return trustee;
	}

}
