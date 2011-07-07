/**
 * 
 */
package cu.repsystestbed.simulation.messages;

import cu.repsystestbed.data.Feedback;
import cu.repsystestbed.simulation.Agent;

/**
 * @author partheinstein
 *
 */
public class ResponseFeedbackMsg extends Message
{

	private Feedback feedback;
	/**
	 * @param sender
	 * @param receiver
	 * @throws Exception 
	 */
	public ResponseFeedbackMsg(Agent sender, Agent receiver, Agent assesee, Double value) throws Exception
	{
		super(sender, receiver);
		//asessor is always the sender
		setFeedback(new Feedback(sender, assesee, value));
		
	}
	private void setFeedback(Feedback feedback)
	{
		this.feedback = feedback;
	}
	/**
	 * 
	 * @return Feedback
	 */
	public Feedback getFeedback()
	{
		return feedback;
	}

}
