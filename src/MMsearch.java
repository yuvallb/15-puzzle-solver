import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class MMsearch {

    /**
     * Solve 15-puzzle using bidirectional A* search
     * 
     * @param fwdInitial initial state
     * @param fwdGoal    goal state
     * @return Array of 2 Nodes where both forward and backward
     *         paths meet.  First node has back pointer towards
     *         initial state and second node has back pointer
     *         towards goal state.
     */
    public static Node[] MMSolve(State fwdInitial, State fwdGoal) {
        
        final int FWD = 0;        // Forward direction
        final int REV = 1;        // Backward direction
        int U = Integer.MAX_VALUE;
        
        int[] directions = {FWD, REV};
        
        // These lists have two elements, one for the forward
        // direction and one for the backward direction.
        
        // Min-heap for removing the node from the open set with the
        // smallest f-score.
        List<Queue<Node>> fOpenHeap = new ArrayList<>(2);
        List<Queue<Node>> gOpenHeap = new ArrayList<>(2);
        List<Queue<Node>> prOpenHeap = new ArrayList<>(2);
        
        // set comparators. by f is built in the Node object
		Comparator<Node> byG = (Node a, Node b) -> a.getDepth() - b.getDepth();
		Comparator<Node> byPriority = (Node a, Node b) -> a.getPriority() - b.getPriority();

        // Hash tables with States as keys and Nodes as data for
        // checking if a state is in the open or closed set.
        List<Map<State, Node>> openHash = new ArrayList<>(2);
        List<Map<State, Node>> closedHash = new ArrayList<>(2);
        
        // Initial and goal states
        State[] initial = new State[] {fwdInitial, fwdGoal};
        State[] goal = new State[] {fwdGoal, fwdInitial};
        
        // For both forward and backward directions
        for (int i : directions) {
            // Create empty heap and hash maps
        	fOpenHeap.add(new PriorityQueue<Node>());
        	gOpenHeap.add(new PriorityQueue<Node>(byG));
        	prOpenHeap.add(new PriorityQueue<Node>(byPriority));
            openHash.add(new HashMap<State, Node>());
            closedHash.add(new HashMap<State, Node>());
            
            // Add initial node to the open set
            Node n = new Node(initial[i], null, null, (short)(initial[i].h(goal[i])));
            openHash.get(i).put(initial[i], n);
            fOpenHeap.get(i).add(n);
            gOpenHeap.get(i).add(n);
            prOpenHeap.get(i).add(n);
        }
        
        // For first iteration we start from the forward direction
        int i = FWD; // Index into our lists for the current direction
        int j = REV; // Index into our lists for the opposite direction
        
        // While there are still elements in the open set
        while(!fOpenHeap.get(FWD).isEmpty() && !fOpenHeap.get(REV).isEmpty()) {
        	//
        	
            // Remove node with minimum f-score
            Node n = fOpenHeap.get(i).poll();
            State s = n.getState();
                
            // Move the node from the open to closed set
            openHash.get(i).remove(s);
            closedHash.get(i).put(s, n);
            
            // For each of the four possible operators
            for (State.Operator op : State.Operator.values()) {
                // Create a new state that is the result of the move
                State newState = s.move(op);
                
                // If the move is invalid or has already been tried,
                // go on to next move
                if (newState == null || closedHash.get(i).containsKey(newState))
                    continue;
                    
                // If the new state is not already in the open set
                if (!openHash.get(i).containsKey(newState)) {
                    // Create a new Node for this state
                    Node newNode = new Node(newState, n, op, (short)(newState.h(goal[i]) ));
                    
                    // Check for a match in the nodes of the opposite direction
                    Node matchedNode = null;
                    if (openHash.get(j).containsKey(newState))
                        matchedNode = openHash.get(j).get(newState);
                    if (closedHash.get(j).containsKey(newState))
                        matchedNode = closedHash.get(j).get(newState);
                    
                    // If there is a match, return the pair of nodes
                    if (matchedNode != null) {
                        int openNodeCount = openHash.get(i).size() + openHash.get(j).size() + 1;
                        int closedNodeCount = closedHash.get(i).size() + closedHash.get(j).size();
                        
                        System.out.print("Nodes Generated: " + (openNodeCount + closedNodeCount));
                        System.out.print(" (" + openNodeCount + " open/");
                        System.out.println(closedNodeCount + " closed)");
                        
                        if (i == FWD)
                            return new Node[]{newNode, matchedNode};
                        else
                            return new Node[]{matchedNode, newNode};
                    // Otherwise, add the new node to the open set
                    } else {
                        openHash.get(i).put(newState, newNode);
                        fOpenHeap.get(i).add(newNode);
                    }
                } else { // If the new state is already in the open set
                    // Retrieve the existing node
                    Node existingNode = openHash.get(i).get(newState);
                    
                    // If we have found a shorter path to this node
                    if (n.getDepth() + 1 < existingNode.getDepth()) {
                        
                        // Update node depth, back pointer, and operator
                        existingNode.setDepth((short)(n.getDepth() + 1));
                        existingNode.setBackPtr(n);
                        existingNode.setOp(op);
                        
                        // Remove and re-add node from heap.  This will
                        // cause the node to be placed in the proper
                        // minheap order.
                        fOpenHeap.get(i).remove(existingNode);
                        fOpenHeap.get(i).add(existingNode);
                    }
                }
            }
            
            // Swap directions
            i = 1-i;
            j = 1-j;
        }
        return null;    // No solution found
    }

}
