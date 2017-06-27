/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import controller.ClientProtocolController;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import static poker.ArtificialIntelligencePlayer.CALL_CODE;
import static poker.ArtificialIntelligencePlayer.FOLD_CODE;
import poker.Card;
import poker.Hand;
import protocol.ProtocolCommand;
import static protocol.ProtocolController.ANTE_COMMAND;
import static protocol.ProtocolController.ANTE_OK_COMMAND;
import static protocol.ProtocolController.BET_COMMAND;
import static protocol.ProtocolController.CALL_COMMAND;
import static protocol.ProtocolController.DRAW_COMMAND;
import static protocol.ProtocolController.DRAW_SERVER_COMMAND;
import static protocol.ProtocolController.ERROR_COMMAND;
import static protocol.ProtocolController.FOLD_COMMAND;
import static protocol.ProtocolController.HAND_COMMAND;
import static protocol.ProtocolController.PASS_COMMAND;
import static protocol.ProtocolController.QUIT_COMMAND;
import static protocol.ProtocolController.RAISE_COMMAND;
import static protocol.ProtocolController.SEPARATOR;
import static protocol.ProtocolController.STAKES_COMMAND;
import static protocol.ProtocolController.START_COMMAND;
import utils.ComUtils;
import view.ClientThread.ClientInteractivityMode;

/**
 * Unitat de reacció del client a les comandes que rep del servidor; regeix com
 * respondre a aquestes
 * @author a
 */
class ClientCommandManager {

    
    // controlador de protocol
    private final ClientProtocolController ctrl;
    
    // instància de ComUtils
    private final ComUtils utils;
    
    // mode d'interacció
    private final ClientInteractivityMode mode;
    
    // scanner
    private final Scanner scanner;
    
    // indicador de si s'ha confirmat l'inici
    private boolean isStartConfirmed;
    
    // nombre molt elevat
    private final int VERY_HIGH_NUM = 99999999;
    
    
     public ClientCommandManager(ComUtils utils, ClientProtocolController ctrl, ClientInteractivityMode mode) {
        this.utils = utils;
        this.ctrl = ctrl;
        this.mode = mode;
        this.scanner = new Scanner(System.in);
        this.isStartConfirmed = false;
    }

     
    /**
     * Es reacciona a una comanda del servidor, retorna true si l'usuari decideix marxar
     * @param command 
     */
    public boolean reactToCommand(ProtocolCommand command) {
        
        
        if (command != null){
            
            try{

                String commandType = (String)command.getCommandParts().get(0);

                switch (commandType){

                    // rebre ANTE confirma l'acceptació de l'START per part del servidor
                    case ANTE_COMMAND:
                        isStartConfirmed = true;
                        break;

                    // es decideix si acceptar o no una partida
                    case STAKES_COMMAND:

                        boolean wantsToQuit = !acceptOrDeclineGame();
                        if (wantsToQuit) return true;
                        else break;

                    // si es rep HAND, PASS o DRAW_SERVER es pot apostar o passar 
                    // si es té el torn
                        
                    case HAND_COMMAND:
                        if (hasTurn()) betOrPass();
                        break;
                    case DRAW_SERVER_COMMAND:
                        resetTurn();
                        if (hasTurn()) betOrPass();
                        break; 
                    case PASS_COMMAND:
                        // si esl dos han passat i cal fer el draw, es fa
                        if (haveBothPlayersPassed() && !ctrl.isDrawDone()){
                            draw();
                        }
                        // si es té torn s'aposta/passa
                        else if (hasTurn() && !haveBothPlayersPassed()){
                            boolean passed = !betOrPass();
                            // es fa el draw si escau
                            if (passed && !ctrl.isDrawDone()) draw();
                        }
                        break;

                    case CALL_COMMAND:
                        // es fa el draw si escau
                        if (ctrl.isDrawTime()){
                            draw();
                        }
                        break;
                        
                    // si es rep una aposta o un raise, es pot fer FOLD/CALL/RAISE
                    // si es té torn
                    case BET_COMMAND:
                    case RAISE_COMMAND:
                        
                        if (hasTurn()){
                            foldCallOrRaise();
                        }
                        break;
                      
                    // si arriba un error es considerarà l'opció de marxar
                    case ERROR_COMMAND:
                        return true;
                        
                }

                // si encara no s'ha confirmat la comanda inicial, cal reenviar-la
                if (!isStartConfirmed){
                    sendStartCommand();
                }
                
            } catch (ClientQuitException ex){
                return true;
            }
        }
        
        return false;
    }

    

    
    /**
     * Es mostra una pregunta per pantalla i s'obté una resposta booleana
     * @param question
     * @return 
     */
    private boolean chooseYesOrNo(String question) throws ClientQuitException {

        String answer;
        
        while (true){
            
            System.out.println("\nPlease answer the following question with Y/N:\n" + question);
            answer = scanner.nextLine();
            
            if (answer.equalsIgnoreCase("Y")) return true;
            else if (answer.equalsIgnoreCase("N")) return false;
            else if (wantsToQuit(answer)) throw new ClientQuitException();
            else System.err.println("Answer is not valid.");
        }
    }   
    
    
    private int chooseOption(String[] options) throws ClientQuitException{
        
        String answer;
        int number;
        
        while (true){
        
            System.out.println("\nPlease choose an option:");
        
            for (int i = 0; i < options.length; i++){
                
                System.out.println(i+1 + ": " + options[i]);
            }
            
            answer = scanner.nextLine();

            if (wantsToQuit(answer)) throw new ClientQuitException();
            
            else if (ComUtils.isNumeric(answer)){
                
                number = Integer.parseInt(answer);
                
                if (number >= 1 && number <= options.length){
                    
                    return number - 1;
                }
                
                else{
                    System.err.println("A number in the option range was expected.");
                }
            }
            
            else System.err.println("A number associated with an option was expected.");
        }
    }
    
    
    /**
     * Demana un enter positiu per teclat (s'accepten nombres fins a un límit màxim)
     * @param message
     * @param bound
     * @return 
     */
    private int askForPositiveInt(String message, int bound) throws ClientQuitException {
 
        int number;
        
        while (true){
            
            System.out.println("\nPlease answer with a positive integer:\n" + message);

            String answer = scanner.nextLine();
            
            if (wantsToQuit(answer)) throw new ClientQuitException();
            
            else if (ComUtils.isNumeric(answer)){
            
                number = Integer.parseInt(answer);

                if (number > 0){
                    
                    if (number > bound) System.err.println("The number is too high.");
                    
                    else return number;
                }
                
                else {
                System.err.println("A positive number was expected.");
                }
            }
            
            else System.err.println("A positive number (of a non-massive dimension) was expected.");
            
        }
    }
    
    /**
     * Demana un enter positiu per teclat
     * @param message
     * @return
     * @throws view.ClientCommandManager.ClientQuitException 
     */
    private int askForPositiveInt(String message) throws ClientQuitException {
        return askForPositiveInt(message, VERY_HIGH_NUM);
    }
    
    
    private int askForStakesToBet() throws ClientQuitException {
        System.out.println("You now have " + ctrl.getStakes() + " stakes.");
        int stakesNum = ctrl.getStakes();
        if (stakesNum < 0){
            System.out.println("This is a non-positive number, but you will bet symbolically (1 stake) and try to win.");
            return 1;
        }
        return askForPositiveInt("How many stakes do you want add to your bet?", stakesNum - 1);
    }
    
    
    /**
     * Demana les cartes a canviar
     * @return 
     */
    private ArrayList<Card> askCardsToSwap() {
        
        ArrayList<Card> swapCards = new ArrayList();
        
        Hand hand = ctrl.getHand();
        
        Card card;
        String[] answer;
        String line;
        int index;
        boolean done = false;
        
        while (!done){
            
            swapCards.clear();
            
            System.out.println("Marca les posicions (1-" + hand.getCards().size() + ") de les cartes que vols canviar separades per espais.\n<Enter> si no vols canviar cap.");
            
            line = scanner.nextLine();
            
            if (line.equals("")){
                done = true;
            }
            answer = line.trim().split(SEPARATOR);
            
           if (!done && answer.length <= hand.getCards().size()){
               
                for (int i = 0; i < answer.length; i++){

                    if (ComUtils.isNumeric(answer[i])){

                        index = Integer.parseInt(answer[i]) - 1;

                        if (index >= 0 && index < hand.getCards().size()){

                            card = hand.getCards().get(index);

                            if (!swapCards.contains(card)){
                                swapCards.add(card);
                            } 
                            else break;
                        } 
                        else break;
                    } 
                    else break;  
                }
                
                if (swapCards.size() == answer.length) done = true;
           }
            
           if (!done){
               System.err.println("No s'ha fet la selecció de forma correcta o s'han donat repeticions.");
           }
        }
        
        return swapCards;
        
    }
    

    /**
     * Mostra per pantalla i envia una comanda
     * @param command 
     */
    private void showAndWriteCommand(ProtocolCommand command) {
        System.out.println("C: " + command);
        utils.writeProtocolCommand(command);
        boolean shouldDrawNow = ctrl.applyCommandEffects(command);
        if (shouldDrawNow) draw();
    }

    /**
     * Envia la comanda d'inici
     */
    void sendStartCommand() throws ClientQuitException {
        
        int id;
        
        switch(mode){
            
            // al cas manual es demana l'id
            case MANUAL:
                id = askForPositiveInt("What's your id?");
                break;
                
            // al cas automàtic s'escull a l'atzar
            default:
                id = ComUtils.obtainRandomPositiveInt(9999);
                break;
        }
        
        ProtocolCommand command = ProtocolCommand.obtainProtocolCommandFromString(START_COMMAND + SEPARATOR + id);
        showAndWriteCommand(command);
    }
    
    /**
     * Es decideix si s'accepta o no una partida
     */
    private boolean acceptOrDeclineGame() throws ClientQuitException {
        
        
        boolean accepted;
        
        switch(mode){
            
            case MANUAL:
                accepted = chooseYesOrNo("Do you accept the game conditions?");
                break;
                
                
            // s'escull a l'atzar als dos modes automàtics si continuar o abandonar, donant més probabilitats
            // a continuar
            default:
                accepted = new Random().nextFloat() > 0.15f;
                break;
        }
        
        if (!accepted){
            System.out.println("[!] The client has decided to quit the game.");
        }
        
        ProtocolCommand command = ProtocolCommand.obtainProtocolCommandFromString(accepted ? ANTE_OK_COMMAND : QUIT_COMMAND);
        showAndWriteCommand(command);
        
        return accepted;
    }
    
    
    /**
     * Decideix entre fer BET o PASS
     */
    private boolean betOrPass() throws ClientQuitException {
        
        boolean hasBet = false;
        
        switch(mode){
            
            // al mode manual, s'escull per opcions
            case MANUAL:
            
                String [] options = {BET_COMMAND, PASS_COMMAND};
                
                switch(chooseOption(options)){
                    
                    case 0:
                        bet();
                        hasBet = true;
                        break;
                        
                    case 1:
                        pass();
                        break;
                }
                break;
                
            // el mode automàtic atazrós decideix a l'atzar si passar o apostar    
            case AUTOMATIC_RANDOM:
                
                if (new Random().nextFloat() > 0.5){
                    pass();
                }
                
                else{
                    bet();
                    hasBet = true;
                }
                
                break;
               
            // la IA decideix si passar o apostar
            case AUTOMATIC_AI:
                
                if (ctrl.shouldAIBet()){
                    bet();
                    hasBet = true;
                }
                else pass();
                
                break;
        }
        
        return hasBet;
    }
    
    
    /**
     * Decideix entre fer FOLD, CALL o RAISE i ho porta a terme
     */
    private void foldCallOrRaise() throws ClientQuitException {
        
        switch(mode){
            
            // al mode manual, s'escull per opcions
            case MANUAL:
            
                String [] options = {FOLD_COMMAND, CALL_COMMAND, RAISE_COMMAND};
                
                switch(chooseOption(options)){

                    case 0:
                        fold();
                        break;
                        
                    case 1:
                        call();
                        break;
                        
                    case 2:
                        raise();
                        break;
                }
                break;
                
            // s'escull a l'atzar
            case AUTOMATIC_RANDOM:
                switch(new Random().nextInt(3)){

                    case 0:
                        fold();
                        break;
                        
                    case 1:
                        call();
                        break;
                        
                    case 2:
                        raise(Math.max(1, new Random().nextInt(Math.max(1, ctrl.getStakes()/2))));
                        break;
                }
                break;
                
            // la IA tria què fer
            case AUTOMATIC_AI:
                
                int response = ctrl.obtainAIBetResponse();
                
                switch(response){
                    
                    case FOLD_CODE:
                        fold();
                        break;
                        
                    case CALL_CODE:
                        call();
                        break;
                        
                    default:
                        raise(response);
                        break;
                }
                
                
                break;
            
        }  
    }
    
    
       
    /**
     * Realitza una aposta
     */
    private void bet() throws ClientQuitException {
       
        int bet = 0;
        
        switch(mode){
            
            // al mode manual es demana quant es vol apostar
            case MANUAL:
                bet = askForStakesToBet();
                break;
            
            // s'aposta una quantitat a l'atzar
            case AUTOMATIC_RANDOM:
                
                bet = Math.max(1, new Random().nextInt(Math.max(1, (int)(ctrl.getStakes() * 0.4))));
                
                break;
                
            // la IA decideix quant apostar
            case AUTOMATIC_AI:
                
                bet = Math.max(1, ctrl.obtainAIQuantityToBet());
                
                break;
        }
        
        ctrl.bet(bet);
        ProtocolCommand betCommand = ProtocolCommand.obtainProtocolCommandFromString(BET_COMMAND + SEPARATOR + bet);
        showAndWriteCommand(betCommand);
    }
    
    
    /**
     * Realitza un fold
     */
    private void fold() {
        ctrl.fold();
        ProtocolCommand fold = ProtocolCommand.obtainProtocolCommandFromString(FOLD_COMMAND);
        showAndWriteCommand(fold);
    }

    /**
     * Realitza un call
     */
    private void call() {
        ctrl.call();
        ProtocolCommand fold = ProtocolCommand.obtainProtocolCommandFromString(CALL_COMMAND);
        showAndWriteCommand(fold);
    }

    /**
     * Realitza un raise
     * @param raise 
     */
    private void raise(int raise) {
        raise = Math.max(1, raise);
        ctrl.bet(raise);
        ProtocolCommand raiseCommand = ProtocolCommand.obtainProtocolCommandFromString(RAISE_COMMAND + SEPARATOR + raise);
        showAndWriteCommand(raiseCommand);
    }

    /**
     * Demana una quantitat i fa un raise
     */
    private void raise() throws ClientQuitException {
        raise(askForStakesToBet());
    }
    
    /**
     * Passa de torn
     */
    private void pass() {
        ctrl.pass();
        ProtocolCommand passCommand = ProtocolCommand.obtainProtocolCommandFromString(PASS_COMMAND);
        showAndWriteCommand(passCommand);
    }
    
    
    /**
     * Realitza el draw
     */
    private void draw() {
        
        ArrayList<Card> swapCards = obtainSwapCards();
        
        int numCards = swapCards.size();
        String swapCardsString = "";
        for (Card card: swapCards){
            swapCardsString += SEPARATOR + card.toString();
        }
        
        ctrl.draw();
        
        // TODO: editar comanda
        ProtocolCommand drawCommand = ProtocolCommand.obtainProtocolCommandFromString(DRAW_COMMAND + SEPARATOR + numCards + swapCardsString);
        showAndWriteCommand(drawCommand);
        
    }
    
    
    
    
    /**
     * Obté les cartes a canviar al draw
     * @return 
     */
    private ArrayList<Card> obtainSwapCards(){
        
        ArrayList<Card> swapCards = new ArrayList();
        
        switch (mode){
            
            // al mode manual es demanen les cartes a canviar
            case MANUAL:
                swapCards = askCardsToSwap();
                break;
                
            // al mode aleatori se seleccionen a l'atzar
            case AUTOMATIC_RANDOM:
                 Random random = new Random();
                int swapCardNum = random.nextInt(Hand.NUM_CARDS + 1);
                for (int i = 0; i < swapCardNum; i++){
                    swapCards.add(ctrl.getHand().getCards().get(i));
                }
                break;
            
            // al mode amb IA, la IA decideix
            case AUTOMATIC_AI:
                swapCards = ctrl.obtainAICardsToSwap();
                break;
                
        }
        
        return swapCards;
    }
    

    /**
     * Retorna si el client vol marxar, a jutgar pel missatge passat
     * @param input
     * @return 
     */
    private boolean wantsToQuit(String input) {
        return input.equalsIgnoreCase(QUIT_COMMAND);
    }

    
    /**
     * Retorna si el client té torn
     * @return 
     */
    private boolean hasTurn() {
        return ctrl.hasTurn();
    }

    /**
     * Retorna si el rival ha passat
     * @return 
     */
    private boolean hasRivalPassed() {
        return ctrl.hasRivalPassed();
    }

    /**
     * Retorna si els dos jugadors han passat
     * @return 
     */
    private boolean haveBothPlayersPassed() {
        return ctrl.haveBothPlayersPassed();
    }

    /**
     * Reseteja el torn
     */
    private void resetTurn() {
        ctrl.resetTurn();
    }

 















    
    /**
     * Excepció de voler sortir amb QUIT
     */
    public static class ClientQuitException extends Exception {

        public ClientQuitException() {
        }
    }


}
