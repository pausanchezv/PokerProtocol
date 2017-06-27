/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Random;
import static poker.ArtificialIntelligencePlayer.CALL_CODE;
import static poker.ArtificialIntelligencePlayer.FOLD_CODE;
import static poker.Hand.NUM_CARDS;
import poker.HandComparatorAlgorithm.ComparativeHandResult;
import poker_state_machine.PokerState;
import poker_state_machine.StartState;


/**
 *
 * @author aespinro11.alumnes
 */
public class Poker {
    
    
    // índex per identificar el jugador anfitrió o l'altre en certes llistes
    public static final int NON_HOST_INDEX = 0;
    public static final int HOST_INDEX = 1;
    
    // stakes per defecte en la primera partida
    public static final int DEFAULT_STAKES = 500;

    
    // estat actual del joc del Pòker
    private PokerState currentState;
    
    // quantitat de l'aposta inicial
    private final int ante;
    
    // saldo dels dos jugadors
    private final int [] stakes;
    
    // saldo que tenien els jugadors quan van iniciar la partida
    private int [] startStakes;
    
    // apostes pendents d'aplicar de cada jugador
    private int [] bets;
    
    // quantitat apostada pels jugadors sobre la taula
    private int pot;
    
    // ronda d'apostes actual
    private int round;
    
    // índex del jugador a qui toca apostar/passar/rendir-se
    private int turnIndex;
    
    // indica si l'anfitrió és el dealer
    private boolean isHostTheDealer;
    
    // barala de cartes de la partida
    private Deck deck;
    
    // mans del jugador anfitrió i de l'altre
    private final Hand [] hands;
    
    // indica si s'ha produït fold a la partida
    private boolean hasFold;
    
    // indica si la IA està o no activa
    private final boolean isArtificialIntelligenceActive;
    
    // Entitat IA
    ArtificialIntelligencePlayer AI;
    
    // indica si s'ha d'abandonar tota opció de seguir jugant
    private boolean isForcedToQuit;
    
    
    /**
     * Constructor
     * @param stakes
     * @param isArtificialIntelligenceActive
     */
    public Poker(int [] stakes, boolean isArtificialIntelligenceActive) {
        
        // temp
        this.stakes = stakes.length == 2 ? stakes : new int[] {DEFAULT_STAKES, DEFAULT_STAKES};
        startStakes = new int[2];
        bets = new int[] {0, 0};
        pot = 0;
        hands = new Hand[2];
        ante = 100;
        round = 0;
        
        this.isArtificialIntelligenceActive = isArtificialIntelligenceActive;
        if (isArtificialIntelligenceActive) AI = new ArtificialIntelligencePlayer();
        
    }
 
    /**
     * Retorna l'estat actual
     * @return L'estat actual
     */
    public PokerState getCurrentState() {
        return currentState;
    }
    

    
    /**
     * Canvia l'estat actual al passat per paràmetre
     * @param currentState 
     */
    public void setCurrentState(PokerState currentState) {
        this.currentState = currentState;
    }

    
    /**
     * Retorna la quatitat de l'ante
     * @return 
     */
    public int getAnte() {
        return ante;
    }
    
    /**
     * Retorna la quantita de fitxes de cada jugador
     * @return 
     */
    public int [] getStakes() {
        return stakes;
    }
    
    /**
     * Retorna la ronda de joc actual
     * @return 
     */
    public int getRound() {
        return round;
    }
    
    
    /**
     * Retorna si hi ha apostes pendents d'aplicar
     * @return 
     */
    public boolean areTherePendingBets(){
        return bets[HOST_INDEX] != 0 || bets[NON_HOST_INDEX] != 0;
    }
    
    
    /**
     * Indica si la IA està o no activa
     * @return 
     */
    public boolean isIsArtificialIntelligenceActive() {
        return isArtificialIntelligenceActive;
    }
    
    
    /**
     * Indica si s'ha d'abandonar o cancel·lar una partida i acabar amb tot
     * @return 
     */
    public boolean isForcedToQuit(){
        
        // per si s'acaba d'iniciar la partida cal fer una comprovació extra
        if (round == 0 && (stakes[HOST_INDEX] < ante || stakes[NON_HOST_INDEX] < ante)){
            isForcedToQuit = true;
        }
        return isForcedToQuit;
    }
    
    
    /**
     * Retorna l'String representatiu de la mà del jugador no anfitrió
     * @return 
     */
    public String getNonHostHandString() {
        return hands[NON_HOST_INDEX].toString();
    }
    
    /**
     * Retorna l'String representatiu de la mà del jugador  anfitrió
     * @return 
     */
    public String getHostHandString() {
        return hands[HOST_INDEX].toString();
    }
    
    /**
     * Retorna si l'anfitrió çes el dealer
     * @return 
     */
    public boolean isHostTheDealer() {
        return isHostTheDealer;
    }
    
    /**
     * Obté la quantitat a apostar
     * @return 
     */
    public int obtainQuantityToBet() {
        
        int betQuantity = 0;
        
        // la IA determina la quantitat a apostar
        if (isArtificialIntelligenceActive) {
            betQuantity = Math.max(1, AI.calculateQuantityToBet(hands[HOST_INDEX], Math.max(1, stakes[HOST_INDEX] - bets[HOST_INDEX] - 1)));
            
        // si la IA no està activa es genera una quantitat random
        } else {
            Random rand = new Random();
            betQuantity = 1 + rand.nextInt(Math.max(1, stakes[HOST_INDEX] - bets[HOST_INDEX] - 1));
        }
        
        return betQuantity;
         
    }

    
    
    /**
     * Inicialitza la partida de pòker
     * @return 
     */
    public ArrayList<String> start() {
        
        deck = new Deck();
        
        // el Pòker sempre comença amb l'estat inicial del joc, fent les tasques
        // d'aquest (que inclouen internament posar l'estat actual a aquest estat)
       StartState startState = new StartState();
       return startState.doAction(this);   
    }
    


    /**
     * Estableix qui és el dealer de la partida, retornant un 0 si el dealer 
     * és el jugador anfitrió i un 1 altrament
     * @return 
     */
    public int setUpDealer() {
        int dealerInt = (int) Math.round(Math.random());
        isHostTheDealer = dealerInt == 0;
        
        // configura el torn
        resetTurn();
        
        return dealerInt;
    }

    /**
     * Genera la baralla de cartes i extreu i guarda les mans dels dos jugadors
     */
    public void setUpCards() {
        
        deck = new Deck();
                
        hands[HOST_INDEX] = generateHand();
        hands[NON_HOST_INDEX] = generateHand();
    }
    
     /**
     * Genera una mà aleatòriament
     * 
     * @return 
     */
    private Hand generateHand() {
        
        // array per guardar les cartes que seran passades al constructor de Hand
        ArrayList<Card> cards = new ArrayList();
        
        // omplim l'array amb una carta aleatoria provinent del Deck
        for (int i = 0; i < Hand.NUM_CARDS; i++) {
            cards.add(deck.popRandomCard());
        }
        
        // creem i retornem la mà
        return new Hand(cards);
  
    }

    /**
     * Es computa l'aposta inicial forçada als jugadors; aquesta
     * quantitat passa al pot
     */
    public void computeAnte() {
        
        // es resetegen certs valors de partida (per si no és la primera)
        round = 1;
        hasFold = false;
        
        // es guarda com a stakes d'inici de partida els actuals, abans
        // de computar l'ante
        startStakes[HOST_INDEX] = stakes[HOST_INDEX];
        startStakes[NON_HOST_INDEX] = stakes[NON_HOST_INDEX];

        for (int i = 0; i < stakes.length; i++) {
            stakes[i] -= ante;
            pot += ante;
        }
        
    }

    /**
     * Computa la darrera aposta feta i qui l'ha fet
     * @param bet
     * @param playerIndex 
     */
    public void computeBet(int bet, int playerIndex) {
                
        // quantitat actualment apostada pel jugador
        bets[playerIndex] = bet;

        // quan algú aposta consumeix el seu torn
        turnIndex = obtainOtherPlayerIndex(playerIndex);
    }
    
     /**
     * Computa una passada de torn
     * @param playerIndex 
     */
    public void computePass(int playerIndex) {
        
       // quan algú passa consumeix el seu torn
       turnIndex = obtainOtherPlayerIndex(playerIndex);
    }
    
    
      
    /**
     * Genera una resposta per part del jugador anfitrió a la darrera aposta
     * del no anfitrió, retornant la quantitat augmentada (raise), 0 (call) o -1
     * (fold)
     * @return 
     */
    public int computeBetResponseFromHost() {
        
        int betResponse = 0;
        
        if (isArtificialIntelligenceActive) {
 
            betResponse = AI.obtainBetCriteria(hands[HOST_INDEX], Math.max(1, stakes[HOST_INDEX] - bets[HOST_INDEX] - 1));
            
        // si la IA no està activa s'escull entre FOLD, CALL i RAISE (i la quantitat
        // d'aquest) a l'atzar
        } else {
            Random rand = new Random();
            
            int choice = rand.nextInt(3);
            
            switch(choice){
                
                // FOLD
                case 0:
                    betResponse = FOLD_CODE;
                    break;
                
                // CALL
                case 1:
                    betResponse = CALL_CODE;
                    break;
                
                // RAISE
                case 2:
                    betResponse = 1 + rand.nextInt(Math.max(1, Math.max(1, stakes[HOST_INDEX] - bets[HOST_INDEX] - 1) / 10 - 1));
                    break;
                    
                default:
                    break;
            } 
        }
        
        
        switch(betResponse){
            
            case FOLD_CODE:
                computeFold(HOST_INDEX);
                break;
                
            case CALL_CODE:
                computeCall(HOST_INDEX);
                break;
                
            default:
                computeRaise(betResponse, HOST_INDEX);    
        }
        
        return betResponse;
    }

    
    /**
     * Bolca els stakes de les apostes encara no computades al pot,
     * buidant la llista d'apostes no computades
     */
    public void putPendingBetsIntoPot(){
        
        // es bolquen les apostes al pot, descomptant-les dels stakes dels jugadors
        stakes[HOST_INDEX] -= bets[HOST_INDEX]; 
        pot += bets[HOST_INDEX]; 
        bets[HOST_INDEX] = 0;
        stakes[NON_HOST_INDEX] -= bets[NON_HOST_INDEX]; 
        pot += bets[NON_HOST_INDEX]; 
        bets[NON_HOST_INDEX] = 0;
    } 
    
    
    /**
     * Donat l'índex d'un dels dos jugadors, dóna l'altre
     * @param playerIndex 
     * @return  
     */
    public int obtainOtherPlayerIndex(int playerIndex){
        
        return abs(playerIndex - 1);
    }
    
    /**
     * S'indica quin jugador s'ha rendit i es dóna tot el pot d'apostes
     * a l'altre
     * @param foldPlayerIndex 
     */
    public void computeFold(int foldPlayerIndex) {

        // es computen les apostes pendents
        putPendingBetsIntoPot();
        
        // qui no ha fet fold, guanya
        int winnerIndex = obtainOtherPlayerIndex(foldPlayerIndex);
        
        // el guanyador obté tot el contingut del pot
        stakes[winnerIndex] += pot;
        
        // es buida el pot per a la següent partida
        pot = 0;
        
        hasFold = true;
    }

    
    
    /**
     * Reseteja el torn: el no dealer començarà
     */
    public void resetTurn(){
                
        // el torn comença sent per al jugador no dealer
        turnIndex = isHostTheDealer ? NON_HOST_INDEX : HOST_INDEX; 
    }

    /**
     * Es computa un call
     * @param playerIndex 
     */
    public void computeCall(int playerIndex) {
        
        if (areTherePendingBets()){
         
            // el jugador iguala la darrera aposta, de l'altre jugador
            int previousBet = bets[obtainOtherPlayerIndex(playerIndex)];
            computeBet(previousBet, playerIndex);
        }
    }

    /**
     * Es computa un raise amb l'augment d'aposta indicat
     * @param raise 
     * @param playerIndex 
     */
    public void computeRaise(int raise, int playerIndex) {

        if (areTherePendingBets()){
         
            // el jugador aposta tant com la darrera aposta, de l'altre jugador,
            // més un cert augment
            int previousBet = bets[obtainOtherPlayerIndex(playerIndex)];
            computeBet(previousBet + raise, playerIndex);
        }
    }

    public void computeNonHostExit() {
        
        // l'abandonament del no jugador té, respecte al que havia apostat, els
        // mateixos efectes que un FOLD: perd tot el que havia apostat, que l'altre
        // guanya
        computeFold(NON_HOST_INDEX);
    }

    
    /**
     * Determina si li convé o no passar de torn a l'anfitrió
     * @return 
     */
    public boolean shouldHostPass() {
        
        boolean shouldPass = false;
        
        // la IA analitza si convé apostar
        if (isArtificialIntelligenceActive) {
            shouldPass = !AI.shouldBet(hands[HOST_INDEX]);
        }
        
        // si la IA no està activa es decideix a l'atzar
        else {
            Random rand = new Random();
            shouldPass = rand.nextFloat() > 0.5;
        }
        
        return shouldPass;
    }

    
    /**
     * Retorna si és el torn de l'anfitrió
     * @return 
     */
    public boolean isHostTurn() {
        
        return turnIndex == HOST_INDEX; 
    }


    /**
     * Es processa el final d'una partida, retornant si hi ha hagut showndown
     */
    public boolean computeEnd(){
        
        boolean showndown = !hasFold;
        
        if (showndown){
            
            putPendingBetsIntoPot();
            
            // es comparen les mans per saber qui guanya
            ComparativeHandResult handComparisonResult = HandComparatorAlgorithm.compareHands(hands[HOST_INDEX], hands[NON_HOST_INDEX]);
            
            switch (handComparisonResult){
                
                // si guanya l'anfitrió, obté tot el pot
                case BETTER:
                    stakes[HOST_INDEX] += pot;
                    break;
                    
                // si guanya el no anfitrió, obté tot el pot
                case WORSE:
                    stakes[NON_HOST_INDEX] += pot;
                    break;
                    
                // en cas d'empat total, es recuperen les quantitats inicials
                case EQUALS:
                    stakes[HOST_INDEX] = startStakes[HOST_INDEX];                    
                    stakes[NON_HOST_INDEX] = startStakes[NON_HOST_INDEX];
            }
            
        }
        else{
            // es reseteja la ronda per si comença una partida nova
            round = 0;    
        }
        
        pot = 0;
        
        // si al final d'una partida algun dels dos jugadors no assoleix l'aposta
        // mínima per participar en una altra partida, no es podrà iniciar una altra
        if (stakes[NON_HOST_INDEX] < ante || stakes[HOST_INDEX] < ante){
            isForcedToQuit = true;
        }

        return showndown;
    }

    
    /**
     * Elimina de la mà de cartes del jugador indicat les cartes passades
     * @param swapCards 
     */
    public void removePlayerCards(ArrayList<Card> swapCards, int playerIndex) {
        
        for (Card swapCard : swapCards){
            
            // se cerca la carta a les mans del jugador i s'elimina
            boolean removed = hands[playerIndex].getCards().remove(swapCard);
            
            // si no hi era, s'elimina una altra arbitràriament
            if (!removed){
                swapCard = hands[playerIndex].getCards().remove(0);
            }
            
            // es retorna a la baralla la carta
            deck.addCard(swapCard);
        }
    }

    
    /**
     * Es donen noves cartes al jugador no anfitrió
     * @param playerIndex
     * @return 
     */
    public ArrayList<Card> giveNewCardsToPlayer(int playerIndex) {
        
        // es donen al jugador tantes cartes com li faltin
        int newCardNum = NUM_CARDS - hands[playerIndex].getCards().size();
        
        ArrayList<Card> newCards = new ArrayList();
        
        for (int i = 0; i < newCardNum; i++){
            
            // s'obté una carta a l'atzar de la baralla
            newCards.add(deck.popRandomCard());
        }
        
        hands[playerIndex].addCards(newCards);
        
        return newCards;
    }

    
    /**
     * Gestiona el canvi de cartes (draw) del jugador anfitrió, tornant
     * el nombre de cartes canviades
     * @return 
     */
    public int swapHostCards() {
        
        // s'obté el conjunt de cartes a retirar
        ArrayList<Card> swapCards = obtainHostCardsToSwap();
        
        // es retornen cartes a la baralla
        removePlayerCards(swapCards, HOST_INDEX);
        
        // s'obtenen noves cartes de la baralla
        giveNewCardsToPlayer(HOST_INDEX);
        
        return swapCards.size();
    }

    
    /**
     * Es determina i retornen les cartes a treure a l'anfitrió
     * @return 
     */
    private ArrayList<Card> obtainHostCardsToSwap() {
        
        ArrayList<Card> swapCards = new ArrayList();
        
        if (isArtificialIntelligenceActive){
           swapCards = AI.calculateCardsToSwap(hands[HOST_INDEX]);
        }
        
        // es treu un nombre de cartes a l'atzar
        else{
            
            Random random = new Random();
            int swapCardNum = random.nextInt(NUM_CARDS + 1);
            for (int i = 0; i < swapCardNum; i++){
                swapCards.add(hands[HOST_INDEX].getCards().get(i));
            }
        }
            
        return swapCards;
    }

    /**
     * Es prepara el joc per la segona ronda d'apostes
     */
    public void prepareForSecondRound() {
        resetTurn();
        putPendingBetsIntoPot();
        round = 2;
    }
    
    

    
}


