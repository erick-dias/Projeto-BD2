package br.edu.bd2.modelo;

import java.math.BigDecimal;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Entidade intermediaria Venda x Medicamento. Tem atributos proprios
 * (quantidade, preco, desconto), por isso nao e um @ManyToMany simples.
 * E a origem das medidas da tabela fato do DW (granularidade: 1 item vendido).
 */
@Entity
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id")
    private Venda venda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

    private Integer quantidade;

    private BigDecimal precoUnitario;

    /** Desconto em reais aplicado a linha inteira. */
    private BigDecimal desconto = BigDecimal.ZERO;

    public ItemVenda() {
    }

    public ItemVenda(Medicamento medicamento, Integer quantidade, BigDecimal precoUnitario, BigDecimal desconto) {
        this.medicamento = medicamento;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.desconto = desconto;
    }

    public BigDecimal getValorBruto() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public BigDecimal getValorLiquido() {
        return getValorBruto().subtract(desconto);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Venda getVenda() { return venda; }
    public void setVenda(Venda venda) { this.venda = venda; }
    public Medicamento getMedicamento() { return medicamento; }
    public void setMedicamento(Medicamento medicamento) { this.medicamento = medicamento; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }
    public BigDecimal getDesconto() { return desconto; }
    public void setDesconto(BigDecimal desconto) { this.desconto = desconto; }
}
