import com.avioconsulting.mule.linter.dsl.GDSLGenerator

println 'Generating GDSL file'
def outputPath = "./build/classes/groovy/main/mule-linter-core.gdsl"
def templateFileName = "./mule-linter-core.gdsl.vm"
GDSLGenerator gdslGenerator = new GDSLGenerator()
gdslGenerator.generate(templateFileName, outputPath )