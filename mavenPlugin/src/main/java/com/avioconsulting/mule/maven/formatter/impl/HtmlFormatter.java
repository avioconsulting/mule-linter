package com.avioconsulting.mule.maven.formatter.impl;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;
import org.apache.maven.doxia.sink.impl.SinkEventAttributeSet;
import org.apache.maven.reporting.MavenReportException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;

public class HtmlFormatter extends AbstractFormatter {

    @Override
    /**
     * Use Skin to create html report
     * Sink mainSink = this.mojo.getSink();
     *
     */
    public void buildReport() throws MavenReportException {
        Sink mainSink = this.mojo.getSink();

        if (mainSink == null) {
            throw new MavenReportException("Could not get the Doxia sink");
        }
    
        String projectName = this.mojo.getProjectName();
        String projectVersion = this.mojo.getProjectVersion();
        Integer rulesCount = this.ruleExecutor.getRuleCount();

        // Page title
        mainSink.head();
        mainSink.title();
        mainSink.sectionTitle1();
        mainSink.text("AVIO Mule linter execution results for  " + projectName + ":" + projectVersion);
        mainSink.sectionTitle1_();
        mainSink.title_();
        mainSink.head_();

        mainSink.body();
        
        // Heading 1
        mainSink.section1();
        mainSink.sectionTitle1();
        mainSink.text("AVIO Mule linter execution results for  " + projectName + ":" + projectVersion);
        mainSink.sectionTitle1_();
        mainSink.sectionTitle2();
        mainSink.text(String.format("%s rules executed.", rulesCount));
        mainSink.sectionTitle2_();


        final SinkEventAttributes tableAtt = new SinkEventAttributeSet();
        tableAtt.addAttribute(SinkEventAttributes.WIDTH, "%90");

        final SinkEventAttributes severityTextAtt = new SinkEventAttributeSet();
        severityTextAtt.addAttribute(SinkEventAttributes.WIDTH, "150px");
        severityTextAtt.addAttribute(SinkEventAttributes.ALIGN, "left");
        severityTextAtt.addAttribute(SinkEventAttributes.BGCOLOR, "#ADADAD");

        final SinkEventAttributes headerTextAtt = new SinkEventAttributeSet();
        headerTextAtt.addAttribute(SinkEventAttributes.WIDTH, "250px");
        headerTextAtt.addAttribute(SinkEventAttributes.ALIGN, "left");
        headerTextAtt.addAttribute(SinkEventAttributes.BGCOLOR, "#ADADAD");

        final SinkEventAttributes nameTextAtt = new SinkEventAttributeSet();
        nameTextAtt.addAttribute(SinkEventAttributes.WIDTH, "400px");
        nameTextAtt.addAttribute(SinkEventAttributes.ALIGN, "left");
        nameTextAtt.addAttribute(SinkEventAttributes.BGCOLOR, "#ADADAD");

        final SinkEventAttributes messageTextAtt = new SinkEventAttributeSet();
        messageTextAtt.addAttribute(SinkEventAttributes.WIDTH, "400px");
        messageTextAtt.addAttribute(SinkEventAttributes.ALIGN, "left");
        messageTextAtt.addAttribute(SinkEventAttributes.BGCOLOR, "#ADADAD");



        // Content
        mainSink.paragraph();
        mainSink.text("Rule validation results: ");
        mainSink.table(tableAtt);
        mainSink.tableRow();
        mainSink.tableHeaderCell(severityTextAtt);
        mainSink.text("Severity");
        mainSink.tableHeaderCell_();
        mainSink.tableHeaderCell(headerTextAtt);
        mainSink.text("Id");
        mainSink.tableHeaderCell_();
        mainSink.tableHeaderCell(nameTextAtt);
        mainSink.text("Name");
        mainSink.tableHeaderCell_();
        mainSink.tableHeaderCell(headerTextAtt);
        mainSink.text("File:Line");
        mainSink.tableHeaderCell_();
        mainSink.tableHeaderCell(messageTextAtt);
        mainSink.text("Message");
        mainSink.tableHeaderCell_();
        mainSink.tableRow_();

        final SinkEventAttributes rowPairAtt = new SinkEventAttributeSet();
        rowPairAtt.addAttribute(SinkEventAttributes.BGCOLOR, "##d89696");
        final SinkEventAttributes rowOddAtt = new SinkEventAttributeSet();
        rowOddAtt.addAttribute(SinkEventAttributes.BGCOLOR, "#eabc95");
        final SinkEventAttributes currentRowAtt = rowPairAtt;

        this.ruleExecutor.getResults().forEach( violation ->{
                    com.avioconsulting.mule.linter.model.rule.Rule rule = violation.getRule();
                    String ruleSeverity = rule.getSeverity().toString();
                    String ruleId = rule.getRuleId();
                    String ruleName = rule.getRuleName();
                    StringBuilder messageLogBuilder = new StringBuilder();
                    mainSink.tableRow( (currentRowAtt==rowPairAtt)?rowOddAtt:rowOddAtt );

                    for( String el : Arrays.asList(ruleSeverity, ruleId, ruleName) )
                    {
                        mainSink.tableCell();
                        mainSink.paragraph();
                        mainSink.text(el);
                        mainSink.paragraph_();
                        mainSink.tableCell_();
                    }
                    String fileLine = violation.getFileName();
                    if (violation.getLineNumber()>0 ){
                        fileLine+=":"+violation.getLineNumber().toString();
                    }
                    mainSink.tableCell();
                    mainSink.paragraph();
                    mainSink.text(fileLine);
                    mainSink.paragraph_();
                    mainSink.tableCell_();

                    mainSink.tableCell();
                    mainSink.paragraph();
                    mainSink.text(violation.getMessage() );
                    mainSink.paragraph_();
                    mainSink.tableCell_();

                    mainSink.tableRow_();
                }
        );
        mainSink.table_();

        mainSink.section2_();
        
        mainSink.paragraph_();

        // Close
        mainSink.section1_();


        mainSink.body_();

    }
}
