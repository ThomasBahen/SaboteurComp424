package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Saboteur.SaboteurMove;
import boardgame.Board;

public class MonteCarloTree {
	
	//give some time to finish calculation (2000 is 2 seconds)
	private static final int TIME_LIMIT = 1800;
	private static final int MAX_TURNS_ROLLOUT = 50;
	private static final int GROWTH_FACTOR = 3;
	private static final int MAX_TREE_DEPTH = 10;
	private Random rand;
	
	
	public SaboteurMove chooseMove(MyBoardState boardState) {
		
		long startTime = System.currentTimeMillis();
		
		TreeNode root = new TreeNode(boardState, null);
		
		while(System.currentTimeMillis() - startTime < TIME_LIMIT) {
			
			//Tree Policy
			TreeNode leaf = treePolicy(root);
			//Roll out Policy and grow tree 
			double result = rollOutPolicy(leaf);
			//Back propagate 
			backPropagate(leaf, result);
			
			
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
		
		int player_id = node.getBoardState().turnPlayer;
		//Rank the cards
		
		//Get all possible moves for the highest ranked card
		
		//Currently just trying random moves
		MyBoardState boardState =node.getBoardState();
		ArrayList<SaboteurMove> moves = boardState.getAllLegalMoves();
		
		SaboteurMove randomMove =  moves.get(rand.nextInt(moves.size()));
		if(node.getDepth() < MAX_TREE_DEPTH ) {
			int[] randList = new Random().ints(0, moves.size()).distinct().limit(GROWTH_FACTOR).toArray();
			for(int i=0;i<GROWTH_FACTOR;i++) {
				TreeNode child = new TreeNode(boardState, moves.get(randList[i]));
				node.addChild(child);
			}
		}
		//repeatedly try moves until winner or turn limit
		int turnCount = 0; 
		while(boardState.winner == Board.NOBODY && turnCount < MAX_TURNS_ROLLOUT) {
			boardState.processMove(randomMove);
			moves = boardState.getAllLegalMoves();
			randomMove =  moves.get(rand.nextInt(moves.size()));
		}
		
		if (boardState.winner == player_id) {
			return 1;
		} else if (boardState.winner == Board.NOBODY) {
			return 0;
		} else {
			return -1;
		}
	}
	
	private void backPropagate(TreeNode node, double result){
		node.addResult(result);
		
		if(!node.isRoot()){
			backPropagate(node.getParent(),result);
		}
	}
	
}

