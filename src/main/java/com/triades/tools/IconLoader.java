package com.triades.tools;

import com.triades.gui.MainFrame;

import javax.swing.*;
import java.util.HashMap;
import java.util.Vector;

public class IconLoader {
   protected static Vector<ImageIcon> bigPictureList;
   protected static HashMap<String, ImageIcon> tinyIcon;
   protected static HashMap<String, ImageIcon> smallIcon;
   protected static HashMap<String, ImageIcon> mediumIcon;
   protected static HashMap<String, ImageIcon> bigIcon;

   public static void initializeHashMap() {
      tinyIcon = new HashMap<String, ImageIcon>();
      smallIcon = new HashMap<String, ImageIcon>();
      mediumIcon = new HashMap<String, ImageIcon>();
      bigIcon = new HashMap<String, ImageIcon>();
      bigPictureList = loadBigPictureList();
   }

   public static IconLoader.NamedImageIcon getTinyIcon(String name) {
      if (tinyIcon == null) {
         initializeHashMap();
      }

      IconLoader.NamedImageIcon result = (IconLoader.NamedImageIcon)tinyIcon.get(name);
      if (result == null) {
         try {
            result = new IconLoader.NamedImageIcon(16, name);
            tinyIcon.put(name, result);
         } catch (RuntimeException var3) {
            JOptionPane.showMessageDialog(MainFrame.getSingleton(), "Erreur lors du chargement de l'icone /Icones/16x16/" + name + " : \n" + var3.getMessage());
            var3.printStackTrace();
            return null;
         }
      }

      return result;
   }

   public static IconLoader.NamedImageIcon getSmallIcon(String name) {
      if (smallIcon == null) {
         initializeHashMap();
      }

      IconLoader.NamedImageIcon result = (IconLoader.NamedImageIcon)smallIcon.get(name);
      if (result == null) {
         try {
            result = new IconLoader.NamedImageIcon(22, name);
            smallIcon.put(name, result);
         } catch (Exception var3) {
            JOptionPane.showMessageDialog(MainFrame.getSingleton(), "Erreur lors du chargement de l'icone /Icones/22x22/" + name + " : \n" + var3.getMessage());
            var3.printStackTrace();
            return null;
         }
      }

      return result;
   }

   public static IconLoader.NamedImageIcon getMediumIcon(String name) {
      if (mediumIcon == null) {
         initializeHashMap();
      }

      IconLoader.NamedImageIcon result = (IconLoader.NamedImageIcon)mediumIcon.get(name);
      if (result == null) {
         try {
            result = new IconLoader.NamedImageIcon(32, name);
            mediumIcon.put(name, result);
         } catch (Exception var3) {
            JOptionPane.showMessageDialog(MainFrame.getSingleton(), "Erreur lors du chargement de l'icone /Icones/32x32/" + name + " : \n" + var3.getMessage());
            var3.printStackTrace();
            return null;
         }
      }

      return result;
   }

   public static IconLoader.NamedImageIcon getBigIcon(String name) {
      if (bigIcon == null) {
         initializeHashMap();
      }

      IconLoader.NamedImageIcon result = (IconLoader.NamedImageIcon)bigIcon.get(name);
      if (result == null) {
         try {
            result = new IconLoader.NamedImageIcon(48, name);
            bigIcon.put(name, result);
         } catch (Exception var3) {
            JOptionPane.showMessageDialog(MainFrame.getSingleton(), "Erreur lors du chargement de l'icone /Icones/48x48/" + name + " : \n" + var3.getMessage());
            var3.printStackTrace();
            return null;
         }
      }

      return result;
   }

   public static Vector<ImageIcon> getBigPictureList() {
      return bigPictureList;
   }

   protected static Vector<ImageIcon> loadBigPictureList() {
      Vector<ImageIcon> result = new Vector<ImageIcon>();
      result.add(getBigIcon("acteurHumainBleu"));
      result.add(getBigIcon("acteurHumainJaune"));
      result.add(getBigIcon("acteurHumainMarron"));
      result.add(getBigIcon("acteurHumainNoir"));
      result.add(getBigIcon("acteurHumainRouge"));
      result.add(getBigIcon("acteurHumainVert"));
      result.add(getBigIcon("acteurHumainViolet"));
      result.add(getBigIcon("activite"));
      result.add(getBigIcon("compas"));
      result.add(getBigIcon("contact"));
      result.add(getBigIcon("contrat"));
      result.add(getBigIcon("flecheVersExterieur"));
      result.add(getBigIcon("group"));
      result.add(getBigIcon("idee"));
      result.add(getBigIcon("methode"));
      result.add(getBigIcon("moyenGenerique"));
      result.add(getBigIcon("note"));
      result.add(getBigIcon("outils"));
      result.add(getBigIcon("processus"));
      result.add(getBigIcon("resultat"));
      result.add(getBigIcon("reunion"));
      result.add(getBigIcon("stock"));
      result.add(getBigIcon("Triade"));
      return result;
   }

   public static class NamedImageIcon extends ImageIcon {
      private static final long serialVersionUID = 1L;
      private String name;
      private int size;

      public NamedImageIcon(int size, String name) {
         super(ImageIcon.class.getResource("/Icones/" + size + "x" + size + "/" + name + ".png"));
         this.size = size;
         this.name = name;
      }

      public String getName() {
         return this.name;
      }

      public int getSize() {
         return this.size;
      }

      public boolean equals(Object other) {
         return super.equals(other);
      }
   }
}
