package com.aluracursos.literalibros.principal;

import com.aluracursos.literalibros.medel.*;
import com.aluracursos.literalibros.repository.LIbroRepository;
import com.aluracursos.literalibros.service.ConsumoAPI;
import com.aluracursos.literalibros.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Stream;

public class Principal {
    Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private static final String URL_BASE = "http://gutendex.com/books/?search=";
    private LIbroRepository repositorio;

    public Principal(LIbroRepository repository) {
        this.repositorio = repository;
    }

    //menu
    public void muestraElMenu(){
        var opcion = -1;
        while (opcion != 0)
        {
            System.out.println("\n***************MENU***************");
            System.out.println("\nElija opcion atraves de su numero");
            var menu = """
                1 - Buscar libro por titulo
                2 - Listar libros registrados
                3 - Listar autores registrados
                4 - Listar autores vivos en un determinado año
                5 - Listar libros por idioma
                0 - Salir
                """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();
            switch (opcion)
            {
                case 1:
                    buscarLibroWeb();
                    break;
                case 2:
                    librosGuardados();
                    break;
                case 3:
                    autoresRegistrados();
                    break;
                case 4:
                    autoresVivosRegistrados();
                    break;
                case 5:
                    librosPorIdioma();
                    break;
                case 0:
                    System.out.println("Usted Salio del programa, cerrando la aplicacion ...\n");
                    break;
                default:
                    System.out.println("Opcion invalida.\n");
                    break;
            }

        }

    }
    private void getDatosLibros() {
        System.out.println("Ingrese el título del libro que desea buscar:");
        String tituloLibro = teclado.nextLine();


        List<Libro> librosExistentes = repositorio.findByTituloContainingIgnoreCase(tituloLibro);
        if (!librosExistentes.isEmpty()) {
            System.out.println("Se encontraron los siguientes  con el título ingresado:");
            librosExistentes.forEach(libro -> System.out.println(" - " + libro.getTitulo()));
            System.out.println("El libro ya está registrado en la base de datos. Por favor, ingrese un título diferente.");
            return;
        }

        // Obtener datos del libro desde la API
        String json = consumoApi.obtenerDatos(URL_BASE + tituloLibro.replace(" ","+"));
        Datos datoBusqueda = conversor.obtenerDatos(json, Datos.class);

        // Verificación del libro presente
        System.out.println("\n********** LIBRO **********\n");
        if (datoBusqueda != null && datoBusqueda.resultados() != null && !datoBusqueda.resultados().isEmpty()) {
            DatosLibros datosLibros = datoBusqueda.resultados().get(0);

            System.out.println("Título: " + datosLibros.titulo());
            List<DatosAutor> datosAutores = datosLibros.autor();
            datosAutores.forEach(autor -> System.out.println("Autor: " + autor.nombre()));
            List<String> idiomas = datosLibros.idiomas();
            idiomas.forEach(idioma -> System.out.println("Idioma: " + idioma));
            System.out.println("Número de Descargas: " + datosLibros.descargas());

            // Guardar los datos en la base de datos
            Libro libro = new Libro();
            libro.setTitulo(datosLibros.titulo());
            libro.setAutor(datosAutores.stream().map(DatosAutor::nombre).reduce((a1, a2) -> a1 + ", " + a2).orElse(""));
            libro.setFechaDeNacimiento(datosAutores.stream().map(DatosAutor::fechaDeNacimiento).reduce((a1, a2) -> a1 + ", " + a2).orElse(""));
            libro.setFechaDeFallecimiento(datosAutores.stream().map(DatosAutor::fechaDeFallecimiento).reduce((a1, a2) -> a1 + ", " + a2).orElse(""));
            libro.setIdiomas(idiomas.stream().reduce((i1, i2) -> i1 + ", " + i2).orElse(""));
            libro.setDescargas(datosLibros.descargas());

            repositorio.save(libro);
        } else {
            System.out.println("\nLibro no encontrado.");
        }
    }


    public void buscarLibroWeb(){

        getDatosLibros();

    }

    //libros guardados
    public void librosGuardados() {
        List<Libro> libros = repositorio.findAll();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados.");
        } else {
            System.out.println("\n********** LIBROS GUARDADOS **********\n");
            for (Libro libro : libros) {
                System.out.println("Titulo: " + libro.getTitulo());
                System.out.println("Autor: " + libro.getAutor());
                System.out.println("Idiomas: " + libro.getIdiomas());
                System.out.println("Numero de Descargas: " + libro.getDescargas());
                System.out.println("-------------------------------------------------");
            }
        }
    }

    //autores registrados
    public void autoresRegistrados() {
        List<Libro> libros = repositorio.findAll();
        if (libros.isEmpty()) {
            System.out.println("\nNo hay libros registrados, por lo tanto no hay autores.");
        } else {
            System.out.println("\n********** AUTORES REGISTRADOS **********\n");

            libros.stream()
                    .flatMap(libro -> Stream.of(libro.getAutor())
                            .map(autor -> new AutorLibro(autor.trim(), libro)))
                    .distinct()
                    .forEach(autorLibro -> {
                        System.out.println("Autor: " + autorLibro.autor);
                        System.out.println("Fecha de Nacimiento: " + autorLibro.libro.getFechaDeNacimiento());
                        System.out.println("Fecha de Fallecimiento: " + autorLibro.libro.getFechaDeFallecimiento());
                        System.out.println("Libro: " + autorLibro.libro.getTitulo());
                        System.out.println("-------------------------------------------------");
                    });
        }
    }

    public void autoresVivosRegistrados() {
        System.out.println("Ingrese el año que desea buscar:");
        int ano = teclado.nextInt();
        teclado.nextLine();

        List<Libro> libros = repositorio.findAll();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados");
        } else {
            System.out.println("\n********** AUTORES VIVOS EN EL AÑO " + ano + " **********\n");

            libros.stream()
                    .flatMap(libro -> Stream.of(libro.getAutor())
                            .map(autor -> new AutorLibro(autor.trim(), libro)))
                    .filter(dato -> {
                        try {
                            int nacimiento = Integer.parseInt(dato.libro.getFechaDeNacimiento());
                            String fallecimientoStr = dato.libro.getFechaDeFallecimiento();
                            int fallecimiento = fallecimientoStr != null && !fallecimientoStr.isEmpty() ? Integer.parseInt(fallecimientoStr) : Integer.MAX_VALUE;
                            return nacimiento <= ano && ano <= fallecimiento;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .distinct()
                    .forEach(dato -> {
                        System.out.println("Autor: " + dato.autor);
                        System.out.println("Fecha de Nacimiento: " + dato.libro.getFechaDeNacimiento());
                        System.out.println("Fecha de Fallecimiento: " + (dato.libro.getFechaDeFallecimiento() != null ? dato.libro.getFechaDeFallecimiento() : "N/A"));
                        System.out.println("Libro: " + dato.libro.getTitulo());
                        System.out.println("-------------------------------------------------");
                    });
        }

    }

    public void librosPorIdioma() {

        System.out.println("Seleccione el idioma:");
        System.out.println("1. Español");
        System.out.println("2. Portugués");
        System.out.println("3. Francés");
        System.out.println("4. Inglés");
        System.out.print("Opción: ");
        int opcionIdioma = teclado.nextInt();
        teclado.nextLine(); // Limpiar el buffer de entrada

        String idiomaSeleccionado = "";
        switch (opcionIdioma) {
            case 1:
                idiomaSeleccionado = "es";
                break;
            case 2:
                idiomaSeleccionado = "pt";
                break;
            case 3:
                idiomaSeleccionado = "fr";
                break;
            case 4:
                idiomaSeleccionado = "en";
                break;
            default:
                System.out.println("Opción no válida.");
                return;
        }

        // Obtener libros por idioma
        List<Libro> librosPorIdioma = repositorio.findAll();
        List<Libro> librosFiltrados = new ArrayList<>();

        //  libros por idioma
        for (Libro libro : librosPorIdioma) {
            if (libro.getIdiomas().toLowerCase().contains(idiomaSeleccionado.toLowerCase())) {
                librosFiltrados.add(libro);
            }
        }

        // Mostrar libro del  idioma seleccionado
        if (librosFiltrados.isEmpty()) {
            System.out.println("\NNo se encontraron libros en el idioma seleccionado.");
        } else {
            System.out.println("\n********** LIBROS EN " + idiomaSeleccionado.toUpperCase() + " **********\n");
            for (Libro libro : librosFiltrados) {
                System.out.println("Título: " + libro.getTitulo());
                System.out.println("Autores: " + libro.getAutor());
                System.out.println("Idiomas: " + libro.getIdiomas());
                System.out.println("Número de Descargas: " + libro.getDescargas());
                System.out.println("-------------------------------------------------");
            }
        }
    }

}
