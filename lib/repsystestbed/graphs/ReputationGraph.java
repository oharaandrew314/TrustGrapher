package cu.repsystestbed.graphs;

import java.util.ArrayList;

import org.jgrapht.graph.SimpleDirectedGraph;

import cu.repsystestbed.algorithms.TrustAlgorithm;
import cu.repsystestbed.entities.Agent;

public class ReputationGraph extends SimpleDirectedGraph<Agent, ReputationEdge>
{

	private static final long serialVersionUID = 2768260651851459417L;
	private ArrayList<TrustAlgorithm> observers;
	
	public ReputationGraph(ReputationEdgeFactory reputationEdgeFactory)
	{
		super(reputationEdgeFactory);
		observers = new ArrayList<TrustAlgorithm>();
	}

	public void addObserver(TrustAlgorithm alg)
	{
		this.observers.add(alg);
		alg.setReputationGraph(this);
	}
	
	public void notifyObservers()
	{
		for(TrustAlgorithm alg : observers)
		{
			alg.update();
		}
		
	}

}
