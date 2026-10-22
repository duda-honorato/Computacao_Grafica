
package aula.md2;

import aula.obj.Vector2f;
import aula.obj.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

public class Md2Renderer {

    private final Md2Model modelo;
    private final Md2Animator animator;

    private int vboVertices;
    private int vboTexturas;
    private int quantidadeVertices;

    private FloatBuffer bufferVertices;

    public Md2Renderer(Md2Model modelo, Md2Animator animator) {
        this.modelo = modelo;
        this.animator = animator;
    }

    public void load() {
        quantidadeVertices = modelo.obterQuantidadeTriangulos() * 3;

        bufferVertices = BufferUtils.createFloatBuffer(
            quantidadeVertices * 3
        );

        FloatBuffer bufferTexturas = BufferUtils.createFloatBuffer(
            quantidadeVertices * 2
        );

        for (int indiceTriangulo = 0;
             indiceTriangulo < modelo.obterQuantidadeTriangulos();
             indiceTriangulo++) {

            for (int indiceVertice = 0; indiceVertice < 3; indiceVertice++) {

                int indiceTextura =
                    modelo.indicesTexturas[indiceTriangulo][indiceVertice];

                Vector2f coordenada =
                    modelo.coordenadasTextura.get(indiceTextura);

                bufferTexturas.put(coordenada.x);
                bufferTexturas.put(coordenada.y);
            }
        }

        bufferTexturas.flip();

        vboVertices = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);

        glBufferData(
            GL_ARRAY_BUFFER,
            (long) quantidadeVertices * 3 * Float.BYTES,
            GL_DYNAMIC_DRAW
        );

        vboTexturas = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTexturas);

        glBufferData(
            GL_ARRAY_BUFFER,
            bufferTexturas,
            GL_STATIC_DRAW
        );

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void atualizarVertices() {
        bufferVertices.clear();

        for (int indiceTriangulo = 0;
             indiceTriangulo < modelo.obterQuantidadeTriangulos();
             indiceTriangulo++) {

            for (int indiceVertice = 0; indiceVertice < 3; indiceVertice++) {

                int indiceOriginal =
                    modelo.indicesVertices[indiceTriangulo][indiceVertice];

                Vector3f vertice =
                    animator.interpolarVertice(indiceOriginal);

                bufferVertices.put(vertice.x);
                bufferVertices.put(vertice.y);
                bufferVertices.put(vertice.z);
            }
        }

        bufferVertices.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);

        glBufferSubData(
            GL_ARRAY_BUFFER,
            0,
            bufferVertices
        );

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void draw() {
        glEnableClientState(GL_VERTEX_ARRAY);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertices);
        glVertexPointer(3, GL_FLOAT, 0, 0L);

        glBindBuffer(GL_ARRAY_BUFFER, vboTexturas);
        glTexCoordPointer(2, GL_FLOAT, 0, 0L);

        glDrawArrays(
            GL_TRIANGLES,
            0,
            quantidadeVertices
        );

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

    public void liberar() {
        if (vboVertices != 0) {
            glDeleteBuffers(vboVertices);
            vboVertices = 0;
        }

        if (vboTexturas != 0) {
            glDeleteBuffers(vboTexturas);
            vboTexturas = 0;
        }
    }
}
