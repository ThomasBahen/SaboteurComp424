
package student_player;


import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MyBoardState implements Cloneable{
    public int[][] intBoardState;
    public SaboteurTile[][] board;
    public ArrayList<SaboteurCard> hand = new ArrayList<>();
    public HashMap<String, Integer> deckLeft = new HashMap<>();
    public int myMalus;
    public int theirMalus;
    public int playernumber;
    public boolean[] revealed;
    private int[][] hiddenPos = new int[][] {{12,3},{12,5},{12,7}};
    final Integer BOARD_SIZE = 14;

    public MyBoardState(SaboteurBoardState state){
        intBoardState = state.getHiddenIntBoard().clone();
        board = state.getHiddenBoard();
        hand = state.getCurrentPlayerCards();
        playernumber = state.getTurnPlayer();
        myMalus = state.getNbMalus(playernumber);
        theirMalus = state.getNbMalus(playernumber^1);

        revealed = new boolean[3];
        findHidden();

        deckLeft = (HashMap<String, Integer>) SaboteurCard.getDeckcomposition();
        for (SaboteurCard card: hand) {
            removeFromDeck(card);
        }
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[0].length; j++){
                if(board[i][j] != null){
                    String index = board[i][j].getIdx();
                    if(index.equals("entrance")||index.equals("nugget")||index.equals("hidden1")||index.equals("hidden2")){
                        continue;
                    }else{
                        index = index.split("_")[0];
                        deckLeft.put(index, deckLeft.get(index) - 1);
                    }
                }
            }
        }
    }

    public MyBoardState(MyBoardState state){
        this.intBoardState = state.getIntBoardState().clone();
        this.board = state.getBoard().clone();
    }

    public Object clone() {
        return new MyBoardState(this);
    }
    public int[][] getIntBoardState(){
        return intBoardState;
    }

    public SaboteurTile[][] getBoard(){
        return board;
    }

    public void removeFromDeck(SaboteurCard card){
        String cardName = card.getName();
        String[] splitted = cardName.split(":");
        String index = "";
        if(splitted.length == 2){
            index = splitted[1];
        }else{
            index = splitted[0];
        }
        deckLeft.put(index, deckLeft.get(index) - 1);
    }

    public void findHidden(){
        for(int i = 0; i < 3; i++){
            if(!board[12][3+2*i].getIdx().equals("8")){
                revealed[i] = true;
            }
        }
    }

    //gets a random possibility of the opponent's hand from teh cards remaining
    public ArrayList<SaboteurCard> getOppontsHand(){
        ArrayList<SaboteurCard> deck = dictToList();
        ArrayList<SaboteurCard> hand = new ArrayList<>();
        if(deck.size() < 8){
            return deck;
        }else{
            Random random = new Random();
            for(int i = 0; i < 8; i++){
                hand.add(deck.get(random.nextInt(deck.size())));
            }
        }
        return hand;
    }


    public ArrayList<SaboteurCard> dictToList(){
        ArrayList<SaboteurCard> deck = new ArrayList<>();
        for (String s: deckLeft.keySet()){
            if(deckLeft.get(s) != 0){
                for(int i = 0; i < deckLeft.get(s); i++){
                    switch (s){
                        case("map"):
                            deck.add(new SaboteurMap());
                            break;
                        case("malus"):
                            deck.add(new SaboteurMalus());
                            break;
                        case("bonus"):
                            deck.add(new SaboteurBonus());
                            break;
                        case("destroy"):
                            deck.add(new SaboteurDestroy());
                        default:
                            deck.add(new SaboteurTile(s));
                    }
                }
            }
        }
        return deck;
    }

    //updated from SaboteurBoardState
    public void processMove(SaboteurMove m) throws IllegalArgumentException {
        SaboteurCard testCard = m.getCardPlayed();
        int[] pos = m.getPosPlayed();
        if(testCard instanceof SaboteurTile){
            this.board[pos[0]][pos[1]] = new SaboteurTile(((SaboteurTile) testCard).getIdx());
            for(SaboteurCard card : this.hand) {
                if (card instanceof SaboteurTile) {
                    if (((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                        this.hand.remove(card);
                        break; //leave the loop....
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurBonus){
            myMalus --;
            for(SaboteurCard card : this.hand) {
                if (card instanceof SaboteurBonus) {
                    this.hand.remove(card);
                    break;
                }
            }
        }
        else if(testCard instanceof SaboteurMalus){
            theirMalus ++;
            for(SaboteurCard card : this.hand) {
                if (card instanceof SaboteurMalus) {
                    this.hand.remove(card);
                    break; //leave the loop....
                }
            }
        }
        else if(testCard instanceof SaboteurMap){
            for(SaboteurCard card : this.hand) {
                if (card instanceof SaboteurMap) {
                    //TODO: Figure out what to do when the the rollout uses SaboteurMap
                }
            }
        }
        else if (testCard instanceof SaboteurDestroy) {
            int i = pos[0];
            int j = pos[1];
            for(SaboteurCard card : this.hand) {
                if (card instanceof SaboteurDestroy) {
                    this.hand.remove(card);
                    this.board[i][j] = null;
                    break; //leave the loop....
                }
            }
        }
        else if(testCard instanceof SaboteurDrop){
            this.hand.remove(pos[0]);
        }
    }

    //Adapted from SaboteurBoardState
    public ArrayList<int[]> possiblePositions(SaboteurTile card) {
        // Given a card, returns all the possiblePositions at which the card could be positioned in an ArrayList of int[];
        // Note that the card will not be flipped in this test, a test for the flipped card should be made by giving to the function the flipped card.
        ArrayList<int[]> possiblePos = new ArrayList<int[]>();
        int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}}; //to make the test faster, we simply verify around all already placed tiles.
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (this.board[i][j] != null) {
                    for (int m = 0; m < 4; m++) {
                        if (0 <= i+moves[m][0] && i+moves[m][0] < BOARD_SIZE && 0 <= j+moves[m][1] && j+moves[m][1] < BOARD_SIZE) {
                            if (this.verifyLegit(card.getPath(), new int[]{i + moves[m][0], j + moves[m][1]} )){
                                possiblePos.add(new int[]{i + moves[m][0], j +moves[m][1]});
                            }
                        }
                    }
                }
            }
        }
        return possiblePos;
    }
    public boolean verifyLegit(int[][] path,int[] pos){
        // Given a tile's path, and a position to put this path, verify that it respects the rule of positionning;
        if (!(0 <= pos[0] && pos[0] < BOARD_SIZE && 0 <= pos[1] && pos[1] < BOARD_SIZE)) {
            return false;
        }
        if(board[pos[0]][pos[1]] != null) return false;

        //the following integer are used to make sure that at least one path exists between the possible new tile to be added and existing tiles.
        // There are 2 cases:  a tile can't be placed near an hidden objective and a tile can't be connected only by a wall to another tile.
        int requiredEmptyAround=4;
        int numberOfEmptyAround=0;

        ArrayList<SaboteurTile> objHiddenList=new ArrayList<>();
        for(int h = 0; h < 3; h++){
            if(!revealed[h]){
                objHiddenList.add(this.board[this.hiddenPos[h][0]][this.hiddenPos[h][1]]);
            }
        }


        //verify left side:
        if(pos[1]>0) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] - 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[0][0] != neighborPath[2][0] || path[0][1] != neighborPath[2][1] || path[0][2] != neighborPath[2][2] ) return false;
                else if(path[0][0] == 0 && path[0][1]== 0 && path[0][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify right side
        if(pos[1]<BOARD_SIZE-1) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] + 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                if (path[2][0] != neighborPath[0][0] || path[2][1] != neighborPath[0][1] || path[2][2] != neighborPath[0][2]) return false;
                else if(path[2][0] == 0 && path[2][1]== 0 && path[2][2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify upper side
        if(pos[0]>0) {
            SaboteurTile neighborCard = this.board[pos[0]-1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][2],path[1][2],path[2][2]};
                int[] np={neighborPath[0][0],neighborPath[1][0],neighborPath[2][0]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1;
            }
        }
        else numberOfEmptyAround+=1;

        //verify bottom side:
        if(pos[0]<BOARD_SIZE-1) {
            SaboteurTile neighborCard = this.board[pos[0]+1][pos[1]];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if(objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
            else {
                int[][] neighborPath = neighborCard.getPath();
                int[] p={path[0][0],path[1][0],path[2][0]};
                int[] np={neighborPath[0][2],neighborPath[1][2],neighborPath[2][2]};
                if (p[0] != np[0] || p[1] != np[1] || p[2] != np[2]) return false;
                else if(p[0] == 0 && p[1]== 0 && p[2] ==0 ) numberOfEmptyAround +=1; //we are touching by a wall
            }
        }
        else numberOfEmptyAround+=1;

        if(numberOfEmptyAround==requiredEmptyAround)  return false;

        return true;
    }

    public ArrayList<SaboteurMove> getAllLegalMoves() {
        // Given the current player hand, gives back all legal moves he can play.
        boolean isBlocked;
        isBlocked= myMalus > 0;

        ArrayList<SaboteurMove> legalMoves = new ArrayList<>();

        for(SaboteurCard card : hand){
            if( card instanceof SaboteurTile && !isBlocked) {
                ArrayList<int[]> allowedPositions = possiblePositions((SaboteurTile)card);
                for(int[] pos:allowedPositions){
                    legalMoves.add(new SaboteurMove(card,pos[0],pos[1],playernumber));
                }
                //if the card can be flipped, we also had legal moves where the card is flipped;
                if(SaboteurTile.canBeFlipped(((SaboteurTile)card).getIdx())){
                    SaboteurTile flippedCard = ((SaboteurTile)card).getFlipped();
                    ArrayList<int[]> allowedPositionsflipped = possiblePositions(flippedCard);
                    for(int[] pos:allowedPositionsflipped){
                        legalMoves.add(new SaboteurMove(flippedCard,pos[0],pos[1],playernumber));
                    }
                }
            }
            else if(card instanceof SaboteurBonus){
                if(myMalus > 0) legalMoves.add(new SaboteurMove(card,0,0,playernumber));
            }
            else if(card instanceof SaboteurMalus){
                legalMoves.add(new SaboteurMove(card,0,0,playernumber));
            }
            else if(card instanceof SaboteurMap){
                for(int i =0;i<3;i++){ //for each hidden card that has not be revealed, we can still take a look at it.
                    if(! this.revealed[i]) legalMoves.add(new SaboteurMove(card,hiddenPos[i][0],hiddenPos[i][1],playernumber));
                }
            }
            else if(card instanceof SaboteurDestroy){
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) { //we can't destroy an empty tile, the starting, or final tiles.
                        if(this.board[i][j] != null && (i!=5 || j!= 5) && (i != hiddenPos[0][0] || j!=hiddenPos[0][1] )
                                && (i != hiddenPos[1][0] || j!=hiddenPos[1][1] ) && (i != hiddenPos[2][0] || j!=hiddenPos[2][1] ) ){
                            legalMoves.add(new SaboteurMove(card,i,j,playernumber));
                        }
                    }
                }
            }
        }
        // we can also drop any of the card in our hand
        for(int i=0;i<hand.size();i++) {
            legalMoves.add(new SaboteurMove(new SaboteurDrop(), i, 0, playernumber));
        }
        return legalMoves;
    }
}
