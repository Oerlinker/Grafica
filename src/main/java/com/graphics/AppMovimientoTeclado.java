package com.graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;


/**
 * Este programa hace 3 cosas:
 * 1. Abrir una ventana usando la biblioteca GLFW
 * 2. Definir un Triangulo y usar shaders
 * 3. En un Bucle, Dibujar el triangulo en la ventana una y otra vez
 * <p>
 * CONCEPTOS CLAVES:
 * - OpenGL: Conjunto de funciones para dibujar usando al GPU
 * -GLFW: Biblioteca que crea la ventana y maneja el teclado/raton. OpenGL solo dice como dibujar
 * no crea ventanas.
 * -GPU: Tarjeta Grafica
 * -Shaders: codigo que dice como transformar vertice y que color dar a cada pixel. se escribe en lenguaje GLSL
 * -VAO/VBO: Formas de guardar en la GPU los datos de los vertices
 */
public class AppMovimientoTeclado {
    // referencia a nuestra ventana
    private long window;

    private int program;

    //VAO= Vertex Array Object: es como un dosificador en el que queda toda la configuracion
    private int vao;
    //VBO = Vertex Buffer Object: es un bloque de memoria en la GPU donde guardamos las posiciones de los vertices
    private int vbo;

    //uOffsetLocation = Almacena la ubicacion (ID interna) del uniform uOffset dentro del shader
    private int uOffsetLocation;

    //Offset del triangulo que se manda al shader en cada frame
    private float offSetX=0.0f;
    private float offSetY=0.0f;

    //Velocidad
    private static final float VELOCIDAD=1.2f;
    //Limite de desplazamiento
    private static final float LIMITE=0.9f;

    private static final int ANCHO = 800;
    private static final int ALTO = 600;

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
        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "OpenGL_Triangulo", 0, 0);

        //cerrar la ventana con ESC
        GLFW.glfwSetKeyCallback(window,(w,key,scancode,action,mods)->{
            if(key==GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS){
                GLFW.glfwSetWindowShouldClose(w,true);
            }
        });

        //decir que todas las ordenes de OpenGL que ejecutemos a partir de ahora se aplique a esta ventana
        GLFW.glfwMakeContextCurrent(window);
        //Sincronizar el dibujo con el monitor (1=esperar el siguiente refresco) evitamos el "tearing"(imagen partida)
        GLFW.glfwSwapInterval(1);
        //hacer visible la ventana
        GLFW.glfwShowWindow(window);
        //Carga en java las funciones de OpengGL LWJGL necesita este paso para enlazar las llamadas de java con la gpu
        GL.createCapabilities();

        crearShaders();
        crearTriangulo();
    }

    private void crearShaders() {
        /*vertexSrc: Codigo GLSL del vertex shader
        Recibe la posicion de cada vertice (aPos) y la asigna a gl_Position(salida estandar)
        "layout(location=0) in vec3 aPos" = entrada en el canal 0, 3 floats (x,y,z)
        * */
        String vertexSrc = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
                uniform vec2 uOffset;
                void main(){
                    vec3 pos = vec3(aPos.xy + uOffset,aPos.z);
                    gl_Position = vec4(pos,1.0);
                }
                """;

        /*fragmentSrc: codigo GLSL del fragment shader
        fragColor = color de salida del pixel, vec4(R,G,B,A) = Rojo,Verde,Azul,Alpha
        Se ejecuta por cada pixel del triangulo, la salida es el color del pixel
        vec4(0.2,0.8,0.4,1.0)= verde
        */
        String fragmentSrc = """
                #version 330 core
                out vec4 fragColor;
                void main(){
                fragColor = vec4(0.2,0.8,0.4,1.0);
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
        comprobarShader(fragmentShader, "fragment");

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vertexShader);
        GL20.glAttachShader(program, fragmentShader);
        GL20.glLinkProgram(program);

        //comprobar que el enlace fue correcto
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al enlazar programa" + GL20.glGetProgramInfoLog(program));
        }

        uOffsetLocation = GL20.glGetUniformLocation(program,"uOffset");
        if(uOffsetLocation == -1){
            throw new RuntimeException("No se encontro el uniform uOffset");
        }
        //los shaders ya estan copiados dentro del programa, podemos borrarlos para liberar
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    private void comprobarShader(int shader, String tipo) {
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException(tipo + "shader: " + GL20.glGetShaderInfoLog(shader));
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
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,vbo);

        /*OpenGL espera los datos en FloatBuffer,no en un float[] de java
        * copiamos el array al buffer y flip() dejamos listo para la lectura
        */
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        //Enviar los datos al VBO,GL_STATIC_DRAW = "no vamos a cambiar estos datos"
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER,buffer,GL15.GL_STATIC_DRAW);

        /*Decir a OpenFL como voy a leer estos buffer
        * atributo location 0 (como layout(location=0)en el vertex shader)
        * 3 floats por vertice(x,y,z)
        * Tipo GL_FLOAT,sin normalizar
        * stride = 12 bytes(3 floats x 4 byte), offset = 0*/
        GL20.glVertexAttribPointer(0,3,GL11.GL_FLOAT,false,3 * Float.BYTES,0);
        //activar el atributo 0(en el shader es in vec3 aPos en location 0)
        GL20.glEnableVertexAttribArray(0);

        //desenlazar el VBO y VAO, por que ya no hace falta dejarlos activos
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER,0);
        GL30.glBindVertexArray(0);

    }

    private void procesarInput(float deltaTime){
        //Distancia del recorrido velocidad(unidades/s) * tiempo del frame (s)
        float paso = VELOCIDAD * deltaTime;
        if (GLFW.glfwGetKey(window,GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS
        || GLFW.glfwGetKey(window,GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS){
            offSetX -= paso;
        }
        if (GLFW.glfwGetKey(window,GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window,GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS){
            offSetX += paso;
        }
        if (GLFW.glfwGetKey(window,GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window,GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS){
            offSetY += paso;
        }
        if (GLFW.glfwGetKey(window,GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window,GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS){
            offSetY -= paso;
        }

        //Clamping
        offSetX = Math.max(-LIMITE,Math.min(LIMITE,offSetX));
        offSetY = Math.max(-LIMITE,Math.min(LIMITE,offSetY));
    }

    //BUCLE PRINCIPAL: en cada iteracion dibujamos un frame
    //Mientras la ventana no se cierre:
    // 1. Limpiamos la ventana
    // 2. Decimos usar este programa de shaders y este vao para dibujar
    // 3. Ordenamos dibujar los 3 vertices
    // 4. Intercambiamos los buffers
    // 5. Procesamos los eventos
    private void loop() {
        //tiempo ultimo
        float ultimoTiempo = (float) GLFW.glfwGetTime();
        //Movimiento desacoplado de FPS usando deltaTime
        while (!GLFW.glfwWindowShouldClose(window)) {
            float tiempoActual = (float) GLFW.glfwGetTime();
            float deltaTime = tiempoActual - ultimoTiempo;
            ultimoTiempo=tiempoActual;

            //Actualiza el offsetX/offsetY segun el teclado
            procesarInput(deltaTime);

            //color con el que vamos a limpiar la pantalla(gris oscuro:RGBA)
            GL11.glClearColor(0.08f, 0.08f, 0.12f, 1.0f);
            //limpiar el buffer de color(rellenar con el color anterior)
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            //Activar nuestro programa de shaders
            GL20.glUseProgram(program);
            //enviamos el offset actualizado al shader antes de dibujar
            //A partir de este valor el vertex shader desplaza cada vertice
            GL20.glUniform2f(uOffsetLocation,offSetX,offSetY);
            //Activar el VAO que contiene el triangulo
            GL30.glBindVertexArray(vao);
            //orden del dibujo,modo,empezar con el vertice 0 y dibujar 3 vertices
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);

            //Doble buffer: hemos dibujado en un buffer oculto y ahora lo mostrmaos en pantalla e intercambiamos
            GLFW.glfwSwapBuffers(window);
            //procesamos eventos del sistema(si el usuario cerro ventana, si hizo click, etc)
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
        new AppMovimientoTeclado().run();
    }
}
