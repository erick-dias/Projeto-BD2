package br.edu.bd2.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
    @NamedQuery(name = "Medicamento.porTarja",
        query = "SELECT m FROM Medicamento m WHERE m.tarja = :tarja ORDER BY m.nome")
})
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private BigDecimal preco;

    /** Gravado como texto (EnumType.STRING) para nao quebrar se a ordem do enum mudar. */
    @Enumerated(EnumType.STRING)
    private Tarja tarja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratorio_id")
    private Laboratorio laboratorio;

    @ManyToMany
    @JoinTable(name = "medicamento_principio_ativo",
        joinColumns = @JoinColumn(name = "medicamento_id"),
        inverseJoinColumns = @JoinColumn(name = "principio_ativo_id"))
    private List<PrincipioAtivo> principiosAtivos = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public Tarja getTarja() { return tarja; }
    public void setTarja(Tarja tarja) { this.tarja = tarja; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Laboratorio getLaboratorio() { return laboratorio; }
    public void setLaboratorio(Laboratorio laboratorio) { this.laboratorio = laboratorio; }
    public List<PrincipioAtivo> getPrincipiosAtivos() { return principiosAtivos; }
    public void setPrincipiosAtivos(List<PrincipioAtivo> principiosAtivos) { this.principiosAtivos = principiosAtivos; }

    @Override
    public String toString() {
        return "Medicamento[id=" + id + ", nome=" + nome + ", preco=" + preco + ", tarja=" + tarja + "]";
    }
}
