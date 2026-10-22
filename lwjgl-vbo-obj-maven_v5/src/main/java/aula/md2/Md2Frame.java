
package aula.md2;

import aula.obj.Vector3f;

public class Md2Frame {

    public String nome;
    public Vector3f[] vertices;

    public Md2Frame(String nome, int quantidadeVertices) {
        this.nome = nome;
        this.vertices = new Vector3f[quantidadeVertices];
    }

    public void adicionarVertice(int indice, float x, float y, float z) {
        vertices[indice] = new Vector3f(x, y, z);
    }

    public Vector3f obterVertice(int indice) {
        return vertices[indice];
    }

    public int obterQuantidadeVertices() {
        return vertices.length;
    }
}
