package com.triades.model;

import java.awt.*;

public class RelationInfo extends TextInfo {
   public Color color;

   public RelationInfo(String text, int textSize, Color color) {
      super(text, textSize);
      this.color = color;
   }
}
