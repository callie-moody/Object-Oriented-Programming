import java.util.*;

public class KevinBaconGraph {


    public KevinBaconGraph() {
    }

    // BFS to find shortest path tree for a current center of the universe. Return a path tree as a Graph.
    public static <V, E> Graph<V, E> bfs(Graph<V, E> g, V source) {
        //shortest path tree initialized
        Graph<V, E> tree = new AdjacencyMapGraph<V, E>();

        //queue to store vertices that need to be searched
        Queue<V> tosearch = new LinkedList<V>();
        tosearch.add(source);
        tree.insertVertex(source);

        //While there are still vertices to be searched in the queue
        while (!tosearch.isEmpty()) {
            //take the first vertex out of the queue(FIFO) and search its neighbors
            V current = tosearch.remove();
            Iterable<V> neighbors = g.inNeighbors(current);
            for (V n : neighbors) {
                //if the neighbors have not been visited yet
                if (!tree.hasVertex(n)) {
                    //add them to the queue
                    tosearch.add(n);
                    //insert them to the path tree graph
                    tree.insertVertex(n);
                    tree.insertDirected(n, current, g.getLabel(current, n));
                }
            }

        }

        return tree;
    }


    //b. Given a shortest path tree and a vertex, construct a path from the vertex back to the center of the universe.
    public static <V, E> List<V> getPath(Graph<V, E> tree, V v) {
        V vertex = v;
        //initialize structure to hold the path
        List<V> path = new ArrayList<>();
        path.add(vertex);
        //only the root should have an out degree of zero (all edges should point into it)
        while (tree.outDegree(vertex) > 0) {

            //out will only be size one, by nature of our tree
            Iterable<V> out = tree.outNeighbors(vertex);

            //add each vertex to the path and continue tracing backwards
            for (V vtx : out) {
                vertex = vtx;
                path.add(vertex);
            }
        }
        return path;
    }

    //c. Given a graph and a subgraph (here shortest path tree), determine which vertices are
    // in the graph but not the subgraph (here, not reached by BFS).
    public static <V, E> Set<V> missingVertices(Graph<V, E> graph, Graph<V, E> subgraph) {
        //Create two sets containing all vertices in each graph
        Set<V> graphv = new HashSet<V>();
        graphv.addAll((Set<V>) graph.vertices());
        Set<V> subgraphv = new HashSet<V>();
        subgraphv.addAll((Set<V>) subgraph.vertices());

        //subtract the vertices in the subgraph from the larger graph's set
        for (V vtx : subgraphv) {
            graphv.remove(vtx); }

        //remaining vertices are returned
        return graphv;
    }

    //d. Find the average distance-from-root in a shortest path tree, without enumerating all the paths.
    public static <V, E> double averageSeparation(Graph<V, E> tree, V root) {

        //call helper function, divid total distance by the number of actors
        return totaldist(tree,root,0)/(tree.numVertices()-1);
    }
    //helper function for average separation
    public static <V, E> double totaldist(Graph<V, E> tree, V root, double distsofar) {
        //current total is the distance to get to the current node
        double total=distsofar;

        //uses recursion to find the total distance after this node
        if (tree.inDegree(root) > 0) {

            //checks each neighbor down the tree
            for (V next : tree.inNeighbors(root)) {
                //increments distance so far by one because each neighbor is one step away from the current node
               total+=totaldist(tree,next,distsofar+1);
            }

        }

        return total;
    }
}




