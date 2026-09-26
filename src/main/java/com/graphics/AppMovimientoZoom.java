package com.graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;


/**
 * Triangulo que se mueve con el teclado (flechas / WASD) y hace zoom
 * con la rueda del mouse o con las teclas + y -.
 * <p>
 * Dos uniforms llegan al vertex shader en cada frame:
 * - uOffset (vec2): cuanto se desplaza el triangulo
 * - uZoom (float): cuanto se escala el triangulo
 * <p>
 * ESC cierra la ventana.
 */
public class AppMovimientoZoom {
    // referencia a nuestra ventana
    private long window;

    private int program;

    private int vao;
    private int vbo;

    private static final int ANCHO = 800;
    private static final int ALTO = 600;

    //Ubicaciones (IDs internos) de los uniforms dentro del shader
    private int uOffsetLocation;
    private int uZoomLocation;

    //Movimiento
    private float offSetX = 0.0f;
    private float offSetY = 0.0f;
    private static final float VELOCIDAD = 1.2f;
    private static final float LIMITE = 0.9f;

    //Zoom
    private float zoom = 1.0f;
    private static final float ZOOM_MIN = 0.25f;
    private static final float ZOOM_MAX = 3.00f;
    private static final float VELOCIDAD_ZOOM = 1.25f;

    public void run() {
        init();
        loop();
        cleanup();
    }

    public void init() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("no se pudo inicializar GLFW");
        }
        //Reestablece todas las opciones por defecto
        GLFW.glfwDefaultWindowHints();
        //ocultar la ventana al principio
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        //Permitir que el usuario redimensione la ventana
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        //Queremos que use la version OpenGL 3.3
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        //creamos la ventana
        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "OpenGL_Movimiento_Zoom", 0, 0);
        if (window == 0) {
            throw new RuntimeException("No se pudo crear la ventana");
        }

        //cerrar la ventana con ESC
        GLFW.glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS) {
                GLFW.glfwSetWindowShouldClose(w, true);
            }
        });

        //zoom con la rueda del mouse
        GLFW.glfwSetScrollCallback(window, (w, xoffset, yoffset) -> {
            zoom += (float) yoffset * 0.10f;
            zoom = clamp(zoom, ZOOM_MIN, ZOOM_MAX);
        });

        //decir que todas las ordenes de OpenGL que ejecutemos a partir de ahora se aplique a esta ventana
        GLFW.glfwMakeContextCurrent(window);
        //Sincronizar el dibujo con el monitor (evita el "tearing")
        GLFW.glfwSwapInterval(1);
        //hacer visible la ventana
        GLFW.glfwShowWindow(window);
        //Carga en java las funciones de OpenGL
        GL.createCapabilities();

        crearShaders();
        crearTriangulo();
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private void crearShaders() {
        /*Primero se escala (aPos.xy * uZoom) y despues se desplaza (+ uOffset).
         * Asi el zoom agranda el triangulo sobre su propio centro y el movimiento
         * siempre avanza la misma distancia en pantalla, sin importar el zoom.
         * */
        String vertexSrc = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                uniform vec2 uOffset;
                uniform float uZoom;
                void main(){
                    vec2 xy = aPos.xy * uZoom + uOffset;
                    gl_Position = vec4(xy, aPos.z, 1.0);
                }
                """;

        String fragmentSrc = """
                #version 330 core
                out vec4 fragColor;
                void main(){
                    fragColor = vec4(0.2, 0.8, 0.4, 1.0);
                }
                """;

        //crear el objeto en la GPU y compilar el codigo
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader);
        comprobarShader(vertexShader, "Vertex");

        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);
        GL20.glCompileShader(fragmentShader);
        comprobarShader(fragmentShader, "Fragment");

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);

        //comprobar que el enlace fue correcto
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al enlazar programa: " + GL20.glGetProgramInfoLog(program));
        }

        //buscar la ubicacion de cada uniform (los nombres deben coincidir con el shader)
        uOffsetLocation = GL20.glGetUniformLocation(program, "uOffset");
        if (uOffsetLocation == -1) {
            throw new RuntimeException("No se encontro el uniform uOffset");
        }
        uZoomLocation = GL20.glGetUniformLocation(program, "uZoom");
        if (uZoomLocation == -1) {
            throw new RuntimeException("No se encontro el uniform uZoom");
        }

        //los shaders ya estan copiados dentro del programa, podemos borrarlos
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private void comprobarShader(int shader, String tipo) {
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException(tipo + " shader: " + GL20.glGetShaderInfoLog(shader));
        }
    }

    private void crearTriangulo() {
        float[] vertices = {
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
        };
        //VAO: objeto que guarda la configuracion de los buffers y atributos
        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        //VBO: buffer en la GPU donde guardamos las coordenadas
        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        //desenlazar el VBO y VAO, ya no hace falta dejarlos activos
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void procesarInput(float deltaTime) {
        //--- Movimiento ---
        float paso = VELOCIDAD * deltaTime;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            offSetX -= paso;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            offSetX += paso;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            offSetY += paso;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            offSetY -= paso;
        }
        offSetX = clamp(offSetX, -LIMITE, LIMITE);
        offSetY = clamp(offSetY, -LIMITE, LIMITE);

        //--- Zoom con teclado ---
        float pasoZoom = VELOCIDAD_ZOOM * deltaTime;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_KP_ADD) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_EQUAL) == GLFW.GLFW_PRESS) {
            zoom += pasoZoom;
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_KP_SUBTRACT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_MINUS) == GLFW.GLFW_PRESS) {
            zoom -= pasoZoom;
        }
        zoom = clamp(zoom, ZOOM_MIN, ZOOM_MAX);
    }

    //BUCLE PRINCIPAL: en cada iteracion dibujamos un frame
    private void loop() {
        float ultimoTiempo = (float) GLFW.glfwGetTime();

        while (!GLFW.glfwWindowShouldClose(window)) {
            float tiempoActual = (float) GLFW.glfwGetTime();
            float deltaTime = tiempoActual - ultimoTiempo;
            ultimoTiempo = tiempoActual;

            //Actualiza offset y zoom segun el teclado
            procesarInput(deltaTime);

            GL11.glClearColor(0.08f, 0.08f, 0.12f, 1.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            //Activar el programa y DESPUES enviar los uniforms
            GL20.glUseProgram(program);
            GL20.glUniform2f(uOffsetLocation, offSetX, offSetY);
            GL20.glUniform1f(uZoomLocation, zoom);

            GL30.glBindVertexArray(vao);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    private void cleanup() {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL20.glDeleteProgram(program);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public static void main(String[] args) {
        new AppMovimientoZoom().run();
    }
}