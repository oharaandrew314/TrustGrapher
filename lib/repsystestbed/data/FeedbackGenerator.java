package cu.repsystestbed.data;

import java.util.List;

public interface FeedbackGenerator 
{
	/**
	 * Implement this method to generate feedbacks given some a strategy
	 * @param strategy
	 * @return a list of feedbacks for an agent
	 */
	
	public List<Feedback> generate(Strategy strategy);

}
