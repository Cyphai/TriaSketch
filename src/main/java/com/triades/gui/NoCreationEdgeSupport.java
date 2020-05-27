package com.triades.gui;

import com.google.common.base.Supplier;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.control.SimpleEdgeSupport;

/**
 * Created by babcool on 5/26/17.
 */
public class NoCreationEdgeSupport<V, E> extends SimpleEdgeSupport<V,E> {


    public NoCreationEdgeSupport() {
        super(null);
    }

    @Override
    public void endEdgeCreate(BasicVisualizationServer<V, E> vv, V endVertex) {
        startVertex = null;
        super.endEdgeCreate(vv, endVertex);
    }
}
