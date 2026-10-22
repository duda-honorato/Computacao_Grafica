package aula;

import aula.md2.Md2Animator;
import aula.md2.Md2Model;
import aula.md2.Md2Renderer;
import aula.md2.Md2Frame;
import aula.obj.Vector3f;
import aula.util.TextureLoader;

import org.lwjgl.opengl.GL;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class MainMd2 {

    public static void main(String[] args) throws Exception {

        Path caminhoModelo = Path.of(
                args.length > 0
                        ? args[0]
                        : "src/main/resources/ogro/ogro.md2"
        );

        Path caminhoTextura = Path.of(
                args.length > 1
                        ? args[1]
                        : "src/main/resources/ogro/ogro.jpg"
        );

        Md2Model modelo = new Md2Model(caminhoModelo.toString());
        Md2Animator animator = new Md2Animator(modelo);

        animator.selecionarAnimacao(0, 39, 8.0f); // comeca parado (stand)

        if (!glfwInit()) {
            throw new IllegalStateException("GLFW nao iniciou.");
        }

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

        long window = glfwCreateWindow(
                1024,
                768,
                "Aula VBO - MD2 Animado",
                0,
                0
        );

        if (window == 0) {
            glfwTerminate();
            throw new IllegalStateException("Falha ao criar janela.");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glActiveTexture(GL_TEXTURE0);
        glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

        Md2Renderer renderer = new Md2Renderer(modelo, animator);

        int textureId = 0;

        try {
            BufferedImage imagem = ImageIO.read(
                    new File(caminhoTextura.toString())
            );

            if (imagem == null) {
                throw new IllegalStateException(
                        "Nao foi possivel ler a textura."
                );
            }

            textureId = TextureLoader.loadTexture(imagem);

            renderer.load();

            Vector3f minimo = new Vector3f(
                    Float.MAX_VALUE,
                    Float.MAX_VALUE,
                    Float.MAX_VALUE
            );

            Vector3f maximo = new Vector3f(
                    -Float.MAX_VALUE,
                    -Float.MAX_VALUE,
                    -Float.MAX_VALUE
            );

            for (Md2Frame frame : modelo.frames) {
                for (Vector3f vertice : frame.vertices) {
                    minimo.x = Math.min(minimo.x, vertice.x);
                    minimo.y = Math.min(minimo.y, vertice.y);
                    minimo.z = Math.min(minimo.z, vertice.z);

                    maximo.x = Math.max(maximo.x, vertice.x);
                    maximo.y = Math.max(maximo.y, vertice.y);
                    maximo.z = Math.max(maximo.z, vertice.z);
                }
            }

            float centroX = (minimo.x + maximo.x) / 2.0f;
            float centroY = (minimo.y + maximo.y) / 2.0f;
            float centroZ = (minimo.z + maximo.z) / 2.0f;

            float tamanhoX = maximo.x - minimo.x;
            float tamanhoY = maximo.y - minimo.y;
            float tamanhoZ = maximo.z - minimo.z;

            float maiorDimensao = Math.max(
                    tamanhoX,
                    Math.max(tamanhoY, tamanhoZ)
            );

            if (maiorDimensao <= 0.0f) {
                throw new IllegalStateException(
                        "Modelo sem dimensoes validas."
                );
            }

            float escala = 2.0f / maiorDimensao;

            int[] largura = new int[1];
            int[] altura = new int[1];

            double inicioRotacao = glfwGetTime();

            // Animacoes do ogro: {frame inicial, frame final}. Tecla 1 = primeira, 2 = segunda...
            int[][] animacoes = {
                    {0, 39},    // 1 stand  (parado)
                    {40, 45},   // 2 run    (correr)
                    {46, 53},   // 3 attack (atacar)
                    {54, 57},   // 4 pain   (dor)
                    {66, 71},   // 5 jump   (pular)
                    {72, 83},   // 6 flip   (cambalhota)
                    {84, 94},   // 7 salute (continencia)
                    {112, 122}, // 8 wave   (acenar)
                    {178, 183}  // 9 death  (morrer)
            };
            int animacaoAtual = 0;

            while (!glfwWindowShouldClose(window)) {

                if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                    glfwSetWindowShouldClose(window, true);
                }

                // Troca de animacao pelo teclado (so quando muda, senao reiniciaria todo frame)
                for (int i = 0; i < animacoes.length; i++) {
                    if (glfwGetKey(window, GLFW_KEY_1 + i) == GLFW_PRESS && i != animacaoAtual) {
                        animator.selecionarAnimacao(animacoes[i][0], animacoes[i][1], 8.0f);
                        animacaoAtual = i;
                    }
                }

                glfwGetFramebufferSize(window, largura, altura);

                if (altura[0] == 0) {
                    glfwPollEvents();
                    continue;
                }

                glViewport(0, 0, largura[0], altura[0]);

                glClear(
                        GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT
                );

                glMatrixMode(GL_PROJECTION);
                glLoadIdentity();

                double proporcao =
                        (double) largura[0] / altura[0];

                glFrustum(
                        -proporcao * 0.7,
                        proporcao * 0.7,
                        -0.7,
                        0.7,
                        1,
                        100
                );

                glMatrixMode(GL_MODELVIEW);
                glLoadIdentity();

                glTranslatef(0, 0, -4);

                // Sem giro automatico: o ogro fica parado, virado para a camera
                glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);

                // Inclinacao de 20 graus so enquanto segura S (antes era o contrario)
                if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
                    glRotatef(20, 1, 0, 0);
                }

                glPolygonMode(
                        GL_FRONT_AND_BACK,
                        glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS
                                ? GL_LINE
                                : GL_FILL
                );

                glColor3f(1, 1, 1);
                glEnable(GL_TEXTURE_2D);
                glBindTexture(GL_TEXTURE_2D, textureId);

                glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);

                glScalef(escala, escala, escala);
                glTranslatef(-centroX, -centroY, -centroZ);

                animator.atualizar(glfwGetTime());

                renderer.atualizarVertices();
                renderer.draw();

                glfwSwapBuffers(window);
                glfwPollEvents();
            }

        } finally {
            renderer.liberar();

            if (textureId != 0) {
                glDeleteTextures(textureId);
            }

            glfwDestroyWindow(window);
            glfwTerminate();
        }
    }
}