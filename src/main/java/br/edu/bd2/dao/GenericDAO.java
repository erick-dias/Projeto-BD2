package br.edu.bd2.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 * DAO generico com CRUD. Cada operacao abre seu proprio EntityManager e controla
 * a transacao (commit em caso de sucesso, rollback em caso de erro).
 */
public class GenericDAO<T, ID> {

    private final Class<T> classe;

    public GenericDAO(Class<T> classe) {
        this.classe = classe;
    }

    /** Insere (persist). Preenche o id da instancia recebida. */
    public T salvar(T entidade) {
        EntityManager em = JPAUtil.criarEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entidade);
            tx.commit();
            return entidade;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** Atualiza (merge) uma entidade destacada e devolve a instancia gerenciada. */
    public T atualizar(T entidade) {
        EntityManager em = JPAUtil.criarEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T gerenciada = em.merge(entidade);
            tx.commit();
            return gerenciada;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public T buscarPorId(ID id) {
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            return em.find(classe, id);
        } finally {
            em.close();
        }
    }

    public List<T> listarTodos() {
        EntityManager em = JPAUtil.criarEntityManager();
        try {
            return em.createQuery("SELECT e FROM " + classe.getSimpleName() + " e", classe).getResultList();
        } finally {
            em.close();
        }
    }

    /** Remove pelo id. Retorna false se nao existir. */
    public boolean remover(ID id) {
        EntityManager em = JPAUtil.criarEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entidade = em.find(classe, id);
            if (entidade == null) {
                tx.rollback();
                return false;
            }
            em.remove(entidade);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
