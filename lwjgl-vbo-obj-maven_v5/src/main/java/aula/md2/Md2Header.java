
package aula.md2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Md2Header {

    public int ident;
    public int version;
    public int skinWidth;
    public int skinHeight;
    public int frameSize;
    public int numSkins;
    public int numVertices;
    public int numTextureCoordinates;
    public int numTriangles;
    public int numGlCommands;
    public int numFrames;
    public int offsetSkins;
    public int offsetTextureCoordinates;
    public int offsetTriangles;
    public int offsetFrames;
    public int offsetGlCommands;
    public int offsetEnd;

    public Md2Header(ByteBuffer buffer) throws IOException {
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        ident = buffer.getInt();
        version = buffer.getInt();
        skinWidth = buffer.getInt();
        skinHeight = buffer.getInt();
        frameSize = buffer.getInt();
        numSkins = buffer.getInt();
        numVertices = buffer.getInt();
        numTextureCoordinates = buffer.getInt();
        numTriangles = buffer.getInt();
        numGlCommands = buffer.getInt();
        numFrames = buffer.getInt();
        offsetSkins = buffer.getInt();
        offsetTextureCoordinates = buffer.getInt();
        offsetTriangles = buffer.getInt();
        offsetFrames = buffer.getInt();
        offsetGlCommands = buffer.getInt();
        offsetEnd = buffer.getInt();

        validar();
    }

    private void validar() throws IOException {
        if (ident != 844121161) {
            throw new IOException("Arquivo nao possui assinatura MD2 valida.");
        }

        if (version != 8) {
            throw new IOException("Versao MD2 nao suportada: " + version);
        }

        if (numVertices <= 0 || numFrames <= 0 || numTriangles <= 0) {
            throw new IOException("Geometria MD2 invalida.");
        }

        if (skinWidth <= 0 || skinHeight <= 0) {
            throw new IOException("Dimensoes da textura invalidas.");
        }

        if (frameSize < 40L + 4L * numVertices) {
            throw new IOException("Tamanho de frame MD2 invalido.");
        }

        if (offsetEnd < 68) {
            throw new IOException("Tamanho total MD2 invalido.");
        }
    }

    public void exibirInformacoes() {
        System.out.println("Versao MD2: " + version);
        System.out.println("Vertices por frame: " + numVertices);
        System.out.println("Triangulos: " + numTriangles);
        System.out.println("Frames: " + numFrames);
        System.out.println("Coordenadas de textura: " + numTextureCoordinates);
        System.out.println("Largura da textura: " + skinWidth);
        System.out.println("Altura da textura: " + skinHeight);
    }
}
