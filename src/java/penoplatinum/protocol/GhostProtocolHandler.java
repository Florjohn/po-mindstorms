  package penoplatinum.protocol;

/**
 * GhostProtocolHandler
 * 
 * Implementation of a MessageHandler, handling the Ghost Protocol.
 * 
 * @author: Team Platinum
 */

import penoplatinum.Config;
import penoplatinum.gateway.GatewayClient;
import penoplatinum.grid.BarcodeAgent;
import penoplatinum.grid.PacmanAgent;
import penoplatinum.grid.Sector;
import penoplatinum.util.Bearing;
import penoplatinum.util.Point;
import penoplatinum.util.Scanner;
import penoplatinum.util.SimpleHashMap;


public abstract class GhostProtocolHandler implements ProtocolHandler {

  private final static int MIN_JOINS = 4;

  // counter for received join commands
  private int joins = 0;
  // indicates if we've successfully joined
  private boolean joined = false;
  private final String protocolVersion = "2.1";
  static final String baseName = "Platinum";
  private final String name = GhostProtocolHandler.generateName();

  // the client we're using to communicate with the gateway
  private GatewayClient client;
  private ExternalEventHandler eventHandler;
  
  private SimpleHashMap<String, String> names = new SimpleHashMap<String, String>();
  private SimpleHashMap<String, String> renamed;
  
  private boolean renaming = false;
  
  
  /*
   ********************
   * Things we receive*
   ********************
   */
  
  // accepts a string, parses it and dispatches it...
  @Override
  public void receive(String msg) {
    // strip off the newline
    msg = msg.substring(0, msg.length() -1);
    Scanner scanner = new Scanner(msg);

    String agentName = scanner.next();
    if( "JOIN".equals(agentName) && ! scanner.hasNext() ) {
      this.handleJoin();
    }
    else {
      try{handleCommand(agentName, scanner);}
      catch(Exception e){System.out.println("An error in a command!");}
    }
  }
  
  private void handleCommand(String agentName, Scanner scanner){
    String command = scanner.next();
    
    switch(command){
      case "NAME":
        handleName(agentName, scanner);
        break;
      case "RENAME":
        handleRename(agentName, scanner);
        break;
      case "POSITION":
        handlePosition(agentName, scanner);
        break;
      case "DISCOVER":
        handleDiscover(agentName, scanner);
        break;
      case "BARCODEAT":
        handleBarcodeAt(agentName, scanner);
        break;
      case "PACMAN":
        handlePacman(agentName, scanner);
        break;
      case "CAPTURED":
        handleCaptured(agentName, scanner);
        break;
      case "PING":
        handlePing(agentName, scanner);
        break;
//      case "PONG":
//        handlePong(agentName, scanner);
//        break;
      case "SHOWMAP":
        handleShowMap(agentName, scanner);
        break;
//      case "REPOSITION":
//        handleReposition(agentName, scanner);
//        break;
//      case "REDISCOVER":
//        handleRediscover(agentName, scanner);
//        break;
//      case "REBARCODEAT":
//        handleRebarcodeAt(agentName, scanner);
//        break;
//      case "REPACMAN":
//        handleRepacman(agentName, scanner);
//        break;
      default:
        //We don't recognize the command so we ignore it
        break;
    }
  }
  
  
  
  /*
   **************** 
   * Things we do *
   ****************
   */
  
  @Override
  public ProtocolHandler handleStart() {
    this.sendJoin();
    return this;
  }

  // when we enter a new sector, we need to send out our position
  @Override
  public ProtocolHandler handleEnterSector(Sector sector) {
    this.sendPosition(sector.getGrid().getPositionOf(sector));
    return this;
  }

  // when we find a new sector, we need to send out discover information
  @Override
  public ProtocolHandler handleFoundSector(Sector sector) {
    this.sendDiscover(sector.getGrid().getPositionOf(sector),
                      sector.hasWall(Bearing.N), sector.hasWall(Bearing.E),
                      sector.hasWall(Bearing.S), sector.hasWall(Bearing.W));
    return this;
  }

  // there are different types of agents:  
  // - Pacman
  @Override
  public ProtocolHandler handleFoundAgent(Sector sector, PacmanAgent agent) {
    this.sendPacman(sector.getGrid().getPositionOf(sector));
    return this;
  }

  // - Barcode
  @Override
  public ProtocolHandler handleFoundAgent(Sector sector, BarcodeAgent agent) {
    this.sendBarcodeAt(sector.getGrid().getPositionOf(sector), agent.getValue(),
                       agent.getBearing());
    return this;
  }
  
  public ProtocolHandler handleResendData(Iterable<Sector> sectors, Point pacmanPoint, Point position){
    this.sendRePosition(position);
    this.sendRePacman(position);
    for(Sector sec: sectors){
      this.sendReDiscover(sec.getGrid().getPositionOf(sec),
                      sec.hasWall(Bearing.N), sec.hasWall(Bearing.E),
                      sec.hasWall(Bearing.S), sec.hasWall(Bearing.W));
      //TODO check voor barcode
    }
    return this;
  }
  
  // internal implementation of the actual protocol
  
  
  
 /*
  ******************
  * Things we send *
  ******************
  */
  
  // example: JOIN
  private void sendJoin() {
    this.send("JOIN");
  }

  // example: platinum NAME 2.1
  private void sendName() {
    this.send(this.getName() + " NAME " + this.getVersion());
  }
  
  private void sendPosition(Point position) {
    this.send(this.getName() + " POSITION " + 
              this.translateToExternalFormat(position));
  }

  // example: platinum DISCOVER 10,13
  private void sendDiscover(Point position, 
                            Boolean n, Boolean e, Boolean s, Boolean w)
  {
    this.send(this.getName() + " DISCOVER " +
              this.translateToExternalFormat(position) + " " + 
              this.encodeTrit(n) + " " + this.encodeTrit(e) + " " +
              this.encodeTrit(s) + " " + this.encodeTrit(w) );
  }

  // example: platinum PACMAN 10,13
  private void sendPacman(Point position) {
    this.send(this.getName() + " PACMAN " + 
              this.translateToExternalFormat(position));
  }
  
  // example: platinum BARCODEAT 10,13 24 2
  private void sendBarcodeAt(Point position, int code, Bearing bearing) {
    this.send(this.getName() + " BARCODEAT " + 
              this.translateToExternalFormat(position) + " " + 
              code + " " +
              this.translateBearingToDirection(bearing));
  }
  
  private void sendRename(){
    this.send(this.getName()+" RENAME "+this.getVersion());
  }
  
  private void sendPong(String agentName, String message){
    this.send(this.getName()+" PONG "+agentName+" "+message);
  }
  
  private void sendRePosition(Point position) {
    this.send(this.getName() + " REPOSITION " + 
              this.translateToExternalFormat(position));
  }
  private void sendReDiscover(Point position, 
                            Boolean n, Boolean e, Boolean s, Boolean w)
  {
    this.send(this.getName() + " REDISCOVER " +
              this.translateToExternalFormat(position) + " " + 
              this.encodeTrit(n) + " " + this.encodeTrit(e) + " " +
              this.encodeTrit(s) + " " + this.encodeTrit(w) );
  }
  
  private void sendReBarcodeAt(Point position, int code, Bearing bearing) {
    this.send(this.getName() + " REBARCODEAT " + 
              this.translateToExternalFormat(position) + " " + 
              code + " " +
              this.translateBearingToDirection(bearing));
  }
  
  private void sendRePacman(Point position) {
    this.send(this.getName() + " REPACMAN " + 
              this.translateToExternalFormat(position));
  }

  /*
   *************************************
   * Handlers for commandos we receive *
   *************************************
   */

  // we need to count incoming JOINs, after MIN_JOINS we can begin
  private void handleJoin() {
    this.joins++;
    // init conversation if we have seen 4 joins (we were first)
    if( !this.joined && this.joins >= MIN_JOINS ) {
      this.begin();
    }
    else if(this.joined){
      this.renaming = true;
      this.renamed = new SimpleHashMap<String, String>();
      this.sendRename();
    }
  }
  
  private void begin() {
    this.joined = true;
    this.joins  = MIN_JOINS;

    // send out own name
    this.sendName();
    
    // raise the Activtion event
    this.eventHandler.handleActivation();
  }

  private void handleName(String agentName, Scanner scanner) {
    String version = scanner.next();
    if(renaming){
        if(renamed.get(agentName) != null){
        renamed.put(agentName, version);
        this.eventHandler.handleNewAgent(agentName);
      }
        checkRenamingFinished();
    }
    else{
      // if we're still waiting for joins, we now can begin...
      if(!this.joined) {
        this.begin();
      }
      if(names.get(agentName) == null){
        names.put(agentName, version);
        this.eventHandler.handleNewAgent(agentName);
      }
      // raise new Agent event
    }
  }
  
  private void handleRename(String agentName, Scanner scanner){
    String version = scanner.next();
    if(renaming){
        if(renamed.get(agentName) != null){
        renamed.put(agentName, version);
      }
    }
  }
  private void handlePosition(String agentName, Scanner scanner) {
    if(!isKnown(agentName))
      return;
    Point position = new Point(scanner.nextInt(), scanner.nextInt());
    this.eventHandler.handleAgentInfo(agentName,
                                      this.translateToInternalFormat(position),
                                      0, Bearing.UNKNOWN);
  }
  
  private void handleDiscover(String agentName, Scanner scanner){
    if(!isKnown(agentName))
      return;
    Point position = new Point(scanner.nextInt(), scanner.nextInt());
    int n = scanner.nextInt();
    int e = scanner.nextInt();
    int s = scanner.nextInt();
    int w = scanner.nextInt();
    
    // raise SectorInfo event
    this.eventHandler.handleSectorInfo(agentName, 
                                       this.translateToInternalFormat(position), 
                                       this.decodeTrit(n), this.decodeTrit(e),
                                       this.decodeTrit(s), this.decodeTrit(w));
  }
  
  private void handleBarcodeAt(String agentName, Scanner scanner)
  {
    if(!isKnown(agentName))
      return;
    Point position = new Point(scanner.nextInt(), scanner.nextInt());
    int code = scanner.nextInt();
    Bearing direction = this.translateDirectionToBearing(scanner.nextInt());
    // raise AgentInfo event
    this.eventHandler.handleAgentInfo(agentName,
                                      this.translateToInternalFormat(position),
                                      code, direction);
  }
  
  private void handlePacman(String agentName, Scanner scanner) {
    if(!isKnown(agentName))
      return;
    Point position = new Point(scanner.nextInt(), scanner.nextInt());
    // raise AgentInfo event
    this.eventHandler.handleTargetInfo(agentName, 
                                      this.translateToInternalFormat(position));
  }
  
  private void handlePing(String agentName, Scanner scanner){
    if(!isKnown(agentName))
      return;
    String receiver = scanner.next();
    String message = scanner.next();
    if(receiver.equals(this.getName()) || receiver.equals("*")){
      sendPong(agentName, message);
    }
  }
  
//  private void handlePong(String agentName, Scanner scanner){
//    if(!isKnown(agentName))
//      return;
//    String receiver = scanner.next();
//    String message = scanner.next();
//    if(receiver.equals(this.getName())){
//      //TODO do something
//    }
//  }
  
  private void handleShowMap(String agentName, Scanner scanner){
    if(!isKnown(agentName))
      return;
    String requestedClient = scanner.next();
    if(!requestedClient.equals(this.name))
      return;
    this.eventHandler.handleSendGridInformation();
  }
  
  private void handleCaptured(String agentName, Scanner scanner){
    if(!isKnown(agentName))
      return;
    this.eventHandler.handleCaptured(agentName);
    
  }
 

  // general purpose send command

  private void send(String msg) {
    this.client.send(msg + "\n", Config.BT_GHOST_PROTOCOL);
  }
  
  

  /*
   *********************
   * Encode and decode *
   *********************
   */
  
    // we use a left/top oriented coordinate system, the outside world uses
    // a system where the Y-axis points up
    // invert the Y-axis
    private Point translateToExternalFormat(Point point) {
      return new Point(point.getX(), -1 * point.getY());
    }

    // we use a left/top oriented coordinate system, the outside world uses
    // a system where the Y-axis points up
    // invert the Y-axis
    private Point translateToInternalFormat(Point point) {
     return new Point(point.getX(), -1 * point.getY());
    }

    // 1=N>S 2=E>W 3=S>N 4=W>E  
    private int translateBearingToDirection(Bearing bearing) {
     switch(bearing) {
       case N: return 1;
       case E: return 2;
       case S: return 3;
       case W: return 4;
     }
     throw new RuntimeException( "Invalid Bearing: " + bearing );
    }

   private Bearing translateDirectionToBearing(int direction) {
    switch(direction) {
      case 1: return Bearing.N;
      case 2: return Bearing.E;
      case 3: return Bearing.S;
      case 4: return Bearing.W;
    }
    return Bearing.UNKNOWN;
    // let's not throw an Exception here, this is based on other team's input
    // we can't trust them and don't want to provide them with a DOS tool ;-)
    // throw new RuntimeException( "Invalid direction: " + direction );
   }
  
   private int encodeTrit(Boolean wall) {
    return wall == null ? 2 : (wall ? 1 : 0);
   }

   protected static Boolean decodeTrit(int wall) {
    return wall == 2 ? null : (wall == 1);
   }
  
   
   
  /*
   ******************
   * Utility methods*
   ******************
   */
  
  @Override
  public String getVersion() {
    return this.protocolVersion;
  }
  
  // abstract callback to retrieve own name
  @Override
  public String getName(){
    return this.name;
  }
  
  // keep a reference to the GatewayClient to send messages
  @Override
  public GhostProtocolHandler useGatewayClient(GatewayClient client) {
    this.client = client;
    return this;
  }
  
  @Override
  public ProtocolHandler useExternalEventHandler(ExternalEventHandler handler) {
    this.eventHandler = handler;
    return this;
  }
  
  private boolean isKnown(String agentName){
    if(names.get(agentName) == null)
      return false;
    return true;
  }
  
  private static String generateName(){
    return baseName+System.currentTimeMillis();
  }
  
  private void checkRenamingFinished(){
    if (!this.renaming)
      return;
    if(this.renamed.size() >= GhostProtocolHandler.MIN_JOINS-1){
      for(String s: names){
        if(renamed.get(s) == null)
          this.eventHandler.handleRemoveAgent(s);
      }
      this.names = new SimpleHashMap<String, String>();
      for(String s: renamed){
        names.put(s, this.renamed.get(s));
      }
      renaming = false;
    }
  }
}
