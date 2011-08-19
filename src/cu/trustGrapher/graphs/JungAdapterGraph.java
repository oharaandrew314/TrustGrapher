package cu.trustGrapher.graphs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.graph.SimpleDirectedGraph;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;


public class JungAdapterGraph<V,E> implements edu.uci.ics.jung.graph.DirectedGraph<V,E>
{
	
	/**
	 * a placeholder for the jgraph graph that is held within this adapter
	 */
	protected SimpleDirectedGraph<V, E> g;
	
	public SimpleDirectedGraph<V,E> getInnerGraph(){
		return g;
	}

	public JungAdapterGraph(SimpleDirectedGraph<V, E> g)
	{
		this.g=g;
		
	}

	@Override
	public boolean addEdge(E arg0, V arg1, V arg2)
	{
		
		return g.addEdge(arg1, arg2, arg0);
	}

	@Override
	public boolean addEdge(E arg0, V arg1, V arg2, EdgeType arg3)
	{
		if (arg3.equals(EdgeType.UNDIRECTED))
				throw new IllegalArgumentException();
		
		return addEdge(arg0, arg1, arg2);
	}

	@Override
	public V getDest(E arg0)
	{
		
		return g.getEdgeTarget(arg0);
	}

	@Override
	public Pair<V> getEndpoints(E arg0)
	{
		
		return new Pair(g.getEdgeSource(arg0),g.getEdgeTarget(arg0));
	}

	@Override
	public Collection<E> getInEdges(V arg0)
	{
		
		return g.incomingEdgesOf(arg0);
	}

	@Override
	public V getOpposite(V arg0, E arg1)
	{
		V first = g.getEdgeSource(arg1);
		if (first.equals(arg0)){
			return g.getEdgeTarget(arg1);
		} else
		return first;
	}

	@Override
	public Collection<E> getOutEdges(V arg0)
	{
		
		return g.outgoingEdgesOf(arg0);
	}

	@Override
	public int getPredecessorCount(V arg0)
	{
		
		
		return getPredecessors(arg0).size();
	}

	@Override
	public Collection<V> getPredecessors(V arg0)
	{
		Set<V> nodes= new HashSet<V>();
		for(E edge:	g.incomingEdgesOf(arg0)){
			nodes.add(g.getEdgeSource(edge));
		}
		return nodes;
	}

	@Override
	public V getSource(E arg0)
	{
		
		return g.getEdgeSource(arg0);
	}

	@Override
	public int getSuccessorCount(V arg0)
	{
		
		return getSuccessors(arg0).size();
	}

	@Override
	public Collection<V> getSuccessors(V arg0)
	{

		Set<V> nodes= new HashSet<V>();
		for(E edge:	g.outgoingEdgesOf(arg0)){
			nodes.add(g.getEdgeTarget(edge));
		}
		return nodes;
	}

	@Override
	public int inDegree(V arg0)
	{
		
		return g.inDegreeOf(arg0);
	}

	@Override
	public boolean isDest(V arg0, E arg1)
	{
		
		return g.getEdgeTarget(arg1).equals(arg0);
	}

	@Override
	public boolean isPredecessor(V arg0, V arg1)
	{
		
		return (g.getEdge(arg0, arg1) != null);
	}

	@Override
	public boolean isSource(V arg0, E arg1)
	{
		
		return (g.getEdgeSource(arg1).equals(arg0));
	}

	@Override
	public boolean isSuccessor(V arg0, V arg1)
	{
		
		return (g.getEdge(arg1,arg0)!=null);
	}

	@Override
	public int outDegree(V arg0)
	{

		return g.outDegreeOf(arg0);
	}

	@Override
	public boolean addEdge(E arg0, Collection<? extends V> arg1)
	{

		if (arg1.size()!=2)
			throw new IllegalArgumentException();
		
		Iterator<? extends V> it = arg1.iterator();
		V v1 = it.next();
		V v2 = it.next();
		return g.addEdge(v1, v2, arg0);
	}

	@Override
	public boolean addEdge(E arg0, Collection<? extends V> arg1, EdgeType arg2)
	{
		if (!arg2.equals(EdgeType.DIRECTED))
			throw new IllegalArgumentException();
		return addEdge(arg0,arg1);
	}

	@Override
	public boolean addVertex(V arg0)
	{

		return g.addVertex(arg0);
	}

	@Override
	public boolean containsEdge(E arg0)
	{

		return g.containsEdge(arg0);
	}

	@Override
	public boolean containsVertex(V arg0)
	{
		
		return g.containsVertex(arg0);
	}

	@Override
	public int degree(V arg0)
	{

		return g.degreeOf(arg0);
	}

	@Override
	public E findEdge(V arg0, V arg1)
	{

		return g.getEdge(arg0, arg1);
	}

	@Override
	public Collection<E> findEdgeSet(V arg0, V arg1)
	{
		
		return g.getAllEdges(arg0, arg1);
	}

	@Override
	public int getEdgeCount()
	{
		
		return g.edgeSet().size();
	}

	@Override
	public int getEdgeCount(EdgeType arg0)
	{

		if (!arg0.equals(EdgeType.DIRECTED))
			throw new IllegalArgumentException();
		return g.edgeSet().size();
	}

	@Override
	public EdgeType getEdgeType(E arg0)
	{
		
		return EdgeType.DIRECTED;
	}

	@Override
	public Collection<E> getEdges()
	{
		
		return g.edgeSet();
	}

	@Override
	public Collection<E> getEdges(EdgeType arg0)
	{
		if (!arg0.equals(EdgeType.DIRECTED))
			throw new IllegalArgumentException();
		return g.edgeSet();
	}

	@Override
	public int getIncidentCount(E arg0)
	{
		if (g.getEdgeSource(arg0).equals(g.getEdgeTarget(arg0)))
			return 1; // loop
		else
			return 2;
	}

	@Override
	public Collection<E> getIncidentEdges(V arg0)
	{
		
		return g.edgesOf(arg0);
	}

	@Override
	public Collection<V> getIncidentVertices(E arg0)
	{

		return new Pair<V>(g.getEdgeSource(arg0), g.getEdgeTarget(arg0));
	}

	@Override
	public int getNeighborCount(V arg0)
	{
		return getNeighbors(arg0).size();
	}

	@Override
	public Collection<V> getNeighbors(V arg0)
	{
		Set<V> nodes= new HashSet<V>();
		for(E edge:	g.outgoingEdgesOf(arg0)){
			nodes.add(g.getEdgeTarget(edge));
		}
		for(E edge:	g.incomingEdgesOf(arg0)){
			nodes.add(g.getEdgeTarget(edge));
		}
		return nodes;
	}

	@Override
	public int getVertexCount()
	{

		return g.vertexSet().size();
	}

	@Override
	public Collection<V> getVertices()
	{
		// TODO Auto-generated method stub
		return g.vertexSet();
	}

	@Override
	public boolean isIncident(V arg0, E arg1)
	{
		
		return getIncidentEdges(arg0).contains(arg1);
	}

	@Override
	public boolean isNeighbor(V arg0, V arg1)
	{
		
		return (isPredecessor(arg0, arg1)||isSuccessor(arg0, arg1));
	}

	@Override
	public boolean removeEdge(E arg0)
	{
		
		return g.removeEdge(arg0);
	}

	@Override
	public boolean removeVertex(V arg0)
	{

		return g.removeVertex(arg0);
	}

    public EdgeType getDefaultEdgeType() {
        return EdgeType.DIRECTED;
    }

}
