package dat.daos;

import dat.dto.GenreDTO;
import dat.entities.Genre;
import dat.entities.Movie;
import dat.exceptions.ApiException;
import dat.services.DTOMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

public class GenreDAO implements IDAO<Genre, Long> {
    private static EntityManagerFactory emf;
    private static GenreDAO instance = null;

    public GenreDAO() {
    }

    public static GenreDAO getInstance(EntityManagerFactory _emf) {
        if (emf == null) {
            emf = _emf;
            instance = new GenreDAO();
        }
        return instance;
    }

    public Genre saveFromDTO(GenreDTO genreDTO) {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                em.getTransaction().begin();

                Genre genre = em.find(Genre.class, genreDTO.getId());
                if (genre != null) {
                    System.out.println("Genre already exists: " + genre.getName());
                    em.getTransaction().commit();
                    return genre;
                }

                genre = DTOMapper.genreToEntity(genreDTO);
                em.persist(genre);
                em.getTransaction().commit();
                System.out.println("New genre saved: " + genre.getName());

                return genre;
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw new ApiException(401, "An error occurred while saving the genre: " + e.getMessage());
            }
        }
    }


    @Override
    public Genre create(Genre genre) {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                em.getTransaction().begin();
                em.persist(genre);
                em.getTransaction().commit();
                return genre;
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw new ApiException(401, "An error occurred while saving the genre");
            }
        }
    }

    @Override
    public Genre read(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Genre.class, id);
        }
    }

    @Override
    public List<Genre> readAll() {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT g FROM Genre g", Genre.class).getResultList();
        }
    }

    @Override
    public Genre update(Genre genre) {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                em.getTransaction().begin();
                Genre updatedGenre = em.merge(genre);
                em.getTransaction().commit();
                return updatedGenre;
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw new ApiException(401, "An error occurred while updating the genre");
            }
        }
    }

    @Override
    public void delete(Long id) {
        try (EntityManager em = emf.createEntityManager()) {
            try {
                em.getTransaction().begin();
                Genre genre = em.find(Genre.class, id);
                if (genre != null) {
                    em.remove(genre);
                }
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw new ApiException(401, "An error occurred while deleting the genre");
            }
        }
    }

    public List<Movie> getMoviesByGenre(String genreName) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("SELECT DISTINCT m FROM Movie m " +
                            "JOIN FETCH m.genres g " +
                            "LEFT JOIN FETCH m.actor " +
                            "LEFT JOIN FETCH m.director " +
                            "WHERE LOWER(g.name) = LOWER(:genre)", Movie.class)
                    .setParameter("genre", genreName.toLowerCase())
                    .getResultList();
        }
    }
}
