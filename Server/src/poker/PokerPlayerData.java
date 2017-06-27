/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;

import java.util.ArrayList;

/**
 * Dades d'un jugador de Pòker (si és dealer, mà, etc.)
 * @author a
 */
public class PokerPlayerData {
    
    
    // quantitat de fitxes de l'aposta inicial
    private int ante;
    
    // nombre fitxes del jugador
    private int stakes;
    
    // aposta (bet o raise) més recent del rival
    private int mostRecentBetOfRival;

    
    // indicador de si el jugador és el dealer
    private boolean isDealer;
    
    // indicador de si toca tirar al jugador
    private boolean hasTurn;
    
    // indicador de si és moment de fer el draw
    private boolean isDrawTime;
    
    // indica si ja s'ha fet el draw en aquesta partida
    private boolean isDrawDone;
    
    // indica si el jugador acaba de passar
    private boolean hasPassed;

    // indica si el rival acaba de passar
    private boolean hasRivalPassed;
    

    // mà del jugador
    private Hand hand;
    
    // IA
    private ArtificialIntelligencePlayer AI;



    /**
     * Retorna l'ante
     * @return 
     */
    public int getAnte() {
        return ante;
    }

    /**
     * Retorna els stakes
     * @return 
     */
    public int getStakes() {
        return stakes;
    }
    
    /**
     * Configura l'aposta més recent del rival
     * @param mostRecentBetOfRival 
     */
    public void setMostRecentBetOfRival(int mostRecentBetOfRival) {
        this.mostRecentBetOfRival = mostRecentBetOfRival;
    }

    /**
     * Retorna si és el dealer
     * @return 
     */
    public boolean isIsDealer() {
        return isDealer;
    }
    
    /**
     * Retorna si és moment de fer el draw
     * @return 
     */
    public boolean isDrawTime() {
        return isDrawTime;
    }
    
    /**
     * Estableix si és moment de fer el draw
     * @param isDrawTime 
     */
    public void setIsDrawTime(boolean isDrawTime) {
        this.isDrawTime = isDrawTime;
    }

    /**
     * Retorna si s'ha fet el draw ja
     * @return 
     */
    public boolean isDrawDone() {
        return isDrawDone;
    }

    /**
     * Retorna la mà
     * @return 
     */
    public Hand getHand() {
        return hand;
    }

    /**
     * Configura l'ante
     * @param ante 
     */
    public void setAnte(int ante) {
        this.ante = ante;
    }

    /**
     * Configura els stakes
     * @param stakes 
     */
    public void setStakes(int stakes) {
        this.stakes = stakes;
    }

    /**
     * Configura si és dealer
     * @param isDealer 
     */
    public void setIsDealer(boolean isDealer) {
        
        this.isDealer = isDealer;
        
        // inicialitza el torn quan el dealer canvia
        resetTurn();
        this.isDrawDone = false;
        this.isDrawTime = false;
        this.hasRivalPassed = false;
        this.hasPassed = false;
    }

    /**
     * Configura la mà
     * @param hand 
     */
    public void setHand(Hand hand) {
        this.hand = hand;
    }

    
    /**
     * Retorna si és el torn del jugador
     * @return 
     */
    public boolean hasTurn() {
        return hasTurn;
    }

    /**
     * Configura si és el torn del jugador
     * @param hasTurn 
     */
    public void setHasTurn(boolean hasTurn) {
        this.hasTurn = hasTurn;
    }
    
    
    
    /**
     * Retorna si el rival ha passat
     * @return 
     */
    public boolean hasRivalPassed() {
        return hasRivalPassed;
    }
    
    /**
     * Estableix si el rival ha pasat
     * @param hasRivalPassed 
     */
    public void setHasRivalPassed(boolean hasRivalPassed){
        this.hasRivalPassed = hasRivalPassed;
    }
    
    
    /**
     * Retorna si els dos jugadors han passat
     * @return 
     */
    public boolean haveBothPlayersPassed() {
        return hasPassed && hasRivalPassed;
    }
    
    public ArtificialIntelligencePlayer getAI() {
        return AI;
    }
    
    
    /**
     * Constructor
     * @param isArtificialIntelligenceActive
     */
    public PokerPlayerData(boolean isArtificialIntelligenceActive) {
        if (isArtificialIntelligenceActive) AI = new ArtificialIntelligencePlayer();
    }

    
    
    
    
    
    /**
     * Elimina de la mà de cartes del jugador passades
     * @param swapCards 
     */
    public void removeCards(ArrayList<Card> swapCards) {
        
        for (Card swapCard : swapCards){
            
            // se cerca la carta a les mans del jugador i s'elimina
            boolean removed = hand.getCards().remove(swapCard);
            
            // si no hi era, s'elimina una altra arbitràriament
            if (!removed){
                hand.getCards().remove(0);
            }
        }
    }
    
    /**
     * Afegeix les noves cartes a la mà del jugador
     * @param newCards 
     */
    public void addCards(ArrayList<Card> newCards){
        hand.addCards(newCards);
    }
    
    
    /**
     * Inverteix el torn
     */
    public void invertHasTurn(){
        hasTurn = !hasTurn;
    }
    
    
    /**
     * Reseteja el torn; es dóna al no dealer
     */
    public void resetTurn() {
        this.hasTurn = !isDealer;    
    }

    
    /**
     * Realitza l'acció de passar
     */
    public void pass(){
        invertHasTurn();
        mostRecentBetOfRival = 0;
        hasPassed = true;
    }
    
    /**
     * Realitza l'acció d'apostar una quantitat
     * @param bet
     */
    public void bet(int bet){
        stakes -= bet;
        invertHasTurn();
        hasPassed = false;
        hasRivalPassed = false;
    }

    /**
     * Realitza un fold
     */
    public void fold() {
        invertHasTurn();
        mostRecentBetOfRival = 0;
        hasRivalPassed = false;
        hasPassed = false;
    }

    /**
     * Realitza un call
     */
    public void call() {
        bet(mostRecentBetOfRival);
        mostRecentBetOfRival = 0;
        hasRivalPassed = false;
        hasPassed = false;
    }

    /**
     * Realitza el draw
     */
    public void draw(){
        isDrawDone = true;
        isDrawTime = false;
        hasRivalPassed = false;
        hasPassed = false;
    }


    
    
}
