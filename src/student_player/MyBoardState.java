package student_player;


import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.*;
import boardgame.Board;
import boardgame.BoardState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class MyBoardState implements Cloneable{

    private final Integer BOARD_SIZE = 14;
    private final int[][] HIDDEN_POS = new int[][] {{12,3},{12,5},{12,7}};
    public final int[] ORIGIN = new int[]{SaboteurBoardState.originPos, SaboteurBoardState.originPos};

    //boards
    public int[][] intBoardState;
    public SaboteurTile[][] board;

    //Cards in the game
    public ArrayList<SaboteurCard> hand = new ArrayList<>();
    public ArrayList<SaboteurCard> opponentHand = new ArrayList<>();
    public HashMap<String, Integer> deckLeft = new HashMap<>();

    //number of malus
    public int myMalus;
    public int theirMalus;

    //what our player number is
    public int playerNumber;

    //which players turn it is
    public int turnPlayer;

    //hidden tiles
    public boolean[] revealed;

    //coordinate if the goal has been found in {x,y}
    public int[] goal = new int[]{-1};
    public SaboteurTile[] hidden = new SaboteurTile[3];

    public int winner;


    public MyBoardState(SaboteurBoardState state){
        //get information about the game state
        intBoardState = state.getHiddenIntBoard().clone();
        board = state.getHiddenBoard();
        hand = state.getCurrentPlayerCards();
        playerNumber = state.getTurnPlayer();
        myMalus = state.getNbMalus(playerNumber);
        theirMalus = state.getNbMalus(playerNumber ^1);

        //figure out which tiles are revealed
        revealed = new boolean[3];
        findHidden();

        //randomly place the nugget under a hidden tile if the location is unknown
        randomizeNugget();


        deckLeft = (HashMap<String, Integer>) SaboteurCard.getDeckcomposition();
        //remove cards from deck that are in your hand
        for (SaboteurCard card: hand) {
            removeFromDeck(card);
        }
        //remove cards from deck that are already on the board
        for (SaboteurTile[] saboteurTiles : board) {
            for (int j = 0; j < board[0].length; j++) {
                if (saboteurTiles[j] != null) {
                    String index = saboteurTiles[j].getIdx();
                    if (index.equals("entrance") || index.equals("nugget") || index.equals("hidden1") || index.equals("hidden2")) {
                    } else {
                        index = index.split("_")[0];
                        deckLeft.put(index, deckLeft.get(index) - 1);
                    }
                }
            }
        }

        //gets a random opponent hand from the cards remaining
        opponentHand = getOppontsHand();

        winner = 0;
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

    //remove a card from the deck
    public void removeFromDeck(SaboteurCard card){
        String cardName = card.getName();
        String[] splitted = cardName.split(":");
        String index = "";
        if(splitted.length == 2){
            index = splitted[1];
        }else{
            index = splitted[0];
        }
        index = index.toLowerCase();
        deckLeft.put(index, deckLeft.get(index) - 1);
    }

    public void randomizeNugget(){
        if(goal[0] == -1){
            int unrevealed = 0;
            for(Boolean reveal: revealed){
                if(!reveal){
                    unrevealed++;
                }
            }
            Random random = new Random();
            int index = random.nextInt(unrevealed);
            int counter = 0;
            for(int i = 0; i < 3; i++){
                if(!revealed[i]){
                    if(counter != index){
                        counter ++;
                        hidden[i] = new SaboteurTile("hidden1");
                    }else{
                        hidden[i] = new SaboteurTile("nugget");
                        counter++;
                    }
                }else{
                    hidden[i] = board[12][3+i*2];
                }
            }
        }
    }

    //figure out which tiles are still hidden
    public void findHidden(){
        for(int i = 0; i < 3; i++){
            if(!board[12][3+2*i].getIdx().equals("8")){
                revealed[i] = true;
                if(board[12][3+2*i].getIdx().equals("nugget")){
                    goal = new int[]{12, 3+2*i};
                }
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

    //creates the dictionary of remaining cards left into a list
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

    //changes whose turn it is
    public void changePlayer(){
        this.turnPlayer = this.turnPlayer ^ 1;
    }

    public Boolean gameOver(){
        return this.deckLeft.size()==0 && this.hand.size()==0 || winner != 0;
    }

    public String unflippedTile(SaboteurTile tile){
        String[] id = tile.getIdx().split("_");
        return id[0];
    }

    //updated from SaboteurBoardState
    public void processMove(SaboteurMove m) throws IllegalArgumentException {
        //get the card that was played for the move
        SaboteurCard testCard = m.getCardPlayed();
        int[] pos = m.getPosPlayed();

        //if the card was a tile
        if(testCard instanceof SaboteurTile){
            this.board[pos[0]][pos[1]] = new SaboteurTile(((SaboteurTile) testCard).getIdx());
            if(turnPlayer == playerNumber){
                //if it was the player, remove from the hand
                for(SaboteurCard card : this.hand) {
                    if (card instanceof SaboteurTile) {
                        if (((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.hand.remove(card);
                            break; //leave the loop....
                        }
                    }
                }
            }else{
                //if it was the opponent, remove from the deck
                String tileIndex = unflippedTile((SaboteurTile) testCard);
                deckLeft.put(tileIndex, deckLeft.get(tileIndex) - 1);
            }
        }
        else if(testCard instanceof SaboteurBonus){
            //if the card was a bonus
            if(turnPlayer == playerNumber){
                //decrement myMalus and remove from hand
                myMalus --;
                for(SaboteurCard card : this.hand) {
                    if (card instanceof SaboteurBonus) {
                        this.hand.remove(card);
                        break;
                    }
                }
            }else{
                //decrement their malus and remove from hand
                theirMalus --;
                deckLeft.put("bonus", deckLeft.get("bonus") - 1);
            }
        }
        else if(testCard instanceof SaboteurMalus){
            if(turnPlayer == playerNumber){
                theirMalus ++;
                for(SaboteurCard card : this.hand) {
                    if (card instanceof SaboteurMalus) {
                        this.hand.remove(card);
                        break; //leave the loop....
                    }
                }
            }else{
                myMalus ++;
                deckLeft.put("malus", deckLeft.get("malus") - 1);
            }

        }
        else if(testCard instanceof SaboteurMap){
            for(SaboteurCard card : this.hand) {
                if (card instanceof SaboteurMap) {
                    for (int i = 0; i < 3; i++) {
                        if(!revealed[i]){
                            this.board[pos[0]][pos[1]] = hidden[i];
                            break;
                        }
                    }
                }
            }
        }
        else if (testCard instanceof SaboteurDestroy) {
            int i = pos[0];
            int j = pos[1];
            if(turnPlayer == playerNumber){
                for(SaboteurCard card : this.hand) {
                    if (card instanceof SaboteurDestroy) {
                        this.hand.remove(card);
                        this.board[i][j] = null;
                        break; //leave the loop....
                    }
                }
            }else{
                deckLeft.put("destroy", deckLeft.get("destroy") - 1);
                this.board[i][j] = null;
            }

        }
        else if(testCard instanceof SaboteurDrop){
            this.hand.remove(pos[0]);
        }
        revealConnecting();
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
                objHiddenList.add(this.board[HIDDEN_POS[h][0]][this.HIDDEN_POS[h][1]]);
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

    //modified from SaboteurBoardState getAllLegalMoves
    public ArrayList<SaboteurMove> getAllLegalMoves() {
        // Given the current player hand, gives back all legal moves he can play.
        boolean isBlocked;
        isBlocked= myMalus > 0;

        ArrayList<SaboteurMove> legalMoves = new ArrayList<>();

        for(SaboteurCard card : hand){
            if( card instanceof SaboteurTile && !isBlocked) {
                ArrayList<int[]> allowedPositions = possiblePositions((SaboteurTile)card);
                for(int[] pos:allowedPositions){
                    legalMoves.add(new SaboteurMove(card,pos[0],pos[1], playerNumber));
                }
                //if the card can be flipped, we also had legal moves where the card is flipped;
                if(SaboteurTile.canBeFlipped(((SaboteurTile)card).getIdx())){
                    SaboteurTile flippedCard = ((SaboteurTile)card).getFlipped();
                    ArrayList<int[]> allowedPositionsflipped = possiblePositions(flippedCard);
                    for(int[] pos:allowedPositionsflipped){
                        legalMoves.add(new SaboteurMove(flippedCard,pos[0],pos[1], playerNumber));
                    }
                }
            }
            else if(card instanceof SaboteurBonus){
                if(myMalus > 0) legalMoves.add(new SaboteurMove(card,0,0, playerNumber));
            }
            else if(card instanceof SaboteurMalus){
                legalMoves.add(new SaboteurMove(card,0,0, playerNumber));
            }
            else if(card instanceof SaboteurMap){
                for(int i =0;i<3;i++){ //for each hidden card that has not be revealed, we can still take a look at it.
                    if(! this.revealed[i]) legalMoves.add(new SaboteurMove(card,HIDDEN_POS[i][0],HIDDEN_POS[i][1], playerNumber));
                }
            }
            else if(card instanceof SaboteurDestroy){
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) { //we can't destroy an empty tile, the starting, or final tiles.
                        if(this.board[i][j] != null && (i!=5 || j!= 5) && (i != HIDDEN_POS[0][0] || j!=HIDDEN_POS[0][1] )
                                && (i != HIDDEN_POS[1][0] || j!=HIDDEN_POS[1][1] ) && (i != HIDDEN_POS[2][0] || j!=HIDDEN_POS[2][1] ) ){
                            legalMoves.add(new SaboteurMove(card,i,j, playerNumber));
                        }
                    }
                }
            }
        }
        // we can also drop any of the card in our hand
        for(int i=0;i<hand.size();i++) {
            legalMoves.add(new SaboteurMove(new SaboteurDrop(), i, 0, playerNumber));
        }
        return legalMoves;
    }
    private void getIntBoard() {
        //update the int board.
        //Note that this tool is not available to the player.
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if(this.board[i][j] == null){
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                            this.intBoardState[i * 3 + k][j * 3 + h] = -1;
                        }
                    }
                }
                else {
                    int[][] path = this.board[i][j].getPath();
                    for (int k = 0; k < 3; k++) {
                        for (int h = 0; h < 3; h++) {
                            this.intBoardState[i * 3 + k][j * 3 + h] = path[h][2-k];
                        }
                    }
                }
            }
        }
    }

    //modified from SaboteurBoardState code
    //checks if there is a path to the nugget
    private boolean pathToGoal(){
        if(goal[0] == -1){
            return false;
        }

        //checks that there is a cardPath
        return cardPath(goal, ORIGIN, true);
    }

    private void revealConnecting(){
        for(int i = 0; i < 3; i++){
            if(revealed[i]){
                continue;
            }
            if(cardPath(HIDDEN_POS[i], ORIGIN, true)){
                revealed[i] = true;
                board[HIDDEN_POS[i][0]][HIDDEN_POS[i][1]] = hidden[i];
                if(hidden[i].getIdx().equals("nugget")){
                    goal = new int[]{HIDDEN_POS[i][0], HIDDEN_POS[i][1]};
                }
            }
        }
    }

    private Boolean cardPath(int[] originTargets,int[] targetPos,Boolean usingCard){
        // the search algorithm, usingCard indicate weither we search a path of cards (true) or a path of ones (aka tunnel)(false).
        ArrayList<int[]> queue = new ArrayList<>(); //will store the current neighboring tile. Composed of position (int[]).
        ArrayList<int[]> visited = new ArrayList<int[]>(); //will store the visited tile with an Hash table where the key is the position the board.
        visited.add(targetPos);
        if(usingCard) addUnvisitedNeighborToQueue(targetPos,queue,visited,BOARD_SIZE,usingCard);
        else addUnvisitedNeighborToQueue(targetPos,queue,visited,BOARD_SIZE*3,usingCard);
        while(queue.size()>0){
            int[] visitingPos = queue.remove(0);
            if(originTargets[0] == visitingPos[0] && originTargets[1] == visitingPos[1])
            {
                return  true;
            }
            visited.add(visitingPos);
            if(usingCard) addUnvisitedNeighborToQueue(visitingPos,queue,visited,BOARD_SIZE,usingCard);
            else addUnvisitedNeighborToQueue(visitingPos,queue,visited,BOARD_SIZE*3,usingCard);
        }
        return false;
    }

    private void addUnvisitedNeighborToQueue(int[] pos,ArrayList<int[]> queue, ArrayList<int[]> visited,int maxSize,boolean usingCard){
        int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}};
        int i = pos[0];
        int j = pos[1];
        for (int m = 0; m < 4; m++) {
            if (0 <= i+moves[m][0] && i+moves[m][0] < maxSize && 0 <= j+moves[m][1] && j+moves[m][1] < maxSize) { //if the hypothetical neighbor is still inside the board
                int[] neighborPos = new int[]{i+moves[m][0],j+moves[m][1]};
                if(!contains(visited, neighborPos)){
                    if(usingCard && this.board[neighborPos[0]][neighborPos[1]]!=null) queue.add(neighborPos);
                    else if(!usingCard && this.intBoardState[neighborPos[0]][neighborPos[1]]==1) queue.add(neighborPos);
                }
            }
        }
    }

    public boolean contains(ArrayList<int[]> visted, int[] query){
        for(int[] vist: visted){
            if(Arrays.equals(vist, query)){
                return true;
            }
        }
        return false;
    }


    private int updateWinner() {
        revealConnecting();
        boolean playerWin = pathToGoal();
        if (playerWin) { // Current player has won
            winner = turnPlayer;
        } else if (gameOver() && winner== Board.NOBODY) {
            winner = Board.DRAW;
        }
        return winner;
    }

    //modified from SaboteurBoardState toString
    public String toString() {
        getIntBoard();
        StringBuilder boardString = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE*3; i++) {
            for (int j = 0; j < BOARD_SIZE*3; j++) {
                if(this.intBoardState[i][j] != -1){
                    boardString.append(" ");
                }
                boardString.append(this.intBoardState[i][j]);

                boardString.append(",");
            }
            boardString.append("\n");
        }
        return boardString.toString();
    }



    public static void main(String[] args){
        MyBoardState bs = new MyBoardState(new SaboteurBoardState());
        bs.turnPlayer = 1;
        bs.processMove(new SaboteurMove(new SaboteurTile("0"), 6,5,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("0"), 7,5,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("0"), 8,5,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("0"), 9,5,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("0"), 10,5,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("0"), 11,5,1));
        System.out.println(bs);
        System.out.print(bs.updateWinner());
    }
}
