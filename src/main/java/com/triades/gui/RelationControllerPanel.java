package com.triades.gui;

import com.triades.model.*;
import com.triades.tools.LabelledComponent;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.LinkedList;

/**
 * Created by babcool on 5/27/17.
 */
public class RelationControllerPanel extends JPanel {

    private Relation relation;
    private VisualizationViewer vv;
    private SchemaPanel mainPanel;


    public RelationControllerPanel(Relation relation, VisualizationViewer vv, SchemaPanel mainPanel) {
        this.relation = relation;
        this.vv = vv;
        this.mainPanel = mainPanel;
        buildPanel();
    }

    private void buildPanel() {
        JPanel result = this;
        result.setLayout(new BorderLayout());
        JPanel leftLayout = new JPanel();
        leftLayout.setLayout(new BoxLayout(leftLayout, BoxLayout.Y_AXIS));
        final AutoCompleteComboBoxModel comboModel = new AutoCompleteComboBoxModel((LinkedList) null, MainFrame.getSingleton().getProgramData().getRelationInfos(), (Element) null, relation, this.vv);
        JComboBox comboBox = new JComboBox(comboModel);
        comboBox.setEditable(true);
        comboBox.setEditor(new BasicComboBoxEditor.UIResource() {
            protected JTextField createEditorComponent() {
                JTextField editor = new SchemaControllerPanel.BorderlessTextField("", 9);
                editor.setBorder((Border) null);
                return editor;
            }

            public void selectAll() {
            }
        });
        comboModel.setComboBox(comboBox);
        final JTextField comboEditor = (JTextField) comboBox.getEditor().getEditorComponent();
        comboEditor.setText(relation.getLabel());
        comboEditor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                comboModel.setSelectedItem((Object) null);
                if (comboEditor.getText().length() >= 3) {
                    comboModel.filterList(comboEditor.getText());
                }

            }

            public void removeUpdate(DocumentEvent e) {
                comboModel.setSelectedItem((Object) null);
                comboModel.filterList(comboEditor.getText());
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });
        JPanel labelAndSizePanel = new JPanel();
        labelAndSizePanel.setLayout(new BoxLayout(labelAndSizePanel, BoxLayout.X_AXIS));

        final JSlider sizeSlider = new JSlider(0, TextInfo.minimalSize, TextInfo.maximalSize, relation.getTextSize());
        sizeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                relation.setTextSize(sizeSlider.getValue());
                vv.repaint();
            }
        });
        labelAndSizePanel.add(new LabelledComponent("Etiquette", comboBox, true));
        labelAndSizePanel.add(new LabelledComponent("Taille", sizeSlider, true));

        leftLayout.add(labelAndSizePanel);
        comboEditor.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                comboModel.setSelectedItem(comboEditor.getText());
            }
        });
        comboBox.setPreferredSize(new Dimension(300, 20));
        JButton delete = new JButton("Supprimer l'élément");
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(mainPanel, "Etes-vous sur de vouloir supprimer cette relation ?") == 0) {
                    mainPanel.removeRelation(relation);
                }

            }
        });
        JButton color = new JButton("Couleur");
        final JLabel colorLabel = new JLabel("   ");
        colorLabel.setOpaque(true);
        colorLabel.setBackground(relation.getColor());
        colorLabel.setBorder(BorderFactory.createBevelBorder(0, relation.getColor().brighter(), relation.getColor().darker()));
        color.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(mainPanel, "Couleur de l'arête", relation.getColor());
                if (newColor != null) {
                    relation.setColor(newColor);
                    colorLabel.setBorder(BorderFactory.createBevelBorder(0, relation.getColor().brighter(), relation.getColor().darker()));
                    colorLabel.setBackground(newColor);
                    colorLabel.repaint();
                    vv.repaint();
                }

            }
        });
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Color newColor = relation.getColor();
                colorLabel.setBorder(BorderFactory.createBevelBorder(0, newColor.brighter(), newColor.darker()));
                colorLabel.setBackground(newColor);
                colorLabel.repaint();
            }
        });
        JPanel leftSouth = new JPanel();
        leftSouth.add(Box.createGlue());
        leftSouth.setLayout(new BoxLayout(leftSouth, 0));
        leftSouth.add(delete);
        leftSouth.add(Box.createGlue());
        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new BoxLayout(colorPanel, 0));
        colorPanel.add(Box.createGlue());
        colorPanel.add(color);
        colorPanel.add(Box.createHorizontalStrut(10));
        colorPanel.add(colorLabel);
        colorPanel.add(Box.createGlue());
        leftSouth.add(colorPanel);
        leftLayout.add(leftSouth);
        leftLayout.add(Box.createHorizontalStrut(350));
        result.add(leftLayout, "Center");
        UIDefaults defaults = UIManager.getDefaults();
        defaults.put("Slider.verticalSize", new Dimension(20, 75));
        defaults.put("Slider.horizontalSize", new Dimension(75, 20));
        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, 1));
        sliderPanel.add(new JLabel("Positions :"));
        final JSlider vertical = new JSlider(1, -150, 150, relation.getLabelVerticalPosition());
        vertical.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                relation.setLabelVerticalPosition(vertical.getValue());
                vv.repaint();
            }
        });
        final JSlider horizontal = new JSlider(0, 100, (int) (100.0D * relation.getLabelHorizontalPosition()));
        horizontal.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                relation.setLabelHorizontalPosition((double) horizontal.getValue() / 100.0D);
                vv.repaint();
            }
        });
        Dimension prefSize = new Dimension(150, 40);
        sliderPanel.add(horizontal);
        horizontal.setPreferredSize(prefSize);
        vertical.setPreferredSize(new Dimension(40, 100));
        JPanel sliderPanel2 = new JPanel();
        sliderPanel2.setLayout(new BoxLayout(sliderPanel2, 0));
        sliderPanel2.add(sliderPanel);
        sliderPanel2.add(vertical);
        result.add(sliderPanel2, "East");
        result.setBorder(BorderFactory.createTitledBorder("Edition de la relation"));
    }
}
