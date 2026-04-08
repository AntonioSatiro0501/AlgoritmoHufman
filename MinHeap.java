import java.util.ArrayList;

/**
 Projeto 1 - Compressão de Arquivos com o Algoritmo de Huffman

 Antônio Costa Satiro de Souza  10723636
 Giovanna Borges Coelho         10756784
 Kaua Victor Oliveira de Sousa

*/

public class MinHeap {
    private final ArrayList<No> heap;

    public MinHeap() {
        this.heap = new ArrayList<>();
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    // insere um novo nó no heap
    // adiciona o nó no final e faz ajuste para cima para manter a propriedade de min-heap
    public void insert(No node) {
        if (node == null) {
            return;
        }
        heap.add(node);
        heapifyUp(heap.size() - 1);
    }

    // remove e retorna o elemento mínimo do heap
    // extrai a raiz, coloca o último elemento no topo e faz ajuste para baixo
    public No extractMin() {
        if (heap.isEmpty()) {
            return null;
        }

        No min = heap.get(0);
        No last = heap.remove(heap.size() - 1);

        if (!heap.isEmpty()) {
            heap.set(0, last);
            heapifyDown(0);
        }

        return min;
    }

    // retorna o elemento mínimo sem remover
    public No peek() {
        if (heap.isEmpty()) {
            return null;
        }
        return heap.get(0);
    }

    // ajusta o heap para cima
    // compara nó com pai e troca se for menor, repetindo até alcançar a raiz
    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap.get(index).compareTo(heap.get(parent)) < 0) {
                swap(index, parent);
                index = parent;
            } else {
                break;
            }
        }
    }

    // ajusta o heap para baixo
    // compara nó com filhos e troca com o menor, repetindo até restaurar propriedade
    private void heapifyDown(int index) {
        int size = heap.size();

        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;

            if (left < size && heap.get(left).compareTo(heap.get(smallest)) < 0) {
                smallest = left;
            }
            if (right < size && heap.get(right).compareTo(heap.get(smallest)) < 0) {
                smallest = right;
            }

            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break;
            }
        }
    }

    // troca dois elementos no heap
    private void swap(int i, int j) {
        No temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }

    public void printHeap() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n--------------------------------------------------");
        sb.append("\nETAPA 2: Min-Heap Inicial (Vetor)");
        sb.append("\n--------------------------------------------------");
        sb.append("\n[ ");

        ArrayList<No> copiaOrdenada = new ArrayList<>(heap);
        copiaOrdenada.sort(No::compareTo);

        for (int i = 0; i < copiaOrdenada.size(); i++) {
            No no = copiaOrdenada.get(i);
            String representacao = representarCaractere(no.caractere);
            sb.append(String.format("No('%s',%d)", representacao, no.frequencia));
            if (i < copiaOrdenada.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append(" ]");
        System.out.println(sb.toString());
    }

    private String representarCaractere(char caractere) {
        if (caractere == '\n') {
            return "\\n";
        }
        if (caractere == '\r') {
            return "\\r";
        }
        if (caractere == '\t') {
            return "\\t";
        }
        if (caractere == '\0') {
            return "\\0";
        }
        return String.valueOf(caractere);
    }

    // constroi a arvore de Huffman
    // extrai os dois menores nos, cria um novo no pai com a soma das frequências
    // e reinsere na fila repetindo até restar apenas um nó (a raiz da arvore)
    public No montarArvore() {
        if (this.isEmpty()) {
            return null;
        }

        while (this.size() > 1) {
            No primeiroMenor = this.extractMin();
            No segundoMenor = this.extractMin();

            No novo = new No('\0', primeiroMenor.frequencia + segundoMenor.frequencia);
            novo.esquerda = primeiroMenor;
            novo.direita = segundoMenor;

            this.insert(novo);
        }

        return this.peek();
    }
}