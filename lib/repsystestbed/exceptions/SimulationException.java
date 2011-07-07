package cu.repsystestbed.exceptions;

public class SimulationException extends Exception
{

	private static final long serialVersionUID = 1448681985660507773L;
	
	public SimulationException(String msg, Throwable e)
	{
		super(msg, e);
	}

	public SimulationException(String msg)
	{
		super(msg);
	}
	

}
