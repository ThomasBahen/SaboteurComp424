package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Saboteur.SaboteurMove;

public class MonteCarloTree {
	
	//give some time to finish calculation (2000 is 2 seconds)
	private static final int TIME_LIMIT = 1800;
	private static final int MAX_TURNS_ROLLOUT = 50;
	private Random rand;
	
	
	public SaboteurMove chooseMove(MyBoardState boardState) {
		
		long startTime = System.currentTimeMillis();
		
		TreeNode root = new TreeNode(boardState, null);
		
		while(System.currentTimeMillis() - startTime < TIME_LIMIT) {
			
			//Tree Policy
			TreeNode leaf = treePolicy(root);
			//Roll out Policy 
			double result = rollOutPolicy(leaf);
			//Back propagate 
			backPropagate(leaf, result);
			//Add node to the Tree
			
		}
		List<TreeNode> children = root.getChildren();
        TreeNode bestNode = Collections.max(children);
        
		return bestNode.getStartingMove();
	}
	
	private TreeNode treePolicy(TreeNode start) {
		List<TreeNode> children = start.getChildren();
		if(children.size() == 0) {
			return start;	
		}else {
			return treePolicy(start.getBestChild());
		}
	}
	
	private double rollOutPolicy(TreeNode node) {
		
		//Rank the cards
		
		//Get all possible moves for the highest ranked card
		
		//Currently just trying random moves
		MyBoardState boardState = new MyBoardState(node.getBoardState());
		ArrayList<SaboteurMove> moves = boardState.getAllLegalMoves();
		SaboteurMove randomMove =  moves.get(rand.nextInt(moves.size()));
		
		//repeatedly try moves until winner or turn limit
		while(boardState.getWinner)
		
		
		
		return 0.0;
	}
	
	private void backPropagate(TreeNode node, double result){
		node.addResult(result);
		
		if(!node.isRoot()){
			backPropagate(node.getParent(),result);
		}
	}
	
}

