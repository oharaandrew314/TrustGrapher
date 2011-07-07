package cu.repsystestbed.data;

import cu.repsystestbed.entities.Agent;


public class Feedback
{
	private Agent assesor;
	private Agent assesee;
	
	public Double value;
	private static int transactionCounter=0;
	public int id;
	
	/**
	 * 
	 * @param assesor
	 * @param assesee
	 * @param value should be in [0,1]
	 * @throws Exception
	 */
	public Feedback(Agent assesor, Agent assesee, Double value) throws Exception
	{
		if(assesor.equals(assesee)) throw new Exception("source == sink.");
		if(value>=0 && value<=1.0) throw new Exception("Feedback value must be in [0,1] range.");
		this.assesor = assesor;
		this.assesee = assesee;
		this.value = value;
		id = transactionCounter++;
	}
	
	public Agent getAssesor()
	{
		return this.assesor;
	}
	
	public Agent getAssesee()
	{
		return this.assesee;
	}
	
	public String toString()
	{
		return "Feedback " + id + ": Assessor = " + this.assesor + " Assesee = " + this.assesee + " metric value = " + value;
	}

}
