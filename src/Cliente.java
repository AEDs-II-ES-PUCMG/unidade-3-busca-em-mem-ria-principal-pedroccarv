import java.util.Objects;

public class Cliente {

    private static int ultimoId = 10_100;
    private int documento;
    private String nome;

    public Cliente(String nome) {
        this.nome = nome;
        this.documento = ultimoId++;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.documento);
    }

    @Override
    public String toString() {
        return (this.nome + " -- " + this.documento);
    }
}