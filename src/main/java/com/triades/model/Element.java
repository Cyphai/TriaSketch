package com.triades.model;

import com.triades.tools.IconLoader;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;


import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.Point;

import static com.triades.model.TextInfo.defaultTextSize;

public class Element {
   private String name;
   private Integer id;
   private int apparitionStep;
   private String pictureName;
   private transient IconLoader.NamedImageIcon picture;
   private int textSize;
   private Position labelPosition;
   private Point2D.Double position;
   private static final String defaultPictureName = "acteurHumainBleu";
   public static final String defaultName = "Nouvel élément";
   private transient ElementInfo baseInfo;

   public Element(Integer id, Point2D.Double position, int apparitionStep) {
      this.id = id;
      this.name = "Nouvel élément";
      this.pictureName = "acteurHumainBleu";
      this.labelPosition = Position.S;
      this.position = position;
      this.apparitionStep = apparitionStep;
      this.textSize = defaultTextSize;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Integer getId() {
      return this.id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public String getPictureName() {
      return this.pictureName;
   }

   public void setPictureName(String pictureName) {
      this.pictureName = pictureName;
      if (this.baseInfo != null) {
         this.baseInfo.pictureName = pictureName;
      }

      this.picture = IconLoader.getBigIcon(pictureName);
   }

   public ElementInfo getBaseInfo() {
      return this.baseInfo;
   }

   public void setBaseInfo(ElementInfo baseInfo) {
      this.baseInfo = baseInfo;
      if (baseInfo != null) {
         if (baseInfo.pictureName == null) {
            baseInfo.pictureName = "acteurHumainBleu";
         }

         pictureName = baseInfo.pictureName;
         name = baseInfo.getText();
         textSize = baseInfo.getTextSize();
      } else {
         this.setName("");
         textSize = defaultTextSize;
      }

   }

   public IconLoader.NamedImageIcon getPicture() {
      if (this.picture == null) {
         this.picture = IconLoader.getBigIcon(this.pictureName);
      }

      return this.picture;
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

   public Position getLabelPosition() {
      return this.labelPosition;
   }

   public void setLabelPosition(Position labelPosition) {
      this.labelPosition = labelPosition;
   }

   @Override
   public int hashCode() {
      return id != null ? id.hashCode() : 0;
   }

   public Point2D.Double getPosition() {
      return this.position;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (!(obj instanceof Element)) {
         return false;
      } else {
         Element other = (Element)obj;
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

   public void setPosition(Point2D.Double newPosition) {
      this.position = newPosition;
   }

   public void setPosition(Point2D newPosition) {
      this.position.setLocation(newPosition.getX(), newPosition.getY());
   }

   public int getApparitionStep() {
      return this.apparitionStep;
   }

   public void setApparitionTransfer(int apparitiontransfer) {
      this.apparitionStep = apparitionStep;
   }
      public int getApparitionTransfer() {
      return this.apparitionTransfer;
   }

   public void setApparitionTransfer(int apparitionTransfer) {
      this.apparitionTransfer = apparitionTransfer;
   }
}
