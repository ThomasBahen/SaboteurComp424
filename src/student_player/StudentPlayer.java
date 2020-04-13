package student_player;

import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurMap;
import boardgame.Move;

import Saboteur.*;

import java.util.ArrayList;

import Saboteur.SaboteurBoardState;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {
    int malus;
    int bonus;
    int map;
    int destroy;
    int turnPlayer;
    SaboteurBoardState prev;

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260675971");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(SaboteurBoardState boardState) {
        if(boardState.getTurnNumber() <= 2 ){
            malus = 0;
            bonus = 0;
            map = 0;
            destroy = 0;
            prev = null;
            turnPlayer = boardState.getTurnPlayer();
        }
        if(prev != null){
            if(prev.getNbMalus(turnPlayer) != boardState.getNbMalus(turnPlayer)){
                malus++;
            }else if(prev.getNbMalus(turnPlayer^1) != boardState.getNbMalus(turnPlayer^1)){
                bonus++;
            }
        }
    	MonteCarloTree mcts = new MonteCarloTree();
    	MyBoardState myBoard = new MyBoardState(boardState, malus, bonus, map, destroy);

        // Return your move to be processed by the server.
        SaboteurMove move = mcts.chooseMove(myBoard);
        if(move.getCardPlayed() instanceof SaboteurMalus){
            malus++;
        }else if(move.getCardPlayed() instanceof SaboteurBonus){
            bonus++;
        }else if(move.getCardPlayed() instanceof SaboteurMap){
            map++;
        }else if(move.getCardPlayed() instanceof SaboteurDestroy){
            destroy++;
        }
        prev = boardState;
        return move;
    }
}