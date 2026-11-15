package br.edu.bd2.consultas;

import br.edu.bd2.modelo.Cliente;
import br.edu.bd2.modelo.Medicamento;
import br.edu.bd2.modelo.Tarja;
import br.edu.bd2.modelo.Venda;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.EntityManager;

/** As 10 consultas JPQL da Entrega 1 (2 delas sao @NamedQuery). */
public class ConsultasJPQL {

    private final EntityManager em;

    public ConsultasJPQL(EntityManager em) {
        this.em = em;
    }

    /** 1. Vendas de uma loja em um periodo: JOIN + parametros nomeados. */
    public List<Venda> vendasDaLojaNoPeriodo(Long lojaId, LocalDate inicio, LocalDate fim) {
        return em.createQuery(
                "SELECT v FROM Venda v JOIN v.loja l "
              + "WHERE l.id = :lojaId AND v.dataVenda BETWEEN :inicio AND :fim "
              + "ORDER BY v.dataVenda", Venda.class)
            .setParameter("lojaId", lojaId)
            .setParameter("inicio", inicio)
            .setParameter("fim", fim)
            .getResultList();
    }

    /** 2. Venda com itens e medicamentos em uma unica ida ao banco: JOIN FETCH (evita N+1). */
    public Venda vendaComItens(Long vendaId) {
        return em.createQuery(
                "SELECT DISTINCT v FROM Venda v "
              + "JOIN FETCH v.itens i JOIN FETCH i.medicamento "
              + "WHERE v.id = :id", Venda.class)
            .setParameter("id", vendaId)
            .getSingleResult();
    }

    /** 3. Faturamento por loja acima de um minimo: GROUP BY / HAVING. */
    public List<Object[]> faturamentoPorLojaAcimaDe(BigDecimal minimo) {
        return em.createQuery(
                "SELECT l.nome, SUM(v.valorTotal) FROM Venda v JOIN v.loja l "
              + "GROUP BY l.nome HAVING SUM(v.valorTotal) > :minimo "
              + "ORDER BY SUM(v.valorTotal) DESC", Object[].class)
            .setParameter("minimo", minimo)
            .getResultList();
    }

    /** 4. Medicamentos com preco acima da media da propria categoria: subconsulta. */
    public List<Medicamento> medicamentosAcimaDaMediaDaCategoria() {
        return em.createQuery(
                "SELECT m FROM Medicamento m WHERE m.preco > "
              + "(SELECT AVG(m2.preco) FROM Medicamento m2 WHERE m2.categoria = m.categoria) "
              + "ORDER BY m.nome", Medicamento.class)
            .getResultList();
    }

    /** 5. Total gasto por cliente: @NamedQuery Venda.totalGastoPorCliente. */
    public List<Object[]> totalGastoPorCliente(int limite) {
        return em.createNamedQuery("Venda.totalGastoPorCliente", Object[].class)
            .setMaxResults(limite)
            .getResultList();
    }

    /** 6. Medicamentos por tarja: @NamedQuery Medicamento.porTarja (usa o enum). */
    public List<Medicamento> medicamentosPorTarja(Tarja tarja) {
        return em.createNamedQuery("Medicamento.porTarja", Medicamento.class)
            .setParameter("tarja", tarja)
            .getResultList();
    }

    /** 7. Listagem de clientes pagina a pagina (paginas iniciam em 0). */
    public List<Cliente> clientesPaginado(int pagina, int tamanho) {
        return em.createQuery("SELECT c FROM Cliente c ORDER BY c.nome", Cliente.class)
            .setFirstResult(pagina * tamanho)
            .setMaxResults(tamanho)
            .getResultList();
    }

    /** 8. Top N medicamentos mais vendidos: agregacao + ordenacao + setMaxResults. */
    public List<Object[]> topMedicamentosMaisVendidos(int n) {
        return em.createQuery(
                "SELECT m.nome, SUM(i.quantidade) FROM ItemVenda i JOIN i.medicamento m "
              + "GROUP BY m.id, m.nome ORDER BY SUM(i.quantidade) DESC, m.nome", Object[].class)
            .setMaxResults(n)
            .getResultList();
    }

    /** 9. Clientes que nunca compraram medicamento controlado: NOT EXISTS. */
    public List<Cliente> clientesSemCompraControlada() {
        return em.createQuery(
                "SELECT c FROM Cliente c WHERE NOT EXISTS "
              + "(SELECT r.id FROM Receita r WHERE r.venda.cliente = c) "
              + "ORDER BY c.nome", Cliente.class)
            .getResultList();
    }

    /** 10. Ticket medio por forma de pagamento: AVG. */
    public List<Object[]> ticketMedioPorFormaPagamento() {
        return em.createQuery(
                "SELECT v.formaPagamento, AVG(v.valorTotal), COUNT(v) FROM Venda v "
              + "GROUP BY v.formaPagamento ORDER BY AVG(v.valorTotal) DESC", Object[].class)
            .getResultList();
    }
}
