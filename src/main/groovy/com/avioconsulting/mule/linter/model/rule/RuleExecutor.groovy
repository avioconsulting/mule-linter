package com.avioconsulting.mule.linter.model.rule

import com.avioconsulting.mule.linter.model.Application

import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import com.google.gson.*
import org.json.*;
import javax.xml.transform.*
import javax.xml.transform.stream.*

class RuleExecutor {

    List<RuleSet> rules
    Application application
    List<RuleViolation> results = []
    private Integer ruleCount = 0

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

    void displayResults(outputFormat,OutputStream outputStream) {
        outputStream.write("$ruleCount rules executed.\n".bytes)
        outputStream.write('Rule Results\n'.bytes)
        if(outputFormat == 'json'){
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json1 = gson.toJson(results)
            outputStream.write(json1)
            //String message;
            JSONObject json = new JSONObject();

            JSONArray array = new JSONArray();

            json.put("issues", array);
            JSONObject objtobeadded =  new JSONObject();
            JsonObject innerObject = new JsonObject();

            results.each { violation ->

                objtobeadded.put("ruleId", violation.rule.ruleId);
                objtobeadded.put("engineId", violation.rule.ruleName);
                objtobeadded.put("severity", violation.rule.severity);
                objtobeadded.put("type", violation.rule.severity);
                innerObject.addProperty("filePath", violation.fileName);
                innerObject.addProperty("message", violation.message);
                objtobeadded.put("primaryLocation", innerObject);
                array.put(objtobeadded)
                System.out.println(objtobeadded);

//                item.put("ruleId", violation.rule.ruleId);
//                item.put("engineId", violation.rule.ruleName)
//                item.put("severity", violation.rule.severity)
//                item.put("type", violation.rule.severity)
//                innerObject.addProperty("filePath", violation.fileName);
//                innerObject.addProperty("message", violation.message);
//                //innerObject.addProperty("filePath", "violation.fileName");
//                jsonObject.add("primaryLocation", innerObject);
//                item.put("primaryLocation",innerObject)
//                def builder = new JsonBuilder()
//                def data = builder {
//                    "ruleId" violation.rule.ruleId
//                    "engineId" violation.rule.ruleName
//                    "severity" violation.rule.severity
//                    "type" "CODE_SMELL"
//                    "primaryLocation" {
//                        "filePath" violation.fileName
//                        //"message" violation.message
//                        "textRange" {"startLine" violation.lineNumber}
//                    }
//                }

//                appendToList(jsonObj, data);
                //outputStream.write(data)
                //outputStream.write(builder.toPrettyString())

//                item.add(builder.toPrettyString())
//                ArrayUtils.add(item, builder.toPrettyString())
                //outputStream.write(json)
//                JsonObject inputObj  = gson.fromJson(json, JsonObject.class);
//                JsonObject newObject = new JsonObject() ;
//                newObject.addProperty("ruleId" ,"violation.rule.ruleId");
//                newObject.addProperty("engineId", "violation.rule.ruleName");
//                inputObj.get("results").getAsJsonArray().add(newObject);
//                message = json.toString();
//                System.out.println(message);
//                System.out.println(builder);
//                System.out.println(builder.toPrettyString());
//                builder.eachWithIndex{ JsonBuilder entry, int i ->
//                    array.put(entry.getAt("content").collect());
//                }
                //array.put(builder.toPrettyString())
                // outputStream.write(json)
//                String json1 = gson.toJson(json.toString())
////                outputStream.write(json1)
//                System.out.println(array.myArrayList);
                //System.out.println(json.toString());
                //outputStream(json1)
            }
            //Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJsonString = gson.toJson(json);
            System.out.println(prettyJsonString);
            File file = new File('/Users/anusha.konakalla/Desktop/sample.json')
            file.write(prettyJsonString)

        }
        else if(outputFormat == 'xml')
        {  final StringBuilder builder = new StringBuilder();
            results.each { violation ->
                String json = new Gson().toJson(violation);
                JSONObject jsonObject = new JSONObject(json);
                String xml =  "<"+'rules'+">" + XML.toString(jsonObject) + "</"+'rules'+">"
                builder.append(xml + "")
            }
            String concatenatedString = builder.toString();
            String xmlString = "<?xml version=\"1.0\" encoding=\"ISO-8859-15\"?>\n<"+'root'+">" + concatenatedString + "</"+'root'+">";
            String xmlOutput = convertToXML(xmlString);
            outputStream.write(xmlOutput)

        }
        else
            results.each { violation ->
                outputStream.write("    [$violation.rule.severity] $violation.rule.ruleId - $violation.fileName ".bytes)
                outputStream.write((violation.lineNumber > 0 ? "( $violation.lineNumber ) " : '').bytes)
                outputStream.write("$violation.message \n".bytes)
            }

        outputStream.write("\nFound a total of $results.size violations.\n".bytes)
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
