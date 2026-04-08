import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 Projeto 1 - Compressão de Arquivos com o Algoritmo de Huffman

 Antônio Costa Satiro de Souza  10723636
 Giovanna Borges Coelho         10756784
 Kaua Victor Oliveira de Sousa

*/

public class Huffman {
    private static final int TAM = 256;

    // atributos da classe
    private static No raizGlobal = null;
    private static String[] dicionarioGlobal = null;
    private static String bitsCodificadosGlobal = "";
    private static long originalBits = 0L;
    private static long comprimidoBits = 0L;
    private static String mensagemDecodificadaGlobal = "";
    private static String conteudoArquivoTexto = "";

    public static void main(String[] args) {
        try {
            if (args.length != 3) {
                imprimirUso();
                return;
            }

            String acao = args[0].trim();

            long inicio;
            long fim;

            switch (acao) {
                case "-c": {
                    String arquivoOriginal = args[1].trim();
                    String arquivoComprimido = args[2].trim();

                    File entrada = recuperarArquivoEntrada(arquivoOriginal);
                    File saida = prepararArquivoSaida(arquivoComprimido);
                    inicio = System.nanoTime();
                    comprimirArquivo(entrada, saida);
                    fim = System.nanoTime();
                    System.out.println("\nTempo de execução de tarefa = " + ((fim - inicio) / 1_000_000.0) + " ms");
                    break;
                }
                case "-d": {
                    String arquivoComprimido = args[1].trim();
                    String arquivoRestaurado = args[2].trim();

                    File entrada = recuperarArquivoEntrada(arquivoComprimido);
                    File saida = prepararArquivoSaida(arquivoRestaurado);
                    inicio = System.nanoTime();
                    descomprimirArquivo(entrada, saida);
                    fim = System.nanoTime();
                    System.out.println("\nTempo de execução de tarefa = " + ((fim - inicio) / 1_000_000.0) + " ms");
                    break;
                }
                default:
                    System.out.println("Ação inválida. Use -c para compressão ou -d para descompressão.");
                    imprimirUso();
            }
        } catch (Exception ex) {
            System.out.println("Erro: " + ex.getMessage());
        }
    }

    // método para imprimir os possiveis comandos no console caso o usuário entre com argumentos
    private static void imprimirUso() {
        System.out.println("Uso:");
        System.out.println("java -jar huffman.jar -c <arquivo_original> <arquivo_comprimido>");
        System.out.println("java -jar huffman.jar -d <arquivo_comprimido> <arquivo_restaurado>");
    }

    // método para validar se arquivo de entrada existe e é regular ao formato esperado
    public static File recuperarArquivoEntrada(String caminhoArquivo) throws IOException {
        Path path = Paths.get(caminhoArquivo);

        if (!Files.exists(path)) {
            throw new FileNotFoundException("Arquivo não encontrado: " + caminhoArquivo);
        }

        if (!Files.isRegularFile(path)) {
            throw new IOException("O caminho informado não é um arquivo válido: " + caminhoArquivo);
        }

        return path.toFile();
    }

    // método para preparar o arquivo de saida
    public static File prepararArquivoSaida(String caminhoArquivo) throws IOException {
        Path path = Paths.get(caminhoArquivo);

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        Files.deleteIfExists(path);
        Files.createFile(path);
        return path.toFile();
    }

    // método para extrair a frequencia de cada caracter válido dentro do arquivo de input
    public static int[] analizar(File file) {
        int[] frequencias = new int[TAM];
        StringBuilder sb = new StringBuilder();

        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
             BufferedReader leitor = new BufferedReader(inputStreamReader)) {

            int caractereLido;
            while ((caractereLido = leitor.read()) != -1) {
                if (caractereLido >= 0 && caractereLido < TAM) {
                    frequencias[caractereLido]++;
                    sb.append((char) caractereLido);
                } else {
                    System.out.println("Caractere fora do intervalo ASCII 0..255 foi ignorado: " + caractereLido);
                }
            }

            conteudoArquivoTexto = sb.toString();
            return frequencias;
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo de entrada: " + e.getMessage());
            return null;
        }
    }

    // método para imprimir quais caracteres apresentam frequencia no arquivo > 0
    public static void printFrequencias(int[] frequencias) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n--------------------------------------------------");
        sb.append("\nETAPA 1: Tabela de Frequencia de Caracteres");
        sb.append("\n--------------------------------------------------");

        for (int i = 0; i < frequencias.length; i++) {
            if (frequencias[i] > 0) {
                sb.append(String.format("\nCaractere '%s' (ASCII: %d): %d",
                        representarCaractere((char) i), i, frequencias[i]));
            }
        }

        sb.append("\n--------------------------------------------------");
        System.out.println(sb.toString());
    }

    // método para imprimir cabeçalho da terceira etapa e chamar a função recursiva imprimirArvoreRec
    public static void imprimirArvore(No raiz) {
        System.out.println("\n--------------------------------------------------");
        System.out.println("ETAPA 3: Arvore de Huffman");
        System.out.println("--------------------------------------------------");

        if (raiz == null) {
            System.out.println("(arvore vazia)");
            return;
        }

        imprimirArvoreRec(raiz, "", true);
    }

    // método para imprimir recursivamente a Arvire de caracteres
    public static void imprimirArvoreRec(No node, String prefix, boolean isTail) {
        if (node == null) {
            return;
        }

        String representacao = node.ehFolha() ? "'" + representarCaractere(node.caractere) + "'" : "RAIZ/N";
        System.out.println(prefix + (isTail ? "└── " : "├── ") + "(" + representacao + ", " + node.frequencia + ")");

        if (node.esquerda != null || node.direita != null) {
            if (node.esquerda != null && node.direita != null) {
                imprimirArvoreRec(node.esquerda, prefix + (isTail ? "    " : "│   "), false);
                imprimirArvoreRec(node.direita, prefix + (isTail ? "    " : "│   "), true);
            } else if (node.esquerda != null) {
                imprimirArvoreRec(node.esquerda, prefix + (isTail ? "    " : "│   "), true);
            } else {
                imprimirArvoreRec(node.direita, prefix + (isTail ? "    " : "│   "), true);
            }
        }
    }

    // método para gerar a tabela de endereços binários de cada caracter
    public static void gerarTabelaDeCodigos(String[] dicionario, No raiz, String caminho) {
        if (raiz == null) {
            return;
        }

        if (raiz.ehFolha()) {
            dicionario[(int) raiz.caractere] = caminho.isEmpty() ? "0" : caminho;
            return;
        }

        gerarTabelaDeCodigos(dicionario, raiz.esquerda, caminho + "0");
        gerarTabelaDeCodigos(dicionario, raiz.direita, caminho + "1");
    }

    // método para imprimir cabeçalho da quarta etapa e a tabela de endereços binários de cada caracter
    public static void imprimirTabelaDeCodigos(String[] dicionario) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n--------------------------------------------------");
        sb.append("\nETAPA 4: Tabela de Codigos de Huffman");
        sb.append("\n--------------------------------------------------");

        for (int i = 0; i < dicionario.length; i++) {
            if (dicionario[i] != null) {
                sb.append(String.format("\nCaractere '%s': %s", representarCaractere((char) i), dicionario[i]));
            }
        }

        sb.append("\n--------------------------------------------------");
        System.out.println(sb.toString());
    }

    // método para criação de nós para cada caracter com frequencia > 0
    public static MinHeap criarMinHeap(int[] frequencias) {
        MinHeap heap = new MinHeap();

        for (int i = 0; i < frequencias.length; i++) {
            if (frequencias[i] > 0) {
                heap.insert(new No((char) i, frequencias[i]));
            }
        }

        return heap;
    }

    // método para codificar os caracteres presentes no conteudoArquivoDeTexto
    public static void codificar() {
        if (conteudoArquivoTexto == null || dicionarioGlobal == null) {
            bitsCodificadosGlobal = "";
            originalBits = 0L;
            comprimidoBits = 0L;
            return;
        }

        StringBuilder codificado = new StringBuilder();
        originalBits = conteudoArquivoTexto.length() * 8L;

        for (int i = 0; i < conteudoArquivoTexto.length(); i++) {
            int indice = conteudoArquivoTexto.charAt(i);
            String codigo = dicionarioGlobal[indice];

            if (codigo == null) {
                throw new IllegalStateException("Caractere sem código no dicionário: ASCII " + indice);
            }

            codificado.append(codigo);
        }

        bitsCodificadosGlobal = codificado.toString();
        comprimidoBits = bitsCodificadosGlobal.length();
    }

    // método para decodificar os caracteres presentes em bitsCodificadosGlobal
    public static void decodificar() {
        if (raizGlobal == null) {
            mensagemDecodificadaGlobal = "";
            return;
        }

        StringBuilder saida = new StringBuilder();

        if (raizGlobal.ehFolha()) {
            for (int i = 0; i < raizGlobal.frequencia; i++) {
                saida.append(raizGlobal.caractere);
            }
            mensagemDecodificadaGlobal = saida.toString();
            return;
        }

        No atual = raizGlobal;
        for (int i = 0; i < bitsCodificadosGlobal.length(); i++) {
            char bit = bitsCodificadosGlobal.charAt(i);

            if (bit == '0') {
                atual = atual.esquerda;
            } else if (bit == '1') {
                atual = atual.direita;
            } else {
                throw new IllegalStateException("Bit inválido encontrado durante a decodificação: " + bit);
            }

            if (atual == null) {
                throw new IllegalStateException("Arquivo comprimido inválido: percurso da árvore ficou nulo.");
            }

            if (atual.ehFolha()) {
                saida.append(atual.caractere);
                atual = raizGlobal;
            }
        }

        mensagemDecodificadaGlobal = saida.toString();
    }

    // método para imprimir informações e cabeçalho da quinta etapa
    public static void imprimirResumoCompressao() {
        StringBuilder sb = new StringBuilder();

        long bytesOrig = (originalBits + 7) / 8;
        long bytesComp = (comprimidoBits + 7) / 8;
        double taxa = (originalBits == 0) ? 0.0 : (1.0 - (comprimidoBits / (double) originalBits)) * 100.0;

        sb.append("\n--------------------------------------------------");
        sb.append("\nETAPA 5: Resumo da Compressao");
        sb.append("\n--------------------------------------------------");
        sb.append(String.format("\nTamanho original....: %d bits (%d bytes)", originalBits, bytesOrig));
        sb.append(String.format("\nTamanho comprimido..: %d bits (%d bytes)", comprimidoBits, bytesComp));
        sb.append(String.format("\nTaxa de compressao..: %.2f%%", taxa));
        sb.append("\n--------------------------------------------------");

        System.out.println(sb.toString());
    }

    // método para transformar string de bits em um array de bytes para permitir gravação no arquivo
    private static byte[] transformarStringEmByte(String strBinaria) {
        if (strBinaria == null || strBinaria.isEmpty()) {
            return new byte[0];
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte resultado = 0;
        int bitPosicao = 7;

        for (int i = 0; i < strBinaria.length(); i++) {
            if (strBinaria.charAt(i) == '1') {
                byte mascara = (byte) (1 << bitPosicao);
                resultado = (byte) (resultado | mascara);
            }

            bitPosicao--;

            if (bitPosicao < 0) {
                buffer.write(resultado);
                resultado = 0;
                bitPosicao = 7;
            }
        }

        if (bitPosicao != 7) {
            buffer.write(resultado);
        }

        return buffer.toByteArray();
    }

    // método para reconstrução da heap a partir das frequencias de caracteres
    public static void descomprimirArquivo(File arquivoComprimido, File arquivoDescomprimido) {
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(arquivoComprimido))) {
            int numeroEntradas = inputStream.readInt();
            int[] tabelaDeFrequencia = new int[TAM];

            for (int i = 0; i < numeroEntradas; i++) {
                int simbolo = inputStream.readUnsignedByte();
                int frequencia = inputStream.readInt();
                tabelaDeFrequencia[simbolo] = frequencia;
            }

            long totalBits = inputStream.readLong();
            byte[] restante = inputStream.readAllBytes();

            if (numeroEntradas == 0) {
                raizGlobal = null;
                bitsCodificadosGlobal = "";
                mensagemDecodificadaGlobal = "";
                Files.writeString(arquivoDescomprimido.toPath(), "", StandardCharsets.UTF_8);
                return;
            }

            MinHeap heap = criarMinHeap(tabelaDeFrequencia);
            raizGlobal = heap.montarArvore();

            StringBuilder bitsString = new StringBuilder();
            long bitsLidos = 0;

            for (byte valorByte : restante) {
                for (int j = 7; j >= 0 && bitsLidos < totalBits; j--) {
                    boolean bit = ((valorByte >> j) & 1) == 1;
                    bitsString.append(bit ? '1' : '0');
                    bitsLidos++;
                }
            }

            if (bitsLidos != totalBits) {
                throw new IOException("Arquivo .huff inválido: quantidade de bits inconsistente.");
            }

            bitsCodificadosGlobal = bitsString.toString();
            decodificar();
            Files.writeString(arquivoDescomprimido.toPath(), mensagemDecodificadaGlobal, StandardCharsets.UTF_8);
        } catch (EOFException e) {
            System.out.println("Arquivo .huff inválido ou corrompido.");
        } catch (IOException | IllegalStateException e) {
            System.out.println("Erro ao descomprimir: " + e.getMessage());
        }
    }

    // método que realiza todo o processo de compressao do texto
    public static void comprimirArquivo(File arquivoOriginal, File arquivoComprimido) {
        try {
            int[] frequencias = analizar(arquivoOriginal);

            if (frequencias == null) {
                System.out.println("Não foi possível calcular a frequência dos caracteres.");
                return;
            }

            printFrequencias(frequencias);
            MinHeap heap = criarMinHeap(frequencias);
            heap.printHeap();

            if (heap.isEmpty()) {
                raizGlobal = null;
                dicionarioGlobal = new String[TAM];
                bitsCodificadosGlobal = "";
                originalBits = 0L;
                comprimidoBits = 0L;
                imprimirArvore(null);
                imprimirTabelaDeCodigos(dicionarioGlobal);
                imprimirResumoCompressao();
                criarArquivoComprimido(frequencias, arquivoComprimido);
                return;
            }

            No raiz = heap.montarArvore();
            imprimirArvore(raiz);

            String[] dicionario = new String[TAM];
            gerarTabelaDeCodigos(dicionario, raiz, "");
            imprimirTabelaDeCodigos(dicionario);

            raizGlobal = raiz;
            dicionarioGlobal = dicionario;

            codificar();
            imprimirResumoCompressao();
            criarArquivoComprimido(frequencias, arquivoComprimido);
        } catch (Exception ex) {
            System.out.println("Erro ao comprimir: " + ex.getMessage());
        }
    }

    // método que garante escrita dos caracteres comprimidos no arquivo
    public static void criarArquivoComprimido(int[] frequencias, File arquivoComprimido) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        try (DataOutputStream dataOutputStream = new DataOutputStream(buffer)) {
            int numeroEntradas = 0;

            for (int i = 0; i < TAM; i++) {
                if (frequencias[i] > 0) {
                    numeroEntradas++;
                }
            }

            dataOutputStream.writeInt(numeroEntradas);

            for (int i = 0; i < TAM; i++) {
                if (frequencias[i] > 0) {
                    dataOutputStream.writeByte(i);
                    dataOutputStream.writeInt(frequencias[i]);
                }
            }

            dataOutputStream.flush();
        } catch (IOException e) {
            System.out.println("Erro ao montar o cabeçalho do arquivo comprimido: " + e.getMessage());
            return;
        }

        long totalBits = (bitsCodificadosGlobal == null) ? 0L : bitsCodificadosGlobal.length();
        byte[] dadosComprimidos = transformarStringEmByte(bitsCodificadosGlobal);

        try (DataOutputStream out = new DataOutputStream(new FileOutputStream(arquivoComprimido))) {
            out.write(buffer.toByteArray());
            out.writeLong(totalBits);
            out.write(dadosComprimidos);
        } catch (IOException e) {
            System.out.println("Erro ao escrever o arquivo comprimido: " + e.getMessage());
        }
    }

    // método que transforma caracteres especiais em versoes capazes de serem interpretadas 
    private static String representarCaractere(char caractere) {
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
}