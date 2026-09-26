package com.graphics;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class AppCiudad {
    private long ventana;
    private int programa;
    private int vao;
    private int vbo;
    private int ancho = 1100;
    private int alto = 760;

    // Angulo inicial de la camara de la ciudad
    private float orbita = 0.6f;

    // Cambiar el angulo o la vista del mapa
    private boolean vistaMapa = false;

    // Distancia del origen a cada borde
    private static final float LIMITE = 35;

    // Ancho y profundidad de cada celda del mapa
    private static final float CELDA = 10;

    // Zoom de la camara: valores mayores acercan la vista a la ciudad
    private float zoom = 1.0f;
    private static final float ZOOM_MIN = 0.4f;
    private static final float ZOOM_MAX = 3.0f;
    private static final float VELOCIDAD_ZOOM = 1.25f;

    // Altura de la camara sobre la ciudad, controlada con arriba/abajo
    private float alturaCamara = 55;
    private static final float ALTURA_MIN = 15;
    private static final float ALTURA_MAX = 120;
    private static final float VELOCIDAD_ALTURA = 25f;

    // Velocidad angular de la orbita (radianes por segundo)
    private static final float VELOCIDAD_ORBITA = 1.0f;

    // Posicion en X del auto que recorre la calle central; reaparece al llegar al borde
    private float autoPosicion = -LIMITE;
    private static final float VELOCIDAD_AUTO = 6f;

    private final Map<String, Integer> uniforms = new HashMap<>();

    private static final int[][] MAPA = {
            { 0, 0, 0, 0, 0, 0, 0 },
            { 0, 1, 0, 1, 0, 1, 0 },
            { 0, 0, 0, 0, 0, 0, 0 },
            { 0, 2, 0, 1, 0, 1, 0 },
            { 0, 0, 0, 0, 0, 0, 0 },
            { 0, 1, 0, 3, 0, 1, 0 },
            { 0, 0, 0, 0, 0, 0, 0 }

    };

    // Organizar las tres fases de la aplicación y limpiar en caso ocurra un error
    public void run() {
        // Preparamos mensajes de error en la consola
        GLFWErrorCallback errores = GLFWErrorCallback.createPrint(System.err);
        // Instamos el manejador de errores en GLFW
        errores.set();
        try {
            iniciar();
            loop();
        } finally {
            limpiar();
            glfwSetErrorCallback(null);
            errores.free();
        }
    }

    // Configurar GLFW y OpenGL igual que las clases de App
    private void iniciar() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }

        // Reestablece todas las opciones por defecto
        GLFW.glfwDefaultWindowHints();

        // Ocultar la ventana al principio; la mostrar recien cuando OpenGL este listo
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        // Permitir que el usuario pueda redimensionar la pantalla
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        // Queremos que use la version de OpenGL 3.3
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        // Solo funciones modernas de OpenGL (evitar compatibilidad con codigo muy
        // antiguo)
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        // MacOS hay que reactivar para usar el core profile
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        // Creamos la ventana: ancho, alto, tiitulo, monitor(0 = ventana normal),
        // ventana para compartir
        ventana = GLFW.glfwCreateWindow(ancho, alto,
                "Ciudad | Izq/Der: orbitar | Arriba/Abajo: altura | Rueda o +/-: zoom | M: minimapa | Esc: salir", 0,
                0);

        // Decir que todas las ordenes de OPENGL que ejecutamos a partir de ahora
        // se aplique a esta venta
        GLFW.glfwMakeContextCurrent(ventana);

        // Sincronizar el dibujo con el monitor (1 = esperar al siguiente refresco)
        // Evitamos el "tearing" (imagen partida)
        GLFW.glfwSwapInterval(1);
        // hacer visible la ventana
        GLFW.glfwShowWindow(ventana);

        // Carga en java las funciones de OpenGl
        // LWJGL necesita este paso para enlazar las llamadas de Java con la GPU
        GL.createCapabilities();

        // Hace que las superficie cercana oculten a las lejanas
        glEnable(GL_DEPTH_TEST);

        // Registramos la funcion del evento del teclado para cerrar la ventana
        glfwSetKeyCallback(ventana, (ventanaEvento, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                tecla(key);
            }
        });

        // Registramos la rueda del mouse para controlar el zoom de la camara
        glfwSetScrollCallback(ventana, (ventanaEvento, xoffset, yoffset) -> {
            zoom += (float) yoffset * 0.10f;
            zoom = clamp(zoom, ZOOM_MIN, ZOOM_MAX);
        });

        crearPrograma();
        crearCubo();
    }

    // Compila los shaders, los enlaza en un programa y libera los objetos temporales
    private void crearPrograma() {
        int vertice = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertice, vertexShader());
        glCompileShader(vertice);
        comprobarShader(vertice, "Vertex");

        int fragmento = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmento, fragmentShader());
        glCompileShader(fragmento);
        comprobarShader(fragmento, "Fragment");

        programa = glCreateProgram();
        glAttachShader(programa, vertice);
        glAttachShader(programa, fragmento);
        glLinkProgram(programa);

        if (glGetProgrami(programa, GL_LINK_STATUS) == GL_FALSE) {
            throw new IllegalStateException("Error al enlazar programa: " + glGetProgramInfoLog(programa));
        }

        // Los shaders ya quedaron copiados dentro del programa, no hacen falta mas
        glDeleteShader(vertice);
        glDeleteShader(fragmento);
    }

    private void comprobarShader(int shader, String tipo) {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new IllegalStateException(tipo + " shader: " + glGetShaderInfoLog(shader));
        }
    }

    // Genera un cubo unitario (centrado en el origen, lado 1) con su normal por cada cara
    private void crearCubo() {
        float[] vertices = {
                // posicion            // normal
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
                 0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
                 0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
                 0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,

                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
                 0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
                 0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
                 0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,

                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
                -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,
                -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,
                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,

                 0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
                 0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
                 0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
                 0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,
                 0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,
                 0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
                 0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,
                 0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
                 0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,

                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
                 0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
                 0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
                 0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,
        };

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        int stride = 6 * Float.BYTES;
        // Posicion: 3 floats en el atributo 0
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        // Normal: 3 floats en el atributo 1, despues de la posicion
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void loop() {
        // Guardamos el instante inicial para calcular el tiempo entre cuadros
        double tiempoAnterior = glfwGetTime();
        // Cuenta los cuadros dibujados para la comprobación
        int cuadros = 0;
        // Cero permite jugar; otro valor limita el arranque de prueba
        int maxCuadros = Integer.getInteger("demo.frames", 0);
        // Reserva espacio para que GLFW escriba el ancho en pixeles
        int[] anchoReal = new int[1];
        // Reserva espacio para qie GLFW escriba el alto en pixeles
        int[] altoReal = new int[1];

        while (!glfwWindowShouldClose(ventana)) {
            // Procesa el teclado, redimensionamiento y boton de cierre
            glfwPollEvents();
            // Consulta el tiempo actual en segundos
            double tiempoActual = glfwGetTime();
            float deltaTime = (float) Math.min(tiempoActual - tiempoAnterior, 0.05);
            tiempoAnterior = tiempoActual;
            procesarInput(deltaTime);
            glfwGetFramebufferSize(ventana, anchoReal, altoReal);
            ancho = anchoReal[0];
            alto = altoReal[0];
            if (ancho > 0 && alto > 0) {
                dibujarFrame();
            }
            glfwSwapBuffers(ventana);
            cuadros++;
            if (maxCuadros > 0 && cuadros >= maxCuadros) {
                glfwSetWindowShouldClose(ventana, true);
            }
        }
    }

    private void limpiar() {
        if (programa != 0) {
            glDeleteProgram(programa);
        }
        if (vbo != 0) {
            glDeleteBuffers(vbo);
        }
        if (vao != 0) {
            glDeleteVertexArrays(vao);
        }
        if (ventana != 0) {
            Callbacks.glfwFreeCallbacks(ventana);
            glfwDestroyWindow(ventana);
        }
        glfwTerminate();
    }

    // ===================== Teclas y camaras ===================

    /** Atiende las acciones de una sola pulsación */
    private void tecla(int key) {
        if (key == GLFW_KEY_ESCAPE) {
            glfwSetWindowShouldClose(ventana, true);
        }
        if (key == GLFW_KEY_M) {
            vistaMapa = !vistaMapa; // Alterna entre la vista 3D y el minimapa
        }
    }

    private boolean pulsada(int key) {
        return glfwGetKey(ventana, key) == GLFW_PRESS;
    }

    private float clamp(float valor, float min, float max) {
        return Math.max(min, Math.min(max, valor));
    }

    /** Mueve y hace zoom con la camara alrededor de la ciudad usando el teclado */
    private void procesarInput(float deltaTime) {
        float pasoOrbita = VELOCIDAD_ORBITA * deltaTime;
        if (pulsada(GLFW_KEY_LEFT)) {
            orbita -= pasoOrbita; // Reducimos el angulo
        }

        if (pulsada(GLFW_KEY_RIGHT)) {
            orbita += pasoOrbita; // Aumentamos el angulo
        }

        float pasoAltura = VELOCIDAD_ALTURA * deltaTime;
        if (pulsada(GLFW_KEY_UP)) {
            alturaCamara += pasoAltura; // Subimos la camara
        }
        if (pulsada(GLFW_KEY_DOWN)) {
            alturaCamara -= pasoAltura; // Bajamos la camara
        }
        alturaCamara = clamp(alturaCamara, ALTURA_MIN, ALTURA_MAX);

        float pasoZoom = VELOCIDAD_ZOOM * deltaTime;
        if (pulsada(GLFW_KEY_KP_ADD) || pulsada(GLFW_KEY_EQUAL)) {
            zoom += pasoZoom;
        }
        if (pulsada(GLFW_KEY_KP_SUBTRACT) || pulsada(GLFW_KEY_MINUS)) {
            zoom -= pasoZoom;
        }
        zoom = clamp(zoom, ZOOM_MIN, ZOOM_MAX);

        autoPosicion += VELOCIDAD_AUTO * deltaTime;
        if (autoPosicion > LIMITE) {
            autoPosicion = -LIMITE; // Reaparece en el extremo opuesto de la calle
        }
    }

    private void configurarCamara() {
        // Un zoom mayor acerca la camara: dividimos el radio base entre el zoom
        float radio = 65 / zoom;
        float camaraX = (float) Math.sin(orbita) * radio;
        float camaraZ = (float) Math.cos(orbita) * radio;

        vector("uOjo", camaraX, alturaCamara, camaraZ);
        vector("uObjetivo", 0, 0, 0);
        decimal("uAspecto", (float) ancho / alto);
    }

    // Envia una luz solar fija: direccion, color y un poco de luz ambiental
    private void prepararLuces() {
        vector("uLuzDir", 0.35f, 0.8f, 0.45f);
        vector("uLuzColor", 1.0f, 0.96f, 0.88f);
        vector("uAmbiente", 0.35f, 0.35f, 0.38f);
    }

    // ===================== Ciudad a partir de la matriz ===================
    private float centro(int indice) {
        return -LIMITE + CELDA * (indice + 0.5f);
    }

    private void dibujarFrame() {
        glViewport(0, 0, ancho, alto);
        glClearColor(0.12f, 0.20f, 0.30f, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(programa);
        glBindVertexArray(vao);
        entero("uMapa", vistaMapa ? 1 : 0);
        configurarCamara();
        prepararLuces();
        escena();
        if (glGetError() != GL_NO_ERROR) {
            throw new IllegalStateException("Error OpenGL al dibujar");
        }
    }

    private void escena() {
        caja(0, -0.25f, 0, 70, 0.5f, 70, 0.16f, 0.19f, 0.23f);
        for (int fila = 0; fila < MAPA.length; fila++) {
            for (int columna = 0; columna < MAPA[fila].length; columna++) {
                float x = centro(columna);
                float z = centro(fila);
                int tipo = MAPA[fila][columna];
                switch (tipo) {
                    case 0 -> dibujarMarcasCalle(fila, columna, x, z);
                    case 1 -> {
                        caja(x, 0.15f, z, 10, 0.3f, 10, 0.60f, 0.64f, 0.66f);
                        dibujarManzana(x, z, fila, columna);
                    }
                    case 2 -> {
                        caja(x, 0.15f, z, 10, 0.3f, 10, 0.60f, 0.64f, 0.66f);
                        dibujarParque(x, z);
                    }
                    case 3 -> {
                        caja(x, 0.15f, z, 10, 0.3f, 10, 0.60f, 0.64f, 0.66f);
                        dibujarEdificio(x, z, fila, columna);
                    }
                    default -> throw new IllegalStateException("Tipo de celda desconocido: " + tipo);
                }
            }
        }
        // El auto avanza sobre la calle horizontal de la fila 2 (todas sus celdas son calle)
        dibujarAuto(autoPosicion, centro(2));
    }

    // Manzana residencial: varias casas bajas en vez de una sola torre (celdas tipo 1)
    private void dibujarManzana(float x, float z, int fila, int columna) {
        float[] desplazamientoX = { -2.3f, 2.3f, -2.3f, 2.3f };
        float[] desplazamientoZ = { -2.3f, -2.3f, 2.3f, 2.3f };
        for (int i = 0; i < desplazamientoX.length; i++) {
            float altura = 2.5f + (fila + columna + i) % 3;
            float verde = 0.42f + i * 0.03f;
            caja(x + desplazamientoX[i], altura / 2 + 0.3f, z + desplazamientoZ[i], 3.6f, altura, 3.6f, 0.65f, verde,
                    0.45f);
            caja(x + desplazamientoX[i], altura + 0.45f, z + desplazamientoZ[i], 3.8f, 0.25f, 3.8f, 0.40f, 0.22f,
                    0.18f);
        }
    }

    // Parque con cesped y un par de personas paseando (celdas tipo 2)
    private void dibujarParque(float x, float z) {
        caja(x, 0.32f, z, 9, 0.1f, 9, 0.20f, 0.45f, 0.28f);
        dibujarPersona(x - 2.2f, z - 1.5f, 0.75f, 0.35f, 0.25f);
        dibujarPersona(x + 1.6f, z + 1.0f, 0.30f, 0.45f, 0.70f);
    }

    // Edificio alto tipo torre (celdas tipo 3)
    private void dibujarEdificio(float x, float z, int fila, int columna) {
        float altura = 5 + (fila * 3 + columna * 7) % 9;
        float rojo = 0.28f + columna * 0.045f;
        float azul = 0.48f + fila * 0.025f;
        caja(x, altura / 2 + 0.3f, z, 7, altura, 7, rojo, 0.40f, azul);
        caja(x, altura + 0.45f, z, 7.3f, 0.3f, 7.3f, 0.20f, 0.26f, 0.32f);
    }

    // Auto simple armado con cajas: carroceria, cabina y cuatro llantas
    private void dibujarAuto(float x, float z) {
        caja(x, 0.5f, z, 3.4f, 1.0f, 1.7f, 0.75f, 0.15f, 0.15f);
        caja(x - 0.3f, 1.15f, z, 1.6f, 0.6f, 1.5f, 0.80f, 0.85f, 0.90f);
        caja(x - 1.1f, 0.25f, z - 0.9f, 0.5f, 0.5f, 0.35f, 0.05f, 0.05f, 0.05f);
        caja(x - 1.1f, 0.25f, z + 0.9f, 0.5f, 0.5f, 0.35f, 0.05f, 0.05f, 0.05f);
        caja(x + 1.1f, 0.25f, z - 0.9f, 0.5f, 0.5f, 0.35f, 0.05f, 0.05f, 0.05f);
        caja(x + 1.1f, 0.25f, z + 0.9f, 0.5f, 0.5f, 0.35f, 0.05f, 0.05f, 0.05f);
    }

    // Persona simple armada con un cuerpo y una cabeza
    private void dibujarPersona(float x, float z, float r, float g, float b) {
        caja(x, 0.9f, z, 0.6f, 1.8f, 0.6f, r, g, b);
        caja(x, 2.05f, z, 0.5f, 0.5f, 0.5f, 0.92f, 0.78f, 0.65f);
    }

    private void dibujarMarcasCalle(int fila, int columna, float x, float z) {
        if (fila % 2 == 0 && columna % 2 == 1) {
            for (int desplazamiento = -3; desplazamiento <= 3; desplazamiento += 3) {
                caja(x + desplazamiento, 0.025f, z, 1.6f, 0.03f, 0.13f, 1, 0.84f, 0.35f);
            }
        }

        if (columna % 2 == 0 && fila % 2 == 1) {
            for (int desplazamiento = -3; desplazamiento <= 3; desplazamiento += 3) {
                caja(x, 0.025f, z + desplazamiento, 0.13f, 0.03f, 0.13f, 1, 0.84f, 0.35f);
            }
        }
    }

    // ===================== Dibujar cajas y enviar uniform ===================
    private void caja(float x, float y, float z, float sx, float sy, float sz, float r, float g, float b) {
        cajaGirada(x, y, z, sx, sy, sz, r, g, b, 0);
    }

    private void cajaGirada(float x, float y, float z, float sx, float sy, float sz, float r, float g, float b,
                            float angulo) {
        vector("uPos", x, y, z);
        vector("uEscala", sx, sy, sz);
        vector("uColor", r, g, b);
        decimal("uGiro", angulo);
        glDrawArrays(GL_TRIANGLES, 0, 36);
    }

    private int uniform(String nombre) {
        if (!uniforms.containsKey(nombre)) {
            int ubicacion = glGetUniformLocation(programa, nombre);
            uniforms.put(nombre, ubicacion);
        }
        return uniforms.get(nombre);
    }

    private void vector(String nombre, float x, float y, float z) {
        glUniform3f(uniform(nombre), x, y, z);
    }

    private void decimal(String nombre, float valor) {
        glUniform1f(uniform(nombre), valor);
    }

    private void entero(String nombre, int valor) {
        glUniform1i(uniform(nombre), valor);
    }

    // ===================== Shaders: Codigo que se ejecuta en la GPU
    // ===================
    private String vertexShader() {
        return """
                    #version 330 core // Seleccionamos GLSL 3.30
                    layout (location = 0) in vec3 aPos; // Leemos la posicion local del vertice desde el VBO
                    layout (location = 1)  in vec3 aNormal; // Leemos la normal de la cara desde el mismo VBO
                    uniform vec3 uPos; // Recibe el centro e la caja de la ciudad
                    uniform vec3 uEscala; // Recibe el tamaño de la caja en cada eje
                    uniform vec3 uOjo; // Recibe la posicion de la camara
                    uniform vec3 uObjetivo; // Punto que observa la camara
                    uniform float uGiro; // Recibe el giro del objeto alrededor de Y
                    uniform float uAspecto; // Recibe la relacion del ancho y alto de la imagen
                    uniform int uMapa; // Seleccionamos la perspectiva (0, 1)
                    out vec3 vMundo; // La posicion mundial al shader de fragmentos
                    out vec3 vNormal; // Enviamos la normal transformada par ala iluminacion

                    void main() {
                        float coseno = cos(uGiro); // Calculamos el coseno del giro del objeto
                        float seno = sin(uGiro);
                        mat3 giro = mat3(
                            coseno, 0.0,    -seno,
                            0.0,    1.0,    0.0,
                            seno,   0.0,    coseno
                        );
                        vMundo = giro * (aPos * uEscala) + uPos; // Escalar, firar y trasladar el vertice al mundo
                        vNormal = normalize(giro * (aNormal / uEscala)); // Corregimos la normal con la inversa transpuesta de la escala y giro

                        if (uMapa == 1) { //Esta rama se usa al dibujar el minimapa
                            float pantallaX = vMundo.x / 37.0;
                            float pantallaY = vMundo.z / 37.0;
                            float profundidad = -vMundo.y / 100.0;
                            gl_Position = vec4(pantallaX, pantallaY, profundidad, 1.0);
                        } else { // La escena principal utilizando la camara como perspectiva
                            vec3 frente = normalize(uObjetivo - uOjo); // Calcular la direccion hacia lo que mira la camara
                            vec3 derecha = normalize(cross(frente, vec3(0.0, 1.0, 0.0))); // Obtenemos el eje horizontal de la camra
                            vec3 arriba = cross(derecha, frente); // Obtenemos el eje vertical
                            vec3 diferencia = vMundo - uOjo; // Trasladamos el origen del mundo hasta la camara
                            float vistaX = dot(diferencia, derecha);
                            float vistaY = dot(diferencia, arriba);
                            float vistaZ = -dot(diferencia, frente);
                            float factor = 1.0 / tan(radians(55.0) * 0.5); // Convertimos el campo visual (55 grados) en escala de perspectiva
                            float cerca = 0.1;
                            float lejos = 250.0;
                            float clipX = vistaX * factor / uAspecto;
                            float clipY = vistaY * factor;
                            float clipZ = (lejos + cerca) / (cerca - lejos) * vistaZ;
                            clipZ += 2.0 * lejos * cerca / (cerca - lejos); // añadir el constante de profundidad
                            gl_Position = vec4(clipX, clipY, clipZ, -vistaZ);
                        }
                    }
                """;
    }

    private String fragmentShader() {
        return """
                    #version 330 core
                    in vec3 vNormal; // Normal interpolada que llega del vertex shader
                    uniform vec3 uColor;
                    uniform vec3 uLuzDir; // Direccion hacia la luz
                    uniform vec3 uLuzColor; // Color/intensidad de la luz
                    uniform vec3 uAmbiente; // Luz base que ilumina todo por igual
                    out vec4 color;
                    void main() {
                        vec3 normal = normalize(vNormal);
                        float difusa = max(dot(normal, normalize(uLuzDir)), 0.0);
                        vec3 iluminacion = uAmbiente + uLuzColor * difusa;
                        color = vec4(uColor * iluminacion, 1.0);
                    }
                """;
    }

    public static void main(String[] args) {
        new AppCiudad().run();
    }
}
