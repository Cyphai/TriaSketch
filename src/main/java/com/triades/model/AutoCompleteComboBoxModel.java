package com.triades.model;

import com.triades.gui.MainFrame;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import javax.swing.*;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public class AutoCompleteComboBoxModel extends AbstractListModel implements ComboBoxModel {
   private static final long serialVersionUID = 1L;
   private String lastTestedString;
   private ArrayList currentList;
   private LinkedList baseList;
   private TextInfo selectedItem = null;
   private String currentEditedString = null;
   private Element currentElement;
   private Relation currentRelation;
   private JComboBox comboBox;
   private VisualizationViewer vv;

   public AutoCompleteComboBoxModel(LinkedList baseElementList, LinkedList baseRelationList, Element currentElement, Relation currentRelation, VisualizationViewer vv) {
      if (baseElementList != null) {
         this.baseList = new LinkedList(baseElementList);
      } else {
         this.baseList = new LinkedList(baseRelationList);
      }

      this.currentElement = currentElement;
      this.currentRelation = currentRelation;
      this.currentList = new ArrayList(this.baseList);
      this.currentList = null;
      this.lastTestedString = null;
      this.comboBox = null;
      this.vv = vv;
   }

   public void setComboBox(JComboBox comboBox) {
      this.comboBox = comboBox;
   }

   public int getSize() {
      return this.currentList != null ? this.currentList.size() : this.baseList.size();
   }

   public Object getElementAt(int index) {
      if (this.currentList != null) {
         return index < this.currentList.size() ? this.currentList.get(index) : this.currentEditedString;
      } else {
         return this.baseList.get(index);
      }
   }

   public void setSelectedItem(Object anItem) {
      if (anItem instanceof String) {
         String s = (String)anItem;
         if (this.currentElement != null && this.currentElement.getName().equals(s)) {
            return;
         }

         if (this.currentRelation != null && this.currentRelation.getLabel().equals(s)) {
            return;
         }
      }

      if (anItem instanceof ElementInfo) {
         this.selectedItem = (ElementInfo)anItem;
         if (this.currentElement != null) {
            this.currentElement.setBaseInfo((ElementInfo)this.selectedItem);
         }
      } else if (anItem instanceof RelationInfo) {
         this.selectedItem = (RelationInfo)anItem;
         if (this.currentRelation != null) {
            this.currentRelation.setBaseInfo((RelationInfo)this.selectedItem);
         }
      } else if (anItem instanceof String) {
         if (this.currentElement != null) {
            if (this.currentElement.getBaseInfo() == null) {
               if (!anItem.toString().equals("Nouvel élément") && anItem.toString().length() > 0) {
                  ElementInfo newInfo = new ElementInfo(anItem.toString(), this.currentElement.getTextSize(), this.currentElement.getPictureName());
                  this.currentElement.setBaseInfo(newInfo);
                  MainFrame.getSingleton().getProgramData().addElementInfo(newInfo);
               }
            } else {
               if (anItem.toString().length() > 0) {
                  this.currentElement.getBaseInfo().setText(anItem.toString());
                  this.currentElement.setName(anItem.toString());
               } else {
                  this.currentElement.setBaseInfo((ElementInfo)null);
               }

               this.vv.repaint();
            }
         } else if (this.currentRelation != null) {
            if (this.currentRelation.getBaseInfo() == null) {
               if (anItem.toString().length() > 0) {
                  RelationInfo newInfo = new RelationInfo(anItem.toString(), this.currentRelation.getTextSize(), this.currentRelation.getColor());
                  this.currentRelation.setBaseInfo(newInfo);
                  MainFrame.getSingleton().getProgramData().addRelationInfo(newInfo);
               }
            } else {
               if (anItem.toString().length() > 0) {
                  this.currentRelation.getBaseInfo().setText(anItem.toString());
                  this.currentRelation.setLabel(anItem.toString());
               } else {
                  this.currentRelation.setBaseInfo((RelationInfo)null);
               }

               this.vv.repaint();
            }
         }
      } else if (anItem != null) {
         System.err.println("AutoCompleteComboBox::setSelected Ukow class type : " + anItem.getClass());
      }

      this.vv.repaint();
   }

   public Object getSelectedItem() {
      return this.selectedItem != null ? this.selectedItem : this.currentEditedString;
   }

   public void filterList(String newPrefix) {
      if (this.selectedItem == null) {
         this.currentEditedString = newPrefix;
         if (newPrefix.equals("Nouvel élément")) {
            newPrefix = "";
         }

         if (this.lastTestedString != null && !newPrefix.contains(this.lastTestedString)) {
            this.currentList = null;
            this.comboBox.hidePopup();
         }

         this.lastTestedString = newPrefix;
         if (newPrefix.length() < 3) {
            this.currentList = new ArrayList(this.baseList);
            this.setSelectedItem((Object)null);
            this.fireContentsChanged(this, 0, this.getSize() - 1);
            this.comboBox.hidePopup();
            this.comboBox.showPopup();
         } else {
            newPrefix = Normalizer.normalize(newPrefix.toLowerCase(), Form.NFD).replaceAll("[̀-ͯ]", "");
            if (this.currentList == null) {
               this.currentList = new ArrayList(this.baseList);
            }

            ArrayList newList = new ArrayList();
            Iterator var4 = this.currentList.iterator();

            while(var4.hasNext()) {
               TextInfo tI = (TextInfo)var4.next();
               if (tI != null && tI.lowerCaseText != null && tI.lowerCaseText.indexOf(newPrefix) != -1) {
                  newList.add(tI);
               }
            }

            this.currentList = newList;
            this.setSelectedItem((Object)null);
            this.fireContentsChanged(this, 0, this.getSize() - 1);
            if (this.comboBox != null) {
               if (this.currentList.size() > 0) {
                  this.comboBox.showPopup();
               } else {
                  this.comboBox.hidePopup();
               }
            }

         }
      }
   }
}
