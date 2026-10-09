package com.graphics;

import java.util.HashMap;
import java.util.Map;

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
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
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
    protected long ventana;
    private int programa;
    private int vao;
    private int vbo;
    protected int ancho = 1100;
    protected int alto = 760;

    protected static final float LIMITE = 55;
    protected static final float CELDA = 10;
    private static final int MAX_FAROLAS = 20;
    private static final int MAX_SEMAFOROS = 10;

    private float orbita = 0.6f;
    private float zoom = 1.0f;
    private float alturaCamara = 55;
    private static final float ZOOM_MIN = 0.4f;
    private static final float ZOOM_MAX = 3.0f;
    private static final float VELOCIDAD_ZOOM = 1.25f;
    private static final float ALTURA_MIN = 15;
    private static final float ALTURA_MAX = 120;
    private static final float VELOCIDAD_ALTURA = 25f;
    private static final float VELOCIDAD_ORBITA = 1.0f;

    private final Map<String, Integer> uniforms = new HashMap<>();

    protected static final int[][] MAPA = {
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
        { 0, 1, 0, 1, 0, 3, 0, 2, 0, 1, 0 },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
        { 0, 2, 0, 1, 0, 1, 0, 1, 0, 3, 0 },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
        { 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0 },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
        { 0, 1, 0, 2, 0, 1, 0, 1, 0, 2, 0 },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
        { 0, 3, 0, 1, 0, 2, 0, 1, 0, 1, 0 },
        { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
    };

    protected float autoX = 0;
    protected float autoZ = -50;
    protected float autoAngulo = 0;
    protected boolean farosEncendidos = false;

    protected boolean modoNoche = false;
    protected float transicionDiaNoche = 0.0f;

    private final float[][] farolaPosiciones = calcularFarolas();
    private int farolasCount;

    private float[][] semaforoPosiciones;
    private int[] semaforoEjes;
    private float[] semaforoTiempos;
    private int[] semaforoFases;
    private int semaforosCount;

    private boolean minimapaVisible = true;

    protected static final int NUM_NPCS = 8;
    protected float[] npcX = new float[NUM_NPCS];
    protected float[] npcZ = new float[NUM_NPCS];
    private int[] npcDir = new int[NUM_NPCS];
    private float[] npcVelBase = new float[NUM_NPCS];
    private float[][] npcColores = new float[NUM_NPCS][3];
    private int npcTick = 0;

    // ===================== Ciclo de vida ===================

    public void run() {
        GLFWErrorCallback errores = GLFWErrorCallback.createPrint(System.err);
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

    private void iniciar() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("No se pudo inicializar GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        ventana = GLFW.glfwCreateWindow(ancho, alto,
            "Ciudad | Izq/Der: orbitar | Arriba/Abajo: altura | +/-: zoom | N: noche | M: minimapa | Esc: salir", 0, 0);

        GLFW.glfwMakeContextCurrent(ventana);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(ventana);
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);

        glfwSetKeyCallback(ventana, (v, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                tecla(key);
            }
        });

        glfwSetScrollCallback(ventana, (v, xoffset, yoffset) -> {
            zoom += (float) yoffset * 0.10f;
            zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoom));
        });

        inicializarSemaforos();
        inicializarNPCs();
        crearPrograma();
        crearCubo();
    }

    private void loop() {
        double tiempoAnterior = glfwGetTime();
        int cuadros = 0;
        int maxCuadros = Integer.getInteger("demo.frames", 0);
        int[] anchoReal = new int[1];
        int[] altoReal = new int[1];

        while (!glfwWindowShouldClose(ventana)) {
            glfwPollEvents();
            double tiempoActual = glfwGetTime();
            float deltaTime = (float) Math.min(tiempoActual - tiempoAnterior, 0.05);
            tiempoAnterior = tiempoActual;
            actualizar(deltaTime);
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
        if (programa != 0) glDeleteProgram(programa);
        if (vbo != 0) glDeleteBuffers(vbo);
        if (vao != 0) glDeleteVertexArrays(vao);
        if (ventana != 0) {
            Callbacks.glfwFreeCallbacks(ventana);
            glfwDestroyWindow(ventana);
        }
        glfwTerminate();
    }

    // ===================== Teclas ===================

    protected void tecla(int key) {
        if (key == GLFW_KEY_ESCAPE) glfwSetWindowShouldClose(ventana, true);
        if (key == GLFW_KEY_N) modoNoche = !modoNoche;
        if (key == GLFW_KEY_M) minimapaVisible = !minimapaVisible;
    }

    protected boolean pulsada(int key) {
        return glfwGetKey(ventana, key) == GLFW_PRESS;
    }

    // ===================== Actualizar ===================

    protected void actualizar(float deltaTime) {
        float pasoOrbita = VELOCIDAD_ORBITA * deltaTime;
        if (pulsada(GLFW_KEY_LEFT)) orbita -= pasoOrbita;
        if (pulsada(GLFW_KEY_RIGHT)) orbita += pasoOrbita;

        float pasoAltura = VELOCIDAD_ALTURA * deltaTime;
        if (pulsada(GLFW_KEY_UP)) alturaCamara += pasoAltura;
        if (pulsada(GLFW_KEY_DOWN)) alturaCamara -= pasoAltura;
        alturaCamara = Math.max(ALTURA_MIN, Math.min(ALTURA_MAX, alturaCamara));

        float pasoZoom = VELOCIDAD_ZOOM * deltaTime;
        if (pulsada(GLFW_KEY_KP_ADD) || pulsada(GLFW_KEY_EQUAL)) zoom += pasoZoom;
        if (pulsada(GLFW_KEY_KP_SUBTRACT) || pulsada(GLFW_KEY_MINUS)) zoom -= pasoZoom;
        zoom = Math.max(ZOOM_MIN, Math.min(ZOOM_MAX, zoom));

        float objetivo = modoNoche ? 1.0f : 0.0f;
        float velocidadTransicion = 0.25f;
        if (transicionDiaNoche < objetivo) {
            transicionDiaNoche = Math.min(objetivo, transicionDiaNoche + velocidadTransicion * deltaTime);
        } else if (transicionDiaNoche > objetivo) {
            transicionDiaNoche = Math.max(objetivo, transicionDiaNoche - velocidadTransicion * deltaTime);
        }

        actualizarNPCs(deltaTime);

        float[] duraciones = { 5.0f, 2.0f, 5.0f };
        for (int i = 0; i < semaforosCount; i++) {
            semaforoTiempos[i] += deltaTime;
            if (semaforoTiempos[i] >= duraciones[semaforoFases[i]]) {
                semaforoTiempos[i] -= duraciones[semaforoFases[i]];
                semaforoFases[i] = (semaforoFases[i] + 1) % 3;
            }
        }
    }

    // ===================== Camara orbital ===================

    protected void configurarCamara() {
        float radio = 90 / zoom;
        float camaraX = (float) Math.sin(orbita) * radio;
        float camaraZ = (float) Math.cos(orbita) * radio;

        vector("uOjo", camaraX, alturaCamara, camaraZ);
        vector("uObjetivo", 0, 0, 0);
        decimal("uAspecto", (float) ancho / alto);
    }

    // ===================== Luces ===================

    private void prepararLuces() {
        decimal("uModoNoche", transicionDiaNoche);
        float ambR = 0.9f - 0.65f * transicionDiaNoche;
        float ambG = 0.9f - 0.6f * transicionDiaNoche;
        float ambB = 0.9f - 0.55f * transicionDiaNoche;
        vector("uLuzAmbiente", ambR, ambG, ambB);

        entero("uFarolasCount", farolasCount);
        for (int i = 0; i < farolasCount; i++) {
            vector("uFarolas[" + i + "]", farolaPosiciones[i][0], farolaPosiciones[i][1], farolaPosiciones[i][2]);
        }

        entero("uFarosEncendidos", farosEncendidos ? 1 : 0);
        float cos = (float) Math.cos(autoAngulo);
        float sin = (float) Math.sin(autoAngulo);
        float izqX = autoX + sin * 1.2f + cos * 0.4f;
        float izqZ = autoZ + cos * 1.2f - sin * 0.4f;
        float derX = autoX + sin * 1.2f - cos * 0.4f;
        float derZ = autoZ + cos * 1.2f + sin * 0.4f;
        float dirX = (float) Math.sin(autoAngulo);
        float dirZ = (float) Math.cos(autoAngulo);
        vector("uFarosPos[0]", izqX, 0.6f, izqZ);
        vector("uFarosPos[1]", derX, 0.6f, derZ);
        vector("uFarosDir[0]", dirX, -0.15f, dirZ);
        vector("uFarosDir[1]", dirX, -0.15f, dirZ);

        entero("uSemaforosCount", semaforosCount);
        for (int i = 0; i < semaforosCount; i++) {
            vector("uSemaforos[" + i + "]", semaforoPosiciones[i][0], 3.5f, semaforoPosiciones[i][1]);
            float sr = 0, sg = 0, sb = 0;
            switch (semaforoFases[i]) {
                case 0: sg = 1.0f; sr = 0.1f; sb = 0.2f; break;
                case 1: sr = 1.0f; sg = 0.9f; sb = 0.1f; break;
                case 2: sr = 1.0f; sg = 0.1f; sb = 0.1f; break;
            }
            vector("uSemaforosColor[" + i + "]", sr, sg, sb);
        }
    }

    // ===================== Farolas: calcular posiciones ===================

    private static float[][] calcularFarolas() {
        java.util.List<float[]> lista = new java.util.ArrayList<>();
        for (int fila = 0; fila < 11; fila += 2) {
            for (int col = 0; col < 11; col += 2) {
                if ((fila + col) % 4 == 0) {
                    float x = -55 + 10 * (col + 0.5f);
                    float z = -55 + 10 * (fila + 0.5f);
                    lista.add(new float[] { x, 5.0f, z });
                }
            }
        }
        return lista.toArray(new float[0][]);
    }

    // ===================== Semaforos: inicializar ===================

    private void inicializarSemaforos() {
        farolasCount = farolaPosiciones.length;

        int[][] intersecciones = {
            { 2, 2 }, { 2, 6 }, { 4, 4 }, { 6, 2 }, { 6, 8 },
            { 8, 4 }, { 8, 8 }, { 4, 8 }
        };
        semaforosCount = intersecciones.length;
        semaforoPosiciones = new float[semaforosCount][2];
        semaforoEjes = new int[semaforosCount];
        semaforoTiempos = new float[semaforosCount];
        semaforoFases = new int[semaforosCount];

        for (int i = 0; i < semaforosCount; i++) {
            int fila = intersecciones[i][0];
            int col = intersecciones[i][1];
            semaforoPosiciones[i][0] = -LIMITE + CELDA * (col + 0.5f);
            semaforoPosiciones[i][1] = -LIMITE + CELDA * (fila + 0.5f);
            semaforoEjes[i] = i % 2;
            semaforoFases[i] = (i % 2 == 0) ? 0 : 2;
            semaforoTiempos[i] = (i * 2.3f) % 12.0f;
        }
    }

    // ===================== Dibujar frame ===================

    protected float centro(int indice) {
        return -LIMITE + CELDA * (indice + 0.5f);
    }

    private void dibujarFrame() {
        float fondoR = 0.50f - 0.45f * transicionDiaNoche;
        float fondoG = 0.70f - 0.60f * transicionDiaNoche;
        float fondoB = 0.90f - 0.70f * transicionDiaNoche;

        glViewport(0, 0, ancho, alto);
        glClearColor(fondoR, fondoG, fondoB, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glUseProgram(programa);
        glBindVertexArray(vao);
        entero("uMapa", 0);
        configurarCamara();
        prepararLuces();
        escena();
        dibujarVehiculo();
        dibujarFarolas();
        dibujarSemaforos();
        dibujarAutosNPC();

        if (minimapaVisible) {
            dibujarMinimapa();
        }

        if (glGetError() != GL_NO_ERROR) {
            throw new IllegalStateException("Error OpenGL al dibujar");
        }
    }

    // ===================== Escena principal ===================

    private void escena() {
        caja(0, -0.5f, 0, 110, 0.6f, 110, 0.12f, 0.18f, 0.25f);

        for (int fila = 0; fila < MAPA.length; fila++) {
            for (int columna = 0; columna < MAPA[fila].length; columna++) {
                float x = centro(columna);
                float z = centro(fila);
                int tipo = MAPA[fila][columna];
                if (tipo == 0) {
                    dibujarMarcasCalle(fila, columna, x, z);
                    dibujarPasosPeatonales(fila, columna, x, z);
                } else {
                    caja(x, 0.15f, z, 10, 0.3f, 10, 0.60f, 0.64f, 0.66f);
                    if (tipo == 1 || tipo == 2) {
                        float altura;
                        if (tipo == 1) {
                            altura = 5 + (fila * 3 + columna * 7) % 9;
                        } else {
                            altura = 3 + (fila * 5 + columna * 3) % 4;
                        }
                        float rojo = 0.28f + columna * 0.035f;
                        float azul = 0.48f + fila * 0.02f;
                        caja(x, altura / 2 + 0.3f, z, 7, altura, 7, rojo, 0.40f, azul);
                        caja(x, altura + 0.45f, z, 7.3f, 0.3f, 7.3f, 0.20f, 0.26f, 0.32f);
                        dibujarVentanas(fila, columna, x, z, altura);
                    } else if (tipo == 3) {
                        caja(x, 0.32f, z, 9, 0.1f, 9, 0.20f, 0.45f, 0.15f);
                        dibujarArbolesYBancos(fila, columna, x, z);
                    }
                }
            }
        }
    }

    // ===================== Marcas de calle ===================

    private void dibujarMarcasCalle(int fila, int columna, float x, float z) {
        if (fila % 2 == 0 && columna % 2 == 1) {
            for (int d = -3; d <= 3; d += 3) {
                caja(x + d, 0.025f, z, 1.6f, 0.03f, 0.13f, 1, 0.84f, 0.35f);
            }
        }
        if (columna % 2 == 0 && fila % 2 == 1) {
            for (int d = -3; d <= 3; d += 3) {
                caja(x, 0.025f, z + d, 0.13f, 0.03f, 1.6f, 1, 0.84f, 0.35f);
            }
        }
    }

    // ===================== Pasos peatonales ===================

    private void dibujarPasosPeatonales(int fila, int columna, float x, float z) {
        if (fila % 2 != 0 || columna % 2 != 0) return;

        boolean arribaEsManzana = fila > 0 && MAPA[fila - 1][columna] != 0;
        boolean abajoEsManzana = fila < 10 && MAPA[fila + 1][columna] != 0;
        boolean izqEsManzana = columna > 0 && MAPA[fila][columna - 1] != 0;
        boolean derEsManzana = columna < 10 && MAPA[fila][columna + 1] != 0;

        if (arribaEsManzana) {
            for (int i = 0; i < 5; i++) {
                float offset = -2.0f + i * 1.0f;
                caja(x + offset, 0.01f, z - 4.0f, 0.3f, 0.02f, 2.0f, 0.9f, 0.9f, 0.9f);
            }
        }
        if (abajoEsManzana) {
            for (int i = 0; i < 5; i++) {
                float offset = -2.0f + i * 1.0f;
                caja(x + offset, 0.01f, z + 4.0f, 0.3f, 0.02f, 2.0f, 0.9f, 0.9f, 0.9f);
            }
        }
        if (izqEsManzana) {
            for (int i = 0; i < 5; i++) {
                float offset = -2.0f + i * 1.0f;
                caja(x - 4.0f, 0.01f, z + offset, 2.0f, 0.02f, 0.3f, 0.9f, 0.9f, 0.9f);
            }
        }
        if (derEsManzana) {
            for (int i = 0; i < 5; i++) {
                float offset = -2.0f + i * 1.0f;
                caja(x + 4.0f, 0.01f, z + offset, 2.0f, 0.02f, 0.3f, 0.9f, 0.9f, 0.9f);
            }
        }
    }

    // ===================== Arboles y bancos ===================

    private void dibujarArbolesYBancos(int fila, int columna, float x, float z) {
        int seed = fila * 17 + columna * 31;
        int numArboles = 2 + (seed % 3);
        for (int i = 0; i < numArboles; i++) {
            float offsetX = ((seed + i * 7) % 5 - 2) * 1.5f;
            float offsetZ = ((seed + i * 13) % 5 - 2) * 1.5f;
            float altArbol = 2.0f + ((seed + i * 3) % 3) * 0.7f;
            float radioC = 1.0f + ((seed + i * 11) % 3) * 0.2f;
            caja(x + offsetX, altArbol / 2, z + offsetZ, 0.3f, altArbol, 0.3f, 0.35f, 0.25f, 0.1f);
            caja(x + offsetX, altArbol + radioC * 0.5f, z + offsetZ,
                 radioC * 2, radioC, radioC * 2, 0.15f, 0.5f, 0.1f);
        }

        int numBancos = 1 + (seed % 2);
        for (int i = 0; i < numBancos; i++) {
            float bx = ((seed + i * 23) % 7 - 3) * 1.0f;
            float bz = ((seed + i * 29) % 7 - 3) * 1.0f;
            float anguloB = ((seed + i * 5) % 4) * 1.5708f;
            cajaGirada(x + bx, 0.35f, z + bz, 1.5f, 0.5f, 0.5f, 0.45f, 0.30f, 0.15f, anguloB);
        }
    }

    // ===================== Ventanas en edificios ===================

    private void dibujarVentanas(int fila, int columna, float x, float z, float altura) {
        int pisos = (int) (altura / 1.5f);
        if (pisos < 1) pisos = 1;
        if (pisos > 6) pisos = 6;
        int ventanasPorFila = 3;
        int seed = fila * 101 + columna * 53;

        for (int piso = 0; piso < pisos; piso++) {
            float yVentana = 1.2f + piso * 1.5f;
            if (yVentana > altura) break;

            for (int v = 0; v < ventanasPorFila; v++) {
                float offsetV = (v - 1) * 2.0f;
                boolean iluminada = ((seed + piso * 7 + v * 13) % 10) < 5;

                float vr, vg, vb;
                if (transicionDiaNoche > 0.5f && iluminada) {
                    vr = 0.9f; vg = 0.8f; vb = 0.4f;
                } else if (transicionDiaNoche > 0.5f) {
                    vr = 0.05f; vg = 0.05f; vb = 0.1f;
                } else {
                    vr = 0.3f; vg = 0.35f; vb = 0.45f;
                }

                caja(x + offsetV, yVentana, z + 3.55f, 0.4f, 0.6f, 0.05f, vr, vg, vb);
                caja(x + offsetV, yVentana, z - 3.55f, 0.4f, 0.6f, 0.05f, vr, vg, vb);
                caja(x + 3.55f, yVentana, z + offsetV, 0.05f, 0.6f, 0.4f, vr, vg, vb);
                caja(x - 3.55f, yVentana, z + offsetV, 0.05f, 0.6f, 0.4f, vr, vg, vb);
            }
        }
    }

    // ===================== Dibujar vehiculo ===================

    private void dibujarVehiculo() {
        cajaGirada(autoX, 0.45f, autoZ, 1.6f, 0.6f, 3.0f, 0.15f, 0.35f, 0.65f, autoAngulo);
        cajaGirada(autoX, 0.95f, autoZ, 1.2f, 0.4f, 1.6f, 0.12f, 0.30f, 0.55f, autoAngulo);

        float cos = (float) Math.cos(autoAngulo);
        float sin = (float) Math.sin(autoAngulo);
        float[][] ruedas = {
            { 0.85f, -1.0f }, { -0.85f, -1.0f },
            { 0.85f, 1.0f }, { -0.85f, 1.0f }
        };
        for (float[] r : ruedas) {
            float rx = autoX + cos * r[0] + sin * r[1];
            float rz = autoZ - sin * r[0] + cos * r[1];
            cajaGirada(rx, 0.2f, rz, 0.2f, 0.4f, 0.5f, 0.1f, 0.1f, 0.1f, autoAngulo);
        }

        if (farosEncendidos) {
            float izqX = autoX + sin * 1.2f + cos * 0.4f;
            float izqZ = autoZ + cos * 1.2f - sin * 0.4f;
            float derX = autoX + sin * 1.2f - cos * 0.4f;
            float derZ = autoZ + cos * 1.2f + sin * 0.4f;
            caja(izqX, 0.6f, izqZ, 0.2f, 0.15f, 0.1f, 1.0f, 0.95f, 0.8f);
            caja(derX, 0.6f, derZ, 0.2f, 0.15f, 0.1f, 1.0f, 0.95f, 0.8f);
        }
    }

    // ===================== Dibujar farolas ===================

    private void dibujarFarolas() {
        for (int i = 0; i < farolasCount; i++) {
            float fx = farolaPosiciones[i][0];
            float fz = farolaPosiciones[i][2];
            caja(fx, 2.5f, fz, 0.2f, 5.0f, 0.2f, 0.2f, 0.2f, 0.2f);
            caja(fx, 5.2f, fz, 0.6f, 0.3f, 0.6f, 0.3f, 0.3f, 0.25f);
            if (transicionDiaNoche > 0.3f) {
                caja(fx, 4.95f, fz, 0.4f, 0.1f, 0.4f, 1.0f, 0.8f, 0.3f);
            }
        }
    }

    // ===================== Dibujar semaforos ===================

    private void dibujarSemaforos() {
        for (int i = 0; i < semaforosCount; i++) {
            float sx = semaforoPosiciones[i][0];
            float sz = semaforoPosiciones[i][1];
            int fase = semaforoFases[i];

            caja(sx, 1.75f, sz, 0.15f, 3.5f, 0.15f, 0.15f, 0.15f, 0.15f);
            caja(sx, 3.8f, sz, 0.5f, 1.2f, 0.5f, 0.1f, 0.1f, 0.1f);

            float rBrillo = (fase == 2) ? 1.0f : 0.2f;
            caja(sx, 4.2f, sz + 0.28f, 0.25f, 0.25f, 0.05f, rBrillo, 0.02f, 0.02f);
            float aBrillo = (fase == 1) ? 1.0f : 0.2f;
            caja(sx, 3.8f, sz + 0.28f, 0.25f, 0.25f, 0.05f, aBrillo, aBrillo * 0.9f, 0.02f);
            float gBrillo = (fase == 0) ? 1.0f : 0.2f;
            caja(sx, 3.4f, sz + 0.28f, 0.25f, 0.25f, 0.05f, 0.02f, gBrillo, 0.04f);
        }
    }

    // ===================== Autos NPC ===================

    private void inicializarNPCs() {
        int[][] spawns = {
            { 0, 0, 0 }, { 0, 6, 1 }, { 4, 0, 2 }, { 4, 10, 3 },
            { 8, 4, 1 }, { 10, 8, 0 }, { 6, 2, 2 }, { 2, 10, 3 }
        };
        float[][] palette = {
            { 0.7f, 0.15f, 0.15f }, { 0.2f, 0.2f, 0.65f }, { 0.6f, 0.55f, 0.1f },
            { 0.1f, 0.5f, 0.2f }, { 0.55f, 0.55f, 0.55f }, { 0.6f, 0.3f, 0.1f },
            { 0.8f, 0.8f, 0.8f }, { 0.35f, 0.1f, 0.45f }
        };
        for (int i = 0; i < NUM_NPCS; i++) {
            npcX[i] = centro(spawns[i][1]);
            npcZ[i] = centro(spawns[i][0]);
            npcDir[i] = spawns[i][2];
            npcVelBase[i] = 6.0f + (i % 3) * 2.0f;
            npcColores[i] = palette[i % palette.length];
        }
    }

    private void actualizarNPCs(float dt) {
        npcTick++;
        for (int i = 0; i < NUM_NPCS; i++) {
            float ax = npcX[i];
            float az = npcZ[i];

            int col = Math.round((ax + LIMITE) / CELDA - 0.5f);
            int fila = Math.round((az + LIMITE) / CELDA - 0.5f);
            col = Math.max(0, Math.min(10, col));
            fila = Math.max(0, Math.min(10, fila));
            float centroX = centro(col);
            float centroZ = centro(fila);
            float distCentro = Math.abs(ax - centroX) + Math.abs(az - centroZ);

            boolean enInterseccion = (fila % 2 == 0 && col % 2 == 0 && distCentro < 0.5f);
            if (enInterseccion) {
                int seed = i * 31 + fila * 7 + col * 13 + npcTick / 60;
                int[] opciones = direccionesValidas(fila, col, npcDir[i]);
                if (opciones.length > 0) {
                    npcDir[i] = opciones[Math.abs(seed) % opciones.length];
                }
            }

            float dx = 0, dz = 0;
            switch (npcDir[i]) {
                case 0: dz = 1; break;
                case 1: dx = 1; break;
                case 2: dz = -1; break;
                case 3: dx = -1; break;
            }

            float vel = npcVelBase[i];

            if (debeFrenarPorSemaforo(ax, az, npcDir[i])) {
                vel = 0;
            }

            float djx = autoX - ax;
            float djz = autoZ - az;
            float enFrenteJ = djx * dx + djz * dz;
            float lateralJ = Math.abs(djx * dz - djz * dx);
            if (enFrenteJ > 0 && enFrenteJ < 4.0f && lateralJ < 1.5f) {
                vel = 0;
            }

            for (int j = 0; j < NUM_NPCS; j++) {
                if (j == i) continue;
                float enfrente = (npcX[j] - ax) * dx + (npcZ[j] - az) * dz;
                if (enfrente > 0.5f && enfrente < 3.0f) {
                    float lateral = Math.abs((npcX[j] - ax) * dz - (npcZ[j] - az) * dx);
                    if (lateral < 1.0f) {
                        vel = 0;
                    }
                }
            }

            float nuevoX = ax + dx * vel * dt;
            float nuevoZ = az + dz * vel * dt;

            if (nuevoX > LIMITE - 2) { nuevoX = -LIMITE + 3; }
            if (nuevoX < -LIMITE + 2) { nuevoX = LIMITE - 3; }
            if (nuevoZ > LIMITE - 2) { nuevoZ = -LIMITE + 3; }
            if (nuevoZ < -LIMITE + 2) { nuevoZ = LIMITE - 3; }

            npcX[i] = nuevoX;
            npcZ[i] = nuevoZ;
        }
    }

    private int[] direccionesValidas(int fila, int col, int dirActual) {
        java.util.List<Integer> opciones = new java.util.ArrayList<>();
        int opuesta = (dirActual + 2) % 4;
        if (fila + 1 < 11 && MAPA[fila + 1][col] == 0 && 0 != opuesta) opciones.add(0);
        if (col + 1 < 11 && MAPA[fila][col + 1] == 0 && 1 != opuesta) opciones.add(1);
        if (fila - 1 >= 0 && MAPA[fila - 1][col] == 0 && 2 != opuesta) opciones.add(2);
        if (col - 1 >= 0 && MAPA[fila][col - 1] == 0 && 3 != opuesta) opciones.add(3);
        if (opciones.isEmpty()) {
            opciones.add(opuesta);
        }
        return opciones.stream().mapToInt(Integer::intValue).toArray();
    }

    private boolean debeFrenarPorSemaforo(float ax, float az, int dir) {
        for (int s = 0; s < semaforosCount; s++) {
            float sx = semaforoPosiciones[s][0];
            float sz = semaforoPosiciones[s][1];
            float haciaX = sx - ax;
            float haciaZ = sz - az;
            float dx = 0, dz = 0;
            switch (dir) {
                case 0: dz = 1; break;
                case 1: dx = 1; break;
                case 2: dz = -1; break;
                case 3: dx = -1; break;
            }
            float enfrente = haciaX * dx + haciaZ * dz;
            if (enfrente < 1.0f || enfrente > 8.0f) continue;
            float lateral = Math.abs(haciaX * dz - haciaZ * dx);
            if (lateral > 3.0f) continue;
            int fase = semaforoFases[s];
            boolean ejeZ = (dir == 0 || dir == 2);
            int ejeSemaforo = semaforoEjes[s];
            if (ejeZ && ejeSemaforo == 0 && fase == 2) return true;
            if (!ejeZ && ejeSemaforo == 1 && fase == 2) return true;
            if (ejeZ && ejeSemaforo == 1 && fase == 0) return true;
            if (!ejeZ && ejeSemaforo == 0 && fase == 0) return true;
        }
        return false;
    }

    private float dist(float x1, float z1, float x2, float z2) {
        float dx = x1 - x2;
        float dz = z1 - z2;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    private void dibujarAutosNPC() {
        for (int i = 0; i < NUM_NPCS; i++) {
            float angulo = 0;
            switch (npcDir[i]) {
                case 0: angulo = 0; break;
                case 1: angulo = (float)(-Math.PI / 2); break;
                case 2: angulo = (float) Math.PI; break;
                case 3: angulo = (float)(Math.PI / 2); break;
            }
            float[] c = npcColores[i];
            cajaGirada(npcX[i], 0.4f, npcZ[i], 1.4f, 0.5f, 2.6f, c[0], c[1], c[2], angulo);
            cajaGirada(npcX[i], 0.85f, npcZ[i], 1.0f, 0.35f, 1.4f,
                       c[0] * 0.85f, c[1] * 0.85f, c[2] * 0.85f, angulo);
        }
    }

    // ===================== Minimapa ===================

    private void dibujarMinimapa() {
        int tamMini = 180;
        int margen = 10;
        glViewport(ancho - tamMini - margen, margen, tamMini, tamMini);
        entero("uMapa", 1);

        caja(0, -0.1f, 0, 112, 0.01f, 112, 0.08f, 0.10f, 0.14f);

        for (int fila = 0; fila < MAPA.length; fila++) {
            for (int col = 0; col < MAPA[fila].length; col++) {
                float x = centro(col);
                float z = centro(fila);
                int tipo = MAPA[fila][col];
                float r, g, b;
                switch (tipo) {
                    case 1: r = 0.5f; g = 0.5f; b = 0.55f; break;
                    case 2: r = 0.4f; g = 0.4f; b = 0.45f; break;
                    case 3: r = 0.2f; g = 0.5f; b = 0.2f; break;
                    default: r = 0.15f; g = 0.15f; b = 0.2f; break;
                }
                caja(x, 0, z, CELDA - 0.5f, 0.01f, CELDA - 0.5f, r, g, b);
            }
        }

        cajaGirada(autoX, 0.5f, autoZ, 1.5f, 0.5f, 3.0f, 1.0f, 0.9f, 0.2f, autoAngulo);

        glViewport(0, 0, ancho, alto);
        entero("uMapa", 0);
    }

    // ===================== Dibujar cajas ===================

    protected void caja(float x, float y, float z, float sx, float sy, float sz,
                        float r, float g, float b) {
        cajaGirada(x, y, z, sx, sy, sz, r, g, b, 0);
    }

    protected void cajaGirada(float x, float y, float z, float sx, float sy, float sz,
                              float r, float g, float b, float angulo) {
        vector("uPos", x, y, z);
        vector("uEscala", sx, sy, sz);
        vector("uColor", r, g, b);
        decimal("uGiro", angulo);
        glDrawArrays(GL_TRIANGLES, 0, 36);
    }

    // ===================== Uniforms ===================

    protected int uniform(String nombre) {
        if (!uniforms.containsKey(nombre)) {
            int ubicacion = glGetUniformLocation(programa, nombre);
            uniforms.put(nombre, ubicacion);
        }
        return uniforms.get(nombre);
    }

    protected void vector(String nombre, float x, float y, float z) {
        glUniform3f(uniform(nombre), x, y, z);
    }

    protected void decimal(String nombre, float valor) {
        glUniform1f(uniform(nombre), valor);
    }

    protected void entero(String nombre, int valor) {
        glUniform1i(uniform(nombre), valor);
    }

    // ===================== Shaders ===================

    private String vertexShader() {
        return """
            #version 330 core
            layout (location = 0) in vec3 aPos;
            layout (location = 1) in vec3 aNormal;
            uniform vec3 uPos;
            uniform vec3 uEscala;
            uniform vec3 uOjo;
            uniform vec3 uObjetivo;
            uniform float uGiro;
            uniform float uAspecto;
            uniform int uMapa;
            out vec3 vMundo;
            out vec3 vNormal;

            void main() {
                float coseno = cos(uGiro);
                float seno = sin(uGiro);
                mat3 giro = mat3(
                    coseno, 0.0, -seno,
                    0.0,    1.0,  0.0,
                    seno,   0.0,  coseno
                );
                vMundo = giro * (aPos * uEscala) + uPos;
                vNormal = normalize(giro * (aNormal / uEscala));

                if (uMapa == 1) {
                    float pantallaX = vMundo.x / 57.0;
                    float pantallaY = vMundo.z / 57.0;
                    float profundidad = -vMundo.y / 100.0;
                    gl_Position = vec4(pantallaX, pantallaY, profundidad, 1.0);
                } else {
                    vec3 frente = normalize(uObjetivo - uOjo);
                    vec3 derecha = normalize(cross(frente, vec3(0.0, 1.0, 0.0)));
                    vec3 arriba = cross(derecha, frente);
                    vec3 diferencia = vMundo - uOjo;
                    float vistaX = dot(diferencia, derecha);
                    float vistaY = dot(diferencia, arriba);
                    float vistaZ = -dot(diferencia, frente);
                    float factor = 1.0 / tan(radians(55.0) * 0.5);
                    float cerca = 0.1;
                    float lejos = 250.0;
                    float clipX = vistaX * factor / uAspecto;
                    float clipY = vistaY * factor;
                    float clipZ = (lejos + cerca) / (cerca - lejos) * vistaZ;
                    clipZ += 2.0 * lejos * cerca / (cerca - lejos);
                    gl_Position = vec4(clipX, clipY, clipZ, -vistaZ);
                }
            }
            """;
    }

    private String fragmentShader() {
        return """
            #version 330 core
            in vec3 vMundo;
            in vec3 vNormal;
            uniform vec3 uColor;
            uniform float uModoNoche;
            uniform vec3 uLuzAmbiente;

            #define MAX_FAROLAS 20
            #define MAX_SEMAFOROS 10
            uniform vec3 uFarolas[MAX_FAROLAS];
            uniform int uFarolasCount;

            uniform vec3 uFarosPos[2];
            uniform vec3 uFarosDir[2];
            uniform int uFarosEncendidos;

            uniform vec3 uSemaforos[MAX_SEMAFOROS];
            uniform vec3 uSemaforosColor[MAX_SEMAFOROS];
            uniform int uSemaforosCount;

            out vec4 color;

            void main() {
                vec3 luz = uLuzAmbiente;
                vec3 normal = normalize(vNormal);

                if (uModoNoche > 0.3) {
                    float factorNoche = smoothstep(0.3, 0.7, uModoNoche);
                    for (int i = 0; i < uFarolasCount; i++) {
                        vec3 delta = vMundo - uFarolas[i];
                        float dist = length(delta);
                        float radio = 10.0;
                        if (dist < radio) {
                            float brillo = (1.0 - dist / radio) * factorNoche;
                            luz += vec3(1.0, 0.8, 0.3) * brillo * 0.6;
                        }
                    }

                    for (int i = 0; i < uSemaforosCount; i++) {
                        vec3 delta = vMundo - uSemaforos[i];
                        float dist = length(delta);
                        float radio = 5.0;
                        if (dist < radio) {
                            float brillo = (1.0 - dist / radio) * factorNoche * 0.3;
                            luz += uSemaforosColor[i] * brillo;
                        }
                    }
                }

                if (uFarosEncendidos == 1) {
                    for (int i = 0; i < 2; i++) {
                        vec3 hacia = vMundo - uFarosPos[i];
                        float dist = length(hacia);
                        float alcance = 20.0;
                        if (dist < alcance) {
                            vec3 dirNorm = normalize(uFarosDir[i]);
                            float cosAngulo = dot(normalize(hacia), dirNorm);
                            float conoLimite = cos(radians(30.0));
                            if (cosAngulo > conoLimite) {
                                float factorCono = (cosAngulo - conoLimite) / (1.0 - conoLimite);
                                float factorDist = 1.0 - dist / alcance;
                                float brillo = factorCono * factorDist;
                                luz += vec3(1.0, 0.95, 0.8) * brillo * 0.8;
                            }
                        }
                    }
                }

                float difuso = max(dot(normal, normalize(vec3(0.3, 1.0, 0.2))), 0.0);
                float factorDifuso = mix(0.3, 0.1, uModoNoche);
                luz += vec3(1.0) * difuso * factorDifuso;

                luz = min(luz, vec3(1.5));
                color = vec4(uColor * luz, 1.0);
            }
            """;
    }

    // ===================== Compilar y crear programa ===================

    private int compilar(int tipo, String fuente) {
        int shader = glCreateShader(tipo);
        glShaderSource(shader, fuente);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String error = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new IllegalStateException(error);
        }
        return shader;
    }

    private void crearPrograma() {
        int shaderVertices = compilar(GL_VERTEX_SHADER, vertexShader());
        int shaderFragmentos = 0;
        try {
            shaderFragmentos = compilar(GL_FRAGMENT_SHADER, fragmentShader());
            programa = glCreateProgram();
            glAttachShader(programa, shaderVertices);
            glAttachShader(programa, shaderFragmentos);
            glLinkProgram(programa);
            if (glGetProgrami(programa, GL_LINK_STATUS) == GL_FALSE) {
                throw new IllegalStateException(glGetProgramInfoLog(programa));
            }
        } finally {
            glDeleteShader(shaderVertices);
            if (shaderFragmentos != 0) glDeleteShader(shaderFragmentos);
        }
    }

    // ===================== Geometria del cubo ===================

    private void crearCubo() {
        float[] vertices = {
            -0.5f,-0.5f, 0.5f, 0, 0, 1,  0.5f,-0.5f, 0.5f, 0, 0, 1,  0.5f, 0.5f, 0.5f, 0, 0, 1,
             0.5f, 0.5f, 0.5f, 0, 0, 1, -0.5f, 0.5f, 0.5f, 0, 0, 1, -0.5f,-0.5f, 0.5f, 0, 0, 1,
            -0.5f,-0.5f,-0.5f, 0, 0,-1, -0.5f, 0.5f,-0.5f, 0, 0,-1,  0.5f, 0.5f,-0.5f, 0, 0,-1,
             0.5f, 0.5f,-0.5f, 0, 0,-1,  0.5f,-0.5f,-0.5f, 0, 0,-1, -0.5f,-0.5f,-0.5f, 0, 0,-1,
            -0.5f, 0.5f, 0.5f,-1, 0, 0, -0.5f, 0.5f,-0.5f,-1, 0, 0, -0.5f,-0.5f,-0.5f,-1, 0, 0,
            -0.5f,-0.5f,-0.5f,-1, 0, 0, -0.5f,-0.5f, 0.5f,-1, 0, 0, -0.5f, 0.5f, 0.5f,-1, 0, 0,
             0.5f, 0.5f, 0.5f, 1, 0, 0,  0.5f,-0.5f, 0.5f, 1, 0, 0,  0.5f,-0.5f,-0.5f, 1, 0, 0,
             0.5f,-0.5f,-0.5f, 1, 0, 0,  0.5f, 0.5f,-0.5f, 1, 0, 0,  0.5f, 0.5f, 0.5f, 1, 0, 0,
            -0.5f, 0.5f,-0.5f, 0, 1, 0, -0.5f, 0.5f, 0.5f, 0, 1, 0,  0.5f, 0.5f, 0.5f, 0, 1, 0,
             0.5f, 0.5f, 0.5f, 0, 1, 0,  0.5f, 0.5f,-0.5f, 0, 1, 0, -0.5f, 0.5f,-0.5f, 0, 1, 0,
            -0.5f,-0.5f,-0.5f, 0,-1, 0,  0.5f,-0.5f,-0.5f, 0,-1, 0,  0.5f,-0.5f, 0.5f, 0,-1, 0,
             0.5f,-0.5f, 0.5f, 0,-1, 0, -0.5f,-0.5f, 0.5f, 0,-1, 0, -0.5f,-0.5f,-0.5f, 0,-1, 0,
        };
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        int stride = 6 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public static void main(String[] args) {
        new AppCiudad().run();
    }
}
