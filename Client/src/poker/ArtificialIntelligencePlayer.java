/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package poker;

import java.util.ArrayList;
import java.util.Random;
import poker.HandComparatorAlgorithm.ComparativeHandResult;

/**
 *
 * @author pausanchezv
 */
public final class ArtificialIntelligencePlayer {
    
    
    // es crea una baralla paral·lela al joc
    private static Deck artificialIntelligenceDeck;
    
    // nombre de mans artificials que es generaran per comparar la mà
    private static final int NUM_COMPARE = 7000;
    
    // mínima probabilitat estimada d'èxit per arriscar-se a apostar
    private static final float SECURE_BET_THRESHOLD = 0.55f;
    
    // mínima probabilitat estimada d'èxit per descartar FOLD
    private static final float AVOID_FOLD_THRESHOLD = 0.2f;
    
    // stakes considerats com a quantitat "normal"
    public static final int DEFAULT_STAKES = 500;

    
    // codis d'operacions
    public static final int CALL_CODE = 0;
    public static final int FOLD_CODE = -1;
    
    
    /**
     * Calcula i retorna un valor entre 0 i 1 que estima la probabilitat d'èxit en cas
     * d'enfrontar-se a una altra mà
     * 
     * @param hand mà del jugador que aplica la IA
     * @return 
     */
    public float calculateBetSuccessProbability(Hand hand) {
        
        // inicialitzem la nova baralla
        artificialIntelligenceDeck = new Deck();
        
        // elimina les cartes del jugador actual del deck per formar possibles mans
        // amb las resta de cartes i fer-ho més realista. Al jugador que aplica la IA
        // no li interessa utilitzar les cartes que poseeix per a fer estimacions
        balanceDeck(artificialIntelligenceDeck, hand);
        
        // es crea el comtador de les vegades que la IA creu que s'ha d'apostar i de les vegades
        // que determina que no apostaria
        int timesThatHandIsBetter = 0;
        int timesThatHandIsWorse= 0;
        int timesThatHandsAreEquals = 0;
        
        for (int i = 0; i < NUM_COMPARE; i ++) {
            
            Hand artificialHand = generateArtificialHand(artificialIntelligenceDeck);
            
            // s'utilitza l'algorisme comparador de mans 'n' vegades per determinar si la mà del jugador
            // és millor que la mitjana de mans generades aleatòriament
            ComparativeHandResult IAResult = HandComparatorAlgorithm.compareHands(hand, artificialHand);
            
            // es valoren els resultats obtinguts
            switch (IAResult) {

                // cas en que la mà és millor que la mà generada i es determina que és millor apostar
                case BETTER:
                    timesThatHandIsBetter ++;
                    break;

                // cas en que la mà és pitjor que la mà generada i es determina que és millor no apostar
                case WORSE:
                    timesThatHandIsWorse ++;
                    break;

                // cas en què les mans són iguals (en valor)
                default:
                    timesThatHandsAreEquals ++;
                    break;
            }  
        }

        float percentage = getPercentageResult(timesThatHandIsBetter, timesThatHandIsWorse, timesThatHandsAreEquals);
        
        //System.out.println(percentage);
        
        return percentage/100.f;
        
    }
    
    
    /**
     * Calcula i retorna la quantitat que convé apostar donada una mà i un saldo
     * @param hand
     * @param stakes
     * @return 
     */
    public int calculateQuantityToBet(Hand hand, int stakes) {
        
        // com major sigui la probabilitat de vèncer amb una mà, major percentatge
        // del saldo total disponible s'aposta
        return (int)(stakes * calculateBetSuccessProbability(hand) * 0.45);
    }
    
    /**
     * Determina si és aconsellable apostar donada la mà que es té o seria millor
     * passar de torn
     * @param hand
     * @return 
     */
    public boolean shouldBet(Hand hand){
        
        // cal superar un llindar de segurerat d'èxit estimada per atrevir-se a apostar,
        // i això només si es té un mínim de fitxes
        return calculateBetSuccessProbability(hand) >= SECURE_BET_THRESHOLD;
        
    }
    
     /**
     * Determina el criteri d'apostat (FOLD, CALL o RAISE) donada una mà i un saldo
     * @param hand
     * @param stakes
     * @return 
     */
    public int obtainBetCriteria(Hand hand, int stakes) {
        
        float successProbability = calculateBetSuccessProbability(hand);
        
        // si es tenen molt poques fitxes opcions, millor abandonar
        if (stakes < DEFAULT_STAKES * 0.1 || successProbability < AVOID_FOLD_THRESHOLD){
            return FOLD_CODE;
        }
        
        // en un cas de pràctica equiprobabilitat de guanyar o perdre, s'iguala
        else if (successProbability < SECURE_BET_THRESHOLD){
            return CALL_CODE;
        }
        
        // si per contra les probabilitats de guanyar s'estimen molt altes, s'augmenta
        // l'aposta
        else{
            return calculateQuantityToBet(hand, (int)(stakes * 0.25));
        }
    }
    
    
    
    
    /**
     * Es calcula i retorna quines cartes de la mà passada per paràmetre convé canviar
     * @param hand
     * @return 
     */
    public ArrayList<Card> calculateCardsToSwap(Hand hand) {
        
        ArrayList<Card> swapCards = new ArrayList();
        
        // com pitjor es consideri la mà passada, més cartes es considerarà que cal
        // canviar, i a la inversa
        int swapCardNum = Math.min(Hand.NUM_CARDS, Math.round((1 - calculateBetSuccessProbability(hand)) * Hand.NUM_CARDS));
        
        for (int i = 0; i < swapCardNum; i++){
            swapCards.add(hand.getCards().get(i));
        }
        
        return swapCards;
    
    }
    
    
    /**
     * Genera una mà artificial per establir comparacions
     * @param artificialIntelligenceDeck
     * @return 
     */
    private Hand generateArtificialHand(Deck artificialIntelligenceDeck) {
        
        // array per guardar les cartes que seran passades al constructor de Hand
        ArrayList<Card> cards = new ArrayList();
        
        // es crea un random per les cartes
        Random rand = new Random();
        
        // omplim l'array amb una carta aleatoria provinent del Deck sense eliminar-la
        // tot i evitant cartes repetides a la mà
        while (cards.size() < Hand.NUM_CARDS) {
            
            Card obj = artificialIntelligenceDeck.getCards().get(rand.nextInt(Deck.CARD_NUM - Hand.NUM_CARDS));
            if (!cards.contains(obj)) {
                cards.add(obj);
            } 
        }

        // creem i retornem la mà artificial
        return new Hand(cards);
    }
    
    /**
     * Posa a punt la baralla
     * @param artificialIntelligenceDeck
     * @param A 
     */
    private void balanceDeck(Deck artificialIntelligenceDeck, Hand A) {
        
        // s'elimnia les cartes de la mà de la baralla
        for (Card obj : A.getCards()) {
            artificialIntelligenceDeck.getCards().remove(obj);
        }
    }
    
    /**
     * Retorna un enter que representa el percentatge de vegades que la mà és millor
     * que la resta de mans.
     * @param timesThatHandIsBetter
     * @param timesThatHandISWorst
     * @param timesThatHandsAreEquals
     * @return 
     */
    private float getPercentageResult(int timesThatHandIsBetter, int timesThatHandISWorst, int timesThatHandsAreEquals) {
        
        // nombre total de comparacions
        int total = timesThatHandIsBetter + timesThatHandISWorst + timesThatHandsAreEquals;
        return (float) (timesThatHandIsBetter) /  (float) total * 100;
    }

    





    
}
