/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import poker.ArtificialIntelligencePlayer;
import poker.Card;
import poker.CardRank;
import poker.CardSuit;
import poker.Hand;
import static poker.Hand.NUM_CARDS;
import poker.PokerPlayerData;
import protocol.ProtocolCommand;
import protocol.ProtocolController;
import static protocol.ProtocolController.BET_COMMAND;
import protocol.ProtocolError;
import protocol.ProtocolObject;
import utils.ComUtils;
import utils.ComUtilsBase;
import static utils.ComUtilsBase.isNumeric;

/**
 * Controlador de protocol pel client
 * 
 * @author psanchva9.alumnes
 */
public class ClientProtocolController extends ProtocolController {
    
    
    
    // dades de joc del jugador client
    private final PokerPlayerData clientPokerData;
    
    // indica si la tasca controladora ha acabat
    private boolean hasEnded;
    
    
    /**
     * Constructor
     * @param isArtificialIntelligenceActive
     */
    public ClientProtocolController(boolean isArtificialIntelligenceActive) {
        
        super();
        
        this.clientPokerData = new PokerPlayerData(isArtificialIntelligenceActive);
    }

    
    /**
     * Retorna si ha acabat
     * @return 
     */
    public boolean hasEnded() {
        return hasEnded;
    }
    
    
    /**
     * Marca la tasca controladora com a acabada
     * @param hasEnded 
     */
    public void setEnded(boolean hasEnded) {
        this.hasEnded = hasEnded;
    }
    
    
    
        
    /**
     * Llegeix les dades d'una comanda DRAW_SERVER
     * @param utils
     * @return
     * @throws IOException 
     */
    @Override
    public ProtocolObject readDrawServerCommandData(ComUtilsBase utils) throws IOException {  
        
        String unexpectedSyntaxMessage = "Unexpected arguments or syntax! Expected syntax: ";


        ArrayList<Object> commandParts = new ArrayList();
        commandParts.add(DRAW_SERVER_COMMAND);

        // s'esperen tantes cartes com li faltin al jugador client per tenir les
        // que han de formar la mà
        int swapCardsNum = NUM_CARDS - clientPokerData.getHand().getCards().size();

        char separator;

        // es llegeix cadascuna de les cartes esperades
        for (int i = 0; i < swapCardsNum; i++){

            // hem de llegir el separador, en cas contrari es notifica un
            // error de sintaxi
            separator = utils.readSeparator();

            if (separator != SEPARATOR_CHAR){
               return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(HAND_COMMAND));
            }

            Object cardReadObj = readCard(utils);

            if (cardReadObj instanceof ProtocolError){
                return (ProtocolError)cardReadObj;
            }

            else if (cardReadObj instanceof String){

                 String cardString = (String)cardReadObj;

                 commandParts.add(cardString);

            }
        }
        
        // hem de llegir el separador, en cas contrari es notifica un
        // error de sintaxi
        separator = utils.readSeparator();

        if (separator != SEPARATOR_CHAR){
           return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(HAND_COMMAND));
        }

        
        // es llegeix el nombre de cartes que ha canviat el servidor
        int serverSwaptCards = Character.getNumericValue(utils.readSeparator());
        commandParts.add(serverSwaptCards);
        

        if (commandParts.size() != swapCardsNum + 2) { 
            return new ProtocolError(unexpectedSyntaxMessage + commandSyntaxMap.get(HAND_COMMAND));
        }

        return new ProtocolCommand(commandParts);

    }
  
    
    /**
     * Processa una comanda rebuda pel client
     * @param command
     * @return 
     */
    public ArrayList<String> processCommand(String command) {

        List<String> commandParts = Arrays.asList(command.toUpperCase().trim().split(SEPARATOR));
    
        if (!commandParts.isEmpty()){
            
            String commandType = commandParts.get(0);
           
            
            switch (commandType){
                
                
                // si es rep un ANTE vàlid, es guarda l'ante a les dades del jugador
                case ANTE_COMMAND:
                    
                    if (commandParts.size() == 2){
                    
                        if (isNumeric(commandParts.get(1))){

                            int ante = Integer.parseInt(commandParts.get(1));

                            if (ante > 0){
                                clientPokerData.setAnte(ante);
                            }
                        }
                    }
                    
                // si es rep un STAKES vàlid, es guarden les fitxes del jugador                  
                case STAKES_COMMAND:
                    
                    if (commandParts.size() == 3){
                    
                        if (isNumeric(commandParts.get(1))){

                            int stakes = Integer.parseInt(commandParts.get(1));

                            if (stakes > 0){
                                clientPokerData.setStakes(stakes);
                            }
                        }
                    }
                
                // si es rep un DEALER vàlid, es guarda si és dealer el jugador
                case DEALER_COMMAND:
                    
                    if (commandParts.size() == 2){
                        
                        String dealerIndicator = commandParts.get(1);
                        
                        if (dealerIndicator.equals(HOST_IS_DEALER) || dealerIndicator.equals(NON_HOST_IS_DEALER)){
                            
                            clientPokerData.setIsDealer(dealerIndicator.equals(NON_HOST_IS_DEALER));
                        }
                    }
                    
                    break;
                    
                    
                    // si es rep un HAND vàlid, es guarda la mà del jugador
                    case HAND_COMMAND:
                        
                        if (commandParts.size() == 6){
                            
                            ArrayList<Card> cards = new ArrayList();
                                                        
                            for (String cardString : commandParts.subList(1, 6)){
                                
                                if (cardString.length() <= 3){
                                    
                                    String rank = cardString.substring(0, cardString.length()-1);
                                    
                                    if (!CardRank.obtainPossibleCardRankStrings().contains(rank)){
                                        break;
                                    }
                                    
                                    char suit = cardString.substring(cardString.length()-1, cardString.length()).charAt(0);
                                    
                                    if (!CardSuit.obtainPossibleCardSuitValues().contains(suit)){
                                        break;
                                    }
                                    Card card = new Card(rank, suit);
                                    cards.add(card);

                                } else break;
                            }
                            
                            
                            if (cards.size() == 5){
                                
                                clientPokerData.setHand(new Hand(cards));
                            }
                        }
                        
                        break;
                        
                        
                    // s'afegeixen les noves cartes de DRAW_SERVER a la mà del jugador
                    case DRAW_SERVER_COMMAND:
                        
                        ArrayList<String> cardStrings = new ArrayList<>(Arrays.asList(command.toUpperCase().trim().split(SEPARATOR)));
                        
                        if (cardStrings.size() >= 2){
                            
                            cardStrings.remove(0);
                            cardStrings.remove(cardStrings.size() - 1);

                            ArrayList<Card> newCards = new ArrayList();

                            for (String cardString : cardStrings){
                                newCards.add(new Card(cardString.substring(0, cardString.length()-1), cardString.substring(cardString.length()-1, cardString.length()).charAt(0)));
                            }

                            clientPokerData.addCards(newCards);
                        }
                        break;

                    // anotem la quantitat afegida pel rival a la seva aposta
                    case BET_COMMAND:
                    case RAISE_COMMAND:
                        
                        if (commandParts.size() == 2 && ComUtils.isNumeric(commandParts.get(1))){
                            int rivalAmount = Integer.parseInt(commandParts.get(1));
                            clientPokerData.setMostRecentBetOfRival(rivalAmount);
                        }    
                        clientPokerData.invertHasTurn();
                        break;
                        
                    case CALL_COMMAND:
                        if (!clientPokerData.isDrawDone()) setIsDrawTime(true);
                        clientPokerData.invertHasTurn();
                        break;
                        
                    case PASS_COMMAND:
                        clientPokerData.setHasRivalPassed(true);
                        clientPokerData.invertHasTurn();
                        break;
                    
                    // mostrarem el missatge d'error rebut
                    case ERROR_COMMAND:
                        break;
                    
                // No s'identifica cap comanda vàlida        
                default:
                    return null;
                
            }
            
        }
        
        
        
        // TEMP
        return null;
    
    }

    /**
     * Aplica els efectes d'una comanda que el client es disposa a enviar
     * (si s'ha de fer el draw, ho notifica retornant true)
     * @param command 
     * @return  
     */
    public boolean applyCommandEffects(ProtocolCommand command) {
        
        String commandType = (String)command.getCommandParts().get(0);
        
        
        switch (commandType){
            
            // quan el client envia un draw, cal treure-li les cartes a què renuncia
            case DRAW_COMMAND:
                
                if (command.getCommandParts().size() > 1){
                
                    if (isNumeric((String)command.getCommandParts().get(1))){

                        int swapCardNum = Integer.parseInt((String)command.getCommandParts().get(1));

                        if (command.getCommandParts().size() == swapCardNum + 2){

                            ArrayList<Card> swapCards = new ArrayList();

                            for (int i = 0; i < swapCardNum; i++){  
                                String cardString = (String)command.getCommandParts().get(2 + i);
                                try{
                                    swapCards.add(new Card(cardString.substring(0, cardString.length()-1), cardString.substring(cardString.length()-1, cardString.length()).charAt(0)));
                                } catch(IllegalArgumentException e){
                                    System.err.println("One or more cards have an illegal format, so they won't be swapt.");
                                }
                            }

                            clientPokerData.removeCards(swapCards);
                        }
                    }
                }
                break;
                
             // es fa el draw si escau   
            case CALL_COMMAND:
                if (!isDrawDone()){
                    return true;
                }
                break;
        }
        
        return false;
    }

    
    /**
     * Retorna si es té el torn
     * @return 
     */
    public boolean hasTurn() {
        return clientPokerData.hasTurn();
    }

    /**
     * Passa de torn
     */
    public void pass() {
        clientPokerData.pass();
    }

    /**
     * Retorna la IA del client
     * @return 
     */
    public ArtificialIntelligencePlayer getAI() {
        return clientPokerData.getAI();
    }
    
    /**
     * Retorna la mà del jugador
     * @return 
     */
    public Hand getHand() {
        return clientPokerData.getHand();
    }
    
    /**
     * Retorna els stakes del client
     * @return 
     */
    public int getStakes() {
        return clientPokerData.getStakes();
    }


    /**
     * Retorna si la IA hauria d'apostar
     * @return 
     */
    public boolean shouldAIBet() {
        return getAI().shouldBet(getHand());
    }

    /**
     * Aposta una quantitat
     * @param bet 
     */
    public void bet(int bet) {
        // el protocol no admet quantitat no positives (mai hauria de passar)
        bet = Math.max(1, bet);
        clientPokerData.bet(bet);
    }
    
    /**
     * Obté la decisió de la IA sobre quant apostar
     * @return 
     */
    public int obtainAIQuantityToBet() {
        return getAI().calculateQuantityToBet(getHand(), getStakes());
    }

    
    /**
     * Obté la resposta de la IA a una aposta
     * @return 
     */
    public int obtainAIBetResponse() {
        return getAI().obtainBetCriteria(getHand(), getStakes());
    }

    /**
     * Realitza un fold
     */
    public void fold() {
        clientPokerData.fold();
    }

    /**
     * Realitza un call
     */
    public void call() {
        clientPokerData.call();
    }
    
    /**
     * Realitza el draw
     */
    public void draw(){
        clientPokerData.draw();
    }
    
    
    /**
     * Retorna si és moment de fer el draw
     * @return 
     */
    public boolean isDrawTime() {
        return clientPokerData.isDrawTime();
    }

    /**
     * Estableix si és moment de fer el draw
     * @param isDrawTime 
     */
    public void setIsDrawTime(boolean isDrawTime) {
        clientPokerData.setIsDrawTime(isDrawTime);
    }

    /**
     * Retorna si s'ha fet el draw
     * @return 
     */
    public boolean isDrawDone() {
        return clientPokerData.isDrawDone();
    }

    /**
     * Retorna si el rival ha passat
     * @return 
     */
    public boolean hasRivalPassed() {
        return clientPokerData.hasRivalPassed();
    }

    /**
     * Retorna si els dos jugadors han passat
     * @return 
     */
    public boolean haveBothPlayersPassed() {
        return clientPokerData.haveBothPlayersPassed();
    }

    /**
     * Reseteja el torn
     */
    public void resetTurn() {
        clientPokerData.resetTurn();
    }

    /**
     * Retorna les cartes a canviar segons la IA
     * @return 
     */
    public ArrayList<Card> obtainAICardsToSwap() {
        return getAI().calculateCardsToSwap(getHand());
    }

 


    
    
}
