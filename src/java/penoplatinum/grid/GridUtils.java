/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package penoplatinum.grid;

import penoplatinum.util.Point;

/**
 * Utility functions for grid debuggin
 * @author MHGameWork
 */
public class GridUtils {

  public static String createGridSectorsString(Grid grid) {
    String ret = "";

    for (int top = grid.getMinTop(); top <= grid.getMaxTop(); top++) {
      for (int left = grid.getMinLeft(); left <= grid.getMaxLeft(); left++) {
        Sector sector = grid.getSectorAt(new Point(left, top));
        if (sector != null) {
          ret += "(" + left + "," + top + "): " + sector.toString();
          ret += "\n";
        } else {
        }
      }
    }
    return ret;
  }

  public void DEBUG_checkGridCorrectness(Agent agent) {
//    if (!Config.DEBUGMODE) {
//      return;
//    }
//
//    boolean fail = false;
//
//    Point initial = Simulator.Running_Instance.getSimulatedEntityByName(agent.getName()).getInitialPosition();
//
//    for (Sector s : getSectors()) {
//      penoplatinum.simulator.tiles.Sector t = (penoplatinum.simulator.tiles.Sector) Simulator.Running_Instance.getMap().get(s.getLeft() + initial.x, s.getTop() + initial.y);
//      if (t == null) {
//        fail = true;
//        break;
//
//      }
//      for (int i = 0; i < 4; i++) {
//        if (!s.isKnown(i)) {
//          continue;
//        }
//        if (s.hasWall(i) != t.hasWall(i)) {
//          fail = true;
//          break;
//
//        }
//      }
//
//
//    }
//
//    if (fail) {
//      int magic = 5;//throw new RuntimeException("Grid incorrect!!!");
//    }

  }
}
