package cu.repsystestbed.graphs;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jgrapht.graph.SimpleDirectedGraph;

import cu.repsystestbed.algorithms.ReputationAlgorithm;
import cu.repsystestbed.data.Feedback;
import cu.repsystestbed.entities.Agent;


public class FeedbackHistoryGraph extends SimpleDirectedGraph<Agent, FeedbackHistoryGraphEdge>
{
	private ArrayList<ReputationAlgorithm> observers;
	private FeedbackHistoryGraph originalGraph;
	static Logger logger = Logger.getLogger(FeedbackHistoryGraph.class.getName());
	
	/**
	 * 
	 * @param FeedbackHistoryEdgeFactory ef
	 * 
	 * The edge factory is created like this FeedbackHistoryEdgeFactory ef = new FeedbackHistoryEdgeFactory();
	 */
	public FeedbackHistoryGraph(FeedbackHistoryEdgeFactory ef)
	{		
		super(ef);
		setOriginalGraph(this);
		observers = new ArrayList<ReputationAlgorithm>();
	}
	
	

	public void addObserver(ReputationAlgorithm algorithm)
	{
		//TODO check to ensure multiple copies for the algorithms are not added. Problematic?
		this.observers.add(algorithm);
		algorithm.setFeedbackHistoryGraph(this); //any data mod to this graph, trust algs have that to work with.
	}
	
	public void notifyObservers()
	{
		for(ReputationAlgorithm alg : observers)
		{
			alg.update();
		}
	}
	
	
	
	/**
	 * 
	 * @param feedback feedback to add to the graph
	 * @param updateObservers notifies the reputation algorithms
	 */
	public void addFeedback(Feedback feedback, boolean updateObservers) throws Exception
	{
		/*
		 * Add the source and destination nodes of the feedback and the edge to the 
		 * feedback history graph
		 */
		if(!this.containsVertex(feedback.getAssesor())) this.addVertex(feedback.getAssesor());
		if(!this.containsVertex(feedback.getAssesee())) this.addVertex(feedback.getAssesee());
		if(!this.containsEdge(feedback.getAssesor(), feedback.getAssesee())) this.addEdge(feedback.getAssesor(), feedback.getAssesee());
		//hopefully this method returns the ptr to the edge (and not a copy)
		FeedbackHistoryGraphEdge edge = this.getEdge(feedback.getAssesor(), feedback.getAssesee()); 
		edge.addFeedback(feedback);
		if(updateObservers) notifyObservers();
	}



	public void setOriginalGraph(FeedbackHistoryGraph originalGraph)
	{
		this.originalGraph = originalGraph;
	}



	public FeedbackHistoryGraph getOriginalGraph()
	{
		return originalGraph;
	}
	
	

	/**
	 * merges the attack graph to the original graph.
	 * @param attackGraph
	 * @param attackEdges
	 * @throws Exception
	 */
	/*
	public void attack(ExperienceGraph attackGraph, ArrayList attackEdges) throws Exception
	{
		if(attackEdges==null) throw new Exception("attackEdges is null.");
		if(attackEdges!=null && attackEdges.size()==0) throw new Exception("No attackEdges. Can't attack.");
		if(attackGraph==null) throw new Exception("attackGraph is null.");
		
		Set attackAgents = attackGraph.vertexSet();
		if(attackAgents.size()==0) throw new Exception("no agents in the attack graph.");
		
		//right now, I am not saving the original graph before attacking it. you should do so.
		
		//make sure the sink in the attack edge is in the original graph
		Iterator it0 = attackEdges.iterator();
		while(it0.hasNext())
		{
			ExperienceEdge e = (ExperienceEdge)it0.next();
			if(!this.containsVertex((Agent)e.sink)) throw new Exception("the sink in the attack edge not in the original graph. this is not a attack edge");
		}
		
		//merge the attack graph to the original graph
		Iterator it = attackAgents.iterator();
		while(it.hasNext())
		{
			Agent a = (Agent) it.next();
			if(!this.containsVertex(a))
			{
				logger.info("Adding a bad node. " + a);
				this.addVertex(a); //some nodes may already be in the original graph. These are nodes attacked by the attacker.
			}
		}
		
		Iterator it1 = attackGraph.edgeSet().iterator();
		while(it1.hasNext())
		{
			ExperienceEdge e = (ExperienceEdge) it1.next();
			this.addEdge((Agent)e.src, (Agent)e.sink); //add the edge from the attack graph
			ExperienceEdge e1 = this.getEdge((Agent)e.src, (Agent)e.sink); //get the edge you just added
			e1.experiences = e.experiences; //add the experience arraylist
		}
		
		this.notifyObservers();
		

	}*/

}
