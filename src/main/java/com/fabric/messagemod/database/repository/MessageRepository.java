package com.fabric.messagemod.database.repository;

import com.fabric.messagemod.database.entity.MessageEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.UUID;

public class MessageRepository {
    private final EntityManagerFactory emf;

    public MessageRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void save(UUID playerUuid, String text) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            var message = new MessageEntity(playerUuid, text);
            em.persist(message);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
