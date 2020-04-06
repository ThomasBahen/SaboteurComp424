package student_player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;



public class TreeNode implements Comparable<TreeNode> {
	
	private static final double EXPLORATION_PARAMTER = Math.sqrt(2);
	
	private MyBoardState boardState;
	private SaboteurMove startingMove;
	private TreeNode parent;
	private List<TreeNode> children;
	private double winScore;
	private int simulationCount;
	
	//Create New Node for UCTS
	public TreeNode(MyBoardState boardState, SaboteurMove startingMove) {
        super();
        
        this.boardState = boardState;
        this.startingMove = startingMove;
        children = new ArrayList<TreeNode>();
        winScore = 0;
        simulationCount = 0;
        
        boardState.processMove(startingMove);
	}
	
	// make comparable so that we can compare which child to choose at end of search
	@Override
	public int compareTo(TreeNode other) {
		return (int) (winScore - other.getWinScore());
	}
	
	
	public boolean isRoot() {
		return parent == null;
	}
	
	public TreeNode getParent() {
		return parent;
	}

	public TreeNode setParent(TreeNode parentNode) {
		return parent = parentNode;
	}
	
	public List<TreeNode> getChildren() {
		return children;
	}
	
	public MyBoardState getBoardState() {
		return boardState;
	}
	
	public double getWinScore() {
		return winScore;
	}
	
	public int getSimulationCount() {
		return simulationCount;
	}
	
	public SaboteurMove getStartingMove() {
		return startingMove;
	}
	
	public void addChild(TreeNode childNode) {
		children.add(childNode);
		childNode.setParent(this);
	}
	
	
	public void addResult(double winScore) {
		this.winScore += winScore;
		simulationCount++;
	}
	
	public double getUCTScore() {
		double n_wins  = this.getWinScore();
		int n_simulations = this.getSimulationCount();
		
		
		if (n_simulations == 0 || this.isRoot()) {
			return Integer.MAX_VALUE;
		}
		
		int parent_simulations = this.getParent().getSimulationCount();
		
		return n_wins / n_simulations + EXPLORATION_PARAMTER * Math.sqrt(Math.log(parent_simulations)/n_simulations);
	}
	
	
	public TreeNode getBestChild() {
		double maxScore = Integer.MIN_VALUE;
		List<TreeNode> children = this.getChildren();
		List<TreeNode> unexplored = new ArrayList<TreeNode>();
		TreeNode bestChild = children.get(0);
		
		for (TreeNode child : children) {
			double score = child.getUCTScore();
			
			// take note of all unexplored nodes
			if (score == Integer.MAX_VALUE) {
				unexplored.add(child);
			} else if (score > maxScore) {
				maxScore = score;
				bestChild = child;
			}
		}
		
		//  select first unexplored node if any exist
		if (unexplored.size() > 0) {
			return unexplored.get(0);
		}
		
		return bestChild;
	}

	
	public TreeNode getWorstChild() {
		double minScore = Integer.MAX_VALUE;
		List<TreeNode> children = this.getChildren();
		TreeNode worstChild = children.get(0);
		
		for (TreeNode child : children) {
			double score = child.getUCTScore();
			
			if (score < minScore) {
				minScore = score;
				worstChild = child;
			}
		}
		
		return worstChild;
	}
	
	public int getDepth() {
		int depth = 0;
		
		TreeNode currentNode = this;
		while (!currentNode.isRoot()) {
			depth++;
			currentNode = currentNode.getParent();
		}
		return depth;
	}

}