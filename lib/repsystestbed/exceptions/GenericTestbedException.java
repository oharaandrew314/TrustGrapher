package cu.repsystestbed.exceptions;

public class GenericTestbedException extends Exception
{

	private static final long serialVersionUID = -5502694036733547528L;
	
	public GenericTestbedException(String msg, Throwable e)
	{
		super(msg, e);
	}

	public GenericTestbedException(String string)
	{
		super(string);
	}
	

}
