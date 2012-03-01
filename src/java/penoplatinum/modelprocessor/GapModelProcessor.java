package penoplatinum.modelprocessor;

/**
 * Implements a modelProcessor that gathers the information of the SonarSweep
 * It records a complete sweep and at the end, pushes the information to the
 * model.
 * 
 * Author: Team Platinum
 */
import penoplatinum.simulator.Model;

public class GapModelProcessor extends ModelProcessor {

  private Boolean direction;
  private int prevAngle = 0;
  public static final int MIN_GAP_ANGLE = 40;
  public static final int MAX_GAP_ANGLE = 90;
  public static final int MIN_GAP_DISTANCE = 80;
  public static final int MAX_GAP_INGORE_ANGLE = 1;
  private boolean gapUnique = true;

  public GapModelProcessor(ModelProcessor nextProcessor) {
    super(nextProcessor);
  }

  public void setGapMustBeUnique(boolean value) {
    gapUnique = value;
  }

  public void work() {
    model.setGapFound(false);
    // if we changed direction
    if (this.changedDirection()) {

      this.direction = this.getDirection();

      // close a possible open gap
      closeGap();

      // push a copy of the info to model
      model.setGapFound(!fail && uniqueGapFound);
      if (model.isGapFound()) {
        model.setGapEndAngle(gapEndAngle);
        model.setGapStartAngle(gapStartAngle);
      }

      // prepare for next sweep

      fail = false;
      uniqueGapFound = false;
      gapStartAngle = Integer.MAX_VALUE;
      gapEndAngle = Integer.MAX_VALUE;
      gapStartFound = false;
    }

    if (model.isTurning()) {
      fail = true; // Fail when robot turns
    }

    // now record the new ping
    this.record();

    this.prevAngle = this.getAngle();
  }

  private Boolean changedDirection() {
    if (this.direction == null) {
      this.direction = this.getDirection();
    }
    if (this.getAngle() == this.prevAngle) {
      return false;
    }
    return this.direction != this.getDirection();
  }

  // true  = -135 -> 135
  // false = -135 <- 135
  private boolean getDirection() {
    return this.getAngle() > this.prevAngle;
  }
  private boolean gapStartFound = false;
  private int gapStartAngle;
  private int gapEndAngle;
  private boolean fail = false;
  private boolean uniqueGapFound = false;

  private void record() {
    int distance = this.getDistance();
    int angle = this.getAngle();

    if (fail) {
      return;
    }

    if (distance > MIN_GAP_DISTANCE) {
      growGap();
    } else {
      closeGap();
    }
  }

  private void growGap() {
    if (gapStartAngle == Integer.MAX_VALUE) {
      // start of gap
      gapStartAngle = getAngle();
      gapStartFound = true;
      return;
    }

    // just grow the gap, (do nothing)
  }

  private void closeGap() {
    if (!gapStartFound) {
      return;
    }
    if (fail) {
      return;
    }

    int endAngle = getAngle();
    int size = Math.abs(endAngle - gapStartAngle);

    if (size < MAX_GAP_INGORE_ANGLE) {
      // Gap is too small for the robot anyways, just ignore
      return;
    }

    if (uniqueGapFound) {
      // Another gap found, invalid data or no walls
      if (gapUnique) {
        fail = true;
      }
      return;
    }


    if (size > MIN_GAP_ANGLE && size < MAX_GAP_ANGLE) {
      // correct gap found
      uniqueGapFound = true;
      gapEndAngle = endAngle;
      gapStartFound = false;
    } else {
      // incorrect gap found (invalid data or no walls), fail
      fail = true;
    }
  }

  private int getDistance() {
    return this.model.getSensorValue(Model.S3);
  }

  private int getAngle() {
    return this.model.getSensorValue(Model.M3);// - middleTacho;
  }
}
