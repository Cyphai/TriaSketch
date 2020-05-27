package com.triades.tools;

import javax.swing.*;

public class LabelledComponent extends JPanel {
   private static final long serialVersionUID = 1L;
   protected JComponent component;
   protected JLabel label;

   public LabelledComponent(String text, JComponent component, boolean roundWithGlue) {
      this.setLayout(new BoxLayout(this, 0));
      if (roundWithGlue) {
         this.add(Box.createGlue());
      }

      this.label = new JLabel(text);
      this.label.setHorizontalAlignment(4);
      this.add(this.label);
      this.add(Box.createHorizontalStrut(5));
      this.component = component;
      this.add(component);
      if (roundWithGlue) {
         this.add(Box.createGlue());
      }

      this.label.setLabelFor(component);
   }

   public JComponent getComponent() {
      return this.component;
   }

   public void setComponent(JComponent component) {
      this.component = component;
   }

   public JLabel getLabel() {
      return this.label;
   }

   public void setLabel(JLabel label) {
      this.label = label;
   }
}
