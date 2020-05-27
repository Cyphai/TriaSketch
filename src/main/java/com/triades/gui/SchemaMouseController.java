package com.triades.gui;

import com.triades.model.Element;
import com.triades.model.Relation;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public class SchemaMouseController extends EditingGraphMousePlugin<Element, Relation> {
   protected boolean movingVertex = false;
   protected boolean edgeVisible;
   protected boolean translating;
   protected Element startVertex;
   protected Point2D startPoint;

   protected SchemaPanel mainPanel;
   protected VisualizationViewer<Element, Relation> vv;
   protected final int minDistance = 24;

   public SchemaMouseController(SchemaPanel mainPanel) {
      super(null, null);
      setEdgeSupport(new NoCreationEdgeSupport<Element, Relation>());
      this.mainPanel = mainPanel;
      this.vv = mainPanel.getVisualisationViewer();
       this.movingVertex = false;
      this.translating = false;
      this.edgeVisible = false;
      this.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
   }

   public void mousePressed(MouseEvent e) {
      this.startPoint = e.getPoint();
      if (e.isShiftDown() && e.isControlDown()) {
         this.translating = true;
         this.mainPanel.setEnabledVertexHighlight(false);
         this.vv.repaint();
      } else {
         Point2D p = e.getPoint();
         GraphElementAccessor<Element, Relation> pickSupport = this.vv.getPickSupport();
         if (pickSupport != null) {
            Element vertex = pickSupport.getVertex(this.vv.getModel().getGraphLayout(), p.getX(), p.getY());
            Relation relation = pickSupport.getEdge(this.vv.getGraphLayout(), p.getX(), p.getY());
            if (vertex != null) {
               this.startVertex = vertex;
               Point2D tempPoint = this.vv.getRenderContext().getMultiLayerTransformer().transform(vertex.getPosition());
               this.startPoint.setLocation(tempPoint.getX(), tempPoint.getY());
               if (e.isControlDown()) {
                  this.mainPanel.selectElement(this.startVertex);
                  this.mainPanel.setEnabledVertexHighlight(false);
                  this.movingVertex = true;
               }
            } else if (relation != null) {
               this.mainPanel.selectRelation(relation);
            } else if (e.isShiftDown()) {
               Point2D temp = this.vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.getPoint());
               Double position = new Double(temp.getX(), temp.getY());
               int apparitionStep = this.mainPanel.getCurrentStep();
               if (MainFrame.getSingleton().getProgramData().isElementAddedToFirstStep()) {
                  apparitionStep = 0;
               }

               Element newVertex = new Element(this.mainPanel.getSchema().getDatas().getNextElementId(), position, apparitionStep);
               this.mainPanel.addElement(newVertex);
               this.startVertex = newVertex;
            } else if (e.isControlDown()) {
               this.translating = true;
               this.startPoint = e.getPoint();
               this.mainPanel.setEnabledVertexHighlight(false);
            } else {
               this.mainPanel.unselectItem();
            }
         }
      }

   }

   public void mouseReleased(MouseEvent e) {
      if (this.movingVertex) {
         this.movingVertex = false;
         this.mainPanel.setEnabledVertexHighlight(MainFrame.getSingleton().getProgramData().isHighlightVertices());
      } else if (this.startVertex != null) {
         this.mainPanel.selectElement(this.startVertex);
      }

      if (this.edgeVisible) {
         this.edgeVisible = false;
         edgeSupport.endEdgeCreate(vv, null);
         GraphElementAccessor<Element, Relation> pickSupport = this.vv.getPickSupport();
         if (pickSupport != null) {
            Point2D p = e.getPoint();
            Element vertex = pickSupport.getVertex(this.vv.getModel().getGraphLayout(), p.getX(), p.getY());
            if (vertex != null && this.startVertex != null) {
               Relation relation = this.mainPanel.getGraph().findEdge(this.startVertex, vertex);
               if (relation == null) {
                  if (!(this.startVertex).equals(vertex) || e.isShiftDown()) {
                     relation = new Relation(this.mainPanel.getSchema().getDatas().getNextRelationId(), (this.startVertex).getId(), vertex.getId());
                     this.mainPanel.addRelation(relation, this.startVertex, vertex);
                  } else {
                     this.mainPanel.selectElement(vertex);
                  }
               } else {
                  this.mainPanel.selectRelation(relation);
               }
            }
         }
      }

      if (this.translating) {
         this.mainPanel.setEnabledVertexHighlight(MainFrame.getSingleton().getProgramData().isHighlightVertices());
      }

      this.translating = false;
      this.startPoint = null;
      this.startVertex = null;
   }

   public void mouseDragged(MouseEvent e) {
      if (this.translating) {
         Point2D translation = e.getPoint();
         translation.setLocation(translation.getX() - this.startPoint.getX(), translation.getY() - this.startPoint.getY());
         this.vv.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).translate(translation.getX(), translation.getY());
         this.mainPanel.updateVertexSelectionShape();
         this.mainPanel.updateTitlePosition();
         this.startPoint = e.getPoint();
      } else if (this.startVertex != null) {

          Point2D downPoint = this.vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.getPoint());
          if (this.movingVertex) {

              this.vv.getGraphLayout().setLocation(this.startVertex, downPoint);
            (this.startVertex).setPosition(downPoint);
            this.mainPanel.updateVertexSelectionShape();
            this.mainPanel.updateNewVertexAnnotation();
         } else {
            if (!this.edgeVisible && this.startPoint.distance(e.getPoint()) > minDistance) {
               edgeSupport.startEdgeCreate(vv, startVertex, startPoint, EdgeType.DIRECTED);

               this.edgeVisible = true;
            }

            if (this.edgeVisible) {
               GraphElementAccessor<Element, Relation> pickSupport = this.vv.getPickSupport();
               boolean drawAtEnd = true;
               if (pickSupport != null) {
                  Point2D p = e.getPoint();
                  Element vertex = pickSupport.getVertex(this.vv.getModel().getGraphLayout(), p.getX(), p.getY());
                  if (vertex != null) {
                     drawAtEnd = false;
                     Point2D endPoint = this.vv.getRenderContext().getMultiLayerTransformer().transform(vertex.getPosition());
                     edgeSupport.midEdgeCreate(vv,endPoint);
                  }
               }

               if (drawAtEnd) {
                  edgeSupport.midEdgeCreate(vv, e.getPoint());
               }
            }
         }

         this.vv.repaint();
      }

   }

   public void mouseMoved(MouseEvent e) {
      if (this.edgeVisible) {
         this.edgeVisible = false;
         edgeSupport.endEdgeCreate(vv, null);
      }

   }

}
