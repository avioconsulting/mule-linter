package com.avioconsulting.mule.linter.model.configuration

class ComponentIdentifier {
    final String name
    final String namespaceURI

    ComponentIdentifier(String name, String namespaceURI) {
        Objects.requireNonNull(name, "name cannot be null")
        Objects.requireNonNull(namespaceURI, "namespaceURI cannot be null")
        this.name = name
        this.namespaceURI = namespaceURI
    }


    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ComponentIdentifier that = (ComponentIdentifier) o

        if (name != that.name) return false
        if (namespaceURI != that.namespaceURI) return false

        return true
    }

    int hashCode() {
        int result
        result = name.hashCode()
        result = 31 * result + namespaceURI.hashCode()
        return result
    }

    @Override
    String toString() {
        return String.format("%s:%s", name, namespaceURI);
    }
}
