public class GridUpdateProcessor extends ModelProcessor {

  public GridUpdateProcessor() { super(); }
  public GridUpdateProcessor( ModelProcessor nextProcessor ) {
    super(nextProcessor);
  }

  // update the agent
  protected void work() {
    this.updateWallInfo();
    this.addNewSectors();
    this.updateHillClimbingInfo();
  }

  // update the current Sector on the Grid to reflect the currently selected
  private void updateWallInfo() {
    GhostModel model = (GhostModel)this.model;

    Sector detected = model.getDetectedSector();
    Sector current  = model.getCurrentSector();
    for(int atLocation=Bearing.N; atLocation<=Bearing.W; atLocation++ ) {
      if( detected.isKnown(atLocation) ) {
        if( detected.hasWall(atLocation) ) {
          //if( current.isKnown(atLocation) && !current.hasWall(atLocation) ) {
          //  current.clearWall(atLocation);
          //} else {
            current.addWall(atLocation);
          //}
        } else {
          //if( current.isKnown(atLocation) && current.hasWall(atLocation) ) {
          //  current.clearWall(atLocation);
          //} else {
            current.removeWall(atLocation);
          //}
        }
      }
    }
  }

  // if there are bearing without walls, providing access to unknown Sectors,
  // add such Sectors to the Grid
  private void addNewSectors() {
    Sector current = ((GhostModel)this.model).getCurrentSector();
    for(int location=Bearing.N; location<=Bearing.W; location++) {
      if( current.givesAccessTo(location) &&
          ! current.hasNeighbour(location) )
      {
        // TODO: parameterize the value
        //System.out.println(current.getAgent().getName() + " : adding unknown sector(" + location +")" );
        current.createNeighbour(location).setValue(5000);
      }
    }
  }
  
  private void updateHillClimbingInfo() {
    ((GhostModel)this.model).getGrid().refresh();
  }
}
