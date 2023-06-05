println 'Generating GDSL file'
def outputPath = "./src/main/resources/mule-linter-core.gdsl"
def templateFileName = "./mule-linter-core.gdsl.vm"
com.avioconsulting.mule.linter.dsl.GDSLGenerator gdslGenerator = new com.avioconsulting.mule.linter.dsl.GDSLGenerator();
gdslGenerator.generate(templateFileName, outputPath )