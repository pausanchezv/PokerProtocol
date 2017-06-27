/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 *
 * @author pausanchezv
 */
public class HandComparatorAlgorithm {
    
    
    // constants que es dónen quan la mà té straight, flush o combinació d'ambdós
    // tipus
    private static final Integer [] SOLVER_NORMAL = {1};
    private static final Integer [] SOLVER_STRAIGHT = {3, 1, 1, 1};
    private static final Integer [] SOLVER_FLUSH = {3, 1, 1, 2};
    private static final Integer [] SOLVER_POWER = {5};
    
    // resultats a retornar per l'algorisme
    // hi ha 3 possibilitats al comparar una mà amb una altra, que sigui millor, igual o pitjor
    public enum ComparativeHandResult {BETTER, EQUALS, WORSE}
    
    
    /**
     * Retorna la puntuació d'una mà basant-se en el fet de comptar.
     * L'algorisme es basa en el fet de 'comptar' tot i agrupant els diferents suits per una banda
     * i els diferents valors de carta per una altra.
     * 
     * L'avantatge d'utilitzar la tècnica del recompte és que en cap moment necessitem que la mà
     * guardi informació del seu tipus (straight, flush, full, etc.) ja que en temps d'execució
     * podem obtenir aquesta informació comptant. (Memòria vs complexitat).
     * 
     * La complexitat de l'algorisme és O(n log n). Es garanetix doncs que en temps super-lineal podem saber
     * (en temps real) el valor exacte d'una mà i comparar-lo amb el valor d'una altra mà.
     * 
     * La superlinealitat de l'algorisme es deu a que en un cert put aquest ha de fer una ordenació dels
     * parells clau/valor, de no necessitar ordenar podríem gaudir d'un maravellós O(n).
     * 
     * @param hand 
     * @return  
     */
    private static ArrayList<ArrayList<Integer>> computeHandScore(Hand hand) {
        
        // valors possibles que pot assolir una carta
        String ranks = "23456789TJQKA";
        
        // diccionari comptador d'ocurrences de valors en una mà
        HashMap<Integer, Integer> dCounts = new HashMap();
        
        String strCards = "";
            
        // afegim les cartes a l'string de cartes
        for (Card obj : hand.getCards()) {
            strCards += obj.getRank().getNumericValue() == 10 ? "T" : obj.getRank().toString();
            strCards += obj.getSuit().toString();
        }
        
        // itera les cartes de la mà per construïr un string on desar totes les cartes juntes
        for (Card obj : hand.getCards()) {

            // afegim al diccionari el parell (clau = rang, valor = matches)
            String key = obj.getRank().getNumericValue() == 10 ? "T" : obj.getRank().toString();
            dCounts.put(ranks.indexOf(key), countMatches(strCards, key.charAt(0)));
        }
        
        // creem un array de parells amb els parells anteriors
        ArrayList<Pair<Integer, Integer>> rCounts = new ArrayList();
        
        // omplim l'array de parells amb els parells
        for (Map.Entry pair : dCounts.entrySet()) {
            rCounts.add(new Pair(pair.getValue(), pair.getKey()));
        }
           
        // ordena els parells de forma ascendent
        Collections.sort(rCounts, new ComparatorImpl());

        // creem dos arrays que guarden els rangs i les puntuacions
        ArrayList<Integer> rankArray = new ArrayList();
        ArrayList<Integer> scoreArray = new ArrayList();
        
        // omplim l'array de valors i de puntuacions
        for (Pair<Integer, Integer> pair : rCounts) {
            scoreArray.add(pair.getKey());
            rankArray.add(pair.getValue());
        }
        
        // li donem la volta als arrays per tenir els valors de més pes al capdavant
        Collections.reverse(scoreArray);
        Collections.reverse(rankArray);
        
        // mirem si l'array de puntuacions de cartes és igual al tamany genèric de la mà
        // per operar en conseqüència
        if (scoreArray.size() == hand.getCards().size()) {
            
            // mirem si té 3 asos
            if (rankArray.get(0) == 12 && rankArray.get(1) == 3) {
                
                Integer [] ranksArr = {3, 2, 1, 0, -1};
                rankArray.clear();
                rankArray.addAll(Arrays.asList(ranksArr));
                
            }
            
            // mirem si és un straight
            boolean straight = (rankArray.get(0) - rankArray.get(4) == 4);
            
            // creem un conjunt de suits suits
            Set suit = new HashSet();
            for (Card obj : hand.getCards()) {
                suit.add(obj.getSuit());
            }
            
            // comprovem si és un flush
            boolean flush = suit.size() == 1;
            
            // la mà té straight + flush
            if (flush && straight) {
                
                scoreArray.clear();
                scoreArray.addAll(Arrays.asList(SOLVER_POWER));
                
            // la mà té flush però no pas straight
            } else if (flush && !straight) {
                
                scoreArray.clear();
                scoreArray.addAll(Arrays.asList(SOLVER_FLUSH));
                
            // la mà té straight però no pas flush
            } else if (!flush && straight) {
                
                scoreArray.clear();
                scoreArray.addAll(Arrays.asList(SOLVER_STRAIGHT));
                
            // la mà no té ni straight ni flush
            } else {
                
                scoreArray.clear();
                scoreArray.addAll(Arrays.asList(SOLVER_NORMAL));
                
            } 
        }
        
        // arraylist que conté la solució
        ArrayList<ArrayList<Integer>> response = new ArrayList();
        response.add(scoreArray);
        response.add(rankArray);
        
        return response;
 
    }
    
    
    /**
     * Comparador de mans
     * Retorna el valor corresponent tenient en compte sempre que la primera mà passada per paràmetre
     * és comparada amb la segona.
     * 
     * @param A
     * @param B
     * @return 
     */
    public static ComparativeHandResult compareHands(Hand A, Hand B) {
        
        // es recullen els resultats del còmput
        ArrayList<ArrayList<Integer>> compute_A = computeHandScore(A);
        ArrayList<ArrayList<Integer>> compute_B = computeHandScore(B);
        
        // s'ajunten els ranks i les puntuacions en un sol array
        ArrayList<Integer> values_A = compute_A.get(0);
        values_A.addAll(compute_A.get(1));
        
        // s'ajunten els ranks i les puntuacions en un sol array
        ArrayList<Integer> values_B = compute_B.get(0);
        values_B.addAll(compute_B.get(1));
        
        // indica l'índex del guanyador
        int mutex = 0;
        
        // limit iterador
        int limit = values_A.size() < values_B.size() ? values_A.size() : values_B.size();
        
        // mentre el primer valor mutex sigui igual tenim un empat
        while (values_A.get(mutex).equals(values_B.get(mutex)) && mutex < limit - 1) {
            mutex++;
        }
        
        // desempat a favor del jugador A
        if (values_A.get(mutex) > values_B.get(mutex)) {
            return ComparativeHandResult.BETTER;
        }
        
        // desempat a favor del jugador B
        if (values_A.get(mutex) < values_B.get(mutex)) {
            return ComparativeHandResult.WORSE;
        }
    
        return ComparativeHandResult.EQUALS;
    }
    
    /**
     * Compta el nombre d'ocurrences d'un rank en un string de ranks
     * 
     * @param ranks
     * @param rank
     * @return 
     */
    private static int countMatches(String ranks, char rank) {
  
        int counter = 0;
        for(int i = 0; i < ranks.length(); i++) {
            if(ranks.charAt(i) == rank) {
                counter++;
            } 
        }
        
        return counter;
    }
    
    /**
     * Ordenador de parells
     */
    private static class ComparatorImpl implements Comparator<Pair<Integer, Integer>> {
        
        /**
         * ComparatorImpl Constructor
         */
        public ComparatorImpl() {}

        @Override
        public int compare(final Pair<Integer, Integer> p1, final Pair<Integer, Integer> p2) {
            
            if (p1.getKey() < p2.getKey()) {
                return -1;
            }
            
            if (p1.getKey() > p2.getKey()) {
                return 1;
            }
            
            if (p1.getValue() < p2.getValue()) {
                return -1;
            }
            
            if (p1.getValue() > p2.getValue()) {
                return 1;
            }
            
            return 0;
        }
    }
}

/**
 * Creador de parelles de dades
 * 
 * @author pausanchezv
 * @param <K>
 * @param <V> 
 */
final class Pair<K, V> implements Map.Entry<K, V> {
    
    //cada parella té una clau i un valor
    private final K key;
    private V value;
    
    /**
     * Constructor
     * 
     * @param key
     * @param value 
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
    
    /**
     * 
     * @return key
     */
    @Override
    public K getKey() {
        return key;
    }

    /**
     * 
     * @return value
     */
    @Override
    public V getValue() {
        return value;
    }
    
    /**
     * Canvia el valor i retorna l'antic
     * 
     * @param value
     * @return 
     */
    @Override
    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
    
    @Override
    public String toString() {
        return getKey() + " >> " + getValue();
    }
}
