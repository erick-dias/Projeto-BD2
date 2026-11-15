package br.edu.bd2.app;

import br.edu.bd2.consultas.ConsultasCriteria;
import br.edu.bd2.consultas.ConsultasJPQL;
import br.edu.bd2.dao.GenericDAO;
import br.edu.bd2.dao.JPAUtil;
import br.edu.bd2.modelo.Cliente;
import br.edu.bd2.modelo.Endereco;
import br.edu.bd2.modelo.Funcionario;
import br.edu.bd2.modelo.ItemVenda;
import br.edu.bd2.modelo.Loja;
import br.edu.bd2.modelo.Medicamento;
import br.edu.bd2.modelo.Tarja;
import br.edu.bd2.modelo.FormaPagamento;
import br.edu.bd2.modelo.Venda;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import javax.persistence.EntityManager;

/** Interface de console: CRUD de Cliente e Medicamento, cadastro de venda e demonstracao das consultas. */
public class Main {

    private static final Scanner in = new Scanner(System.in);
    private static final GenericDAO<Cliente, Long> clienteDAO = new GenericDAO<>(Cliente.class);
    private static final GenericDAO<Medicamento, Long> medicamentoDAO = new GenericDAO<>(Medicamento.class);
    private static final GenericDAO<Venda, Long> vendaDAO = new GenericDAO<>(Venda.class);

    public static void main(String[] args) {
        try {
            int op;
            do {
                System.out.println("\n=== FARMACIA - MENU ===");
                System.out.println("1 - Clientes (CRUD)");
                System.out.println("2 - Medicamentos (CRUD)");
                System.out.println("3 - Cadastrar venda");
                System.out.println("4 - Executar consultas JPQL e Criteria");
                System.out.println("0 - Sair");
                op = lerInt("Opcao: ");
                try {
                    switch (op) {
                        case 1: menuClientes(); break;
                        case 2: menuMedicamentos(); break;
                        case 3: cadastrarVenda(); break;
                        case 4: demonstrarConsultas(); break;
                        default: break;
                    }
                } catch (RuntimeException e) {
                    System.out.println("Erro: " + e.getMessage());
                }
            } while (op != 0);
        } finally {
            JPAUtil.fechar();
        }
    }

    // ------------------------------------------------------------ clientes

    private static void menuClientes() {
        System.out.println("\n[Clientes] 1-Listar (pagina)  2-Cadastrar  3-Alterar  4-Remover");
        int op = lerInt("Opcao: ");
        if (op == 1) {
            int pagina = lerInt("Pagina (inicia em 0): ");
            EntityManager em = JPAUtil.criarEntityManager();
            try {
                for (Cliente c : new ConsultasJPQL(em).clientesPaginado(pagina, 10)) {
                    System.out.println(c.getId() + " | " + c.getNome() + " | " + c.getEndereco().getCidade());
                }
            } finally {
                em.close();
            }
        } else if (op == 2) {
            Cliente c = new Cliente();
            c.setNome(lerTexto("Nome: "));
            c.setCpf(lerTexto("CPF: "));
            c.setEmail(lerTexto("Email: "));
            c.setEndereco(new Endereco(lerTexto("Logradouro: "), lerTexto("Numero: "), lerTexto("Bairro: "),
                lerTexto("Cidade: "), lerTexto("UF: "), lerTexto("CEP: ")));
            c.setDataCadastro(LocalDate.now());
            clienteDAO.salvar(c);
            System.out.println("Cliente cadastrado com id " + c.getId());
        } else if (op == 3) {
            Cliente c = clienteDAO.buscarPorId((long) lerInt("Id: "));
            if (c == null) {
                System.out.println("Nao encontrado.");
                return;
            }
            String nome = lerTexto("Novo nome [" + c.getNome() + "]: ");
            if (!nome.isEmpty()) {
                c.setNome(nome);
            }
            String cidade = lerTexto("Nova cidade [" + c.getEndereco().getCidade() + "]: ");
            if (!cidade.isEmpty()) {
                c.getEndereco().setCidade(cidade);
            }
            clienteDAO.atualizar(c);
            System.out.println("Cliente atualizado.");
        } else if (op == 4) {
            System.out.println(clienteDAO.remover((long) lerInt("Id: ")) ? "Removido." : "Nao encontrado.");
        }
    }

    // ------------------------------------------------------------ medicamentos

    private static void menuMedicamentos() {
        System.out.println("\n[Medicamentos] 1-Listar por tarja  2-Cadastrar  3-Alterar preco  4-Remover");
        int op = lerInt("Opcao: ");
        if (op == 1) {
            Tarja tarja = Tarja.valueOf(lerTexto("Tarja (LIVRE/VERMELHA/PRETA): ").toUpperCase());
            EntityManager em = JPAUtil.criarEntityManager();
            try {
                for (Medicamento m : new ConsultasJPQL(em).medicamentosPorTarja(tarja)) {
                    System.out.println(m.getId() + " | " + m.getNome() + " | R$ " + m.getPreco());
                }
            } finally {
                em.close();
            }
        } else if (op == 2) {
            Medicamento m = new Medicamento();
            m.setNome(lerTexto("Nome: "));
            m.setPreco(new BigDecimal(lerTexto("Preco (ex 12.50): ")));
            m.setTarja(Tarja.valueOf(lerTexto("Tarja (LIVRE/VERMELHA/PRETA): ").toUpperCase()));
            medicamentoDAO.salvar(m);
            System.out.println("Medicamento cadastrado com id " + m.getId());
        } else if (op == 3) {
            Medicamento m = medicamentoDAO.buscarPorId((long) lerInt("Id: "));
            if (m == null) {
                System.out.println("Nao encontrado.");
                return;
            }
            m.setPreco(new BigDecimal(lerTexto("Novo preco: ")));
            medicamentoDAO.atualizar(m);
            System.out.println("Preco atualizado.");
        } else if (op == 4) {
            try {
                System.out.println(medicamentoDAO.remover((long) lerInt("Id: ")) ? "Removido." : "Nao encontrado.");
            } catch (RuntimeException e) {
                System.out.println("Nao foi possivel remover (medicamento ja vendido?).");
            }
        }
    }

    // ------------------------------------------------------------ venda

    private static void cadastrarVenda() {
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            Cliente cliente = em.find(Cliente.class, (long) lerInt("Id do cliente: "));
            Loja loja = em.find(Loja.class, (long) lerInt("Id da loja (1-5): "));
            if (cliente == null || loja == null) {
                System.out.println("Cliente ou loja nao encontrado.");
                return;
            }
            Funcionario func = em.createQuery("SELECT f FROM Funcionario f WHERE f.loja = :l ORDER BY f.id",
                Funcionario.class).setParameter("l", loja).setMaxResults(1).getSingleResult();

            Venda venda = new Venda();
            venda.setCliente(cliente);
            venda.setLoja(loja);
            venda.setFuncionario(func);
            venda.setDataVenda(LocalDate.now());
            venda.setFormaPagamento(FormaPagamento.valueOf(lerTexto("Pagamento (DINHEIRO/CARTAO_CREDITO/CARTAO_DEBITO/PIX): ").toUpperCase()));

            int idMed;
            while ((idMed = lerInt("Id do medicamento (0 para finalizar): ")) != 0) {
                Medicamento m = em.find(Medicamento.class, (long) idMed);
                if (m == null) {
                    System.out.println("Medicamento nao encontrado.");
                    continue;
                }
                venda.adicionarItem(new ItemVenda(m, lerInt("Quantidade: "), m.getPreco(), BigDecimal.ZERO));
            }
            if (venda.getItens().isEmpty()) {
                System.out.println("Venda sem itens, cancelada.");
                return;
            }
            em.getTransaction().begin();
            em.persist(venda);
            em.getTransaction().commit();
            System.out.println("Venda " + venda.getId() + " registrada. Total: R$ " + venda.getValorTotal());
        } finally {
            em.close();
        }
    }

    // ------------------------------------------------------------ consultas

    private static void demonstrarConsultas() {
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            ConsultasJPQL q = new ConsultasJPQL(em);
            ConsultasCriteria c = new ConsultasCriteria(em);
            LocalDate ini = LocalDate.of(2025, 1, 1);
            LocalDate fim = LocalDate.of(2025, 12, 31);

            System.out.println("\n1) Vendas da loja 1 em 2025: " + q.vendasDaLojaNoPeriodo(1L, ini, fim).size());
            Venda v = q.vendaComItens(1L);
            System.out.println("2) Venda 1 com itens (JOIN FETCH): " + v.getItens().size() + " itens");
            System.out.println("3) Faturamento por loja (> 1000):");
            for (Object[] r : q.faturamentoPorLojaAcimaDe(new BigDecimal("1000"))) {
                System.out.println("   " + r[0] + " = " + r[1]);
            }
            System.out.println("4) Medicamentos acima da media da categoria: "
                + q.medicamentosAcimaDaMediaDaCategoria().size());
            System.out.println("5) Top 5 clientes por gasto (NamedQuery):");
            for (Object[] r : q.totalGastoPorCliente(5)) {
                System.out.println("   " + r[1] + " = " + r[2]);
            }
            System.out.println("6) Medicamentos tarja PRETA (NamedQuery): " + q.medicamentosPorTarja(Tarja.PRETA).size());
            System.out.println("7) Clientes pagina 0 (10 por pagina): " + q.clientesPaginado(0, 10).size());
            System.out.println("8) Top 10 medicamentos mais vendidos:");
            for (Object[] r : q.topMedicamentosMaisVendidos(10)) {
                System.out.println("   " + r[0] + " = " + r[1]);
            }
            System.out.println("9) Clientes sem compra controlada: " + q.clientesSemCompraControlada().size());
            System.out.println("10) Ticket medio por forma de pagamento:");
            for (Object[] r : q.ticketMedioPorFormaPagamento()) {
                System.out.println("   " + r[0] + " = " + r[1] + " (" + r[2] + " vendas)");
            }

            System.out.println("C1) Busca dinamica (nome contem 'vitamina', preco <= 20): "
                + c.buscarMedicamentos("vitamina", null, null, null, new BigDecimal("20")).size());
            System.out.println("C2) Vendas do funcionario 1 em 2025: "
                + c.vendasPorFuncionario(1L, ini, fim).size());
            System.out.println("C3) Quantidade de vendas por loja:");
            for (Object[] r : c.quantidadeDeVendasPorLoja()) {
                System.out.println("   " + r[0] + " = " + r[1]);
            }
        } finally {
            em.close();
        }
    }

    // ------------------------------------------------------------ entrada

    private static String lerTexto(String msg) {
        System.out.print(msg);
        return in.nextLine().trim();
    }

    private static int lerInt(String msg) {
        while (true) {
            try {
                return Integer.parseInt(lerTexto(msg));
            } catch (NumberFormatException e) {
                System.out.println("Numero invalido.");
            }
        }
    }
}
