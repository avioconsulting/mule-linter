package com.avioconsulting.mule.maven.formatter.impl;

import org.apache.maven.doxia.sink.Sink;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class HtmlFormatter extends AbstractFormatter {

    @Override
    /**
     * Use Skin to create html report
     * Sink mainSink = this.mojo.getSink();
     *
     */
    public void buildReport() {
        throw new NotImplementedException();

    }
}
