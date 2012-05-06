package penoplatinum.navigator.mode;

/**
 * ChaseHillClimbingNavigatorMode
 *
 * Implements a NavigatorMode that uses Hill Climbing to discover all Sectors
 * on a Grid.
 *
 * @author Team Platinum
 */

import java.util.List;
import java.util.Arrays;

import penoplatinum.util.Bearing;

import penoplatinum.grid.Grid;
import penoplatinum.grid.Sector;
import penoplatinum.grid.agent.Agent;

import penoplatinum.model.Model;
import penoplatinum.model.part.GridModelPart;

import penoplatinum.navigator.action.NavigatorAction;


public class ChaseHillClimbingNavigatorMode extends HillClimbingNavigatorMode {

  public ChaseHillClimbingNavigatorMode(Model model) {
    super(model);
    
    this.grids.onlyApplyCollaborateDiffusionOnPacman();
  }

  // we reached our goal if we're next to Pacman
  public boolean reachedGoal() {
    // TODO: clean this up ;-)
    Grid myGrid = this.grids.getMyGrid();
    Agent pacman = myGrid.getAgent("pacman"), // TODO: magic name constant
          me     = this.grids.getMyAgent();
    if(pacman == null){ //no pacman found
      //Utils.Log("Pacman not found");
      return false;
    }
    Sector pacmanSector = myGrid.getSectorAt(myGrid.getPositionOf(pacman)),
           mySector     = myGrid.getSectorAt(myGrid.getPositionOf(me));

    for(Bearing atLocation : Bearing.NESW) {
      if( pacmanSector == mySector.getNeighbour(atLocation) ) { return true; }
    }

    return false;
  }
}
