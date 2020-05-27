package com.triades.model;

public class SchemaData {
   private String name;
   private int nextElementId;
   private int nextRelationId;
   private int stepMax;

   public SchemaData(String name) {
      this.name = name;
      this.nextElementId = 0;
      this.nextRelationId = 0;
   }

   public int getNextElementId() {
      return this.nextElementId++;
   }

   public int getNextRelationId() {
      return this.nextRelationId++;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setStepMax(int newMax) {
      this.stepMax = newMax;
   }

   public int getStepMax() {
      return this.stepMax;
   }
}
