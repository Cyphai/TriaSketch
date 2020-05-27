package com.triades.gui;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.triades.model.Element;
import com.triades.model.Relation;
import com.triades.model.Schema;
import com.triades.tools.IconLoader;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.annotations.Annotation;
import edu.uci.ics.jung.visualization.annotations.Annotation.Layer;
import edu.uci.ics.jung.visualization.annotations.AnnotationManager;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Positioner;
import edu.uci.ics.jung.visualization.util.ArrowFactory;


import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.QuadCurve2D.Float;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

public class SchemaPanel extends JPanel {
   private static final long serialVersionUID = 1L;
   private Schema schema;
   private TreeMap<Integer, Element> elementsMap;
   private DirectedSparseGraph<Element, Relation> graph;
   private VisualizationViewer<Element, Relation> vv;
   private SchemaControllerPanel controllerPanel;
   private SchemaMouseController mouseController;
   private Annotation<String> title;
   private Double titlePosition = new Double(50.0D, 50.0D);
   private Relation selectedRelation;
   private Element selectedElement;
   private Annotation selectedVertexShape;
   private static HashMap<String, Font> fontMap = new HashMap<String, Font>();
   private java.awt.geom.Ellipse2D.Double selectedVertexEllipse;
   private AnnotationManager annotationManager;
   private ArrayList highlightedVertexShape;
   private boolean enableVertexHighlight;
   private int currentStep;



   public SchemaPanel(Schema schema) {
      this.schema = schema;
      this.currentStep = 0;
      this.enableVertexHighlight = MainFrame.getSingleton().getProgramData().isHighlightVertices();
      this.buildGraph();
      this.buildPanel();
   }

   protected void buildGraph() {
      elementsMap = new TreeMap<Integer, Element>();
      graph = new DirectedSparseGraph<Element, Relation>();
      Iterator var2 = this.schema.getElements().iterator();

      while(var2.hasNext()) {
         Element e = (Element)var2.next();
         this.elementsMap.put(e.getId(), e);
         this.graph.addVertex(e);
      }

      var2 = this.schema.getRelations().iterator();

      while(var2.hasNext()) {
         Relation r = (Relation)var2.next();
         Element source = elementsMap.get(r.getSource());
         Element destination = elementsMap.get(r.getDestination());
         this.graph.addEdge(r, source, destination);
      }

      final Layout<Element, Relation> layout = new StaticLayout<Element, Relation>(this.graph, new Function<Element, Point2D>() {
         public Point2D apply(Element e) {
            return e.getPosition();
         }
      });
      this.vv = new VisualizationViewer<Element, Relation>(layout, new Dimension(1200, 850));
      this.vv.getRenderer().getVertexLabelRenderer().setPosition(Position.AUTO);
      this.vv.getRenderer().getVertexLabelRenderer().setPositioner(new Positioner() {
         public Position getPosition(float x, float y, Dimension d) {
            Element e = SchemaPanel.this.vv.getPickSupport().getVertex(layout, (double)x, (double)y);
            if (e != null) {
               return e.getLabelPosition();
            } else {
               System.err.println("Warning, an error occurs to determine the position of the label of a vertex");
               return Position.S;
            }
         }
      });
      this.vv.setPickSupport(new ShapePickSupport<Element, Relation>(this.vv));
      this.setUpRenderer(this.vv.getRenderContext());
      this.annotationManager = new AnnotationManager(this.vv.getRenderContext());
      this.title = new Annotation<String>("", Layer.UPPER, Color.black, false, new Double());
      this.updateTitle(this.schema.getDatas().getName());
      this.updateTitlePosition();
      this.annotationManager.add(Layer.UPPER, this.title);
      this.vv.addPreRenderPaintable(this.annotationManager.getAnnotationPaintable(Layer.UPPER));
      this.vv.addPostRenderPaintable(this.annotationManager.getAnnotationPaintable(Layer.UPPER));
      this.vv.addPreRenderPaintable(this.annotationManager.getAnnotationPaintable(Layer.LOWER));
      this.selectedVertexEllipse = new java.awt.geom.Ellipse2D.Double(0.0D, 0.0D, 90.0D, 90.0D);
      this.selectedVertexShape = new Annotation<Ellipse2D.Double>(this.selectedVertexEllipse, Layer.LOWER, Color.white, true, new Double(0.0D, 0.0D));
      this.highlightedVertexShape = new ArrayList();
      this.mouseController = new SchemaMouseController(this);
      this.vv.addMouseListener(this.mouseController);
      this.vv.addMouseMotionListener(this.mouseController);
      this.vv.setBackground(Color.white);
   }

   protected void buildPanel() {
      this.setLayout(new BorderLayout());
      this.add(this.buildGraphPanel(), "Center");
      this.add(this.buildBottomPanel(), "South");
   }

   protected JPanel buildGraphPanel() {
      JPanel result = new JPanel();
      result.setLayout(new BorderLayout());
      result.add(this.vv, "Center");
      result.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createEtchedBorder(1)));
      return result;
   }

   protected JPanel buildBottomPanel() {
      this.controllerPanel = new SchemaControllerPanel(this.schema, this.vv, this);
      return this.controllerPanel;
   }

   protected void setUpRenderer(final RenderContext<Element, Relation> renderer) {
      renderer.setVertexIncludePredicate(new Predicate<Context<Graph<Element, Relation>, Element>>() {

         public boolean apply(Context<Graph<Element, Relation>, Element> arg0) {
            return (arg0.element).getApparitionStep() <= SchemaPanel.this.currentStep;
         }
      });
      renderer.setVertexIconTransformer(new Function<Element, Icon>() {
         public Icon apply(Element arg0) {
            return SchemaPanel.this.getIconForElement(arg0);
         }
      });
      renderer.setVertexLabelTransformer(new Function<Element, String>() {
         public String apply(Element arg0) {
            return SchemaPanel.this.getLabelForElement(arg0);
         }
      });
      vv.getRenderer().setEdgeLabelRenderer(new LoopLabelEdgeRenderer<Element, Relation>());
      renderer.setEdgeLabelTransformer(new Function<Relation, String>() {
         public String apply(Relation relation) {
            return relation.getLabel();
         }
      });
      renderer.getEdgeLabelRenderer().setRotateEdgeLabels(true);
      renderer.setEdgeDrawPaintTransformer(new Function<Relation, Paint>() {
         public Paint apply(Relation relation) {
            return relation.getColor();
         }
      });
      renderer.setEdgeFontTransformer(new Function<Relation, Font>() {


         public Font apply(Relation relation) {
            return getFontForRelation(relation);
         }
      });
      renderer.setVertexFontTransformer(new Function<Element, Font>() {

         public Font apply(Element element) {
            return getFontForElement(element);
         }
      });
      renderer.setLabelOffset(-15);
      renderer.setEdgeLabelClosenessTransformer(new Function<Context<Graph<Element, Relation>, Relation>, Number>() {
         public Number apply(Context<Graph<Element, Relation>, Relation> arg0) {
            renderer.setLabelOffset(((Relation)arg0.element).getLabelVerticalPosition());
            return ((Relation)arg0.element).getLabelHorizontalPosition();
         }
      });
      renderer.setVertexShapeTransformer(new Function<Element, Shape>() {
         Ellipse2D vertexSelectionShape = new java.awt.geom.Ellipse2D.Double(-30.0D, -30.0D, 60.0D, 60.0D);

         public Shape apply(Element element) {
            return this.vertexSelectionShape;
         }
      });
      renderer.setEdgeStrokeTransformer(new Function<Relation, Stroke>() {
         float[] dash = new float[]{20.0F, 8.0F, 5.0F, 8.0F};
         private Stroke bigStroke;
         private Stroke littleStroke;

         {
            this.bigStroke = new BasicStroke(4.0F, 1, 1, 10.0F, this.dash, 10.0F);
            this.littleStroke = new BasicStroke(2.0F);
         }

         public Stroke apply(Relation relation) {
            boolean selected = relation == SchemaPanel.this.selectedRelation;

            return selected ? this.bigStroke : this.littleStroke;

         }
      });
      renderer.setEdgeArrowTransformer(new Function<Context<Graph<Element, Relation>, Relation>, Shape>() {
         private Shape bigArrow = ArrowFactory.getNotchedArrow(25.0F, 12.0F, 25.0F);
         private Shape littleArrow = ArrowFactory.getNotchedArrow(15.0F, 8.0F, 15.0F);

         public Shape apply(Context<Graph<Element, Relation>, Relation> arg0) {
            return arg0.element == SchemaPanel.this.selectedRelation ? this.bigArrow : this.littleArrow;
         }
      });
      renderer.setEdgeShapeTransformer(new Function<Relation, Shape>() {
         private QuadCurve2D instance = new Float();
         private EdgeShape<Element, Relation>.Loop loopInstance = new EdgeShape<Element, Relation>(graph).new Loop();

         public Shape apply( Relation r) {
            boolean isALoop = r.getSource().equals(r.getDestination());

            if (isALoop) {
               return loopInstance.apply(r);
            } else {
               float distance = (float) ( SchemaPanel.this.graph.getSource(r)).getPosition().distance(((Element) SchemaPanel.this.graph.getDest(r)).getPosition());
               this.instance.setCurve(0.0D, 0.0D, 0.5D, 55.0D, (double) (1.0F - 27.0F / distance), 5.0D);
               return this.instance;
            }
         }
      });
      renderer.setArrowPlacementTolerance(4.0F);
   }

   private Font getFontForElement(Element element) {
      boolean selected = (element.equals(SchemaPanel.this.selectedElement));
      boolean highlighted = SchemaPanel.this.enableVertexHighlight && SchemaPanel.this.currentStep > 0 && element.getApparitionStep() == SchemaPanel.this.currentStep;
      int size = element.getTextSize();
      return buildFont(size, selected, highlighted);
   }

   private Font getFontForRelation(Relation relation) {
      boolean selected = (relation.equals(SchemaPanel.this.selectedElement));
      boolean highlighted = !(!SchemaPanel.this.enableVertexHighlight || SchemaPanel.this.currentStep <= 0
              || ((Element)SchemaPanel.this.graph.getSource(relation)).getApparitionStep() != SchemaPanel.this.currentStep
                           && ((Element)SchemaPanel.this.graph.getDest(relation)).getApparitionStep() != SchemaPanel.this.currentStep);
      int size = relation.getTextSize();
      return buildFont(size, selected, highlighted);
   }

   protected Icon getIconForElement(Element e) {
      return IconLoader.getBigIcon(e.getPictureName());
   }

   protected String getLabelForElement(Element e) {
      return e.getName();
   }

   public void updateTitle(String newTitle) {
      if (newTitle == null) {
         newTitle = this.schema.getDatas().getName();
      }

      if (newTitle.length() == 0) {
         this.title.setAnnotation("");
         this.vv.repaint();
      } else {
         StringBuilder newAnnotation = new StringBuilder(95 + newTitle.length());
         newAnnotation.append("<html><TABLE border=1><TR><TD><center><font face=\"times new roman\" size=6>");
         newAnnotation.append(newTitle);
         if (this.schema.getDatas().getStepMax() > 0 && this.currentStep <= this.schema.getDatas().getStepMax()) {
            newAnnotation.append(" (" + (this.currentStep + 1) + "/" + (this.schema.getDatas().getStepMax() + 1) + ")");
         }

         newAnnotation.append("</center></font></TD></TR></TABLE></html>");
         this.title.setAnnotation(newAnnotation.toString());
         this.vv.repaint();
      }
   }

   public SchemaControllerPanel getControllerPanel() {
      return this.controllerPanel;
   }

   public VisualizationViewer<Element, Relation> getVisualisationViewer() {
      return this.vv;
   }

   public Schema getSchema() {
      return this.schema;
   }

   public void selectElement(Element newSelection) {
      if (this.selectedElement != null || this.selectedRelation != null) {
         this.unselectItem();
      }

      this.selectedElement = newSelection;
      this.controllerPanel.showElement(newSelection);
      this.updateVertexSelectionShape();
      this.annotationManager.add(Layer.LOWER, this.selectedVertexShape);
      this.vv.repaint();
   }

   public void selectRelation(Relation newSelection) {
      if (this.selectedElement != null || this.selectedRelation != null) {
         this.unselectItem();
      }

      this.selectedRelation = newSelection;
      this.controllerPanel.showRelation(newSelection);
      this.vv.repaint();
   }

   public void unselectItem() {
      if (this.selectedElement != null) {
         this.annotationManager.remove(this.selectedVertexShape);
         this.selectedElement = null;
         this.vv.repaint();
      }

      if (this.selectedRelation != null) {
         this.selectedRelation = null;
         this.vv.repaint();
      }

      this.controllerPanel.removePopup();
   }

   public void updateVertexSelectionShape() {
      if (this.selectedElement != null) {
         Point2D annotationPosition = this.selectedVertexShape.getLocation();
         annotationPosition.setLocation(this.selectedElement.getPosition());
         double deltaX = (double)((Shape)this.selectedVertexShape.getAnnotation()).getBounds().width / 2.0D;
         double deltaY = (double)((Shape)this.selectedVertexShape.getAnnotation()).getBounds().height / 2.0D;
         annotationPosition.setLocation(annotationPosition.getX() - deltaX, annotationPosition.getY() - deltaY);
         this.selectedVertexEllipse.x = annotationPosition.getX();
         this.selectedVertexEllipse.y = annotationPosition.getY();
         Point2D center = new Double(this.selectedVertexEllipse.x + this.selectedVertexEllipse.width / 2.0D, this.selectedVertexEllipse.y + this.selectedVertexEllipse.height / 2.0D);
         center = this.vv.getRenderContext().getMultiLayerTransformer().transform(center);
         RadialGradientPaint gradient = new RadialGradientPaint(center, (float)(this.selectedVertexEllipse.width / 4.0D + this.selectedVertexEllipse.height / 4.0D), new float[]{0.0F, 1.0F}, new Color[]{new Color(154, 179, 193), new Color(154, 179, 193, 0)});
         this.selectedVertexShape.setPaint(gradient);
      }
   }

   public void updateTitlePosition() {
      this.title.setLocation(this.vv.getRenderContext().getMultiLayerTransformer().inverseTransform(this.titlePosition));
   }

   public void addElement(Element newElement) {
      this.schema.addElement(newElement);
      this.graph.addVertex(newElement);
      this.vv.getGraphLayout().setLocation(newElement, newElement.getPosition());
      this.selectElement(newElement);
   }

   public void addRelation(Relation newRelation, Element source, Element destination) {
      this.schema.addRelation(newRelation);
      this.graph.addEdge(newRelation, source, destination);
      this.selectRelation(newRelation);
      this.vv.repaint();
   }

   public void removeElement(Element oldElement) {
      Iterator var3 = this.graph.getIncidentEdges(oldElement).iterator();

      while(var3.hasNext()) {
         Relation r = (Relation)var3.next();
         this.removeRelation(r);
      }

      this.schema.removeElement(oldElement);
      this.graph.removeVertex(oldElement);
      this.unselectItem();
      this.vv.repaint();
   }

   public void removeRelation(Relation oldRelation) {
      this.schema.removeRelation(oldRelation);
      this.graph.removeEdge(oldRelation);
      this.unselectItem();
      this.vv.repaint();
   }

   public DirectedSparseGraph<Element, Relation> getGraph() {
      return this.graph;
   }

   public void exportCurrentStepToPicture(File target) {
      if (target == null) {
         File currentFile = MainFrame.getSingleton().getCurrentFile();
         if (currentFile != null) {
            String fileName = currentFile.getAbsolutePath();
            String prefix = "";
            if (this.schema.getDatas().getStepMax() > 0) {
               if (this.currentStep <= this.schema.getDatas().getStepMax()) {
                  prefix = "_etape_" + (this.currentStep + 1);
               } else {
                  prefix = "_final";
               }
            }

            fileName = fileName.replace(".scm", prefix + ".png");
            currentFile = new File(fileName);
         }

         JFileChooser fileChooser = new JFileChooser(currentFile);
         fileChooser.setSelectedFile(currentFile);
         fileChooser.setFileFilter(new FileNameExtensionFilter("Fichier image (png)", "png"));
         if (fileChooser.showSaveDialog(this) != 0) {
            return;
         }

         File selectedFile = fileChooser.getSelectedFile();
         if (!selectedFile.getName().endsWith(".png")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".png");
         }

         if (selectedFile.exists() && JOptionPane.showConfirmDialog(this, "Le fichier " + selectedFile.getName() + " existe déjà, êtes-vous sur de vouloir l'écraser?") != 0) {
            this.exportCurrentStepToPicture((File)null);
            return;
         }

         target = selectedFile;
      }

      this.unselectItem();
      BufferedImage bI = new BufferedImage(this.vv.getWidth(), this.vv.getHeight(), 1);
      this.vv.paint(bI.getGraphics());

      try {
         ImageIO.write(bI, "png", target);
      } catch (IOException var5) {
         JOptionPane.showMessageDialog(this, "Erreur durant l'enregistrement de l'image :\n" + var5.toString(), "Erreur", 0);
         var5.printStackTrace();
      }

   }

   public void setCurrentStep(int newStep) {
      if (newStep < 0) {
         newStep = 0;
      }

      if (newStep > this.schema.getDatas().getStepMax() + 1) {
         newStep = this.schema.getDatas().getStepMax() + 1;
      }

      this.currentStep = newStep;
      this.updateTitle((String)null);
      this.updateNewVertexAnnotation();
      if (this.selectedElement != null && this.selectedElement.getApparitionStep() > this.currentStep) {
         this.unselectItem();
      }

      if (this.selectedRelation != null && (((Element)this.graph.getSource(this.selectedRelation)).getApparitionStep() > this.currentStep || ((Element)this.graph.getDest(this.selectedRelation)).getApparitionStep() > this.currentStep)) {
         this.unselectItem();
      }

   }

   public int getCurrentStep() {
      return this.currentStep;
   }

   public void exportLastStep(File target) {
      int oldStep = this.getCurrentStep();
      this.setCurrentStep(this.schema.getDatas().getStepMax() + 1);
      this.exportCurrentStepToPicture(target);
      this.setCurrentStep(oldStep);
   }

   public void exportAllSteps() {
      if (this.schema.countUsefulSteps() < this.schema.getDatas().getStepMax() + 1) {
         int r = JOptionPane.showConfirmDialog(this, "Certaines étapes sont inutiles (aucun sommet affiché en plus)\nvoulez-vous les supprimez avant de générer les images ?");
         if (r == 0) {
            this.schema.cleanStep();
            this.controllerPanel.updateStepLabel();
            this.repaint();
         } else if (r != 1) {
            return;
         }
      }

      if (this.schema.getDatas().getStepMax() == 0) {
         this.setCurrentStep(0);
         this.exportCurrentStepToPicture((File)null);
      } else {
         File currentFile = MainFrame.getSingleton().getCurrentFile();
         if (currentFile != null) {
            String fileName = currentFile.getAbsolutePath();
            fileName = fileName.replace(".scm", ".png");
            currentFile = new File(fileName);
         }

         JFileChooser fileChooser = new JFileChooser(currentFile);
         fileChooser.setSelectedFile(currentFile);
         fileChooser.setFileFilter(new FileNameExtensionFilter("Fichier image (png)", new String[]{"png"}));
         if (fileChooser.showSaveDialog(this) == 0) {
            File selectedFile = fileChooser.getSelectedFile();
            String filename = selectedFile.getAbsolutePath();
            if (filename.endsWith(".png")) {
               filename = filename.substring(0, filename.length() - 4);
            }

            int oldStep;
            if (filename.contains("_etape_")) {
               oldStep = filename.indexOf("_etape_");
               filename = filename.substring(0, oldStep);
            }

            oldStep = this.currentStep;
            boolean eraseOld = false;

            for(int i = 0; i <= this.schema.getDatas().getStepMax(); ++i) {
               String completeFileName = filename + "_etape_" + (i + 1) + ".png";
               File target = new File(completeFileName);
               if (target.exists() && !eraseOld) {
                  if (JOptionPane.showConfirmDialog(this, "Le fichier " + target.getName() + " existe déjà, voulez-vous l'écraser?\nOui pour écraser tous les fichiers dans ce cas (étapes suivantes)\nNon pour annuler la suite du processus") != 0) {
                     break;
                  }

                  eraseOld = true;
               }

               this.setCurrentStep(i);
               this.exportCurrentStepToPicture(target);
            }

            String completeName = filename + "_final.png";
            File target = new File(completeName);
            this.exportLastStep(target);
            this.setCurrentStep(oldStep);
         }
      }
   }

   protected void setEnabledVertexHighlight(boolean newValue) {
      this.enableVertexHighlight = newValue;
      this.updateNewVertexAnnotation();
   }

   public void updateNewVertexAnnotation() {
      Iterator var2 = this.highlightedVertexShape.iterator();

      while(var2.hasNext()) {
         Annotation a = (Annotation)var2.next();
         this.annotationManager.remove(a);
      }

      this.highlightedVertexShape.clear();
      float eSize = 60.0F;
      if (this.enableVertexHighlight && this.currentStep > 0) {
         if (this.selectedElement != null) {
            this.annotationManager.remove(this.selectedVertexShape);
         }

         Iterator var3 = this.schema.getElements().iterator();

         while(var3.hasNext()) {
            Element e = (Element)var3.next();
            if (e.getApparitionStep() == this.currentStep) {
               Point2D p = this.vv.getRenderContext().getMultiLayerTransformer().transform(e.getPosition());
               RadialGradientPaint gradient = new RadialGradientPaint(p, 60.0F, new float[]{0.0F, 0.5F, 1.0F}, new Color[]{new Color(166, 20, 20, 120), new Color(166, 20, 20, 40), new Color(166, 20, 20, 0)});
               Annotation newAnn = new Annotation(new java.awt.geom.Ellipse2D.Double(e.getPosition().getX() - 60.0D, e.getPosition().getY() - 60.0D, 120.0D, 120.0D), Layer.LOWER, gradient, true, p);
               this.highlightedVertexShape.add(newAnn);
               this.annotationManager.add(Layer.LOWER, newAnn);
            }
         }

         if (this.selectedElement != null) {
            this.annotationManager.add(Layer.LOWER, this.selectedVertexShape);
         }
      }

      this.vv.repaint();
   }

   protected Font buildFont(int size, boolean selected, boolean highlighted) {
      String fontId = size+(selected?"s":"")+(highlighted?"h":"");
      Font result = fontMap.get(fontId);
      if (result == null) {
         int style = selected?Font.ITALIC:Font.PLAIN;
         style = style | (highlighted?Font.BOLD:Font.PLAIN);
         result = new Font(Font.SERIF, style, size);
         fontMap.put(fontId, result);
      }
      return result;
   }
}
