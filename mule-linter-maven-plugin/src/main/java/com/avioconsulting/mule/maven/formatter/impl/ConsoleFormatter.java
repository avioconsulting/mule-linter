package com.avioconsulting.mule.maven.formatter.impl;

import com.avioconsulting.mule.linter.model.rule.ReportWriters;
import groovy.lang.Closure;
import org.apache.maven.plugin.logging.Log;

/** Maven logging adapter for the shared console writer. */
public class ConsoleFormatter extends AbstractFormatter {
    @Override
    public void buildReport() {
        Log log = getLog();
        ReportWriters.console(ruleExecutor.getAnalysisResult(), true, new Closure<Void>(this) {
            public Void doCall(String line, boolean warning) {
                if (warning) log.warn(line);
                else log.info(line);
                return null;
            }
        });
    }
}
