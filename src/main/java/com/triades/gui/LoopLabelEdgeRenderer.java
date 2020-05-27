package com.triades.gui;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Created by babcool on 5/27/17.
 */
public class LoopLabelEdgeRenderer<V,E> extends BasicEdgeLabelRenderer<V,E> {


    protected boolean isALoop(Pair<V> endPoints) {
        return endPoints.getFirst() != null && endPoints.getFirst().equals(endPoints.getSecond());
    }

    @Override
    public void labelEdge(RenderContext<V, E> rc, Layout<V, E> layout, E e, String label) {
        if (!isALoop(layout.getGraph().getEndpoints(e))) {
            super.labelEdge(rc, layout, e, label);
        } else {
            if (label != null && label.length() != 0) {
                Graph<V, E> graph = layout.getGraph();
                Pair<V> endpoints = graph.getEndpoints(e);
                V v1 = endpoints.getFirst();
                V v2 = endpoints.getSecond();
                if (rc.getEdgeIncludePredicate().apply(Context.getInstance(graph, e))) {
                    if (rc.getVertexIncludePredicate().apply(Context.getInstance(graph, v1)) && rc.getVertexIncludePredicate().apply(Context.getInstance(graph, v2))) {
                        Point2D p = (Point2D) layout.apply(v1);
                        p = rc.getMultiLayerTransformer().transform(Layer.LAYOUT, p);
                        float x = (float) p.getX();
                        float y = (float) p.getY();
                        GraphicsDecorator g = rc.getGraphicsContext();
                        double closeness = ((Number) rc.getEdgeLabelClosenessTransformer().apply(Context.getInstance(graph, e))).doubleValue();
                        int posX = (int) ((double) x + closeness*200 - 100 );
                        int posY = (int) ((double) y - rc.getLabelOffset() );
                        Component component = this.prepareRenderer(rc, rc.getEdgeLabelRenderer(), label, rc.getPickedEdgeState().isPicked(e), e);
                        Dimension d = component.getPreferredSize();
                        Shape edgeShape = (Shape) rc.getEdgeShapeTransformer().apply(e);
                        double parallelOffset = 1.0D;
                        parallelOffset += (double) rc.getParallelEdgeIndexFunction().getIndex(graph, e);
                        parallelOffset *= (double) d.height;
                        if (edgeShape instanceof Ellipse2D) {
                            parallelOffset += edgeShape.getBounds().getHeight();
                            parallelOffset = -parallelOffset;
                        }

                        AffineTransform old = g.getTransform();
                        AffineTransform xform = new AffineTransform(old);
                        xform.translate((double) (posX), (double) (posY));

                        xform.translate((double) (-d.width / 2), -((double) (d.height / 2) - parallelOffset));
                        g.setTransform(xform);
                        g.draw(component, rc.getRendererPane(), 0, 0, d.width, d.height, true);
                        g.setTransform(old);
                    }
                }
            }
        }
    }
}
