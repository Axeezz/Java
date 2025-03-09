package com.movio.moviolab.dao;

import com.movio.moviolab.models.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<User> findAll() {
        return entityManager.createQuery("SELECT u FROM User u", User.class)
                .getResultList();
    }

    public Optional<User> findById(Integer id) {
        User user = entityManager.find(User.class, id);
        return Optional.ofNullable(user);
    }

    public List<User> findByNameIgnoreCase(String name) {
        return entityManager.createQuery("SELECT u FROM User u WHERE LOWER(u.name) = LOWER(:name)",
                        User.class)
                .setParameter("name", name)
                .getResultList();
    }

    public List<User> findByEmailIgnoreCase(String email) {
        return entityManager.createQuery("SELECT u FROM User u"
                        + " WHERE LOWER(u.email) = LOWER(:email)", User.class)
                .setParameter("email", email)
                .getResultList();
    }

    public List<User> findByNameIgnoreCaseAndEmailIgnoreCase(String name, String email) {
        return entityManager.createQuery("SELECT u FROM User u WHERE LOWER(u.name) = LOWER(:name)"
                        + "AND LOWER(u.email) = LOWER(:email)", User.class)
                .setParameter("name", name).setParameter("email", email).getResultList();
    }

    public boolean existsById(Integer id) {
        return findById(id).isPresent();
    }

    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);  // для новых пользователей
            return user;
        } else {
            return entityManager.merge(user);  // для обновленных пользователей
        }
    }

    @Transactional
    public void deleteById(Integer id) {
        Optional<User> user = findById(id);
        user.ifPresent(entityManager::remove);  // Если пользователь найден, удаляем его
    }
}

