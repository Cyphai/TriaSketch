package com.triades.model;

import java.util.ArrayList;
import java.util.Iterator;

public class Schema {
   private ArrayList slides;
   private ArrayList<Element> elements;
   private ArrayList<Relation> relations;
   private SchemaData datas;

   public Schema() {
      this("Nouveau schéma");
   }

   public Schema(String title) {
      this.slides = new ArrayList();
      this.elements = new ArrayList<Element>();
      this.relations = new ArrayList<Relation>();
      this.datas = new SchemaData(title);
   }

   public void addElement(Element newElement) {
      if (!this.elements.contains(newElement)) {
         this.elements.add(newElement);
      }
   }

   public void removeElement(Element oldElement) {
      this.elements.remove(oldElement);
   }

   public void addRelation(Relation newRelation) {
      if (!this.relations.contains(newRelation)) {
         this.relations.add(newRelation);
      }

   }

   public void removeRelation(Relation oldRelation) {
      this.relations.remove(oldRelation);
   }

   public ArrayList getSlides() {
      return this.slides;
   }

   public void setSlides(ArrayList slides) {
      this.slides = slides;
   }

   public ArrayList getElements() {
      return this.elements;
   }

   public void setElements(ArrayList elements) {
      this.elements = elements;
   }

   public ArrayList getRelations() {
      return this.relations;
   }

   public void setRelations(ArrayList relations) {
      this.relations = relations;
   }

   public SchemaData getDatas() {
      return this.datas;
   }

   public void setDatas(SchemaData datas) {
      this.datas = datas;
   }

   public boolean checkMax() {
      int newMax = 0;

      Element e;
      for(Iterator var3 = this.elements.iterator(); var3.hasNext(); newMax = Math.max(newMax, e.getApparitionStep())) {
         e = (Element)var3.next();
      }

      if (newMax != this.datas.getStepMax()) {
         this.datas.setStepMax(newMax);
         return true;
      } else {
         return false;
      }
   }

   public void addStepAfter(int step) {
      Iterator var3 = this.elements.iterator();

      while(var3.hasNext()) {
         Element e = (Element)var3.next();
         if (e.getApparitionStep() > step) {
            e.setApparitionStep(e.getApparitionStep() + 1);
         }
      }

      this.datas.setStepMax(this.datas.getStepMax() + 1);
   }

   public void removeStep(int step) {
      Element e;
      Iterator var3;
      if (step < this.datas.getStepMax()) {
         var3 = this.elements.iterator();

         while(var3.hasNext()) {
            e = (Element)var3.next();
            if (e.getApparitionStep() > step) {
               e.setApparitionStep(e.getApparitionStep() - 1);
            }
         }
      } else {
         var3 = this.elements.iterator();

         while(var3.hasNext()) {
            e = (Element)var3.next();
            if (e.getApparitionStep() == step) {
               e.setApparitionStep(e.getApparitionStep() - 1);
            }
         }
      }

      this.checkMax();
   }

   public void cleanStep() {
      int i = 0;

      while(i <= this.datas.getStepMax()) {
         boolean ok = false;
         Iterator var4 = this.elements.iterator();

         while(var4.hasNext()) {
            Element e = (Element)var4.next();
            if (e.getApparitionStep() == i) {
               ok = true;
               break;
            }
         }

         if (!ok) {
            this.removeStep(i);
         } else {
            ++i;
         }
      }

   }

   public int countUsefulSteps() {
      int result = 0;

      for(int i = 0; i <= this.datas.getStepMax(); ++i) {
         boolean ok = false;
         Iterator var5 = this.elements.iterator();

         while(var5.hasNext()) {
            Element e = (Element)var5.next();
            if (e.getApparitionStep() == i) {
               ok = true;
               break;
            }
         }

         if (ok) {
            ++result;
         }
      }

      System.out.println("Schema.countUsefulSteps() : nombre d'étape " + result);
      return result;
   }
}
