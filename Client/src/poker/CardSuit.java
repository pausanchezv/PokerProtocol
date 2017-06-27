/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;

import java.util.ArrayList;

/**
 * Suit d'una carta
 * @author aespinro11.alumnes
 */
public class CardSuit {

    
    // representació de suit com a caràcter
    private final char value;
    
    // valors possibles per al suit d'una carta
    
    // valors acceptats com a suit
    private static final ArrayList<Character> possibleCardSuitValues = obtainPossibleCardSuitValues();
    
    
    public static ArrayList<Character> obtainPossibleCardSuitValues(){
        
        ArrayList<Character> arrayList = new ArrayList<>();
        arrayList.add('C');
        arrayList.add('D');
        arrayList.add('H');
        arrayList.add('S');
        
        return arrayList;
    }
    
    
     /**
     * Retorna un String representatiu de l'objecte
     * @return String representatiu de l'objecte
     */
    @Override
    public String toString() {
        return String.valueOf(value);
    }
    
    
    /**
     * Constructor
     * @param value Valor de suit
     */
    public CardSuit(char value) throws IllegalArgumentException {
        
        this.value = Character.toUpperCase(value);
        
        if (!possibleCardSuitValues.contains(this.value)){
            
            throw new IllegalArgumentException("El valor " + value + " no és un valor de suit de carta vàlid!\n");
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.value;
        return hash;
    }
    
    /**
     * Compara dos suits
     * @param obj
     * @return 
     */
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
        final CardSuit other = (CardSuit) obj;
        if (this.value != other.value) {
            return false;
        }
        return true;
    }
    
    
    
}
