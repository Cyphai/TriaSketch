package com.triades.gui;


import com.triades.model.Element;
import com.triades.model.Relation;
import com.triades.model.Schema;

import com.triades.tools.LabelledComponent;
import edu.uci.ics.jung.visualization.VisualizationViewer;


import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxEditor.UIResource;
import java.awt.*;
import java.awt.event.*;


public class SchemaControllerPanel extends JPanel {
   private static final long serialVersionUID = 1L;
   private Schema schema;
   private VisualizationViewer<Element, Relation> vv;
   private SchemaPanel mainPanel;
   private JPanel emptyPanel;
   private JPanel contentPanel;
   private JLabel stepLabel;

   public SchemaControllerPanel(Schema schema, VisualizationViewer<Element, Relation> vv, SchemaPanel mainPanel) {
      this.schema = schema;
      this.vv = vv;
      this.mainPanel = mainPanel;
      this.buildPanel();
   }

   protected void buildPanel() {
      this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
      this.emptyPanel = new JPanel();
      this.emptyPanel.setLayout(new BoxLayout(this.emptyPanel, BoxLayout.X_AXIS));
      this.emptyPanel.add(Box.createRigidArea(new Dimension(600, 50)));
      JPanel titlePanel = new JPanel();
      titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
      final JTextField titleField = new JTextField(this.schema.getDatas().getName());
      titleField.addCaretListener(new CaretListener() {
         public void caretUpdate(CaretEvent e) {
            SchemaControllerPanel.this.updateTitle(titleField.getText());
         }
      });
      titlePanel.add(Box.createGlue());
      titlePanel.add(new LabelledComponent("Titre du schéma", titleField, false));
      titlePanel.add(this.buildStepControlPanel());
      this.contentPanel = new JPanel();
      this.contentPanel.setLayout(new BorderLayout());
      this.contentPanel.add(this.emptyPanel, "Center");
      this.add(titlePanel);
      this.add(Box.createVerticalStrut(75));
      this.add(this.contentPanel);
      this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5), BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(0), BorderFactory.createEmptyBorder(5, 5, 5, 5))));
   }

   public void showElement(Element element) {
      this.showInContentPanel(this.buildElementPanel(element));
   }

   private JPanel buildElementPanel(final Element element) {
      return new ElementControllerPanel(element, vv, mainPanel, this);
   }

   public void showRelation(Relation r) {
      this.showInContentPanel(this.buildRelationPanel(r));
   }

   private JPanel buildRelationPanel(final Relation relation) {
      return new RelationControllerPanel(relation, vv, mainPanel);
   }

   public void removePopup() {
      this.showInContentPanel(this.emptyPanel);
   }

   private void showInContentPanel(JPanel panel) {
      this.contentPanel.removeAll();
      this.contentPanel.add(panel, "Center");
      this.contentPanel.revalidate();
      this.contentPanel.repaint();
   }

   protected JPanel buildStepControlPanel() {
      this.stepLabel = new JLabel("Blablablabla");
      this.updateStepLabel();
      JPanel result = new JPanel();
      result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
      JButton firstStep = new JButton("<<");
      firstStep.setToolTipText("Première étape");
      firstStep.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SchemaControllerPanel.this.mainPanel.setCurrentStep(0);
            SchemaControllerPanel.this.updateStepLabel();
            SchemaControllerPanel.this.vv.repaint();
         }
      });
      JButton previousStep = new JButton("<");
      previousStep.setToolTipText("Etape précédente");
      previousStep.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (SchemaControllerPanel.this.mainPanel.getCurrentStep() > 0) {
               SchemaControllerPanel.this.mainPanel.setCurrentStep(SchemaControllerPanel.this.mainPanel.getCurrentStep() - 1);
               SchemaControllerPanel.this.updateStepLabel();
               SchemaControllerPanel.this.vv.repaint();
            } else {
               SchemaControllerPanel.this.mainPanel.setCurrentStep(SchemaControllerPanel.this.schema.getDatas().getStepMax() + 1);
               SchemaControllerPanel.this.updateStepLabel();
               SchemaControllerPanel.this.vv.repaint();
            }

         }
      });
      JButton nextStep = new JButton(">");
      nextStep.setToolTipText("Etape suivante");
      nextStep.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (SchemaControllerPanel.this.mainPanel.getCurrentStep() < SchemaControllerPanel.this.mainPanel.getSchema().getDatas().getStepMax() + 1) {
               SchemaControllerPanel.this.mainPanel.setCurrentStep(SchemaControllerPanel.this.mainPanel.getCurrentStep() + 1);
               SchemaControllerPanel.this.updateStepLabel();
               SchemaControllerPanel.this.vv.repaint();
            } else {
               SchemaControllerPanel.this.mainPanel.setCurrentStep(0);
               SchemaControllerPanel.this.updateStepLabel();
               SchemaControllerPanel.this.vv.repaint();
            }

         }
      });
      JButton lastStep = new JButton(">>");
      lastStep.setToolTipText("Dernière étape");
      lastStep.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SchemaControllerPanel.this.mainPanel.setCurrentStep(SchemaControllerPanel.this.schema.getDatas().getStepMax() + 1);
            SchemaControllerPanel.this.updateStepLabel();
            SchemaControllerPanel.this.vv.repaint();
         }
      });
      JButton addStep = new JButton("+");
      addStep.setToolTipText("Ajoute une étape après l'étape courante en décalant les étapes suivantes");
      addStep.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SchemaControllerPanel.this.schema.addStepAfter(SchemaControllerPanel.this.mainPanel.getCurrentStep());
            SchemaControllerPanel.this.mainPanel.setCurrentStep(SchemaControllerPanel.this.mainPanel.getCurrentStep() + 1);
            SchemaControllerPanel.this.updateStepLabel();
         }
      });
      JButton removeStep = new JButton("-");
      removeStep.setToolTipText("Supprime l'étape courante, les sommets apparaissant à l'étape suivante\napparaîtront dès cette étape");
      removeStep.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SchemaControllerPanel.this.schema.removeStep(SchemaControllerPanel.this.mainPanel.getCurrentStep());
            if (SchemaControllerPanel.this.mainPanel.getCurrentStep() > SchemaControllerPanel.this.schema.getDatas().getStepMax()) {
               SchemaControllerPanel.this.mainPanel.setCurrentStep(SchemaControllerPanel.this.schema.getDatas().getStepMax());
            }

            SchemaControllerPanel.this.updateStepLabel();
            SchemaControllerPanel.this.vv.repaint();
         }
      });
      result.add(Box.createGlue());
      result.add(firstStep);
      result.add(Box.createHorizontalStrut(10));
      result.add(previousStep);
      result.add(Box.createHorizontalStrut(25));
      result.add(this.stepLabel);
      result.add(Box.createHorizontalStrut(25));
      result.add(nextStep);
      result.add(Box.createHorizontalStrut(10));
      result.add(lastStep);
      result.add(Box.createGlue());
      result.add(addStep);
      result.add(removeStep);
      result.add(Box.createGlue());
      return result;
   }

   public void updateStepLabel() {
      if (this.stepLabel != null) {
         if (this.mainPanel.getCurrentStep() <= this.schema.getDatas().getStepMax()) {
            this.stepLabel.setText(" Etape : " + (this.mainPanel.getCurrentStep() + 1) + " / " + (this.schema.getDatas().getStepMax() + 1) + " ");
         } else {
            this.stepLabel.setText("Schéma complet");
         }

         this.stepLabel.repaint();
      }

      this.mainPanel.updateTitle(null);
   }

   protected void updateTitle(String newTitle) {
      this.schema.getDatas().setName(newTitle);
      MainFrame.getSingleton().updateTitle(newTitle);
      this.mainPanel.updateTitle(newTitle);
   }





   static class BorderlessTextField extends JTextField {
      private static final long serialVersionUID = 1L;

      public BorderlessTextField(String value, int n) {
         super(value, n);
      }

      public void setText(String s) {
         if (!this.getText().equals(s)) {
            super.setText(s);
         }
      }

      public void setBorder(Border b) {
         if (!(b instanceof UIResource)) {
            super.setBorder(b);
         }

      }

      public void selectAll() {
      }
   }
}
