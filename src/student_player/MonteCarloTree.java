package student_player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurMap;
import boardgame.Board;
import boardgame.BoardState;

public class MonteCarloTree {
	
	//give some time to finish calculation (2000 is 2 seconds)
	private static final int TIME_LIMIT = 1800;
	private static final int MAX_TURNS_ROLLOUT = 100;
	private static final int GROWTH_FACTOR = 1;
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
       	SaboteurMove bestMove = bestNode.getStartingMove();
		for (SaboteurCard card :
				boardState.hand) {

			if(card instanceof SaboteurMap && bestNode.getBoardState().winner != boardState.playerNumber){
				for(int i = 0; i < 3; i++){
					if(!boardState.revealed[i]){
						bestMove = new SaboteurMove(card, 12, 3+2*i, boardState.playerNumber);
					}
				}
			}
		}
		return bestMove;
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
		if(moves.size() == 0 ){
			return 0;
		}
		for(TreeNode child: node.getChildren()){
			moves.remove(child.getStartingMove());
		}
		SaboteurMove bestMove =  boardState.chooseBest(moves);
		if(node.getDepth() < MAX_TREE_DEPTH ) {
			for(int i=0;i<GROWTH_FACTOR;i++) {
				MyBoardState newBoardstate = null;
			    try{
					newBoardstate = (MyBoardState) boardState.clone();
                }catch (CloneNotSupportedException e){
			    	e.printStackTrace();
				}
				TreeNode child = new TreeNode(newBoardstate, bestMove);
				node.addChild(child);
			}
		}
		//repeatedly try moves until winner or turn limit
		int turnCount = 0;
		MyBoardState rolloutBoardState = null;
		try {
			rolloutBoardState = (MyBoardState) boardState.clone();
			rolloutBoardState.processMove(bestMove);
			while (rolloutBoardState.winner == Board.NOBODY && turnCount < MAX_TURNS_ROLLOUT) {
				moves = rolloutBoardState.getAllLegalMoves();
				bestMove = rolloutBoardState.chooseBest(moves);
				rolloutBoardState.processMove(bestMove);
				turnCount++;
			}
			rolloutBoardState.updateWinner();
			if (rolloutBoardState.winner == player_id) {
				return 1;
			} else if (rolloutBoardState.winner == Board.NOBODY) {
				return 0;
			} else {
				return -1;
			}
		}catch (CloneNotSupportedException e){
			e.printStackTrace();
		}
		return 0;
	}
	
	private void backPropagate(TreeNode node, double result){
		node.addResult(result);
		
		if(!node.isRoot()){
			backPropagate(node.getParent(),result);
		}
	}
	
}

