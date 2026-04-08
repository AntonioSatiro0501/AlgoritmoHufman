/**
 Projeto 1 - Compressão de Arquivos com o Algoritmo de Huffman

 Antônio Costa Satiro de Souza  10723636
 Giovanna Borges Coelho         10756784
 Kaua Victor Oliveira de Sousa

*/

public class No implements Comparable<No> {
    char caractere;
    int frequencia;
    No esquerda;
    No direita;

    public No(char caractere, int frequencia) {
        this.caractere = caractere;
        this.frequencia = frequencia;
        this.esquerda = null;
        this.direita = null;
    }

    public boolean ehFolha() {
        return esquerda == null && direita == null;
    }

    @Override
    public int compareTo(No outroNo) {
        return Integer.compare(this.frequencia, outroNo.frequencia);
    }
}