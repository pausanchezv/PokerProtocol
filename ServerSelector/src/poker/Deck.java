/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Conjunt de cartes d'una partida de Pòker
 * @author aespinro11.alumnes
 */
public class Deck {
    
    
    // conjunt de cartes
    private ArrayList<Card> cards;
    
    // nombre de cartes total
    public static final int CARD_NUM = 52;
   
    /**
     * Constructor
     */
    public Deck(){
        generateCards();
    }
    
    /**
     * Retorna les cartes de la baralla
     * @return 
     */
    public ArrayList<Card> getCards() {
        return cards;
    }
    
    /**
     * Retorna una carta a l'atzar i l'elimina de la baralla
     * 
     * @return 
     */
    public Card popRandomCard() {
        
        Random rand = new Random();
        // eliminem una carta random de la baralla i la retornem
        return cards.remove(rand.nextInt(getCurrentSize()));
    }
    
    /**
     * Retorna el nombre de cartes actuals del deck
     * 
     * @return 
     */
    public int getCurrentSize() {
        return cards.size();
    }

     /**
     * Retorna un String representatiu de l'objecte
     * @return String representatiu de l'objecte
     */
    @Override
    public String toString() {
        return "Deck{" + "cards=" + cards + '}';
    }

    
    /**
     * Genera el conjunt de cartes
     */
    private void generateCards() {
        
        cards = new ArrayList(CARD_NUM);
        
        // tindrem una carta per cada combinació de rang i suit possible (com hi ha 4 suits, 4 de cada rang)
        for (String rankString : CardRank.obtainPossibleCardRankStrings()){
            
            for (char suitChar : CardSuit.obtainPossibleCardSuitValues()){
                
                cards.add(new Card(rankString, suitChar));
            }
        }
        
        // barregem la baralla de cartes
        Collections.shuffle(cards);
    }

    /**
     * S'afegeix una carta a la baralla
     * @param swapCard 
     */
    void addCard(Card card) {
        cards.add(card);
    }
}
