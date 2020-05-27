package com.triades.model;

public class ElementInfo extends TextInfo {
   public String pictureName;

   public ElementInfo(String text, int textSize, String pictureName) {
      super(text, textSize);
      this.pictureName = pictureName;
   }
}
