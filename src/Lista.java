import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

public class Lista<E> implements IMedicao {

	private Celula<E> primeiro;
	private Celula<E> ultimo;
	private int tamanho;

	private long comparacoes;
	private long inicio;
	private long termino;

	public Lista() {
		Celula<E> sentinela = new Celula<>();
		this.primeiro = this.ultimo = sentinela;
		this.tamanho = 0;
	}

	public boolean vazia() {
		return (this.primeiro == this.ultimo);
	}

	/**
	 * Insere um elemento na posição final da lista.
	 */
	public void inserir(E elemento) {
		inserir(elemento, tamanho);
	}

	/**
	 * Insere um novo elemento na posição indicada.
	 * @throws IndexOutOfBoundsException em caso de posição inválida
	 */
	public void inserir(E novo, int posicao) {
		Celula<E> anterior, novaCelula, proximaCelula;

		if ((posicao < 0) || (posicao > this.tamanho))
			throw new IndexOutOfBoundsException("Não foi possível inserir o item na lista: "
					+ "a posição informada é inválida!");

		anterior = this.primeiro;
		for (int i = 0; i < posicao; i++)
			anterior = anterior.getProximo();

		novaCelula = new Celula<>(novo);
		proximaCelula = anterior.getProximo();
		anterior.setProximo(novaCelula);
		novaCelula.setProximo(proximaCelula);

		if (posicao == this.tamanho)
			this.ultimo = novaCelula;

		this.tamanho++;
	}

	/**
	 * Remove o último elemento da lista.
	 * @throws IllegalStateException em caso de lista vazia
	 */
	public E remover() {
		return remover(tamanho - 1);
	}

	/**
	 * Remove um elemento da posição indicada.
	 * @throws IllegalStateException se a lista estiver vazia
	 * @throws IndexOutOfBoundsException em caso de posição inválida
	 */
	public E remover(int posicao) {
		Celula<E> anterior;

		if (vazia())
			throw new IllegalStateException("Não foi possível remover o item da lista: "
					+ "a lista está vazia!");

		if ((posicao < 0) || (posicao >= this.tamanho))
			throw new IndexOutOfBoundsException("Não foi possível remover o item da lista: "
					+ "a posição informada é inválida!");

		anterior = this.primeiro;
		for (int i = 0; i < posicao; i++)
			anterior = anterior.getProximo();

		return removerProxima(anterior);
	}

	/**
	 * Localiza e remove um elemento da lista usando equals.
	 * @throws IllegalStateException caso a lista esteja vazia
	 * @throws NoSuchElementException caso o elemento não exista na lista
	 */
	public E remover(E elemento) {
		Celula<E> anterior;

		if (vazia())
			throw new IllegalStateException("Não foi possível remover o item da lista: "
					+ "a lista está vazia!");

		anterior = this.primeiro;
		while ((anterior.getProximo() != null) && !(anterior.getProximo().getItem().equals(elemento)))
			anterior = anterior.getProximo();

		if (anterior.getProximo() == null)
			throw new NoSuchElementException("Item não encontrado!");

		return removerProxima(anterior);
	}

	/**
	 * Método privado que encapsula a remoção tomando como referência o elemento anterior.
	 */
	private E removerProxima(Celula<E> anterior) {
		Celula<E> celulaRemovida, proximaCelula;

		celulaRemovida = anterior.getProximo();
		proximaCelula = celulaRemovida.getProximo();

		anterior.setProximo(proximaCelula);
		celulaRemovida.setProximo(null);

		if (celulaRemovida == this.ultimo)
			this.ultimo = anterior;

		this.tamanho--;

		return celulaRemovida.getItem();
	}

	/**
	 * Pesquisa um elemento na lista usando equals.
	 * @throws NoSuchElementException se não existir elemento igual ao procurado
	 */
	public E pesquisar(E procurado) {
		comparacoes = 0;
		inicio = System.nanoTime();

		Celula<E> aux = this.primeiro.getProximo();

		while (aux != null) {
			comparacoes++;
			if (aux.getItem().equals(procurado)) {
				termino = System.nanoTime();
				return aux.getItem();
			}
			aux = aux.getProximo();
		}

		throw new NoSuchElementException("Item não encontrado!");
	}

	/**
	 * Conta quantos elementos da lista atendem à condição estabelecida pelo predicado.
	 */
	public int contarRepeticoes(Predicate<E> condicional) {
		int repeticoes = 0;
		Celula<E> aux = primeiro.getProximo();

		while (aux != null) {
			if (condicional.test(aux.getItem()))
				repeticoes++;
			aux = aux.getProximo();
		}

		return repeticoes;
	}

	/**
	 * Calcula e retorna o valor total de um atributo dos elementos da lista.
	 */
	public double calcularValorTotal(Function<E, Double> extrator) {
		double soma = 0;

		if (vazia())
			throw new IllegalStateException("A lista está vazia!");

		Celula<E> aux = primeiro.getProximo();
		while (aux != null) {
			soma += extrator.apply(aux.getItem());
			aux = aux.getProximo();
		}

		return soma;
	}

	/**
	 * Busca na lista o primeiro elemento correspondente ao item informado,
	 * utilizando o critério de comparação especificado.
	 * @return O elemento encontrado, ou null caso nenhum seja encontrado
	 */
	public E buscarPor(Comparator<E> criterioDeBusca, E item) {
		Celula<E> aux = primeiro.getProximo();

		while (aux != null) {
			if (criterioDeBusca.compare(aux.getItem(), item) == 0)
				return aux.getItem();
			aux = aux.getProximo();
		}

		return null;
	}

	/**
	 * Calcula o somatório do produto entre dois valores extraídos de cada elemento da lista.
	 * @throws IllegalStateException caso a lista esteja vazia
	 */
	public double somarMultiplicacoes(Function<E, Double> extratorValor, Function<E, Integer> extratorFator) {
		double soma = 0;
		Celula<E> aux = primeiro.getProximo();

		if (vazia())
			throw new IllegalStateException("A lista está vazia!");

		while (aux != null) {
			soma += (extratorValor.apply(aux.getItem()) * extratorFator.apply(aux.getItem()));
			aux = aux.getProximo();
		}

		return soma;
	}

	/**
	 * Retorna uma nova lista contendo os elementos que satisfazem uma condição.
	 * @throws IllegalStateException caso a lista esteja vazia
	 */
	public Lista<E> filtrar(Predicate<E> condicional) {
		if (vazia())
			throw new IllegalStateException("A lista está vazia!");

		Lista<E> subLista = new Lista<>();
		Celula<E> aux = primeiro.getProximo();

		while (aux != null) {
			if (condicional.test(aux.getItem()))
				subLista.inserir(aux.getItem(), subLista.tamanho());
			aux = aux.getProximo();
		}

		return subLista;
	}

	public int tamanho() {
		return tamanho;
	}

	@Override
	public String toString() {
		if (vazia())
			return "A lista está vazia!\n";

		StringBuilder listaString = new StringBuilder();
		Celula<E> aux = this.primeiro.getProximo();

		while (aux != null) {
			listaString.append(aux.getItem()).append("\n");
			aux = aux.getProximo();
		}

		return listaString.toString();
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