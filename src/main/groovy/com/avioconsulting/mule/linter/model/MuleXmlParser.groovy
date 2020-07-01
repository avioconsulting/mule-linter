package com.avioconsulting.mule.linter.model

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import groovy.xml.slurpersupport.NodeChildren
import org.xml.sax.Attributes
import org.xml.sax.Locator
import org.xml.sax.SAXException
import org.xml.sax.ext.Attributes2Impl

import javax.xml.parsers.ParserConfigurationException

class MuleXmlParser extends XmlSlurper {

    public static final String START_LINE_NO_ATTRIBUTE = '_startLineNo'
    public static final String START_LINE_NO_NAMESPACE = 'http://www.avioconsulting.com/mule/linter'
    public static final String START_LINE_NO_NAMESPACE_PREFIX = 'mule-lint'
    Locator locator


    MuleXmlParser() throws ParserConfigurationException, SAXException {
        super()
        this.startPrefixMapping(START_LINE_NO_NAMESPACE_PREFIX, START_LINE_NO_NAMESPACE)
    }

    @Override
    void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    @Override
    void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        Attributes2Impl newAttrs = new Attributes2Impl(attrs);
        newAttrs.addAttribute(START_LINE_NO_NAMESPACE, START_LINE_NO_ATTRIBUTE, null, 'ENTITY',  String.valueOf(locator.lineNumber))
        super.startElement(uri, localName, qName, newAttrs);
    }

    Integer getNodeLineNumber(GPathResult node) {
        return Integer.valueOf(String.valueOf(node["@${START_LINE_NO_NAMESPACE_PREFIX}:${START_LINE_NO_ATTRIBUTE}"]))
    }

}
