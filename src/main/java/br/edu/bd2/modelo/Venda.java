package br.edu.bd2.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
@NamedQueries({
    @NamedQuery(name = "Venda.totalGastoPorCliente",
        query = "SELECT c.id, c.nome, SUM(v.valorTotal) FROM Venda v JOIN v.cliente c "
              + "GROUP BY c.id, c.nome ORDER BY SUM(v.valorTotal) DESC")
})
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** java.time e suportado nativamente pelo Hibernate 5.2 (coluna DATE). */
    private LocalDate dataVenda;

    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    private BigDecimal valorTotal = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funcionario_id")
    private Funcionario funcionario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loja_id")
    private Loja loja;

    /**
     * Lado inverso da relacao bidirecional. cascade ALL + orphanRemoval: os itens
     * nao existem sem a venda (composicao), entao persistir/remover a venda age nos itens.
     */
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens = new ArrayList<>();

    /** Lado inverso do 1:1; so existe para venda de medicamento controlado. */
    @OneToOne(mappedBy = "venda", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Receita receita;

    /** Mantem os dois lados da associacao consistentes e acumula o total. */
    public void adicionarItem(ItemVenda item) {
        item.setVenda(this);
        itens.add(item);
        valorTotal = valorTotal.add(item.getValorLiquido());
    }

    public void setReceita(Receita receita) {
        this.receita = receita;
        if (receita != null) {
            receita.setVenda(this);
        }
    }

    public Receita getReceita() { return receita; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDataVenda() { return dataVenda; }
    public void setDataVenda(LocalDate dataVenda) { this.dataVenda = dataVenda; }
    public FormaPagamento getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(FormaPagamento formaPagamento) { this.formaPagamento = formaPagamento; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public Funcionario getFuncionario() { return funcionario; }
    public void setFuncionario(Funcionario funcionario) { this.funcionario = funcionario; }
    public Loja getLoja() { return loja; }
    public void setLoja(Loja loja) { this.loja = loja; }
    public List<ItemVenda> getItens() { return itens; }
    public void setItens(List<ItemVenda> itens) { this.itens = itens; }

    @Override
    public String toString() {
        return "Venda[id=" + id + ", data=" + dataVenda + ", total=" + valorTotal + "]";
    }
}
