package com.triades.model;

import java.awt.*;

public class Relation {
   private final Integer source;
   private final Integer destination;
   private Integer id;
   private String label;
   private Color color;
   private int textSize;
   private int labelVerticalPosition;
   private double labelHorizontalPosition;
   private static final Color defaultColor;
   private transient RelationInfo baseInfo;

   static {
      defaultColor = Color.BLACK;
   }

   public Relation(Integer id, Integer source, Integer destination) {
      this.id = id;
      this.source = source;
      this.destination = destination;
      this.label = "";
      this.color = defaultColor;
      this.labelVerticalPosition = -25;
      this.labelHorizontalPosition = 0.5D;
      this.textSize = TextInfo.defaultTextSize;
   }

   public Integer getSource() {
      return this.source;
   }

   public Integer getDestination() {
      return this.destination;
   }

   public Integer getId() {
      return this.id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getLabel() {
      return this.label;
   }

   public void setLabel(String label) {
      this.label = label;
   }

   public void setColor(Color color) {
      this.color = color;
      if (this.baseInfo != null) {
         this.baseInfo.color = color;
      }

   }

   public int getTextSize() {
      if (textSize == 0) {
         textSize = TextInfo.defaultTextSize;
      }
      return textSize;
   }

   public void setTextSize(int textSize) {
      this.textSize = textSize;
      if (baseInfo != null) {
         baseInfo.setTextSize(textSize);
      }
   }

   public Color getColor() {
      return this.color;
   }

   public int getLabelVerticalPosition() {
      return this.labelVerticalPosition;
   }

   public void setLabelVerticalPosition(int labelVerticalPosition) {
      this.labelVerticalPosition = labelVerticalPosition;
   }

   public double getLabelHorizontalPosition() {
      return this.labelHorizontalPosition;
   }

   public void setLabelHorizontalPosition(double labelHoritontalPosition) {
      this.labelHorizontalPosition = labelHoritontalPosition;
   }

   @Override
   public int hashCode() {
      return id != null ? id.hashCode() : 0;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!(obj instanceof Relation)) {
         return false;
      } else {
         Relation other = (Relation)obj;
         if (this.id == null) {
            if (other.id != null) {
               return false;
            }
         } else if (!this.id.equals(other.id)) {
            return false;
         }

         return true;
      }
   }

   public RelationInfo getBaseInfo() {
      return this.baseInfo;
   }

   public void setBaseInfo(RelationInfo baseInfo) {
      this.baseInfo = baseInfo;
      if (baseInfo != null) {
         if (baseInfo.color == null) {
            baseInfo.color = defaultColor;
         }

         this.color = baseInfo.color;
         this.label = baseInfo.getText();
         this.textSize = baseInfo.getTextSize();
      } else {
         this.setLabel("");
      }

   }
}
