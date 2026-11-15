package br.edu.bd2.carga;

import br.edu.bd2.dao.JPAUtil;
import br.edu.bd2.modelo.Categoria;
import br.edu.bd2.modelo.Cliente;
import br.edu.bd2.modelo.Departamento;
import br.edu.bd2.modelo.Endereco;
import br.edu.bd2.modelo.FormaPagamento;
import br.edu.bd2.modelo.Funcionario;
import br.edu.bd2.modelo.ItemVenda;
import br.edu.bd2.modelo.Laboratorio;
import br.edu.bd2.modelo.Loja;
import br.edu.bd2.modelo.Medicamento;
import br.edu.bd2.modelo.PrincipioAtivo;
import br.edu.bd2.modelo.Receita;
import br.edu.bd2.modelo.Tarja;
import br.edu.bd2.modelo.Venda;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * Carga de dados realistas: 5 lojas, 30 funcionarios, 200 clientes, 122 medicamentos
 * e 2.000 vendas distribuidas em 2 anos (16/11/2024 a 14/11/2026).
 *
 * A carga embute padroes de proposito, para o DW ter o que mostrar:
 *  - antigripal comprado junto com vitamina C (regra de associacao);
 *  - clientes 0..29 compram anti-hipertensivo de forma recorrente;
 *  - ansioliticos tarja preta sempre com receita (1:1 Venda-Receita);
 *  - protetor solar + repelente juntos.
 * Execute com:  mvn compile exec:java -Dexec.mainClass=br.edu.bd2.carga.CargaDados
 * Use o argumento "--reset" para apagar tudo e recarregar.
 */
public class CargaDados {

    private static final int QTD_CLIENTES = 200;
    private static final int QTD_VENDAS = 2000;
    private static final LocalDate DATA_INICIAL = LocalDate.of(2024, 11, 16);
    private static final LocalDate DATA_FINAL = LocalDate.of(2026, 11, 14);

    private final Random rnd = new Random(42);
    private final EntityManager em;

    private final List<Loja> lojas = new ArrayList<>();
    private final Map<Long, List<Funcionario>> funcionariosPorLoja = new HashMap<>();
    private final List<Cliente> clientes = new ArrayList<>();
    private final List<Medicamento> medicamentos = new ArrayList<>();
    private final Map<String, Medicamento> medPorNome = new HashMap<>();

    // nome | categoria (indice) | tarja | preco base
    private static final String[][] BASES = {
        // 0 Analgesicos
        {"Dipirona", "0", "LIVRE", "8.50"}, {"Paracetamol", "0", "LIVRE", "9.90"},
        {"Neosaldina", "0", "LIVRE", "18.90"}, {"Dorflex", "0", "LIVRE", "14.50"},
        {"Aspirina", "0", "LIVRE", "11.00"},
        // 1 Antibioticos
        {"Amoxicilina", "1", "VERMELHA", "32.00"}, {"Azitromicina", "1", "VERMELHA", "28.50"},
        {"Cefalexina", "1", "VERMELHA", "35.00"}, {"Ciprofloxacino", "1", "VERMELHA", "29.90"},
        {"Sulfametoxazol", "1", "VERMELHA", "24.00"},
        // 2 Antigripais
        {"Resfenol", "2", "LIVRE", "19.90"}, {"Benegrip", "2", "LIVRE", "17.50"},
        {"Coristina", "2", "LIVRE", "16.00"}, {"Naldecon", "2", "LIVRE", "21.00"},
        {"Sinutab", "2", "LIVRE", "22.50"}, {"Vick Xarope", "2", "LIVRE", "26.00"},
        // 3 Anti-hipertensivos
        {"Losartana", "3", "VERMELHA", "22.00"}, {"Atenolol", "3", "VERMELHA", "15.00"},
        {"Captopril", "3", "VERMELHA", "12.50"}, {"Enalapril", "3", "VERMELHA", "13.90"},
        {"Anlodipino", "3", "VERMELHA", "18.00"}, {"Hidroclorotiazida", "3", "VERMELHA", "10.50"},
        // 4 Anti-inflamatorios
        {"Ibuprofeno", "4", "LIVRE", "15.90"}, {"Diclofenaco", "4", "VERMELHA", "16.50"},
        {"Nimesulida", "4", "VERMELHA", "19.00"}, {"Cetoprofeno", "4", "VERMELHA", "21.50"},
        {"Naproxeno", "4", "VERMELHA", "20.00"},
        // 5 Antialergicos
        {"Loratadina", "5", "LIVRE", "13.50"}, {"Desloratadina", "5", "VERMELHA", "27.00"},
        {"Cetirizina", "5", "LIVRE", "17.00"}, {"Dexclorfeniramina", "5", "LIVRE", "12.00"},
        {"Fexofenadina", "5", "VERMELHA", "31.00"},
        // 6 Controlados
        {"Clonazepam", "6", "PRETA", "24.00"}, {"Alprazolam", "6", "PRETA", "27.50"},
        {"Diazepam", "6", "PRETA", "19.00"}, {"Zolpidem", "6", "PRETA", "38.00"},
        {"Fluoxetina", "6", "VERMELHA", "29.00"}, {"Sertralina", "6", "VERMELHA", "34.00"},
        {"Amitriptilina", "6", "VERMELHA", "21.00"},
        // 7 Vitaminas
        {"Vitamina C", "7", "LIVRE", "15.90"}, {"Vitamina D", "7", "LIVRE", "39.90"},
        {"Complexo B", "7", "LIVRE", "23.00"}, {"Vitamina B12", "7", "LIVRE", "28.00"},
        {"Acido Folico", "7", "LIVRE", "12.90"}, {"Polivitaminico", "7", "LIVRE", "42.00"},
        // 8 Suplementos
        {"Omega 3", "8", "LIVRE", "49.90"}, {"Colageno", "8", "LIVRE", "64.00"},
        {"Magnesio", "8", "LIVRE", "35.00"}, {"Melatonina", "8", "LIVRE", "33.00"},
        {"Calcio", "8", "LIVRE", "29.00"}, {"Creatina", "8", "LIVRE", "89.00"},
        // 9 Higiene pessoal
        {"Sabonete", "9", "LIVRE", "3.50"}, {"Shampoo", "9", "LIVRE", "14.90"},
        {"Creme Dental", "9", "LIVRE", "6.90"}, {"Fio Dental", "9", "LIVRE", "7.50"},
        {"Alcool em Gel", "9", "LIVRE", "9.00"}, {"Desodorante", "9", "LIVRE", "13.50"},
        // 10 Dermocosmeticos
        {"Protetor Solar", "10", "LIVRE", "54.90"}, {"Hidratante", "10", "LIVRE", "32.00"},
        {"Sabonete Facial", "10", "LIVRE", "27.00"}, {"Repelente", "10", "LIVRE", "22.90"}
    };

    private static final String[] CATEGORIAS = {
        "Analgesicos", "Antibioticos", "Antigripais", "Anti-hipertensivos", "Anti-inflamatorios",
        "Antialergicos", "Controlados", "Vitaminas", "Suplementos", "Higiene Pessoal", "Dermocosmeticos"
    };
    // departamento de cada categoria
    private static final int[] DEPTO_DA_CATEGORIA = {0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 2};
    private static final String[] DEPARTAMENTOS = {"Medicamentos", "Vitaminas e Suplementos", "Higiene e Beleza"};

    private static final String[] LABS_GENERICOS = {"EMS", "Medley", "Neo Quimica", "Cimed"};
    private static final String[] LABS_REFERENCIA = {"Sanofi", "Bayer", "Pfizer", "Eurofarma"};

    private static final String[] NOMES = {"Ana", "Bruno", "Carla", "Daniel", "Eduarda", "Felipe", "Gabriela",
        "Henrique", "Isabela", "Joao", "Karina", "Lucas", "Mariana", "Nicolas", "Olivia", "Pedro", "Quiteria",
        "Rafael", "Sofia", "Thiago", "Ursula", "Vitor", "Wesley", "Yasmin", "Zeca"};
    private static final String[] SOBRENOMES = {"Silva", "Santos", "Oliveira", "Souza", "Rodrigues", "Ferreira",
        "Alves", "Pereira", "Lima", "Gomes", "Costa", "Ribeiro", "Martins", "Carvalho", "Almeida", "Lopes",
        "Barbosa", "Araujo", "Melo", "Nascimento"};

    // cidade, estado
    private static final String[][] CIDADES = {
        {"Salvador", "BA"}, {"Lauro de Freitas", "BA"}, {"Feira de Santana", "BA"}, {"Camacari", "BA"},
        {"Recife", "PE"}, {"Olinda", "PE"}, {"Aracaju", "SE"}, {"Simoes Filho", "BA"}
    };

    // nome, bairro, cidade, estado
    private static final String[][] LOJAS = {
        {"Farmacia Pituba", "Pituba", "Salvador", "BA"},
        {"Farmacia Barra", "Barra", "Salvador", "BA"},
        {"Farmacia Feira Centro", "Centro", "Feira de Santana", "BA"},
        {"Farmacia Boa Viagem", "Boa Viagem", "Recife", "PE"},
        {"Farmacia Atalaia", "Atalaia", "Aracaju", "SE"}
    };

    public CargaDados(EntityManager em) {
        this.em = em;
    }

    public static void main(String[] args) {
        boolean reset = args.length > 0 && "--reset".equals(args[0]);
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            Long existentes = em.createQuery("SELECT COUNT(c) FROM Cliente c", Long.class).getSingleResult();
            if (existentes > 0 && !reset) {
                System.out.println("O banco ja possui dados. Rode com --reset para apagar e recarregar.");
                return;
            }
            if (reset) {
                limpar(em);
            }
            long t0 = System.currentTimeMillis();
            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                new CargaDados(em).executar();
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                throw e;
            }
            System.out.println("Carga concluida em " + (System.currentTimeMillis() - t0) + " ms.");
            imprimirContagens(em);
        } finally {
            em.close();
            JPAUtil.fechar();
        }
    }

    private static void limpar(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();
        em.createNativeQuery("TRUNCATE receita, item_venda, venda, medicamento_principio_ativo, medicamento, "
            + "principio_ativo, laboratorio, categoria, departamento, funcionario, cliente, pessoa, loja "
            + "RESTART IDENTITY CASCADE").executeUpdate();
        tx.commit();
        System.out.println("Tabelas limpas.");
    }

    private static void imprimirContagens(EntityManager em) {
        String[] entidades = {"Loja", "Funcionario", "Cliente", "Departamento", "Categoria", "Laboratorio",
            "PrincipioAtivo", "Medicamento", "Venda", "ItemVenda", "Receita"};
        for (String e : entidades) {
            Long n = em.createQuery("SELECT COUNT(x) FROM " + e + " x", Long.class).getSingleResult();
            System.out.printf("  %-15s %d%n", e, n);
        }
    }

    public void executar() {
        criarLojasEFuncionarios();
        criarCatalogo();
        criarClientes();
        criarVendas();
        em.flush();
    }

    // ---------------------------------------------------------------- lojas / funcionarios

    private void criarLojasEFuncionarios() {
        int cpfSeq = 900000;
        for (int i = 0; i < LOJAS.length; i++) {
            String[] d = LOJAS[i];
            Loja loja = new Loja();
            loja.setNome(d[0]);
            loja.setEndereco(new Endereco("Rua das Palmeiras", String.valueOf(100 + i * 37), d[1], d[2], d[3],
                String.format("4%04d-000", 1000 + i)));
            em.persist(loja);
            lojas.add(loja);

            List<Funcionario> equipe = new ArrayList<>();
            String[] cargos = {"Gerente", "Farmaceutico", "Farmaceutico", "Atendente", "Atendente", "Caixa"};
            for (int f = 0; f < cargos.length; f++) {
                Funcionario fun = new Funcionario();
                fun.setNome(NOMES[rnd.nextInt(NOMES.length)] + " " + SOBRENOMES[rnd.nextInt(SOBRENOMES.length)]);
                fun.setCpf(String.format("%011d", cpfSeq++));
                fun.setEmail("func" + cpfSeq + "@farmacia.com.br");
                fun.setEndereco(new Endereco("Av. Principal", String.valueOf(10 + f), d[1], d[2], d[3], "40000-000"));
                fun.setCargo(cargos[f]);
                fun.setSalario(new BigDecimal(f == 0 ? "5500.00" : f < 3 ? "4200.00" : "1900.00"));
                fun.setLoja(loja);
                em.persist(fun);
                equipe.add(fun);
            }
            funcionariosPorLoja.put(loja.getId(), equipe);
        }
    }

    // ---------------------------------------------------------------- catalogo

    private void criarCatalogo() {
        List<Departamento> deptos = new ArrayList<>();
        for (String nome : DEPARTAMENTOS) {
            Departamento d = new Departamento(nome);
            em.persist(d);
            deptos.add(d);
        }
        List<Categoria> categorias = new ArrayList<>();
        for (int i = 0; i < CATEGORIAS.length; i++) {
            Categoria c = new Categoria(CATEGORIAS[i], deptos.get(DEPTO_DA_CATEGORIA[i]));
            em.persist(c);
            categorias.add(c);
        }
        List<Laboratorio> labsGen = new ArrayList<>();
        for (String nome : LABS_GENERICOS) {
            Laboratorio l = new Laboratorio(nome);
            em.persist(l);
            labsGen.add(l);
        }
        List<Laboratorio> labsRef = new ArrayList<>();
        for (String nome : LABS_REFERENCIA) {
            Laboratorio l = new Laboratorio(nome);
            em.persist(l);
            labsRef.add(l);
        }

        Map<String, PrincipioAtivo> ativos = new HashMap<>();
        for (String[] b : BASES) {
            PrincipioAtivo ativo = new PrincipioAtivo(b[0]);
            em.persist(ativo);
            ativos.put(b[0], ativo);
        }

        for (String[] b : BASES) {
            BigDecimal precoBase = new BigDecimal(b[3]);
            for (int v = 0; v < 2; v++) {
                Medicamento m = new Medicamento();
                boolean generico = v == 0;
                m.setNome(b[0] + (generico ? " Generico" : " Referencia"));
                m.setTarja(Tarja.valueOf(b[2]));
                m.setCategoria(categorias.get(Integer.parseInt(b[1])));
                BigDecimal preco = generico ? precoBase.multiply(new BigDecimal("0.65")) : precoBase;
                m.setPreco(preco.setScale(2, RoundingMode.HALF_UP));
                m.setLaboratorio(generico ? labsGen.get(rnd.nextInt(labsGen.size()))
                                          : labsRef.get(rnd.nextInt(labsRef.size())));
                m.getPrincipiosAtivos().add(ativos.get(b[0]));
                if ("2".equals(b[1])) { // antigripais combinam paracetamol com outro ativo
                    m.getPrincipiosAtivos().add(ativos.get("Paracetamol"));
                }
                em.persist(m);
                medicamentos.add(m);
                medPorNome.put(m.getNome(), m);
            }
        }
    }

    // ---------------------------------------------------------------- clientes

    private void criarClientes() {
        for (int i = 0; i < QTD_CLIENTES; i++) {
            Cliente c = new Cliente();
            String nome = NOMES[rnd.nextInt(NOMES.length)] + " " + SOBRENOMES[rnd.nextInt(SOBRENOMES.length)]
                + " " + SOBRENOMES[rnd.nextInt(SOBRENOMES.length)];
            c.setNome(nome);
            c.setCpf(String.format("%011d", 10000000000L + i * 7919L));
            c.setEmail("cliente" + i + "@email.com");
            String[] cidade = CIDADES[rnd.nextInt(CIDADES.length)];
            c.setEndereco(new Endereco("Rua " + SOBRENOMES[rnd.nextInt(SOBRENOMES.length)],
                String.valueOf(1 + rnd.nextInt(900)), "Centro", cidade[0], cidade[1],
                String.format("4%04d-000", rnd.nextInt(9999))));
            c.setDataNascimento(LocalDate.of(1950, 1, 1).plusDays(rnd.nextInt(55 * 365)));
            c.setDataCadastro(DATA_INICIAL.minusDays(rnd.nextInt(700)));
            em.persist(c);
            clientes.add(c);
        }
    }

    // ---------------------------------------------------------------- vendas

    private void criarVendas() {
        long dias = ChronoUnit.DAYS.between(DATA_INICIAL, DATA_FINAL);
        for (int i = 0; i < QTD_VENDAS; i++) {
            Venda venda = new Venda();
            venda.setDataVenda(DATA_INICIAL.plusDays((long) (rnd.nextDouble() * (dias + 1))));
            Loja loja = lojas.get(rnd.nextInt(lojas.size()));
            venda.setLoja(loja);
            List<Funcionario> equipe = funcionariosPorLoja.get(loja.getId());
            venda.setFuncionario(equipe.get(rnd.nextInt(equipe.size())));
            venda.setFormaPagamento(sortearPagamento());

            double sorteio = rnd.nextDouble();
            boolean controlada = false;
            Cliente cliente = clientes.get(rnd.nextInt(clientes.size()));

            if (sorteio < 0.20) { // gripe: antigripal + vitamina C
                String[] gripe = {"Resfenol", "Benegrip", "Coristina", "Naldecon", "Sinutab"};
                adicionar(venda, gripe[rnd.nextInt(gripe.length)], 1);
                adicionar(venda, "Vitamina C", 1);
                if (rnd.nextDouble() < 0.4) {
                    adicionar(venda, "Paracetamol", 1);
                }
            } else if (sorteio < 0.35) { // hipertensos recorrentes
                cliente = clientes.get(rnd.nextInt(30));
                adicionar(venda, "Losartana", 1 + rnd.nextInt(2));
                if (rnd.nextDouble() < 0.5) {
                    adicionar(venda, "Hidroclorotiazida", 1);
                }
            } else if (sorteio < 0.43) { // alergia
                String[] alergia = {"Loratadina", "Cetirizina", "Desloratadina"};
                adicionar(venda, alergia[rnd.nextInt(alergia.length)], 1);
                adicionar(venda, "Hidratante", 1);
            } else if (sorteio < 0.50) { // controlado (tarja preta) sempre com receita
                cliente = clientes.get(30 + rnd.nextInt(30));
                String[] pretos = {"Clonazepam", "Alprazolam", "Zolpidem", "Diazepam"};
                adicionar(venda, pretos[rnd.nextInt(pretos.length)], 1);
                if (rnd.nextDouble() < 0.4) {
                    adicionar(venda, "Melatonina", 1);
                }
                controlada = true;
            } else if (sorteio < 0.62) { // sol
                adicionar(venda, "Protetor Solar", 1);
                adicionar(venda, "Repelente", 1);
            } else if (sorteio < 0.72) { // higiene
                String[] higiene = {"Sabonete", "Shampoo", "Creme Dental", "Fio Dental", "Desodorante"};
                int n = 2 + rnd.nextInt(2);
                for (int k = 0; k < n; k++) {
                    adicionar(venda, higiene[(k + rnd.nextInt(higiene.length)) % higiene.length], 1 + rnd.nextInt(2));
                }
            } else { // cesta aleatoria
                int n = 1 + rnd.nextInt(4);
                for (int k = 0; k < n; k++) {
                    Medicamento m = medicamentos.get(rnd.nextInt(medicamentos.size()));
                    if (m.getTarja() == Tarja.PRETA) {
                        continue; // preta so entra pelo padrao com receita
                    }
                    adicionarMedicamento(venda, m, 1 + rnd.nextInt(2));
                }
                if (venda.getItens().isEmpty()) {
                    adicionar(venda, "Dipirona", 1);
                }
            }

            venda.setCliente(cliente);
            if (controlada) {
                Receita r = new Receita();
                r.setNomeMedico("Dr. " + SOBRENOMES[rnd.nextInt(SOBRENOMES.length)]);
                r.setCrmMedico("CRM-BA " + (10000 + rnd.nextInt(80000)));
                r.setDataEmissao(venda.getDataVenda().minusDays(rnd.nextInt(6)));
                venda.setReceita(r);
            }
            em.persist(venda); // cascade ALL grava itens e receita

            if (i % 200 == 199) {
                em.flush();
            }
        }
    }

    private void adicionar(Venda venda, String base, int quantidade) {
        // generico (65%) ou referencia (35%)
        String nome = base + (rnd.nextDouble() < 0.65 ? " Generico" : " Referencia");
        for (ItemVenda it : venda.getItens()) { // nao repete o mesmo produto na venda
            if (it.getMedicamento().getNome().startsWith(base + " ")) {
                return;
            }
        }
        adicionarMedicamento(venda, medPorNome.get(nome), quantidade);
    }

    private void adicionarMedicamento(Venda venda, Medicamento m, int quantidade) {
        for (ItemVenda it : venda.getItens()) {
            if (it.getMedicamento() == m) {
                return;
            }
        }
        int qtd = m.getTarja() == Tarja.PRETA ? 1 : quantidade;
        BigDecimal desconto = BigDecimal.ZERO;
        if (rnd.nextDouble() < 0.20) {
            BigDecimal percentual = new BigDecimal(5 + rnd.nextInt(11)).divide(new BigDecimal(100));
            desconto = m.getPreco().multiply(BigDecimal.valueOf(qtd)).multiply(percentual)
                .setScale(2, RoundingMode.HALF_UP);
        }
        venda.adicionarItem(new ItemVenda(m, qtd, m.getPreco(), desconto));
    }

    private FormaPagamento sortearPagamento() {
        int p = rnd.nextInt(100);
        if (p < 35) {
            return FormaPagamento.PIX;
        } else if (p < 65) {
            return FormaPagamento.CARTAO_CREDITO;
        } else if (p < 85) {
            return FormaPagamento.CARTAO_DEBITO;
        }
        return FormaPagamento.DINHEIRO;
    }
}
