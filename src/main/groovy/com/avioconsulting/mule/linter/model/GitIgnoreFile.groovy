package com.avioconsulting.mule.linter.model

class GitIgnoreFile extends ProjectFile {

    private final Boolean exists

    GitIgnoreFile(File file) {
        super(file)
        if (file.exists()) {
            exists = true
        } else {
            exists = false
        }
    }

    GitIgnoreFile(File application, String fileName) {
        this(new File(application, fileName))
    }

    Boolean doesExist() {
        return exists
    }

    String[] checkUntrackedFiles(String[] untrackedFiles) {
        List<String> listOfFiles = untrackedFiles
        List<String> found =  []

        this.file.eachLine {
            fileLine -> listOfFiles.each {
                    untrackFile ->
                if (fileLine.startsWithIgnoreCase(untrackFile)) {
                    found.add(untrackFile)
                }
            }
        }

        listOfFiles.removeAll(found)

        return listOfFiles
    }
}
