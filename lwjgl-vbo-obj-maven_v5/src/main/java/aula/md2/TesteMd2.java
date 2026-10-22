
package aula.md2;

public class TesteMd2 {

    public static void main(String[] args) {
        try {
            Md2Model modelo = new Md2Model(
                "src/main/resources/ogro/ogro.md2"
            );

            modelo.exibirFrames();

            System.out.println(
                "Total de frames carregados: "
                    + modelo.obterQuantidadeFrames()
            );

            System.out.println(
                "Total de triangulos: "
                    + modelo.obterQuantidadeTriangulos()
            );

        } catch (Exception erro) {
            erro.printStackTrace();
        }
    }
}
