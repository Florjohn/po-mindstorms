public class Sector {
  // back-link to the Grid we live in
  private Grid grid;
  
  // links to the adjectant Sectors
  private Sector[] neighbours = new Sector[4];
  // absolute positions within Grid
  private int left, top;

  // walls and the certainty about them
  private Tile walls     = new Tile();
  private Tile certainty = new Tile();

  // the agent currently on this Sector
  private Agent agent;

  // the value associated with this sector. it is used to create collaborated
  // diffusion
  private int value = 0;


  // TODO: sort this out ;-)
  public Sector() {
    this.grid = new Grid(); // a dummy one, untill we get one assigned
  }
  
  public Sector(Grid grid) {
    this.grid = grid;
  }

  public Sector setGrid(Grid grid) {
    this.grid = grid;
    return this;
  }
  
  public Grid getGrid() {
    return this.grid;
  }
  
  // adds a neighbour at a given location
  public Sector addNeighbour(Sector neighbour, int atLocation) {
    this.neighbours[atLocation] = neighbour;
    this.exchangeWallInfo(atLocation);
    return this;
  }
  
  public Sector createNeighbour(int atLocation) {
    // if we already have a neighbour at this location, return it
    if(this.hasNeighbour(atLocation)) { return this.getNeighbour(atLocation);}
    // create empty sector and assign it coordinates relative to ours
    int left = this.getLeft(), top = this.getTop();
    switch(atLocation) {
      case Bearing.N: top--;  break;
      case Bearing.E: left++; break;
      case Bearing.S: top++;  break;
      case Bearing.W: left--; break;
    }
    Sector neighbour = new Sector().setCoordinates(left, top);
    // add it to the grid, it will be connected to us and all other relevant
    // sectors
    this.grid.addSector(neighbour);
    return neighbour;
  }
  
  public Sector exchangeWallInfo(int atLocation) {
    Sector  neighbour = this.neighbours[atLocation];
    if( neighbour == null ) { return this; }
    
    int     locationAtNeighbour = Bearing.reverse(atLocation);
    Boolean iHaveWall           = this.hasWall(atLocation);
    Boolean neighbourHasWall    = neighbour.hasWall(locationAtNeighbour);

    // if we have different information, we need to update it
    if( neighbourHasWall != iHaveWall ) {
      if( neighbourHasWall == null ) { // T/F != null
        if(iHaveWall) { neighbour.inheritWall(locationAtNeighbour);   }
                 else { neighbour.inheritNoWall(locationAtNeighbour); }
      } else if( iHaveWall == null ) {   // null != T/F
        if(neighbourHasWall) { this.inheritWall(atLocation);   }
                        else { this.inheritNoWall(atLocation); }
      } else {                           // T/F != F/T
        // conflicting information => clear both, go back to unknown state
        System.err.println( "Conflicting Wall information: " + this.getLeft() + "," + this.getTop() + " / " + atLocation );
        this.clearWall(atLocation);
        neighbour.clearWall(locationAtNeighbour);
      }
    }
    return this;
  }
  
  public boolean hasNeighbour(int atLocation) {
    return this.getNeighbour(atLocation) != null;
  }

  // returns the neighbour at a given location
  public Sector getNeighbour(int atLocation) {
    if( atLocation == Bearing.NONE ) { return this; }
    return this.neighbours[atLocation];
  }

  // sets the absolute coordinates in the Grid this Sector is placed in
  public Sector setCoordinates(int left, int top) {
    this.left = left;
    this.top  = top;
    return this;
  }
  
  public int getLeft() {
    return this.left;
  }
  
  public int getTop() {
    return this.top;
  }
  
  // keeps track of an agent occupying this sector
  public Sector putAgent(Agent agent, int bearing) {
    this.agent = agent;
    this.agent.setSector(this, bearing);
    this.grid.addAgent(this.agent);
    return this;
  }

  public boolean hasAgent() {
    return this.agent != null;
  }
  
  public Agent getAgent() {
    return this.agent;
  }
  
  // removes an agent
  public Sector removeAgent() {
    this.agent = null;
    return this;
  }
  
  // if an agent is occupying us, return the agent's value else our own
  public int getValue() {
    return this.agent != null ? this.agent.getValue() : this.value;
  }
  
  // returns the value of the sector, not taking in account any agent
  public int getRawValue() {
    return this.value;
  }
  
  // sets the value of the sector
  public Sector setValue(int value) {
    this.value = value;
    return this;
  }
  
  // adds a wall on this sector at given location
  public Sector addWall(int atLocation) {
    this.inheritWall(atLocation);
    // also set the wall at our neighbour's
    if( this.hasNeighbour(atLocation) ) {
      this.getNeighbour(atLocation).inheritWall(Bearing.reverse(atLocation));
    }
    // refresh the rendering of the walls
    this.grid.getView().refreshWalls();
    return this;
  }
  
  protected void inheritWall(int atLocation) {
    this.walls.withWall(atLocation);
    this.certainty.withWall(atLocation);
  }

  // removes a wall from this sector at given location
  public Sector removeWall(int atLocation) {
    this.inheritNoWall(atLocation);
    // also remove the wall at our neighbour's
    if( this.hasNeighbour(atLocation) ) {
      this.getNeighbour(atLocation).inheritNoWall(Bearing.reverse(atLocation));
    }
    // refresh the rendering of the walls
    this.grid.getView().refreshWalls();
    return this;
  }
  
  protected void inheritNoWall(int atLocation) {
    this.walls.withoutWall(atLocation);
    this.certainty.withWall(atLocation);
  }

  // adds all walls at once
  public Sector addWalls(char walls) {
    this.walls.withWalls(walls);
    this.certainty.withWalls((char)15); // mark all walls as certain
    // also update neighbours
    this.updateNeighboursWalls();

    // refresh the rendering of the walls
    this.grid.getView().refreshWalls();
    return this;
  }
  
  protected void updateNeighboursWalls() {
    for(int atLocation=Bearing.N; atLocation<=Bearing.W; atLocation++ ) {
      Sector neighbour = this.getNeighbour(atLocation);
      Boolean haveWall = this.hasWall(atLocation);
      if( neighbour != null && haveWall != null ) {
        if( haveWall ) {
          neighbour.inheritWall(Bearing.reverse(atLocation));
        } else {
          neighbour.inheritNoWall(Bearing.reverse(atLocation));
        }
      }
    }
  }
  
  // we use the Boolean class here to be able to return "null" when we don't
  // know anything about the wall.
  public Boolean hasWall(int wall) {
    return this.certainty.hasWall(wall) ? this.walls.hasWall(wall) : null;
  }
  
  // returns all wall configuration
  public char getWalls() {
    return this.walls.getWalls();
  }

  // clears the certainty information of the Sector
  public Sector clearCertainty() {
    this.certainty.clear();
    return this;
  }
  
  // clears all knowledge about a wall
  public Sector clearWall(int atLocation) {
    this.walls.withoutWall(atLocation);
    this.certainty.withoutWall(atLocation);
    return this;
  }
  
  // returns the certainty information about the Sector
  public int getCertainty() {
    return this.certainty.getWalls();
  }

  // determines if the wall at given location is known (to be there or not)
  public boolean isKnown(int atLocation) {
    return this.certainty.hasWall(atLocation);
  }
  
  public boolean isFullyKnown() {
    return this.getCertainty() == 15;
  }
  
  // we use the Boolean class to allow returning null to indicate we don't
  // know (because of uncertainty)
  public Boolean isBlocked(int atLocation) {
    return this.hasWall(atLocation) == null ? 
      null : (this.hasWall(atLocation) || this.facesAgent(atLocation));
  }

  // TODO: should we take into account dat if there's a wall between these
  //       Sections and also take into account uncertainty ?
  public boolean facesAgent(int atLocation) {
    return this.getNeighbour(atLocation) != null
           && this.getNeighbour(atLocation).hasAgent();
  }
}