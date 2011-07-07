/**
 * 
 */
package cu.repsystestbed.data;

import cu.repsystestbed.entities.Agent;

/**
 * @author partheinstein
 *
 */
public class Reputation
{
	private Agent trustor;
	private Agent trustee;
	
	public Double value;
	private static int transactionCounter=0;
	public int id;
	
	/**
	 * 
	 * @param trustor
	 * @param trustee
	 * @param value
	 * @throws Exception
	 */
	public Reputation(Agent trustor, Agent trustee, Double value) throws Exception
	{
		if(trustor.equals(trustee)) throw new Exception("source == sink.");
		this.trustor = trustor;
		this.trustee = trustee;
		this.value = value;
		id = transactionCounter++;
	}
	
	public Agent getAssesor()
	{
		return this.trustor;
	}
	
	public Agent getAssesee()
	{
		return this.trustee;
	}
	
	public String toString()
	{
		return "Reputation " + id + ": Trustor = " + this.trustor + " Trustee = " + this.trustee + " metric value = " + value;
	}
	
	
}
