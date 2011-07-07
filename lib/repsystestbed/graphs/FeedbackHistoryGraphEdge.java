package cu.repsystestbed.graphs;

import java.util.ArrayList;

import cu.repsystestbed.data.Feedback;
import cu.repsystestbed.entities.Agent;

public class FeedbackHistoryGraphEdge extends TestbedEdge
{

	private static final long serialVersionUID = 1590992129571899946L;
	public ArrayList<Feedback> feedbacks; 
	
	public FeedbackHistoryGraphEdge(Agent src, Agent sink) throws Exception
	{
		if(src.equals(sink)) throw new Exception("src == sink.");
		super.src = src;
		super.sink = sink;
		feedbacks = new ArrayList<Feedback>();
		
	}
	
	public void addFeedback(Feedback feedback) throws Exception
	{
		if(feedback.getAssesor()!=super.src) throw new Exception("Adding a feedback to the wrong edge. e.source!=edge.src");
		this.feedbacks.add(feedback);
	}
	
	public ArrayList getFeedbacks()
	{
		return this.feedbacks;
	}
	
	public boolean equals(Object o)
	{
		FeedbackHistoryGraphEdge otherEdge = (FeedbackHistoryGraphEdge) o;
		if(this.sink.equals(otherEdge.sink) && this.src.equals(otherEdge.src)) return true;
		else return false;
	}
	

	

}