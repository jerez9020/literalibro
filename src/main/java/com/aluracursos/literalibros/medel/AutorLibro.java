package com.aluracursos.literalibros.medel;

import java.util.Objects;

public class AutorLibro {
    public String autor;
    public Libro libro;

    public AutorLibro(String autor, Libro libro) {
        this.autor = autor;
        this.libro = libro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutorLibro that = (AutorLibro) o;
        return Objects.equals(autor, that.autor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(autor);
    }
}