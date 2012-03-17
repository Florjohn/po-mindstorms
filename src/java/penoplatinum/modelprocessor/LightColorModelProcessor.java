/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package penoplatinum.modelprocessor;

import penoplatinum.util.LightColor;
import penoplatinum.Utils;
import penoplatinum.pacman.GhostModel;

/**
 *
 * @author Thomas
 */
public class LightColorModelProcessor extends ModelProcessor {

  private final int BROWN_START = 440;
  private final int BROWN_END = 470;
  private final float AVERAGE_EXPONENT = 0.001f;
  private final float AVERAGE_COLOR_EXPONENT = 0.80f;
  private final int SENSOR_VARIATION = 30;
  public static final int BLACK = -1;
  public static final int BROWN = 0;
  public static final int WHITE = 1;

  public LightColorModelProcessor() {
    super();
  }

  public LightColorModelProcessor(ModelProcessor nextProcessor) {
    super(nextProcessor);
  }
  int prevValue;
  int prevColor; // -1 = black 0 = brown 1 = white

  @Override
  protected void work() {
    GhostModel model = (GhostModel) this.model;

    int value = this.model.getSensorValue(this.model.S4);
    //Utils.Log("color: "+value);
    float averageLightValue = model.getAverageLightValue();
    if (averageLightValue < BROWN_START || averageLightValue > BROWN_END) {
      averageLightValue = (BROWN_END + BROWN_START) * 0.5f;
    }
    if (value > BROWN_START && value < BROWN_END) {
      // In the brown range
      averageLightValue = model.getAverageLightValue() * (1 - AVERAGE_EXPONENT) + value * AVERAGE_EXPONENT;
    }
    model.setAverageLightValue(averageLightValue);
    // Detect discontinuity
    if (Math.abs(value - averageLightValue) <= SENSOR_VARIATION) {
      prevColor = BROWN;
    } else if (value < averageLightValue) { // found a color difference
      // Black
      prevColor = BLACK;
    } else if (value > averageLightValue) {
      // White
      prevColor = WHITE;
    } else {
      prevColor = BROWN;
    }

    switch (prevColor) {
      case WHITE:
        model.setCurrentLightColor(LightColor.White);
        model.setAverageWhiteValue(model.getAverageWhiteValue() * (1 - AVERAGE_COLOR_EXPONENT) + value * AVERAGE_COLOR_EXPONENT);
        break;
      case BROWN:
        model.setCurrentLightColor(LightColor.Brown);
        break;
      case BLACK:
        model.setCurrentLightColor(LightColor.Black);
        model.setAverageBlackValue(model.getAverageBlackValue() * (1 - AVERAGE_COLOR_EXPONENT) + value * AVERAGE_COLOR_EXPONENT);
        break;
    }

    //Utils.Log((int)  model.getAverageBlackValue() + "," + (int) averageLightValue + "," + (int) model.getAverageWhiteValue());

//    Utils.Log(prevColor+"");
    
    prevValue = value;
  }
}
