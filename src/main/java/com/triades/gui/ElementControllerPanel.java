package com.triades.gui; 

import com.triades.model.*;
import com.triades.tools.IconLoader;
import com.triades.tools.LabelledComponent;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.renderers.Renderer;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Created by babcool on 5/27/17.
 */
public class ElementControllerPanel extends JPanel {

    private Element element;
    private VisualizationViewer<Element, Relation> vv;
    private SchemaPanel mainPanel;
    private SchemaControllerPanel mainController;

    public ElementControllerPanel(Element element, VisualizationViewer<Element, Relation> vv, SchemaPanel mainPanel, SchemaControllerPanel mainController) {
        this.element = element;
        this.vv = vv;
        this.mainPanel = mainPanel;
        this.mainController = mainController;
        buildPanel();
    }

    private void buildPanel() {
        JPanel result = this;
        result.setLayout(new BoxLayout(result, 0));
        final JTextField labelField = new JTextField(element.getName());
        labelField.addCaretListener(new CaretListener() {
            public void caretUpdate(CaretEvent e) {
                element.setName(labelField.getText());
                vv.repaint();
            }
        });
        final AutoCompleteComboBoxModel comboModel = new AutoCompleteComboBoxModel(MainFrame.getSingleton().getProgramData().getElementsInfos(), null, element, null, this.vv);
        JComboBox comboBox = new JComboBox(comboModel);
        comboBox.setEditable(true);
        comboBox.setEditor(new BasicComboBoxEditor.UIResource() {
            protected JTextField createEditorComponent() {
                JTextField editor = new SchemaControllerPanel.BorderlessTextField("", 9);
                editor.setBorder(null);
                return editor;
            }

            public void selectAll() {
            }
        });
        comboModel.setComboBox(comboBox);
        final JTextField comboEditor = (JTextField)comboBox.getEditor().getEditorComponent();
        comboEditor.setText(element.getName());
        comboEditor.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                comboModel.setSelectedItem(null);
                if (comboEditor.getText().length() >= 3) {
                    comboModel.filterList(comboEditor.getText());
                }

            }

            public void removeUpdate(DocumentEvent e) {
                comboModel.setSelectedItem(null);
                comboModel.filterList(comboEditor.getText());
            }

            public void changedUpdate(DocumentEvent e) {
            }
        });
        comboEditor.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e) {
                comboModel.setSelectedItem(comboEditor.getText());
            }

            public void focusGained(FocusEvent e) {
                if (comboEditor.getText().equals("Nouvel élément")) {
                    comboEditor.select(0, "Nouvel élément".length());
                }

            }
        });
        JButton delete = new JButton("Supprimer");
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(mainPanel, "Etes-vous sur de vouloir supprimer cet élément ?") == 0) {
                    mainPanel.removeElement(element);
                }

            }
        });
        final SpinnerNumberModel spinnerModel = new SpinnerNumberModel(element.getApparitionStep() + 1, 1, 100, 1);
        JSpinner step = new JSpinner(spinnerModel);
        step.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                int oldStep = element.getApparitionStep();
                int newStep = spinnerModel.getNumber().intValue() - 1;
                if (oldStep != newStep) {
                    element.setApparitionStep(newStep);
                    if (oldStep > newStep && oldStep == mainPanel.getSchema().getDatas().getStepMax() && mainPanel.getSchema().checkMax()) {
                        mainController.updateStepLabel();
                        if (mainPanel.getCurrentStep() > newStep) {
                            mainPanel.setCurrentStep(newStep);
                        }
                    }

                    Schema schema = mainPanel.getSchema();
                    if (newStep > schema.getDatas().getStepMax()) {
                        schema.getDatas().setStepMax(newStep);
                        mainController.updateStepLabel();
                    }

                    vv.repaint();
                }

            }
        });
        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.add(Box.createGlue());
        namePanel.add(new LabelledComponent("Etiquette", comboBox, true));
        namePanel.add(Box.createGlue());
        JPanel delAndStepPanel = new JPanel();
        delAndStepPanel.setLayout(new BoxLayout(delAndStepPanel, BoxLayout.X_AXIS));
        delAndStepPanel.add(Box.createGlue());
        delAndStepPanel.add(delete);
        delAndStepPanel.add(Box.createGlue());
        delAndStepPanel.add(new LabelledComponent("Etape", step, false));
        namePanel.add(delAndStepPanel);
        result.add(namePanel);
        comboBox.setPreferredSize(new Dimension(200, 40));

        JPanel iconAndSizePanel = new JPanel();
        iconAndSizePanel.setLayout(new BoxLayout(iconAndSizePanel, BoxLayout.Y_AXIS));

        final JSlider sizeSlider = new JSlider(0, TextInfo.minimalSize, TextInfo.maximalSize, element.getTextSize());
        sizeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                element.setTextSize(sizeSlider.getValue());
                vv.repaint();
            }
        });
    iconAndSizePanel.add(new LabelledComponent("Taille", sizeSlider, true));
        final JComboBox iconList = new JComboBox<ImageIcon>(IconLoader.getBigPictureList());
        iconList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (iconList.getSelectedIndex() != -1) {
                    element.setPictureName(((IconLoader.NamedImageIcon)iconList.getSelectedItem()).getName());
                    vv.repaint();
                }

            }
        });
        iconList.setSelectedItem(element.getPicture());
        comboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iconList.setSelectedItem(element.getPicture());
            }
        });
        iconAndSizePanel.add(new LabelledComponent("Image", iconList, true));

        result.add(iconAndSizePanel);
        result.add(new LabelledComponent("Position", this.buildPositionChooser(element), true));
        result.setBorder(BorderFactory.createTitledBorder("Edition de l'élément"));
    }


    protected JPanel buildPositionChooser(Element e) {
        JPanel result = new JPanel(new GridLayout(3, 3));
        ButtonGroup group = new ButtonGroup();
        result.add(this.getPositionButton(Renderer.VertexLabel.Position.NW, e, group));
        result.add(this.getPositionButton(Renderer.VertexLabel.Position.N, e, group));
        result.add(this.getPositionButton(Renderer.VertexLabel.Position.NE, e, group));
        result.add(this.getPositionButton(Renderer.VertexLabel.Position.W, e, group));
        result.add(Box.createRigidArea(new Dimension(30, 20)));
        result.add(this.getPositionButton(Renderer.VertexLabel.Position.E, e, group));
        result.add(this.getPositionButton(Renderer.VertexLabel.Position.SW, e, group));
        result.add(this.getPositionButton(Renderer.VertexLabel.Position.S, e, group));
        result.add(this.getPositionButton(Renderer.VertexLabel.Position.SE, e, group));
        return result;
    }

    protected JRadioButton getPositionButton(final Renderer.VertexLabel.Position p, final Element e, ButtonGroup group) {
        JRadioButton result = new JRadioButton();
        result.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                e.setLabelPosition(p);
                vv.repaint();
            }
        });
        group.add(result);
        if (p.equals(e.getLabelPosition())) {
            result.setSelected(true);
        } else {
            result.setSelected(false);
        }

        result.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return result;
    }

}
