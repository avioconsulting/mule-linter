package com.avioconsulting.mule.maven.formatter.impl;


import org.apache.maven.plugin.logging.Log;

import com.avioconsulting.mule.linter.model.rule.Rule;

/**
 * @author aivaldi
 * Create report using conosle
 */
public class ConsoleFormatter extends AbstractFormatter {

    @Override
    public void buildReport() {
        Log log = this.mojo.getLog();
        Integer rulesCount = this.ruleExecutor.getRuleCount();
        String separator = "****************************************************************************";
        log.info( separator );
        log.info( "AVIO Mule linter execution results" );

        log.info( String.format("%s rules executed.", rulesCount));
        log.info("Rule validation results");

        this.ruleExecutor.getResults().forEach( violation ->{
                    Rule rule = violation.getRule();
                    String ruleSeverity = rule.getSeverity().toString();
                    String ruleId = rule.getRuleId();
                    String ruleName = rule.getRuleName();
                    StringBuilder messageLogBuilder = new StringBuilder();
                    messageLogBuilder.append(String.format(" - [%s] %s - %s : ",ruleSeverity, ruleId, ruleName  ));
                    if (violation.getLineNumber() > 0)
                        messageLogBuilder.append(String.format("Line number: %s ", violation.getLineNumber()));

                    messageLogBuilder.append(violation.getMessage());
                    log.info( messageLogBuilder.toString());
                }
        );

        log.info( String.format("Found a total of %s violations of %s rules.", rulesCount,rulesCount));

        log.info( separator );

    }


}
