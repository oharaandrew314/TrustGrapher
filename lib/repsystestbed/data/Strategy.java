package cu.repsystestbed.data;

/**
 * This class encapsulates the behaviour of an agent. That is given a strategy, an agent should create feedbacks. 
 * @author pchandra
 *
 */

public class Strategy 
{
	private int numberOfFeedbacks;
	private double feedbackMean, feedbackStdDev;
	//It is assumed that there will be 
	private int targetGroupID;
	//the input arff file contains group id and agent id. don't need them as the model is as follows:
	//agent(id,groupID)--->strategy--->feedbacks
	

	/**
	 * 
	 * @param numberOfFeedbacks
	 * @param feedbackMean
	 * @param feedbackStdDev
	 * @param targetGroupID
	 */
	public Strategy(int numberOfFeedbacks, double feedbackMean,
			double feedbackStdDev, int targetGroupID) 
	{
		this.numberOfFeedbacks = numberOfFeedbacks;
		this.feedbackMean = feedbackMean;
		this.feedbackStdDev = feedbackStdDev;
		this.targetGroupID = targetGroupID;
	}
	/**
	 * @param numberOfFeedbacks the numberOfFeedbacks to set
	 */
	public void setNumberOfFeedbacks(int numberOfFeedbacks) 
	{
		this.numberOfFeedbacks = numberOfFeedbacks;
	}
	/**
	 * @return the numberOfFeedbacks
	 */
	public int getNumberOfFeedbacks() 
	{
		return numberOfFeedbacks;
	}

	/**
	 * @param feedbackMean the feedbackMean to set
	 */
	public void setFeedbackMean(double feedbackMean) 
	{
		this.feedbackMean = feedbackMean;
	}
	/**
	 * @return the feedbackMean
	 */
	public double getFeedbackMean() 
	{
		return feedbackMean;
	}
	/**
	 * @param feedbackStdDev the feedbackStdDev to set
	 */
	public void setFeedbackStdDev(double feedbackStdDev) 
	{
		this.feedbackStdDev = feedbackStdDev;
	}
	/**
	 * @return the feedbackStdDev
	 */
	public double getFeedbackStdDev() 
	{
		return feedbackStdDev;
	}
	/**
	 * @param targetGroupID the targetGroupID to set
	 */
	public void setTargetGroupID(int targetGroupID) 
	{
		this.targetGroupID = targetGroupID;
	}
	/**
	 * @return the targetGroupID
	 */
	public int getTargetGroupID() 
	{
		return targetGroupID;
	}
	

}
