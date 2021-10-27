package com.avioconsulting.mule.linter.model

class GitIgnoreFile extends ProjectFile {

    public static final String GITIGNORE = '.gitignore'
    private final Boolean exists
    private final List<String> expressions = []

    GitIgnoreFile(File file) {
        super(file)
        exists = file.exists()
        loadExpressions()
    }

    GitIgnoreFile(File application, String fileName) {
        this(new File(application, fileName))
    }

    Boolean doesExist() {
        return exists
    }

    void loadExpressions() {
        expressions.clear()
        if (exists) {
            file.eachLine { line ->
                if (!line.empty && !line.startsWith('#')) {
                    expressions.add(line.trim())
                }
            }
        }
    }

    Boolean contains(String expression) {
        return expressions.contains(expression)
    }

}
