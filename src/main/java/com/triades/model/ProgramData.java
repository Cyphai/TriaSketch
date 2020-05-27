package com.triades.model;

import com.google.gson.Gson;
import com.triades.gui.MainFrame;


import javax.swing.*;
import java.io.*;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;

public class ProgramData {
   private LinkedList<File> precedentFiles = new LinkedList<File>();
   private LinkedList<ElementInfo> elementsInfos = new LinkedList<ElementInfo>();
   private LinkedList<RelationInfo> relationInfos = new LinkedList<RelationInfo>();
   private boolean elementAddedToFirstStep = false;
   private boolean highlightVertices = true;

   public void addFile(File newFile) {
      if (this.precedentFiles.isEmpty() || !(this.precedentFiles.element()).equals(newFile)) {
         this.precedentFiles.removeFirstOccurrence(newFile);
         this.precedentFiles.addFirst(newFile);
      }
   }

   public LinkedList<File> getPrecedentFiles() {
      return this.precedentFiles;
   }

   public LinkedList<ElementInfo> getElementsInfos() {
      return this.elementsInfos;
   }

   public LinkedList<RelationInfo> getRelationInfos() {
      return this.relationInfos;
   }

   public void addRelationInfo(RelationInfo newInfo) {
      if (!this.relationInfos.contains(newInfo)) {
         this.relationInfos.add(newInfo);
         Collections.sort(this.relationInfos, new Comparator<TextInfo>() {
            public int compare(TextInfo o1, TextInfo o2) {
               return Collator.getInstance().compare(o1.getText(), o2.getText());
            }
         });
      }
   }

   public void addElementInfo(ElementInfo newInfo) {
      if (!this.elementsInfos.contains(newInfo)) {
         this.elementsInfos.add(newInfo);
         Collections.sort(this.elementsInfos, new Comparator<TextInfo>() {
            public int compare(TextInfo o1, TextInfo o2) {
               return Collator.getInstance().compare(o1.getText(), o2.getText());
            }
         });
      }
   }

   public static ProgramData loadProgramData() {
      File file = new File("TriaSketchData.dta");
      if (!file.exists()) {
         MainFrame.showLicenceDialog();
         return new ProgramData();
      } else {
         ProgramData result = null;

         try {
            FileReader fileReader = new FileReader(file);
            Gson gson = new Gson();
            result = gson.fromJson(fileReader, ProgramData.class);
            fileReader.close();
         } catch (IOException var4) {
            JOptionPane.showMessageDialog(MainFrame.getSingleton(), "Erreur durant l'ouverture du fichier de données de programme :\n" + var4, "Erreur", 0);
            var4.printStackTrace();
            return new ProgramData();
         }

         if (result == null) {
            JOptionPane.showMessageDialog(MainFrame.getSingleton(), "Erreur inconnue durant l'ouverture du fichier de données de programme", "Erreur", 0);
            result = new ProgramData();
         }

         result.checkFields();
         result.updateLowerCaseText();
         return result;
      }
   }

   public void saveProgramData() {
      while(this.elementsInfos.size() > 500) {
         this.elementsInfos.pollFirst();
      }

      while(this.relationInfos.size() > 500) {
         this.relationInfos.pollFirst();
      }

      File file = new File("TriaSketchData.dta");

      try {
         FileWriter writer = new FileWriter(file);
         Gson gson = new Gson();
         gson.toJson((Object)this, (Appendable)writer);
         writer.close();
      } catch (IOException error) {
         JOptionPane.showMessageDialog(MainFrame.getSingleton(), "Erreur durant l'enregistrement du fichier de données de programme", "Erreur", 0);
         error.printStackTrace();
      }

   }

   protected void updateLowerCaseText() {
      LinkedList newElements = new LinkedList();
      Iterator iterInfos = this.elementsInfos.iterator();

      while(iterInfos.hasNext()) {
         ElementInfo e = (ElementInfo)iterInfos.next();
         if (e.getText().length() > 0) {
            newElements.add(e);
         }
      }

      this.elementsInfos = newElements;
      LinkedList newRelations = new LinkedList();
      Iterator var4 = this.relationInfos.iterator();

      RelationInfo e;
      while(var4.hasNext()) {
         e = (RelationInfo)var4.next();
         if (e.getText().length() > 0) {
            newRelations.add(e);
         }
      }

      this.relationInfos = newRelations;
      var4 = this.elementsInfos.iterator();

      while(var4.hasNext()) {
         ElementInfo info = (ElementInfo)var4.next();
         info.updateLowerCase();
      }

      var4 = this.relationInfos.iterator();

      while(var4.hasNext()) {
         e = (RelationInfo)var4.next();
         e.updateLowerCase();
      }

   }

   protected void checkFields() {
      if (this.elementsInfos == null) {
         this.elementsInfos = new LinkedList();
         this.highlightVertices = true;
      }

      if (this.relationInfos == null) {
         this.relationInfos = new LinkedList();
      }

   }

   public boolean isElementAddedToFirstStep() {
      return this.elementAddedToFirstStep;
   }

   public void setElementAddedToFirstStep(boolean newValue) {
      this.elementAddedToFirstStep = newValue;
   }

   public boolean isHighlightVertices() {
      return this.highlightVertices;
   }

   public void setHighlightVertices(boolean highlightVertices) {
      this.highlightVertices = highlightVertices;
   }

   public void checkSchemaItems(Schema loadedSchema) {
      Iterator var3 = loadedSchema.getElements().iterator();

      while(true) {
         Element element;
         boolean ok;
         Iterator var6;
         do {
            do {
               if (!var3.hasNext()) {
                  var3 = loadedSchema.getRelations().iterator();

                  while(true) {
                     Relation relation;
                     do {
                        if (!var3.hasNext()) {
                           return;
                        }

                        relation = (Relation)var3.next();
                     } while(relation.getLabel().length() <= 0);

                     ok = false;
                     var6 = this.relationInfos.iterator();

                     while(var6.hasNext()) {
                        RelationInfo rI = (RelationInfo)var6.next();
                        if (rI.getText().equalsIgnoreCase(relation.getLabel())) {
                           ok = true;
                           break;
                        }
                     }

                     if (!ok) {
                        this.relationInfos.add(new RelationInfo(relation.getLabel(), relation.getTextSize(), relation.getColor()));
                     }
                  }
               }

               element = (Element)var3.next();
            } while(element.getName().length() <= 0);
         } while(element.equals("Nouvel élément"));

         ok = false;
         var6 = this.elementsInfos.iterator();

         while(var6.hasNext()) {
            ElementInfo eI = (ElementInfo)var6.next();
            if (eI.getText().equalsIgnoreCase(element.getName())) {
               ok = true;
               break;
            }
         }

         if (!ok) {
            this.elementsInfos.add(new ElementInfo(element.getName(), element.getTextSize(), element.getPictureName()));
         }
      }
   }
}
