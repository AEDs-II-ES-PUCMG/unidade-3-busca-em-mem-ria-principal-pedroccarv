import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

    /** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;

    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente */
    static int quantosProdutos = 0;

    static AVL<String, Produto> produtosCadastradosPorNome;
    static AVL<Integer, Produto> produtosCadastradosPorId;
    static AVL<Integer, Cliente> clientesCadastrados;

    static TabelaHash<Produto, Lista<Pedido>> pedidosPorProduto;
    static TabelaHash<Cliente, Lista<Pedido>> pedidosPorCliente;

    static int quantosClientes = 0;

    static TabelaHash<Pedido, Lista<Pedido>> historicoDePedidosPorCliente;

    /** Limpa o buffer do console */
    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void pausa() {
        System.out.println("Pressione ENTER para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        limparTela();
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }

    /**
     * Método genérico para ler dados numéricos do teclado.
     */
    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
        T valor;
        System.out.println(mensagem);
        try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }

    /**
     * Imprime o menu principal, lê a opção do usuário e a retorna.
     */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar produto, por nome");
        System.out.println("3 - Procurar produto, por id");
        System.out.println("4 - Remover produto, por nome");
        System.out.println("5 - Remover produto, por id");
        System.out.println("6 - Recortar a lista de produtos, por nome");
        System.out.println("7 - Recortar a lista de produtos, por id");
        System.out.println("8 - Gravar, em arquivo, pedidos de um produto");
        System.out.println("9 - Exibir histórico de compras de um cliente");
        System.out.println("0 - Finalizar");
        return lerOpcao("Digite sua opção: ", Integer.class);
    }

    /**
     * Lê os dados de um arquivo-texto e retorna uma AVL de produtos.
     */
    static <K> AVL<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
        Scanner arquivo = null;
        int numProdutos;
        String linha;
        Produto produto;
        AVL<K, Produto> produtosCadastrados;

        try {
            arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
            numProdutos = Integer.parseInt(arquivo.nextLine());
            produtosCadastrados = new AVL<K, Produto>();

            for (int i = 0; i < numProdutos; i++) {
                linha = arquivo.nextLine();
                produto = Produto.criarDoTexto(linha);
                K chave = extratorDeChave.apply(produto);
                produtosCadastrados.inserir(chave, produto);
            }
            quantosProdutos = numProdutos;

        } catch (IOException excecaoArquivo) {
            produtosCadastrados = null;
        } finally {
            if (arquivo != null) arquivo.close();
        }

        return produtosCadastrados;
    }

    /**
     * Lê os dados de um arquivo-texto e retorna uma AVL de clientes.
     */
    static AVL<Integer, Cliente> lerClientes(String nomeArquivo) {
        clientesCadastrados = new AVL<>();
        int numClientes;
        String linha;

        try (Scanner arquivo = new Scanner(new File(nomeArquivo), Charset.forName("UTF-8"))) {
            numClientes = Integer.parseInt(arquivo.nextLine());
            quantosClientes = numClientes;

            for (int i = 0; i < numClientes; i++) {
                linha = arquivo.nextLine();
                try {
                    Cliente cliente = new Cliente(linha);
                    clientesCadastrados.inserir(cliente.hashCode(), cliente);
                } catch (IllegalArgumentException e) {
                    System.out.println("Linha inválida");
                }
            }
        } catch (IOException e) {
            System.out.println("Arquivo não encontrado");
        }

        return clientesCadastrados;
    }

    static <K> Produto localizarProduto(AVL<K, Produto> produtosCadastrados, K procurado) {
        Produto produto;

        cabecalho();
        System.out.println("Localizando um produto...");

        try {
            produto = produtosCadastrados.pesquisar(procurado);
        } catch (NoSuchElementException excecao) {
            produto = null;
        }

        System.out.println("Número de comparações realizadas: " + produtosCadastrados.getComparacoes());
        System.out.println("Tempo de processamento da pesquisa: " + produtosCadastrados.getTempo() + " ms");

        return produto;
    }

    /** Localiza um produto na AVL por id */
    static Produto localizarProdutoID(AVL<Integer, Produto> produtosCadastrados) {
        int idProduto = lerOpcao("Digite o identificador do produto desejado: ", Integer.class);
        return localizarProduto(produtosCadastrados, idProduto);
    }

    /** Localiza um produto na AVL por nome */
    static Produto localizarProdutoNome(AVL<String, Produto> produtosCadastrados) {
        System.out.println("Digite o nome ou a descrição do produto desejado:");
        String descricao = teclado.nextLine();
        return localizarProduto(produtosCadastrados, descricao);
    }

    private static void mostrarProduto(Produto produto) {
        cabecalho();
        StringBuilder mensagem = new StringBuilder("Produto não encontrado.\n");

        if (produto != null)
            mensagem = new StringBuilder(String.format("%s\n", produto));

        System.out.println(mensagem.toString());
    }

    /** Lista todos os produtos cadastrados */
    static <K> void listarTodosOsProdutos(AVL<K, Produto> produtosCadastrados) {
        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        System.out.println(produtosCadastrados.toString());
    }

    /** Remove um produto da AVL por id */
    static Produto removerProdutoId(AVL<Integer, Produto> produtosCadastrados) {
        cabecalho();
        System.out.println("Localizando o produto por id");
        int id = lerOpcao("Digite o id do produto que deve ser removido", Integer.class);
        return removerProduto(produtosCadastrados, id);
    }

    /** Remove um produto da AVL por nome */
    static Produto removerProdutoNome(AVL<String, Produto> produtosCadastrados) {
        cabecalho();
        System.out.println("Localizando o produto por nome");
        System.out.print("Digite a descrição do produto que deve ser removido: ");
        String descricao = teclado.nextLine();
        return removerProduto(produtosCadastrados, descricao);
    }

    static <K> Produto removerProduto(AVL<K, Produto> produtosCadastrados, K chave) {
        cabecalho();
        return produtosCadastrados.remover(chave);
    }

    private static <K> void recortarProduto(AVL<K, Produto> produtosCadastrados, K deOnde, K ateOnde) {
        cabecalho();
        System.out.println(produtosCadastrados.recortar(deOnde, ateOnde).toString());
    }

    private static void recortarProdutosNome(AVL<String, Produto> produtosCadastrados) {
        cabecalho();
        System.out.print("Digite o nome do primeiro produto do filtro: ");
        String descricaoDeOnde = teclado.nextLine();
        System.out.print("Digite o nome do último produto do filtro: ");
        String descricaoAteOnde = teclado.nextLine();
        recortarProduto(produtosCadastrados, descricaoDeOnde, descricaoAteOnde);
    }

    private static void recortarProdutosId(AVL<Integer, Produto> produtosCadastrados) {
        cabecalho();
        int idDeOnde = lerOpcao("Digite o id do primeiro produto do filtro", Integer.class);
        int idAteOnde = lerOpcao("Digite o id do último produto do filtro", Integer.class);
        recortarProduto(produtosCadastrados, idDeOnde, idAteOnde);
    }

    private static Lista<Pedido> gerarPedidos(int quantidade) {
        Lista<Pedido> pedidos = new Lista<>();
        Random sorteio = new Random(42);
        int quantProdutos;
        int formaDePagamento;
        int quant;
        int idCliente;
        Cliente cliente;

        for (int i = 0; i < quantidade; i++) {
            formaDePagamento = sorteio.nextInt(2) + 1;

            // TODO: selecione aleatoriamente um cliente para este pedido.
            // Sorteie um documento de cliente (use sorteio.nextInt(quantosClientes) + 10_000)
            // e localize o cliente correspondente em clientesCadastrados.
            idCliente = sorteio.nextInt(quantosClientes) + 10_000;
            try {
                cliente = clientesCadastrados.pesquisar(idCliente);
            } catch (NoSuchElementException e) {
                cliente = null;
            }

            Pedido pedido = new Pedido(LocalDate.now(), formaDePagamento, cliente);
            quantProdutos = sorteio.nextInt(8) + 1;

            for (int j = 0; j < quantProdutos; j++) {
                int id = sorteio.nextInt(7750) + 10_000;
                Produto produto = produtosCadastradosPorId.pesquisar(id);
                quant = sorteio.nextInt(10) + 1;
                pedido.incluirProduto(produto, quant);
                inserirNaTabela(produto, pedido);
            }

            pedidos.inserir(pedido);

            // TODO: vincule o cliente sorteado ao seu novo pedido na tabela hash pedidosPorCliente,
            if (cliente != null) {
                inserirNaTabelaPedidosDoCliente(cliente, pedido);
            }
        }
        return pedidos;
    }

    /**
     * Associa, na tabela hash pedidosPorCliente, o pedido ao histórico de pedidos do cliente.
     * Caso o cliente ainda não possua histórico registrado, um novo é criado.
     */
    private static void inserirNaTabelaPedidosDoCliente(Cliente cliente, Pedido pedido) {
        Lista<Pedido> historico;
        try {
            historico = pedidosPorCliente.pesquisar(cliente);
        } catch (NoSuchElementException excecao) {
            historico = new Lista<>();
            pedidosPorCliente.inserir(cliente, historico);
        }
        historico.inserir(pedido);
    }

    private static void inserirNaTabela(Produto produto, Pedido pedido) {
        Lista<Pedido> pedidosDoProduto;

        try {
            pedidosDoProduto = pedidosPorProduto.pesquisar(produto);
        } catch (NoSuchElementException excecao) {
            pedidosDoProduto = new Lista<>();
            pedidosPorProduto.inserir(produto, pedidosDoProduto);
        }
        pedidosDoProduto.inserir(pedido);
    }

    private static void pedidosDoProduto() {
        Lista<Pedido> pedidosDoProduto;
        Produto produto = localizarProdutoID(produtosCadastradosPorId);
        String nomeArquivo = "RelatorioProduto" + produto.hashCode() + ".txt";

        try {
            FileWriter arquivoRelatorio = new FileWriter(nomeArquivo, Charset.forName("UTF-8"));
            pedidosDoProduto = pedidosPorProduto.pesquisar(produto);
            arquivoRelatorio.append(pedidosDoProduto.toString() + "\n");
            arquivoRelatorio.close();
            System.out.println("Dados salvos em " + nomeArquivo);
        } catch (IOException excecao) {
            System.out.println("Problemas para criar o arquivo " + nomeArquivo + ". Tente novamente");
        }
    }

    /**
     * Lê o documento de um cliente, localiza-o e exibe seu histórico de pedidos.
     */
    public static void pedidosDoCliente() {
        Integer docCliente = lerOpcao("Digite documento: ", Integer.class);

        if (docCliente == null)
            return;

        Cliente cliente;
        try {
            cliente = clientesCadastrados.pesquisar(docCliente);
        } catch (NoSuchElementException e) {
            System.out.println("Cliente não encontrado");
            return;
        }

        System.out.println("Histórico de pedidos - cliente: " + cliente.getNome());

        Lista<Pedido> historico;
        try {
            historico = pedidosPorCliente.pesquisar(cliente);
        } catch (NoSuchElementException e) {
            historico = null;
        }

        if (historico == null)
            System.out.println("Nenhum pedido registrado");
        else
            System.out.println(historico.toString());
    }

    public static void main(String[] args) {
        teclado = new Scanner(System.in, Charset.forName("UTF-8"));

        nomeArquivoDados = "produtos.txt";
        produtosCadastradosPorNome = lerProdutos(nomeArquivoDados, (p -> p.descricao));
        produtosCadastradosPorId = new AVL<Integer, Produto>(produtosCadastradosPorNome, (p -> p.idProduto));
        nomeArquivoDados = "clientes.txt";
        clientesCadastrados = lerClientes(nomeArquivoDados);

        pedidosPorProduto = new TabelaHash<>((int) (quantosProdutos * 1.25));
        pedidosPorCliente = new TabelaHash<>((int) (quantosClientes * 1.25));

        gerarPedidos(25_000);

        historicoDePedidosPorCliente = new TabelaHash<>(quantosClientes);

        int opcao = -1;

        do {
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos(produtosCadastradosPorNome);
                case 2 -> mostrarProduto(localizarProdutoNome(produtosCadastradosPorNome));
                case 3 -> mostrarProduto(localizarProdutoID(produtosCadastradosPorId));
                case 4 -> mostrarProduto(removerProdutoNome(produtosCadastradosPorNome));
                case 5 -> mostrarProduto(removerProdutoId(produtosCadastradosPorId));
                case 6 -> recortarProdutosNome(produtosCadastradosPorNome);
                case 7 -> recortarProdutosId(produtosCadastradosPorId);
                case 8 -> pedidosDoProduto();
                case 9 -> pedidosDoCliente();
                case 0 -> System.out.println("FLW VLW OBG VLT SMP.");
            }
            pausa();
        } while (opcao != 0);

        teclado.close();
    }
}