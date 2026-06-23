import java.util.NoSuchElementException;

public class TabelaHash<K, V> implements IMapeamento<K, V> {

	private Lista<Entrada<K, V>>[] tabelaHash;
	private int capacidade;

	private int comparacoes;
	private long inicio;
	private long termino;

	/**
	 * Construtor da classe.
	 * Inicializa a tabela hash com endereçamento em separado.
	 */
	@SuppressWarnings("unchecked")
	public TabelaHash(int capacidade) {
		if (capacidade < 1) {
			throw new IllegalStateException("A capacidade da tabela hash não pode ser menor do que 1.");
		}
		this.capacidade = capacidade;
		tabelaHash = (Lista<Entrada<K, V>>[]) new Lista[capacidade];

		for (int i = 0; i < capacidade; i++)
			tabelaHash[i] = new Lista<>();
	}

	/**
	 * Função hash: calcula a posição na tabela para uma dada chave.
	 */
	private int funcaoHash(K chave) {
		return Math.abs(chave.hashCode() % capacidade);
	}

	/**
	 * Insere um item na tabela hash.
	 * @throws IllegalArgumentException se um item com a mesma chave já existir.
	 */
	@Override
	public int inserir(K chave, V item) {
		comparacoes = 1;
		int posicao = funcaoHash(chave);
		Entrada<K, V> entrada = new Entrada<>(chave, item);

		try {
			tabelaHash[posicao].pesquisar(entrada);
			comparacoes += tabelaHash[posicao].getComparacoes();
			throw new IllegalArgumentException("O item já havia sido inserido anteriormente na tabela hash!");
		} catch (NoSuchElementException excecao) {
			tabelaHash[posicao].inserir(entrada);
			return posicao;
		}
	}

	/**
	 * Pesquisa um item na tabela hash pela chave.
	 * @throws NoSuchElementException caso o item não seja localizado.
	 */
	@Override
	public V pesquisar(K chave) {
		comparacoes = 1;
		int posicao = funcaoHash(chave);
		Entrada<K, V> procurado = new Entrada<>(chave, null);

		inicio = System.nanoTime();
		procurado = tabelaHash[posicao].pesquisar(procurado);
		comparacoes += tabelaHash[posicao].getComparacoes();
		termino = System.nanoTime();

		return procurado.getValor();
	}

	/**
	 * Remove um item da tabela hash pela chave.
	 * @throws NoSuchElementException caso o item não seja localizado.
	 */
	@Override
	public V remover(K chave) {
		comparacoes = 1;
		int posicao = funcaoHash(chave);
		Entrada<K, V> procurado = new Entrada<>(chave, null);

		procurado = tabelaHash[posicao].remover(procurado);
		return procurado.getValor();
	}

	@Override
	public String toString() {
		return percorrer();
	}

	/**
	 * Percorre toda a tabela e retorna sua representação em String.
	 */
	@Override
	public String percorrer() {
		String conteudo = "Tabela com " + capacidade + " posições e " + tamanho() + " itens\n";
		for (int i = 0; i < capacidade; i++) {
			conteudo += "Posição[" + i + "]: ";
			if (tabelaHash[i].vazia())
				conteudo += "vazia\n";
			else
				conteudo += tabelaHash[i].toString() + "\n";
		}
		return conteudo;
	}

	/**
	 * Retorna a quantidade de itens efetivamente armazenados na tabela.
	 */
	@Override
	public int tamanho() {
		int tamanho = 0;
		for (int i = 0; i < capacidade; i++) {
			tamanho += tabelaHash[i].tamanho();
		}
		return tamanho;
	}

	@Override
	public long getComparacoes() {
		return comparacoes;
	}

	@Override
	public double getTempo() {
		return (termino - inicio) / 1_000_000.0;
	}
}