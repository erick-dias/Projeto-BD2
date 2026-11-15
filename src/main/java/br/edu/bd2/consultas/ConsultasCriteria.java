package br.edu.bd2.consultas;

import br.edu.bd2.modelo.Categoria;
import br.edu.bd2.modelo.Funcionario;
import br.edu.bd2.modelo.Loja;
import br.edu.bd2.modelo.Medicamento;
import br.edu.bd2.modelo.Tarja;
import br.edu.bd2.modelo.Venda;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/** As 3 consultas com Criteria API da Entrega 1. */
public class ConsultasCriteria {

    private final EntityManager em;

    public ConsultasCriteria(EntityManager em) {
        this.em = em;
    }

    /**
     * C1. Busca de medicamento com filtros opcionais (nome, categoria, tarja, faixa de preco).
     * Parametros nulos sao ignorados: o WHERE e montado dinamicamente.
     */
    public List<Medicamento> buscarMedicamentos(String nome, String categoria, Tarja tarja,
                                                BigDecimal precoMin, BigDecimal precoMax) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Medicamento> cq = cb.createQuery(Medicamento.class);
        Root<Medicamento> m = cq.from(Medicamento.class);

        List<Predicate> filtros = new ArrayList<>();
        if (nome != null && !nome.trim().isEmpty()) {
            filtros.add(cb.like(cb.lower(m.<String>get("nome")), "%" + nome.trim().toLowerCase() + "%"));
        }
        if (categoria != null && !categoria.trim().isEmpty()) {
            Join<Medicamento, Categoria> c = m.join("categoria");
            filtros.add(cb.equal(cb.lower(c.<String>get("nome")), categoria.trim().toLowerCase()));
        }
        if (tarja != null) {
            filtros.add(cb.equal(m.get("tarja"), tarja));
        }
        if (precoMin != null) {
            filtros.add(cb.greaterThanOrEqualTo(m.<BigDecimal>get("preco"), precoMin));
        }
        if (precoMax != null) {
            filtros.add(cb.lessThanOrEqualTo(m.<BigDecimal>get("preco"), precoMax));
        }

        cq.select(m).where(filtros.toArray(new Predicate[0])).orderBy(cb.asc(m.get("nome")));
        return em.createQuery(cq).getResultList();
    }

    /** C2. Vendas realizadas por um funcionario em um intervalo de datas (JOIN). */
    public List<Venda> vendasPorFuncionario(Long funcionarioId, LocalDate inicio, LocalDate fim) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Venda> cq = cb.createQuery(Venda.class);
        Root<Venda> v = cq.from(Venda.class);
        Join<Venda, Funcionario> f = v.join("funcionario");

        cq.select(v)
          .where(cb.equal(f.get("id"), funcionarioId),
                 cb.between(v.<LocalDate>get("dataVenda"), inicio, fim))
          .orderBy(cb.asc(v.get("dataVenda")));
        return em.createQuery(cq).getResultList();
    }

    /** C3. Quantidade de vendas por loja (agregacao COUNT + GROUP BY). */
    public List<Object[]> quantidadeDeVendasPorLoja() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<Venda> v = cq.from(Venda.class);
        Join<Venda, Loja> l = v.join("loja");

        cq.multiselect(l.get("nome"), cb.count(v))
          .groupBy(l.get("id"), l.get("nome"))
          .orderBy(cb.desc(cb.count(v)));
        return em.createQuery(cq).getResultList();
    }
}
