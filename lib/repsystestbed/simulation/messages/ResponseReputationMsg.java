/**
 * 
 */
package cu.repsystestbed.simulation.messages;

import cu.repsystestbed.data.Reputation;
import cu.repsystestbed.simulation.Agent;

/**
 * @author partheinstein
 *
 */
public class ResponseReputationMsg extends Message
{

	private Reputation reputation;
	public ResponseReputationMsg(Agent sender, Agent receiver, Agent trustee, Double value) throws Exception
	{
		super(sender, receiver);
		setReputation(new Reputation(sender, trustee, value));
	
	}
	private void setReputation(Reputation reputation)
	{
		this.reputation = reputation;
	}
	public Reputation getReputation()
	{
		return reputation;
	}

}
