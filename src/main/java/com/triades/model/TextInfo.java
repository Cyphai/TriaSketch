package com.triades.model;

import java.text.Normalizer;
import java.text.Normalizer.Form;

public class TextInfo {
    private String text;
   private int textSize;
   public transient String lowerCaseText;

   public static final int minimalSize = 8;
   public static final int maximalSize = 30;
   public static final int defaultTextSize = 16;

   public TextInfo(String text, int textSize) {
      this.text = text;
      this.lowerCaseText = text.toLowerCase();
      if (textSize == 0) {
         textSize = defaultTextSize;
      }
      this.textSize = textSize;
   }

   public String toString() {
      return this.text;
   }

   public int hashCode() {
      return 31 + (this.text == null ? 0 : this.text.hashCode());
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!(obj instanceof TextInfo)) {
         return false;
      } else {
         TextInfo other = (TextInfo)obj;
         if (this.text == null) {
            if (other.text != null) {
               return false;
            }
         } else if (!this.text.equalsIgnoreCase(other.text)) {
            return false;
         }

         return true;
      }
   }

   public void updateLowerCase() {
      //Supprime l'ensemble des accents pour faciliter les recherches full-text
      this.lowerCaseText = Normalizer.normalize(this.text.toLowerCase(), Form.NFD).replaceAll("[̀-ͯ]", "");
   }

   public String getText() {
      return this.text;
   }

   public int getTextSize() {
      return textSize;
   }

   public void setTextSize(int textSize) {
      this.textSize = textSize;
   }

   public void setText(String text) {
      this.text = text;
      this.updateLowerCase();
   }
}
