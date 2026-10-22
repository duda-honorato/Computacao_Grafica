
package aula.md2;

import aula.obj.Vector2f;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class Md2Model {

    public Md2Header header;
    public ArrayList<Md2Frame> frames;
    public ArrayList<Vector2f> coordenadasTextura;

    public int[][] indicesVertices;
    public int[][] indicesTexturas;

    private final Path caminhoArquivo;

    public Md2Model(String arquivo) throws IOException {
        caminhoArquivo = Path.of(arquivo);

        frames = new ArrayList<>();
        coordenadasTextura = new ArrayList<>();

        load();
    }

    public void load() throws IOException {
        if (!Files.isRegularFile(caminhoArquivo)) {
            throw new IOException("Arquivo MD2 nao encontrado: " + caminhoArquivo);
        }

        byte[] conteudoArquivo = Files.readAllBytes(caminhoArquivo);

        if (conteudoArquivo.length < 68) {
            throw new IOException("Arquivo MD2 incompleto.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(conteudoArquivo);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        header = new Md2Header(buffer);

        validarEstrutura(conteudoArquivo.length);

        frames.clear();
        coordenadasTextura.clear();

        indicesVertices = new int[header.numTriangles][3];
        indicesTexturas = new int[header.numTriangles][3];

        carregarCoordenadasTextura(buffer);
        carregarTriangulos(buffer);
        carregarFrames(buffer);

        System.out.println("Modelo MD2 carregado: " + caminhoArquivo);
        header.exibirInformacoes();
    }

    private void validarEstrutura(int tamanhoArquivo) throws IOException {
        if (header.offsetEnd > tamanhoArquivo) {
            throw new IOException("O arquivo MD2 esta incompleto.");
        }

        if (header.offsetEnd != tamanhoArquivo) {
            throw new IOException("Tamanho do arquivo MD2 inconsistente.");
        }

        validarBloco(
            header.offsetTextureCoordinates,
            (long) header.numTextureCoordinates * 4,
            tamanhoArquivo
        );

        validarBloco(
            header.offsetTriangles,
            (long) header.numTriangles * 12,
            tamanhoArquivo
        );

        validarBloco(
            header.offsetFrames,
            (long) header.numFrames * header.frameSize,
            tamanhoArquivo
        );
    }

    private void validarBloco(
        int deslocamento,
        long quantidadeBytes,
        int tamanhoArquivo
    ) throws IOException {

        if (deslocamento < 68 || quantidadeBytes < 0) {
            throw new IOException("Estrutura MD2 invalida.");
        }

        long fimBloco = (long) deslocamento + quantidadeBytes;

        if (fimBloco > header.offsetEnd || fimBloco > tamanhoArquivo) {
            throw new IOException("Bloco MD2 fora dos limites do arquivo.");
        }
    }

    private void carregarCoordenadasTextura(ByteBuffer buffer) {
        buffer.position(header.offsetTextureCoordinates);

        for (int indice = 0; indice < header.numTextureCoordinates; indice++) {
            short coordenadaS = buffer.getShort();
            short coordenadaT = buffer.getShort();

            float u = (float) coordenadaS / header.skinWidth;
            float v = 1.0f - (float) coordenadaT / header.skinHeight;

            coordenadasTextura.add(new Vector2f(u, v));
        }
    }

    private void carregarTriangulos(ByteBuffer buffer) throws IOException {
        buffer.position(header.offsetTriangles);

        for (int indiceTriangulo = 0;
             indiceTriangulo < header.numTriangles;
             indiceTriangulo++) {

            for (int indiceVertice = 0; indiceVertice < 3; indiceVertice++) {
                int indice = Short.toUnsignedInt(buffer.getShort());

                if (indice >= header.numVertices) {
                    throw new IOException("Indice de vertice MD2 invalido.");
                }

                indicesVertices[indiceTriangulo][indiceVertice] = indice;
            }

            for (int indiceTextura = 0; indiceTextura < 3; indiceTextura++) {
                int indice = Short.toUnsignedInt(buffer.getShort());

                if (indice >= header.numTextureCoordinates) {
                    throw new IOException("Indice de textura MD2 invalido.");
                }

                indicesTexturas[indiceTriangulo][indiceTextura] = indice;
            }
        }
    }

    private void carregarFrames(ByteBuffer buffer) {
        for (int indiceFrame = 0;
             indiceFrame < header.numFrames;
             indiceFrame++) {

            int inicioFrame = header.offsetFrames
                + indiceFrame * header.frameSize;

            buffer.position(inicioFrame);

            float escalaX = buffer.getFloat();
            float escalaY = buffer.getFloat();
            float escalaZ = buffer.getFloat();

            float translacaoX = buffer.getFloat();
            float translacaoY = buffer.getFloat();
            float translacaoZ = buffer.getFloat();

            byte[] bytesNome = new byte[16];
            buffer.get(bytesNome);

            int comprimentoNome = 0;

            while (comprimentoNome < bytesNome.length
                && bytesNome[comprimentoNome] != 0) {
                comprimentoNome++;
            }

            String nomeFrame = new String(
                bytesNome,
                0,
                comprimentoNome,
                StandardCharsets.US_ASCII
            );

            Md2Frame frame = new Md2Frame(
                nomeFrame,
                header.numVertices
            );

            for (int indiceVertice = 0;
                 indiceVertice < header.numVertices;
                 indiceVertice++) {

                int xCompactado = Byte.toUnsignedInt(buffer.get());
                int yCompactado = Byte.toUnsignedInt(buffer.get());
                int zCompactado = Byte.toUnsignedInt(buffer.get());

                buffer.get();

                float x = xCompactado * escalaX + translacaoX;
                float y = yCompactado * escalaY + translacaoY;
                float z = zCompactado * escalaZ + translacaoZ;

                frame.adicionarVertice(indiceVertice, x, y, z);
            }

            frames.add(frame);
        }
    }

    public Md2Frame obterFrame(int indice) {
        return frames.get(indice);
    }

    public int obterQuantidadeFrames() {
        return frames.size();
    }

    public int obterQuantidadeTriangulos() {
        return indicesVertices.length;
    }

    public void exibirFrames() {
        for (int indice = 0; indice < frames.size(); indice++) {
            Md2Frame frame = frames.get(indice);

            System.out.println(
                "Frame " + indice + ": " + frame.nome
            );
        }
    }
}
