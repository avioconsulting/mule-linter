package com.avioconsulting.mule.linter.model

class ReadmeFile extends ProjectFile {

    public static final String README = 'README.md'
    private final Boolean exists
    private final List<String> content = []

    ReadmeFile(File file) {
        super(file)
        exists = file.exists()
        loadContent()
    }

    ReadmeFile(File application, String fileName) {
        this(new File(application, fileName))
    }

    void loadContent() {
        content.clear()
        if (exists) {
            file.eachLine { line ->
                if (!line.empty) {
                    content.add(line.trim())
                }
            }
        }
    }

    Boolean doesExist() {
        return exists
    }

    int getSize() {
        content.size()
    }
}
