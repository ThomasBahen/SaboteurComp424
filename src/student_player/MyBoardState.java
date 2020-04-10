package student_player;


import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.*;
import boardgame.Board;
import boardgame.BoardState;
import com.sun.jndi.toolkit.dir.HierMemDirCtx;

import java.awt.event.HierarchyBoundsAdapter;
import java.lang.reflect.Array;
import java.util.*;

public class MyBoardState implements Cloneable{

    //CONSTANTS
    private final Integer BOARD_SIZE = 14;
    private final int[][] HIDDEN_POS = new int[][] {{12,3},{12,5},{12,7}};
    private final int[] ORIGIN = new int[]{SaboteurBoardState.originPos, SaboteurBoardState.originPos};

    //boards
    private int[][] intBoardState;
    public SaboteurTile[][] board;

    //Cards in the game
    public ArrayList<SaboteurCard> hand = new ArrayList<>();
    public ArrayList<SaboteurCard> opponentHand = new ArrayList<>();
    public ArrayList<SaboteurCard> deck = new ArrayList<>();

    //number of malus
    public int myMalus;
    public int theirMalus;

    //what our player number is
    public int playerNumber;

    //which players turn it is
    public int turnPlayer;

    //coordinate if the goal has been found in {x,y}
    public int[] goal = new int[]{-1};
    public Boolean[] revealed;
    public Boolean[] pathToHidden;

    public int winner;
    public int turnNumber;

    //constructor
    public MyBoardState(SaboteurBoardState state){
        //get information about the game state
        intBoardState = state.getHiddenIntBoard().clone();
        board = state.getHiddenBoard();
        hand = state.getCurrentPlayerCards();
        playerNumber = state.getTurnPlayer();
        myMalus = state.getNbMalus(playerNumber);
        theirMalus = state.getNbMalus(playerNumber ^1);
        turnNumber = state.getTurnNumber();
        turnPlayer = playerNumber;

        //randomly place the nugget under a hidden tile if the location is unknown
        findHidden();
        revealConnecting();
        randomizeNugget();

        HashMap<String, Integer> deckLeft = (HashMap<String, Integer>) SaboteurCard.getDeckcomposition();
        //remove cards from deck that are in your hand
        for (SaboteurCard card: hand) {
            removeFromDeck(card, deckLeft);
        }
        //remove cards from deck that are already on the board
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] != null) {
                    String index = board[i][j].getIdx();
                    int[] position = new int[]{i,j};
                    if (comparePosition(position, HIDDEN_POS[0]) || comparePosition(position, HIDDEN_POS[1]) || comparePosition(position, HIDDEN_POS[2]) ||
                        comparePosition(position, ORIGIN)){
                    } else {
                        index = index.split("_")[0];
                        deckLeft.put(index, deckLeft.get(index) - 1);
                    }
                }
            }
        }
        //gets a random opponent hand from the cards remaining
        this.deck = dictToList(deckLeft);
        opponentHand = getOppontsHand();

        winner = Board.NOBODY;
    }

    public Boolean comparePosition(int[] pos1, int[] pos2){
        return pos1[0] == pos2[0] && pos1[1] == pos2[1];
    }


    public Object clone() throws CloneNotSupportedException{
        MyBoardState clone = (MyBoardState) super.clone();
        clone.hand = (ArrayList<SaboteurCard>) this.hand.clone();
        clone.opponentHand = (ArrayList<SaboteurCard>) this.opponentHand.clone();
        clone.deck = (ArrayList<SaboteurCard>) this.deck.clone();
        clone.intBoardState = new int[this.intBoardState.length][];
        for(int i = 0; i < this.intBoardState.length; i++){
            clone.intBoardState[i] = this.intBoardState[i].clone();
        }
        clone.board = new SaboteurTile[this.board.length][];
        for(int i = 0; i < this.board.length; i++){
            clone.board[i] = this.board[i].clone();
        }
        for(int i = 0; i < 3; i++){
            if(!revealed[i]){
                clone.board[HIDDEN_POS[i][0]][HIDDEN_POS[i][1]] = new SaboteurTile("8");
            }
        }
        clone.findHidden();
        clone.randomizeNugget();
        return clone;
    }

    public SaboteurTile[][] getBoard(){
        return board;
    }

    //remove a card from the deck
    public void removeFromDeck(SaboteurCard card, HashMap<String, Integer> deckLeft){
        String cardName = card.getName();
        String[] splitted = cardName.split(":");
        String index = "";
        if(splitted.length == 2){
            String[] temp = splitted[1].split("_");
            index = temp[0];
        }else{
            index = splitted[0];
        }
        index = index.toLowerCase();
        deckLeft.put(index, deckLeft.get(index) - 1);
    }

    public void findHidden(){
        this.revealed = new Boolean[]{false, false, false};
        this.pathToHidden = new Boolean[]{false, false, false};
        for(int i = 0; i < 3; i++){
            if(!board[12][3+2*i].getIdx().equals("8")){
                revealed[i] = true;
                if(board[12][3+2*i].getIdx().equals("nugget")){
                    goal = new int[]{12, 3+2*i};
                }
            }
        }
    }

//    public boolean hasNeighbours(int[] pos){
//        if(this.board[pos[0]][pos[1]-1] != null){
//            return true;
//        }
//        if(this.board[pos[0]][pos[1]+1] != null){
//            return true;
//        }
//        if(this.board[pos[0]+1][pos[1]] != null){
//            return true;
//        }
//        if(this.board[pos[0]-1][pos[1]] != null){
//            return true;
//        }
//        return false;
//    }

    public void randomizeNugget(){
        if(goal[0] == -1){
            int unrevealed = 0;
            for(Boolean reveal: revealed){
                if(!reveal){
                    unrevealed++;
                }
            }
            if(unrevealed > 0) {
                Random random = new Random();
                int index = random.nextInt(unrevealed);
                int counter = 0;
                for (int i = 0; i < 3; i++) {
                    if (!revealed[i]) {
                        board[HIDDEN_POS[i][0]][HIDDEN_POS[i][1]] = new SaboteurTile("hidden1");
                        if(counter == index){
                            goal = new int[]{12, 3+2*i};
                            counter++;
                        }else{
                            counter++;
                        }
                    }
                }
            }
        }
    }

    //gets a random possibility of the opponent's hand from teh cards remaining
    public ArrayList<SaboteurCard> getOppontsHand(){
        Collections.shuffle(deck);
        ArrayList<SaboteurCard> newHand = new ArrayList<>();
        if(deck.size() < 8){
            newHand = (ArrayList<SaboteurCard>) deck.clone();
            deck.clear();
        }else{
            Random random = new Random();
            for(int i = 0; i < 7; i++){
                int index = random.nextInt(deck.size());
                newHand.add(deck.get(index));
                deck.remove(index);
            }
        }
        return newHand;
    }

    //creates the dictionary of remaining cards left into a list
    public ArrayList<SaboteurCard> dictToList(HashMap<String, Integer> deckLeft){
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
                            break;
                        default:
                            deck.add(new SaboteurTile(s));
                            break;
                    }
                }
            }
        }
        return deck;
    }

    public Boolean gameOver(){
        return this.deck.size()==0 && this.hand.size()==0 || winner != Board.NOBODY;
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
                        }else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())){
                            this.hand.remove(card);
                            break;
                        }
                    }
                }
            }else{
                //if it was the opponent, remove from the deck
                for(SaboteurCard card: this.opponentHand){
                    if(card instanceof SaboteurTile) {
                        if (((SaboteurTile) card).getIdx().equals(((SaboteurTile) testCard).getIdx())) {
                            this.opponentHand.remove(card);
                            break;
                        }else if(((SaboteurTile) card).getFlipped().getIdx().equals(((SaboteurTile) testCard).getIdx())){
                            this.opponentHand.remove(card);
                            break;
                        }
                    }
                }
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
                for(SaboteurCard card: this.opponentHand){
                    if(card instanceof  SaboteurBonus){
                        this.opponentHand.remove(card);
                        break;
                    }
                }
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
                for(SaboteurCard card: this.opponentHand){
                    if(card instanceof SaboteurMalus){
                        this.opponentHand.remove(card);
                        break;
                    }
                }
            }
        }
        else if(testCard instanceof SaboteurMap){
            if(turnPlayer == playerNumber){
                for(SaboteurCard card : this.hand){
                    this.hand.remove(card);
                    break;
                }
            }else{
                for(SaboteurCard card : opponentHand){
                    this.opponentHand.remove(card);
                    break;
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
                for(SaboteurCard card: this.opponentHand){
                    if(card instanceof  SaboteurDestroy){
                        this.opponentHand.remove(card);
                        this.board[i][j] = null;
                        break;
                    }
                }
            }

        }
        else if(testCard instanceof SaboteurDrop){
            if(turnPlayer == playerNumber){
                this.hand.remove(pos[0]);
            }else{
                opponentHand.remove(pos[0]);
            }
        }
        //revealConnecting();
        draw();
        turnNumber++;
        updateWinner();
        turnPlayer = 1 ^ turnPlayer;
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

    //taken from SaboeurBoardState
    public boolean verifyLegit(int[][] path,int[] pos){
        // Given a tile's path, and a position to put this path, verify that it respects the rule of positionning;
        if (!(0 <= pos[0] && pos[0] < BOARD_SIZE && 0 <= pos[1] && pos[1] < BOARD_SIZE)) {
            return false;
        }
        if(board[pos[0]][pos[1]] != null) return false;

        ArrayList<SaboteurTile> objHiddenList=new ArrayList<>();
        for(int i=0;i<3;i++) {
            if (!pathToHidden[i]){
                objHiddenList.add(this.board[HIDDEN_POS[i][0]][HIDDEN_POS[i][1]]);
            }
        }

        //the following integer are used to make sure that at least one path exists between the possible new tile to be added and existing tiles.
        // There are 2 cases:  a tile can't be placed near an hidden objective and a tile can't be connected only by a wall to another tile.
        int requiredEmptyAround=4;
        int numberOfEmptyAround=0;

        //verify left side:
        if(pos[1]>0) {
            SaboteurTile neighborCard = this.board[pos[0]][pos[1] - 1];
            if (neighborCard == null) numberOfEmptyAround += 1;
            else if (objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
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
            else if (objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
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
            else if (objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
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
            else if (objHiddenList.contains(neighborCard)) requiredEmptyAround -= 1;
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
        ArrayList<SaboteurCard> curHand;
        if(playerNumber == turnPlayer) {
            isBlocked = myMalus > 0;
            curHand = this.hand;
        }else{
            isBlocked = theirMalus > 0;
            curHand = opponentHand;
        }

        ArrayList<SaboteurMove> legalMoves = new ArrayList<>();
        System.out.println(curHand.size());
        for(SaboteurCard card : curHand){

            if( card instanceof SaboteurTile && !isBlocked) {
                ArrayList<int[]> allowedPositions = possiblePositions((SaboteurTile)card);
                for(int[] pos:allowedPositions){
                    legalMoves.add(new SaboteurMove(card,pos[0],pos[1], turnPlayer));
                }
                //if the card can be flipped, we also had legal moves where the card is flipped;
                if(SaboteurTile.canBeFlipped(((SaboteurTile)card).getIdx())){
                    SaboteurTile flippedCard = ((SaboteurTile)card).getFlipped();
                    ArrayList<int[]> allowedPositionsflipped = possiblePositions(flippedCard);
                    for(int[] pos:allowedPositionsflipped){
                        legalMoves.add(new SaboteurMove(flippedCard,pos[0],pos[1], turnPlayer));
                    }
                }
            }
            else if(card instanceof SaboteurBonus){
                if(myMalus > 0) legalMoves.add(new SaboteurMove(card,0,0, turnPlayer));
            }
            else if(card instanceof SaboteurMalus){
                legalMoves.add(new SaboteurMove(card,0,0, turnPlayer));
            }
            else if(card instanceof SaboteurMap){
//                for(int i =0;i<3;i++){ //for each hidden card that has not be revealed, we can still take a look at it.
//                    if(! this.revealed[i]) legalMoves.add(new SaboteurMove(card,HIDDEN_POS[i][0],HIDDEN_POS[i][1], turnPlayer));
//                }
            }
            else if(card instanceof SaboteurDestroy){
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) { //we can't destroy an empty tile, the starting, or final tiles.
                        if(this.board[i][j] != null && (i!=5 || j!= 5) && (i != HIDDEN_POS[0][0] || j!=HIDDEN_POS[0][1] )
                                && (i != HIDDEN_POS[1][0] || j!=HIDDEN_POS[1][1] ) && (i != HIDDEN_POS[2][0] || j!=HIDDEN_POS[2][1] ) ){
                            legalMoves.add(new SaboteurMove(card,i,j, turnPlayer));
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

    //taken from SaboteurBoardState
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

    //taken from SaboteurboardState
    private void draw(){
        if(this.deck.size() >0){
            if(turnPlayer==playerNumber){
                this.hand.add(this.deck.remove(0));
            }
            else{
                this.opponentHand.add(this.deck.remove(0));
            }
        }
    }

    public SaboteurMove chooseBest(ArrayList<SaboteurMove> moves){
        SaboteurMove best = null;
        int bestDistance = Integer.MAX_VALUE;
        for(SaboteurMove move: moves){
            if(myMalus > 0){
                if(move.getCardPlayed() instanceof SaboteurBonus){
                    best = move;
                    break;
                }
            }
            if(move.getCardPlayed() instanceof SaboteurMalus){
               best = move;
               break;
            }
            String[] name = move.getCardPlayed().getName().split(":");
            if(name[0].equals("Tile")){
                int [] position = move.getPosPlayed();
                int [][] tile = ((SaboteurTile) move.getCardPlayed()).getPath();
                int[][] edges = new int[][]{{0,1},{1,0},{1,2},{2,1}};
                if(tile[1][1] == 0){
                    continue;
                }

                int pathCounter = 0;
                for(int i = 0; i < 3; i++){
                    if(tile[edges[i][0]][edges[i][1]] == 1){
                        pathCounter++;
                    }
                }
                if(pathCounter == 1){
                    continue;
                }
                for(int i = 0; i < 4; i++){
                    if(tile[edges[i][0]][edges[i][1]] == 0){
                        continue;
                    }
                    int[] borderPos = new int[]{position[0]*3+edges[i][1], position[1]*3+edges[i][0]};
                    int[] goalPos = new int[]{goal[0]*3 +1, goal[1]*3+1};
                    int distance = Math.abs(borderPos[0] - goalPos[0]) + Math.abs(borderPos[1] - goalPos[1]);
                    if(distance < bestDistance) {
                        best = move;
                        bestDistance = distance;
                    }
//                    }else{
//                        for(int j = 0; j < 3; j++){
//                            if(!revealed[j]){
//                                int[] goalPos = new int[]{HIDDEN_POS[j][0]*3+1, HIDDEN_POS[j][1]*3+1};
//                                int distance = Math.abs(borderPos[0] - goalPos[0]) + Math.abs(borderPos[1] - goalPos[1]);
//                                if(distance < bestDistance){
//                                    best = move;
//                                    bestDistance = distance;
//                                }
//                            }
//                        }

                }
            }
        }
        if(best == null){
            return moves.get(0);
        }
        return best;
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
            if(this.pathToHidden[i]){
                continue;
            }
            if(cardPath(HIDDEN_POS[i], ORIGIN, true)){
                pathToHidden[i] = true;
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


    public int updateWinner() {
       // revealConnecting();
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
        bs.playerNumber = 1;
        for (SaboteurCard card: bs.hand){
            //System.out.print(card.getName() + ", ");
        }
        //System.out.println("");

        ArrayList<SaboteurMove> moves = bs.getAllLegalMoves();
        SaboteurMove bestMove = bs.chooseBest(moves);
        //System.out.println(bestMove.getCardPlayed().getName() + ": " + Arrays.toString(bs.chooseBest(moves).getPosPlayed()));


        bs.processMove(new SaboteurMove(new SaboteurTile("0"), 6,5,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("0"), 7,5,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("0"), 8,5,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("8"), 9,5,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("8"), 9,6,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("8"), 9,7,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("8"), 10,7,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("8"), 11,7,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("0"), 10,5,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("0"), 11,5,1));
        bs.processMove(new SaboteurMove(new SaboteurTile("10"), 12,4,1));
        //System.out.println(bs);
        //System.out.println(bs.updateWinner());
        //System.out.println(bs.winner);
    }
}
