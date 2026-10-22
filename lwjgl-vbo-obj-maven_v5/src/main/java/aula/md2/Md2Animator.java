
package aula.md2;

import aula.obj.Vector3f;

public class Md2Animator {

    private final Md2Model modelo;

    private int frameInicial;
    private int frameFinal;
    private int frameAtual;
    private int proximoFrame;

    private float framesPorSegundo;
    private float interpolacao;

    private double ultimoTempo;

    public Md2Animator(Md2Model modelo) {
        this.modelo = modelo;

        frameInicial = 40;
        frameFinal = 45;
        frameAtual = frameInicial;
        proximoFrame = frameAtual + 1;

        framesPorSegundo = 8.0f;
        interpolacao = 0.0f;

        ultimoTempo = -1.0;
    }

    public void selecionarAnimacao(
        int inicio,
        int fim,
        float velocidade
    ) {
        if (inicio < 0 || fim >= modelo.obterQuantidadeFrames()) {
            throw new IllegalArgumentException(
                "Intervalo de frames invalido."
            );
        }

        if (inicio > fim) {
            throw new IllegalArgumentException(
                "O frame inicial nao pode ser maior que o final."
            );
        }

        if (velocidade <= 0) {
            throw new IllegalArgumentException(
                "A velocidade deve ser positiva."
            );
        }

        frameInicial = inicio;
        frameFinal = fim;
        frameAtual = inicio;

        proximoFrame = frameAtual < frameFinal
            ? frameAtual + 1
            : frameInicial;

        framesPorSegundo = velocidade;
        interpolacao = 0.0f;
        ultimoTempo = -1.0;
    }

    public void atualizar(double tempoAtual) {
        if (ultimoTempo < 0) {
            ultimoTempo = tempoAtual;
            return;
        }

        double tempoDecorrido = tempoAtual - ultimoTempo;
        ultimoTempo = tempoAtual;

        if (tempoDecorrido <= 0) {
            return;
        }

        if (frameInicial == frameFinal) {
            interpolacao = 0.0f;
            return;
        }

        double progresso = interpolacao
            + tempoDecorrido * framesPorSegundo;

        int quantidadeAvancos = (int) Math.floor(progresso);
        interpolacao = (float) (progresso - quantidadeAvancos);

        int quantidadeFrames = frameFinal - frameInicial + 1;

        int posicaoAtual = frameAtual - frameInicial;

        posicaoAtual = (posicaoAtual
            + quantidadeAvancos % quantidadeFrames)
            % quantidadeFrames;

        frameAtual = frameInicial + posicaoAtual;

        proximoFrame = frameAtual < frameFinal
            ? frameAtual + 1
            : frameInicial;
    }

    public Vector3f interpolarVertice(int indiceVertice) {
        Md2Frame primeiroFrame = modelo.obterFrame(frameAtual);
        Md2Frame segundoFrame = modelo.obterFrame(proximoFrame);

        Vector3f primeiroVertice =
            primeiroFrame.obterVertice(indiceVertice);

        Vector3f segundoVertice =
            segundoFrame.obterVertice(indiceVertice);

        float x = primeiroVertice.x
            + (segundoVertice.x - primeiroVertice.x) * interpolacao;

        float y = primeiroVertice.y
            + (segundoVertice.y - primeiroVertice.y) * interpolacao;

        float z = primeiroVertice.z
            + (segundoVertice.z - primeiroVertice.z) * interpolacao;

        return new Vector3f(x, y, z);
    }

    public int obterFrameAtual() {
        return frameAtual;
    }

    public int obterProximoFrame() {
        return proximoFrame;
    }

    public float obterInterpolacao() {
        return interpolacao;
    }
}
