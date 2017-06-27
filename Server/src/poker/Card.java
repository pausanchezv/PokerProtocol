/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;

import java.util.Objects;



/**
 *
 * @author aespinro11.alumnes
 */
public class Card implements Comparable {
  
    
    // rang de la carta
    private final CardRank rank;
    
    // suit de la carta (bastons, diamants, etc.)
    private final CardSuit suit;

    
    /**
     * Retorna el rang de la carta
     * @return El rang de la carta
     */
    public CardRank getRank() {
        return rank;
    }

    /**
     * Retorna el suit de la carta
     * @return El suit de la carta
     */
    public CardSuit getSuit() {
        return suit;
    }
    
    
     /**
     * Retorna un String representatiu de l'objecte
     * @return String representatiu de l'objecte
     */
    @Override
    public String toString() {
        String str = rank.toString() + suit.toString();
        return str;//"Card{" + "rank=" + rank + ", suit=" + suit + '}';
    }
    
    
    /**
     * Constructor de la carta
     * @param rankString String del rang de la carta
     * @param suitChar Caràcter del suit de la carta
     */
    public Card(String rankString, char suitChar) throws IllegalArgumentException{
        
        this.rank = new CardRank(rankString);
        
        this.suit = new CardSuit(suitChar);
    }
    
    /**
     * Compara dues cartes segons el seu rang
     * @param other
     */ 
    @Override
    public int compareTo(Object other) {
        return new Integer(this.getRank().getNumericValue()).compareTo(((Card) other).getRank().getNumericValue());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Card other = (Card) obj;
        if (!Objects.equals(this.rank.getNumericValue(), other.rank.getNumericValue())) {
            return false;
        }
        if (!Objects.equals(this.suit, other.suit)) {
            return false;
        }
        return true;
    }



    
   
    
}
