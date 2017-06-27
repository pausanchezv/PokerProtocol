/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Rang d'una carta, consistent en una representació String amb un valor numèric associat
 * @author aespinro11.alumnes
 */
public class CardRank {

  
    
    // representació del rang com a String
    private final String stringValue;
    
    
    // valor numèric del rang
    private int numericValue;
    
    
    private static final HashMap<String, Integer> cardRankMap = createCardRankMap();

    private static HashMap<String, Integer> createCardRankMap(){
        
        HashMap<String, Integer> hashMap = new HashMap();
        
        hashMap.put("A", 14);
        hashMap.put("K", 13);
        hashMap.put("Q", 12);
        hashMap.put("J", 11);
        hashMap.put("10", 10);
        hashMap.put("9", 9);
        hashMap.put("8", 8);
        hashMap.put("7", 7);
        hashMap.put("6", 6);
        hashMap.put("5", 5);
        hashMap.put("4", 4);
        hashMap.put("3", 3);
        hashMap.put("2", 2);
                
        return hashMap;
    }
    
    
     /**
     * Retorna tots els valors possibles per al rang
     * @return Tots els valors possibles per al rang
     */
    public static ArrayList<String> obtainPossibleCardRankStrings() {
        return new ArrayList(cardRankMap.keySet());
    }
    
    
    /**
     * Retorna la representació String
     * @return La representació String
     */
    public String getStringValue() {
        return stringValue;
    }

    /**
     * Retorna el valor numèric
     * @return El valor numèric
     */
    public int getNumericValue() {
        return numericValue;
    }
    
     /**
     * Retorna un String representatiu de l'objecte
     * @return String representatiu de l'objecte
     */
    @Override
    public String toString() {
        return  stringValue;
    }
    
    /**
     * Constructor
     * 
     * @param stringValue
     * @throws IllegalArgumentException 
     */
    public CardRank(String stringValue) throws IllegalArgumentException {
        
        this.stringValue = stringValue;
        
        // obtenim el valor numèric associat al seu valor String, si existeix
        if (cardRankMap.containsKey(stringValue)){
            
            this.numericValue = cardRankMap.get(stringValue);
        }
        
        // si el valor String rebut no és vàlid, cal fer-ho notar a qui hagi cridat el constructor
        // mitjançant una excepció
        else{
            
            throw new IllegalArgumentException("El valor " + stringValue + " no és un rang de carta vàlid!\n");
        }
        
    }

    

    
}
