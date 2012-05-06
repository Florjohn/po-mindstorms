/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package penoplatinum.grid;

import penoplatinum.util.Bearing;

/**
 *
 * @author MHGameWork
 */
public class FacadeSector implements Sector {

  private final Grid grid;
  private final int sectorId;

  public FacadeSector(Grid grid, int sectorId) {
    this.grid = grid;
    this.sectorId = sectorId;

  }

  public int getSectorId() {
    return sectorId;
  }

  @Override
  public Sector putOn(Grid grid) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Grid getGrid() {
    return grid;
  }

  @Override
  public boolean hasNeighbour(Bearing atBearing) {
    return grid.hasNeighbour(sectorId, atBearing);
  }

  @Override
  public Sector getNeighbour(Bearing atBearing) {
    return grid.getSector(grid.getNeighbourId(sectorId, atBearing));
  }

  @Override
  public Sector setValue(int value) {
    grid.setValue(sectorId, value);
    return this;
  }

  @Override
  public int getValue() {
    return grid.getValue(sectorId);
  }

  @Override
  public Sector setWall(Bearing atBearing) {
    grid.setWall(sectorId, atBearing);
    return this;
  }

  @Override
  public Sector setNoWall(Bearing atBearing) {
    grid.setNoWall(sectorId, atBearing);
    return this;
  }

  @Override
  public Sector clearWall(Bearing atBearing) {
    grid.clearWall(sectorId, atBearing);
    return this;
  }

  @Override
  public boolean hasWall(Bearing wall) {
    return grid.hasWall(sectorId, wall);
  }

  @Override
  public boolean hasNoWall(Bearing wall) {
    return grid.hasNoWall(sectorId, wall);
  }

  @Override
  public boolean knowsWall(Bearing atBearing) {
    return grid.knowsWall(sectorId, atBearing);
  }

  @Override
  public boolean hasSameWallsAs(Sector s) {
      for (Bearing b : Bearing.NESW) {
      if (knowsWall(b) != s.knowsWall(b))
        return false;
      if (!knowsWall(b))
        continue;
      if (hasWall(b) != s.hasWall(b))
        return false;;
    }
    return true;
  }

  @Override
  public boolean isFullyKnown() {
    return grid.isFullyKnown(sectorId);
  }

  @Override
  public Sector clearWalls() {
    grid.clearWalls(sectorId);
    return this;
  }

  @Override
  public boolean givesAccessTo(Bearing atBearing) {
    return grid.givesAccessTo(sectorId, atBearing);
  }

  @Override
  public String toString() {
    return GridUtils.createSectorWallsString(this);
  }
  
  
}