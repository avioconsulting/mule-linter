package com.avioconsulting.mule.linter.model.rule

import com.avioconsulting.mule.linter.model.Application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject

import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

class RuleExecutor {

    List<RuleSet> rules
    Application application
    List<RuleViolation> results = []
    Integer ruleCount = 0

    RuleExecutor(Application application, List<RuleSet> rules) {
        this.rules = rules
        this.application = application
    }

    void executeRules() {
        rules.each { ruleSet ->
            ruleSet.getRules().each { // assigns current rule to 'it'
                results.addAll(it.execute(application))
                ruleCount++
            }
        }
    }

    void displayResults(OutputStream outputStream) {
        outputStream.write("$ruleCount rules executed.\n".bytes)
        outputStream.write('Rule Results\n'.bytes)

        results.each { violation ->
            outputStream.write("    [$violation.rule.severity] $violation.rule.ruleId - $violation.fileName ".bytes)
            outputStream.write((violation.lineNumber > 0 ? "( $violation.lineNumber ) " : '').bytes)
            outputStream.write("$violation.message \n".bytes)
        }
        outputStream.write("\nFound a total of $results.size violations.\n".bytes)
        outputStream.flush()
        outputStream.close()
    }

    void displayResults(outputFormat,OutputStream outputStream) {
        if(outputFormat == 'json'){
            outputStream.write("{\n".bytes)
            outputStream.write("   rulesExecuted:$ruleCount,\n".bytes)

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(results)
            outputStream.write(("   ruleViolations:"+json+",\n").bytes )

                    outputStream.write("   totalViolations:$results.size\n".bytes)
            outputStream.write("}".bytes)
        }
        else if(outputFormat == 'xml')
        {  final StringBuilder builder = new StringBuilder();
            results.each { violation ->
                String json = new Gson().toJson(violation);
                JsonObject jsonObject = new JsonObject(json);
                String xml =  "<"+'rules'+">" + XML.toString(jsonObject) + "</"+'rules'+">"
                builder.append(xml.bytes)
            }
            String concatenatedString = builder.toString();
            String xmlString = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n<"+'root'+">" + concatenatedString + "</"+'root'+">";
            String xmlOutput = convertToXML(xmlString);
            outputStream.write(xmlOutput)
        }
        else {
            outputStream.write("$ruleCount rules executed.\n".bytes)
            outputStream.write('Rule Results\n'.bytes)

            results.each { violation ->
                outputStream.write("    [$violation.rule.severity] $violation.rule.ruleId - $violation.fileName ".bytes)
                outputStream.write((violation.lineNumber > 0 ? "( $violation.lineNumber ) " : '').bytes)
                outputStream.write("$violation.message \n".bytes)
            }
            outputStream.write("\nFound a total of $results.size violations.\n".bytes)
        }
        outputStream.flush()
        outputStream.close()
    }

    String convertToXML(String xml){
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 2);
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter stringWriter = new StringWriter();
        StreamResult xmlOutput = new StreamResult(stringWriter);
        Source xmlInput = new StreamSource(new StringReader(xml));
        transformer.transform(xmlInput, xmlOutput);
        return xmlOutput.getWriter().toString();
    }

}
