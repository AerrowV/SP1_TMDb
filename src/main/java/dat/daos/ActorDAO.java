package dat.daos;

import dat.dto.ActorDTO;
import dat.entities.Actor;
import dat.exceptions.ApiException;
import dat.services.DTOMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class ActorDAO implements IDAO<Actor, Long> {
    private static EntityManagerFactory emf;
    private static ActorDAO instance = null;

    public ActorDAO() {
    }

    public static ActorDAO getInstance(EntityManagerFactory _emf) {
        if (emf == null) {
            emf = _emf;
            instance = new ActorDAO();
        }
        return instance;
    }

    public Actor saveActorFromDTO(ActorDTO actorDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                em.getTransaction().begin();

                Actor actor = em.find(Actor.class, actorDTO.getId());
                if (actor != null) {
                    System.out.println("Actor already exists: " + actor.getName());
                    em.getTransaction().commit();
                    return actor;
                }

                actor = DTOMapper.actorToEntity(actorDTO);
                em.persist(actor);
                em.getTransaction().commit();
                System.out.println("New actor saved: " + actor.getName());

                return actor;
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw new ApiException(401, "An error occurred while saving the actor: " + e.getMessage());
            }
        }
    }

    @Override
    public Actor create(Actor actor) {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                em.getTransaction().begin();
                em.persist(actor);
                em.getTransaction().commit();
                return actor;
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw new ApiException(401, "An error occurred while saving the actor");
            }
        }
    }

    @Override
    public Actor read(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Actor.class, id);
        }
    }

    @Override
    public List<Actor> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT a FROM Actor a", Actor.class).getResultList();
        }
    }

    @Override
    public Actor update(Actor actor) {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                em.getTransaction().begin();
                Actor updatedActor = em.merge(actor);
                em.getTransaction().commit();
                return updatedActor;
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw new ApiException(401, "An error occurred while updating the actor");
            }
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                em.getTransaction().begin();
                Actor actor = em.find(Actor.class, id);
                if (actor != null) {
                    em.remove(actor);
                }
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw new ApiException(401, "An error occurred while deleting the actor");
            }
        }
    }
}