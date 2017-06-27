/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;

import java.util.ArrayList;


/**
 *
 * @author psanchva9.alumnes
 */
public final class Hand {
    
    //nombre de cartes que composen una mà
    public static final int NUM_CARDS = 5;
    
    
    // retorna l'array de cartes que formen la mà
    public ArrayList<Card> getCards() {
        return cards;
    }
     
    // cartes de la mà
    private final ArrayList<Card> cards;
    
    /**
     * Retorna un String representatiu de l'objecte
     * @return String representatiu de l'objecte
     */
    @Override
    public String toString() {
        
        String res = "";
        
        for (Card obj : cards) {
            res += obj.getRank().getStringValue() + obj.getSuit().toString() + protocol.ProtocolController.SEPARATOR;
        }
        
        return res.substring(0, res.length() - 1);
    }
    
    /**
     * Constructor
     * @param cards
     */
    public Hand(ArrayList<Card> cards) {
        this.cards = new ArrayList();
        addCards(cards);
    }
    
    
    /**
     * Afegeix les cartes a la mà
     * @param cardsToAdd
     */
    public void addCards(ArrayList<Card> cardsToAdd) throws IllegalArgumentException {
        
        // es comprova que la mida de la mà sigui correcte (5 cartes)
        if (checkIfNumCardsIsValid(cardsToAdd)) {
            
            for (Card card : cardsToAdd) {
                if (cards.contains(card))
                    
                    // no tindria perquè passar mai però ho gestionem per seguretat
                    throw new IllegalArgumentException("Hi ha cartes repetides!");   
                cards.add(card);
            }
        }
        
        // en cas contrari es llança una excepció
        else {
            throw new IllegalArgumentException("El nombre cartes no és vàlid!");     
        }
    }
    
    /**
     * Elimina les cartes indicades de la mà
     * @param cardsToRemove 
     */
    public void removeCards(ArrayList<Card> cardsToRemove) {
        cards.removeAll(cardsToRemove); 
    }
    
    /**
     * Comprova que la mida de la mà sigui correcte
     * @param newCards
     * @return 
     */
    public boolean checkIfNumCardsIsValid(ArrayList<Card> newCards) {
        return (cards.size() + newCards.size()) == NUM_CARDS;
    }
    
}

